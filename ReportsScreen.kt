package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.FinancialEngine
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.IncomeExpenseBarChart
import com.example.ui.components.NetWorthLineChart
import com.example.ui.components.StatMetricCard
import com.example.ui.theme.FinanceGreen
import com.example.ui.theme.FinanceRed
import com.example.ui.viewmodel.FinancialUiState
import kotlin.math.max

@Composable
fun ReportsScreen(
    state: FinancialUiState,
    modifier: Modifier = Modifier
) {
    val monthlySeries = remember(state.transactions) {
        FinancialEngine.calculateMonthlySeries(state.transactions)
    }
    val categoryExpenses = remember(state.transactions) {
        FinancialEngine.calculateCategoryExpenses(state.transactions)
    }
    val netWorthTrend = remember(state.transactions, state.bankAccounts, state.investments, state.assets, state.liabilities) {
        FinancialEngine.calculateNetWorthTrend(
            state.transactions,
            state.bankAccounts,
            state.investments,
            state.assets,
            state.liabilities
        )
    }

    val incomes = state.transactions.filter { it.type == "INCOME" }
    val expenses = state.transactions.filter { it.type == "EXPENSE" }

    val maxIncome = incomes.maxByOrNull { it.amount }
    val maxExpense = expenses.maxByOrNull { it.amount }
    val topCategory = categoryExpenses.firstOrNull()

    val monthCount = max(1, monthlySeries.size)
    val avgMonthlyIncome = state.overview.income / monthCount
    val avgMonthlyExpense = state.overview.expense / monthCount
    val incomeToExpenseRatio = if (state.overview.expense > 0) String.format("%.2f", state.overview.income / state.overview.expense) else "∞"

    val totalInvested = state.investments.sumOf { it.amount }
    val totalInvValue = state.investments.sumOf { it.currentValue }
    val invProfit = totalInvValue - totalInvested
    val invRoi = if (totalInvested > 0) (invProfit / totalInvested) * 100 else 0.0

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "التقارير المالية والتحليلات",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "مؤشرات وإحصائيات دقيقة لفهم نشاطك المالي وتطوره",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Summary Metric Cards
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatMetricCard(
                    title = "أكبر إيراد منفرد",
                    amount = maxIncome?.amount ?: 0.0,
                    currency = state.currency,
                    icon = Icons.Default.TrendingUp,
                    accentColor = FinanceGreen,
                    subtext = maxIncome?.description ?: "لا توجد إيرادات",
                    modifier = Modifier.weight(1f)
                )
                StatMetricCard(
                    title = "أكبر مصروف منفرد",
                    amount = maxExpense?.amount ?: 0.0,
                    currency = state.currency,
                    icon = Icons.Default.TrendingDown,
                    accentColor = FinanceRed,
                    subtext = maxExpense?.description ?: "لا توجد مصروفات",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatMetricCard(
                    title = "متوسط الدخل الشهري",
                    amount = avgMonthlyIncome,
                    currency = state.currency,
                    icon = Icons.Default.Assessment,
                    accentColor = Color(0xFF2563EB),
                    subtext = "خلال $monthCount أشهر",
                    modifier = Modifier.weight(1f)
                )
                StatMetricCard(
                    title = "متوسط الصرف الشهري",
                    amount = avgMonthlyExpense,
                    currency = state.currency,
                    icon = Icons.Default.Assessment,
                    accentColor = Color(0xFFD97706),
                    subtext = "نسبة الدخل/المصروف: $incomeToExpenseRatio",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "المؤشرات الإضافية",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("أعلى فئة إنفاقاً:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = if (topCategory != null) "${topCategory.name} (${FinancialEngine.formatCurrency(topCategory.amount, state.currency)})" else "—",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("أرباح الاستثمار الإجمالية:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "${if (invProfit >= 0) "+" else ""}${FinancialEngine.formatCurrency(invProfit, state.currency)} (${String.format("%.1f%%", invRoi)})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (invProfit >= 0) FinanceGreen else FinanceRed
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("صافي التدفق المالي:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = FinancialEngine.formatCurrency(state.overview.netFlow, state.currency),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (state.overview.netFlow >= 0) FinanceGreen else FinanceRed
                        )
                    }
                }
            }
        }

        // Charts
        item {
            IncomeExpenseBarChart(
                monthlyData = monthlySeries,
                currency = state.currency
            )
        }

        item {
            CategoryDonutChart(
                title = "توزيع المصروفات حسب الفئات",
                slices = categoryExpenses,
                currency = state.currency
            )
        }

        item {
            NetWorthLineChart(
                points = netWorthTrend,
                currency = state.currency
            )
        }
    }
}
