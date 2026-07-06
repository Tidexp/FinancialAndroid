package com.example.financial.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val amount: Double,
    val isIncome: Boolean,
    val color: Int,
    val budgetGroupId: String? = null,
    val startDate: Long,
    val repeatEnabled: Boolean = true,
    val frequencyValue: Int = 1,
    val frequencyUnit: String = "month",
    val rolloverEnabled: Boolean = false,
    val accountIds: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "budget_groups")
data class BudgetGroupEntity(
    @PrimaryKey val id: String,
    val name: String,
    val color: Int,
    val lastUpdated: Long = System.currentTimeMillis()
)
