package com.example.financial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financial.domain.model.AccountType
import com.example.financial.domain.model.TransactionStatus
import com.example.financial.presentation.viewmodel.FinancialViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.financial.presentation.navigation.NavGraph
import com.example.financial.presentation.navigation.Screen
import com.example.financial.presentation.navigation.bottomNavItems
import com.example.financial.ui.theme.FinancialTestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinancialTestTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp(viewModel: FinancialViewModel = viewModel(factory = FinancialViewModel.Factory)) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val uiState by viewModel.homeUiState.collectAsState()

    val hasDueItems = remember(uiState.transactions, uiState.accounts) {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        val endOfToday = today + 86400000
        
        val plannedDue = uiState.transactions.any { 
            it.status == TransactionStatus.PLANNED && it.date <= endOfToday 
        }
        
        val loansDue = uiState.accounts.any { account ->
            if (account.type == AccountType.LOAN && !account.firstDueDate.isNullOrBlank()) {
                try {
                    val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).parse(account.firstDueDate)?.time ?: 0L
                    date > 0 && date <= endOfToday
                } catch (e: Exception) { false }
            } else false
        }
        
        plannedDue || loansDue
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            bottomNavItems.forEach { screen ->
                val rootRoute = screen.route
                // Kiểm tra xem màn hình hiện tại có thuộc về tab này không (dựa trên route string)
                val currentRoute = navBackStackEntry?.destination?.route ?: ""
                val isTabActive = when (rootRoute) {
                    "accounts" -> currentRoute == "accounts" || currentRoute.contains("account", ignoreCase = true) || currentRoute == "select_account_type"
                    "budgets" -> currentRoute == "budgets" || currentRoute.contains("budget", ignoreCase = true)
                    else -> currentRoute == rootRoute
                }

                item(
                    icon = {
                        BadgedBox(
                            badge = {
                                if (screen == Screen.Scheduled && hasDueItems) {
                                    Badge(containerColor = Color.Red)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.label
                            )
                        }
                    },
                    label = { Text(screen.label) },
                    selected = isTabActive,
                    onClick = {
                        if (isTabActive) {
                            // Nếu nhấn lại vào Tab đang active và không phải ở màn hình gốc, quay về gốc
                            if (currentRoute != rootRoute) {
                                try {
                                    // Thử pop về root của tab
                                    navController.popBackStack(rootRoute, inclusive = false)
                                } catch (e: Exception) {
                                    // Fallback
                                    navController.navigate(rootRoute) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        } else {
                            // Chuyển sang Tab mới
                            navController.navigate(rootRoute) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    )
{
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NavGraph(navController = navController)
        }
    }
}
