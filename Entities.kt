package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType(val labelAr: String) {
    CAPITAL("رأس مال"),
    INCOME("إيراد"),
    EXPENSE("مصروف"),
    INVESTMENT("استثمار"),
    TRANSFER("تحويل"),
    ASSET("أصل"),
    LIABILITY("التزام");

    companion object {
        fun fromString(value: String): TransactionType =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: EXPENSE
    }
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // TransactionType.name
    val amount: Double,
    val date: String, // YYYY-MM-DD
    val time: String = "12:00",
    val category: String = "",
    val description: String,
    val notes: String = "",
    val method: String = "cash", // "cash" or "bank"
    val accountId: Long? = null,
    val fromId: String? = null, // "CASH" or bank account ID
    val toId: String? = null,   // "CASH" or bank account ID
    val dir: String? = null,    // "fund" or "pay"
    val invId: Long? = null
)

@Entity(tableName = "bank_accounts")
data class BankAccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bankName: String,
    val accountName: String,
    val last4: String = "",
    val opening: Double = 0.0,
    val currency: String = "EGP",
    val notes: String = ""
)

@Entity(tableName = "investments")
data class InvestmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // "أسهم", "صناديق", "عملات رقمية", "عقار", "أخرى"
    val amount: Double,
    val currentValue: Double,
    val date: String,
    val status: String = "نشط", // "نشط" or "مغلق"
    val notes: String = ""
)

@Entity(tableName = "assets")
data class AssetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String = "عام",
    val purchasePrice: Double = 0.0,
    val currentValue: Double,
    val date: String,
    val notes: String = ""
)

@Entity(tableName = "liabilities")
data class LiabilityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val creditor: String = "",
    val original: Double,
    val remaining: Double,
    val dueDate: String = "",
    val status: String = "جاري", // "جاري", "متأخر", "مسدد"
    val notes: String = ""
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val month: String, // YYYY-MM
    val category: String,
    val limit: Double
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val target: Double,
    val current: Double = 0.0,
    val deadline: String = "",
    val notes: String = ""
)

@Entity(tableName = "recurring")
data class RecurringEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String = "EXPENSE", // "EXPENSE" or "INCOME"
    val amount: Double,
    val category: String = "",
    val description: String,
    val method: String = "cash",
    val accountId: Long? = null,
    val day: Int = 1,
    val lastMonth: String? = null,
    val notes: String = ""
)

@Entity(tableName = "settings")
data class AppSettingEntity(
    @PrimaryKey val key: String,
    val value: String
)
