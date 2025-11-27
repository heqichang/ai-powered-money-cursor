package com.heqichang.dailymoney2.presentation.transaction

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.heqichang.dailymoney2.presentation.shared.LedgerSelectionViewModel
import java.text.DecimalFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToLong

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

@Composable
fun TransactionEntryRoute(
    ledgerId: Long,
    transactionId: Long? = null,
    transactionDate: LocalDate? = null,
    selectionViewModel: LedgerSelectionViewModel,
    onBack: () -> Unit,
    viewModel: TransactionEntryViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val dateToLoad = transactionDate ?: LocalDate.now()
    
    LaunchedEffect(ledgerId, dateToLoad) {
        viewModel.setLedgerAndDate(ledgerId, dateToLoad)
        selectionViewModel.selectLedger(ledgerId)
    }
    
    // 等待交易记录加载完成后，如果有 transactionId，加载该交易记录进行编辑
    LaunchedEffect(transactionId, uiState.transactions, uiState.isLoadingTransactions) {
        if (transactionId != null && !uiState.isLoadingTransactions) {
            // 在已加载的交易记录中查找
            uiState.transactions.firstOrNull { it.id == transactionId }?.let { transaction ->
                viewModel.onAction(TransactionEntryAction.EditTransaction(transaction))
            }
        }
    }
    
    // 监听保存成功，自动返回
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onBack()
            // 重置标志，避免重复触发
            viewModel.onAction(TransactionEntryAction.ResetSaveSuccess)
        }
    }
    
    TransactionEntryScreen(
        state = uiState,
        onBack = onBack,
        onAction = viewModel::onAction
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TransactionEntryScreen(
    state: TransactionEntryUiState,
    onBack: () -> Unit,
    onAction: (TransactionEntryAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentDate = state.selectedDate ?: LocalDate.now()
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val amountFocusRequester = remember { FocusRequester() }
    var amountInput by rememberSaveable(state.draft.id) {
        mutableStateOf("")
    }
    var isUserInputting by rememberSaveable { mutableStateOf(false) }
    
    // 只在非用户输入状态下更新输入框（例如编辑模式加载数据时）
    LaunchedEffect(state.draft.amountInCents, state.draft.id) {
        if (!isUserInputting) {
            val newValue = kotlin.math.abs(state.draft.amountInCents).toCurrencyInputString()
            if (newValue != amountInput) {
                amountInput = newValue
            }
        }
    }
    
    // 进入页面时自动聚焦到金额输入框
    LaunchedEffect(Unit) {
        amountFocusRequester.requestFocus()
    }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("记一笔") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = {
                    onAction(TransactionEntryAction.ChangeDate(currentDate.minusDays(1)))
                }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "前一天")
                }
                Text(
                    text = currentDate.format(dateFormatter),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showDatePicker = true },
                    fontWeight = FontWeight.Medium
                )
                IconButton(onClick = {
                    onAction(TransactionEntryAction.ChangeDate(currentDate.plusDays(1)))
                }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "后一天")
                }
            }
            
            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = currentDate
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                )
                
                AlertDialog(
                    onDismissRequest = { showDatePicker = false },
                    title = { Text("选择日期") },
                    text = {
                        DatePicker(state = datePickerState)
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    val selectedDate = Instant.ofEpochMilli(millis)
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate()
                                    onAction(TransactionEntryAction.ChangeDate(selectedDate))
                                }
                                showDatePicker = false
                            }
                        ) {
                            Text("确定")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("取消")
                        }
                    }
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "选择类目", style = MaterialTheme.typography.titleMedium)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.categories) { category ->
                        FilterChip(
                            selected = category.id == state.draft.categoryId,
                            onClick = { 
                                onAction(TransactionEntryAction.SelectCategory(category.id, category.type))
                            },
                            label = { Text(category.name) }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = amountInput,
                onValueChange = { newInput ->
                    isUserInputting = true
                    val filtered = newInput.filterAmountInput()
                    if (filtered != amountInput) {
                        amountInput = filtered
                        filtered.toCurrencyCents()?.let { cents ->
                            onAction(TransactionEntryAction.UpdateAmount(cents))
                        }
                    }
                },
                label = { Text("金额") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(amountFocusRequester)
            )
            
            // 延迟重置用户输入标志
            LaunchedEffect(amountInput) {
                kotlinx.coroutines.delay(200)
                isUserInputting = false
            }

            OutlinedTextField(
                value = state.draft.note.orEmpty(),
                onValueChange = { onAction(TransactionEntryAction.UpdateNote(it)) },
                label = { Text("备注") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onAction(TransactionEntryAction.SaveDraft) },
                    enabled = state.draft.categoryId != null && state.ledgerId != null,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("保存")
                }
                if (state.draft.id != null) {
                    // 编辑模式下显示删除按钮
                    Button(
                        onClick = { 
                            state.draft.id?.let { id ->
                                onAction(TransactionEntryAction.DeleteTransaction(id))
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("删除")
                    }
                } else {
                    // 新建模式下显示清空按钮
                    TextButton(onClick = { onAction(TransactionEntryAction.ClearDraft) }) {
                        Text("清空")
                    }
                }
            }

            DailySummaryCard(
                incomeInCents = state.transactions.filter { it.amountInCents >= 0 }.sumOf { it.amountInCents },
                expenseInCents = state.transactions.filter { it.amountInCents < 0 }.sumOf { it.amountInCents }
            )

            state.errorMessage?.let { error ->
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun DailySummaryCard(
    incomeInCents: Long,
    expenseInCents: Long,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("今日汇总", style = MaterialTheme.typography.titleMedium)
            Text("收入：${incomeInCents.toCurrencyDisplay()}", color = MaterialTheme.colorScheme.secondary)
            Text("支出：${expenseInCents.toCurrencyDisplay()}", color = MaterialTheme.colorScheme.error)
            Text("净额：${(incomeInCents + expenseInCents).toCurrencyDisplay()}")
        }
    }
}

private fun Long.toCurrencyInputString(): String {
    if (this == 0L) return ""
    val value = this / 100.0
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        String.format("%.2f", value).trimEnd('0').trimEnd('.')
    }
}

private fun Long.toCurrencyDisplay(): String = DecimalFormat("#,##0.00").format(this / 100.0)

private fun String.toCurrencyCents(): Long? {
    val sanitized = replace(",", "")
    return sanitized.toDoubleOrNull()?.let { (it * 100).roundToLong() }
}

/**
 * 过滤金额输入，只允许数字和小数点，并限制小数部分最多两位
 */
private fun String.filterAmountInput(): String {
    // 移除所有非数字和非小数点的字符
    var filtered = filter { it.isDigit() || it == '.' }
    
    // 确保只有一个小数点
    val dotIndex = filtered.indexOf('.')
    if (dotIndex >= 0) {
        val integerPart = filtered.substring(0, dotIndex)
        val decimalPart = filtered.substring(dotIndex + 1).take(2) // 最多两位小数
        filtered = if (decimalPart.isNotEmpty()) {
            "$integerPart.$decimalPart"
        } else {
            "$integerPart."
        }
    }
    
    // 如果以小数点开头，在前面加0
    if (filtered.startsWith(".")) {
        filtered = "0$filtered"
    }
    
    // 移除前导零（但保留单个0或0.xx）
    if (filtered.length > 1 && filtered.startsWith("0") && filtered[1] != '.') {
        filtered = filtered.trimStart('0')
        if (filtered.isEmpty() || filtered.startsWith(".")) {
            filtered = "0$filtered"
        }
    }
    
    return filtered
}

