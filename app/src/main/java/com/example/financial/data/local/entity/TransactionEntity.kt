package com.example.financial.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.financial.domain.model.TransactionStatus
import com.example.financial.domain.model.TransactionType

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val type: TransactionType,
    val fromAccountId: String,
    val toAccountId: String? = null,
    val amount: Double,
    val categoryId: String? = null,
    val payee: String? = null,
    val description: String? = null,
    val date: Long,
    val status: TransactionStatus,
    val memo: String? = null,
    // Note: tags list would normally need a converter or separate table. 
    // For simplicity I'll skip it or store as string if needed.
    val symbol: String? = null,
    val shares: Double? = null,
    val pricePerShare: Double? = null,
    val commission: Double? = null,
    val exchangeRate: Double? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)
