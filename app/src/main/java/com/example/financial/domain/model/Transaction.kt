package com.example.financial.domain.model

import androidx.compose.ui.graphics.vector.ImageVector

data class Transaction(
    val id: String,
    val payee: String,
    val category: String,
    val amount: String,
    val date: String,
    val accountName: String,
    val isExpense: Boolean,
    val icon: ImageVector,
    val status: TransactionStatus = TransactionStatus.CLEARED
)

enum class TransactionStatus {
    CLEARED, PENDING, RECONCILED
}
