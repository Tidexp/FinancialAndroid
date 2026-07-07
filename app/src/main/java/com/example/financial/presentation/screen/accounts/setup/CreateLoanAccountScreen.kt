package com.example.financial.presentation.screen.accounts.setup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.financial.domain.model.AccountGroup
import com.example.financial.domain.model.Budget
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import com.example.financial.domain.model.Account
import java.text.SimpleDateFormat
import java.util.*

enum class LoanSubPage {
    MAIN, PAYMENT_PLAN, PAYMENT_SCHEDULE
}

data class PaymentScheduleItem(
    val month: Int,
    val date: String,
    val payment: Double,
    val interest: Double,
    val principal: Double,
    val remainingBalance: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateLoanAccountScreen(
    accountId: String? = null,
    onBackClick: () -> Unit,
    onSaveClick: (
        name: String,
        principal: String,
        apr: String,
        duration: String,
        startDate: String,
        firstDueDate: String,
        groupId: String?,
        additionalInfo: String,
        monitoredByBudgetId: String?,
        paymentAccountId: String?,
        paymentCategory: String?,
        paymentPayee: String?
    ) -> Unit,
    onUpdateClick: (Account) -> Unit = {},
    groups: List<AccountGroup> = emptyList(),
    budgets: List<Budget> = emptyList(),
    accounts: List<Account> = emptyList()
) {
    val existingAccount = remember(accountId, accounts) { accounts.find { it.id == accountId } }
    val paymentsMade = existingAccount?.paymentsMade ?: 0

    var currentSubPage by remember { mutableStateOf(LoanSubPage.MAIN) }

    BackHandler(enabled = currentSubPage != LoanSubPage.MAIN) {
        currentSubPage = when (currentSubPage) {
            LoanSubPage.PAYMENT_SCHEDULE -> LoanSubPage.PAYMENT_PLAN
            LoanSubPage.PAYMENT_PLAN -> LoanSubPage.MAIN
            else -> LoanSubPage.MAIN
        }
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Basic", "Advanced")

    var accountName by remember { mutableStateOf(existingAccount?.name ?: "") }
    var principalAmount by remember { mutableStateOf(existingAccount?.principalAmount ?: "0,00 USD") }
    var apr by remember { mutableStateOf(existingAccount?.apr ?: "") }
    var durationMonths by remember { mutableStateOf(existingAccount?.duration ?: "12") }
    
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val initialDate = dateFormatter.format(Date())
    
    var startDate by remember { mutableStateOf(existingAccount?.startDate ?: initialDate) }
    var firstDueDate by remember { mutableStateOf(existingAccount?.firstDueDate ?: initialDate) }

    val startDatePickerState = rememberDatePickerState()
    var showStartDatePicker by remember { mutableStateOf(false) }

    val firstDueDatePickerState = rememberDatePickerState()
    var showFirstDueDatePicker by remember { mutableStateOf(false) }

    // Payment Schedule State
    var schedule by remember { mutableStateOf<List<PaymentScheduleItem>>(emptyList()) }
    var originalEMI by remember { mutableDoubleStateOf(0.0) }
    var editingMonthIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(principalAmount, apr, durationMonths, startDate) {
        val p = parseAmount(principalAmount)
        val r = apr.toDoubleOrNull() ?: 0.0
        val d = durationMonths.toIntOrNull() ?: 0
        val date = try { dateFormatter.parse(startDate) } catch (_: Exception) { Date() }
        
        originalEMI = calculateEMI(p, r, d)
        schedule = generateSchedule(p, r, d, date ?: Date(), dateFormatter)
    }

    fun handleMonthEdit(index: Int, newDate: String, newPrincipal: Double, newInterest: Double) {
        val updatedSchedule = schedule.toMutableList()
        val annualRate = apr.toDoubleOrNull() ?: 0.0
        val monthlyRate = annualRate / 12 / 100
        
        // Previous balance
        val prevBalance = if (index == 0) parseAmount(principalAmount) else schedule[index - 1].remainingBalance
        
        // Update current month
        val newBalance = Math.max(0.0, prevBalance - newPrincipal)
        updatedSchedule[index] = updatedSchedule[index].copy(
            date = newDate,
            principal = newPrincipal,
            interest = newInterest,
            payment = newPrincipal + newInterest,
            remainingBalance = newBalance
        )
        
        // Recalculate subsequent months
        var currentBalance = newBalance
        val calendar = Calendar.getInstance()
        val parsedDate = try { dateFormatter.parse(newDate) } catch (_: Exception) { Date() }
        calendar.time = parsedDate ?: Date()
        
        val remainingMonths = updatedSchedule.size - 1 - index
        if (remainingMonths > 0) {
            val newEMI = calculateEMI(currentBalance, annualRate, remainingMonths)
            
            for (i in (index + 1) until updatedSchedule.size) {
                calendar.add(Calendar.MONTH, 1)
                val interest = currentBalance * monthlyRate
                var principalPaid = newEMI - interest
                
                if (i == updatedSchedule.size - 1) {
                    principalPaid = currentBalance
                }
                
                currentBalance = Math.max(0.0, currentBalance - principalPaid)
                
                updatedSchedule[i] = PaymentScheduleItem(
                    month = i + 1,
                    date = dateFormatter.format(calendar.time),
                    payment = interest + principalPaid,
                    interest = interest,
                    principal = principalPaid,
                    remainingBalance = currentBalance
                )
            }
        }
        schedule = updatedSchedule
    }

    // Payment Plan States
    var paymentAccountId by remember { mutableStateOf<String?>(existingAccount?.paymentAccountId) }
    var paymentCategory by remember { mutableStateOf(existingAccount?.paymentCategory ?: "") }
    var paymentPayee by remember { mutableStateOf(existingAccount?.paymentPayee ?: "") }
    var showAccountPicker by remember { mutableStateOf(false) }

    var additionalInfo by remember { mutableStateOf(existingAccount?.additionalInfo ?: "") }
    var includeInNetWorth by remember { mutableStateOf(true) }
    var includeInGroupBalance by remember { mutableStateOf(true) }
    var selectedGroupId by remember { mutableStateOf<String?>(existingAccount?.groupId) }
    var showGroupPicker by remember { mutableStateOf(false) }
    var selectedBudgetId by remember { mutableStateOf<String?>(existingAccount?.monitoredByBudgetId) }
    var showBudgetPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            val title = when (currentSubPage) {
                LoanSubPage.MAIN -> "Create a Loan Account"
                LoanSubPage.PAYMENT_PLAN -> "Payment Plan"
                LoanSubPage.PAYMENT_SCHEDULE -> "Payment Schedule"
            }
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentSubPage == LoanSubPage.MAIN) onBackClick()
                        else currentSubPage = when (currentSubPage) {
                            LoanSubPage.PAYMENT_SCHEDULE -> LoanSubPage.PAYMENT_PLAN
                            LoanSubPage.PAYMENT_PLAN -> LoanSubPage.MAIN
                            else -> LoanSubPage.MAIN
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (currentSubPage == LoanSubPage.MAIN) {
                        Button(
                            onClick = {
                                if (accountName.isNotBlank() && (durationMonths.toIntOrNull() ?: 0) >= 2) {
                                    if (existingAccount != null) {
                                        onUpdateClick(existingAccount.copy(
                                            name = accountName,
                                            principalAmount = principalAmount,
                                            apr = apr,
                                            duration = durationMonths,
                                            startDate = startDate,
                                            firstDueDate = firstDueDate,
                                            groupId = selectedGroupId,
                                            additionalInfo = additionalInfo,
                                            monitoredByBudgetId = selectedBudgetId,
                                            paymentAccountId = paymentAccountId,
                                            paymentCategory = paymentCategory,
                                            paymentPayee = paymentPayee
                                        ))
                                    } else {
                                        onSaveClick(
                                            accountName,
                                            principalAmount,
                                            apr,
                                            durationMonths,
                                            startDate,
                                            firstDueDate,
                                            selectedGroupId,
                                            additionalInfo,
                                            selectedBudgetId,
                                            paymentAccountId,
                                            paymentCategory,
                                            paymentPayee
                                        )
                                    }
                                }
                            },
                            enabled = accountName.isNotBlank() && (durationMonths.toIntOrNull() ?: 0) >= 2,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Save", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8F9FA))
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (currentSubPage) {
                LoanSubPage.MAIN -> {
                    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        SecondaryTabRow(
                            selectedTabIndex = selectedTabIndex,
                            containerColor = Color(0xFFF8F9FA),
                            contentColor = Color.Black,
                            divider = {}
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTabIndex == index,
                                    onClick = { selectedTabIndex = index },
                                    text = {
                                        Text(
                                            title,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                if (selectedTabIndex == 0) {
                                    LoanBasicFields(
                                        accountName = accountName,
                                        onNameChange = { accountName = it },
                                        principalAmount = principalAmount,
                                        onPrincipalChange = { principalAmount = it },
                                        apr = apr,
                                        onAprChange = { apr = it },
                                        duration = durationMonths,
                                        onDurationChange = { durationMonths = it },
                                        startDate = startDate,
                                        onStartDateClick = { showStartDatePicker = true },
                                        firstDueDate = firstDueDate,
                                        onFirstDueDateClick = { showFirstDueDatePicker = true },
                                        onPaymentPlanClick = { currentSubPage = LoanSubPage.PAYMENT_PLAN },
                                        paymentsMade = paymentsMade
                                    )
                                } else {
                                    LoanAdvancedFields(
                                        additionalInfo = additionalInfo,
                                        onInfoChange = { additionalInfo = it },
                                        includeInNetWorth = includeInNetWorth,
                                        onNetWorthChange = { includeInNetWorth = it },
                                        includeInGroupBalance = includeInGroupBalance,
                                        onGroupBalanceChange = { includeInGroupBalance = it },
                                        selectedGroupName = groups.find { it.id == selectedGroupId }?.name ?: "Select",
                                        onGroupClick = { showGroupPicker = true },
                                        selectedBudgetName = budgets.find { it.id == selectedBudgetId }?.name ?: "None",
                                        onBudgetClick = { showBudgetPicker = true }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
                LoanSubPage.PAYMENT_PLAN -> {
                    PaymentPlanPage(
                        selectedAccountName = accounts.find { it.id == paymentAccountId }?.name ?: "Select",
                        onAccountClick = { showAccountPicker = true },
                        category = paymentCategory,
                        onCategoryChange = { paymentCategory = it },
                        payee = paymentPayee,
                        onPayeeChange = { paymentPayee = it },
                        onScheduleClick = { currentSubPage = LoanSubPage.PAYMENT_SCHEDULE }
                    )
                }
                LoanSubPage.PAYMENT_SCHEDULE -> {
                    PaymentSchedulePage(
                        schedule = schedule,
                        totalInterest = schedule.sumOf { it.interest },
                        totalPrincipal = schedule.sumOf { it.principal },
                        paymentsMade = paymentsMade,
                        onItemClick = { index -> 
                            if (index >= paymentsMade) {
                                editingMonthIndex = index 
                            }
                        }
                    )
                }
            }
        }

        if (showGroupPicker) {
            ModalBottomSheet(onDismissRequest = { showGroupPicker = false }) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth().padding(bottom = 32.dp)) {
                    Text("Select Group", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    ListItem(
                        modifier = Modifier.clickable { 
                            selectedGroupId = null
                            showGroupPicker = false 
                        },
                        headlineContent = { Text("None") }
                    )
                    groups.forEach { group ->
                        ListItem(
                            modifier = Modifier.clickable { 
                                selectedGroupId = group.id
                                showGroupPicker = false 
                            },
                            headlineContent = { Text(group.name) },
                            leadingContent = { 
                                Box(modifier = Modifier.size(24.dp).background(group.color, CircleShape))
                            }
                        )
                    }
                }
            }
        }

        if (showBudgetPicker) {
            ModalBottomSheet(onDismissRequest = { showBudgetPicker = false }) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth().padding(bottom = 32.dp)) {
                    Text("Select Budget", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    ListItem(
                        modifier = Modifier.clickable { 
                            selectedBudgetId = null
                            showBudgetPicker = false 
                        },
                        headlineContent = { Text("None") }
                    )
                    budgets.forEach { budget ->
                        ListItem(
                            modifier = Modifier.clickable { 
                                selectedBudgetId = budget.id
                                showBudgetPicker = false 
                            },
                            headlineContent = { Text(budget.name) },
                            leadingContent = { 
                                Box(modifier = Modifier.size(24.dp).background(budget.color, CircleShape))
                            }
                        )
                    }
                }
            }
        }

        if (showStartDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showStartDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        startDatePickerState.selectedDateMillis?.let {
                            startDate = dateFormatter.format(Date(it))
                        }
                        showStartDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = startDatePickerState)
            }
        }

        if (showFirstDueDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showFirstDueDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        firstDueDatePickerState.selectedDateMillis?.let {
                            firstDueDate = dateFormatter.format(Date(it))
                        }
                        showFirstDueDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showFirstDueDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = firstDueDatePickerState)
            }
        }

        if (showAccountPicker) {
            ModalBottomSheet(onDismissRequest = { showAccountPicker = false }) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth().padding(bottom = 32.dp)) {
                    Text("Select Account", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    accounts.forEach { account ->
                        ListItem(
                            modifier = Modifier.clickable { 
                                paymentAccountId = account.id
                                showAccountPicker = false 
                            },
                            headlineContent = { Text(account.name) },
                            leadingContent = { 
                                Box(modifier = Modifier.size(24.dp).background(account.color, CircleShape))
                            },
                            trailingContent = { Text(account.balance) }
                        )
                    }
                }
            }
        }

        editingMonthIndex?.let { index ->
            EditMonthDialog(
                item = schedule[index],
                maxPayment = originalEMI,
                dateFormatter = dateFormatter,
                onDismiss = { editingMonthIndex = null },
                onConfirm = { date, p, i ->
                    handleMonthEdit(index, date, p, i)
                    editingMonthIndex = null
                }
            )
        }
    }
}

@Composable
fun EditMonthDialog(
    item: PaymentScheduleItem,
    maxPayment: Double,
    dateFormatter: SimpleDateFormat,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Double) -> Unit
) {
    var dateStr by remember { mutableStateOf(item.date) }
    var principalStr by remember { mutableStateOf(String.format("%.2f", item.principal)) }
    var interestStr by remember { mutableStateOf(String.format("%.2f", item.interest)) }
    
    val p = principalStr.toDoubleOrNull() ?: 0.0
    val i = interestStr.toDoubleOrNull() ?: 0.0
    val total = p + i
    val isTotalValid = total <= maxPayment + 0.01 // Small buffer for rounding
    
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = try { dateFormatter.parse(item.date)?.time } catch(_: Exception) { null }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Month ${item.month}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }) {
                    OutlinedTextField(
                        value = dateStr,
                        onValueChange = {},
                        label = { Text("Date") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { Icon(Icons.Default.CalendarToday, null) },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                OutlinedTextField(
                    value = principalStr,
                    onValueChange = { input ->
                        principalStr = input
                        input.toDoubleOrNull()?.let { newP ->
                            val newI = Math.max(0.0, maxPayment - newP)
                            interestStr = String.format(Locale.getDefault(), "%.2f", newI)
                            if (newP > maxPayment) {
                                principalStr = String.format(Locale.getDefault(), "%.2f", maxPayment)
                            }
                        }
                    },
                    label = { Text("Principal") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isTotalValid || p < 0
                )
                OutlinedTextField(
                    value = interestStr,
                    onValueChange = { input ->
                        interestStr = input
                        input.toDoubleOrNull()?.let { newI ->
                            val newP = Math.max(0.0, maxPayment - newI)
                            principalStr = String.format(Locale.getDefault(), "%.2f", newP)
                            if (newI > maxPayment) {
                                interestStr = String.format(Locale.getDefault(), "%.2f", maxPayment)
                            }
                        }
                    },
                    label = { Text("Interest") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isTotalValid || i < 0
                )
                
                Text(
                    "Total: ${String.format(Locale.getDefault(), "%.2f", total)} / Max: ${String.format(Locale.getDefault(), "%.2f", maxPayment)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isTotalValid) Color.Gray else Color.Red
                )
                
                if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                datePickerState.selectedDateMillis?.let {
                                    dateStr = dateFormatter.format(Date(it))
                                }
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
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(dateStr, p, i) },
                enabled = isTotalValid && p >= 0 && i >= 0
            ) { Text("Confirm") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun PaymentPlanPage(
    selectedAccountName: String,
    onAccountClick: () -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
    payee: String,
    onPayeeChange: (String) -> Unit,
    onScheduleClick: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                ClickableLoanItem(Icons.Default.AccountBalance, "Account used for payments", selectedAccountName, onClick = onAccountClick)
                LoanDivider()
                ListItem(
                    leadingContent = { Icon(Icons.Default.Category, null, tint = Color.Gray) },
                    headlineContent = {
                        TextField(
                            value = category,
                            onValueChange = onCategoryChange,
                            placeholder = { Text("Category") },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                )
                LoanDivider()
                ListItem(
                    leadingContent = { Icon(Icons.Default.Person, null, tint = Color.Gray) },
                    headlineContent = {
                        TextField(
                            value = payee,
                            onValueChange = onPayeeChange,
                            placeholder = { Text("Payee") },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                )
                LoanDivider()
                ListItem(
                    modifier = Modifier.clickable { onScheduleClick() },
                    leadingContent = { Icon(Icons.AutoMirrored.Filled.EventNote, null, tint = Color.Gray) },
                    headlineContent = { Text("Payment schedule", color = Color.Gray) },
                    trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.LightGray) }
                )
            }
        }
    }
}

@Composable
fun PaymentSchedulePage(
    schedule: List<PaymentScheduleItem>,
    totalInterest: Double,
    totalPrincipal: Double,
    paymentsMade: Int = 0,
    onItemClick: (Int) -> Unit
) {
    val totalAmount = totalInterest + totalPrincipal

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ScheduleSummaryCard(
                label = "Total Interest",
                value = String.format(Locale.getDefault(), "$%.2f", totalInterest),
                valueColor = Color(0xFFF44336),
                modifier = Modifier.weight(1f)
            )
            ScheduleSummaryCard(
                label = "Total Principal",
                value = String.format(Locale.getDefault(), "$%.2f", totalPrincipal),
                valueColor = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
            ScheduleSummaryCard(
                label = "Total",
                value = String.format(Locale.getDefault(), "$%.2f", totalAmount),
                valueColor = Color.Black,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            LazyColumn {
                itemsIndexed(schedule) { index, item ->
                    val isPaid = index < paymentsMade
                    Column(modifier = Modifier.clickable(enabled = !isPaid) { onItemClick(index) }) {
                        ScheduleItemRow(item, isPaid = isPaid)
                    }
                    if (index < schedule.size - 1) LoanDivider()
                }
            }
        }
    }
}

@Composable
fun LoanBasicFields(
    accountName: String,
    onNameChange: (String) -> Unit,
    principalAmount: String,
    onPrincipalChange: (String) -> Unit,
    apr: String,
    onAprChange: (String) -> Unit,
    duration: String,
    onDurationChange: (String) -> Unit,
    startDate: String,
    onStartDateClick: () -> Unit,
    firstDueDate: String,
    onFirstDueDateClick: () -> Unit,
    onPaymentPlanClick: () -> Unit,
    paymentsMade: Int = 0
) {
    Column {
        ListItem(
            leadingContent = { Icon(Icons.Default.Edit, null, tint = Color.Gray) },
            headlineContent = {
                TextField(
                    value = accountName,
                    onValueChange = onNameChange,
                    placeholder = { Text("Account name") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
        LoanDivider()

        ClickableLoanItem(Icons.AutoMirrored.Filled.ShowChart, "Icon", "Default")
        LoanDivider()

        ListItem(
            leadingContent = { Icon(Icons.Default.AddCircleOutline, null, tint = Color.Gray) },
            headlineContent = {
                TextField(
                    value = principalAmount,
                    onValueChange = onPrincipalChange,
                    label = { Text("Principal amount", color = Color.Gray) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
        LoanDivider()

        ListItem(
            leadingContent = { Icon(Icons.Default.AccountBalance, null, tint = Color.Gray) },
            headlineContent = {
                TextField(
                    value = apr,
                    onValueChange = onAprChange,
                    placeholder = { Text("APR %") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
        LoanDivider()

        ListItem(
            leadingContent = { Icon(Icons.Default.Schedule, null, tint = Color.Gray) },
            headlineContent = {
                val isDurationValid = duration.toIntOrNull()?.let { it >= 2 } ?: false
                TextField(
                    value = duration,
                    onValueChange = { 
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            onDurationChange(it)
                        }
                    },
                    label = { Text("Duration (months)", color = Color.Gray) },
                    isError = !isDurationValid && duration.isNotEmpty(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
        LoanDivider()

        ClickableLoanItem(Icons.Default.CalendarToday, "Start date", startDate, isDate = true, onClick = onStartDateClick)
        LoanDivider()

        ClickableLoanItem(Icons.Outlined.EventNote, "First due date", firstDueDate, isDate = true, onClick = onFirstDueDateClick)
        LoanDivider()

        val isLoanFullyPaid = duration.toIntOrNull()?.let { it > 0 && paymentsMade >= it } ?: false
        ListItem(
            modifier = Modifier.clickable { onPaymentPlanClick() },
            leadingContent = { Icon(Icons.Default.Description, null, tint = Color.Gray) },
            headlineContent = { 
                Text(
                    if (isLoanFullyPaid) "Payment plan (FULLY PAID)" else "Payment plan", 
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        textDecoration = if (isLoanFullyPaid) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                    )
                ) 
            },
            trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.LightGray) }
        )
    }
}

@Composable
fun LoanAdvancedFields(
    additionalInfo: String,
    onInfoChange: (String) -> Unit,
    includeInNetWorth: Boolean,
    onNetWorthChange: (Boolean) -> Unit,
    includeInGroupBalance: Boolean,
    onGroupBalanceChange: (Boolean) -> Unit,
    selectedGroupName: String,
    onGroupClick: () -> Unit,
    selectedBudgetName: String,
    onBudgetClick: () -> Unit
) {
    Column {
        ListItem(
            leadingContent = { Icon(Icons.AutoMirrored.Filled.Note, null, tint = Color.Gray, modifier = Modifier.padding(top = 8.dp)) },
            headlineContent = {
                TextField(
                    value = additionalInfo,
                    onValueChange = onInfoChange,
                    placeholder = { Text("Additional information") },
                    minLines = 4,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
        LoanDivider()

        ListItem(
            leadingContent = { Icon(Icons.Default.AccountBalance, null, tint = Color.Gray) },
            headlineContent = { Text("Include in Net Worth") },
            trailingContent = { Switch(checked = includeInNetWorth, onCheckedChange = onNetWorthChange) }
        )
        LoanDivider()

        ListItem(
            leadingContent = { Icon(Icons.Default.Group, null, tint = Color.Gray) },
            headlineContent = { Text("Include in Group balance") },
            trailingContent = { Switch(checked = includeInGroupBalance, onCheckedChange = onGroupBalanceChange) }
        )
        LoanDivider()

        ClickableLoanItem(
            icon = Icons.Default.Layers,
            label = "Put in Group",
            value = selectedGroupName,
            onClick = onGroupClick
        )
        LoanDivider()

        ClickableLoanItem(
            icon = Icons.Default.WorkOutline, 
            label = "Monitored by Budgets", 
            value = selectedBudgetName,
            onClick = onBudgetClick
        )
    }
}

@Composable
fun ClickableLoanItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    isDate: Boolean = false,
    onClick: () -> Unit = {}
) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        leadingContent = { Icon(icon, null, tint = Color.Gray) },
        headlineContent = { Text(label, color = if (isDate) Color.Black else Color.Gray) },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isDate) {
                    Surface(
                        color = Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(value, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(value, color = Color.Black)
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.LightGray)
                }
            }
        }
    )
}

@Composable
fun LoanDivider() {
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color(0xFFEEEEEE))
}

private fun parseAmount(amountStr: String): Double {
    return amountStr.filter { it.isDigit() || it == '.' || it == ',' }
        .replace(',', '.')
        .toDoubleOrNull() ?: 0.0
}

private fun calculateEMI(principal: Double, annualRate: Double, months: Int): Double {
    if (months <= 0) return 0.0
    val monthlyRate = annualRate / 12 / 100
    if (monthlyRate == 0.0) return principal / months
    return (principal * monthlyRate * Math.pow(1 + monthlyRate, months.toDouble())) /
            (Math.pow(1 + monthlyRate, months.toDouble()) - 1)
}

private fun generateSchedule(
    principal: Double,
    annualRate: Double,
    months: Int,
    startDate: Date,
    dateFormatter: SimpleDateFormat
): List<PaymentScheduleItem> {
    val emi = calculateEMI(principal, annualRate, months)
    val monthlyRate = annualRate / 12 / 100
    var balance = principal
    val schedule = mutableListOf<PaymentScheduleItem>()
    val calendar = Calendar.getInstance()
    calendar.time = startDate

    for (i in 1..months) {
        val interest = balance * monthlyRate
        var principalPaid = emi - interest
        
        if (i == months) {
            principalPaid = balance
        }
        
        balance -= principalPaid

        schedule.add(
            PaymentScheduleItem(
                month = i,
                date = dateFormatter.format(calendar.time),
                payment = interest + principalPaid,
                interest = interest,
                principal = principalPaid,
                remainingBalance = Math.max(0.0, balance)
            )
        )
        calendar.add(Calendar.MONTH, 1)
    }
    return schedule
}

@Composable
fun ScheduleSummaryCard(
    label: String,
    value: String,
    valueColor: Color = Color.Black,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

@Composable
fun ScheduleItemRow(item: PaymentScheduleItem, isPaid: Boolean = false) {
    val contentAlpha = if (isPaid) 0.5f else 1f
    val textDecoration = if (isPaid) androidx.compose.ui.text.style.TextDecoration.LineThrough else null

    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "Month ${item.month}", 
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge.copy(textDecoration = textDecoration),
                color = Color.Black.copy(alpha = contentAlpha)
            )
            Text(
                item.date, 
                style = MaterialTheme.typography.bodySmall.copy(textDecoration = textDecoration),
                color = Color.Gray.copy(alpha = contentAlpha)
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                String.format(Locale.getDefault(), "$%.2f", item.payment),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge.copy(textDecoration = textDecoration),
                color = Color.Black.copy(alpha = contentAlpha)
            )
            Row {
                Text(
                    "P: ${String.format(Locale.getDefault(), "$%.2f", item.principal)}",
                    style = MaterialTheme.typography.labelSmall.copy(textDecoration = textDecoration),
                    color = (if (isPaid) Color.Gray else Color(0xFF4CAF50)).copy(alpha = contentAlpha)
                )
                Text(" | ", style = MaterialTheme.typography.labelSmall, color = Color.Gray.copy(alpha = contentAlpha))
                Text(
                    "I: ${String.format(Locale.getDefault(), "$%.2f", item.interest)}",
                    style = MaterialTheme.typography.labelSmall.copy(textDecoration = textDecoration),
                    color = (if (isPaid) Color.Gray else Color(0xFFF44336)).copy(alpha = contentAlpha)
                )
            }
            Text(
                "Bal: ${String.format(Locale.getDefault(), "$%.2f", item.remainingBalance)}",
                style = MaterialTheme.typography.labelSmall.copy(textDecoration = textDecoration),
                color = Color.DarkGray.copy(alpha = contentAlpha),
                fontWeight = FontWeight.Medium
            )
        }
    }
}
