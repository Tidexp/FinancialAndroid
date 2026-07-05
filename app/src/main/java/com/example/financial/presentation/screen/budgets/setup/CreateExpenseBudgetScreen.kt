package com.example.financial.presentation.screen.budgets.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financial.domain.model.BudgetGroup

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
        rolloverEnabled: Boolean
    ) -> Unit,
    budgetGroups: List<BudgetGroup> = emptyList()
) {
    // --- States ---
    var budgetName by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("0.00") }
    var selectedColor by remember { mutableStateOf(Color(0xFFF44336)) }
    var selectedGroupId by remember { mutableStateOf<String?>(null) }
    var showGroupPicker by remember { mutableStateOf(false) }
    var startDate by remember { mutableLongStateOf(System.currentTimeMillis()) }

    var repeatEnabled by remember { mutableStateOf(true) }
    var frequencyValue by remember { mutableStateOf("1") }
    var frequencyUnit by remember { mutableStateOf("month") }

    var rolloverEnabled by remember { mutableStateOf(false) }

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
                                    frequencyValue.toIntOrNull() ?: 1,
                                    frequencyUnit,
                                    rolloverEnabled
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
                
                BudgetDivider()
                BudgetClickableRow(Icons.Default.Edit, "Icon", "")
            }

            BudgetSectionHeader("Which transactions should be included?")
            BudgetCard {
                BudgetClickableRow(Icons.Default.AccountBalanceWallet, "All Accounts", "")
                BudgetDivider()
                BudgetClickableRow(Icons.Outlined.Folder, "All Categories", "")
                BudgetDivider()
                BudgetClickableRow(Icons.Outlined.Label, "All Tags", "")
            }

            BudgetSectionHeader("Configure how the budget repeats")
            BudgetCard {
                ListItem(
                    leadingContent = { Icon(Icons.Default.CalendarToday, null, tint = Color.Gray) },
                    headlineContent = {
                        Surface(
                            color = Color(0xFFE0E0E0),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Today",
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
                        headlineContent = { Text("Frequency") },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    frequencyValue,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Text(
                                    frequencyUnit,
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
    }
}

@Composable
fun BudgetSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun BudgetCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        content = content
    )
}

@Composable
fun BudgetInputRow(icon: androidx.compose.ui.graphics.vector.ImageVector, placeholder: String, value: String, onValueChange: (String) -> Unit) {
    ListItem(
        leadingContent = { Icon(icon, null, tint = Color.Gray) },
        headlineContent = {
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(placeholder) },
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
}

@Composable
fun BudgetClickableRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, secondaryValue: String) {
    ListItem(
        modifier = Modifier.clickable { },
        leadingContent = { Icon(icon, null, tint = Color.Gray) },
        headlineContent = { Text(label, color = if (secondaryValue.isEmpty()) Color.Gray else Color.Black) },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (secondaryValue.isNotEmpty()) {
                    Text(secondaryValue, color = Color.Gray, modifier = Modifier.padding(end = 8.dp))
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.LightGray)
            }
        }
    )
}

@Composable
fun BudgetDivider() {
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color(0xFFEEEEEE))
}
