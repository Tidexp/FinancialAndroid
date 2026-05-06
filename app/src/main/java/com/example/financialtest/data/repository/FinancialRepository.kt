package com.example.financialtest.data.repository

import com.example.financialtest.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FinancialRepository {
    fun getTransactions(): Flow<List<Transaction>> = flowOf(emptyList())

    fun getBalanceData(): Flow<BalanceData> = flowOf(
        BalanceData("$0.00", "$0.00", "$0.00", 0.0f)
    )

    fun getAccounts(): Flow<List<Account>> = flowOf(emptyList())

    fun getCategorySpending(): Flow<List<CategorySpending>> = flowOf(emptyList())
}
