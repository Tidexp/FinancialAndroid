package com.example.financial.data.repository

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.financial.data.local.entity.AccountEntity
import com.example.financial.data.local.entity.AccountGroupEntity
import com.example.financial.data.local.entity.BudgetEntity
import com.example.financial.data.local.entity.BudgetGroupEntity
import com.example.financial.data.local.entity.TransactionEntity
import com.example.financial.domain.model.Account
import com.example.financial.domain.model.AccountGroup
import com.example.financial.domain.model.Transaction

fun TransactionEntity.toDomain(): Transaction {
    return Transaction(
        id = id,
        type = type,
        fromAccountId = fromAccountId,
        toAccountId = toAccountId,
        amount = amount,
        categoryId = categoryId,
        payee = payee,
        description = description,
        date = date,
        status = status,
        memo = memo,
        symbol = symbol,
        shares = shares,
        pricePerShare = pricePerShare,
        commission = commission,
        exchangeRate = exchangeRate
    )
}

fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        type = type,
        fromAccountId = fromAccountId,
        toAccountId = toAccountId,
        amount = amount,
        categoryId = categoryId,
        payee = payee,
        description = description,
        date = date,
        status = status,
        memo = memo,
        symbol = symbol,
        shares = shares,
        pricePerShare = pricePerShare,
        commission = commission,
        exchangeRate = exchangeRate
    )
}

fun AccountEntity.toDomain(): Account {
    return Account(
        id = id,
        name = name,
        balance = balance,
        type = type,
        color = Color(color),
        groupId = groupId,
        iconUri = iconUri,
        creditLimit = creditLimit,
        statementCloseDay = statementCloseDay,
        autoClear = autoClear,
        additionalInfo = additionalInfo,
        principalAmount = principalAmount,
        apr = apr,
        duration = duration,
        startDate = startDate,
        firstDueDate = firstDueDate,
        asOfDate = asOfDate,
        currency = currency,
        orderIndex = orderIndex,
        monitoredByBudgetId = monitoredByBudgetId
    )
}

fun Account.toEntity(): AccountEntity {
    return AccountEntity(
        id = id,
        name = name,
        balance = balance,
        type = type,
        color = color.toArgb(),
        groupId = groupId,
        iconUri = iconUri,
        creditLimit = creditLimit,
        statementCloseDay = statementCloseDay,
        autoClear = autoClear,
        additionalInfo = additionalInfo,
        principalAmount = principalAmount,
        apr = apr,
        duration = duration,
        startDate = startDate,
        firstDueDate = firstDueDate,
        asOfDate = asOfDate,
        currency = currency,
        orderIndex = orderIndex,
        monitoredByBudgetId = monitoredByBudgetId
    )
}

fun AccountGroupEntity.toDomain(): AccountGroup {
    return AccountGroup(
        id = id,
        name = name,
        iconName = iconName,
        iconUri = iconUri,
        color = Color(color),
        orderIndex = orderIndex
    )
}

fun AccountGroup.toEntity(): AccountGroupEntity {
    return AccountGroupEntity(
        id = id,
        name = name,
        iconName = iconName,
        iconUri = iconUri,
        color = color.toArgb(),
        orderIndex = orderIndex
    )
}

fun BudgetEntity.toDomain(): com.example.financial.domain.model.Budget {
    return com.example.financial.domain.model.Budget(
        id = id,
        name = name,
        amount = amount,
        isIncome = isIncome,
        color = Color(color),
        budgetGroupId = budgetGroupId,
        startDate = startDate,
        repeatEnabled = repeatEnabled,
        frequencyValue = frequencyValue,
        frequencyUnit = frequencyUnit,
        rolloverEnabled = rolloverEnabled
    )
}

fun com.example.financial.domain.model.Budget.toEntity(): BudgetEntity {
    return BudgetEntity(
        id = id,
        name = name,
        amount = amount,
        isIncome = isIncome,
        color = color.toArgb(),
        budgetGroupId = budgetGroupId,
        startDate = startDate,
        repeatEnabled = repeatEnabled,
        frequencyValue = frequencyValue,
        frequencyUnit = frequencyUnit,
        rolloverEnabled = rolloverEnabled
    )
}

fun BudgetGroupEntity.toDomain(): com.example.financial.domain.model.BudgetGroup {
    return com.example.financial.domain.model.BudgetGroup(
        id = id,
        name = name,
        color = Color(color)
    )
}

fun com.example.financial.domain.model.BudgetGroup.toEntity(): BudgetGroupEntity {
    return BudgetGroupEntity(
        id = id,
        name = name,
        color = color.toArgb()
    )
}
