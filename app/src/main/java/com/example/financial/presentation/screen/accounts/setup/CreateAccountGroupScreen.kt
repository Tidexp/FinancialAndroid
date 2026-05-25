package com.example.financial.presentation.screen.accounts.setup

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.financial.presentation.component.IconPickerBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountGroupScreen(
    onBackClick: () -> Unit,
    onSaveClick: (name: String, iconName: String?, iconUri: String?, color: Color) -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf<ImageVector?>(Icons.Default.Folder) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedColor by remember { mutableStateOf(Color(0xFF2196F3)) }
    var showIconPicker by remember { mutableStateOf(false) }

    val colors = listOf(
        Color(0xFFF44336), Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF673AB7),
        Color(0xFF3F51B5), Color(0xFF2196F3), Color(0xFF03A9F4), Color(0xFF00BCD4),
        Color(0xFF009688), Color(0xFF4CAF50), Color(0xFF8BC34A), Color(0xFFCDDC39),
        Color(0xFFFFEB3B), Color(0xFFFFC107), Color(0xFFFF9800), Color(0xFFFF5722),
        Color(0xFF795548), Color(0xFF9E9E9E), Color(0xFF607D8B), Color(0xFF000000)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("New Account Group", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        if (groupName.isNotBlank()) {
                            onSaveClick(groupName, "folder", selectedImageUri?.toString(), selectedColor)
                        }
                    }) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Group Header Preview
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = selectedColor.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(selectedColor),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = selectedIcon ?: Icons.Default.Folder,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = if (groupName.isBlank()) "Group Name" else groupName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = selectedColor
                    )
                }
            }

            // Input Fields
            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("Group Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Icon Selector
            ListItem(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showIconPicker = true }
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                leadingContent = { 
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(selectedIcon ?: Icons.Default.Folder, contentDescription = null)
                    }
                },
                headlineContent = { Text("Group Icon") },
                trailingContent = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (selectedImageUri != null) "Custom" else "Default", style = MaterialTheme.typography.bodySmall)
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    }
                }
            )

            // Color Selector
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = selectedColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Theme Color", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(48.dp),
                    modifier = Modifier.height(160.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(colors) { color ->
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { selectedColor = color }
                                .border(
                                    width = if (selectedColor == color) 3.dp else 0.dp,
                                    color = if (selectedColor == color) Color.DarkGray else Color.Transparent,
                                    shape = CircleShape
                                )
                        )
                    }
                }
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
    }
}
