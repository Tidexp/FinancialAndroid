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
        exchangeRate = exchangeRate,
        recurrence = frequencyUnit?.let { unit ->
            com.example.financial.domain.model.Recurrence(
                frequencyValue = frequencyValue ?: 1,
                frequencyUnit = unit,
                endType = endType ?: "Never",
                endAfterCount = endAfterCount ?: 1
            )
        }
    )
}

fun Transaction.toEntity(userId: String = "", isSynced: Boolean = false): TransactionEntity {
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
        exchangeRate = exchangeRate,
        frequencyValue = recurrence?.frequencyValue,
        frequencyUnit = recurrence?.frequencyUnit,
        endType = recurrence?.endType,
        endAfterCount = recurrence?.endAfterCount,
        userId = userId,
        isSynced = isSynced
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

fun Account.toEntity(userId: String = "", isSynced: Boolean = false): AccountEntity {
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
        monitoredByBudgetId = monitoredByBudgetId,
        userId = userId,
        isSynced = isSynced
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

fun AccountGroup.toEntity(userId: String = "", isSynced: Boolean = false): AccountGroupEntity {
    return AccountGroupEntity(
        id = id,
        name = name,
        iconName = iconName,
        iconUri = iconUri,
        color = color.toArgb(),
        orderIndex = orderIndex,
        userId = userId,
        isSynced = isSynced
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
        rolloverEnabled = rolloverEnabled,
        accountIds = accountIds,
        categories = categories
    )
}

fun com.example.financial.domain.model.Budget.toEntity(userId: String = "", isSynced: Boolean = false): BudgetEntity {
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
        rolloverEnabled = rolloverEnabled,
        accountIds = accountIds,
        categories = categories,
        userId = userId,
        isSynced = isSynced
    )
}

fun BudgetGroupEntity.toDomain(): com.example.financial.domain.model.BudgetGroup {
    return com.example.financial.domain.model.BudgetGroup(
        id = id,
        name = name,
        color = Color(color)
    )
}

fun com.example.financial.domain.model.BudgetGroup.toEntity(userId: String = "", isSynced: Boolean = false): BudgetGroupEntity {
    return BudgetGroupEntity(
        id = id,
        name = name,
        color = color.toArgb(),
        userId = userId,
        isSynced = isSynced
    )
}
