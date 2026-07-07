package com.example.financial.presentation.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import com.example.financial.presentation.viewmodel.FinancialViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: FinancialViewModel,
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.homeUiState.collectAsState()
    val isGuest = uiState.authStatus.contains("Guest", ignoreCase = true) || uiState.authStatus == "Not Logged In"
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Profile Header
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = if (isGuest) "Chế độ Khách" else "Tài khoản Cloud",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = uiState.authStatus,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Settings Groups
            Text(
                text = "Cấu hình chung",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp, bottom = 8.dp, top = 16.dp)
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column {
                    SettingsItem(Icons.Default.AccountCircle, "Quản lý tài khoản Cloud") {
                        scope.launch { snackbarHostState.showSnackbar("Tính năng Quản lý tài khoản đang được phát triển") }
                    }
                    SettingsItem(Icons.Default.CloudSync, "Đồng bộ hóa dữ liệu") {
                        scope.launch { snackbarHostState.showSnackbar("Đang bắt đầu đồng bộ dữ liệu...") }
                    }
                    
                    ListItem(
                        headlineContent = { Text("Trạng thái Cloud") },
                        trailingContent = { 
                            Text(
                                uiState.syncStatus, 
                                color = if(uiState.syncStatus == "Synced") Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                            ) 
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
            
            Text(
                text = "Tùy chỉnh & Bảo mật",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp, bottom = 8.dp, top = 24.dp)
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column {
                    SettingsItem(Icons.Default.Palette, "Giao diện (Dark/Light)") {
                        scope.launch { snackbarHostState.showSnackbar("Tính năng đổi Theme đang được phát triển") }
                    }
                    SettingsItem(Icons.Default.Lock, "Mã PIN & Sinh trắc học") {
                        scope.launch { snackbarHostState.showSnackbar("Tính năng bảo mật PIN đang được phát triển") }
                    }
                    SettingsItem(Icons.Default.Language, "Ngôn ngữ & Tiền tệ") {
                        scope.launch { snackbarHostState.showSnackbar("Tính năng ngôn ngữ đang được phát triển") }
                    }
                }
            }

            Text(
                text = "Hệ thống",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp, bottom = 8.dp, top = 24.dp)
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column {
                    SettingsItem(Icons.Default.Storage, "Dọn dẹp bộ nhớ đệm") {
                        scope.launch { snackbarHostState.showSnackbar("Đã dọn dẹp bộ nhớ đệm") }
                    }
                    SettingsItem(Icons.Default.Info, "Phiên bản 1.0.0") { }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (!isGuest) {
                Button(
                    onClick = { viewModel.signOut(onNavigateToLogin) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Đăng xuất (Offline data kept)")
                }
            } else {
                Button(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Đăng nhập ngay")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector, 
    label: String, 
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(label) },
        leadingContent = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
        modifier = Modifier.clickable { onClick() },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}
