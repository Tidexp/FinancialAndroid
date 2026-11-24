package com.example.financial

import android.app.Application
import androidx.room.Room
import com.example.financial.data.local.AppDatabase
import com.example.financial.data.local.AppDatabase.Companion.MIGRATION_17_18

class FinancialApplication : Application() {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "financial_database"
        )
            .addMigrations(MIGRATION_17_18)
            .build()
    }
}
