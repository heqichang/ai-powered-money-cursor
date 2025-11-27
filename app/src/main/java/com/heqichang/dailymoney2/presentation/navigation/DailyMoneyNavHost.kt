package com.heqichang.dailymoney2.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import java.time.LocalDate
import com.heqichang.dailymoney2.data.local.PreferencesManager
import com.heqichang.dailymoney2.presentation.category.CategoryManagerRoute
import com.heqichang.dailymoney2.presentation.ledger.detail.LedgerDetailRoute
import com.heqichang.dailymoney2.presentation.ledger.list.LedgerListRoute
import com.heqichang.dailymoney2.presentation.ledger.list.LedgerListViewModel
import com.heqichang.dailymoney2.presentation.shared.LedgerSelectionViewModel
import com.heqichang.dailymoney2.presentation.statistics.StatisticsRoute
import com.heqichang.dailymoney2.presentation.transaction.TransactionEntryRoute
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

// 用于在 Composable 中注入 PreferencesManager 的辅助类
@HiltViewModel
class PreferencesManagerHolder @Inject constructor(
    val preferencesManager: PreferencesManager
) : ViewModel()

sealed class DailyMoneyDestination(val route: String) {
    data object LedgerList : DailyMoneyDestination("ledger_list")

    data object LedgerDetail : DailyMoneyDestination("ledger_detail/{ledgerId}") {
        const val LEDGER_ARG = "ledgerId"
        fun createRoute(ledgerId: Long) = "ledger_detail/$ledgerId"
    }

    data object CategoryManager : DailyMoneyDestination("category_manager/{ledgerId}") {
        const val LEDGER_ARG = "ledgerId"
        fun createRoute(ledgerId: Long) = "category_manager/$ledgerId"
    }

    data object TransactionEntry : DailyMoneyDestination("transaction_entry/{ledgerId}") {
        const val LEDGER_ARG = "ledgerId"
        fun createRoute(ledgerId: Long) = "transaction_entry/$ledgerId"
    }
    
    data object TransactionEntryEdit : DailyMoneyDestination("transaction_entry/{ledgerId}/{transactionId}/{date}") {
        const val LEDGER_ARG = "ledgerId"
        const val TRANSACTION_ARG = "transactionId"
        const val DATE_ARG = "date"
        fun createRoute(ledgerId: Long, transactionId: Long, date: String) = "transaction_entry/$ledgerId/$transactionId/$date"
    }

    data object Statistics : DailyMoneyDestination("statistics/{ledgerId}") {
        const val LEDGER_ARG = "ledgerId"
        fun createRoute(ledgerId: Long) = "statistics/$ledgerId"
    }
}

