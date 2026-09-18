package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.CategorySlice
import com.example.domain.MonthlyData
import com.example.domain.NetWorthPoint
import com.example.ui.theme.FinanceGreen
import com.example.ui.theme.FinanceRed
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

@Composable
fun IncomeExpenseBarChart(
    monthlyData: List<MonthlyData>,
    currency: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                Text(
                    text = "الإيرادات مقابل المصروفات",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(FinanceGreen, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إيراد", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.size(10.dp).background(FinanceRed, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("مصروف", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (monthlyData.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("لا توجد بيانات كافية", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                val maxVal = max(1.0, monthlyData.maxOf { max(it.income, it.expense) })

                Canvas(
                    modifier = Modifier.fillMaxWidth().height(180.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val bottomPadding = 30f
                    val chartH = h - bottomPadding
                    val count = monthlyData.size
                    val groupWidth = w / count
                    val barWidth = min(groupWidth * 0.32f, 22f)

                    // Base horizontal axis
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.5f),
                        start = Offset(0f, chartH),
                        end = Offset(w, chartH),
                        strokeWidth = 1.5f
                    )

                    monthlyData.forEachIndexed { i, d ->
                        val groupCenterX = i * groupWidth + (groupWidth / 2f)

                        // Income Bar (Green)
                        val incH = ((d.income / maxVal) * (chartH - 20f)).toFloat()
                        val incX = groupCenterX - barWidth - 2f
                        val incY = chartH - incH
                        drawRect(
                            color = FinanceGreen,
                            topLeft = Offset(incX, incY),
                            size = Size(barWidth, incH)
                        )

                        // Expense Bar (Red)
                        val expH = ((d.expense / maxVal) * (chartH - 20f)).toFloat()
                        val expX = groupCenterX + 2f
                        val expY = chartH - expH
                        drawRect(
                            color = FinanceRed,
                            topLeft = Offset(expX, expY),
                            size = Size(barWidth, expH)
                        )
                    }
                }

                // Month labels below bars
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    monthlyData.forEach { d ->
                        Text(
                            text = d.displayLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryDonutChart(
    title: String,
    slices: List<CategorySlice>,
    currency: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (slices.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("لا توجد بيانات مسجلة", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                val total = slices.sumOf { it.amount }
                val fallbackColor = MaterialTheme.colorScheme.primary

                Box(
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(150.dp)) {
                        val strokeWidth = 28f
                        val radius = (size.minDimension - strokeWidth) / 2f
                        val center = Offset(size.width / 2f, size.height / 2f)

                        var startAngle = -90f
                        slices.forEach { slice ->
                            val sweepAngle = (slice.percentage / 100f) * 360f
                            val color = try {
                                Color(android.graphics.Color.parseColor(slice.colorHex))
                            } catch (e: Exception) {
                                fallbackColor
                            }

                            drawArc(
                                color = color,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                topLeft = Offset(center.x - radius, center.y - radius),
                                size = Size(radius * 2, radius * 2),
                                style = Stroke(width = strokeWidth)
                            )
                            startAngle += sweepAngle
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format("%,.0f", total),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = currency,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Legend chips
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    slices.take(6).forEach { slice ->
                        val color = try {
                            Color(android.graphics.Color.parseColor(slice.colorHex))
                        } catch (e: Exception) {
                            MaterialTheme.colorScheme.primary
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${slice.name} (${String.format("%.1f", slice.percentage)}%)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NetWorthLineChart(
    points: List<NetWorthPoint>,
    currency: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "نمو صافي الثروة عبر الزمن",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (points.size < 2) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("بيانات غير كافية لرسم المنحنى", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                val minVal = points.minOf { it.netWorth }
                val maxVal = points.maxOf { it.netWorth }
                val range = if (maxVal - minVal > 0) maxVal - minVal else 1.0

                Canvas(
                    modifier = Modifier.fillMaxWidth().height(160.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val padding = 20f
                    val chartW = w - (padding * 2)
                    val chartH = h - (padding * 2)

                    val path = Path()
                    val coords = points.mapIndexed { i, p ->
                        val x = padding + (i.toFloat() / (points.size - 1)) * chartW
                        val y = padding + chartH - (((p.netWorth - minVal) / range) * chartH).toFloat()
                        Offset(x, y)
                    }

                    // Draw line path
                    coords.forEachIndexed { i, pt ->
                        if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
                    }

                    drawPath(
                        path = path,
                        color = Color(0xFF2563EB),
                        style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                    )

                    // Datapoint circles
                    coords.forEach { pt ->
                        drawCircle(
                            color = Color(0xFF1D4ED8),
                            radius = 4.5f,
                            center = pt
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.5f,
                            center = pt
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    points.forEach { pt ->
                        Text(
                            text = pt.monthLabel,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
