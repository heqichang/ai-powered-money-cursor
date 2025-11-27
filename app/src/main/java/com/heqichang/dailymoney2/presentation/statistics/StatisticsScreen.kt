package com.heqichang.dailymoney2.presentation.statistics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.heqichang.dailymoney2.domain.model.MoneyTransaction
import com.heqichang.dailymoney2.presentation.shared.LedgerSelectionViewModel
import java.text.DecimalFormat
import java.time.Year
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val yearFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy")
private val monthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("M月. yyyy", Locale.CHINESE)
private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d")
private val dayOfWeekFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("E", Locale.CHINESE)

data class MonthGroup(
    val yearMonth: YearMonth,
    val transactions: List<MoneyTransaction>,
    val incomeInCents: Long,
    val expenseInCents: Long
) {
    val netInCents: Long = incomeInCents + expenseInCents
}

@Composable
fun StatisticsRoute(
    ledgerId: Long,
    selectionViewModel: LedgerSelectionViewModel,
    onBack: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    LaunchedEffect(ledgerId) {
        viewModel.setLedger(ledgerId)
        selectionViewModel.selectLedger(ledgerId)
    }
    StatisticsScreen(
        state = uiState,
        onBack = onBack,
        onAction = viewModel::onAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    state: StatisticsUiState,
    onBack: () -> Unit,
    onAction: (StatisticsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("统计") },
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
            // 年份选择
            if (state.yearlyStats.isNotEmpty()) {
                val selected = state.selectedYear ?: state.yearlyStats.first().year
                FlowingYearChips(
                    stats = state.yearlyStats,
                    selected = selected,
                    onSelect = { onAction(StatisticsAction.SelectYear(it)) }
                )
                
                // 当年支出总额
                val selectedYearStats = state.yearlyStats.firstOrNull { it.year == selected }
                selectedYearStats?.let { stats ->
                    YearlyExpenseCard(
                        year = stats.year,
                        expenseInCents = stats.expenseInCents
                    )
                }
            }
            
            // 交易列表
            if (state.transactions.isEmpty()) {
                Text("暂无数据", style = MaterialTheme.typography.bodyLarge)
            } else {
                val selectedYear = state.selectedYear ?: state.yearlyStats.firstOrNull()?.year
                val monthGroups = groupTransactionsByMonth(
                    transactions = state.transactions,
                    selectedYear = selectedYear
                )
                
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(monthGroups) { monthGroup ->
                        MonthGroupItem(
                            monthGroup = monthGroup,
                            isExpanded = state.expandedMonths.contains(monthGroup.yearMonth),
                            onToggleExpanded = { onAction(StatisticsAction.ToggleMonthExpanded(monthGroup.yearMonth)) },
                            getCategoryName = { state.getCategoryName(it) }
                        )
                    }
                }
            }
            
            state.errorMessage?.let { error ->
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun FlowingYearChips(
    stats: List<YearlyStats>,
    selected: Year,
    onSelect: (Year) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        stats.forEach { stat ->
            FilterChip(
                selected = stat.year == selected,
                onClick = { onSelect(stat.year) },
                label = { Text(stat.year.format(yearFormatter)) }
            )
        }
    }
}

@Composable
private fun MonthGroupItem(
    monthGroup: MonthGroup,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    getCategoryName: (Long?) -> String?
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column {
            // 月份标题行
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpanded() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = monthGroup.yearMonth.format(monthFormatter),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "结余 ${monthGroup.netInCents.toCurrencyDisplay()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (monthGroup.netInCents >= 0) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "收起" else "展开"
                    )
                }
            }
            
            // 交易列表
            if (isExpanded) {
                val transactionsByDate = monthGroup.transactions.groupBy { it.occurredOn }
                    .toList()
                    .sortedByDescending { it.first }
                
                transactionsByDate.forEach { (date, transactions) ->
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        // 日期标题
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = date.format(dateFormatter),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "(${date.format(dayOfWeekFormatter)})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // 该日期的交易
                        transactions.forEach { transaction ->
                            TransactionItem(
                                transaction = transaction,
                                categoryName = getCategoryName(transaction.categoryId),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun YearlyExpenseCard(
    year: Year,
    expenseInCents: Long,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${year.value}年总支出",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = expenseInCents.toExpenseDisplay(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: MoneyTransaction,
    categoryName: String?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = categoryName ?: "未分类",
                style = MaterialTheme.typography.bodyLarge
            )
            if (!transaction.note.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = transaction.note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Text(
            text = transaction.amountInCents.toCurrencyDisplay(),
            style = MaterialTheme.typography.titleMedium,
            color = if (transaction.amountInCents >= 0) {
                MaterialTheme.colorScheme.secondary
            } else {
                MaterialTheme.colorScheme.error
            }
        )
    }
}

private fun groupTransactionsByMonth(
    transactions: List<MoneyTransaction>,
    selectedYear: Year?
): List<MonthGroup> {
    val filtered = if (selectedYear != null) {
        transactions.filter { Year.from(it.occurredOn) == selectedYear }
    } else {
        transactions
    }
    
    return filtered.groupBy { YearMonth.from(it.occurredOn) }
        .map { (yearMonth, monthTransactions) ->
            val sortedTransactions = monthTransactions.sortedWith(
                compareByDescending<MoneyTransaction> { it.occurredOn }
                    .thenByDescending { it.createdAtEpochMillis }
            )
            
            val income = sortedTransactions.filter { it.amountInCents >= 0 }
                .sumOf { it.amountInCents }
            val expense = sortedTransactions.filter { it.amountInCents < 0 }
                .sumOf { it.amountInCents }
            
            MonthGroup(
                yearMonth = yearMonth,
                transactions = sortedTransactions,
                incomeInCents = income,
                expenseInCents = expense
            )
        }
        .sortedByDescending { it.yearMonth }
}

private fun Long.toCurrencyDisplay(): String {
    val amount = this / 100.0
    val sign = if (this >= 0) "+" else ""
    return "$sign${DecimalFormat("#,##0.00").format(amount)}"
}

private fun Long.toExpenseDisplay(): String {
    val amount = kotlin.math.abs(this) / 100.0
    return DecimalFormat("#,##0.00").format(amount)
}
