package com.example.financialtest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.financialtest.domain.model.Transaction
import com.example.financialtest.domain.model.TransactionStatus

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val payee: String,
    val category: String,
    val amount: String,
    val date: String,
    val accountName: String,
    val isExpense: Boolean,
    val status: TransactionStatus,
    val lastUpdated: Long = System.currentTimeMillis()
)
