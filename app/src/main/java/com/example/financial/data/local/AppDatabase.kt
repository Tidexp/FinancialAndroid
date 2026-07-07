package com.example.financial.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.financial.data.local.dao.AccountDao
import com.example.financial.data.local.dao.AccountGroupDao
import com.example.financial.data.local.dao.BudgetDao
import com.example.financial.data.local.dao.TransactionDao
import com.example.financial.data.local.entity.*

@Database(
    entities = [
        TransactionEntity::class,
        AccountEntity::class,
        AccountGroupEntity::class,
        BudgetEntity::class,
        BudgetGroupEntity::class
    ],
    version = 13,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao
    abstract fun accountGroupDao(): AccountGroupDao
    abstract fun budgetDao(): BudgetDao

    fun clearDatabase() {
        this.clearAllTables()
    }
}
