package com.example.financial.presentation.screen.budgets.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financial.domain.model.Budget
import com.example.financial.domain.model.BudgetGroup
import com.example.financial.presentation.viewmodel.FinancialViewModel
import kotlinx.coroutines.launch

@Composable
fun CreateBudgetScreen(
    viewModel: FinancialViewModel,
    initialIsIncome: Boolean = false,
    budgetId: String? = null,
    onCloseClick: () -> Unit
) {
    val uiState by viewModel.homeUiState.collectAsState()
    val existingBudget = remember(budgetId, uiState.budgets) {
        uiState.budgets.find { it.id == budgetId }
    }
    
    val tabs = listOf("Expense", "Income")
    val initialPage = when {
        existingBudget != null -> if (existingBudget.isIncome) 1 else 0
        initialIsIncome -> 1
        else -> 0
    }
    
    val pagerState = rememberPagerState(initialPage = initialPage) { tabs.size }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7))
            .statusBarsPadding()
    ) {
        // --- TOP BAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onCloseClick,
                modifier = Modifier
                    .background(Color(0xFFE5E5EA), CircleShape)
                    .size(36.dp)
            ) {
                Icon(Icons.Default.Close, null, tint = Color.Black, modifier = Modifier.size(20.dp))
            }

            // Tab Selector Pill (Chỉ hiện nếu không phải mode Edit)
            if (existingBudget == null) {
                Row(
                    modifier = Modifier
                        .background(Color(0xFFE5E5EA), RoundedCornerShape(24.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEachIndexed { index, title ->
                        val icon = if (title == "Expense") Icons.Outlined.RemoveCircleOutline else Icons.Outlined.AddCircleOutline
                        
                        Box(
                            modifier = Modifier
                                .background(
                                    if (pagerState.currentPage == index) Color.White else Color.Transparent,
                                    CircleShape
                                )
                                .clickable {
                                    scope.launch { pagerState.animateScrollToPage(index) }
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = title,
                                    tint = if (pagerState.currentPage == index) Color.Black else Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                                if (pagerState.currentPage == index) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = if (existingBudget.isIncome) "Edit Income Budget" else "Edit Expense Budget",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.width(36.dp))
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().weight(1f),
            userScrollEnabled = false,
            verticalAlignment = Alignment.Top
        ) { page ->
            if (tabs[page] == "Expense") {
                CreateExpenseBudgetScreen(
                    initialBudget = if (existingBudget?.isIncome == false) existingBudget else null,
                    onCloseClick = onCloseClick,
                    onSaveClick = { name, amount, isIncome, color, groupId, start, repeat, freqV, freqU, rollover, accountIds, categories ->
                        if (existingBudget != null) {
                            viewModel.updateBudget(
                                existingBudget.copy(
                                    name = name,
                                    amount = amount,
                                    isIncome = isIncome,
                                    color = color,
                                    budgetGroupId = groupId,
                                    startDate = start,
                                    repeatEnabled = repeat,
                                    frequencyValue = freqV,
                                    frequencyUnit = freqU,
                                    rolloverEnabled = rollover,
                                    accountIds = accountIds,
                                    categories = categories
                                )
                            )
                        } else {
                            viewModel.addBudget(name, amount, isIncome, color, groupId, start, repeat, freqV, freqU, rollover, accountIds, categories)
                        }
                        onCloseClick()
                    },
                    budgetGroups = uiState.budgetGroups,
                    accounts = uiState.accounts
                )
            } else {
                CreateIncomeBudgetScreen(
                    initialBudget = if (existingBudget?.isIncome == true) existingBudget else null,
                    onCloseClick = onCloseClick,
                    onSaveClick = { name, amount, isIncome, color, groupId, start, repeat, freqV, freqU, rollover, accountIds, categories ->
                        if (existingBudget != null) {
                            viewModel.updateBudget(
                                existingBudget.copy(
                                    name = name,
                                    amount = amount,
                                    isIncome = isIncome,
                                    color = color,
                                    budgetGroupId = groupId,
                                    startDate = start,
                                    repeatEnabled = repeat,
                                    frequencyValue = freqV,
                                    frequencyUnit = freqU,
                                    rolloverEnabled = rollover,
                                    accountIds = accountIds,
                                    categories = categories
                                )
                            )
                        } else {
                            viewModel.addBudget(name, amount, isIncome, color, groupId, start, repeat, freqV, freqU, rollover, accountIds, categories)
                        }
                        onCloseClick()
                    },
                    budgetGroups = uiState.budgetGroups,
                    accounts = uiState.accounts
                )
            }
        }
    }
}
