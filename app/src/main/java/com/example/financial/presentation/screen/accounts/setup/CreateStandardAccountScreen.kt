// This is screen for creating CHECKING, SAVINGS and CASH_WALLET account type
package com.example.financial.presentation.screen.accounts.setup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.financial.domain.model.AccountType
import com.example.financial.presentation.component.IconPickerBottomSheet
import android.net.Uri
import androidx.compose.ui.graphics.vector.ImageVector
import coil.compose.AsyncImage
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import com.example.financial.domain.model.AccountGroup
import androidx.compose.foundation.background

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateStandardAccountScreen(
    accountType: AccountType,
    onBackClick: () -> Unit,
    onSaveClick: (
        name: String,
        balance: String,
        type: AccountType,
        groupId: String?,
        autoClear: Boolean,
        additionalInfo: String
    ) -> Unit,
    groups: List<AccountGroup> = emptyList()
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Basic", "Advanced")

    // Basic States
    var accountName by remember { mutableStateOf("") }
    var openingBalance by remember { mutableStateOf("0,00 USD") }
    var selectedIcon by remember { mutableStateOf<ImageVector?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showIconPicker by remember { mutableStateOf(false) }
    var autoClear by remember { mutableStateOf(false) }

    // Advanced States
    var includeInNetWorth by remember { mutableStateOf(true) }
    var includeInGroupBalance by remember { mutableStateOf(true) }
    var additionalInfo by remember { mutableStateOf("") }
    var selectedGroupId by remember { mutableStateOf<String?>(null) }
    var showGroupPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Create a ${accountType.displayName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
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
                                onSaveClick(accountName, openingBalance, accountType, selectedGroupId, autoClear, additionalInfo)
                            }
                        },
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = Color(0xFFF8F9FA) // Light background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SecondaryTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color(0xFFF8F9FA),
                contentColor = MaterialTheme.colorScheme.primary,
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

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTabIndex == 0) {
                BasicTabContent(
                    accountName = accountName,
                    onNameChange = { accountName = it },
                    openingBalance = openingBalance,
                    onBalanceChange = { openingBalance = it },
                    selectedIcon = selectedIcon,
                    selectedImageUri = selectedImageUri,
                    onIconClick = { showIconPicker = true },
                    autoClear = autoClear,
                    onAutoClearChange = { autoClear = it }
                )
            } else {
                AdvancedTabContent(
                    includeInNetWorth = includeInNetWorth,
                    onNetWorthChange = { includeInNetWorth = it },
                    includeInGroupBalance = includeInGroupBalance,
                    onGroupBalanceChange = { includeInGroupBalance = it },
                    additionalInfo = additionalInfo,
                    onInfoChange = { additionalInfo = it },
                    selectedGroupName = groups.find { it.id == selectedGroupId }?.name ?: "Select",
                    onGroupClick = { showGroupPicker = true }
                )
            }
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
fun BasicTabContent(
    accountName: String,
    onNameChange: (String) -> Unit,
    openingBalance: String,
    onBalanceChange: (String) -> Unit,
    selectedIcon: ImageVector?,
    selectedImageUri: Uri?,
    onIconClick: () -> Unit,
    autoClear: Boolean,
    onAutoClearChange: (Boolean) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                
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
                    headlineContent = { Text("Icon") },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(if (selectedImageUri != null) "Custom" else if (selectedIcon != null) "Selected" else "Default", color = Color.Gray)
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
                        }
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)

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
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)

                ListItem(
                    leadingContent = { Icon(Icons.Default.Settings, contentDescription = null, tint = Color.Gray) },
                    headlineContent = { Text("Auto-clear transactions") },
                    trailingContent = {
                        Switch(checked = autoClear, onCheckedChange = onAutoClearChange)
                    }
                )
            }
        }
    }
}

@Composable
fun AdvancedTabContent(
    includeInNetWorth: Boolean,
    onNetWorthChange: (Boolean) -> Unit,
    includeInGroupBalance: Boolean,
    onGroupBalanceChange: (Boolean) -> Unit,
    additionalInfo: String,
    onInfoChange: (String) -> Unit,
    selectedGroupName: String,
    onGroupClick: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column {
                ListItem(
                    leadingContent = { Icon(Icons.AutoMirrored.Filled.Note, contentDescription = null, tint = Color.Gray) },
                    headlineContent = {
                        TextField(
                            value = additionalInfo,
                            onValueChange = onInfoChange,
                            placeholder = { Text("Additional information") },
                            minLines = 3,
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
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)

                ListItem(
                    leadingContent = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Color.Gray) },
                    headlineContent = { Text("Include in Net Worth") },
                    trailingContent = {
                        Switch(checked = includeInNetWorth, onCheckedChange = onNetWorthChange)
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)

                ListItem(
                    leadingContent = { Icon(Icons.Default.Group, contentDescription = null, tint = Color.Gray) },
                    headlineContent = { Text("Include in Group balance") },
                    trailingContent = {
                        Switch(checked = includeInGroupBalance, onCheckedChange = onGroupBalanceChange)
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)

                ClickableListItem(
                    leadingIcon = Icons.Default.Layers,
                    label = "Put in Group",
                    value = selectedGroupName,
                    onClick = onGroupClick
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)

                ClickableListItem(
                    leadingIcon = Icons.Default.PieChart,
                    label = "Monitored by Budgets",
                    value = "Shopee"
                )
            }
        }
    }
}

@Composable
fun ClickableListItem(
    leadingIcon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit = {}
) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        leadingContent = { Icon(leadingIcon, contentDescription = null, tint = Color.Gray) },
        headlineContent = { Text(label) },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(value, color = Color.Gray)
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
            }
        }
    )
}
