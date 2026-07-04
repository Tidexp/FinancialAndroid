package com.example.financial.presentation.screen.budgets.setup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
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
fun CreateIncomeBudgetScreen(
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
    var selectedColor by remember { mutableStateOf(Color(0xFF4CAF50)) }
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
                        "New Income Budget",
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
                                    true,
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
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
                                unfocusedContainerColor = Color.Transparent
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

            BudgetSectionHeader("Configuration")
            BudgetCard {
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
                                Text(frequencyValue, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(frequencyUnit, fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }
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
