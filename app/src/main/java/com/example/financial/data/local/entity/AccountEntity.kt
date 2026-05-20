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
    val color: Int, // Store as ARGB Int
    val groupId: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)
