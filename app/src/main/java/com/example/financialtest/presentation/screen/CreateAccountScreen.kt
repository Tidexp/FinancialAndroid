package com.example.financialtest.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.financialtest.domain.model.AccountType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountScreen(
    accountType: AccountType,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Basic", "Advanced")

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
                        onClick = onSaveClick,
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
                BasicTabContent()
            } else {
                AdvancedTabContent()
            }
        }
    }
}

@Composable
fun BasicTabContent() {
    var accountName by remember { mutableStateOf("") }
    var autoClear by remember { mutableStateOf(false) }

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
                            onValueChange = { accountName = it },
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
                
                ClickableListItem(
                    leadingIcon = Icons.AutoMirrored.Filled.ShowChart,
                    label = "Icon",
                    value = "Default"
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)

                ClickableListItem(
                    leadingIcon = Icons.Default.AddCircleOutline,
                    label = "Opening balance",
                    value = "0,00 USD"
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)

                ListItem(
                    leadingContent = { Icon(Icons.Default.Settings, contentDescription = null, tint = Color.Gray) },
                    headlineContent = { Text("Auto-clear transactions") },
                    trailingContent = {
                        Switch(checked = autoClear, onCheckedChange = { autoClear = it })
                    }
                )
            }
        }
    }
}

@Composable
fun AdvancedTabContent() {
    var includeInNetWorth by remember { mutableStateOf(true) }
    var includeInGroupBalance by remember { mutableStateOf(true) }
    var additionalInfo by remember { mutableStateOf("") }

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
                            onValueChange = { additionalInfo = it },
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
                        Switch(checked = includeInNetWorth, onCheckedChange = { includeInNetWorth = it })
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)

                ListItem(
                    leadingContent = { Icon(Icons.Default.Group, contentDescription = null, tint = Color.Gray) },
                    headlineContent = { Text("Include in Group balance") },
                    trailingContent = {
                        Switch(checked = includeInGroupBalance, onCheckedChange = { includeInGroupBalance = it })
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)

                ClickableListItem(
                    leadingIcon = Icons.Default.Layers,
                    label = "Put in Group",
                    value = "Select"
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
fun ClickableListItem(leadingIcon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    ListItem(
        modifier = Modifier.clickable { },
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
