package com.example.financial.data.local.dao

import androidx.room.*
import com.example.financial.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE userId = :userId")
    fun getAllAccounts(userId: String): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE userId = :userId")
    suspend fun getAllAccountsList(userId: String): List<AccountEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query("DELETE FROM accounts WHERE userId = :userId AND substr(id, 1, 15) = '__perf_account_'")
    suspend fun deletePerformanceAccounts(userId: String)

    @Delete
    suspend fun deleteAccount(account: AccountEntity)
}
