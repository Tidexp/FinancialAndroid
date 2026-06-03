package com.example.financial.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Accounts : Screen("accounts", "Accounts", Icons.Default.AccountBalance)
    object Budgets : Screen("budgets", "Budgets", Icons.Default.PieChart)
    object Scheduled : Screen("scheduled", "Scheduled", Icons.Default.Event)
    object Reports : Screen("reports", "Reports", Icons.Default.BarChart)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object SelectAccountType : Screen("select_account_type", "Select Account Type", Icons.Default.Add)
    object CreateAccountGroup : Screen("create_account_group", "Create Account Group", Icons.Default.GroupAdd)
    object CreateAccount : Screen("create_account/{type}", "Create Account", Icons.Default.Add)
    object AccountDetail : Screen("account_detail/{accountId}", "Account Detail", Icons.Default.Info)
    object AddTransaction : Screen("add_transaction/{accountId}", "Add Transaction", Icons.Default.Add)
    object AddExpenseBudget : Screen("add_expense_budget", "Add Expense Budget", Icons.Default.Add)
    object AddIncomeBudget : Screen("add_income_budget", "Add Income Budget", Icons.Default.Add)
    object AddBudgetsGroup : Screen("add_budgets_group", "Add Budgets Group", Icons.Default.Add)
}

val bottomNavItems = listOf(
    Screen.Accounts,
    Screen.Budgets,
    Screen.Scheduled,
    Screen.Reports,
    Screen.Settings
)
