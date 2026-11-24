package com.example.financial.data.local.dao

import androidx.room.*
import com.example.financial.data.local.entity.AccountGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountGroupDao {
    @Query("SELECT * FROM account_groups WHERE userId = :userId")
    fun getAllGroups(userId: String): Flow<List<AccountGroupEntity>>

    @Query("SELECT * FROM account_groups WHERE userId = :userId")
    suspend fun getAllGroupsList(userId: String): List<AccountGroupEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: AccountGroupEntity)

    @Update
    suspend fun updateGroup(group: AccountGroupEntity)

    @Delete
    suspend fun deleteGroup(group: AccountGroupEntity)
}
