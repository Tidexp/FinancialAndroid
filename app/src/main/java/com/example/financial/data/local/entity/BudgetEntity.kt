package com.example.financial.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey var id: String = "",
    var name: String = "",
    var amount: Double = 0.0,
    var isIncome: Boolean = false,
    var color: Int = 0,
    var budgetGroupId: String? = null,
    var startDate: Long = 0L,
    var repeatEnabled: Boolean = true,
    var frequencyValue: Int = 1,
    var frequencyUnit: String = "month",
    var rolloverEnabled: Boolean = false,
    var accountIds: List<String> = emptyList(),
    var categories: List<String> = emptyList(),
    var lastUpdated: Long = System.currentTimeMillis(),
    var userId: String = "",
    var isSynced: Boolean = false
)

@Entity(tableName = "budget_groups")
data class BudgetGroupEntity(
    @PrimaryKey var id: String = "",
    var name: String = "",
    var color: Int = 0,
    var lastUpdated: Long = System.currentTimeMillis(),
    var userId: String = "",
    var isSynced: Boolean = false
)
