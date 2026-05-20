package com.example.financial.data.repository

import com.example.financial.data.local.dao.AccountDao
import com.example.financial.data.local.dao.AccountGroupDao
import com.example.financial.data.local.dao.TransactionDao
import com.example.financial.domain.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class FinancialRepository(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val accountGroupDao: AccountGroupDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
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

    fun getAccountGroups(): Flow<List<AccountGroup>> =
        accountGroupDao.getAllGroups().map { entities ->
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
        
        // 2. Push to Firestore (Cloud) - Optional
        try {
            firestore.collection("users")
                .document(userId)
                .collection("transactions")
                .document(transaction.id)
                .set(transaction.toEntity())
        } catch (e: Exception) {
            android.util.Log.e("FinancialRepository", "Firestore error: ${e.message}")
        }
    }

    suspend fun addAccount(account: Account) {
        accountDao.insertAccount(account.toEntity())
        
        try {
            firestore.collection("users")
                .document(userId)
                .collection("accounts")
                .document(account.id)
                .set(account.toEntity())
        } catch (e: Exception) {
            android.util.Log.e("FinancialRepository", "Firestore error: ${e.message}")
        }
    }

    suspend fun addAccountGroup(group: AccountGroup) {
        accountGroupDao.insertGroup(group.toEntity())
        
        try {
            firestore.collection("users")
                .document(userId)
                .collection("account_groups")
                .document(group.id)
                .set(group.toEntity())
        } catch (e: Exception) {
            android.util.Log.e("FinancialRepository", "Firestore error: ${e.message}")
        }
    }
}
