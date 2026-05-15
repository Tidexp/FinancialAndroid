package com.example.financialtest.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.financialtest.FinancialApplication
import com.example.financialtest.data.repository.AuthRepository
import com.example.financialtest.data.repository.FinancialRepository
import com.example.financialtest.domain.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val balanceData: BalanceData? = null,
    val accounts: List<Account> = emptyList(),
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
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FinancialApplication)
                val database = application.database
                val repository = FinancialRepository(
                    database.transactionDao(),
                    database.accountDao()
                )
                FinancialViewModel(repository, AuthRepository())
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

    private fun loadHomeData() {
        viewModelScope.launch {
            combine(
                repository.getBalanceData(),
                repository.getAccounts(),
                repository.getTransactions(),
                repository.getBudgets()
            ) { balance, accounts, transactions, budgets ->
                HomeUiState(
                    balanceData = balance,
                    accounts = accounts,
                    transactions = transactions,
                    budgets = budgets,
                    isLoading = false
                )
            }.collect { state ->
                _homeUiState.value = state
            }
        }
    }

    private fun loadStatsData() {
        viewModelScope.launch {
            repository.getCategorySpending().collect { categories ->
                _statsUiState.update { it.copy(categories = categories, isLoading = false) }
            }
        }
    }
}
