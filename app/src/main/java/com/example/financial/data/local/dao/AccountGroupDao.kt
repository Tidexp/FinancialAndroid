package com.example.financial.data.local.dao

import androidx.room.*
import com.example.financial.data.local.entity.AccountGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountGroupDao {
    @Query("SELECT * FROM account_groups")
    fun getAllGroups(): Flow<List<AccountGroupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: AccountGroupEntity)

    @Update
    suspend fun updateGroup(group: AccountGroupEntity)

    @Delete
    suspend fun deleteGroup(group: AccountGroupEntity)

    @Query("SELECT * FROM account_groups WHERE isSynced = 0")
    suspend fun getUnsyncedAccountGroups(): List<AccountGroupEntity>

    @Query("UPDATE account_groups SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccountGroups(groups: List<AccountGroupEntity>)
}
