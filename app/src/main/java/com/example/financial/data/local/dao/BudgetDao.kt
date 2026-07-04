package com.example.financial.data.local.dao

import androidx.room.*
import com.example.financial.data.local.entity.BudgetEntity
import com.example.financial.data.local.entity.BudgetGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets")
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budget_groups")
    fun getAllBudgetGroups(): Flow<List<BudgetGroupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity)

    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgetGroup(group: BudgetGroupEntity)

    @Update
    suspend fun updateBudgetGroup(group: BudgetGroupEntity)

    @Delete
    suspend fun deleteBudgetGroup(group: BudgetGroupEntity)
}
