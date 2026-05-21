package com.example.financial

import android.app.Application
import androidx.room.Room
import com.example.financial.data.local.AppDatabase

class FinancialApplication : Application() {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "financial_database"
        ).build()
    }
}
