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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionType
import com.example.domain.FinancialEngine
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.IncomeExpenseBarChart
import com.example.ui.components.NetWorthHeroCard
import com.example.ui.components.NetWorthLineChart
import com.example.ui.components.StatMetricCard
import com.example.ui.theme.FinanceGreen
import com.example.ui.theme.FinanceRed
import com.example.ui.viewmodel.FinancialUiState

@Composable
fun DashboardScreen(
    state: FinancialUiState,
    onAddTransaction: (TransactionType) -> Unit,
    onNavigateTab: (Int) -> Unit,
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

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Net Worth Banner
        item {
            NetWorthHeroCard(
                overview = state.overview,
                currency = state.currency,
                businessName = state.businessName
            )
        }

        // Quick Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onAddTransaction(TransactionType.INCOME) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = FinanceGreen)
                ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("إضافة إيراد", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Button(
                    onClick = { onAddTransaction(TransactionType.EXPENSE) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = FinanceRed)
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("إضافة مصروف", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = { onAddTransaction(TransactionType.TRANSFER) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("تحويل", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Key Stat Metrics Grid (2 per row)
        item {
            Text(
                text = "ملخص الحسابات والتدفقات",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatMetricCard(
                    title = "رأس المال",
                    amount = state.overview.capital,
                    currency = state.currency,
                    icon = Icons.Default.BusinessCenter,
                    accentColor = Color(0xFF2563EB),
                    modifier = Modifier.weight(1f),
                    onClick = { onAddTransaction(TransactionType.CAPITAL) }
                )
                StatMetricCard(
                    title = "إجمالي الإيرادات",
                    amount = state.overview.income,
                    currency = state.currency,
                    icon = Icons.Default.TrendingUp,
                    accentColor = FinanceGreen,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTab(1) } // Transactions
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatMetricCard(
                    title = "إجمالي المصروفات",
                    amount = state.overview.expense,
                    currency = state.currency,
                    icon = Icons.Default.MoneyOff,
                    accentColor = FinanceRed,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTab(1) }
                )
                StatMetricCard(
                    title = "صافي التدفق النقدي",
                    amount = state.overview.netFlow,
                    currency = state.currency,
                    icon = Icons.Default.SwapHoriz,
                    accentColor = if (state.overview.netFlow >= 0) FinanceGreen else FinanceRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatMetricCard(
                    title = "رصيد النقدية (الخزينة)",
                    amount = state.overview.cash,
                    currency = state.currency,
                    icon = Icons.Default.AccountBalanceWallet,
                    accentColor = Color(0xFFD97706),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTab(2) } // Accounts
                )
                StatMetricCard(
                    title = "أرصدة البنوك (${state.bankAccounts.size})",
                    amount = state.overview.totalBanks,
                    currency = state.currency,
                    icon = Icons.Default.AccountBalance,
                    accentColor = Color(0xFF4F46E5),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTab(2) }
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatMetricCard(
                    title = "قيمة الاستثمارات",
                    amount = state.overview.invValue,
                    currency = state.currency,
                    icon = Icons.Default.ShowChart,
                    accentColor = Color(0xFF7C3AED),
                    subtext = "المستثمر: ${FinancialEngine.formatCurrency(state.overview.invested, state.currency)}",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTab(3) } // Investments
                )
                StatMetricCard(
                    title = "الأصول الأخرى",
                    amount = state.overview.assetsOther,
                    currency = state.currency,
                    icon = Icons.Default.Home,
                    accentColor = Color(0xFF0D9488),
                    subtext = "${state.assets.size} أصول مسجلة",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTab(4) } // Assets & Liabilities
                )
            }
        }

        // Charts Section
        item {
            Spacer(modifier = Modifier.height(6.dp))
            IncomeExpenseBarChart(
                monthlyData = monthlySeries,
                currency = state.currency
            )
        }

        item {
            CategoryDonutChart(
                title = "توزيع المصروفات حسب الفئة",
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
