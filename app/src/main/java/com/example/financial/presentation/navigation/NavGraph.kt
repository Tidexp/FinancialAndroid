package com.example.financial.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.financial.presentation.screen.accounts.AccountsScreen
import com.example.financial.presentation.screen.accounts.setup.*
import com.example.financial.presentation.screen.budgets.BudgetsScreen
import com.example.financial.presentation.screen.reports.ReportsScreen
import com.example.financial.presentation.screen.scheduled.ScheduledScreen
import com.example.financial.presentation.screen.settings.SettingsScreen
import com.example.financial.presentation.viewmodel.FinancialViewModel
import com.example.financial.domain.model.AccountType

@Composable
fun NavGraph(navController: NavHostController) {
    val viewModel: FinancialViewModel = viewModel(factory = FinancialViewModel.Factory)

    NavHost(
        navController = navController,
        startDestination = Screen.Accounts.route
    ) {
        composable(Screen.Accounts.route) {
            AccountsScreen(
                viewModel = viewModel,
                onAddAccountClick = {
                    navController.navigate(Screen.SelectAccountType.route)
                },
                onAddGroupClick = {
                    navController.navigate(Screen.CreateAccountGroup.route)
                },
                onAccountClick = { accountId ->
                    navController.navigate("account_detail/$accountId")
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
                            navController.navigate("create_standard_account/${type.name}")
                        }
                        AccountType.CREDIT -> {
                            navController.navigate("create_credit_account")
                        }
                        AccountType.LOAN -> {
                            navController.navigate("create_loan_account")
                        }
                        AccountType.INVESTMENT -> {
                            navController.navigate("create_investment_account")
                        }
                        AccountType.FOREX -> {
                            navController.navigate("create_forex_crypto_account")
                        }
                    }
                }
            )
        }

        composable(
            route = "create_standard_account/{type}",
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { backStackEntry ->
            val typeName = backStackEntry.arguments?.getString("type")
            val accountType = AccountType.valueOf(typeName ?: AccountType.CHECKING.name)
            val uiState by viewModel.homeUiState.collectAsState()

            CreateStandardAccountScreen(
                accountType = accountType,
                onBackClick = { navController.popBackStack() },
                onSaveClick = { name, balance, type, groupId, autoClear, info ->
                    viewModel.addStandardAccount(name, balance, type, groupId, autoClear, info)
                    navController.popBackStack(Screen.Accounts.route, inclusive = false)
                },
                groups = uiState.accountGroups
            )
        }

        composable(route = "create_credit_account") {
            val uiState by viewModel.homeUiState.collectAsState()
            CreateCreditAccountScreen(
                onBackClick = { navController.popBackStack() },
                onSaveClick = { name, balance, limit, icon, day, autoClear, info, groupId ->
                    viewModel.addCreditAccount(name, balance, limit, icon, day, autoClear, info, groupId)
                    navController.popBackStack(Screen.Accounts.route, inclusive = false)
                },
                groups = uiState.accountGroups
            )
        }

        composable(Screen.CreateAccountGroup.route) {
            CreateAccountGroupScreen(
                onBackClick = { navController.popBackStack() },
                onSaveClick = { name, iconName, iconUri, color ->
                    viewModel.addAccountGroup(name, iconName, iconUri, color)
                    navController.popBackStack(Screen.Accounts.route, inclusive = false)
                }
            )
        }

        composable("create_loan_account") {
            val uiState by viewModel.homeUiState.collectAsState()
            CreateLoanAccountScreen(
                onBackClick = { navController.popBackStack() },
                onSaveClick = { name, principal, apr, duration, start, first, groupId, info ->
                    viewModel.addLoanAccount(name, principal, apr, duration, start, first, groupId, info)
                    navController.popBackStack(Screen.Accounts.route, inclusive = false)
                },
                groups = uiState.accountGroups
            )
        }

        composable("create_investment_account") {
            val uiState by viewModel.homeUiState.collectAsState()
            CreateInvestmentAccountScreen(
                onBackClick = { navController.popBackStack() },
                onSaveClick = { name, balance, date, groupId, info ->
                    viewModel.addInvestmentAccount(name, balance, date, groupId, info)
                    navController.popBackStack(Screen.Accounts.route, inclusive = false)
                },
                groups = uiState.accountGroups
            )
        }

        composable("create_forex_crypto_account") {
            val uiState by viewModel.homeUiState.collectAsState()
            CreateForexCryptoAccountScreen(
                onBackClick = { navController.popBackStack() },
                onSaveClick = { name, currency, groupId, info ->
                    viewModel.addForexAccount(name, currency, groupId, info)
                    navController.popBackStack(Screen.Accounts.route, inclusive = false)
                },
                groups = uiState.accountGroups
            )
        }

        composable(
            route = "account_detail/{accountId}",
            arguments = listOf(navArgument("accountId") { type = NavType.StringType })
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId")
            val uiState by viewModel.homeUiState.collectAsState()
            val account = uiState.accounts.find { it.id == accountId }
            
            if (account != null) {
                AccountDetailScreen(
                    account = account,
                    groupName = uiState.accountGroups.find { it.id == account.groupId }?.name,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.Budgets.route) {
            BudgetsScreen(
                viewModel = viewModel,
                onAddExpenseBudgetClick = { navController.navigate(Screen.AddExpenseBudget.route) },
                onAddIncomeBudgetClick = { navController.navigate(Screen.AddIncomeBudget.route) },
                onAddBudgetsGroupClick = { navController.navigate(Screen.AddBudgetsGroup.route) }
            )
        }

        composable(Screen.AddExpenseBudget.route) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Add Expense Budget Screen")
            }
        }

        composable(Screen.AddIncomeBudget.route) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Add Income Budget Screen")
            }
        }
        composable(Screen.Scheduled.route) {
            ScheduledScreen(viewModel = viewModel)
        }
        composable(Screen.Reports.route) {
            ReportsScreen(viewModel = viewModel)
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
