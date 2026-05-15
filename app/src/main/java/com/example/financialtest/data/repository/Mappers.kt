package com.example.financialtest.data.repository

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Money
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.financialtest.data.local.entity.AccountEntity
import com.example.financialtest.data.local.entity.AccountGroupEntity
import com.example.financialtest.data.local.entity.TransactionEntity
import com.example.financialtest.domain.model.Account
import com.example.financialtest.domain.model.AccountGroup
import com.example.financialtest.domain.model.Transaction

fun TransactionEntity.toDomain(): Transaction {
    return Transaction(
        id = id,
        payee = payee,
        category = category,
        amount = amount,
        date = date,
        accountName = accountName,
        isExpense = isExpense,
        icon = Icons.Default.Money, // Default icon, you can map this based on category
        status = status
    )
}

fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        payee = payee,
        category = category,
        amount = amount,
        date = date,
        accountName = accountName,
        isExpense = isExpense,
        status = status
    )
}

fun AccountEntity.toDomain(): Account {
    return Account(
        id = id,
        name = name,
        balance = balance,
        type = type,
        color = Color(color),
        groupId = groupId
    )
}

fun Account.toEntity(): AccountEntity {
    return AccountEntity(
        id = id,
        name = name,
        balance = balance,
        type = type,
        color = color.toArgb(),
        groupId = groupId
    )
}

fun AccountGroupEntity.toDomain(): AccountGroup {
    return AccountGroup(
        id = id,
        name = name,
        iconName = iconName,
        iconUri = iconUri,
        color = Color(color)
    )
}

fun AccountGroup.toEntity(): AccountGroupEntity {
    return AccountGroupEntity(
        id = id,
        name = name,
        iconName = iconName,
        iconUri = iconUri,
        color = color.toArgb()
    )
}
