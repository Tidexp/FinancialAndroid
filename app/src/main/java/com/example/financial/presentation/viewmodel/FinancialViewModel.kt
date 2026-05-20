package com.example.financial.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.financial.FinancialApplication
import com.example.financial.data.repository.AuthRepository
import com.example.financial.data.repository.FinancialRepository
import com.example.financial.domain.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val balanceData: BalanceData? = null,
    val accounts: List<Account> = emptyList(),
    val accountGroups: List<AccountGroup> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val remainingBudget: String = "$0.00",
    val totalBudgeted: String = "$0.00",
    val isLoading: Boolean = false,
    val authStatus: String = "Connecting...",
    val dbStatus: String = "Checking..."
)

data class StatisticsUiState(
    val categories: List<CategorySpending> = emptyList(),
    val graphData: List<Float> = listOf(0.4f, 0.7f, 0.5f, 0.9f, 0.6f, 0.8f, 0.4f),
    val isLoading: Boolean = false
)

class FinancialViewModel(
    private val repository: FinancialRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FinancialApplication)
                val database = application.database
                val firestore = FirebaseFirestore.getInstance()
                val auth = FirebaseAuth.getInstance()
                val repository = FinancialRepository(
                    database.transactionDao(),
                    database.accountDao(),
                    database.accountGroupDao(),
                    firestore,
                    auth
                )
                FinancialViewModel(repository, AuthRepository(auth))
            }
        }
    }

    private val _homeUiState = MutableStateFlow(HomeUiState(isLoading = true))
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

    private val _statsUiState = MutableStateFlow(StatisticsUiState(isLoading = true))
    val statsUiState: StateFlow<StatisticsUiState> = _statsUiState.asStateFlow()

    init {
        checkConnections()
        loadHomeData()
        loadStatsData()
    }

    private fun checkConnections() {
        viewModelScope.launch {
            // Check Firebase Auth
            if (authRepository.currentUser != null) {
                _homeUiState.update { it.copy(authStatus = "Connected (${authRepository.currentUser?.uid?.take(6)})") }
            } else {
                val result = authRepository.signInAnonymously()
                result.onSuccess { user ->
                    val status = if (user != null) "Connected (${user.uid.take(6)})" else "Auth Empty"
                    _homeUiState.update { it.copy(authStatus = status) }
                }.onFailure { error ->
                    _homeUiState.update { it.copy(authStatus = "Error: ${error.message?.take(20)}...") }
                    android.util.Log.e("FinancialViewModel", "Auth failed", error)
                }
            }
            
            // Check local DB (Room) via a simple query
            repository.getAccounts().take(1).collect {
                _homeUiState.update { it.copy(dbStatus = "Local DB Ready") }
            }
        }
    }

    fun addAccountGroup(name: String, iconName: String?, iconUri: String?, color: androidx.compose.ui.graphics.Color) {
        viewModelScope.launch {
            val group = AccountGroup(
                id = java.util.UUID.randomUUID().toString(),
                name = name,
                iconName = iconName,
                iconUri = iconUri,
                color = color
            )
            repository.addAccountGroup(group)
        }
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            combine(
                repository.getBalanceData(),
                repository.getAccounts(),
                repository.getAccountGroups(),
                repository.getTransactions(),
                repository.getBudgets()
            ) { balance, accounts, groups, transactions, budgets ->
                HomeUiState(
                    balanceData = balance,
                    accounts = accounts,
                    accountGroups = groups,
                    transactions = transactions,
                    budgets = budgets,
                    isLoading = false
                )
            }.flowOn(Dispatchers.IO)
             .collect { state ->
                _homeUiState.value = state
            }
        }
    }

    private fun loadStatsData() {
        viewModelScope.launch {
            repository.getCategorySpending()
                .flowOn(Dispatchers.IO)
                .collect { categories ->
                    _statsUiState.update { it.copy(categories = categories, isLoading = false) }
                }
        }
    }
}
