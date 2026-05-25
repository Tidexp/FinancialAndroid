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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background

// Màu xanh lá đậm đặc trưng cho Loan Account trong ảnh mẫu
val LoanPrimaryColor = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateLoanAccountScreen(
    onBackClick: () -> Unit,
    onSaveClick: (
        name: String,
        principal: String,
        apr: String,
        duration: String,
        startDate: String,
        firstDueDate: String,
        groupId: String?,
        additionalInfo: String
    ) -> Unit,
    groups: List<AccountGroup> = emptyList()
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Basic", "Advanced")

    // --- State Hoisting cho Loan ---
    var accountName by remember { mutableStateOf("") }
    var principalAmount by remember { mutableStateOf("0,00 USD") }
    var apr by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("Select") }
    var startDate by remember { mutableStateOf("10 May 2026") }
    var firstDueDate by remember { mutableStateOf("10 May 2026") }

    var additionalInfo by remember { mutableStateOf("") }
    var includeInNetWorth by remember { mutableStateOf(true) }
    var includeInGroupBalance by remember { mutableStateOf(true) }
    var selectedGroupId by remember { mutableStateOf<String?>(null) }
    var showGroupPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Create a Loan Account",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (accountName.isNotBlank()) {
                                onSaveClick(
                                    accountName,
                                    principalAmount,
                                    apr,
                                    duration,
                                    startDate,
                                    firstDueDate,
                                    selectedGroupId,
                                    additionalInfo
                                )
                            }
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black), // Nút Save màu đen theo mẫu
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8F9FA))
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
                            duration = duration,
                            startDate = startDate,
                            firstDueDate = firstDueDate
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
                            onGroupClick = { showGroupPicker = true }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
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
    startDate: String,
    firstDueDate: String
) {
    Column {
        // Tên tài khoản
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

        // Icon
        ClickableLoanItem(Icons.AutoMirrored.Filled.ShowChart, "Icon", "Default")
        LoanDivider()

        // Số tiền gốc (Principal)
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

        // Lãi suất APR
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

        // Thời hạn (Duration)
        ClickableLoanItem(Icons.Default.Schedule, "Duration", duration)
        LoanDivider()

        // Ngày bắt đầu
        ClickableLoanItem(Icons.Default.CalendarToday, "Start date", startDate, isDate = true)
        LoanDivider()

        // Ngày đến hạn đầu tiên
        ClickableLoanItem(Icons.Outlined.EventNote, "First due date", firstDueDate, isDate = true)
        LoanDivider()

        // Kế hoạch trả nợ
        ListItem(
            modifier = Modifier.clickable { },
            leadingContent = { Icon(Icons.Default.Description, null, tint = Color.Gray) },
            headlineContent = { Text("Payment plan", color = Color.Gray) },
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
    onGroupClick: () -> Unit
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

        ClickableLoanItem(Icons.Default.WorkOutline, "Monitored by Budgets", "None")
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
