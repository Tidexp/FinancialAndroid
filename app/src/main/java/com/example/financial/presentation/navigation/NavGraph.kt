package com.example.financial.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.financial.presentation.screen.accounts.AccountsScreen
import com.example.financial.presentation.screen.accounts.setup.*
import com.example.financial.presentation.screen.budgets.BudgetsScreen
import com.example.financial.presentation.screen.budgets.BudgetDetailScreen
import com.example.financial.presentation.screen.budgets.AddBudgetTransactionScreen
import com.example.financial.presentation.screen.budgets.BudgetDetailScreen
import com.example.financial.presentation.screen.budgets.setup.*
import com.example.financial.presentation.screen.reports.ReportsScreen
import com.example.financial.presentation.screen.scheduled.ScheduledScreen
import com.example.financial.presentation.screen.settings.SettingsScreen
import com.example.financial.presentation.screen.transactions.AddTransactionScreen
import com.example.financial.presentation.viewmodel.FinancialViewModel
import com.example.financial.domain.model.AccountType
import com.example.financial.presentation.screen.accounts.AccountDetailScreen

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
                },
                onEditAccountClick = { account ->
                    val route = when (account.type) {
                        AccountType.CHECKING, AccountType.SAVINGS, AccountType.CASH_WALLET -> "edit_standard_account/${account.id}"
                        AccountType.CREDIT -> "edit_credit_account/${account.id}"
                        AccountType.LOAN -> "edit_loan_account/${account.id}"
                        AccountType.INVESTMENT -> "edit_investment_account/${account.id}"
                        AccountType.FOREX -> "edit_forex_account/${account.id}"
                    }
                    navController.navigate(route)
                },
                onEditGroupClick = { group ->
                    // navController.navigate("edit_account_group/${group.id}")
                },
                onNavigateToTransaction = { accountId, type ->
                    navController.navigate("add_transaction/$accountId/$type")
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
            val uiState by viewModel.homeUiState.collectAsState()

            CreateStandardAccountScreen(
                accountType = accountType,
                onBackClick = { navController.popBackStack() },
                onSaveClick = { name, balance, type, groupId, autoClear, info, monitoredByBudgetId ->
                    viewModel.addStandardAccount(name, balance, type, groupId, autoClear, info, monitoredByBudgetId)
                    navController.popBackStack(Screen.Accounts.route, inclusive = false)
                },
                groups = uiState.accountGroups,
                budgets = uiState.budgets
            )
        }

        composable(
            route = "edit_standard_account/{accountId}",
            arguments = listOf(navArgument("accountId") { type = NavType.StringType })
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId")
            val uiState by viewModel.homeUiState.collectAsState()
            val account = uiState.accounts.find { it.id == accountId }
            
            if (account != null) {
                CreateStandardAccountScreen(
                    accountId = accountId,
                    accountType = account.type,
                    onBackClick = { navController.popBackStack() },
                    onSaveClick = { _, _, _, _, _, _, _ -> },
                    onUpdateClick = { updatedAccount ->
                        viewModel.updateAccount(updatedAccount)
                        navController.popBackStack()
                    },
                    groups = uiState.accountGroups,
                    budgets = uiState.budgets,
                    accounts = uiState.accounts
                )
            }
        }

        // 2. Màn hình tạo tài khoản TÍN DỤNG (Credit) mới thêm
        composable(route = "create_credit_account") {
            val uiState by viewModel.homeUiState.collectAsState()
            CreateCreditAccountScreen(
                onBackClick = { navController.popBackStack() },
                onSaveClick = { name, balance, limit, icon, day, autoClear, info, groupId, monitoredByBudgetId ->
                    viewModel.addCreditAccount(name, balance, limit, icon, day, autoClear, info, groupId, monitoredByBudgetId)
                    navController.popBackStack(Screen.Accounts.route, inclusive = false)
                },
                groups = uiState.accountGroups,
                budgets = uiState.budgets
            )
        }

        composable(
            route = "edit_credit_account/{accountId}",
            arguments = listOf(navArgument("accountId") { type = NavType.StringType })
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId")
            val uiState by viewModel.homeUiState.collectAsState()
            val account = uiState.accounts.find { it.id == accountId }
            
            if (account != null) {
                CreateCreditAccountScreen(
                    accountId = accountId,
                    onBackClick = { navController.popBackStack() },
                    onSaveClick = { _, _, _, _, _, _, _, _, _ -> },
                    onUpdateClick = { updatedAccount ->
                        viewModel.updateAccount(updatedAccount)
                        navController.popBackStack()
                    },
                    groups = uiState.accountGroups,
                    budgets = uiState.budgets,
                    accounts = uiState.accounts
                )
            }
        }

        composable(Screen.CreateAccountGroup.route) {
            val uiState by viewModel.homeUiState.collectAsState()
            CreateAccountGroupScreen(
                onBackClick = { navController.popBackStack() },
                onSaveClick = { name, iconName, iconUri, color, accountIds ->
                    viewModel.addAccountGroup(name, iconName, iconUri, color, accountIds)
                    navController.popBackStack(Screen.Accounts.route, inclusive = false)
                },
                accounts = uiState.accounts
            )
        }

        // Màn hình tạo tài khoản LOAN
        composable("create_loan_account") {
            val uiState by viewModel.homeUiState.collectAsState()
            CreateLoanAccountScreen(
                onBackClick = { navController.popBackStack() },
                onSaveClick = { name, principal, apr, duration, start, first, groupId, info, monitoredByBudgetId, paymentAccountId, paymentCategory, paymentPayee ->
                    viewModel.addLoanAccount(name, principal, apr, duration, start, first, groupId, info, monitoredByBudgetId, paymentAccountId, paymentCategory, paymentPayee)
                    navController.popBackStack(Screen.Accounts.route, inclusive = false)
                },
                groups = uiState.accountGroups,
                budgets = uiState.budgets,
                accounts = uiState.accounts
            )
        }

        composable(
            route = "edit_loan_account/{accountId}",
            arguments = listOf(navArgument("accountId") { type = NavType.StringType })
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId")
            val uiState by viewModel.homeUiState.collectAsState()
            CreateLoanAccountScreen(
                accountId = accountId,
                onBackClick = { navController.popBackStack() },
                onSaveClick = { _, _, _, _, _, _, _, _, _, _, _, _ -> }, // Not used for edit
                onUpdateClick = { account ->
                    viewModel.updateAccount(account)
                    navController.popBackStack()
                },
                groups = uiState.accountGroups,
                budgets = uiState.budgets,
                accounts = uiState.accounts
            )
        }

        // Màn hình tạo tài khoản INVESMENT
        composable("create_investment_account") {
            val uiState by viewModel.homeUiState.collectAsState()
            CreateInvestmentAccountScreen(
                onBackClick = { navController.popBackStack() },
                onSaveClick = { name, balance, date, groupId, info, monitoredByBudgetId ->
                    viewModel.addInvestmentAccount(name, balance, date, groupId, info, monitoredByBudgetId)
                    navController.popBackStack(Screen.Accounts.route, inclusive = false)
                },
                groups = uiState.accountGroups,
                budgets = uiState.budgets
            )
        }

        composable(
            route = "edit_investment_account/{accountId}",
            arguments = listOf(navArgument("accountId") { type = NavType.StringType })
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId")
            val uiState by viewModel.homeUiState.collectAsState()
            val account = uiState.accounts.find { it.id == accountId }
            
            if (account != null) {
                CreateInvestmentAccountScreen(
                    accountId = accountId,
                    onBackClick = { navController.popBackStack() },
                    onSaveClick = { _, _, _, _, _, _ -> },
                    onUpdateClick = { updatedAccount ->
                        viewModel.updateAccount(updatedAccount)
                        navController.popBackStack()
                    },
                    groups = uiState.accountGroups,
                    budgets = uiState.budgets,
                    accounts = uiState.accounts
                )
            }
        }

        composable("create_forex_crypto_account") {
            val uiState by viewModel.homeUiState.collectAsState()
            CreateForexCryptoAccountScreen(
                onBackClick = { navController.popBackStack() },
                onSaveClick = { name, currency, groupId, info, monitoredByBudgetId ->
                    viewModel.addForexAccount(name, currency, groupId, info, monitoredByBudgetId)
                    navController.popBackStack(Screen.Accounts.route, inclusive = false)
                },
                groups = uiState.accountGroups,
                budgets = uiState.budgets
            )
        }

        composable(
            route = "edit_forex_account/{accountId}",
            arguments = listOf(navArgument("accountId") { type = NavType.StringType })
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId")
            val uiState by viewModel.homeUiState.collectAsState()
            val account = uiState.accounts.find { it.id == accountId }
            
            if (account != null) {
                CreateForexCryptoAccountScreen(
                    accountId = accountId,
                    onBackClick = { navController.popBackStack() },
                    onSaveClick = { _, _, _, _, _ -> },
                    onUpdateClick = { updatedAccount ->
                        viewModel.updateAccount(updatedAccount)
                        navController.popBackStack()
                    },
                    groups = uiState.accountGroups,
                    budgets = uiState.budgets,
                    accounts = uiState.accounts
                )
            }
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
                    transactions = uiState.transactions.filter { 
                        it.fromAccountId == account.id || it.toAccountId == account.id 
                    },
                    allAccounts = uiState.accounts,
                    allBudgets = uiState.budgets,
                    onBackClick = { navController.popBackStack() },
                    onDeleteClick = {
                        viewModel.deleteAccount(it)
                        navController.popBackStack()
                    },
                    onNavigateToTransaction = { type ->
                        navController.navigate("add_transaction/${account.id}/$type")
                    }
                )
            }
        }

        composable(
            route = Screen.AddTransaction.route,
            arguments = listOf(
                navArgument("accountId") { type = NavType.StringType },
                navArgument("type") { type = NavType.StringType; nullable = true },
                navArgument("isScheduled") { type = NavType.BoolType; defaultValue = false }
            )
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId")
            val type = backStackEntry.arguments?.getString("type")
            val isScheduled = backStackEntry.arguments?.getBoolean("isScheduled") ?: false
            val uiState by viewModel.homeUiState.collectAsState()
            val account = uiState.accounts.find { it.id == accountId }

            AddTransactionScreen(
                viewModel = viewModel,
                account = account,
                initialType = type,
                isScheduled = isScheduled,
                onBackClick = { navController.popBackStack() }
            )
        }

        // Các route khác giữ nguyên
        composable(Screen.Budgets.route) {
            BudgetsScreen(
                viewModel = viewModel,
                onAddExpenseBudgetClick = {
                    navController.navigate("create_budget/false")
                },
                onAddIncomeBudgetClick = {
                    navController.navigate("create_budget/true")
                },
                onAddBudgetsGroupClick = {
                    navController.navigate(Screen.AddBudgetsGroup.route)
                },
                onBudgetClick = { budgetId ->
                    navController.navigate("budget_detail/$budgetId")
                },
                onEditBudgetClick = { budgetId ->
                    navController.navigate("edit_budget/$budgetId")
                }
            )
        }

        composable(
            route = "budget_detail/{budgetId}",
            arguments = listOf(navArgument("budgetId") { type = NavType.StringType })
        ) { backStackEntry ->
            val budgetId = backStackEntry.arguments?.getString("budgetId")
            val uiState by viewModel.homeUiState.collectAsState()
            val budget = uiState.budgets.find { it.id == budgetId }

            if (budget != null) {
                // Determine duration of a period in millis for filtering
                val periodMillis: Long = when (budget.frequencyUnit.lowercase()) {
                    "day" -> 24L * 60 * 60 * 1000
                    "week" -> 7L * 24 * 60 * 60 * 1000
                    "month" -> 30L * 24 * 60 * 60 * 1000
                    "year" -> 365L * 24 * 60 * 60 * 1000
                    else -> 30L * 24 * 60 * 60 * 1000
                }
                val now = System.currentTimeMillis()
                val timePassed = now - budget.startDate
                val periodsPassed = if (timePassed > 0) (timePassed / periodMillis).toInt() else 0
                val currentPeriodStart = budget.startDate + (periodsPassed * periodMillis)

                val budgetTransactions = uiState.transactions.filter { transaction ->
                    // 1. Ưu tiên giao dịch được gắn trực tiếp vào budget này
                    if (transaction.budgetId == budget.id) return@filter true
                    // 2. Không hiển thị giao dịch ảo của budget khác
                    if (transaction.budgetId != null) return@filter false

                    // 3. Hiển thị giao dịch thật khớp tiêu chí
                    val typeMatch = transaction.type == (if (budget.isIncome) com.example.financial.domain.model.TransactionType.INCOME else com.example.financial.domain.model.TransactionType.EXPENSE)
                    val accountMatch = budget.accountIds.isEmpty() || budget.accountIds.contains(transaction.fromAccountId)
                    val categoryMatch = budget.categories.isEmpty() || budget.categories.any { it.equals(transaction.payee, ignoreCase = true) || it.equals(transaction.description, ignoreCase = true) }
                    val dateMatch = transaction.date >= currentPeriodStart

                    typeMatch && accountMatch && categoryMatch && dateMatch
                }

                BudgetDetailScreen(
                    budget = budget,
                    transactions = budgetTransactions,
                    allAccounts = uiState.accounts,
                    allBudgets = uiState.budgets,
                    onBackClick = { navController.popBackStack() },
                    onAddTransactionClick = {
                        navController.navigate("add_budget_transaction/${budget.id}")
                    }
                )
            }
        }

        composable(
            route = "add_budget_transaction/{budgetId}",
            arguments = listOf(navArgument("budgetId") { type = NavType.StringType })
        ) { backStackEntry ->
            val budgetId = backStackEntry.arguments?.getString("budgetId")
            if (budgetId != null) {
                AddBudgetTransactionScreen(
                    viewModel = viewModel,
                    budgetId = budgetId,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = Screen.CreateBudget.route,
            arguments = listOf(navArgument("isIncome") { type = NavType.BoolType })
        ) { backStackEntry ->
            val isIncome = backStackEntry.arguments?.getBoolean("isIncome") ?: false
            CreateBudgetScreen(
                viewModel = viewModel,
                initialIsIncome = isIncome,
                onCloseClick = { navController.popBackStack() }
            )
        }

        composable(Screen.AddExpenseBudget.route) {
            CreateBudgetScreen(
                viewModel = viewModel,
                initialIsIncome = false,
                onCloseClick = { navController.popBackStack() }
            )
        }

        composable(Screen.AddIncomeBudget.route) {
            CreateBudgetScreen(
                viewModel = viewModel,
                initialIsIncome = true,
                onCloseClick = { navController.popBackStack() }
            )
        }

        composable(
            route = "edit_budget/{budgetId}",
            arguments = listOf(navArgument("budgetId") { type = NavType.StringType })
        ) { backStackEntry ->
            val budgetId = backStackEntry.arguments?.getString("budgetId")
            CreateBudgetScreen(
                viewModel = viewModel,
                budgetId = budgetId,
                onCloseClick = { navController.popBackStack() }
            )
        }

        composable(Screen.AddBudgetsGroup.route) {
            CreateBudgetGroupScreen(
                onBackClick = { navController.popBackStack() },
                onSaveClick = { name, color ->
                    viewModel.addBudgetGroup(name, color)
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Scheduled.route) {
            val uiState by viewModel.homeUiState.collectAsState()
            ScheduledScreen(
                viewModel = viewModel,
                onAddClick = {
                    val defaultAccountId = uiState.accounts.firstOrNull()?.id ?: "none"
                    navController.navigate("add_transaction/$defaultAccountId/Expense?isScheduled=true")
                },
                onEditScheduled = { transaction ->
                    if (transaction.id.startsWith("loan_payment_")) {
                        val accountId = transaction.id.removePrefix("loan_payment_")
                        navController.navigate("edit_loan_account/$accountId")
                    } else {
                        navController.navigate("edit_transaction/${transaction.id}?isScheduled=true")
                    }
                }
            )
        }

        composable(
            route = Screen.EditTransaction.route,
            arguments = listOf(
                navArgument("transactionId") { type = NavType.StringType },
                navArgument("isScheduled") { type = NavType.BoolType; defaultValue = false }
            )
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId")
            val isScheduled = backStackEntry.arguments?.getBoolean("isScheduled") ?: false
            val uiState by viewModel.homeUiState.collectAsState()
            val transaction = uiState.transactions.find { it.id == transactionId }
            val account = uiState.accounts.find { it.id == transaction?.fromAccountId }

            if (transaction != null) {
                AddTransactionScreen(
                    viewModel = viewModel,
                    account = account,
                    initialType = when (transaction.type) {
                        com.example.financial.domain.model.TransactionType.EXPENSE -> "Expense"
                        com.example.financial.domain.model.TransactionType.INCOME -> "Income"
                        com.example.financial.domain.model.TransactionType.TRANSFER -> "Transfer"
                        else -> "Expense"
                    },
                    isScheduled = isScheduled,
                    existingTransaction = transaction,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
        composable(Screen.Reports.route) {
            ReportsScreen(viewModel = viewModel)
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}



