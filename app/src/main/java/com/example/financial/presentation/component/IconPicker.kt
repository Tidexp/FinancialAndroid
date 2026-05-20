package com.example.financial.presentation.component

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconPickerBottomSheet(
    onDismiss: () -> Unit,
    onIconSelected: (ImageVector) -> Unit,
    onImageSelected: (Uri) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            onImageSelected(it)
            onDismiss()
        }
    }

    val predefinedIcons = listOf(
        Icons.Default.AccountBalance,
        Icons.Default.AccountBalanceWallet,
        Icons.Default.CreditCard,
        Icons.Default.Savings,
        Icons.Default.MonetizationOn,
        Icons.Default.Payments,
        Icons.Default.Store,
        Icons.Default.ShoppingBag,
        Icons.Default.DirectionsCar,
        Icons.Default.Home,
        Icons.Default.Restaurant,
        Icons.Default.LocalDrink,
        Icons.Default.ElectricBolt,
        Icons.Default.WaterDrop,
        Icons.Default.Wifi,
        Icons.Default.PhoneAndroid,
        Icons.Default.School,
        Icons.Default.HealthAndSafety,
        Icons.Default.FitnessCenter,
        Icons.Default.SportsEsports,
        Icons.Default.Flight,
        Icons.Default.Pets,
        Icons.Default.Work,
        Icons.Default.Build,
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Select Icon",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )

            // Option to pick from phone
            ListItem(
                modifier = Modifier.clickable { galleryLauncher.launch("image/*") },
                leadingContent = { Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                headlineContent = { Text("Pick from phone Gallery", fontWeight = FontWeight.Bold) },
                supportingContent = { Text("Choose a custom image from your device") }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(64.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(predefinedIcons) { icon ->
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF5F5F5))
                            .clickable {
                                onIconSelected(icon)
                                onDismiss()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}
