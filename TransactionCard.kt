package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BankAccountEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.domain.FinancialEngine
import com.example.ui.theme.FinanceGreen
import com.example.ui.theme.FinanceRed

@Composable
fun TransactionCard(
    transaction: TransactionEntity,
    bankAccounts: List<BankAccountEntity>,
    currency: String,
    onEdit: (TransactionEntity) -> Unit,
    onDelete: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val txType = TransactionType.fromString(transaction.type)

    val (badgeColor, isNegative, prefix) = when (txType) {
        TransactionType.CAPITAL -> Triple(Color(0xFF2563EB), false, "+")
        TransactionType.INCOME -> Triple(FinanceGreen, false, "+")
        TransactionType.EXPENSE -> Triple(FinanceRed, true, "−")
        TransactionType.INVESTMENT -> Triple(Color(0xFF7C3AED), true, "−")
        TransactionType.TRANSFER -> Triple(Color(0xFF0F766E), false, "⇄")
        TransactionType.ASSET -> Triple(Color(0xFF0284C7), true, "−")
        TransactionType.LIABILITY -> {
            if (transaction.dir == "pay") {
                Triple(Color(0xFFD97706), true, "−")
            } else {
                Triple(Color(0xFFD97706), false, "+")
            }
        }
    }

    val accountName = when {
        txType == TransactionType.TRANSFER -> {
            val fromName = if (transaction.fromId == "CASH" || transaction.fromId == null) "نقدية"
            else bankAccounts.find { it.id.toString() == transaction.fromId }?.let { "${it.bankName} (${it.accountName})" } ?: "حساب"

            val toName = if (transaction.toId == "CASH" || transaction.toId == null) "نقدية"
            else bankAccounts.find { it.id.toString() == transaction.toId }?.let { "${it.bankName} (${it.accountName})" } ?: "حساب"

            "$fromName ← $toName"
        }
        transaction.method == "bank" -> {
            bankAccounts.find { it.id == transaction.accountId }?.let { "${it.bankName} (${it.accountName})" } ?: "حساب بنكي"
        }
        else -> "💵 نقدية"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type Pill / Indicator
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeColor.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = txType.labelAr,
                    color = badgeColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Main Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description.ifBlank { txType.labelAr },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${transaction.date} ${transaction.time}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (transaction.category.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• ${transaction.category}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = accountName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )

                if (transaction.notes.isNotBlank()) {
                    Text(
                        text = transaction.notes,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Amount
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$prefix ${FinancialEngine.formatCurrency(transaction.amount, currency)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (txType == TransactionType.TRANSFER) Color(0xFF0F766E) else if (isNegative) FinanceRed else FinanceGreen
                )

                Row {
                    IconButton(
                        onClick = { onEdit(transaction) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "تعديل",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { onDelete(transaction) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف",
                            modifier = Modifier.size(16.dp),
                            tint = FinanceRed.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}
