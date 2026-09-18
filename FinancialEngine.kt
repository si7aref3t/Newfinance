package com.example.domain

import com.example.data.model.AssetEntity
import com.example.data.model.BankAccountEntity
import com.example.data.model.InvestmentEntity
import com.example.data.model.LiabilityEntity
import com.example.data.model.TransactionEntity
import kotlin.math.abs

data class TxDeltas(
    val cash: Double = 0.0,
    val banks: Map<Long, Double> = emptyMap()
)

data class FinancialOverview(
    val cash: Double = 0.0,
    val bankBalances: Map<Long, Double> = emptyMap(),
    val totalBanks: Double = 0.0,
    val income: Double = 0.0,
    val expense: Double = 0.0,
    val capital: Double = 0.0,
    val transfers: Double = 0.0,
    val invested: Double = 0.0,
    val invValue: Double = 0.0,
    val assetsOther: Double = 0.0,
    val assets: Double = 0.0,
    val liabilities: Double = 0.0,
    val netWorth: Double = 0.0,
    val netFlow: Double = 0.0
)

data class MonthlyData(
    val monthKey: String, // YYYY-MM
    val displayLabel: String, // MM/YY
    val income: Double,
    val expense: Double,
    val netFlow: Double
)

data class CategorySlice(
    val name: String,
    val amount: Double,
    val percentage: Float,
    val colorHex: String
)

data class NetWorthPoint(
    val monthLabel: String,
    val netWorth: Double
)

object FinancialEngine {

    fun calculateTxDeltas(t: TransactionEntity): TxDeltas {
        var cashDelta = 0.0
        val bankMap = mutableMapOf<Long, Double>()
        val a = t.amount

        fun addBank(id: Long, delta: Double) {
            bankMap[id] = (bankMap[id] ?: 0.0) + delta
        }

        when (t.type.uppercase()) {
            "CAPITAL", "INCOME" -> {
                if (t.method == "bank" && t.accountId != null) {
                    addBank(t.accountId, a)
                } else {
                    cashDelta += a
                }
            }
            "EXPENSE", "INVESTMENT", "ASSET" -> {
                if (t.method == "bank" && t.accountId != null) {
                    addBank(t.accountId, -a)
                } else {
                    cashDelta -= a
                }
            }
            "LIABILITY" -> {
                val sign = if (t.dir == "pay") -1.0 else 1.0
                if (t.method == "bank" && t.accountId != null) {
                    addBank(t.accountId, sign * a)
                } else {
                    cashDelta += sign * a
                }
            }
            "TRANSFER" -> {
                if (t.fromId == "CASH" || t.fromId == null) {
                    cashDelta -= a
                } else {
                    t.fromId.toLongOrNull()?.let { addBank(it, -a) }
                }

                if (t.toId == "CASH" || t.toId == null) {
                    cashDelta += a
                } else {
                    t.toId.toLongOrNull()?.let { addBank(it, a) }
                }
            }
        }
        return TxDeltas(cashDelta, bankMap)
    }

    fun calculateOverview(
        transactions: List<TransactionEntity>,
        bankAccounts: List<BankAccountEntity>,
        investments: List<InvestmentEntity>,
        assets: List<AssetEntity>,
        liabilities: List<LiabilityEntity>
    ): FinancialOverview {
        var cash = 0.0
        val bankMap = mutableMapOf<Long, Double>()
        var income = 0.0
        var expense = 0.0
        var capital = 0.0
        var transfers = 0.0

        for (t in transactions) {
            when (t.type.uppercase()) {
                "INCOME" -> income += t.amount
                "EXPENSE" -> expense += t.amount
                "CAPITAL" -> capital += t.amount
                "TRANSFER" -> transfers += t.amount
            }
            val d = calculateTxDeltas(t)
            cash += d.cash
            for ((id, delta) in d.banks) {
                bankMap[id] = (bankMap[id] ?: 0.0) + delta
            }
        }

        val bankBalances = mutableMapOf<Long, Double>()
        var totalBanks = 0.0
        for (b in bankAccounts) {
            val bal = b.opening + (bankMap[b.id] ?: 0.0)
            bankBalances[b.id] = bal
            totalBanks += bal
        }

        val invested = investments.sumOf { it.amount }
        val invValue = investments.sumOf { it.currentValue }
        val assetsOther = assets.sumOf { it.currentValue }
        val liabilitiesTotal = liabilities.sumOf { it.remaining }
        val totalAssets = cash + totalBanks + invValue + assetsOther
        val netWorth = totalAssets - liabilitiesTotal
        val netFlow = income - expense

        return FinancialOverview(
            cash = cash,
            bankBalances = bankBalances,
            totalBanks = totalBanks,
            income = income,
            expense = expense,
            capital = capital,
            transfers = transfers,
            invested = invested,
            invValue = invValue,
            assetsOther = assetsOther,
            assets = totalAssets,
            liabilities = liabilitiesTotal,
            netWorth = netWorth,
            netFlow = netFlow
        )
    }

