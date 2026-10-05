package com.example.financial.data.repository

import com.example.financial.data.local.dao.AccountDao
import com.example.financial.data.local.dao.AccountGroupDao
import com.example.financial.data.local.dao.TransactionDao
import com.example.financial.domain.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FinancialRepository(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val accountGroupDao: AccountGroupDao,
    private val budgetDao: com.example.financial.data.local.dao.BudgetDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val userId: String get() = auth.currentUser?.uid ?: "anonymous"

    private fun userDoc(collectionName: String, docId: String) =
        firestore.collection("users").document(userId).collection(collectionName).document(docId)

    suspend fun seedDefaultDataIfEmpty() {
        val currentAccounts = accountDao.getAllAccountsList()
        if (currentAccounts.isEmpty()) {
            val defaultAccount = Account(
                id = java.util.UUID.randomUUID().toString(),
                name = "Main Wallet",
                balance = "$0.00",
                type = AccountType.CASH_WALLET,
                color = androidx.compose.ui.graphics.Color(0xFF4CAF50)
            )
            addAccount(defaultAccount)
        }
    }

    suspend fun syncFromCloud() {
        val uid = auth.currentUser?.uid ?: return
        if (auth.currentUser?.isAnonymous == true) {
            seedDefaultDataIfEmpty()
            return
        }

        try {
            // 0. Ensure root user document exists so 'users' collection and document appear in Firebase Console
            val userData = mapOf(
                "uid" to uid,
                "email" to (auth.currentUser?.email ?: ""),
                "displayName" to (auth.currentUser?.displayName ?: ""),
                "lastLogin" to System.currentTimeMillis()
            )
            firestore.collection("users").document(uid).set(userData).await()

            seedDefaultDataIfEmpty()

            // 1. Upload existing local data to Firestore for this signed-in user
            val localAccounts = accountDao.getAllAccountsList().map { it.toDomain() }
            localAccounts.forEach { a ->
                accountDao.insertAccount(a.toEntity(uid))
                firestore.collection("users").document(uid).collection("accounts").document(a.id).set(a.toMap()).await()
            }

            val localGroups = accountGroupDao.getAllGroupsList().map { it.toDomain() }
            localGroups.forEach { g ->
                accountGroupDao.insertGroup(g.toEntity(uid))
                firestore.collection("users").document(uid).collection("account_groups").document(g.id).set(g.toMap()).await()
            }

            val localBudgets = budgetDao.getAllBudgetsList().map { it.toDomain() }
            localBudgets.forEach { b ->
                budgetDao.insertBudget(b.toEntity(uid))
                firestore.collection("users").document(uid).collection("budgets").document(b.id).set(b.toMap()).await()
            }

            val localBGroups = budgetDao.getAllBudgetGroupsList().map { it.toDomain() }
            localBGroups.forEach { bg ->
                budgetDao.insertBudgetGroup(bg.toEntity(uid))
                firestore.collection("users").document(uid).collection("budget_groups").document(bg.id).set(bg.toMap()).await()
            }

            val localTxs = transactionDao.getAllTransactionsList().map { it.toDomain() }
            localTxs.forEach { t ->
                transactionDao.insertTransaction(t.toEntity(uid))
                firestore.collection("users").document(uid).collection("transactions").document(t.id).set(t.toMap()).await()
            }

            // 2. Download any remote cloud data from Firestore to local Room
            val accountSnapshots = firestore.collection("users").document(uid).collection("accounts").get().await()
            val cloudAccounts = accountSnapshots.documents.mapNotNull { it.data?.let { data -> accountFromMap(data) } }
            cloudAccounts.forEach { accountDao.insertAccount(it.toEntity(uid)) }

            val groupSnapshots = firestore.collection("users").document(uid).collection("account_groups").get().await()
            val cloudGroups = groupSnapshots.documents.mapNotNull { it.data?.let { data -> accountGroupFromMap(data) } }
            cloudGroups.forEach { accountGroupDao.insertGroup(it.toEntity(uid)) }

            val budgetSnapshots = firestore.collection("users").document(uid).collection("budgets").get().await()
            val cloudBudgets = budgetSnapshots.documents.mapNotNull { it.data?.let { data -> budgetFromMap(data) } }
            cloudBudgets.forEach { budgetDao.insertBudget(it.toEntity(uid)) }

            val budgetGroupSnapshots = firestore.collection("users").document(uid).collection("budget_groups").get().await()
            val cloudBGroups = budgetGroupSnapshots.documents.mapNotNull { it.data?.let { data -> budgetGroupFromMap(data) } }
            cloudBGroups.forEach { budgetDao.insertBudgetGroup(it.toEntity(uid)) }

            val txSnapshots = firestore.collection("users").document(uid).collection("transactions").get().await()
            val cloudTxs = txSnapshots.documents.mapNotNull { it.data?.let { data -> transactionFromMap(data) } }
            cloudTxs.forEach { transactionDao.insertTransaction(it.toEntity(uid)) }

            seedDefaultDataIfEmpty()
        } catch (e: Exception) {
            android.util.Log.e("FinancialRepo", "Sync from cloud error: ${e.message}", e)
            seedDefaultDataIfEmpty()
        }
    }

    fun listenToCloudSync(scope: kotlinx.coroutines.CoroutineScope) {
        val uid = auth.currentUser?.uid ?: return
        if (auth.currentUser?.isAnonymous == true) return

        firestore.collection("users").document(uid).collection("accounts")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                scope.launch(Dispatchers.IO) {
                    val accounts = snapshot.documents.mapNotNull { doc -> doc.data?.let { accountFromMap(it) } }
                    accounts.forEach { accountDao.insertAccount(it.toEntity(uid)) }
                }
            }

        firestore.collection("users").document(uid).collection("transactions")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                scope.launch(Dispatchers.IO) {
                    val txs = snapshot.documents.mapNotNull { doc -> doc.data?.let { transactionFromMap(it) } }
                    txs.forEach { transactionDao.insertTransaction(it.toEntity(uid)) }
                }
            }

        firestore.collection("users").document(uid).collection("budgets")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                scope.launch(Dispatchers.IO) {
                    val budgets = snapshot.documents.mapNotNull { doc -> doc.data?.let { budgetFromMap(it) } }
                    budgets.forEach { budgetDao.insertBudget(it.toEntity(uid)) }
                }
            }
    }

    fun getTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions().map { entities -> entities.map { it.toDomain() } }.flowOn(Dispatchers.IO)
    fun getAccounts(): Flow<List<Account>> = accountDao.getAllAccounts().map { entities -> entities.map { it.toDomain() } }.flowOn(Dispatchers.IO)
    fun getAccountGroups(): Flow<List<AccountGroup>> = accountGroupDao.getAllGroups().map { entities -> entities.map { it.toDomain() } }.flowOn(Dispatchers.IO)
    fun getBudgetGroups(): Flow<List<BudgetGroup>> = budgetDao.getAllBudgetGroups().map { entities -> entities.map { it.toDomain() } }.flowOn(Dispatchers.IO)
    fun getRawBudgets(): Flow<List<Budget>> = budgetDao.getAllBudgets().map { entities -> entities.map { it.toDomain() } }.flowOn(Dispatchers.IO)

    fun getBudgets(): Flow<List<Budget>> = combine(budgetDao.getAllBudgets(), getTransactions()) { entities, allTransactions ->
        entities.map { entity ->
            val budget = entity.toDomain()
            val relevantTransactions = allTransactions.filter { t ->
                if (t.budgetId == budget.id) return@filter true
                if (t.budgetId != null) return@filter false
                
                val typeMatch = t.type == (if (budget.isIncome) TransactionType.INCOME else TransactionType.EXPENSE)
                val accMatch = budget.accountIds.isEmpty() || budget.accountIds.contains(t.fromAccountId)
                val catMatch = budget.categories.isEmpty() || budget.categories.any { it.equals(t.payee, true) || it.equals(t.description, true) }
                typeMatch && accMatch && catMatch
            }

            val now = System.currentTimeMillis()
            val periodMillis: Long = when (budget.frequencyUnit.lowercase()) {
                "day" -> 24L * 3600000; "week" -> 7L * 24 * 3600000; "year" -> 365L * 24 * 3600000; else -> 30L * 24 * 3600000
            }
            val timePassed = now - budget.startDate
            val periodsPassed = if (timePassed > 0) (timePassed / periodMillis).toInt() else 0
            val currentPeriodStart = budget.startDate + (periodsPassed * periodMillis)

            val spentInCurrentPeriod = relevantTransactions.filter { it.date >= currentPeriodStart }.sumOf { it.amount }

            var rollover = 0.0
            if (budget.rolloverEnabled && periodsPassed > 0) {
                val pastSpent = relevantTransactions.filter { it.date >= budget.startDate && it.date < currentPeriodStart }.sumOf { it.amount }
                val pastBudgeted = budget.amount * periodsPassed
                rollover = if (budget.isIncome) pastSpent - pastBudgeted else pastBudgeted - pastSpent
            }

            budget.copy(spent = spentInCurrentPeriod, remaining = budget.amount - spentInCurrentPeriod + rollover, progress = if (budget.amount > 0) (spentInCurrentPeriod / budget.amount).toFloat().coerceIn(0f, 1f) else 0f)
        }
    }.flowOn(Dispatchers.Default)

    fun getBalanceData(): Flow<BalanceData> = combine(getAccounts(), getTransactions()) { accounts, transactions ->
        val realTransactions = transactions.filter { it.budgetId == null }
        var total = 0.0; var liab = 0.0
        accounts.forEach { a -> val b = parseBalance(a.balance); if (b < 0) liab += kotlin.math.abs(b); total += b }
        val inc = realTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val exp = realTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        BalanceData(netWorth = formatBalance(total), liabilities = formatBalance(-liab), totalIncome = formatBalance(inc), totalExpenses = formatBalance(exp), monthlyBudget = 0f)
    }.flowOn(Dispatchers.Default)

    private fun parseBalance(s: String): Double = try { s.replace(",", ".").replace(Regex("[^0-9.-]"), "").toDouble() } catch (e: Exception) { 0.0 }
    private fun formatBalance(d: Double): String = String.format(java.util.Locale.getDefault(), "$%.2f", d)

    fun getCategorySpending(): Flow<List<CategorySpending>> = flowOf(emptyList())

    suspend fun addTransaction(t: Transaction) {
        transactionDao.insertTransaction(t.toEntity(userId))
        if (userId != "anonymous") {
            try { userDoc("transactions", t.id).set(t.toMap()).await() } catch (_: Exception) {}
        }
    }

    suspend fun updateTransaction(t: Transaction) {
        transactionDao.insertTransaction(t.toEntity(userId))
        if (userId != "anonymous") {
            try { userDoc("transactions", t.id).set(t.toMap()).await() } catch (_: Exception) {}
        }
    }

    suspend fun deleteTransaction(t: Transaction) {
        transactionDao.deleteTransaction(t.toEntity(userId))
        if (userId != "anonymous") {
            try { userDoc("transactions", t.id).delete().await() } catch (_: Exception) {}
        }
    }

    suspend fun addAccount(a: Account) {
        accountDao.insertAccount(a.toEntity(userId))
        if (userId != "anonymous") {
            try { userDoc("accounts", a.id).set(a.toMap()).await() } catch (_: Exception) {}
        }
    }

    suspend fun updateAccount(a: Account) {
        accountDao.updateAccount(a.toEntity(userId))
        if (userId != "anonymous") {
            try { userDoc("accounts", a.id).set(a.toMap()).await() } catch (_: Exception) {}
        }
    }

    suspend fun deleteAccount(a: Account) {
        accountDao.deleteAccount(a.toEntity(userId))
        if (userId != "anonymous") {
            try { userDoc("accounts", a.id).delete().await() } catch (_: Exception) {}
        }
    }

    suspend fun addAccountGroup(g: AccountGroup) {
        accountGroupDao.insertGroup(g.toEntity(userId))
        if (userId != "anonymous") {
            try { userDoc("account_groups", g.id).set(g.toMap()).await() } catch (_: Exception) {}
        }
    }

    suspend fun updateAccountGroup(g: AccountGroup) {
        accountGroupDao.updateGroup(g.toEntity(userId))
        if (userId != "anonymous") {
            try { userDoc("account_groups", g.id).set(g.toMap()).await() } catch (_: Exception) {}
        }
    }

    suspend fun deleteAccountGroup(g: AccountGroup) {
        accountGroupDao.deleteGroup(g.toEntity(userId))
        if (userId != "anonymous") {
            try { userDoc("account_groups", g.id).delete().await() } catch (_: Exception) {}
        }
    }

    suspend fun addBudget(b: Budget) {
        budgetDao.insertBudget(b.toEntity(userId))
        if (userId != "anonymous") {
            try { userDoc("budgets", b.id).set(b.toMap()).await() } catch (_: Exception) {}
        }
    }

    suspend fun updateBudget(b: Budget) {
        budgetDao.updateBudget(b.toEntity(userId))
        if (userId != "anonymous") {
            try { userDoc("budgets", b.id).set(b.toMap()).await() } catch (_: Exception) {}
        }
    }

    suspend fun deleteBudget(b: Budget) {
        budgetDao.deleteBudget(b.toEntity(userId))
        if (userId != "anonymous") {
            try { userDoc("budgets", b.id).delete().await() } catch (_: Exception) {}
        }
    }

    suspend fun addBudgetGroup(g: BudgetGroup) {
        budgetDao.insertBudgetGroup(g.toEntity(userId))
        if (userId != "anonymous") {
            try { userDoc("budget_groups", g.id).set(g.toMap()).await() } catch (_: Exception) {}
        }
    }

    suspend fun updateBudgetGroup(g: BudgetGroup) {
        budgetDao.updateBudgetGroup(g.toEntity(userId))
        if (userId != "anonymous") {
            try { userDoc("budget_groups", g.id).set(g.toMap()).await() } catch (_: Exception) {}
        }
    }

    suspend fun deleteBudgetGroup(g: BudgetGroup) {
        budgetDao.deleteBudgetGroup(g.toEntity(userId))
        if (userId != "anonymous") {
            try { userDoc("budget_groups", g.id).delete().await() } catch (_: Exception) {}
        }
    }
}
