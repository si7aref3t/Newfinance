package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.components.TransactionCard
import com.example.ui.viewmodel.FinancialUiState
import com.example.ui.viewmodel.TxSortOrder

@Composable
fun TransactionsScreen(
    state: FinancialUiState,
    searchQuery: String,
    filterType: TransactionType?,
    filterAccount: String?,
    sortOrder: TxSortOrder,
    onSearchChange: (String) -> Unit,
    onFilterTypeChange: (TransactionType?) -> Unit,
    onFilterAccountChange: (String?) -> Unit,
    onSortChange: (TxSortOrder) -> Unit,
    onClearFilters: () -> Unit,
    onAddTransaction: () -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredList = remember(state.transactions, searchQuery, filterType, filterAccount, sortOrder) {
        var list = state.transactions

        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter {
                it.description.lowercase().contains(q) ||
                it.notes.lowercase().contains(q) ||
                it.category.lowercase().contains(q)
            }
        }

        if (filterType != null) {
            list = list.filter { it.type.equals(filterType.name, ignoreCase = true) }
        }

        if (filterAccount != null) {
            list = list.filter {
                if (filterAccount == "CASH") {
                    it.method == "cash" || it.fromId == "CASH" || it.toId == "CASH"
                } else {
                    it.accountId?.toString() == filterAccount || it.fromId == filterAccount || it.toId == filterAccount
                }
            }
        }

        when (sortOrder) {
            TxSortOrder.DATE_DESC -> list.sortedWith(compareByDescending<TransactionEntity> { it.date }.thenByDescending { it.time })
            TxSortOrder.DATE_ASC -> list.sortedWith(compareBy<TransactionEntity> { it.date }.thenBy { it.time })
            TxSortOrder.AMOUNT_DESC -> list.sortedByDescending { it.amount }
            TxSortOrder.AMOUNT_ASC -> list.sortedBy { it.amount }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransaction,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "معاملة جديدة")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("بحث في الوصف، الفئة، أو الملاحظات...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "مسح")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Type Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = filterType == null,
                    onClick = { onFilterTypeChange(null) },
                    label = { Text("الكل") }
                )
                TransactionType.entries.forEach { t ->
                    FilterChip(
                        selected = filterType == t,
                        onClick = { onFilterTypeChange(if (filterType == t) null else t) },
                        label = { Text(t.labelAr) }
                    )
                }
            }

            // Accounts Filter Row
            if (state.bankAccounts.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filterAccount == "CASH",
                        onClick = { onFilterAccountChange(if (filterAccount == "CASH") null else "CASH") },
                        label = { Text("💵 نقدية") }
                    )
                    state.bankAccounts.forEach { b ->
                        FilterChip(
                            selected = filterAccount == b.id.toString(),
                            onClick = { onFilterAccountChange(if (filterAccount == b.id.toString()) null else b.id.toString()) },
                            label = { Text("🏧 ${b.bankName}") }
                        )
                    }
                }
            }

            // Results count and sort info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "عدد المعاملات: ${filteredList.size}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(
                        selected = sortOrder == TxSortOrder.AMOUNT_DESC,
                        onClick = {
                            onSortChange(if (sortOrder == TxSortOrder.AMOUNT_DESC) TxSortOrder.DATE_DESC else TxSortOrder.AMOUNT_DESC)
                        },
                        label = { Text(if (sortOrder == TxSortOrder.AMOUNT_DESC) "الأعلى مبلغاً" else "الأحدث أولاً", fontSize = 11.sp) }
                    )
                }
            }

            // List or Empty View
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(54.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لا توجد معاملات مطابقة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "أضف معاملة جديدة أو أعد ضبط معايير البحث",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onAddTransaction) {
                            Text("＋ إضافة معاملة الآن")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList, key = { it.id }) { tx ->
                        TransactionCard(
                            transaction = tx,
                            bankAccounts = state.bankAccounts,
                            currency = state.currency,
                            onEdit = onEditTransaction,
                            onDelete = onDeleteTransaction
                        )
                    }
                }
            }
        }
    }
}
