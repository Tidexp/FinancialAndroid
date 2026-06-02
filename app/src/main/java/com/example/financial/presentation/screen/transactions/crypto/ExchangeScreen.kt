package com.example.financial.presentation.screen.transactions.crypto

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchangeScreen() {
    var isCleared by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7)) // Nền xám nhạt iOS
            .padding(16.dp)
    ) {
        // --- TOP BAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nút Close
            IconButton(
                onClick = { },
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(50))
                    .size(40.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = null, tint = Color.Black)
            }

            // Thanh gạt chọn loại giao dịch (Icon Sync đầu tiên đang được chọn)
            Row(
                modifier = Modifier
                    .background(Color(0xFFE5E5EA), RoundedCornerShape(24.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TransactionTypeIcon(Icons.Outlined.Cached, isSelected = true) // Chọn Exchange
                TransactionTypeIcon(Icons.Outlined.RemoveCircleOutline, isSelected = false)
                TransactionTypeIcon(Icons.Outlined.AddCircleOutline, isSelected = false)
                TransactionTypeIcon(Icons.Default.SwapHoriz, isSelected = false)
                TransactionTypeIcon(Icons.Outlined.DragHandle, isSelected = false)
            }

            // Nút Save
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3478F6)),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tiêu đề phân hệ: Exchange
        Text(
            text = "Exchange",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- KHUNG NHẬP LIỆU EXCHANGE ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                // 1. Dòng Tiền gốc (From)
                ExchangeRow(Icons.Outlined.AddCircle, "From", trailingActionText = "Select", contentColor = Color.LightGray, actionColor = Color(0xFF3478F6))
                DividerLine()

                // 2. Dòng Tỷ giá (Exchange rate)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.SwapVert, null, tint = Color.Gray)
                    Spacer(Modifier.width(12.dp))
                    Text("1,00000000", color = Color.Black, modifier = Modifier.weight(1f))
                    Text("Exchange rate", color = Color.LightGray, fontSize = 14.sp, modifier = Modifier.padding(end = 12.dp))
                    Icon(
                        Icons.Outlined.Cached,
                        null,
                        tint = Color(0xFF3478F6),
                        modifier = Modifier.size(20.dp).clickable { }
                    )
                }
                DividerLine()

                // 3. Dòng Tiền đích (To)
                ExchangeRow(Icons.Outlined.AddCircle, "0,00", middleText = "To", trailingActionText = "Select", actionColor = Color(0xFF3478F6))
                DividerLine()

                // 4. Phí dịch vụ (Commission)
                ExchangeRow(Icons.Outlined.AddCircle, "0,00", middleText = "Commission", trailingActionText = "Select", contentColor = Color.LightGray, actionColor = Color.LightGray)
                DividerLine()

                // 5. Tổng tiền (Total)
                ExchangeRow(Icons.Outlined.AddCircle, "0,00", middleText = "Total", trailingActionText = "Select", contentColor = Color.LightGray, actionColor = Color(0xFF3478F6))
                DividerLine()

                // 6. Date & Time
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CalendarMonth, null, tint = Color.Gray)
                    Spacer(Modifier.width(12.dp))
                    DateBadge("2 Jun 2026")
                    Spacer(Modifier.width(8.dp))
                    DateBadge("14:43")
                    Spacer(Modifier.weight(1f))
                    Text("Date & time", color = Color.LightGray, fontSize = 14.sp)
                }
                DividerLine()

                // 7. Cleared / Pending
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.BookmarkBorder, null, tint = Color.Gray)
                    Spacer(Modifier.width(12.dp))
                    StatusToggle("Cleared", isCleared) { isCleared = true }
                    Spacer(Modifier.width(8.dp))
                    StatusToggle("Pending", !isCleared) { isCleared = false }
                }
                DividerLine()

                // 8. Description
                ExchangeRow(Icons.Outlined.FontDownload, "Description", hasArrow = true, contentColor = Color.LightGray)
                DividerLine()

                // 9. Memo
                ExchangeRow(Icons.Outlined.Description, "Memo", contentColor = Color.LightGray)
                DividerLine()

                // 10. Attachment Image
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Icon(Icons.Outlined.Image, null, tint = Color.Gray)
                    Icon(Icons.Outlined.AddCircleOutline, null, tint = Color(0xFF3478F6))
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- NÚT DƯỚI CÙNG ---
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            BottomActionButton("Options", Modifier.weight(1f))
            BottomActionButton("Hide", Modifier.weight(1f), hasIcon = true)
        }
    }
}

// --- HỢP PHẦN GIAO DIỆN CON THAY ĐỔI THEO THIẾT KẾ MỚI ---

@Composable
fun TransactionTypeIcon(icon: ImageVector, isSelected: Boolean) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(if (isSelected) Color.White else Color.Transparent, RoundedCornerShape(50)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = Color.Black, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun ExchangeRow(
    icon: ImageVector,
    label: String,
    middleText: String? = null,
    trailingActionText: String? = null,
    hasArrow: Boolean = false,
    contentColor: Color = Color.Black,
    actionColor: Color = Color(0xFF3478F6)
) {
    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Color.Gray)
        Spacer(Modifier.width(12.dp))
        Text(label, color = contentColor)

        Spacer(Modifier.weight(1f))

        if (middleText != null) {
            Text(middleText, color = Color.LightGray, fontSize = 14.sp, modifier = Modifier.padding(end = 12.dp))
        }

        if (trailingActionText != null) {
            Text(
                text = trailingActionText,
                color = actionColor,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(end = 4.dp)
            )
        }

        // Mũi tên đi kèm nếu có hành động hoặc arrow mặc định
        if (hasArrow || trailingActionText != null) {
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
        }
    }
}

@Composable
fun DateBadge(text: String) {
    Surface(color = Color(0xFFE5E5EA), shape = RoundedCornerShape(8.dp)) {
        Text(text, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontSize = 14.sp)
    }
}

@Composable
fun StatusToggle(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (isSelected) Color(0xFF3478F6) else Color(0xFFE5E5EA),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text,
            color = if (isSelected) Color.White else Color.Gray,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            fontSize = 14.sp
        )
    }
}

@Composable
fun BottomActionButton(text: String, modifier: Modifier, hasIcon: Boolean = false) {
    Button(
        onClick = { },
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(text, color = Color(0xFF3478F6))
        if (hasIcon) {
            Icon(Icons.Default.ArrowDropUp, null, tint = Color(0xFF3478F6))
        }
    }
}

@Composable
fun DividerLine() {
    HorizontalDivider(modifier = Modifier.padding(start = 52.dp), thickness = 0.5.dp, color = Color(0xFFF2F2F7))
}

@Preview(showBackground = true)
@Composable
fun PreviewExchange() {
    ExchangeScreen()
}