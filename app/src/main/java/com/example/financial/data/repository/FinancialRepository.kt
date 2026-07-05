package com.example.financial.data.repository

import com.example.financial.data.local.dao.AccountDao
import com.example.financial.data.local.dao.AccountGroupDao
import com.example.financial.data.local.dao.TransactionDao
import com.example.financial.domain.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*

class FinancialRepository(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val accountGroupDao: AccountGroupDao,
    private val budgetDao: com.example.financial.data.local.dao.BudgetDao,
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

    fun getBudgets(): Flow<List<Budget>> = 
        budgetDao.getAllBudgets().map { entities ->
            entities.map { it.toDomain() }
        }

    fun getBudgetGroups(): Flow<List<BudgetGroup>> =
        budgetDao.getAllBudgetGroups().map { entities ->
            entities.map { it.toDomain() }
        }

    fun getBalanceData(): Flow<BalanceData> = flow {
        combine(
            getAccounts(),
            getTransactions()
        ) { accounts, transactions ->
            // Simple calculation for net worth
            var totalBalance = 0.0
            var liabilities = 0.0
            accounts.forEach { account ->
                val b = parseBalance(account.balance)
                if (b < 0) liabilities += kotlin.math.abs(b)
                totalBalance += b
            }
            
            BalanceData(
                netWorth = formatBalance(totalBalance),
                liabilities = formatBalance(-liabilities),
                totalIncome = "$0.00", // Calculate from transactions
                totalExpenses = "$0.00", // Calculate from transactions
                monthlyBudget = 0.5f
            )
        }.collect { emit(it) }
    }

    private fun parseBalance(balance: String): Double {
        return try {
            val normalized = balance.replace(",", ".")
            val clean = normalized.replace(Regex("[^0-9.-]"), "")
            val lastDotIndex = clean.lastIndexOf('.')
            if (lastDotIndex != -1) {
                val integerPart = clean.substring(0, lastDotIndex).replace(".", "")
                val fractionalPart = clean.substring(lastDotIndex + 1)
                (integerPart + "." + fractionalPart).toDouble()
            } else {
                clean.toDouble()
            }
        } catch (e: Exception) {
            0.0
        }
    }

    private fun formatBalance(balance: Double): String {
        return java.util.Locale.getDefault().let { locale ->
            String.format(locale, "$%.2f", balance)
        }
    }

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

    suspend fun updateAccount(account: Account) {
        accountDao.updateAccount(account.toEntity())
        try {
            firestore.collection("users")
                .document(userId)
                .collection("accounts")
                .document(account.id)
                .set(account.toEntity())
        } catch (e: Exception) {
            android.util.Log.e("FinancialRepository", "Firestore update error: ${e.message}")
        }
    }

    suspend fun deleteAccount(account: Account) {
        accountDao.deleteAccount(account.toEntity())
        try {
            firestore.collection("users")
                .document(userId)
                .collection("accounts")
                .document(account.id)
                .delete()
        } catch (e: Exception) {
            android.util.Log.e("FinancialRepository", "Firestore delete error: ${e.message}")
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

    suspend fun updateAccountGroup(group: AccountGroup) {
        accountGroupDao.updateGroup(group.toEntity())
        try {
            firestore.collection("users")
                .document(userId)
                .collection("account_groups")
                .document(group.id)
                .set(group.toEntity())
        } catch (e: Exception) {
            android.util.Log.e("FinancialRepository", "Firestore group update error: ${e.message}")
        }
    }

    suspend fun deleteAccountGroup(group: AccountGroup) {
        accountGroupDao.deleteGroup(group.toEntity())
        try {
            firestore.collection("users")
                .document(userId)
                .collection("account_groups")
                .document(group.id)
                .delete()
        } catch (e: Exception) {
            android.util.Log.e("FinancialRepository", "Firestore group delete error: ${e.message}")
        }
    }

    suspend fun addBudget(budget: Budget) {
        budgetDao.insertBudget(budget.toEntity())
    }

    suspend fun updateBudget(budget: Budget) {
        budgetDao.updateBudget(budget.toEntity())
    }

    suspend fun deleteBudget(budget: Budget) {
        budgetDao.deleteBudget(budget.toEntity())
    }

    suspend fun addBudgetGroup(group: BudgetGroup) {
        budgetDao.insertBudgetGroup(group.toEntity())
    }

    suspend fun updateBudgetGroup(group: BudgetGroup) {
        budgetDao.updateBudgetGroup(group.toEntity())
    }

    suspend fun deleteBudgetGroup(group: BudgetGroup) {
        budgetDao.deleteBudgetGroup(group.toEntity())
    }
}
