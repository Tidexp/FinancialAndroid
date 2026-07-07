package com.example.financial.presentation.screen.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.financial.domain.model.CategorySpending
import com.example.financial.domain.model.ReportsData
import com.example.financial.presentation.viewmodel.FinancialViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: FinancialViewModel) {
    val uiState by viewModel.reportsUiState.collectAsState()
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }
    var selectingStartDate by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Báo cáo tài chính", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { /* Refresh logic */ }) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Date Range Selector
            DateRangeHeader(
                startDate = uiState.startDate,
                endDate = uiState.endDate,
                onDateClick = { isStart ->
                    selectingStartDate = isStart
                    showDatePicker = true
                }
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    uiState.data?.let { data ->
                        item {
                            SummarySection(data)
                        }

                        if (data.categorySpending.isNotEmpty()) {
                            item {
                                SectionHeader("Chi tiêu theo hạng mục")
                            }
                            items(data.categorySpending) { category ->
                                CategorySpendingItem(category)
                            }
                        }

                        if (data.incomeByCategory.isNotEmpty()) {
                            item {
                                SectionHeader("Thu nhập theo hạng mục")
                            }
                            items(data.incomeByCategory) { category ->
                                CategorySpendingItem(category)
                            }
                        }

                        if (data.dailyTrend.isNotEmpty()) {
                            item {
                                SectionHeader("Xu hướng chi tiêu hàng ngày")
                                Spacer(modifier = Modifier.height(16.dp))
                                DailyTrendChart(data.dailyTrend.map { it.expense.toFloat() })
                            }
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    } ?: item {
                        Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Không có dữ liệu trong khoảng thời gian này", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selectedDate = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    if (selectingStartDate) {
                        viewModel.setReportsDateRange(selectedDate, uiState.endDate)
                    } else {
                        viewModel.setReportsDateRange(uiState.startDate, selectedDate)
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Hủy")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun DateRangeHeader(startDate: Long, endDate: Long, onDateClick: (Boolean) -> Unit) {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.clickable { onDateClick(true) },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Từ ngày", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(sdf.format(Date(startDate)), fontWeight = FontWeight.Bold)
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.Gray)
        Column(
            modifier = Modifier.clickable { onDateClick(false) },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Đến ngày", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(sdf.format(Date(endDate)), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SummarySection(data: ReportsData) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard(
                title = "Tổng thu",
                amount = String.format(Locale.getDefault(), "$%.2f", data.totalIncome),
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "Tổng chi",
                amount = String.format(Locale.getDefault(), "$%.2f", data.totalExpense),
                color = Color(0xFFF44336),
                modifier = Modifier.weight(1f)
            )
        }
        SummaryCard(
            title = "Thặng dư",
            amount = String.format(Locale.getDefault(), "$%.2f", data.netCashFlow),
            color = if (data.netCashFlow >= 0) Color(0xFF2196F3) else Color(0xFFFF9800),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SummaryCard(title: String, amount: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = color)
            Text(amount, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun CategorySpendingItem(category: CategorySpending) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(category.color)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(category.label, fontWeight = FontWeight.Medium)
            }
            Text(category.amount, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { category.progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = category.color,
            trackColor = category.color.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun DailyTrendChart(values: List<Float>) {
    if (values.isEmpty()) return
    val maxValue = values.maxOrNull() ?: 1f
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        values.forEach { value ->
            val heightFraction = if (maxValue > 0) value / maxValue else 0f
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(heightFraction.coerceIn(0.05f, 1f))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
            )
        }
    }
}
