package com.example.financial.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.financial.domain.model.AccountType

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey var id: String = "",
    var name: String = "",
    var balance: String = "0.00",
    var type: AccountType = AccountType.CHECKING,
    var color: Int = 0,
    var groupId: String? = null,
    var iconUri: String? = null,
    var creditLimit: String? = null,
    var statementCloseDay: String? = null,
    var autoClear: Boolean = false,
    var additionalInfo: String? = null,
    // Loan specific
    var principalAmount: String? = null,
    var apr: String? = null,
    var duration: String? = null,
    var startDate: String? = null,
    var firstDueDate: String? = null,
    // Investment specific
    var asOfDate: String? = null,
    // Forex specific
    var currency: String? = null,
    var lastUpdated: Long = System.currentTimeMillis(),
    var orderIndex: Int = 0,
    var monitoredByBudgetId: String? = null,
    var userId: String = "",
    var isSynced: Boolean = false
)
