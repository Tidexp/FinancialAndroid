package com.example.financial.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.financial.domain.model.AccountType

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: String,
    val name: String,
    val balance: String,
    val type: AccountType,
    val color: Int,
    val groupId: String? = null,
    val iconUri: String? = null,
    val creditLimit: String? = null,
    val statementCloseDay: String? = null,
    val autoClear: Boolean = false,
    val additionalInfo: String? = null,
    // Loan specific
    val principalAmount: String? = null,
    val apr: String? = null,
    val duration: String? = null,
    val startDate: String? = null,
    val firstDueDate: String? = null,
    // Investment specific
    val asOfDate: String? = null,
    // Forex specific
    val currency: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)
