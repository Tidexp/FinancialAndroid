package com.example.financial.data.local

import androidx.room.TypeConverter
import com.example.financial.domain.model.AccountType
import com.example.financial.domain.model.TransactionStatus

class Converters {
    @TypeConverter
    fun fromTransactionStatus(status: TransactionStatus): String = status.name

    @TypeConverter
    fun toTransactionStatus(value: String): TransactionStatus = TransactionStatus.valueOf(value)

    @TypeConverter
    fun fromAccountType(type: AccountType): String = type.name

    @TypeConverter
    fun toAccountType(value: String): AccountType = AccountType.valueOf(value)
}
