package com.example.financial.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

@Composable
fun TransactionTypeIcon(
    icon: ImageVector,
    isSelected: Boolean,
    selectedColor: Color = Color.Red,
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
            tint = if (isSelected) selectedColor else Color.Black,
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
    contentColor: Color = Color.Black
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.Gray)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, color = contentColor)

        if (middleText != null) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = middleText,
                color = Color.LightGray,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        if (trailingText != null) {
            Text(text = trailingText, color = Color.Gray, modifier = Modifier.padding(end = 4.dp))
        }
        if (hasArrow) {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

@Composable
fun TransactionDateBadge(text: String) {
    Surface(
        color = Color(0xFFE5E5EA),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 14.sp
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
            .padding(16.dp)
    ) {
        if (showHeader) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onCloseClick,
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(50))
                        .size(40.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.Black)
                }

                typeSelector()

                Button(
                    onClick = onSaveClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3478F6)),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp)
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
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                content()
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TransactionBottomActionButton("Options", Modifier.weight(1f))
            TransactionBottomActionButton("Hide", Modifier.weight(1f), hasIcon = true)
        }
    }
}
