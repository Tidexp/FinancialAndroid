package com.example.financial.data.local.dao

import androidx.room.*
import com.example.financial.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

data class CategorySpendingRow(
    val label: String?,
    val amount: Double
)

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsInRange(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    @Query("SELECT categoryId as label, SUM(amount) as amount FROM transactions WHERE type = 'EXPENSE' AND date BETWEEN :startDate AND :endDate GROUP BY categoryId")
    fun getSpendingByCategory(startDate: Long, endDate: Long): Flow<List<CategorySpendingRow>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE isSynced = 0")
    suspend fun getUnsyncedTransactions(): List<TransactionEntity>

    @Query("UPDATE transactions SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}
