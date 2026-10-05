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
        recurrence = if (frequencyUnit != null) {
            com.example.financial.domain.model.Recurrence(
                frequencyValue = frequencyValue ?: 1,
                frequencyUnit = frequencyUnit,
                endType = endType ?: "Never",
                endAfterCount = endAfterCount ?: 1
            )
        } else null
    )
}

fun Transaction.toEntity(userId: String = "anonymous"): TransactionEntity {
    return TransactionEntity(
        id = id,
        userId = userId,
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
        endAfterCount = recurrence?.endAfterCount
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
        paymentsMade = paymentsMade,
        paymentAccountId = paymentAccountId,
        paymentCategory = paymentCategory,
        paymentPayee = paymentPayee,
        asOfDate = asOfDate,
        currency = currency,
        orderIndex = orderIndex,
        monitoredByBudgetId = monitoredByBudgetId
    )
}

fun Account.toEntity(userId: String = "anonymous"): AccountEntity {
    return AccountEntity(
        id = id,
        userId = userId,
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
        paymentsMade = paymentsMade,
        paymentAccountId = paymentAccountId,
        paymentCategory = paymentCategory,
        paymentPayee = paymentPayee,
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

fun AccountGroup.toEntity(userId: String = "anonymous"): AccountGroupEntity {
    return AccountGroupEntity(
        id = id,
        userId = userId,
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
        rolloverEnabled = rolloverEnabled,
        accountIds = accountIds,
        categories = categories
    )
}

fun com.example.financial.domain.model.Budget.toEntity(userId: String = "anonymous"): BudgetEntity {
    return BudgetEntity(
        id = id,
        userId = userId,
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
        categories = categories
    )
}

fun BudgetGroupEntity.toDomain(): com.example.financial.domain.model.BudgetGroup {
    return com.example.financial.domain.model.BudgetGroup(
        id = id,
        name = name,
        color = Color(color)
    )
}

fun com.example.financial.domain.model.BudgetGroup.toEntity(userId: String = "anonymous"): BudgetGroupEntity {
    return BudgetGroupEntity(
        id = id,
        userId = userId,
        name = name,
        color = color.toArgb()
    )
}

// --- Firestore Mappers ---

fun Account.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "name" to name,
    "balance" to balance,
    "type" to type.name,
    "color" to color.toArgb(),
    "groupId" to groupId,
    "iconUri" to iconUri,
    "creditLimit" to creditLimit,
    "statementCloseDay" to statementCloseDay,
    "autoClear" to autoClear,
    "additionalInfo" to additionalInfo,
    "principalAmount" to principalAmount,
    "apr" to apr,
    "duration" to duration,
    "startDate" to startDate,
    "firstDueDate" to firstDueDate,
    "paymentsMade" to paymentsMade,
    "paymentAccountId" to paymentAccountId,
    "paymentCategory" to paymentCategory,
    "paymentPayee" to paymentPayee,
    "asOfDate" to asOfDate,
    "currency" to currency,
    "orderIndex" to orderIndex,
    "monitoredByBudgetId" to monitoredByBudgetId
)

fun accountFromMap(map: Map<String, Any?>): Account? {
    val id = map["id"] as? String ?: return null
    val name = map["name"] as? String ?: ""
    val balance = map["balance"] as? String ?: "0.00"
    val typeName = map["type"] as? String ?: com.example.financial.domain.model.AccountType.CHECKING.name
    val type = try { com.example.financial.domain.model.AccountType.valueOf(typeName) } catch (_: Exception) { com.example.financial.domain.model.AccountType.CHECKING }
    val colorInt = (map["color"] as? Long)?.toInt() ?: (map["color"] as? Int) ?: Color.Blue.toArgb()

    return Account(
        id = id,
        name = name,
        balance = balance,
        type = type,
        color = Color(colorInt),
        groupId = map["groupId"] as? String,
        iconUri = map["iconUri"] as? String,
        creditLimit = map["creditLimit"] as? String,
        statementCloseDay = map["statementCloseDay"] as? String,
        autoClear = map["autoClear"] as? Boolean ?: false,
        additionalInfo = map["additionalInfo"] as? String,
        principalAmount = map["principalAmount"] as? String,
        apr = map["apr"] as? String,
        duration = map["duration"] as? String,
        startDate = map["startDate"] as? String,
        firstDueDate = map["firstDueDate"] as? String,
        paymentsMade = (map["paymentsMade"] as? Long)?.toInt() ?: (map["paymentsMade"] as? Int) ?: 0,
        paymentAccountId = map["paymentAccountId"] as? String,
        paymentCategory = map["paymentCategory"] as? String,
        paymentPayee = map["paymentPayee"] as? String,
        asOfDate = map["asOfDate"] as? String,
        currency = map["currency"] as? String,
        orderIndex = (map["orderIndex"] as? Long)?.toInt() ?: (map["orderIndex"] as? Int) ?: 0,
        monitoredByBudgetId = map["monitoredByBudgetId"] as? String
    )
}

fun Transaction.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "type" to type.name,
    "fromAccountId" to fromAccountId,
    "toAccountId" to toAccountId,
    "amount" to amount,
    "categoryId" to categoryId,
    "payee" to payee,
    "description" to description,
    "date" to date,
    "status" to status.name,
    "memo" to memo,
    "symbol" to symbol,
    "shares" to shares,
    "pricePerShare" to pricePerShare,
    "commission" to commission,
    "exchangeRate" to exchangeRate,
    "frequencyValue" to recurrence?.frequencyValue,
    "frequencyUnit" to recurrence?.frequencyUnit,
    "endType" to recurrence?.endType,
    "endAfterCount" to recurrence?.endAfterCount
)

