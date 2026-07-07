package com.example.financial.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.financial.domain.model.TransactionStatus
import com.example.financial.domain.model.TransactionType

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey var id: String = "",
    var type: TransactionType = TransactionType.EXPENSE,
    var fromAccountId: String = "",
    var toAccountId: String? = null,
    var amount: Double = 0.0,
    var categoryId: String? = null,
    var payee: String? = null,
    var description: String? = null,
    var date: Long = 0L,
    var status: TransactionStatus = TransactionStatus.CLEARED,
    var memo: String? = null,
    var symbol: String? = null,
    var shares: Double? = null,
    var pricePerShare: Double? = null,
    var commission: Double? = null,
    var exchangeRate: Double? = null,
    var frequencyValue: Int? = null,
    var frequencyUnit: String? = null,
    var endType: String? = null,
    var endAfterCount: Int? = null,
    var lastUpdated: Long = System.currentTimeMillis(),
    var userId: String = "",
    var isSynced: Boolean = false
)
