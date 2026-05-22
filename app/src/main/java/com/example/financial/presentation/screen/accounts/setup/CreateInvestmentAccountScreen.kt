package com.example.financial.presentation.screen.accounts.setup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvestmentAccountScreen(
    onBackClick: () -> Unit,
    onSaveClick: (
        name: String,
        cashBalance: String,
        asOfDate: String,
        groupId: String?,
        additionalInfo: String
    ) -> Unit,
    groups: List<AccountGroup> = emptyList()
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Basic", "Advanced")

    // --- State Hoisting ---
    var accountName by remember { mutableStateOf("") }
    var cashBalance by remember { mutableStateOf("0,00 USD") }
    var asOfDate by remember { mutableStateOf("10 May 2026") }

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
                        "Create an Investment Account",
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
                                onSaveClick(accountName, cashBalance, asOfDate, selectedGroupId, additionalInfo)
                            }
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE3F2FD)), // Màu xanh nhạt
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold, color = Color(0xFF2196F3))
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
                        InvestmentBasicFields(
                            accountName = accountName,
                            onNameChange = { accountName = it },
                            cashBalance = cashBalance,
                            onBalanceChange = { cashBalance = it },
                            asOfDate = asOfDate
                        )
                    } else {
                        InvestmentAdvancedFields(
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
fun InvestmentBasicFields(
    accountName: String,
    onNameChange: (String) -> Unit,
    cashBalance: String,
    onBalanceChange: (String) -> Unit,
    asOfDate: String
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
        InvestmentDivider()

        ClickableInvestmentItem(Icons.AutoMirrored.Filled.ShowChart, "Icon", "Default")
        InvestmentDivider()

        // Cash Balance
        ListItem(
            leadingContent = { Icon(Icons.Default.AddCircleOutline, null, tint = Color.Gray) },
            headlineContent = {
                TextField(
                    value = cashBalance,
                    onValueChange = onBalanceChange,
                    label = { Text("Cash Balance", color = Color.Gray) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
        InvestmentDivider()

        // As of date
        ClickableInvestmentItem(Icons.Default.CalendarToday, "As of date", asOfDate, isDate = true)
    }
}

@Composable
fun InvestmentAdvancedFields(
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
        InvestmentDivider()

        ListItem(
            leadingContent = { Icon(Icons.Default.AccountBalance, null, tint = Color.Gray) },
            headlineContent = { Text("Include in Net Worth") },
            trailingContent = { Switch(checked = includeInNetWorth, onCheckedChange = onNetWorthChange) }
        )
        InvestmentDivider()

        ListItem(
            leadingContent = { Icon(Icons.Default.Group, null, tint = Color.Gray) },
            headlineContent = { Text("Include in Group balance") },
            trailingContent = { Switch(checked = includeInGroupBalance, onCheckedChange = onGroupBalanceChange) }
        )
        InvestmentDivider()

        ClickableInvestmentItem(
            icon = Icons.Default.Layers,
            label = "Put in Group",
            value = selectedGroupName,
            onClick = onGroupClick
        )
        InvestmentDivider()

        ClickableInvestmentItem(Icons.Default.WorkOutline, "Monitored by Budgets", "None")
    }
}

@Composable
fun ClickableInvestmentItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    isDate: Boolean = false,
    onClick: () -> Unit = {}
) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        leadingContent = { Icon(icon, null, tint = Color.Gray) },
        headlineContent = { Text(label, color = Color.Gray) },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isDate) {
                    Surface(
                        color = Color(0xFFF1F3F4),
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
fun InvestmentDivider() {
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color(0xFFEEEEEE))
}
