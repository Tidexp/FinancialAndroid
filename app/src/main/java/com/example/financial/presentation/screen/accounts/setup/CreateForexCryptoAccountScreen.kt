package com.example.financial.presentation.screen.accounts.setup

import androidx.compose.foundation.background
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateForexCryptoAccountScreen(
    onBackClick: () -> Unit,
    onSaveClick: (
        name: String,
        currency: String,
        groupId: String?,
        additionalInfo: String
    ) -> Unit,
    groups: List<AccountGroup> = emptyList()
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Basic", "Advanced")

    // --- State Hoisting ---
    var accountName by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("USD (US Dollar)") }

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
                        "Create a Forex / Crypto Account",
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
                    // Nút Save cho Forex/Crypto
                    Button(
                        onClick = {
                            if (accountName.isNotBlank()) {
                                onSaveClick(accountName, currency, selectedGroupId, additionalInfo)
                            }
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE3F2FD)),
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
                        ForexBasicFields(
                            accountName = accountName,
                            onNameChange = { accountName = it },
                            currency = currency
                        )
                    } else {
                        ForexAdvancedFields(
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
fun ForexBasicFields(
    accountName: String,
    onNameChange: (String) -> Unit,
    currency: String
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
        ForexDivider()

        ClickableForexItem(Icons.AutoMirrored.Filled.ShowChart, "Icon", "Default")
        ForexDivider()

        // Currency
        ClickableForexItem(Icons.Default.Payments, "Currency", currency)
    }
}

@Composable
fun ForexAdvancedFields(
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
        ForexDivider()

        ListItem(
            leadingContent = { Icon(Icons.Default.AccountBalance, null, tint = Color.Gray) },
            headlineContent = { Text("Include in Net Worth") },
            trailingContent = { Switch(checked = includeInNetWorth, onCheckedChange = onNetWorthChange) }
        )
        ForexDivider()

        ListItem(
            leadingContent = { Icon(Icons.Default.Group, null, tint = Color.Gray) },
            headlineContent = { Text("Include in Group balance") },
            trailingContent = { Switch(checked = includeInGroupBalance, onCheckedChange = onGroupBalanceChange) }
        )
        ForexDivider()

        ClickableForexItem(
            icon = Icons.Default.Layers,
            label = "Put in Group",
            value = selectedGroupName,
            onClick = onGroupClick
        )
        ForexDivider()

        ClickableForexItem(Icons.Default.WorkOutline, "Monitored by Budgets", "None")
    }
}

@Composable
fun ClickableForexItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit = {}
) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        leadingContent = { Icon(icon, null, tint = Color.Gray) },
        headlineContent = { Text(label, color = Color.Gray) },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(value, color = Color.Black)
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.LightGray)
            }
        }
    )
}

@Composable
fun ForexDivider() {
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color(0xFFEEEEEE))
}
