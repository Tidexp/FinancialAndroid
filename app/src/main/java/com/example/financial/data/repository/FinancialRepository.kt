package com.example.financial.data.repository

import com.example.financial.data.local.dao.AccountDao
import com.example.financial.data.local.dao.AccountGroupDao
import com.example.financial.data.local.dao.ChatMessageDao
import com.example.financial.data.local.dao.TransactionDao
import com.example.financial.data.local.entity.TransactionEntity
import com.example.financial.domain.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.ui.graphics.Color
import java.util.UUID

class FinancialRepository(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val accountGroupDao: AccountGroupDao,
    private val budgetDao: com.example.financial.data.local.dao.BudgetDao,
    private val chatMessageDao: ChatMessageDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val userId: String get() = auth.currentUser?.uid ?: "anonymous"

    private fun userDoc(collectionName: String, docId: String) =
        firestore.collection("users").document(userId).collection(collectionName).document(docId)

    suspend fun seedDefaultDataIfEmpty() {
        val currentAccounts = accountDao.getAllAccountsList(userId)
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
            val localAccounts = accountDao.getAllAccountsList(uid).map { it.toDomain() }
            localAccounts.forEach { a ->
                accountDao.insertAccount(a.toEntity(uid))
                firestore.collection("users").document(uid).collection("accounts").document(a.id).set(a.toMap()).await()
            }

            val localGroups = accountGroupDao.getAllGroupsList(uid).map { it.toDomain() }
            localGroups.forEach { g ->
                accountGroupDao.insertGroup(g.toEntity(uid))
                firestore.collection("users").document(uid).collection("account_groups").document(g.id).set(g.toMap()).await()
            }

            val localBudgets = budgetDao.getAllBudgetsList(uid).map { it.toDomain() }
            localBudgets.forEach { b ->
                budgetDao.insertBudget(b.toEntity(uid))
                firestore.collection("users").document(uid).collection("budgets").document(b.id).set(b.toMap()).await()
            }

            val localBGroups = budgetDao.getAllBudgetGroupsList(uid).map { it.toDomain() }
            localBGroups.forEach { bg ->
                budgetDao.insertBudgetGroup(bg.toEntity(uid))
                firestore.collection("users").document(uid).collection("budget_groups").document(bg.id).set(bg.toMap()).await()
            }

            val localTxs = transactionDao.getAllTransactionsList(uid).map { it.toDomain() }
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

            val chatSnapshots = firestore.collection("users").document(uid).collection("chat_messages").get().await()
            val cloudMsgs = chatSnapshots.documents.mapNotNull { it.data?.let { data -> chatMessageFromMap(data) } }
            cloudMsgs.forEach { chatMessageDao.insertMessage(it.toEntity(uid)) }

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

        firestore.collection("users").document(uid).collection("chat_messages")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                scope.launch(Dispatchers.IO) {
                    val msgs = snapshot.documents.mapNotNull { doc -> doc.data?.let { chatMessageFromMap(it) } }
                    msgs.forEach { chatMessageDao.insertMessage(it.toEntity(uid)) }
                }
            }
    }

    fun getChatMessages(userId: String): Flow<List<ChatMessage>> =
        chatMessageDao.getMessagesForUser(userId).map { entities -> entities.map { it.toDomain() } }.flowOn(Dispatchers.IO)

    suspend fun addChatMessage(message: ChatMessage) {
        val currentUser = auth.currentUser
        val messageUserId = currentUser?.uid ?: "anonymous"
        chatMessageDao.insertMessage(message.toEntity(messageUserId))
        if (currentUser != null && !currentUser.isAnonymous) {
            firestore.collection("users").document(currentUser.uid)
                .collection("chat_messages").document(message.id)
                .set(message.toMap()).await()
        }
    }

    suspend fun addLocalChatMessage(message: ChatMessage) {
        chatMessageDao.insertMessage(message.toEntity(auth.currentUser?.uid ?: "anonymous"))
    }

    suspend fun clearChatMessages() {
        val currentUser = auth.currentUser
        val messageUserId = currentUser?.uid ?: "anonymous"

        if (currentUser != null && !currentUser.isAnonymous) {
            val chatMessages = firestore.collection("users").document(currentUser.uid)
                .collection("chat_messages")
            val messages = chatMessages.get().await()
            messages.documents.chunked(450).forEach { documents ->
                val batch = firestore.batch()
                documents.forEach { document -> batch.delete(document.reference) }
                batch.commit().await()
            }
        }

        chatMessageDao.clearMessagesForUser(messageUserId)
    }

    fun getTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions(userId).map { entities -> entities.map { it.toDomain() } }.flowOn(Dispatchers.IO)
    fun getReportTransactions(startDateMillis: Long): Flow<List<Transaction>> =
        transactionDao.getReportTransactions(userId, startDateMillis)
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)
    fun getAccounts(): Flow<List<Account>> = accountDao.getAllAccounts(userId).map { entities -> entities.map { it.toDomain() } }.flowOn(Dispatchers.IO)
    fun getAccountGroups(): Flow<List<AccountGroup>> = accountGroupDao.getAllGroups(userId).map { entities -> entities.map { it.toDomain() } }.flowOn(Dispatchers.IO)
    fun getBudgetGroups(): Flow<List<BudgetGroup>> = budgetDao.getAllBudgetGroups(userId).map { entities -> entities.map { it.toDomain() } }.flowOn(Dispatchers.IO)
    fun getRawBudgets(): Flow<List<Budget>> = budgetDao.getAllBudgets(userId).map { entities -> entities.map { it.toDomain() } }.flowOn(Dispatchers.IO)

    fun getBudgets(): Flow<List<Budget>> = combine(budgetDao.getAllBudgets(userId), getTransactions()) { entities, allTransactions ->
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

    suspend fun seedGuestPerformanceData(count: Int) {
        val currentUser = auth.currentUser
        if (currentUser?.isAnonymous != true) {
            throw IllegalStateException("Performance data can only be created for Guest accounts.")
        }
        val guestUserId = currentUser.uid
        transactionDao.deletePerformanceTransactions(guestUserId)
        budgetDao.deletePerformanceBudgets(guestUserId)
        accountDao.deletePerformanceAccounts(guestUserId)
        val accountIds = (1..5).map { "__perf_account_$it" }

        val categories = listOf("Food", "Transport", "Shopping", "Rent", "Utilities")
        val now = System.currentTimeMillis()
        val dayMillis = 24L * 60 * 60 * 1000
        val accountBalances = mutableMapOf(
            accountIds[0] to 10_000_000.0,
            accountIds[1] to 8_000_000.0,
            accountIds[2] to 6_000_000.0,
            accountIds[3] to 2_000_000.0,
            accountIds[4] to -500_000.0
        )

        val performanceTransactions = ArrayList<TransactionEntity>(500)
        for (index in 0 until count) {
            val isIncome = index % 5 == 0
            val category = categories[index % categories.size]
            val accountId = accountIds[index % accountIds.size]
            val amount = if (isIncome) {
                100000.0 + (index % 500) * 1000.0
            } else {
                10000.0 + (index % 500) * 250.0
            }
            accountBalances[accountId] =
                accountBalances.getValue(accountId) + if (isIncome) amount else -amount

            performanceTransactions.add(
                Transaction(
                    id = "__perf_tx_${UUID.nameUUIDFromBytes("guest-$guestUserId-$index".toByteArray()).toString()}",
                    type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE,
                    fromAccountId = accountId,
                    amount = amount,
                    categoryId = if (isIncome) null else category,
                    payee = if (isIncome) "Performance Salary" else category,
                    description = "Performance test transaction $index",
                    date = now - (index % 730).toLong() * dayMillis,
                    status = TransactionStatus.CLEARED,
                    budgetId = null
                ).toEntity(guestUserId)
            )

            if (performanceTransactions.size == 500) {
                transactionDao.insertTransactions(performanceTransactions)
                performanceTransactions.clear()
            }
        }

        val accountTypes = listOf(
            AccountType.CASH_WALLET,
            AccountType.CHECKING,
            AccountType.SAVINGS,
            AccountType.CREDIT,
            AccountType.LOAN
        )
        val accountColors = listOf(
            Color(0xFF4CAF50),
            Color(0xFF2196F3),
            Color(0xFFE91E63),
            Color(0xFFFF9800),
            Color(0xFF9C27B0)
        )
        val performanceAccounts = accountIds.mapIndexed { index, accountId ->
            Account(
                id = accountId,
                name = "Performance Account ${index + 1}",
                balance = formatBalance(accountBalances.getValue(accountId)),
                type = accountTypes[index],
                color = accountColors[index]
            ).toEntity(guestUserId)
        }
        accountDao.insertAccounts(performanceAccounts)

        if (performanceTransactions.isNotEmpty()) {
            transactionDao.insertTransactions(performanceTransactions)
        }

        val performanceBudgets = categories.mapIndexed { index, category ->
            Budget(
                id = "__perf_bud_$index",
                name = "Performance $category",
                amount = 50_000_000.0,
                isIncome = false,
                color = accountColors[index],
                startDate = now - 365L * 24 * 60 * 60 * 1000,
                frequencyUnit = "month",
                rolloverEnabled = false,
                accountIds = accountIds,
                categories = listOf(category),
                spent = 0.0
            ).toEntity(guestUserId)
        }
        budgetDao.insertBudgets(performanceBudgets)
    }

    suspend fun clearGuestPerformanceData() {
        val currentUser = auth.currentUser
        if (currentUser?.isAnonymous != true) {
            throw IllegalStateException("Performance data can only be cleared for Guest accounts.")
        }
        transactionDao.deletePerformanceTransactions(currentUser.uid)
        budgetDao.deletePerformanceBudgets(currentUser.uid)
        accountDao.deletePerformanceAccounts(currentUser.uid)
    }

    suspend fun addTransaction(t: Transaction) {
        transactionDao.insertTransaction(t.toEntity(userId))
        if (auth.currentUser?.isAnonymous == false) {
            try { userDoc("transactions", t.id).set(t.toMap()).await() } catch (_: Exception) {}
        }
    }

    suspend fun updateTransaction(t: Transaction) {
        transactionDao.insertTransaction(t.toEntity(userId))
        if (auth.currentUser?.isAnonymous == false) {
            try { userDoc("transactions", t.id).set(t.toMap()).await() } catch (_: Exception) {}
        }
    }

    suspend fun deleteTransaction(t: Transaction) {
        transactionDao.deleteTransaction(t.toEntity(userId))
        if (auth.currentUser?.isAnonymous == false) {
            try { userDoc("transactions", t.id).delete().await() } catch (_: Exception) {}
        }
    }

    suspend fun addAccount(a: Account) {
        accountDao.insertAccount(a.toEntity(userId))
        if (auth.currentUser?.isAnonymous == false) {
            try { userDoc("accounts", a.id).set(a.toMap()).await() } catch (_: Exception) {}
        }
    }

    suspend fun updateAccount(a: Account) {
        accountDao.updateAccount(a.toEntity(userId))
        if (auth.currentUser?.isAnonymous == false) {
            try { userDoc("accounts", a.id).set(a.toMap()).await() } catch (_: Exception) {}
        }
    }

    suspend fun deleteAccount(a: Account) {
        accountDao.deleteAccount(a.toEntity(userId))
        if (auth.currentUser?.isAnonymous == false) {
            try { userDoc("accounts", a.id).delete().await() } catch (_: Exception) {}
        }
    }

    suspend fun addAccountGroup(g: AccountGroup) {
        accountGroupDao.insertGroup(g.toEntity(userId))
        if (auth.currentUser?.isAnonymous == false) {
            try { userDoc("account_groups", g.id).set(g.toMap()).await() } catch (_: Exception) {}
        }
    }

    suspend fun updateAccountGroup(g: AccountGroup) {
        accountGroupDao.updateGroup(g.toEntity(userId))
        if (auth.currentUser?.isAnonymous == false) {
            try { userDoc("account_groups", g.id).set(g.toMap()).await() } catch (_: Exception) {}
        }
    }

    suspend fun deleteAccountGroup(g: AccountGroup) {
        accountGroupDao.deleteGroup(g.toEntity(userId))
        if (auth.currentUser?.isAnonymous == false) {
            try { userDoc("account_groups", g.id).delete().await() } catch (_: Exception) {}
        }
    }

    suspend fun addBudget(b: Budget) {
        budgetDao.insertBudget(b.toEntity(userId))
        if (auth.currentUser?.isAnonymous == false) {
            try { userDoc("budgets", b.id).set(b.toMap()).await() } catch (_: Exception) {}
        }
    }

    suspend fun updateBudget(b: Budget) {
        budgetDao.updateBudget(b.toEntity(userId))
        if (auth.currentUser?.isAnonymous == false) {
            try { userDoc("budgets", b.id).set(b.toMap()).await() } catch (_: Exception) {}
        }
    }

    suspend fun deleteBudget(b: Budget) {
        budgetDao.deleteBudget(b.toEntity(userId))
        if (auth.currentUser?.isAnonymous == false) {
            try { userDoc("budgets", b.id).delete().await() } catch (_: Exception) {}
        }
    }

    suspend fun addBudgetGroup(g: BudgetGroup) {
        budgetDao.insertBudgetGroup(g.toEntity(userId))
        if (auth.currentUser?.isAnonymous == false) {
            try { userDoc("budget_groups", g.id).set(g.toMap()).await() } catch (_: Exception) {}
        }
    }

    suspend fun updateBudgetGroup(g: BudgetGroup) {
        budgetDao.updateBudgetGroup(g.toEntity(userId))
        if (auth.currentUser?.isAnonymous == false) {
            try { userDoc("budget_groups", g.id).set(g.toMap()).await() } catch (_: Exception) {}
        }
    }

    suspend fun deleteBudgetGroup(g: BudgetGroup) {
        budgetDao.deleteBudgetGroup(g.toEntity(userId))
        if (auth.currentUser?.isAnonymous == false) {
            try { userDoc("budget_groups", g.id).delete().await() } catch (_: Exception) {}
        }
    }
}
