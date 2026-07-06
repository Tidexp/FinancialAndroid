package com.example.financial.data.repository

import com.example.financial.data.local.dao.AccountDao
import com.example.financial.data.local.dao.AccountGroupDao
import com.example.financial.data.local.dao.TransactionDao
import com.example.financial.domain.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import java.util.Calendar

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
        combine(
            budgetDao.getAllBudgets(),
            getTransactions()
        ) { entities, transactions ->
            entities.map { entity ->
                val budget = entity.toDomain()
                
                // Filter transactions based on budget configuration
                val relevantTransactions = transactions.filter { transaction ->
                    val typeMatch = transaction.type == (if (budget.isIncome) TransactionType.INCOME else TransactionType.EXPENSE)
                    val accountMatch = budget.accountIds.isEmpty() || budget.accountIds.contains(transaction.fromAccountId)
                    val categoryMatch = budget.categories.isEmpty() || budget.categories.any { it.equals(transaction.payee, ignoreCase = true) || it.equals(transaction.description, ignoreCase = true) }
                    
                    typeMatch && accountMatch && categoryMatch
                }

                // Current Period Calculation
                val now = System.currentTimeMillis()
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = budget.startDate
                
                // Determine duration of a period in millis
                val periodMillis: Long = when (budget.frequencyUnit.lowercase()) {
                    "day" -> 24L * 60 * 60 * 1000
                    "week" -> 7L * 24 * 60 * 60 * 1000
                    "month" -> 30L * 24 * 60 * 60 * 1000 // Approximate
                    "year" -> 365L * 24 * 60 * 60 * 1000 // Approximate
                    else -> 30L * 24 * 60 * 60 * 1000
                }

                val timePassed = now - budget.startDate
                val periodsPassed = if (timePassed > 0) (timePassed / periodMillis).toInt() else 0
                val currentPeriodStart = budget.startDate + (periodsPassed * periodMillis)
                
                val spentInCurrentPeriod = relevantTransactions
                    .filter { it.date >= currentPeriodStart }
                    .sumOf { it.amount }
                
                // Rollover Logic: Total balance from all past periods
                var rolloverAmount = 0.0
                if (budget.rolloverEnabled && !budget.isIncome && periodsPassed > 0) {
                    val pastTransactionsSpent = relevantTransactions
                        .filter { it.date >= budget.startDate && it.date < currentPeriodStart }
                        .sumOf { it.amount }
                    val totalBudgetedPast = budget.amount * periodsPassed
                    rolloverAmount = totalBudgetedPast - pastTransactionsSpent
                }

                val remaining = budget.amount - spentInCurrentPeriod + rolloverAmount
                val progress = if (budget.amount > 0) (spentInCurrentPeriod / budget.amount).toFloat() else 0f
                
                budget.copy(
                    spent = spentInCurrentPeriod,
                    remaining = remaining,
                    progress = progress.coerceIn(0f, 1f)
                )
            }
        }

    fun getBudgetGroups(): Flow<List<BudgetGroup>> =
        budgetDao.getAllBudgetGroups().map { entities ->
            entities.map { it.toDomain() }
        }

    fun getBalanceData(): Flow<BalanceData> = flow {
        combine(
            getAccounts(),
            getTransactions(),
            getBudgets()
        ) { accounts, transactions, budgets ->
            var totalBalance = 0.0
            var liabilities = 0.0
            accounts.forEach { account ->
                val b = parseBalance(account.balance)
                if (b < 0) liabilities += kotlin.math.abs(b)
                totalBalance += b
            }
            
            val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val totalExpenses = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

            // Calculate overall monthly budget progress
            val expenseBudgets = budgets.filter { !it.isIncome }
            val totalBudgetAmount = expenseBudgets.sumOf { it.amount }
            val totalBudgetSpent = expenseBudgets.sumOf { it.spent }
            
            val budgetProgress = if (totalBudgetAmount > 0) 
                (totalBudgetSpent / totalBudgetAmount).toFloat().coerceIn(0f, 1f)
            else 0f

            BalanceData(
                netWorth = formatBalance(totalBalance),
                liabilities = formatBalance(-liabilities),
                totalIncome = formatBalance(totalIncome),
                totalExpenses = formatBalance(totalExpenses),
                monthlyBudget = budgetProgress
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
