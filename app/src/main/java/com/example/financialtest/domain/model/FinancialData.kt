package com.example.financialtest.domain.model

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

data class BalanceData(
    val netWorth: String,
    val totalIncome: String,
    val totalExpenses: String,
    val monthlyBudget: Float // 0.0 to 1.0
)

data class Account(
    val id: String,
    val name: String,
    val balance: String,
    val type: AccountType,
    val color: Color
)

enum class AccountType(val displayName: String, val description: String, val icon: ImageVector) {
    CHECKING("Checking account", "Standard bank account for daily use", Icons.Default.AccountBalance),
    CREDIT("Credit account", "Credit cards or line of credit", Icons.Default.CreditCard),
    SAVINGS("Savings account", "Interest-bearing account", Icons.Default.Savings),
    LOAN("Loan Account", "Money owed to a lender", Icons.Default.MoneyOff),
    CASH_WALLET("Cash/Wallet account", "Physical cash or wallet", Icons.Default.AccountBalanceWallet),
    INVESTMENT("Investment Account", "Stocks, bonds, or mutual funds", Icons.Default.TrendingUp),
    FOREX("Forex Account", "Foreign currency exchange", Icons.Default.CurrencyExchange)
}

data class CategorySpending(
    val label: String,
    val amount: String,
    val progress: Float,
    val color: Color
)