@Composable
fun DailyMoneyNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val selectionViewModel: LedgerSelectionViewModel = hiltViewModel()
    val ledgerListViewModel: LedgerListViewModel = hiltViewModel()
    val preferencesManagerHolder: PreferencesManagerHolder = hiltViewModel()
    val preferencesManager = preferencesManagerHolder.preferencesManager
    val ledgerListState = ledgerListViewModel.uiState.collectAsStateWithLifecycle().value
    var hasInitialized by remember { mutableStateOf(false) }
    
    // 应用启动时检查是否有上次打开的账本，在 NavHost 创建后立即导航
    LaunchedEffect(ledgerListState.ledgers) {
        if (ledgerListState.ledgers.isNotEmpty() && !hasInitialized) {
            val lastLedgerId = preferencesManager.getLastLedgerId()
            if (lastLedgerId != null && ledgerListState.ledgers.any { it.id == lastLedgerId }) {
                hasInitialized = true
                // 等待 NavHost 完全初始化后再导航
                // 使用 withFrameNanos 确保在下一帧执行
                kotlinx.coroutines.delay(16) // 约一帧的时间（60fps = 16ms）
                val currentRoute = navController.currentBackStackEntry?.destination?.route
                if (currentRoute == DailyMoneyDestination.LedgerList.route) {
                    navController.navigate(DailyMoneyDestination.LedgerDetail.createRoute(lastLedgerId)) {
                        launchSingleTop = true
                    }
                }
            } else {
                hasInitialized = true
            }
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = DailyMoneyDestination.LedgerList.route,
        modifier = modifier
    ) {
        composable(DailyMoneyDestination.LedgerList.route) {
            LedgerListRoute(
                selectionViewModel = selectionViewModel,
                onNavigateToLedgerDetail = { ledgerId ->
                    navController.navigate(DailyMoneyDestination.LedgerDetail.createRoute(ledgerId))
                }
            )
        }
        composable(
            route = DailyMoneyDestination.LedgerDetail.route,
            arguments = listOf(
                navArgument(DailyMoneyDestination.LedgerDetail.LEDGER_ARG) {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val ledgerId = backStackEntry.arguments?.getLong(DailyMoneyDestination.LedgerDetail.LEDGER_ARG)
                ?: return@composable
            LedgerDetailRoute(
                ledgerId = ledgerId,
                selectionViewModel = selectionViewModel,
                onBack = {
                    // 如果返回栈中有页面，则返回；否则导航到账本列表页
                    if (!navController.popBackStack()) {
                        navController.navigate(DailyMoneyDestination.LedgerList.route) {
                            // 清除返回栈，使账本列表页成为根页面
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onAddTransaction = { ledgerId ->
                    navController.navigate(DailyMoneyDestination.TransactionEntry.createRoute(ledgerId))
                },
                onEditTransaction = { ledgerId, transactionId, date ->
                    navController.navigate(DailyMoneyDestination.TransactionEntryEdit.createRoute(ledgerId, transactionId, date.toString()))
                },
                onNavigateToCategoryManager = { ledgerId ->
                    navController.navigate(DailyMoneyDestination.CategoryManager.createRoute(ledgerId))
                },
                onNavigateToStatistics = { ledgerId ->
                    navController.navigate(DailyMoneyDestination.Statistics.createRoute(ledgerId))
                },
                preferencesManager = preferencesManager
            )
        }
        composable(
            route = DailyMoneyDestination.CategoryManager.route,
            arguments = listOf(
                navArgument(DailyMoneyDestination.CategoryManager.LEDGER_ARG) {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val ledgerId = backStackEntry.arguments?.getLong(DailyMoneyDestination.CategoryManager.LEDGER_ARG)
                ?: return@composable
            CategoryManagerRoute(
                ledgerId = ledgerId,
                selectionViewModel = selectionViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = DailyMoneyDestination.TransactionEntry.route,
            arguments = listOf(
                navArgument(DailyMoneyDestination.TransactionEntry.LEDGER_ARG) {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val ledgerId = backStackEntry.arguments?.getLong(DailyMoneyDestination.TransactionEntry.LEDGER_ARG)
                ?: return@composable
            TransactionEntryRoute(
                ledgerId = ledgerId,
                transactionId = null,
                selectionViewModel = selectionViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = DailyMoneyDestination.TransactionEntryEdit.route,
            arguments = listOf(
                navArgument(DailyMoneyDestination.TransactionEntryEdit.LEDGER_ARG) {
                    type = NavType.LongType
                },
                navArgument(DailyMoneyDestination.TransactionEntryEdit.TRANSACTION_ARG) {
                    type = NavType.LongType
                },
                navArgument(DailyMoneyDestination.TransactionEntryEdit.DATE_ARG) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val ledgerId = backStackEntry.arguments?.getLong(DailyMoneyDestination.TransactionEntryEdit.LEDGER_ARG)
                ?: return@composable
            val transactionId = backStackEntry.arguments?.getLong(DailyMoneyDestination.TransactionEntryEdit.TRANSACTION_ARG)
                ?: return@composable
            val dateString = backStackEntry.arguments?.getString(DailyMoneyDestination.TransactionEntryEdit.DATE_ARG)
                ?: return@composable
            val date = try {
                LocalDate.parse(dateString)
            } catch (e: Exception) {
                return@composable
            }
            TransactionEntryRoute(
                ledgerId = ledgerId,
                transactionId = transactionId,
                transactionDate = date,
                selectionViewModel = selectionViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = DailyMoneyDestination.Statistics.route,
            arguments = listOf(
                navArgument(DailyMoneyDestination.Statistics.LEDGER_ARG) {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val ledgerId = backStackEntry.arguments?.getLong(DailyMoneyDestination.Statistics.LEDGER_ARG)
                ?: return@composable
            StatisticsRoute(
                ledgerId = ledgerId,
                selectionViewModel = selectionViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

