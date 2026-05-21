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
fun CreateIncomeBudgetScreen(
    onCloseClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    // --- States ---
    var budgetName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("0,00 USD") }
    var repeatEnabled by remember { mutableStateOf(false) } // Trong ảnh đang tắt
    var startDate by remember { mutableStateOf("1 May 2026") }
    var endDate by remember { mutableStateOf("17 May 2026") }

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
                        onClick = onSaveClick,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)), // Nút Save màu xanh Blue
                        contentPadding = PaddingValues(horizontal = 24.dp)
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F9FA) // Nền xám nhạt toàn màn hình
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // SECTION 1: Describe your budget
            IncomeSectionHeader("Describe your budget")
            IncomeCard {
                IncomeInputRow(Icons.Default.Badge, "Name", budgetName) { budgetName = it }
                IncomeDivider()
                IncomeClickableRow(Icons.Default.MoveToInbox, "0,00", "USD")
                IncomeDivider()
                IncomeClickableRow(Icons.Default.Layers, "Put in Group", "")
                IncomeDivider()
                IncomeClickableRow(Icons.Default.Edit, "Icon", "")
            }

            // SECTION 2: Which transactions should be included?
            IncomeSectionHeader("Which transactions should be included?")
            IncomeCard {
                IncomeClickableRow(Icons.Default.AccountBalanceWallet, "None", "")
                IncomeDivider()
                IncomeClickableRow(Icons.Outlined.Folder, "Select Categories", "")
                IncomeDivider()
                IncomeClickableRow(Icons.Outlined.Label, "Tags", "")
            }

            // SECTION 3: Configure how the budget repeats
            IncomeSectionHeader("Configure how the budget repeats")
            IncomeCard {
                // Hàng Start Date
                ListItem(
                    leadingContent = { Icon(Icons.Default.CalendarToday, null, tint = Color.Gray) },
                    headlineContent = {
                        DatePill(startDate)
                    },
                    trailingContent = { Text("Start date", color = Color.Gray, fontSize = 14.sp) }
                )
                IncomeDivider()

                // Hàng Repeat Toggle
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

                // Nếu không lặp lại (giống ảnh mẫu), hiện End Date
                if (!repeatEnabled) {
                    IncomeDivider()
                    ListItem(
                        leadingContent = { Icon(Icons.Default.CalendarMonth, null, tint = Color.Gray) },
                        headlineContent = {
                            DatePill(endDate)
                        },
                        trailingContent = { Text("End date", color = Color.Gray, fontSize = 14.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// Helper: Ô hiển thị ngày tháng kiểu bo tròn (Pill)
@Composable
fun DatePill(date: String) {
    Surface(
        color = Color(0xFFE0E0E0), // Màu xám pill
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = date,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun IncomeSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun IncomeCard(content: @Composable ColumnScope.() -> Unit) {
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
fun IncomeInputRow(icon: androidx.compose.ui.graphics.vector.ImageVector, placeholder: String, value: String, onValueChange: (String) -> Unit) {
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
fun IncomeClickableRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, secondaryValue: String) {
    ListItem(
        modifier = Modifier.clickable { },
        leadingContent = { Icon(icon, null, tint = Color.Gray) },
        headlineContent = {
            Text(
                text = label,
                color = if (label == "Select Categories" || label == "Tags") Color.Gray else Color.Black,
                maxLines = 1
            )
        },
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
fun IncomeDivider() {
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color(0xFFEEEEEE))
}