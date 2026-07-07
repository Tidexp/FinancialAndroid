package com.example.financial.presentation.screen.budgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financial.domain.model.*
import com.example.financial.presentation.screen.budgets.setup.*
import com.example.financial.presentation.viewmodel.FinancialViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddBudgetTransactionScreen(
    viewModel: FinancialViewModel,
    budgetId: String,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.homeUiState.collectAsState()
    val budget = uiState.budgets.find { it.id == budgetId }
    
    val tabs = remember(budget) {
        if (budget?.isIncome == true) listOf("Income", "Budget Transfer")
        else listOf("Expense", "Budget Transfer")
    }

    val pagerState = rememberPagerState { tabs.size }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF2F2F7)).statusBarsPadding()) {
        // --- TOP BAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .background(Color(0xFFE5E5EA), CircleShape)
                    .size(36.dp)
            ) {
                Icon(Icons.Default.Close, null, tint = Color.Black, modifier = Modifier.size(20.dp))
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
                        else -> Icons.Default.SwapHoriz
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
                            .padding(horizontal = 12.dp, vertical = 6.dp),
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
            Spacer(modifier = Modifier.width(36.dp))
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalAlignment = Alignment.Top,
            userScrollEnabled = false
        ) { page ->
            when (tabs[page]) {
                "Expense", "Income" -> {
                    if (budget != null) {
                        BudgetEntryForm(
                            isIncome = tabs[page] == "Income",
                            budget = budget,
                            allAccounts = uiState.accounts,
                            onSave = { 
                                viewModel.addTransaction(it)
                                onBackClick()
                            }
                        )
                    }
                }
                "Budget Transfer" -> {
                    BudgetTransferForm(
                        initialFromBudget = budget,
                        allBudgets = uiState.budgets,
                        onSave = { from, to, amt, memo, date ->
                            viewModel.transferBudget(from, to, amt, memo, date)
                            onBackClick()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetEntryForm(
    isIncome: Boolean,
    budget: Budget,
    allAccounts: List<Account>,
    onSave: (Transaction) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var memo by remember { mutableStateOf("") }
    var date by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var status by remember { mutableStateOf(TransactionStatus.CLEARED) }
    
    var selectedAccount by remember { mutableStateOf<Account?>(null) }
    var selectedCategory by remember { mutableStateOf(budget.categories.firstOrNull() ?: "General") }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showAccountPicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = date)
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(if(isIncome) "New Income Entry" else "New Expense Entry", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && amt > 0) {
                        onSave(Transaction(
                            id = UUID.randomUUID().toString(),
                            type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE,
                            fromAccountId = selectedAccount?.id ?: "budget_virtual",
                            amount = amt,
                            payee = name,
                            description = description,
                            memo = memo,
                            date = date,
                            budgetId = budget.id,
                            status = status
                        ))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3478F6)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        }

        BudgetSectionHeader("Describe your entry")
        BudgetCard {
            BudgetInputRow(Icons.Default.Badge, "Name", name) { name = it }
            BudgetDivider()
            BudgetInputRow(Icons.Default.Description, "Description", description) { description = it }
            BudgetDivider()
            ListItem(
                leadingContent = { Icon(Icons.Default.MoveToInbox, null, tint = Color.Gray) },
                headlineContent = {
                    TextField(
                        value = amount,
                        onValueChange = { amount = it },
                        placeholder = { Text("0.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                },
                trailingContent = { Text("USD", color = Color.Gray) }
            )
        }

        BudgetSectionHeader("Which transactions should be included?")
        BudgetCard {
            BudgetClickableRow(
                icon = Icons.Default.AccountBalanceWallet,
                label = "Account",
                secondaryValue = selectedAccount?.name ?: "Budget-only entry",
                onClick = { showAccountPicker = true }
            )
            BudgetDivider()
            BudgetClickableRow(
                icon = Icons.Outlined.Folder,
                label = "Category",
                secondaryValue = selectedCategory,
                onClick = { showCategoryPicker = true }
            )
        }

        BudgetSectionHeader("Configure status & date")
        BudgetCard {
            ListItem(
                leadingContent = { Icon(Icons.Default.CheckCircleOutline, null, tint = Color.Gray) },
                headlineContent = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = status == TransactionStatus.CLEARED,
                            onClick = { status = TransactionStatus.CLEARED },
                            label = { Text("Cleared") }
                        )
                        FilterChip(
                            selected = status == TransactionStatus.PENDING,
                            onClick = { status = TransactionStatus.PENDING },
                            label = { Text("Pending") }
                        )
                    }
                }
            )
            BudgetDivider()
            BudgetClickableRow(
                icon = Icons.Default.CalendarToday,
                label = "Date",
                secondaryValue = dateFormatter.format(Date(date)),
                onClick = { showDatePicker = true }
            )
        }
        
        BudgetSectionHeader("Additional info")
        BudgetCard {
            BudgetInputRow(Icons.Default.Notes, "Memo (Optional)", memo) { memo = it }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { date = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showAccountPicker) {
        val budgetAccounts = allAccounts.filter { budget.accountIds.isEmpty() || budget.accountIds.contains(it.id) }
        ModalBottomSheet(onDismissRequest = { showAccountPicker = false }) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth().padding(bottom = 32.dp)) {
                Text("Select Account", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                ListItem(
                    modifier = Modifier.clickable { 
                        selectedAccount = null
                        showAccountPicker = false 
                    },
                    headlineContent = { Text("Budget-only entry") },
                    supportingContent = { Text("No real account affected") }
                )
                LazyColumn {
                    items(budgetAccounts) { account ->
                        ListItem(
                            modifier = Modifier.clickable { 
                                selectedAccount = account
                                showAccountPicker = false 
                            },
                            headlineContent = { Text(account.name) },
                            leadingContent = { Icon(Icons.Default.AccountBalance, null, tint = account.color) }
                        )
                    }
                }
            }
        }
    }

    if (showCategoryPicker) {
        ModalBottomSheet(onDismissRequest = { showCategoryPicker = false }) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth().padding(bottom = 32.dp)) {
                Text("Select Category", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn {
                    items(budget.categories) { cat ->
                        ListItem(
                            modifier = Modifier.clickable { 
                                selectedCategory = cat
                                showCategoryPicker = false 
                            },
                            headlineContent = { Text(cat) },
                            leadingContent = { Icon(Icons.Default.Folder, null, tint = Color.Gray) }
                        )
                    }
                    if (budget.categories.isEmpty()) {
                        item { Text("No categories defined for this budget.", color = Color.Gray) }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetTransferForm(
    initialFromBudget: Budget?,
    allBudgets: List<Budget>,
    onSave: (String, String, Double, String, Long) -> Unit
) {
    var fromBudget by remember { mutableStateOf(initialFromBudget) }
    var toBudget by remember { mutableStateOf<Budget?>(null) }
    var amount by remember { mutableStateOf("") }
    var memo by remember { mutableStateOf("") }
    var date by remember { mutableLongStateOf(System.currentTimeMillis()) }
    
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = date)
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Budget Transfer", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    if (fromBudget != null && toBudget != null && amt > 0) {
                        onSave(fromBudget!!.id, toBudget!!.id, amt, memo, date)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3478F6)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        }

        BudgetSectionHeader("Select Budgets")
        BudgetCard {
            BudgetClickableRow(Icons.Default.MoveToInbox, "From", fromBudget?.name ?: "Select source") { showFromPicker = true }
            BudgetDivider()
            BudgetClickableRow(Icons.Default.MoveToInbox, "To", toBudget?.name ?: "Select target") { showToPicker = true }
        }

        BudgetSectionHeader("Amount & Description")
        BudgetCard {
            ListItem(
                leadingContent = { Icon(Icons.Default.AttachMoney, null, tint = Color.Gray) },
                headlineContent = {
                    TextField(
                        value = amount,
                        onValueChange = { amount = it },
                        placeholder = { Text("0.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                },
                trailingContent = { Text("USD", color = Color.Gray) }
            )
            BudgetDivider()
            BudgetInputRow(Icons.Default.Description, "Memo (Optional)", memo) { memo = it }
        }

        BudgetSectionHeader("Configure date")
        BudgetCard {
            BudgetClickableRow(Icons.Default.CalendarToday, "Date", dateFormatter.format(Date(date))) { showDatePicker = true }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showFromPicker) {
        ModalBottomSheet(onDismissRequest = { showFromPicker = false }) {
            BudgetPickerList(allBudgets) { fromBudget = it; showFromPicker = false }
        }
    }
    if (showToPicker) {
        ModalBottomSheet(onDismissRequest = { showToPicker = false }) {
            BudgetPickerList(allBudgets) { toBudget = it; showToPicker = false }
        }
    }
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { date = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun BudgetPickerList(budgets: List<Budget>, onSelect: (Budget) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp).padding(bottom = 32.dp)) {
        Text("Select Budget", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(budgets) { budget ->
                ListItem(
                    modifier = Modifier.clickable { onSelect(budget) },
                    headlineContent = { Text(budget.name) },
                    leadingContent = { Icon(Icons.Default.Circle, null, tint = budget.color) }
                )
            }
        }
    }
}
