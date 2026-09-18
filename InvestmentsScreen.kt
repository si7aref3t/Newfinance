package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InvestmentEntity
import com.example.domain.FinancialEngine
import com.example.ui.theme.FinanceGreen
import com.example.ui.theme.FinanceRed
import com.example.ui.viewmodel.FinancialUiState

@Composable
fun InvestmentsScreen(
    state: FinancialUiState,
    onAddInvestment: () -> Unit,
    onEditInvestment: (InvestmentEntity) -> Unit,
    onDeleteInvestment: (InvestmentEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalInvested = state.investments.sumOf { it.amount }
    val totalValue = state.investments.sumOf { it.currentValue }
    val netProfitLoss = totalValue - totalInvested
    val roiPercentage = if (totalInvested > 0) (netProfitLoss / totalInvested) * 100 else 0.0

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddInvestment,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "استثمار جديد")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Portfolio Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF5B21B6),
                                        Color(0xFF7C3AED),
                                        Color(0xFF8B5CF6)
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "محفظة الاستثمارات",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${state.investments.size} أصول استثمارية",
                                        color = Color.White,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = FinancialEngine.formatCurrency(totalValue, state.currency),
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "القيمة السوقية الإجمالية الحالية",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("المبلغ المستثمَر", fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
                                    Text(
                                        text = FinancialEngine.formatCurrency(totalInvested, state.currency),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Column {
                                    Text("الربح / الخسارة", fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
                                    Text(
                                        text = "${if (netProfitLoss >= 0) "+" else ""}${FinancialEngine.formatCurrency(netProfitLoss, state.currency)}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (netProfitLoss >= 0) Color(0xFF86EFAC) else Color(0xFFFCA5A5)
                                    )
                                }
                                Column {
                                    Text("عائد الاستثمار (ROI)", fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
                                    Text(
                                        text = String.format("%.2f%%", roiPercentage),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (roiPercentage >= 0) Color(0xFF86EFAC) else Color(0xFFFCA5A5)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Investment List Header
            item {
                Text(
                    text = "الأصول الاستثمارية الفردية",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (state.investments.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShowChart,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("لا توجد استثمارات مسجلة", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "سجل استثماراتك في الأسهم أو الصناديق أو العقارات لحساب أرباحها",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(onClick = onAddInvestment) {
                                Text("＋ إضافة استثمار جديد")
                            }
                        }
                    }
                }
            } else {
                items(state.investments, key = { it.id }) { inv ->
                    val diff = inv.currentValue - inv.amount
                    val itemRoi = if (inv.amount > 0) (diff / inv.amount) * 100 else 0.0

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(inv.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF7C3AED).copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(inv.type, fontSize = 10.sp, color = Color(0xFF7C3AED), fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(inv.date, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Row {
                                    IconButton(onClick = { onEditInvestment(inv) }, modifier = Modifier.size(34.dp)) {
                                        Icon(Icons.Default.Edit, "تعديل", modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(onClick = { onDeleteInvestment(inv) }, modifier = Modifier.size(34.dp)) {
                                        Icon(Icons.Default.Delete, "حذف", modifier = Modifier.size(16.dp), tint = FinanceRed)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("المستثمَر", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = FinancialEngine.formatCurrency(inv.amount, state.currency),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Column {
                                    Text("القيمة الحالية", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = FinancialEngine.formatCurrency(inv.currentValue, state.currency),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("الربح/الخسارة", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = "${if (diff >= 0) "+" else ""}${FinancialEngine.formatCurrency(diff, state.currency)} (${String.format("%.1f%%", itemRoi)})",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (diff >= 0) FinanceGreen else FinanceRed
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
