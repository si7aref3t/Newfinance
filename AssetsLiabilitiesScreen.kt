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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssetEntity
import com.example.data.model.LiabilityEntity
import com.example.domain.FinancialEngine
import com.example.ui.theme.FinanceGreen
import com.example.ui.theme.FinanceRed
import com.example.ui.viewmodel.FinancialUiState

@Composable
fun AssetsLiabilitiesScreen(
    state: FinancialUiState,
    onAddAsset: () -> Unit,
    onEditAsset: (AssetEntity) -> Unit,
    onDeleteAsset: (AssetEntity) -> Unit,
    onAddLiability: () -> Unit,
    onEditLiability: (LiabilityEntity) -> Unit,
    onDeleteLiability: (LiabilityEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Assets, 1: Liabilities

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { if (selectedTab == 0) onAddAsset() else onAddLiability() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = if (selectedTab == 0) "أصل جديد" else "التزام جديد")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("الأصول والممتلكات (${state.assets.size})", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Home, null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("الالتزامات والديون (${state.liabilities.size})", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.TrendingDown, null) }
                )
            }

            if (selectedTab == 0) {
                // Assets Tab
                val totalAssets = state.assets.sumOf { it.currentValue }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("إجمالي قيمة الأصول والممتلكات", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = FinancialEngine.formatCurrency(totalAssets, state.currency),
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FinanceGreen
                                    )
                                }
                                Button(onClick = onAddAsset) {
                                    Text("＋ إضافة أصل")
                                }
                            }
                        }
                    }

                    if (state.assets.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("لا توجد أصول مضافة. أضف المعدات أو السيارات أو العقارات لتقييم ثروتك بدقة.")
                            }
                        }
                    } else {
                        items(state.assets, key = { it.id }) { asset ->
                            val diff = asset.currentValue - asset.purchasePrice
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(asset.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            Text(
                                                text = "${asset.category} • تاريخ الشراء: ${asset.date}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Row {
                                            IconButton(onClick = { onEditAsset(asset) }, modifier = Modifier.size(32.dp)) {
                                                Icon(Icons.Default.Edit, "تعديل", modifier = Modifier.size(16.dp))
                                            }
                                            IconButton(onClick = { onDeleteAsset(asset) }, modifier = Modifier.size(32.dp)) {
                                                Icon(Icons.Default.Delete, "حذف", modifier = Modifier.size(16.dp), tint = FinanceRed)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("سعر الشراء", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(FinancialEngine.formatCurrency(asset.purchasePrice, state.currency), fontSize = 12.sp)
                                        }
                                        Column {
                                            Text("القيمة الحالية", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                text = FinancialEngine.formatCurrency(asset.currentValue, state.currency),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("فارق التقييم", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                text = "${if (diff >= 0) "+" else ""}${FinancialEngine.formatCurrency(diff, state.currency)}",
                                                fontSize = 12.sp,
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
            } else {
                // Liabilities Tab
                val totalRemaining = state.liabilities.sumOf { it.remaining }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("إجمالي الالتزامات المتبقية", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = FinancialEngine.formatCurrency(totalRemaining, state.currency),
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FinanceRed
                                    )
                                }
                                Button(onClick = onAddLiability) {
                                    Text("＋ إضافة التزام")
                                }
                            }
                        }
                    }

                    if (state.liabilities.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("لا توجد التزامات مسجلة. رائع، أنت خالٍ من الديون!")
                            }
                        }
                    } else {
                        items(state.liabilities, key = { it.id }) { liab ->
                            val paid = liab.original - liab.remaining
                            val progress = if (liab.original > 0) ((paid / liab.original).coerceIn(0.0, 1.0)).toFloat() else 1f
                            val isPaidOff = liab.remaining <= 0

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(liab.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            if (liab.creditor.isNotBlank()) {
                                                Text("الدائن: ${liab.creditor}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (isPaidOff) FinanceGreen.copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f))
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = if (isPaidOff) "مسدد بالكامل" else liab.status,
                                                    color = if (isPaidOff) FinanceGreen else Color(0xFFD97706),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            IconButton(onClick = { onEditLiability(liab) }, modifier = Modifier.size(32.dp)) {
                                                Icon(Icons.Default.Edit, "تعديل", modifier = Modifier.size(16.dp))
                                            }
                                            IconButton(onClick = { onDeleteLiability(liab) }, modifier = Modifier.size(32.dp)) {
                                                Icon(Icons.Default.Delete, "حذف", modifier = Modifier.size(16.dp), tint = FinanceRed)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Progress Bar
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("سددت: ${String.format("%.0f%%", progress * 100)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("المتبقي: ${FinancialEngine.formatCurrency(liab.remaining, state.currency)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FinanceRed)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                        color = FinanceGreen,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )

                                    if (liab.dueDate.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("تاريخ الاستحقاق: ${liab.dueDate}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
