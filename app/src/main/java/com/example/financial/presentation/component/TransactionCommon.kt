package com.example.financial.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun parseNumericInput(input: String): Double {
    return try {
        val normalized = input.replace(",", ".")
        val clean = normalized.replace(Regex("[^0-9.-]"), "")
        val lastDotIndex = clean.lastIndexOf('.')
        if (lastDotIndex != -1) {
            val integerPart = clean.substring(0, lastDotIndex).replace(".", "")
            val fractionalPart = clean.substring(lastDotIndex + 1)
            (integerPart + "." + fractionalPart).toDouble()
        } else {
            clean.toDouble()
        }
    } catch (e: Exception) {
        0.0
    }
}

@Composable
fun TransactionTypeIcon(
    icon: ImageVector,
    isSelected: Boolean,
    selectedColor: Color = Color.Black,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(
                color = if (isSelected) Color.White else Color.Transparent,
                shape = RoundedCornerShape(50)
            )
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) selectedColor else Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun TransactionRow(
    icon: ImageVector,
    label: String,
    middleText: String? = null,
    trailingText: String? = null,
    hasArrow: Boolean = false,
    contentColor: Color = Color.Black,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, color = contentColor, fontSize = 16.sp, modifier = Modifier.weight(1f))

        if (middleText != null) {
            Text(
                text = middleText,
                color = Color.LightGray,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        if (trailingText != null) {
            Text(text = trailingText, color = Color.Gray, modifier = Modifier.padding(end = 4.dp))
        }
        if (hasArrow) {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}

@Composable
fun TransactionInputRow(
    icon: ImageVector,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    middleText: String? = null,
    trailingText: String? = null,
    hasArrow: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(text = placeholder, color = Color.LightGray, fontSize = 16.sp)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, color = Color.Black)
            )
        }
        if (middleText != null) {
            Text(text = middleText, color = Color.LightGray, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp))
        }
        if (trailingText != null) {
            Text(text = trailingText, color = Color.Gray, modifier = Modifier.padding(start = 8.dp))
        }
        if (hasArrow) {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}

@Composable
fun TransactionDateBadge(text: String) {
    Surface(
        color = Color(0xFFF2F2F7),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 14.sp,
            color = Color.Black
        )
    }
}

@Composable
fun TransactionStatusToggle(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (isSelected) Color(0xFF3478F6) else Color(0xFFE5E5EA),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.Gray,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            fontSize = 14.sp
        )
    }
}

@Composable
fun TransactionBottomActionButton(text: String, modifier: Modifier, hasIcon: Boolean = false) {
    Button(
        onClick = { },
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(text = text, color = Color(0xFF3478F6))
        if (hasIcon) {
            Icon(Icons.Default.ArrowDropUp, null, tint = Color(0xFF3478F6))
        }
    }
}

@Composable
fun TransactionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 52.dp),
        thickness = 0.5.dp,
        color = Color(0xFFF2F2F7)
    )
}

@Composable
fun ExpenseItem(
    icon: ImageVector,
    label: String,
    value: String = "",
    trailingIcon: ImageVector? = Icons.Default.ChevronRight,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, color = Color.Black, fontSize = 16.sp, modifier = Modifier.weight(1f))
        if (value.isNotEmpty()) {
            Text(text = value, color = Color.Gray, fontSize = 16.sp, modifier = Modifier.padding(end = 8.dp))
        }
        if (trailingIcon != null) {
            Icon(trailingIcon, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun ScheduledTransactionFields(
    isAutoPay: Boolean,
    onAutoPayChange: (Boolean) -> Unit,
    repeatValue: String = "Every month",
    onRepeatClick: () -> Unit = {},
    endValue: String = "Never",
    onEndClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.ScreenshotMonitor, null, tint = Color.Gray, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text("Auto-pay", color = Color.Black, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Outlined.HelpOutline, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = isAutoPay,
            onCheckedChange = onAutoPayChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF3478F6)
            )
        )
    }
    TransactionDivider()

    ExpenseItem(icon = Icons.Outlined.Repeat, label = "Repeat", value = repeatValue, onClick = onRepeatClick)
    TransactionDivider()

    ExpenseItem(icon = Icons.Outlined.StopCircle, label = "End", value = endValue, onClick = onEndClick)
    TransactionDivider()
}

@Composable
fun TransactionBaseScreen(
    title: String,
    onCloseClick: () -> Unit,
    onSaveClick: () -> Unit,
    typeSelector: @Composable () -> Unit,
    showHeader: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7))
            .verticalScroll(rememberScrollState())
    ) {
        if (showHeader) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onCloseClick,
                    modifier = Modifier
                        .background(Color(0xFFE5E5EA), CircleShape)
                        .size(36.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                }

                typeSelector()

                Button(
                    onClick = onSaveClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3478F6)),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text("Save", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                content()
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TransactionBottomActionButton("Options", Modifier.weight(1f))
            TransactionBottomActionButton("Hide", Modifier.weight(1f), hasIcon = true)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
