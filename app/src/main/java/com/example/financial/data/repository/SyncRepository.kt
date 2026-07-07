package com.example.financial.data.repository

import com.example.financial.data.local.dao.AccountDao
import com.example.financial.data.local.dao.AccountGroupDao
import com.example.financial.data.local.dao.BudgetDao
import com.example.financial.data.local.dao.TransactionDao
import com.example.financial.data.local.entity.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SyncRepository(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val accountGroupDao: AccountGroupDao,
    private val budgetDao: BudgetDao,
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) {
    private val userId: String get() = authRepository.currentUser?.id ?: ""

    suspend fun syncUp() = withContext(Dispatchers.IO) {
        if (userId.isEmpty()) return@withContext

        try {
            // 1. Sync Transactions
            val unsyncedTransactions = transactionDao.getUnsyncedTransactions()
            unsyncedTransactions.forEach { transaction ->
                firestore.collection("users").document(userId)
                    .collection("transactions").document(transaction.id).set(transaction).await()
            }
            if (unsyncedTransactions.isNotEmpty()) {
                transactionDao.markAsSynced(unsyncedTransactions.map { it.id })
            }

            // 2. Sync Accounts
            val unsyncedAccounts = accountDao.getUnsyncedAccounts()
            unsyncedAccounts.forEach { account ->
                firestore.collection("users").document(userId)
                    .collection("accounts").document(account.id).set(account).await()
            }
            if (unsyncedAccounts.isNotEmpty()) {
                accountDao.markAsSynced(unsyncedAccounts.map { it.id })
            }

            // 3. Sync Account Groups
            val unsyncedAccountGroups = accountGroupDao.getUnsyncedAccountGroups()
            unsyncedAccountGroups.forEach { group ->
                firestore.collection("users").document(userId)
                    .collection("accountGroups").document(group.id).set(group).await()
            }
            if (unsyncedAccountGroups.isNotEmpty()) {
                accountGroupDao.markAsSynced(unsyncedAccountGroups.map { it.id })
            }

            // 4. Sync Budgets
            val unsyncedBudgets = budgetDao.getUnsyncedBudgets()
            unsyncedBudgets.forEach { budget ->
                firestore.collection("users").document(userId)
                    .collection("budgets").document(budget.id).set(budget).await()
            }
            if (unsyncedBudgets.isNotEmpty()) {
                budgetDao.markBudgetsAsSynced(unsyncedBudgets.map { it.id })
            }

            // 5. Sync Budget Groups
            val unsyncedBudgetGroups = budgetDao.getUnsyncedBudgetGroups()
            unsyncedBudgetGroups.forEach { group ->
                firestore.collection("users").document(userId)
                    .collection("budgetGroups").document(group.id).set(group).await()
            }
            if (unsyncedBudgetGroups.isNotEmpty()) {
                budgetDao.markGroupsAsSynced(unsyncedBudgetGroups.map { it.id })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun syncDown() = withContext(Dispatchers.IO) {
        if (userId.isEmpty()) return@withContext

        try {
            // 1. Pull Transactions
            val transactionDocs = firestore.collection("users").document(userId)
                .collection("transactions").get().await()
            val transactions = transactionDocs.toObjects(TransactionEntity::class.java).map { 
                it.copy(isSynced = true) 
            }
            if (transactions.isNotEmpty()) transactionDao.insertTransactions(transactions)

            // 2. Pull Accounts
            val accountDocs = firestore.collection("users").document(userId)
                .collection("accounts").get().await()
            val accounts = accountDocs.toObjects(AccountEntity::class.java).map { 
                it.copy(isSynced = true) 
            }
            if (accounts.isNotEmpty()) accountDao.insertAccounts(accounts)

            // 3. Pull Account Groups
            val groupDocs = firestore.collection("users").document(userId)
                .collection("accountGroups").get().await()
            val groups = groupDocs.toObjects(AccountGroupEntity::class.java).map { 
                it.copy(isSynced = true) 
            }
            if (groups.isNotEmpty()) accountGroupDao.insertAccountGroups(groups)

            // 4. Pull Budgets
            val budgetDocs = firestore.collection("users").document(userId)
                .collection("budgets").get().await()
            val budgets = budgetDocs.toObjects(BudgetEntity::class.java).map { 
                it.copy(isSynced = true) 
            }
            if (budgets.isNotEmpty()) budgetDao.insertBudgets(budgets)

            // 5. Pull Budget Groups
            val budgetGroupDocs = firestore.collection("users").document(userId)
                .collection("budgetGroups").get().await()
            val budgetGroups = budgetGroupDocs.toObjects(BudgetGroupEntity::class.java).map { 
                it.copy(isSynced = true) 
            }
            if (budgetGroups.isNotEmpty()) budgetDao.insertBudgetGroups(budgetGroups)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
