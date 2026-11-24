package com.example.financial.data.local.dao

import androidx.room.*
import com.example.financial.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
    fun getAllTransactions(userId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE userId = :userId")
    suspend fun getAllTransactionsList(userId: String): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND date >= :startDateMillis AND budgetId IS NULL ORDER BY date DESC")
    fun getReportTransactions(userId: String, startDateMillis: Long): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Query("DELETE FROM transactions WHERE userId = :userId AND substr(id, 1, 10) = '__perf_tx_'")
    suspend fun deletePerformanceTransactions(userId: String)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)
}
