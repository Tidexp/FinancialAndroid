package com.example.financialtest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "account_groups")
data class AccountGroupEntity(
    @PrimaryKey val id: String,
    val name: String,
    val iconName: String?,
    val iconUri: String?,
    val color: Int,
    val lastUpdated: Long = System.currentTimeMillis()
)
