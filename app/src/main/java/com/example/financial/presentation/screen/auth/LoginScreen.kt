package com.example.financial.presentation.screen.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financial.presentation.viewmodel.FinancialViewModel

@Composable
fun LoginScreen(
    viewModel: FinancialViewModel,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }
    
    val uiState by viewModel.homeUiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
        Icon(
            imageVector = Icons.Default.AccountBalance,
            contentDescription = "Logo",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Financial Local-First",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = if (isRegisterMode) "Tạo tài khoản mới" else "Chào mừng quay trở lại",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Tên đăng nhập (Username)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mật khẩu") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { 
                if (isRegisterMode) {
                    viewModel.signUp(email, password, onLoginSuccess)
                } else {
                    viewModel.signIn(email, password, onLoginSuccess)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = email.isNotEmpty() && password.isNotEmpty() && !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(if (isRegisterMode) "Đăng ký" else "Đăng nhập", fontSize = 18.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(
            onClick = { isRegisterMode = !isRegisterMode }
        ) {
            Text(if (isRegisterMode) "Đã có tài khoản? Đăng nhập" else "Chưa có tài khoản? Đăng ký ngay")
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        
        TextButton(
            onClick = { viewModel.signInAnonymously(onLoginSuccess) },
            enabled = !uiState.isLoading
        ) {
            Text("Sử dụng chế độ khách (Guest Mode)", color = MaterialTheme.colorScheme.secondary)
        }
        }
    }
}
