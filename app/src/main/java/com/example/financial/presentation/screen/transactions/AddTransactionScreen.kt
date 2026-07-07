package com.example.financial.presentation.screen.transactions

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
import com.example.financial.domain.model.Account
import com.example.financial.domain.model.AccountType
import com.example.financial.presentation.screen.transactions.standard.*
import com.example.financial.presentation.screen.transactions.crypto.*
import com.example.financial.presentation.screen.transactions.investment.*
import com.example.financial.presentation.screen.scheduled.setup.*
import kotlinx.coroutines.launch

@Composable
fun AddTransactionScreen(
    viewModel: com.example.financial.presentation.viewmodel.FinancialViewModel,
    account: com.example.financial.domain.model.Account?,
    initialType: String? = null,
    isScheduled: Boolean = false,
    existingTransaction: com.example.financial.domain.model.Transaction? = null,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.homeUiState.collectAsState()
    
    val tabs = remember(account?.type, isScheduled) {
        val list = mutableListOf("Expense", "Income", "Transfer")
        if (!isScheduled) {
            if (account?.type == AccountType.FOREX) {
                list.add("Exchange")
            } else if (account?.type == AccountType.INVESTMENT) {
                list.add("Buy")
                list.add("Sell")
            }
            list.add("Adjust Balance")
        }
        list
    }

    val pagerState = rememberPagerState { tabs.size }
    val scope = rememberCoroutineScope()
    val saveActions = remember { mutableStateMapOf<Int, () -> Unit>() }

    LaunchedEffect(initialType) {
        if (initialType != null) {
            val index = tabs.indexOf(initialType)
            if (index != -1) {
                pagerState.scrollToPage(index)
            }
        }
    }

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
            // Close Button
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .background(Color(0xFFE5E5EA), CircleShape)
                    .size(36.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black, modifier = Modifier.size(20.dp))
            }

            // Tab Selector Pill
            Row(
                modifier = Modifier
                    .background(Color(0xFFE5E5EA), RoundedCornerShape(24.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, title ->
                    val icon = when (title) {
                        "Expense" -> Icons.Outlined.RemoveCircleOutline
                        "Income" -> Icons.Outlined.AddCircleOutline
                        "Transfer" -> Icons.Default.SwapHoriz
                        "Adjust Balance" -> Icons.Outlined.DragHandle
                        "Exchange" -> Icons.Outlined.Cached
                        "Buy" -> Icons.Outlined.ArrowCircleUp
                        "Sell" -> Icons.Outlined.ArrowCircleDown
                        else -> Icons.Outlined.QuestionMark
                    }
                    
                    Box(
                        modifier = Modifier
                            .background(
                                if (pagerState.currentPage == index) Color.White else Color.Transparent,
                                CircleShape
                            )
                            .clickable {
                                scope.launch { pagerState.animateScrollToPage(index) }
                            }
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = if (pagerState.currentPage == index) Color.Black else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Save Button
            Button(
                onClick = { saveActions[pagerState.currentPage]?.invoke() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3478F6)),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                modifier = Modifier.height(36.dp),
                enabled = saveActions.containsKey(pagerState.currentPage)
            ) {
                Text("Save", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
            }
        }

        // Title
        Text(
            text = tabs[pagerState.currentPage],
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            userScrollEnabled = true,
            verticalAlignment = Alignment.Top
        ) { page ->
            val screenType = tabs[page]
            when (screenType) {
                "Expense" -> {
                    if (isScheduled) {
                        ScheduledExpenseScreen(
                            viewModel = viewModel,
                            showHeader = false,
                            account = account,
                            existingTransaction = existingTransaction,
                            onSave = { transaction ->
                                viewModel.addTransaction(transaction)
                                onBackClick()
                            },
                            onRegisterSaveAction = { saveActions[page] = it }
                        )
                    } else {
                        ExpenseScreen(
                            showHeader = false,
                            account = account,
                            allAccounts = uiState.accounts,
                            onSave = { transaction ->
                                viewModel.addTransaction(transaction)
                                onBackClick()
                            },
                            onRegisterSaveAction = { saveActions[page] = it }
                        )
                    }
                }
                "Income" -> {
                    if (isScheduled) {
                        ScheduledIncomeScreen(
                            viewModel = viewModel,
                            showHeader = false,
                            account = account,
                            existingTransaction = existingTransaction,
                            onSave = { transaction ->
                                viewModel.addTransaction(transaction)
                                onBackClick()
                            },
                            onRegisterSaveAction = { saveActions[page] = it }
                        )
                    } else {
                        IncomeScreen(
                            showHeader = false,
                            account = account,
                            allAccounts = uiState.accounts,
                            onSave = { transaction ->
                                viewModel.addTransaction(transaction)
                                onBackClick()
                            },
                            onRegisterSaveAction = { saveActions[page] = it }
                        )
                    }
                }
                "Transfer" -> {
                    if (isScheduled) {
                        ScheduledTransferScreen(
                            viewModel = viewModel,
                            showHeader = false,
                            fromAccount = account,
                            existingTransaction = existingTransaction,
                            onSave = { transaction ->
                                viewModel.addTransaction(transaction)
                                onBackClick()
                            },
                            onRegisterSaveAction = { saveActions[page] = it }
                        )
                    } else {
                        TransferScreen(
                            showHeader = false,
                            fromAccount = account,
                            allAccounts = uiState.accounts,
                            onSave = { transaction ->
                                viewModel.addTransaction(transaction)
                                onBackClick()
                            },
                            onRegisterSaveAction = { saveActions[page] = it }
                        )
                    }
                }
                "Adjust Balance" -> AdjustBalanceScreen(
                    showHeader = false,
                    account = account,
                    onSave = { transaction ->
                        viewModel.addTransaction(transaction)
                        onBackClick()
                    },
                    onRegisterSaveAction = { saveActions[page] = it }
                )
                "Exchange" -> ExchangeScreen(showHeader = false)
                "Buy" -> BuyScreen(
                    showHeader = false,
                    account = account,
                    onSave = { transaction ->
                        viewModel.addTransaction(transaction)
                        onBackClick()
                    },
                    onRegisterSaveAction = { saveActions[page] = it }
                )
                "Sell" -> SellScreen(
                    showHeader = false,
                    account = account,
                    onSave = { transaction ->
                        viewModel.addTransaction(transaction)
                        onBackClick()
                    },
                    onRegisterSaveAction = { saveActions[page] = it }
                )
            }
        }
    }
}
