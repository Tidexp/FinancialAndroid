package com.example.financial.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.financial.data.local.dao.AccountDao
import com.example.financial.data.local.dao.AccountGroupDao
import com.example.financial.data.local.dao.BudgetDao
import com.example.financial.data.local.dao.ChatMessageDao
import com.example.financial.data.local.dao.TransactionDao
import com.example.financial.data.local.entity.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        TransactionEntity::class,
        AccountEntity::class,
        AccountGroupEntity::class,
        BudgetEntity::class,
        BudgetGroupEntity::class,
        ChatMessageEntity::class
    ],
    version = 18,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao
    abstract fun accountGroupDao(): AccountGroupDao
    abstract fun budgetDao(): BudgetDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN budgetId TEXT")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_date ON transactions(date)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_budgetId_date ON transactions(budgetId, date)")
            }
        }
    }
}
