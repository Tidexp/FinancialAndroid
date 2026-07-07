package com.example.financial.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "account_groups")
data class AccountGroupEntity(
    @PrimaryKey var id: String = "",
    var name: String = "",
    var iconName: String? = null,
    var iconUri: String? = null,
    var color: Int = 0,
    var lastUpdated: Long = System.currentTimeMillis(),
    var orderIndex: Int = 0,
    var userId: String = "",
    var isSynced: Boolean = false
)
