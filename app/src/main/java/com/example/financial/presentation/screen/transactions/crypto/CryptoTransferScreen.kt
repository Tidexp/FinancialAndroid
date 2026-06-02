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
fun CryptoTransferScreen() {
    var isCleared by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7)) // Nền màu xám nhạt iOS
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

            // Thanh chọn phân hệ (Đầy đủ 5 Icons - Mũi tên hai chiều đang được chọn)
            Row(
                modifier = Modifier
                    .background(Color(0xFFE5E5EA), RoundedCornerShape(24.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TransactionTypeIcon(Icons.Outlined.Cached, isSelected = false)
                TransactionTypeIcon(Icons.Outlined.RemoveCircleOutline, isSelected = false)
                TransactionTypeIcon(Icons.Outlined.AddCircleOutline, isSelected = false)
                TransactionTypeIcon(Icons.Default.SwapHoriz, isSelected = true) // Cố định Transfer
                TransactionTypeIcon(Icons.Outlined.DragHandle, isSelected = false)
            }

            // Nút Save
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3478F6)),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                Text("Save", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tiêu đề: Transfer
        Text(
            text = "Transfer",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- KHUNG NHẬP LIỆU CRYPTO TRANSFER ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                // Khối tài khoản đi/đến có nút Swap dọc ở góc phải
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        // Tài khoản gốc đã chọn Crypto
                        CryptoRow(Icons.Outlined.CreditCard, "Crypto (-$2,73)", hasArrow = true, contentColor = Color.Black)
                        DividerLine()
                        CryptoRow(Icons.Outlined.CreditCard, "To Account", hasArrow = true, contentColor = Color.LightGray)
                    }

                    // Nút đổi chiều tài khoản (vị trí giữa 2 dòng)
                    IconButton(
                        onClick = { },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 36.dp)
                            .background(Color(0xFFE5E5EA), RoundedCornerShape(50))
                            .size(28.dp)
                    ) {
                        Icon(Icons.Default.SwapVert, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    }
                }
                DividerLine()

                // 1. Số tiền gửi đi (Sent)
                CryptoRow(Icons.Outlined.AddCircle, "0,00", middleText = "Sent", trailingText = "USD", hasArrow = true, contentColor = Color.Black)
                DividerLine()

                // 2. Phí chuyển khoản mạng lưới (Transfer Fee)
                CryptoRow(Icons.Outlined.AddCircle, "0,00", middleText = "Transfer Fee", trailingText = "USD", hasArrow = true, contentColor = Color.LightGray)
                DividerLine()

                // 3. Số tiền thực nhận (Received)
                CryptoRow(Icons.Outlined.AddCircle, "0,00", middleText = "Received", trailingText = "USD", hasArrow = true, contentColor = Color.LightGray)
                DividerLine()

                // 4. Description
                CryptoRow(Icons.Outlined.FontDownload, "Description", hasArrow = true, contentColor = Color.LightGray)
                DividerLine()

                // 5. Send date (Ngày gửi)
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CalendarMonth, null, tint = Color.Gray)
                    Spacer(Modifier.width(12.dp))
                    DateBadge("2 Jun 2026")
                    Spacer(Modifier.width(8.dp))
                    DateBadge("14:53")
                    Spacer(Modifier.weight(1f))
                    Text("Send date", color = Color.LightGray, fontSize = 14.sp)
                }
                DividerLine()

                // 6. Receive date (Ngày nhận)
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CalendarToday, null, tint = Color.Gray)
                    Spacer(Modifier.width(12.dp))
                    DateBadge("2 Jun 2026")
                    Spacer(Modifier.width(8.dp))
                    DateBadge("14:53")
                    Spacer(Modifier.weight(1f))
                    Text("Receive date", color = Color.LightGray, fontSize = 14.sp)
                }
                DividerLine()

                // 7. Status Cleared / Pending
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.BookmarkBorder, null, tint = Color.Gray)
                    Spacer(Modifier.width(12.dp))
                    StatusToggle("Cleared", isCleared) { isCleared = true }
                    Spacer(Modifier.width(8.dp))
                    StatusToggle("Pending", !isCleared) { isCleared = false }
                }
                DividerLine()

                // 8. Memo
                CryptoRow(Icons.Outlined.Description, "Memo", contentColor = Color.LightGray)
                DividerLine()

                // 9. Đính kèm hình ảnh
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Icon(Icons.Outlined.Image, null, tint = Color.Gray)
                    Icon(Icons.Outlined.AddCircleOutline, null, tint = Color(0xFF3478F6))
                }
                DividerLine()

                // 10. Tags
                CryptoRow(Icons.Outlined.LocalOffer, "Tags", hasArrow = true, contentColor = Color.LightGray)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- NÚT CHÂN TRANG ---
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            BottomActionButton("Options", Modifier.weight(1f))
            BottomActionButton("Hide", Modifier.weight(1f), hasIcon = true)
        }
    }
}

// --- THÀNH PHẦN GIAO DIỆN CON TÁI SỬ DỤNG ---

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
fun CryptoRow(
    icon: ImageVector,
    label: String,
    middleText: String? = null,
    trailingText: String? = null,
    hasArrow: Boolean = false,
    contentColor: Color = Color.Black
) {
    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Color.Gray)
        Spacer(Modifier.width(12.dp))
        Text(label, color = contentColor)

        if (middleText != null) {
            Spacer(Modifier.weight(1f))
            Text(middleText, color = Color.LightGray, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp))
        } else {
            Spacer(Modifier.weight(1f))
        }

        if (trailingText != null) Text(trailingText, color = Color.Gray, modifier = Modifier.padding(end = 4.dp))
        if (hasArrow) Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
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
fun PreviewCryptoTransfer() {
    CryptoTransferScreen()
}