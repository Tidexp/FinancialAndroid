package com.example.financial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
fun MainApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

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
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.label
                        )
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
