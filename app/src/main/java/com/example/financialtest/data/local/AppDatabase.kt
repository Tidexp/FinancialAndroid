package com.example.financialtest.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.financialtest.data.local.dao.AccountDao
import com.example.financialtest.data.local.dao.TransactionDao
import com.example.financialtest.data.local.entity.AccountEntity
import com.example.financialtest.data.local.entity.TransactionEntity

@Database(entities = [TransactionEntity::class, AccountEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao
}
