package com.heqichang.dailymoney2.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.heqichang.dailymoney2.presentation.navigation.DailyMoneyNavHost
import com.heqichang.dailymoney2.ui.theme.DailyMoney2Theme

@Composable
fun DailyMoneyApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    DailyMoney2Theme {
        DailyMoneyNavHost(
            navController = navController,
            modifier = modifier
        )
    }
}