    fun getAccountBalance(
        accountId: String, // "CASH" or bank account ID
        overview: FinancialOverview
    ): Double {
        return if (accountId == "CASH") {
            overview.cash
        } else {
            val id = accountId.toLongOrNull() ?: return 0.0
            overview.bankBalances[id] ?: 0.0
        }
    }

    fun validateNegativeBalance(
        newTx: TransactionEntity,
        transactions: List<TransactionEntity>,
        bankAccounts: List<BankAccountEntity>,
        investments: List<InvestmentEntity>,
        assets: List<AssetEntity>,
        liabilities: List<LiabilityEntity>,
        preventNegative: Boolean
    ): String? {
        if (!preventNegative) return null

        val simulatedList = transactions.filter { it.id != newTx.id } + newTx
        val overview = calculateOverview(simulatedList, bankAccounts, investments, assets, liabilities)

        if (overview.cash < -0.001) {
            val currentCash = getAccountBalance("CASH", calculateOverview(transactions.filter { it.id != newTx.id }, bankAccounts, investments, assets, liabilities))
            return "رصيد غير كافٍ في النقدية (المتاح: ${formatCurrency(currentCash)})"
        }

        for (b in bankAccounts) {
            val bal = overview.bankBalances[b.id] ?: 0.0
            if (bal < -0.001) {
                val currentBal = getAccountBalance(b.id.toString(), calculateOverview(transactions.filter { it.id != newTx.id }, bankAccounts, investments, assets, liabilities))
                return "رصيد غير كافٍ في ${b.bankName} (${b.accountName}) (المتاح: ${formatCurrency(currentBal)})"
            }
        }
        return null
    }

    fun calculateMonthlySeries(transactions: List<TransactionEntity>): List<MonthlyData> {
        val map = mutableMapOf<String, Pair<Double, Double>>() // key -> Pair(income, expense)

        for (t in transactions) {
            if (t.type != "INCOME" && t.type != "EXPENSE") continue
            val key = if (t.date.length >= 7) t.date.substring(0, 7) else continue
            val current = map[key] ?: Pair(0.0, 0.0)
            if (t.type == "INCOME") {
                map[key] = Pair(current.first + t.amount, current.second)
            } else {
                map[key] = Pair(current.first, current.second + t.amount)
            }
        }

        return map.keys.sorted().map { key ->
            val (inc, exp) = map[key] ?: Pair(0.0, 0.0)
            val label = if (key.length >= 7) "${key.substring(5)}/${key.substring(2, 4)}" else key
            MonthlyData(
                monthKey = key,
                displayLabel = label,
                income = inc,
                expense = exp,
                netFlow = inc - exp
            )
        }
    }

    fun calculateCategoryExpenses(transactions: List<TransactionEntity>): List<CategorySlice> {
        val expenses = transactions.filter { it.type == "EXPENSE" }
        val total = expenses.sumOf { it.amount }
        if (total <= 0) return emptyList()

        val byCategory = mutableMapOf<String, Double>()
        for (t in expenses) {
            val cat = t.category.ifBlank { "أخرى" }
            byCategory[cat] = (byCategory[cat] ?: 0.0) + t.amount
        }

        val colors = listOf(
            "#EF4444", "#F59E0B", "#10B981", "#3B82F6", "#8B5CF6",
            "#EC4899", "#14B8A6", "#F97316", "#6366F1", "#84CC16"
        )

        var idx = 0
        return byCategory.entries
            .sortedByDescending { it.value }
            .map { (cat, amt) ->
                val slice = CategorySlice(
                    name = cat,
                    amount = amt,
                    percentage = ((amt / total) * 100).toFloat(),
                    colorHex = colors[idx % colors.size]
                )
                idx++
                slice
            }
    }

    fun calculateNetWorthTrend(
        transactions: List<TransactionEntity>,
        bankAccounts: List<BankAccountEntity>,
        investments: List<InvestmentEntity>,
        assets: List<AssetEntity>,
        liabilities: List<LiabilityEntity>
    ): List<NetWorthPoint> {
        val months = transactions.mapNotNull {
            if (it.date.length >= 7) it.date.substring(0, 7) else null
        }.toMutableSet()

        if (months.isEmpty()) {
            return emptyList()
        }

        val sortedMonths = months.sorted()
        return sortedMonths.map { month ->
            val txUpToMonth = transactions.filter { it.date.substring(0, 7) <= month }
            val invUpToMonth = investments.filter { it.date.take(7) <= month }
            val assetsUpToMonth = assets.filter { it.date.take(7) <= month }
            val liabUpToMonth = liabilities.filter { it.dueDate.take(7) <= month || it.dueDate.isBlank() }

            val overview = calculateOverview(txUpToMonth, bankAccounts, invUpToMonth, assetsUpToMonth, liabUpToMonth)
            val label = if (month.length >= 7) "${month.substring(5)}/${month.substring(2, 4)}" else month
            NetWorthPoint(monthLabel = label, netWorth = overview.netWorth)
        }
    }

    fun formatCurrency(amount: Double, currency: String = "EGP"): String {
        val formatted = String.format("%,.2f", amount)
        return "$formatted $currency"
    }
}
