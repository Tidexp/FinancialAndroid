package com.example.financialtest.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financialtest.data.repository.FinancialRepository
import com.example.financialtest.domain.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val balanceData: BalanceData? = null,
    val accounts: List<Account> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false
)

data class StatisticsUiState(
    val categories: List<CategorySpending> = emptyList(),
    val graphData: List<Float> = listOf(0.4f, 0.7f, 0.5f, 0.9f, 0.6f, 0.8f, 0.4f),
    val isLoading: Boolean = false
)

class FinancialViewModel(
    private val repository: FinancialRepository = FinancialRepository()
) : ViewModel() {

    private val _homeUiState = MutableStateFlow(HomeUiState(isLoading = true))
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

    private val _statsUiState = MutableStateFlow(StatisticsUiState(isLoading = true))
    val statsUiState: StateFlow<StatisticsUiState> = _statsUiState.asStateFlow()

    init {
        loadHomeData()
        loadStatsData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            combine(
                repository.getBalanceData(),
                repository.getAccounts(),
                repository.getTransactions()
            ) { balance, accounts, transactions ->
                HomeUiState(
                    balanceData = balance,
                    accounts = accounts,
                    transactions = transactions,
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
