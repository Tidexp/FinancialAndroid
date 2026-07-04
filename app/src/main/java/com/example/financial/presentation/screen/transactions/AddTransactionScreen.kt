package com.example.financial.presentation.screen.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SwapHoriz
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
import com.example.financial.presentation.component.TransactionTypeIcon
import com.example.financial.presentation.screen.transactions.standard.*
import com.example.financial.presentation.screen.transactions.crypto.*
import com.example.financial.presentation.screen.transactions.investment.*
import kotlinx.coroutines.launch

@Composable
fun AddTransactionScreen(
    viewModel: com.example.financial.presentation.viewmodel.FinancialViewModel,
    account: Account?,
    initialType: String? = null,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.homeUiState.collectAsState()
    
    val tabs = remember(account?.type) {
        val list = mutableListOf("Expense", "Income", "Transfer", "Adjust Balance")
        if (account?.type == AccountType.FOREX) {
            list.add("Exchange")
        } else if (account?.type == AccountType.INVESTMENT) {
            list.add("Buy")
            list.add("Sell")
        }
        list
    }

    val pagerState = rememberPagerState { tabs.size }
    val scope = rememberCoroutineScope()
    var currentSaveAction by remember { mutableStateOf<(() -> Unit)?>(null) }

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
            .padding(horizontal = 12.dp)
    ) {
        Spacer(modifier = Modifier.height(23.dp))

        // --- TOP BAR (Unifying headers) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Close Button
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(50))
                    .size(40.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
            }

            // Tabs / Type Selector Icons
            Row(
                modifier = Modifier
                    .background(Color(0xFFE5E5EA), RoundedCornerShape(24.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
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
                    
                    TransactionTypeIcon(
                        icon = icon,
                        isSelected = pagerState.currentPage == index,
                        selectedColor = if (title == "Expense") Color.Red else Color.Black,
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(index) }
                        }
                    )
                }
            }

            // Unified Save Button
            Button(
                onClick = { currentSaveAction?.invoke() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3478F6)),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 24.dp),
                modifier = Modifier.height(40.dp),
                enabled = currentSaveAction != null
            ) {
                Text("Save", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Title (Dynamic)
        Text(
            text = tabs[pagerState.currentPage],
            fontSize = 24.sp,
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
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalAlignment = Alignment.Top
        ) { page ->
            when (tabs[page]) {
                "Expense" -> ExpenseScreen(
                    showHeader = false, 
                    account = account,
                    onSave = { transaction ->
                        viewModel.addTransaction(transaction)
                        onBackClick()
                    },
                    onRegisterSaveAction = { currentSaveAction = it }
                )
                "Income" -> IncomeScreen(
                    showHeader = false,
                    account = account,
                    onSave = { transaction ->
                        viewModel.addTransaction(transaction)
                        onBackClick()
                    },
                    onRegisterSaveAction = { currentSaveAction = it }
                )
                "Transfer" -> {
                    if (account?.type == AccountType.FOREX) {
                        CryptoTransferScreen(showHeader = false)
                    } else {
                        TransferScreen(
                            showHeader = false,
                            fromAccount = account,
                            allAccounts = uiState.accounts,
                            onSave = { transaction ->
                                viewModel.addTransaction(transaction)
                                onBackClick()
                            },
                            onRegisterSaveAction = { currentSaveAction = it }
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
                    onRegisterSaveAction = { currentSaveAction = it }
                )
                "Exchange" -> ExchangeScreen(showHeader = false)
                "Buy" -> BuyScreen(
                    showHeader = false,
                    account = account,
                    onSave = { transaction ->
                        viewModel.addTransaction(transaction)
                        onBackClick()
                    },
                    onRegisterSaveAction = { currentSaveAction = it }
                )
                "Sell" -> SellScreen(
                    showHeader = false,
                    account = account,
                    onSave = { transaction ->
                        viewModel.addTransaction(transaction)
                        onBackClick()
                    },
                    onRegisterSaveAction = { currentSaveAction = it }
                )
            }
        }
    }
}
