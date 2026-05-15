package com.example.financialtest.data.repository

import com.example.financialtest.data.local.dao.AccountDao
import com.example.financialtest.data.local.dao.TransactionDao
import com.example.financialtest.domain.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class FinancialRepository(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val userId: String
        get() = auth.currentUser?.uid ?: "anonymous"

    fun getTransactions(): Flow<List<Transaction>> = 
        transactionDao.getAllTransactions().map { entities -> 
            entities.map { it.toDomain() } 
        }

    fun getAccounts(): Flow<List<Account>> = 
        accountDao.getAllAccounts().map { entities -> 
            entities.map { it.toDomain() } 
        }

    fun getBalanceData(): Flow<BalanceData> = flowOf(
        BalanceData("$0.00", "$0.00", "$0.00", "$0.00", 0.0f)
    )

    fun getBudgets(): Flow<List<Budget>> = flowOf(emptyList())

    fun getCategorySpending(): Flow<List<CategorySpending>> = flowOf(emptyList())

    suspend fun addTransaction(transaction: Transaction) {
        // 1. Save to Room (Local)
        transactionDao.insertTransaction(transaction.toEntity())
        
        // 2. Push to Firestore (Cloud) - partitioned by user
        firestore.collection("users")
            .document(userId)
            .collection("transactions")
            .document(transaction.id)
            .set(transaction.toEntity())
    }

    suspend fun addAccount(account: Account) {
        accountDao.insertAccount(account.toEntity())
        
        firestore.collection("users")
            .document(userId)
            .collection("accounts")
            .document(account.id)
            .set(account.toEntity())
    }
}
