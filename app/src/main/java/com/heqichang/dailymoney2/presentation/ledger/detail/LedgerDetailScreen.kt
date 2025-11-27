package com.heqichang.dailymoney2.presentation.ledger.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Download
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.heqichang.dailymoney2.data.local.PreferencesManager
import com.heqichang.dailymoney2.domain.model.MoneyTransaction
import com.heqichang.dailymoney2.presentation.shared.LedgerSelectionViewModel
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd HH:mm")

@Composable
fun LedgerDetailRoute(
    ledgerId: Long,
    selectionViewModel: LedgerSelectionViewModel,
    onBack: () -> Unit,
    onAddTransaction: (Long) -> Unit,
    onEditTransaction: (Long, Long, LocalDate) -> Unit,
    onNavigateToCategoryManager: (Long) -> Unit,
    onNavigateToStatistics: (Long) -> Unit,
    preferencesManager: PreferencesManager,
    viewModel: LedgerDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val coroutineScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main)
    
    // 文件保存器
    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            coroutineScope.launch {
                val result = viewModel.exportLedgerData()
                result.onSuccess { jsonString ->
                    try {
                        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                            outputStream.write(jsonString.toByteArray(Charsets.UTF_8))
                        }
                    } catch (e: Exception) {
                        // 处理错误
                    }
                }
            }
        }
    }
    LaunchedEffect(ledgerId) {
        viewModel.setLedger(ledgerId)
        selectionViewModel.selectLedger(ledgerId)
        // 保存当前打开的账本ID
        preferencesManager.saveLastLedgerId(ledgerId)
    }
    
    // 监听账本删除成功，导航回账本列表
    LaunchedEffect(uiState.ledgerDeleted) {
        if (uiState.ledgerDeleted) {
            // 清除保存的账本ID
            preferencesManager.clearLastLedgerId()
            // 导航回账本列表
            onBack()
        }
    }
    
    LedgerDetailScreen(
        state = uiState,
        ledgerId = ledgerId,
        onBack = onBack,
        onAddTransaction = { onAddTransaction(ledgerId) },
        onEditTransaction = { transactionId, date -> onEditTransaction(ledgerId, transactionId, date) },
        onNavigateToCategoryManager = { onNavigateToCategoryManager(ledgerId) },
        onNavigateToStatistics = { onNavigateToStatistics(ledgerId) },
        onDeleteLedger = { viewModel.deleteLedger() },
        onLoadMore = { viewModel.loadMoreTransactions() },
        onExportLedger = {
            val fileName = "${uiState.ledgerName}_${System.currentTimeMillis()}.json"
            saveFileLauncher.launch(fileName)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerDetailScreen(
    state: LedgerDetailUiState,
    ledgerId: Long,
    onBack: () -> Unit,
    onAddTransaction: () -> Unit,
    onEditTransaction: (Long, LocalDate) -> Unit,
    onNavigateToCategoryManager: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onDeleteLedger: () -> Unit,
    onLoadMore: () -> Unit,
    onExportLedger: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteConfirmDialog by rememberSaveable { mutableStateOf(false) }
    
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(state.ledgerName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToStatistics) {
                        Icon(Icons.Default.BarChart, contentDescription = "统计")
                    }
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.transactions.isEmpty() && !state.isLoading) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "还没有记录",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                val listState = rememberLazyListState()
                
                // 监听滚动接近底部（提前5个item加载）
                LaunchedEffect(listState, state.transactions.size, state.hasMore, state.isLoadingMore) {
                    snapshotFlow { 
                        listState.firstVisibleItemIndex to listState.layoutInfo
                    }.collect { (firstVisibleIndex, layoutInfo) ->
                        val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
                        val totalItems = layoutInfo.totalItemsCount
                        
                        if (lastVisibleItem != null && totalItems > 0 && state.hasMore && !state.isLoadingMore) {
                            // 当最后一个可见item距离底部还有5个item时，触发加载
                            val remainingItems = totalItems - 1 - lastVisibleItem.index
                            if (remainingItems <= 5) {
                                onLoadMore()
                            }
                        }
                    }
                }
                
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                ) {
                    // 统计信息卡片
                    item {
                        StatisticsCard(
                            thisYearExpense = state.thisYearExpense,
                            thisMonthExpense = state.thisMonthExpense,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    
                    items(state.transactions) { transaction ->
                        TransactionListItem(
                            transaction = transaction,
                            categoryName = state.getCategoryName(transaction.categoryId),
                            onClick = { 
                                onEditTransaction(transaction.id, transaction.occurredOn)
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                    
                    // 加载更多指示器
                    if (state.isLoadingMore) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "加载中...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // 记一笔按钮
            Button(
                onClick = onAddTransaction,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.NoteAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("记一笔")
            }

            state.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
    
    if (showSettingsDialog) {
        SettingsDialog(
            onDismiss = { showSettingsDialog = false },
            onManageCategory = {
                showSettingsDialog = false
                onNavigateToCategoryManager()
            },
            onDeleteLedger = {
                showSettingsDialog = false
                showDeleteConfirmDialog = true
            },
            onExportLedger = {
                showSettingsDialog = false
                onExportLedger()
            }
        )
    }
    
    if (showDeleteConfirmDialog) {
        DeleteLedgerConfirmDialog(
            ledgerName = state.ledgerName,
            onDismiss = { showDeleteConfirmDialog = false },
            onConfirm = {
                showDeleteConfirmDialog = false
                onDeleteLedger()
            }
        )
    }
}

@Composable
private fun StatisticsCard(
    thisYearExpense: Long,
    thisMonthExpense: Long,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "今年总支出",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = thisYearExpense.toExpenseDisplay(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "本月总支出",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = thisMonthExpense.toExpenseDisplay(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionListItem(
    transaction: MoneyTransaction,
    categoryName: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = categoryName ?: "未分类",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = transaction.occurredOn.format(dateFormatter),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!transaction.note.isNullOrBlank()) {
                    Spacer(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = transaction.note,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = transaction.amountInCents.toCurrencyDisplay(),
                style = MaterialTheme.typography.titleLarge,
                color = if (transaction.amountInCents >= 0) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }
    }
}

@Composable
private fun SettingsDialog(
    onDismiss: () -> Unit,
    onManageCategory: () -> Unit,
    onDeleteLedger: () -> Unit,
    onExportLedger: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("设置") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onManageCategory,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.List, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("类别管理")
                }
                Button(
                    onClick = {
                        onExportLedger()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("导出账本数据")
                }
                Button(
                    onClick = onDeleteLedger,
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("删除账本")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

@Composable
private fun DeleteLedgerConfirmDialog(
    ledgerName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("删除账本") },
        text = {
            Text("确定要删除账本「$ledgerName」吗？此操作将删除该账本及其所有交易记录，且无法恢复。")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    "删除",
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

private fun Long.toCurrencyDisplay(): String {
    val amount = this / 100.0
    val sign = if (this >= 0) "+" else ""
    return "$sign${DecimalFormat("#,##0.00").format(amount)}"
}

private fun Long.toExpenseDisplay(): String {
    val amount = this / 100.0
    return DecimalFormat("#,##0.00").format(amount)
}

