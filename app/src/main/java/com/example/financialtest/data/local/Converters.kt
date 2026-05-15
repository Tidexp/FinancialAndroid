package com.example.financialtest.data.local

import androidx.room.TypeConverter
import com.example.financialtest.domain.model.AccountType
import com.example.financialtest.domain.model.TransactionStatus

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
