package com.example.financial.data.local.dao

import androidx.room.*
import com.example.financial.data.local.entity.BudgetEntity
import com.example.financial.data.local.entity.BudgetGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE userId = :userId")
    fun getAllBudgets(userId: String): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE userId = :userId")
    suspend fun getAllBudgetsList(userId: String): List<BudgetEntity>

    @Query("SELECT * FROM budget_groups WHERE userId = :userId")
    fun getAllBudgetGroups(userId: String): Flow<List<BudgetGroupEntity>>

    @Query("SELECT * FROM budget_groups WHERE userId = :userId")
    suspend fun getAllBudgetGroupsList(userId: String): List<BudgetGroupEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgets(budgets: List<BudgetEntity>)

    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE userId = :userId AND substr(id, 1, 11) = '__perf_bud_'")
    suspend fun deletePerformanceBudgets(userId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgetGroup(group: BudgetGroupEntity)

    @Update
    suspend fun updateBudgetGroup(group: BudgetGroupEntity)

    @Delete
    suspend fun deleteBudgetGroup(group: BudgetGroupEntity)
}
