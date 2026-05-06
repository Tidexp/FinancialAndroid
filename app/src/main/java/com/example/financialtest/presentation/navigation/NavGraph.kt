package com.example.financialtest.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.financialtest.presentation.screen.*
import com.example.financialtest.presentation.viewmodel.FinancialViewModel
import com.example.financialtest.domain.model.AccountType

@Composable
fun NavGraph(navController: NavHostController) {
    val viewModel: FinancialViewModel = viewModel()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Accounts.route
    ) {
        composable(Screen.Accounts.route) {
            AccountsScreen(
                viewModel = viewModel,
                onAddAccountClick = {
                    navController.navigate(Screen.SelectAccountType.route)
                }
            )
        }
        composable(Screen.SelectAccountType.route) {
            SelectAccountTypeScreen(
                onBackClick = { navController.popBackStack() },
                onTypeSelected = { type ->
                    when (type) {
                        AccountType.CHECKING,
                        AccountType.SAVINGS,
                        AccountType.CASH_WALLET -> {
                            navController.navigate("create_account/${type.name}")
                        }
                        else -> {
                            // Leave other types blank for now
                        }
                    }
                }
            )
        }
        composable(
            route = Screen.CreateAccount.route,
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { backStackEntry ->
            val typeName = backStackEntry.arguments?.getString("type")
            val accountType = AccountType.valueOf(typeName ?: AccountType.CHECKING.name)
            CreateAccountScreen(
                accountType = accountType,
                onBackClick = { navController.popBackStack() },
                onSaveClick = {
                    // Logic to save account
                    navController.popBackStack(Screen.Accounts.route, inclusive = false)
                }
            )
        }
        composable(Screen.Budgets.route) {
            // Reusing statistics as a placeholder for budgets
            StatisticsScreen(viewModel = viewModel)
        }
        composable(Screen.Scheduled.route) {
            // Placeholder
            ProfileScreen()
        }
        composable(Screen.Reports.route) {
            StatisticsScreen(viewModel = viewModel)
        }
        composable(Screen.Settings.route) {
            ProfileScreen()
        }
    }
}
