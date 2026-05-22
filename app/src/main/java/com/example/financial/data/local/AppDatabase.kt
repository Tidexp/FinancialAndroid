package com.example.financial.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.financial.data.local.dao.AccountDao
import com.example.financial.data.local.dao.AccountGroupDao
import com.example.financial.data.local.dao.TransactionDao
import com.example.financial.data.local.entity.AccountEntity
import com.example.financial.data.local.entity.AccountGroupEntity
import com.example.financial.data.local.entity.TransactionEntity

@Database(entities = [TransactionEntity::class, AccountEntity::class, AccountGroupEntity::class], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao
    abstract fun accountGroupDao(): AccountGroupDao
}
