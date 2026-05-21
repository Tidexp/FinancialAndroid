package com.example.financial.presentation.screen.budgets.setup

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateExpenseBudgetsScreen(
    onCloseClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    // --- States ---
    var budgetName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("0,00 USD") }
    var repeatEnabled by remember { mutableStateOf(true) }
    var frequencyValue by remember { mutableStateOf("1") }
    var frequencyUnit by remember { mutableStateOf("month") }

    // State cho tính năng Rollover mới thêm
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
                        onClick = onSaveClick,
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
            // SECTION 1: Describe your budget
            BudgetSectionHeader("Describe your budget")
            BudgetCard {
                BudgetInputRow(Icons.Default.Badge, "Name", budgetName) { budgetName = it }
                BudgetDivider()
                BudgetClickableRow(Icons.Default.MoveToInbox, "0,00", "USD")
                BudgetDivider()
                BudgetClickableRow(Icons.Default.Layers, "Put in Group", "")
                BudgetDivider()
                BudgetClickableRow(Icons.Default.Edit, "Icon", "")
            }

            // SECTION 2: Which transactions should be included?
            BudgetSectionHeader("Which transactions should be included?")
            BudgetCard {
                BudgetClickableRow(Icons.Default.AccountBalanceWallet, "None", "")
                BudgetDivider()
                BudgetClickableRow(Icons.Outlined.Folder, "Select Categories", "")
                BudgetDivider()
                BudgetClickableRow(Icons.Outlined.Label, "Tags", "")
            }

            // SECTION 3: Configure how the budget repeats
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
                                "1 May 2026",
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

            // --- SECTION NEW: Rollover Configuration ---
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
    }
}

// =============================================
// --- COMPOSABLE HỖ TRỢ (HELPERS) ---
// =============================================

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