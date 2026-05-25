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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financial.presentation.component.IconPickerBottomSheet
import android.net.Uri
import androidx.compose.ui.graphics.vector.ImageVector
import coil.compose.AsyncImage
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import com.example.financial.domain.model.AccountGroup

// Định nghĩa màu hồng/đỏ chủ đạo cho Credit Account
val CreditPrimaryColor = Color(0xFFE91E63)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCreditAccountScreen(
    onBackClick: () -> Unit,
    onSaveClick: (
        name: String,
        balance: String,
        creditLimit: String,
        iconUri: String?,
        statementCloseDay: String,
        autoClear: Boolean,
        additionalInfo: String,
        groupId: String?
    ) -> Unit,
    groups: List<AccountGroup> = emptyList()
) {
    // --- STATE HOISTING ---
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Basic", "Advanced")

    // Basic States
    var accountName by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf<ImageVector?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showIconPicker by remember { mutableStateOf(false) }
    var creditLimit by remember { mutableStateOf("0,00 USD") }
    var openingBalance by remember { mutableStateOf("-0,00") }
    var balanceBoxDisplayMode by remember { mutableStateOf("Balance") }
    var autoClear by remember { mutableStateOf(true) }

    // Advanced States
    var additionalInfo by remember { mutableStateOf("") }
    var includeInNetWorth by remember { mutableStateOf(true) }
    var includeInGroupBalance by remember { mutableStateOf(true) }
    var statementCloseDay by remember { mutableStateOf("31") }
    var selectedGroupId by remember { mutableStateOf<String?>(null) }
    var showGroupPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Create a Credit Account",
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
                                    openingBalance,
                                    creditLimit,
                                    selectedImageUri?.toString(),
                                    statementCloseDay,
                                    autoClear,
                                    additionalInfo,
                                    selectedGroupId
                                )
                            }
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = CreditPrimaryColor),
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
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
                contentColor = CreditPrimaryColor,
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
                                color = if (selectedTabIndex == index) Color.Black else Color.Gray,
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
                        CreditBasicFields(
                            accountName = accountName,
                            onNameChange = { accountName = it },
                            selectedIcon = selectedIcon,
                            selectedImageUri = selectedImageUri,
                            onIconClick = { showIconPicker = true },
                            creditLimit = creditLimit,
                            onLimitChange = { creditLimit = it },
                            openingBalance = openingBalance,
                            onBalanceChange = { openingBalance = it },
                            balanceBoxDisplayMode = balanceBoxDisplayMode,
                            onDisplayModeChange = { balanceBoxDisplayMode = it },
                            autoClear = autoClear,
                            onAutoClearChange = { autoClear = it }
                        )
                    } else {
                        CreditAdvancedFields(
                            additionalInfo = additionalInfo,
                            onInfoChange = { additionalInfo = it },
                            includeInNetWorth = includeInNetWorth,
                            onNetWorthChange = { includeInNetWorth = it },
                            includeInGroupBalance = includeInGroupBalance,
                            onGroupBalanceChange = { includeInGroupBalance = it },
                            statementCloseDay = statementCloseDay,
                            onDayChange = { statementCloseDay = it },
                            selectedGroupName = groups.find { it.id == selectedGroupId }?.name ?: "Select",
                            onGroupClick = { showGroupPicker = true }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showIconPicker) {
            IconPickerBottomSheet(
                onDismiss = { showIconPicker = false },
                onIconSelected = {
                    selectedIcon = it
                    selectedImageUri = null
                },
                onImageSelected = {
                    selectedImageUri = it
                    selectedIcon = null
                }
            )
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
fun CreditBasicFields(
    accountName: String,
    onNameChange: (String) -> Unit,
    selectedIcon: ImageVector?,
    selectedImageUri: Uri?,
    onIconClick: () -> Unit,
    creditLimit: String,
    onLimitChange: (String) -> Unit,
    openingBalance: String,
    onBalanceChange: (String) -> Unit,
    balanceBoxDisplayMode: String,
    onDisplayModeChange: (String) -> Unit,
    autoClear: Boolean,
    onAutoClearChange: (Boolean) -> Unit
) {
    Column {
        ListItem(
            leadingContent = { Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Gray) },
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
        CreditDivider()

        ListItem(
            modifier = Modifier.clickable { onIconClick() },
            leadingContent = {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = selectedIcon ?: Icons.AutoMirrored.Filled.ShowChart,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }
            },
            headlineContent = { Text("Icon", color = Color.Gray) },
            trailingContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (selectedImageUri != null) "Custom" else if (selectedIcon != null) "Selected" else "Default", color = Color.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.LightGray)
                }
            }
        )
        CreditDivider()

        ListItem(
            leadingContent = { Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color.Gray) },
            headlineContent = {
                TextField(
                    value = creditLimit,
                    onValueChange = onLimitChange,
                    label = { Text("Credit limit", color = Color.Gray) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
        CreditDivider()

        ListItem(
            leadingContent = { Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = Color.Gray) },
            headlineContent = {
                TextField(
                    value = openingBalance,
                    onValueChange = onBalanceChange,
                    label = { Text("Opening balance", color = Color.Gray) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
        CreditDivider()

        ListItem(
            leadingContent = { Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.Gray) },
            headlineContent = {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text("In the Balance", color = Color.Gray, fontSize = 14.sp)
                    Text("box show", color = Color.Gray, fontSize = 14.sp)
                }
            },
            trailingContent = {
                Column(
                    modifier = Modifier
                        .width(150.dp)
                        .background(Color(0xFFF1F3F4), RoundedCornerShape(8.dp))
                        .padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Balance",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDisplayModeChange("Balance") }
                            .padding(vertical = 8.dp),
                        textAlign = TextAlign.Center,
                        color = if (balanceBoxDisplayMode == "Balance") Color(0xFF03A9F4) else Color.Gray,
                        fontWeight = if (balanceBoxDisplayMode == "Balance") FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        "Available Credit",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDisplayModeChange("Available Credit") }
                            .padding(vertical = 8.dp),
                        textAlign = TextAlign.Center,
                        color = if (balanceBoxDisplayMode == "Available Credit") Color.Black else Color.Gray,
                        fontWeight = if (balanceBoxDisplayMode == "Available Credit") FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        )
        CreditDivider()

        ListItem(
            leadingContent = { Icon(Icons.Default.Settings, contentDescription = null, tint = Color.Gray) },
            headlineContent = { Text("Auto-clear transactions") },
            trailingContent = {
                Switch(
                    checked = autoClear,
                    onCheckedChange = onAutoClearChange,
                    colors = SwitchDefaults.colors(checkedTrackColor = CreditPrimaryColor)
                )
            }
        )
    }
}

@Composable
fun CreditAdvancedFields(
    additionalInfo: String,
    onInfoChange: (String) -> Unit,
    includeInNetWorth: Boolean,
    onNetWorthChange: (Boolean) -> Unit,
    includeInGroupBalance: Boolean,
    onGroupBalanceChange: (Boolean) -> Unit,
    statementCloseDay: String,
    onDayChange: (String) -> Unit,
    selectedGroupName: String,
    onGroupClick: () -> Unit
) {
    Column {
        ListItem(
            leadingContent = {
                Icon(Icons.AutoMirrored.Filled.Note, contentDescription = null, tint = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp))
            },
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
        CreditDivider()

        ListItem(
            leadingContent = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Color.Gray) },
            headlineContent = { Text("Include in Net Worth") },
            trailingContent = {
                Switch(
                    checked = includeInNetWorth,
                    onCheckedChange = onNetWorthChange,
                    colors = SwitchDefaults.colors(checkedTrackColor = CreditPrimaryColor)
                )
            }
        )
        CreditDivider()

        ListItem(
            leadingContent = { Icon(Icons.Default.Group, contentDescription = null, tint = Color.Gray) },
            headlineContent = { Text("Include in Group balance") },
            trailingContent = {
                Switch(
                    checked = includeInGroupBalance,
                    onCheckedChange = onGroupBalanceChange,
                    colors = SwitchDefaults.colors(checkedTrackColor = CreditPrimaryColor)
                )
            }
        )
        CreditDivider()

        ClickableCreditItem(
            leadingIcon = Icons.Default.Layers,
            label = "Put in Group",
            value = selectedGroupName,
            onClick = onGroupClick
        )
        CreditDivider()

        ClickableCreditItem(
            leadingIcon = Icons.Default.PieChart,
            label = "Monitored by Budgets",
            value = "Shopee"
        )
        CreditDivider()

        ListItem(
            leadingContent = { Icon(Icons.Default.SyncAlt, contentDescription = null, tint = Color.Gray) },
            headlineContent = {
                TextField(
                    value = statementCloseDay,
                    onValueChange = onDayChange,
                    label = { Text("Statement close day", color = Color.Gray) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
        CreditDivider()

        ClickableCreditItem(
            leadingIcon = Icons.Default.ViewCarousel,
            label = "When it falls on a weekend",
            value = "No change"
        )
    }
}

@Composable
fun ClickableCreditItem(
    leadingIcon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit = {}
) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        leadingContent = { Icon(leadingIcon, contentDescription = null, tint = Color.Gray) },
        headlineContent = { Text(label, color = Color.Gray) },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(value, color = Color.Black, fontWeight = FontWeight.Normal)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.LightGray)
            }
        }
    )
}

@Composable
fun CreditDivider() {
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color(0xFFEEEEEE))
}