fun transactionFromMap(map: Map<String, Any?>): Transaction? {
    val id = map["id"] as? String ?: return null
    val typeName = map["type"] as? String ?: com.example.financial.domain.model.TransactionType.EXPENSE.name
    val type = try { com.example.financial.domain.model.TransactionType.valueOf(typeName) } catch (_: Exception) { com.example.financial.domain.model.TransactionType.EXPENSE }
    val statusName = map["status"] as? String ?: com.example.financial.domain.model.TransactionStatus.CLEARED.name
    val status = try { com.example.financial.domain.model.TransactionStatus.valueOf(statusName) } catch (_: Exception) { com.example.financial.domain.model.TransactionStatus.CLEARED }

    val freqUnit = map["frequencyUnit"] as? String
    val recurrence = if (freqUnit != null) {
        com.example.financial.domain.model.Recurrence(
            frequencyValue = (map["frequencyValue"] as? Long)?.toInt() ?: (map["frequencyValue"] as? Int) ?: 1,
            frequencyUnit = freqUnit,
            endType = map["endType"] as? String ?: "Never",
            endAfterCount = (map["endAfterCount"] as? Long)?.toInt() ?: (map["endAfterCount"] as? Int) ?: 1
        )
    } else null

    return Transaction(
        id = id,
        type = type,
        fromAccountId = map["fromAccountId"] as? String ?: "",
        toAccountId = map["toAccountId"] as? String,
        amount = (map["amount"] as? Number)?.toDouble() ?: 0.0,
        categoryId = map["categoryId"] as? String,
        payee = map["payee"] as? String,
        description = map["description"] as? String,
        date = (map["date"] as? Number)?.toLong() ?: System.currentTimeMillis(),
        status = status,
        memo = map["memo"] as? String,
        symbol = map["symbol"] as? String,
        shares = (map["shares"] as? Number)?.toDouble(),
        pricePerShare = (map["pricePerShare"] as? Number)?.toDouble(),
        commission = (map["commission"] as? Number)?.toDouble(),
        exchangeRate = (map["exchangeRate"] as? Number)?.toDouble(),
        recurrence = recurrence
    )
}

fun com.example.financial.domain.model.Budget.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "name" to name,
    "amount" to amount,
    "isIncome" to isIncome,
    "color" to color.toArgb(),
    "budgetGroupId" to budgetGroupId,
    "startDate" to startDate,
    "repeatEnabled" to repeatEnabled,
    "frequencyValue" to frequencyValue,
    "frequencyUnit" to frequencyUnit,
    "rolloverEnabled" to rolloverEnabled,
    "accountIds" to accountIds,
    "categories" to categories
)

@Suppress("UNCHECKED_CAST")
fun budgetFromMap(map: Map<String, Any?>): com.example.financial.domain.model.Budget? {
    val id = map["id"] as? String ?: return null
    val name = map["name"] as? String ?: ""
    val colorInt = (map["color"] as? Long)?.toInt() ?: (map["color"] as? Int) ?: Color.Blue.toArgb()

    return com.example.financial.domain.model.Budget(
        id = id,
        name = name,
        amount = (map["amount"] as? Number)?.toDouble() ?: 0.0,
        isIncome = map["isIncome"] as? Boolean ?: false,
        color = Color(colorInt),
        budgetGroupId = map["budgetGroupId"] as? String,
        startDate = (map["startDate"] as? Number)?.toLong() ?: System.currentTimeMillis(),
        repeatEnabled = map["repeatEnabled"] as? Boolean ?: true,
        frequencyValue = (map["frequencyValue"] as? Long)?.toInt() ?: (map["frequencyValue"] as? Int) ?: 1,
        frequencyUnit = map["frequencyUnit"] as? String ?: "month",
        rolloverEnabled = map["rolloverEnabled"] as? Boolean ?: false,
        accountIds = (map["accountIds"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
        categories = (map["categories"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
    )
}

fun AccountGroup.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "name" to name,
    "iconName" to iconName,
    "iconUri" to iconUri,
    "color" to color.toArgb(),
    "orderIndex" to orderIndex
)

fun accountGroupFromMap(map: Map<String, Any?>): AccountGroup? {
    val id = map["id"] as? String ?: return null
    val colorInt = (map["color"] as? Long)?.toInt() ?: (map["color"] as? Int) ?: Color.Blue.toArgb()
    return AccountGroup(
        id = id,
        name = map["name"] as? String ?: "",
        iconName = map["iconName"] as? String,
        iconUri = map["iconUri"] as? String,
        color = Color(colorInt),
        orderIndex = (map["orderIndex"] as? Long)?.toInt() ?: (map["orderIndex"] as? Int) ?: 0
    )
}

fun com.example.financial.domain.model.BudgetGroup.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "name" to name,
    "color" to color.toArgb()
)

fun budgetGroupFromMap(map: Map<String, Any?>): com.example.financial.domain.model.BudgetGroup? {
    val id = map["id"] as? String ?: return null
    val colorInt = (map["color"] as? Long)?.toInt() ?: (map["color"] as? Int) ?: Color.Blue.toArgb()
    return com.example.financial.domain.model.BudgetGroup(
        id = id,
        name = map["name"] as? String ?: "",
        color = Color(colorInt)
    )
}
