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
import com.example.financial.domain.model.BudgetGroup
import com.example.financial.presentation.component.ExpenseItem
import com.example.financial.presentation.component.TransactionDivider
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateExpenseBudgetScreen(
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
    accounts: List<Account> = emptyList()
) {
    // --- States ---
    var budgetName by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("0.00") }
    var selectedColor by remember { mutableStateOf(Color(0xFFF44336)) }
    var selectedGroupId by remember { mutableStateOf<String?>(null) }
    var showGroupPicker by remember { mutableStateOf(false) }
    var startDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate)

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    var repeatEnabled by remember { mutableStateOf(true) }
    var frequencyUnit by remember { mutableStateOf("month") }
    var showFrequencyPicker by remember { mutableStateOf(false) }

    var rolloverEnabled by remember { mutableStateOf(false) }

    val selectedAccountIds = remember { mutableStateListOf<String>() }
    var showAccountPicker by remember { mutableStateOf(false) }

    val categoryList = remember { mutableStateListOf<String>() }
    var categoryInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "New Expense Budget",
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
                        onClick = {
                            if (budgetName.isNotBlank()) {
                                onSaveClick(
                                    budgetName,
                                    amountText.toDoubleOrNull() ?: 0.0,
                                    false,
                                    selectedColor,
                                    selectedGroupId,
                                    startDate,
                                    repeatEnabled,
                                    1, // Fixed at 1 as per request
                                    frequencyUnit,
                                    rolloverEnabled,
                                    selectedAccountIds.toList(),
                                    categoryList.toList()
                                )
                            }
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                        contentPadding = PaddingValues(horizontal = 24.dp)
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
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
                    leadingContent = { Icon(Icons.Default.MoveToInbox, null, tint = Color.Gray) },
                    headlineContent = {
                        TextField(
                            value = amountText,
                            onValueChange = { amountText = it },
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
                
                BudgetDivider()
                
                ListItem(
                    modifier = Modifier.clickable { showGroupPicker = true },
                    leadingContent = { Icon(Icons.Default.Layers, null, tint = Color.Gray) },
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
            }

            BudgetSectionHeader("Which transactions should be included?")
            BudgetCard {
                ListItem(
                    modifier = Modifier.clickable { showAccountPicker = true },
                    leadingContent = { Icon(Icons.Default.AccountBalanceWallet, null, tint = Color.Gray) },
                    headlineContent = { Text("Accounts") },
                    trailingContent = {
                        val text = if (selectedAccountIds.isEmpty()) "All" else "${selectedAccountIds.size} selected"
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text, color = Color.Gray)
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.LightGray)
                        }
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
                            placeholder = { Text("Add Category") },
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
                            Icon(Icons.Default.Add, null, tint = Color(0xFF2196F3))
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
            }

            BudgetSectionHeader("Configure how the budget repeats")
            BudgetCard {
                ListItem(
                    modifier = Modifier.clickable { showDatePicker = true },
                    leadingContent = { Icon(Icons.Default.CalendarToday, null, tint = Color.Gray) },
                    headlineContent = {
                        Surface(
                            color = Color(0xFFE0E0E0),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                dateFormatter.format(Date(startDate)),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    },
                    trailingContent = { Text("Start date", color = Color.Gray, fontSize = 14.sp) }
                )
                BudgetDivider()
                ListItem(
                    headlineContent = { Text("Repeat") },
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
                            "Rollover unused balance",
                            fontWeight = FontWeight.Normal
                        )
                    },
                    supportingContent = {
                        Text(
                            "Automatically carry over leftover money to the next budget period.",
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

            Spacer(modifier = Modifier.height(32.dp))
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
                                    Icon(Icons.Default.Check, null, tint = Color(0xFF2196F3))
                                }
                            }
                        )
                    }
                }
            }
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            startDate = it
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
}
