package com.example.financial.domain.model

data class Transaction(
    val id: String,
    val type: TransactionType,
    val fromAccountId: String,
    val toAccountId: String? = null,
    val amount: Double,
    val categoryId: String? = null,
    val payee: String? = null,
    val description: String? = null,
    val date: Long = System.currentTimeMillis(),
    val status: TransactionStatus = TransactionStatus.CLEARED,
    val memo: String? = null,
    val tags: List<String> = emptyList(),
    val budgetId: String? = null, // Trường mới để tách biệt với Account
    // Investment/Forex
    val symbol: String? = null,
    val shares: Double? = null,
    val pricePerShare: Double? = null,
    val commission: Double? = null,
    val exchangeRate: Double? = null,
)

enum class TransactionType {
    EXPENSE, INCOME, TRANSFER, ADJUSTMENT, EXCHANGE, BUY, SELL
}

enum class TransactionStatus {
    CLEARED, PENDING, RECONCILED
}
