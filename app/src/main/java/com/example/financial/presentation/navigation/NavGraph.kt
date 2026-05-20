package com.example.financial.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
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
                }
            )
        }

        composable(Screen.SelectAccountType.route) {
            SelectAccountTypeScreen(
                onBackClick = { navController.popBackStack() },
                onTypeSelected = { type ->
                    when (type) {
                        // Nhóm các loại dùng chung giao diện Standard
                        AccountType.CHECKING,
                        AccountType.SAVINGS,
                        AccountType.CASH_WALLET -> {
                            navController.navigate("create_standard_account/${type.name}")
                        }
                        // Điều hướng sang màn hình Credit riêng biệt
                        AccountType.CREDIT -> {
                            navController.navigate("create_credit_account")
                        }
                        AccountType.LOAN -> { // Thêm route cho Loan
                            navController.navigate("create_loan_account")
                        }
                        // Trong SelectAccountTypeScreen -> onTypeSelected
                        AccountType.INVESTMENT -> {
                            navController.navigate("create_investment_account")
                        }
                        AccountType.FOREX -> {
                            navController.navigate("create_forex_crypto_account")
                        }
                        else -> {
                            // Các loại khác như LOAN, INVESTMENT
                        }
                    }
                }
            )
        }

        // 1. Màn hình tạo tài khoản TIÊU CHUẨN (CHECKING, SAVINGS and CASH_WALLET)
        composable(
            route = "create_standard_account/{type}",
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { backStackEntry ->
            val typeName = backStackEntry.arguments?.getString("type")
            val accountType = AccountType.valueOf(typeName ?: AccountType.CHECKING.name)

            CreateStandardAccountScreen(
                accountType = accountType,
                onBackClick = { navController.popBackStack() },
                onSaveClick = {
                    // Logic lưu tài khoản tiêu chuẩn
                    navController.popBackStack(Screen.Accounts.route, inclusive = false)
                }
            )
        }

        // 2. Màn hình tạo tài khoản TÍN DỤNG (Credit) mới thêm
        composable(route = "create_credit_account") {
            CreateCreditAccountScreen(
                onBackClick = { navController.popBackStack() },
                onSaveClick = {
                    // Logic lưu tài khoản tín dụng
                    navController.popBackStack(Screen.Accounts.route, inclusive = false)
                }
            )
        }

        // Màn hình tạo nhóm tài khoản (Group)
        composable(Screen.CreateAccountGroup.route) {
            CreateAccountGroupScreen(
                onBackClick = { navController.popBackStack() },
                onSaveClick = { name, iconName, iconUri, color ->
                    viewModel.addAccountGroup(name, iconName, iconUri, color)
                    navController.popBackStack(Screen.Accounts.route, inclusive = false)
                }
            )
        }

        // Màn hình tạo tài khoản LOAN
        composable("create_loan_account") {
            CreateLoanAccountScreen(
                onBackClick = { navController.popBackStack() },
                onSaveClick = { navController.popBackStack() }
            )
        }

        // Màn hình tạo tài khoản INVESMENT
        composable("create_investment_account") {
            CreateInvestmentAccountScreen(
                onBackClick = { navController.popBackStack() },
                onNextClick = {
                    // Điều hướng tới bước thiết lập Portfolio hoặc lưu
                    navController.popBackStack(Screen.Accounts.route, inclusive = false)
                }
            )
        }

        // Màn hình tạo tài khoản FOREX/CRYPTO
        composable("create_forex_crypto_account") {
            CreateForexCryptoAccountScreen(
                onBackClick = { navController.popBackStack() },
                onNextClick = {
                    // Có thể mở một BottomSheet hoặc màn hình chọn sàn giao dịch (Exchange/Wallet)
                }
            )
        }

        // Các route khác giữ nguyên
        composable(Screen.Budgets.route) {
            BudgetsScreen(
                viewModel = viewModel,
                onAddExpenseBudgetClick = {
                    navController.navigate(Screen.AddExpenseBudget.route)
                },
                onAddIncomeBudgetClick = {
                    navController.navigate(Screen.AddIncomeBudget.route)
                },
                onAddBudgetsGroupClick = {
                    navController.navigate(Screen.AddBudgetsGroup.route)
                }
            )
        }

        composable(Screen.AddExpenseBudget.route) {
            // TODO: Create AddExpenseBudgetScreen
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Add Expense Budget Screen")
            }
        }

        composable(Screen.AddIncomeBudget.route) {
            // TODO: Create AddIncomeBudgetScreen
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
