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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Warning
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
import com.example.data.model.BudgetEntity
import com.example.data.model.GoalEntity
import com.example.domain.FinancialEngine
import com.example.ui.theme.FinanceGreen
import com.example.ui.theme.FinanceRed
import com.example.ui.viewmodel.FinancialUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BudgetsGoalsScreen(
    state: FinancialUiState,
    onAddBudget: () -> Unit,
    onEditBudget: (BudgetEntity) -> Unit,
    onDeleteBudget: (BudgetEntity) -> Unit,
    onAddGoal: () -> Unit,
    onEditGoal: (GoalEntity) -> Unit,
    onDeleteGoal: (GoalEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val currentMonth = remember { SimpleDateFormat("yyyy-MM", Locale.US).format(Date()) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { if (selectedTab == 0) onAddBudget() else onAddGoal() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = if (selectedTab == 0) "ميزانية جديدة" else "هدف مالي جديد")
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
                    text = { Text("الميزانيات الشهرية (${state.budgets.size})", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.PieChart, null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("الأهداف المالية (${state.goals.size})", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Flag, null) }
                )
            }

            if (selectedTab == 0) {
                // Budgets List
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
                                    Text("ميزانيات شهر $currentMonth", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("تتبع سقف الصرف لتجنب الإفراط المالي", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Button(onClick = onAddBudget) {
                                    Text("＋ ميزانية")
                                }
                            }
                        }
                    }

                    if (state.budgets.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("لا توجد ميزانيات محددة لهذا الشهر. أنشئ ميزانية لفئات المصروفات.")
                            }
                        }
                    } else {
                        items(state.budgets, key = { it.id }) { b ->
                            // Calculate spent for this category and month
                            val spent = state.transactions
                                .filter { it.type == "EXPENSE" && it.category == b.category && it.date.startsWith(b.month) }
                                .sumOf { it.amount }

                            val remaining = b.limit - spent
                            val progress = if (b.limit > 0) ((spent / b.limit).coerceIn(0.0, 1.0)).toFloat() else 1f
                            val isOver = spent > b.limit

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
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(b.category, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            Spacer(modifier = Modifier.size(6.dp))
                                            Text("(${b.month})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }

                                        Row {
                                            IconButton(onClick = { onEditBudget(b) }, modifier = Modifier.size(32.dp)) {
                                                Icon(Icons.Default.Edit, "تعديل", modifier = Modifier.size(16.dp))
                                            }
                                            IconButton(onClick = { onDeleteBudget(b) }, modifier = Modifier.size(32.dp)) {
                                                Icon(Icons.Default.Delete, "حذف", modifier = Modifier.size(16.dp), tint = FinanceRed)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "صُرف: ${FinancialEngine.formatCurrency(spent, state.currency)}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isOver) FinanceRed else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "الميزانية: ${FinancialEngine.formatCurrency(b.limit, state.currency)}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                        color = if (isOver) FinanceRed else if (progress > 0.8f) Color(0xFFF59E0B) else FinanceGreen,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    if (isOver) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Warning, null, tint = FinanceRed, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.size(4.dp))
                                            Text(
                                                text = "تجاوزت الميزانية بمقدار ${FinancialEngine.formatCurrency(spent - b.limit, state.currency)}",
                                                color = FinanceRed,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = "المتبقي: ${FinancialEngine.formatCurrency(remaining, state.currency)}",
                                            color = FinanceGreen,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Goals List
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
                                    Text("الأهداف المالية والادخار", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("حدد أهدافك وتابع مسار تحقيقها", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Button(onClick = onAddGoal) {
                                    Text("＋ هدف جديد")
                                }
                            }
                        }
                    }

                    if (state.goals.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("لا توجد أهداف مضافة بعد. أضف هدفاً كشراء أصول أو بناء صندوق طوارئ.")
                            }
                        }
                    } else {
                        items(state.goals, key = { it.id }) { goal ->
                            val progress = if (goal.target > 0) ((goal.current / goal.target).coerceIn(0.0, 1.0)).toFloat() else 1f
                            val isAchieved = goal.current >= goal.target

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
                                            Text(goal.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            if (goal.deadline.isNotBlank()) {
                                                Text("الموعد المستهدف: ${goal.deadline}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (isAchieved) {
                                                Icon(Icons.Default.CheckCircle, null, tint = FinanceGreen, modifier = Modifier.size(20.dp))
                                                Spacer(modifier = Modifier.size(4.dp))
                                                Text("مكتمل!", color = FinanceGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                            IconButton(onClick = { onEditGoal(goal) }, modifier = Modifier.size(32.dp)) {
                                                Icon(Icons.Default.Edit, "تعديل", modifier = Modifier.size(16.dp))
                                            }
                                            IconButton(onClick = { onDeleteGoal(goal) }, modifier = Modifier.size(32.dp)) {
                                                Icon(Icons.Default.Delete, "حذف", modifier = Modifier.size(16.dp), tint = FinanceRed)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "المحقق: ${FinancialEngine.formatCurrency(goal.current, state.currency)} (${String.format("%.0f%%", progress * 100)})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "المستهدف: ${FinancialEngine.formatCurrency(goal.target, state.currency)}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                        color = if (isAchieved) FinanceGreen else MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )

                                    if (goal.notes.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(goal.notes, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
