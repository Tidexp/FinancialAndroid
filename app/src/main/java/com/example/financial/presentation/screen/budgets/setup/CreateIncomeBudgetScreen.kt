package com.example.financial.presentation.screen.budgets.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financial.domain.model.Account
import com.example.financial.domain.model.Budget
import com.example.financial.domain.model.BudgetGroup
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateIncomeBudgetScreen(
    initialBudget: Budget? = null,
    onCloseClick: () -> Unit,
    onSaveClick: (
        name: String,
        amount: Double,
        isIncome: Boolean,
        color: Color,
        groupId: String?,
        startDate: Long,
        repeatEnabled: Boolean,
        frequencyValue: Int,
        frequencyUnit: String,
        rolloverEnabled: Boolean,
        accountIds: List<String>,
        categories: List<String>
    ) -> Unit,
    budgetGroups: List<BudgetGroup> = emptyList(),
    accounts: List<Account> = emptyList(),
    showHeader: Boolean = true,
    onRegisterSaveAction: (() -> Unit) -> Unit = {}
) {
    // --- States ---
    var budgetName by remember { mutableStateOf(initialBudget?.name ?: "") }
    var amountText by remember { mutableStateOf(if (initialBudget != null) String.format(Locale.getDefault(), "%.2f", initialBudget.amount) else "") }
    var selectedColor by remember { mutableStateOf(initialBudget?.color ?: Color(0xFF4CAF50)) }
    var selectedGroupId by remember { mutableStateOf<String?>(initialBudget?.budgetGroupId) }
    var showGroupPicker by remember { mutableStateOf(false) }
    
    var startDate by remember { mutableLongStateOf(initialBudget?.startDate ?: System.currentTimeMillis()) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    val startDatePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate)

    var endDate by remember { 
        mutableLongStateOf(initialBudget?.startDate?.let { it + 30L * 24 * 60 * 60 * 1000 } ?: (System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)) 
    }
    var showEndDatePicker by remember { mutableStateOf(false) }
    val endDatePickerState = rememberDatePickerState(initialSelectedDateMillis = endDate)

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    var repeatEnabled by remember { mutableStateOf(initialBudget?.repeatEnabled ?: false) }
    var frequencyUnit by remember { mutableStateOf(initialBudget?.frequencyUnit ?: "month") }
    var showFrequencyPicker by remember { mutableStateOf(false) }

    var rolloverEnabled by remember { mutableStateOf(initialBudget?.rolloverEnabled ?: false) }

    val selectedAccountIds = remember { 
        val list = mutableStateListOf<String>()
        initialBudget?.accountIds?.let { list.addAll(it) }
        list
    }
    var showAccountPicker by remember { mutableStateOf(false) }

    val categoryList = remember { 
        val list = mutableStateListOf<String>()
        initialBudget?.categories?.let { list.addAll(it) }
        list
    }
    var categoryInput by remember { mutableStateOf("") }

    val saveAction = {
        if (budgetName.isNotBlank()) {
            onSaveClick(
                budgetName,
                amountText.replace(",", ".").toDoubleOrNull() ?: 0.0,
                true,
                selectedColor,
                selectedGroupId,
                startDate,
                repeatEnabled,
                1,
                frequencyUnit,
                rolloverEnabled,
                selectedAccountIds.toList(),
                categoryList.toList()
            )
        }
    }

    LaunchedEffect(budgetName, amountText, selectedColor, selectedGroupId, startDate, repeatEnabled, frequencyUnit, rolloverEnabled, selectedAccountIds, categoryList) {
        onRegisterSaveAction(saveAction)
    }

    val content = @Composable { padding: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            BudgetSectionHeader("Describe your budget")
            BudgetCard {
                BudgetInputRow(Icons.Default.Badge, "Name", budgetName) { budgetName = it }
                BudgetDivider()
                
                ListItem(
                    leadingContent = { Icon(Icons.Default.AddCircleOutline, null, tint = Color.Gray) },
                    headlineContent = {
                        TextField(
                            value = amountText,
                            onValueChange = { amountText = it },
                            placeholder = { Text("0,00") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("USD", color = Color.Gray)
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.LightGray)
                        }
                    }
                )
                
                BudgetDivider()
                
                ListItem(
                    modifier = Modifier.clickable { showGroupPicker = true },
                    leadingContent = { Icon(Icons.Default.Folder, null, tint = Color.Gray) },
                    headlineContent = { Text("Put in Group") },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = budgetGroups.find { it.id == selectedGroupId }?.name ?: "None",
                                color = Color.Gray
                            )
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.LightGray)
                        }
                    }
                )
                
                BudgetDivider()
                BudgetClickableRow(Icons.Default.Edit, "Icon", "")
            }

            BudgetSectionHeader("Which transactions should be included?")
            BudgetCard {
                ListItem(
                    modifier = Modifier.clickable { showAccountPicker = true },
                    leadingContent = { Icon(Icons.Default.CreditCard, null, tint = Color.Gray) },
                    headlineContent = {
                        val text = if (selectedAccountIds.isEmpty()) "All Accounts" else {
                            val first = accounts.find { it.id == selectedAccountIds.first() }?.name ?: ""
                            if (selectedAccountIds.size > 1) "$first and ${selectedAccountIds.size - 1} more..." else first
                        }
                        Text(text, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    },
                    trailingContent = {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.LightGray)
                    }
                )
                BudgetDivider()
                
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Folder, null, tint = Color.Gray)
                        Spacer(modifier = Modifier.width(16.dp))
                        TextField(
                            value = categoryInput,
                            onValueChange = { categoryInput = it },
                            placeholder = { Text("Select Categories") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (categoryInput.isNotBlank() && !categoryList.contains(categoryInput)) {
                                    categoryList.add(categoryInput.trim())
                                    categoryInput = ""
                                }
                            }),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                        IconButton(onClick = {
                            if (categoryInput.isNotBlank() && !categoryList.contains(categoryInput)) {
                                categoryList.add(categoryInput.trim())
                                categoryInput = ""
                            }
                        }) {
                            Icon(Icons.Default.Add, null, tint = Color(0xFF3478F6))
                        }
                    }
                    
                    if (categoryList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categoryList.forEach { category ->
                                InputChip(
                                    selected = true,
                                    onClick = { categoryList.remove(category) },
                                    label = { Text(category) },
                                    trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp)) }
                                )
                            }
                        }
                    }
                }
                
                BudgetDivider()
                BudgetClickableRow(Icons.AutoMirrored.Outlined.Label, "Tags", "")
            }

            BudgetSectionHeader("Configure how the budget repeats")
            BudgetCard {
                ListItem(
                    modifier = Modifier.clickable { showStartDatePicker = true },
                    leadingContent = { Icon(Icons.Default.CalendarMonth, null, tint = Color.Gray) },
                    headlineContent = {
                        Surface(
                            color = Color(0xFFF2F2F7),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                dateFormatter.format(Date(startDate)),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    },
                    trailingContent = { Text("Start date", color = Color.Gray, fontSize = 14.sp) }
                )
                BudgetDivider()
                ListItem(
                    headlineContent = { Text("Repeat", color = Color.LightGray) },
                    trailingContent = {
                        Switch(
                            checked = repeatEnabled,
                            onCheckedChange = { repeatEnabled = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF4CAF50))
                        )
                    }
                )
                if (repeatEnabled) {
                    BudgetDivider()
                    ListItem(
                        modifier = Modifier.clickable { showFrequencyPicker = true },
                        leadingContent = { Icon(Icons.Default.Schedule, null, tint = Color.Gray) },
                        headlineContent = { Text("Frequency") },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Every 1 ${frequencyUnit.lowercase()}",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.LightGray)
                            }
                        }
                    )
                }
                BudgetDivider()
                ListItem(
                    modifier = Modifier.clickable { showEndDatePicker = true },
                    leadingContent = { Icon(Icons.Default.CalendarMonth, null, tint = Color.Gray) },
                    headlineContent = {
                        Surface(
                            color = Color(0xFFF2F2F7),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                dateFormatter.format(Date(endDate)),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    },
                    trailingContent = { Text("End date", color = Color.Gray, fontSize = 14.sp) }
                )
            }

            BudgetSectionHeader("Rollover settings")
            BudgetCard {
                ListItem(
                    leadingContent = {
                        Icon(
                            Icons.Default.Sync,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    },
                    headlineContent = {
                        Text(
                            "Rollover surplus balance",
                            fontWeight = FontWeight.Normal
                        )
                    },
                    supportingContent = {
                        Text(
                            "Automatically carry over excess income from the previous budget period.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = rolloverEnabled,
                            onCheckedChange = { rolloverEnabled = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF4CAF50))
                        )
                    }
                )
            }

            if (!showHeader) {
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        if (budgetName.isNotBlank()) {
                            onSaveClick(
                                budgetName,
                                amountText.replace(",", ".").toDoubleOrNull() ?: 0.0,
                                true,
                                selectedColor,
                                selectedGroupId,
                                startDate,
                                repeatEnabled,
                                1,
                                frequencyUnit,
                                rolloverEnabled,
                                selectedAccountIds.toList(),
                                categoryList.toList()
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3478F6))
                ) {
                    Text(if (initialBudget != null) "Update Budget" else "Save Budget", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showHeader) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            if (initialBudget != null) "Edit Income Budget" else "New Income Budget",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onCloseClick) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    actions = {
                        Button(
                            onClick = saveAction,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3478F6)),
                            contentPadding = PaddingValues(horizontal = 24.dp)
                        ) {
                            Text(if (initialBudget != null) "Update" else "Save", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            containerColor = Color(0xFFF8F9FA)
        ) { padding ->
            content(padding)
        }
    } else {
        content(PaddingValues(0.dp))
    }

    if (showGroupPicker) {
        ModalBottomSheet(onDismissRequest = { showGroupPicker = false }) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth().padding(bottom = 32.dp)) {
                Text("Select Budget Group", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                ListItem(
                    modifier = Modifier.clickable { 
                        selectedGroupId = null
                        showGroupPicker = false 
                    },
                    headlineContent = { Text("None") }
                )
                budgetGroups.forEach { group ->
                    ListItem(
                        modifier = Modifier.clickable { 
                            selectedGroupId = group.id
                            showGroupPicker = false 
                        },
                        headlineContent = { Text(group.name) }
                    )
                }
            }
        }
    }

    if (showAccountPicker) {
        ModalBottomSheet(onDismissRequest = { showAccountPicker = false }) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth().padding(bottom = 32.dp)) {
                Text("Select Accounts", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(accounts) { account ->
                        val isSelected = selectedAccountIds.contains(account.id)
                        ListItem(
                            modifier = Modifier.clickable {
                                if (isSelected) selectedAccountIds.remove(account.id)
                                else selectedAccountIds.add(account.id)
                            },
                            leadingContent = {
                                Checkbox(checked = isSelected, onCheckedChange = null)
                            },
                            headlineContent = { Text(account.name) },
                            trailingContent = {
                                Box(modifier = Modifier.size(24.dp).background(account.color, CircleShape))
                            }
                        )
                    }
                }
                Button(
                    onClick = { showAccountPicker = false },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Done")
                }
            }
        }
    }

    if (showFrequencyPicker) {
        val units = listOf("Day", "Week", "Month", "Year")
        ModalBottomSheet(onDismissRequest = { showFrequencyPicker = false }) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth().padding(bottom = 32.dp)) {
                Text("Select Frequency", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                units.forEach { unit ->
                    ListItem(
                        modifier = Modifier.clickable {
                            frequencyUnit = unit.lowercase()
                            showFrequencyPicker = false
                        },
                        headlineContent = { Text("Every 1 $unit") },
                        trailingContent = {
                            if (frequencyUnit == unit.lowercase()) {
                                Icon(Icons.Default.Check, null, tint = Color(0xFF3478F6))
                            }
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
                        startDate = it
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

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endDatePickerState.selectedDateMillis?.let {
                        endDate = it
                    }
                    showEndDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = endDatePickerState)
        }
    }
}
