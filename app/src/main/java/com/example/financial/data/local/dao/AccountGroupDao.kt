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

    @Delete
    suspend fun deleteGroup(group: AccountGroupEntity)
}
