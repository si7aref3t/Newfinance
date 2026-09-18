package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssetEntity
import com.example.data.model.BankAccountEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.GoalEntity
import com.example.data.model.InvestmentEntity
import com.example.data.model.LiabilityEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionDialog(
    transaction: TransactionEntity?,
    bankAccounts: List<BankAccountEntity>,
    presetType: TransactionType? = null,
    onDismiss: () -> Unit,
    onSave: (TransactionEntity) -> Unit
) {
    val initialDate = transaction?.date ?: SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    val initialTime = transaction?.time ?: SimpleDateFormat("HH:mm", Locale.US).format(Date())
    val initialType = transaction?.let { TransactionType.fromString(it.type) } ?: presetType ?: TransactionType.EXPENSE

    var type by remember { mutableStateOf(initialType) }
    var amountText by remember { mutableStateOf(transaction?.amount?.toString() ?: "") }
    var dateText by remember { mutableStateOf(initialDate) }
    var timeText by remember { mutableStateOf(initialTime) }
    var categoryText by remember { mutableStateOf(transaction?.category ?: "") }
    var descText by remember { mutableStateOf(transaction?.description ?: "") }
    var notesText by remember { mutableStateOf(transaction?.notes ?: "") }
    var method by remember { mutableStateOf(transaction?.method ?: "cash") } // "cash" or "bank"
    var accountId by remember { mutableStateOf(transaction?.accountId ?: bankAccounts.firstOrNull()?.id) }
    var fromId by remember { mutableStateOf(transaction?.fromId ?: "CASH") }
    var toId by remember { mutableStateOf(transaction?.toId ?: (bankAccounts.firstOrNull()?.id?.toString() ?: "CASH")) }
    var liabilityDir by remember { mutableStateOf(transaction?.dir ?: "pay") }

    var typeExpanded by remember { mutableStateOf(false) }
    var methodExpanded by remember { mutableStateOf(false) }
    var accountExpanded by remember { mutableStateOf(false) }
    var fromExpanded by remember { mutableStateOf(false) }
    var toExpanded by remember { mutableStateOf(false) }
    var liabilityDirExpanded by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val incomeCategories = listOf("راتب", "مبيعات", "خدمات", "استثمار", "أخرى")
    val expenseCategories = listOf(
        "إيجار", "مرافق", "مواصلات", "طعام", "تسوق", "تسويق",
        "رواتب", "شحن", "صيانة", "برامج", "تعليم", "رعاية صحية", "معدات", "أخرى"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (transaction == null) "معاملة جديدة" else "تعديل المعاملة",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Type Selection Dropdown
                Box {
                    OutlinedTextField(
                        value = type.labelAr,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("نوع المعاملة") },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, null, Modifier.clickable { typeExpanded = true })
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        TransactionType.entries.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t.labelAr) },
                                onClick = {
                                    type = t
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("المبلغ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                // Category (if not transfer)
                if (type != TransactionType.TRANSFER) {
                    val cats = if (type == TransactionType.INCOME) incomeCategories else expenseCategories
                    var catExpanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedTextField(
                            value = categoryText,
                            onValueChange = { categoryText = it },
                            label = { Text("الفئة") },
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, null, Modifier.clickable { catExpanded = true })
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = catExpanded,
                            onDismissRequest = { catExpanded = false }
                        ) {
                            cats.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c) },
                                    onClick = {
                                        categoryText = c
                                        catExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Method / Account or Transfer accounts
                if (type == TransactionType.TRANSFER) {
                    // From
                    Box {
                        val fromLabel = if (fromId == "CASH") "💵 نقدية"
                        else bankAccounts.find { it.id.toString() == fromId }?.let { "${it.bankName} (${it.accountName})" } ?: "حساب"

                        OutlinedTextField(
                            value = fromLabel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("تحويل من حساب") },
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, null, Modifier.clickable { fromExpanded = true })
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = fromExpanded,
                            onDismissRequest = { fromExpanded = false }
                        ) {
                            DropdownMenuItem(text = { Text("💵 نقدية") }, onClick = { fromId = "CASH"; fromExpanded = false })
                            bankAccounts.forEach { b ->
                                DropdownMenuItem(
                                    text = { Text("${b.bankName} (${b.accountName})") },
                                    onClick = { fromId = b.id.toString(); fromExpanded = false }
                                )
                            }
                        }
                    }

                    // To
                    Box {
                        val toLabel = if (toId == "CASH") "💵 نقدية"
                        else bankAccounts.find { it.id.toString() == toId }?.let { "${it.bankName} (${it.accountName})" } ?: "حساب"

                        OutlinedTextField(
                            value = toLabel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("تحويل إلى حساب") },
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, null, Modifier.clickable { toExpanded = true })
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = toExpanded,
                            onDismissRequest = { toExpanded = false }
                        ) {
                            DropdownMenuItem(text = { Text("💵 نقدية") }, onClick = { toId = "CASH"; toExpanded = false })
                            bankAccounts.forEach { b ->
                                DropdownMenuItem(
                                    text = { Text("${b.bankName} (${b.accountName})") },
                                    onClick = { toId = b.id.toString(); toExpanded = false }
                                )
                            }
                        }
                    }
                } else {
                    // Payment Method (Cash or Bank)
                    Box {
                        OutlinedTextField(
                            value = if (method == "cash") "💵 نقدية" else "🏧 حساب بنكي",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("طريقة الدفع / الحساب") },
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, null, Modifier.clickable { methodExpanded = true })
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = methodExpanded,
                            onDismissRequest = { methodExpanded = false }
                        ) {
                            DropdownMenuItem(text = { Text("💵 نقدية") }, onClick = { method = "cash"; methodExpanded = false })
                            DropdownMenuItem(text = { Text("🏧 حساب بنكي") }, onClick = { method = "bank"; methodExpanded = false })
                        }
                    }

                    if (method == "bank" && bankAccounts.isNotEmpty()) {
                        Box {
                            val selAcc = bankAccounts.find { it.id == accountId } ?: bankAccounts.first()
                            OutlinedTextField(
                                value = "${selAcc.bankName} (${selAcc.accountName})",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("اختر الحساب البنكي") },
                                trailingIcon = {
                                    Icon(Icons.Default.ArrowDropDown, null, Modifier.clickable { accountExpanded = true })
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            DropdownMenu(
                                expanded = accountExpanded,
                                onDismissRequest = { accountExpanded = false }
                            ) {
                                bankAccounts.forEach { b ->
                                    DropdownMenuItem(
                                        text = { Text("${b.bankName} (${b.accountName})") },
                                        onClick = {
                                            accountId = b.id
                                            accountExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Liability Direction
                if (type == TransactionType.LIABILITY) {
                    Box {
                        OutlinedTextField(
                            value = if (liabilityDir == "pay") "سداد التزام (دفع نقود)" else "اقتراض / سحب تمويل (استلام نقود)",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("نوع حركة الالتزام") },
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, null, Modifier.clickable { liabilityDirExpanded = true })
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = liabilityDirExpanded,
                            onDismissRequest = { liabilityDirExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("سداد التزام (دفع نقود)") },
                                onClick = { liabilityDir = "pay"; liabilityDirExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("اقتراض / سحب تمويل (استلام نقود)") },
                                onClick = { liabilityDir = "fund"; liabilityDirExpanded = false }
                            )
                        }
                    }
                }

                // Description
                OutlinedTextField(
                    value = descText,
                    onValueChange = { descText = it },
                    label = { Text("الوصف") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Date & Time
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dateText,
                        onValueChange = { dateText = it },
                        label = { Text("التاريخ (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = timeText,
                        onValueChange = { timeText = it },
                        label = { Text("الوقت (HH:mm)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Notes
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("ملاحظات إضافية") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull()
                    if (amt == null || amt <= 0) {
                        errorMessage = "يرجى إدخال مبلغ صحيح أكبر من الصفر"
                        return@Button
                    }
                    if (descText.isBlank()) {
                        errorMessage = "يرجى كتابة وصف للمعاملة"
                        return@Button
                    }
                    if (type == TransactionType.TRANSFER && fromId == toId) {
                        errorMessage = "لا يمكن التحويل لنفس الحساب"
                        return@Button
                    }

                    val updated = TransactionEntity(
                        id = transaction?.id ?: 0L,
                        type = type.name,
                        amount = amt,
                        date = dateText.ifBlank { initialDate },
                        time = timeText.ifBlank { initialTime },
                        category = categoryText.trim(),
                        description = descText.trim(),
                        notes = notesText.trim(),
                        method = if (type == TransactionType.TRANSFER) "transfer" else method,
                        accountId = if (method == "bank" && type != TransactionType.TRANSFER) accountId else null,
                        fromId = if (type == TransactionType.TRANSFER) fromId else null,
                        toId = if (type == TransactionType.TRANSFER) toId else null,
                        dir = if (type == TransactionType.LIABILITY) liabilityDir else null,
                        invId = transaction?.invId
                    )
                    onSave(updated)
                }
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun BankAccountDialog(
    account: BankAccountEntity?,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (BankAccountEntity) -> Unit
) {
    var bankName by remember { mutableStateOf(account?.bankName ?: "") }
    var accountName by remember { mutableStateOf(account?.accountName ?: "") }
    var last4 by remember { mutableStateOf(account?.last4 ?: "") }
    var openingText by remember { mutableStateOf(account?.opening?.toString() ?: "0.0") }
    var notes by remember { mutableStateOf(account?.notes ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (account == null) "إضافة حساب بنكي" else "تعديل الحساب البنكي", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("اسم البنك") },
                    placeholder = { Text("مثال: البنك الأهلي") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = accountName,
                    onValueChange = { accountName = it },
                    label = { Text("اسم الحساب / الغرض") },
                    placeholder = { Text("مثال: الحساب الجاري أو التشغيلي") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = last4,
                    onValueChange = { if (it.length <= 4) last4 = it },
                    label = { Text("آخر 4 أرقام فقط") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("1234") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = openingText,
                    onValueChange = { openingText = it },
                    label = { Text("الرصيد الافتتاحي") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (bankName.isBlank() || accountName.isBlank()) {
                        error = "يرجى ملء اسم البنك واسم الحساب"
                        return@Button
                    }
                    val opening = openingText.toDoubleOrNull() ?: 0.0
                    onSave(
                        BankAccountEntity(
                            id = account?.id ?: 0L,
                            bankName = bankName.trim(),
                            accountName = accountName.trim(),
                            last4 = last4.trim(),
                            opening = opening,
                            currency = currency,
                            notes = notes.trim()
                        )
                    )
                }
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Composable
fun InvestmentDialog(
    investment: InvestmentEntity?,
    onDismiss: () -> Unit,
    onSave: (InvestmentEntity) -> Unit
) {
    var name by remember { mutableStateOf(investment?.name ?: "") }
    var type by remember { mutableStateOf(investment?.type ?: "صناديق") }
    var amountText by remember { mutableStateOf(investment?.amount?.toString() ?: "") }
    var valueText by remember { mutableStateOf(investment?.currentValue?.toString() ?: "") }
    var status by remember { mutableStateOf(investment?.status ?: "نشط") }
    var notes by remember { mutableStateOf(investment?.notes ?: "") }
    var date by remember {
        mutableStateOf(investment?.date ?: SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()))
    }
    var error by remember { mutableStateOf<String?>(null) }

    val types = listOf("أسهم", "صناديق", "عملات رقمية", "عقار", "أخرى")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (investment == null) "استثمار جديد" else "تعديل الاستثمار", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الاستثمار") },
                    placeholder = { Text("مثال: صندوق الذهب، أسهم شركة") },
                    modifier = Modifier.fillMaxWidth()
                )

                var typeExpanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("نوع الاستثمار") },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, null, Modifier.clickable { typeExpanded = true })
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        types.forEach { t ->
                            DropdownMenuItem(text = { Text(t) }, onClick = { type = t; typeExpanded = false })
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("المبلغ المستثمَر (التكلفة)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = valueText,
                    onValueChange = { valueText = it },
                    label = { Text("القيمة السوقية الحالية") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("التاريخ (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull()
                    val curVal = valueText.toDoubleOrNull()
                    if (name.isBlank() || amt == null || amt <= 0 || curVal == null || curVal < 0) {
                        error = "يرجى التأكد من ملء الاسم والمبالغ بشكل صحيح"
                        return@Button
                    }
                    onSave(
                        InvestmentEntity(
                            id = investment?.id ?: 0L,
                            name = name.trim(),
                            type = type,
                            amount = amt,
                            currentValue = curVal,
                            date = date,
                            status = status,
                            notes = notes.trim()
                        )
                    )
                }
            ) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
fun AssetDialog(
    asset: AssetEntity?,
    onDismiss: () -> Unit,
    onSave: (AssetEntity) -> Unit
) {
    var name by remember { mutableStateOf(asset?.name ?: "") }
    var category by remember { mutableStateOf(asset?.category ?: "معدات") }
    var purchasePriceText by remember { mutableStateOf(asset?.purchasePrice?.toString() ?: "") }
    var valueText by remember { mutableStateOf(asset?.currentValue?.toString() ?: "") }
    var date by remember { mutableStateOf(asset?.date ?: SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())) }
    var notes by remember { mutableStateOf(asset?.notes ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (asset == null) "أصل جديد" else "تعديل الأصل", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("اسم الأصل") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("الفئة (مثال: عقارات، معدات)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = purchasePriceText, onValueChange = { purchasePriceText = it }, label = { Text("سعر الشراء") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = valueText, onValueChange = { valueText = it }, label = { Text("القيمة الحالية") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("تاريخ الشراء") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("ملاحظات") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = purchasePriceText.toDoubleOrNull() ?: 0.0
                    val curVal = valueText.toDoubleOrNull()
                    if (name.isBlank() || curVal == null || curVal < 0) {
                        error = "يرجى إدخال اسم الأصل والقيمة الحالية"
                        return@Button
                    }
                    onSave(
                        AssetEntity(
                            id = asset?.id ?: 0L,
                            name = name.trim(),
                            category = category.trim(),
                            purchasePrice = price,
                            currentValue = curVal,
                            date = date,
                            notes = notes.trim()
                        )
                    )
                }
            ) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
fun LiabilityDialog(
    liability: LiabilityEntity?,
    onDismiss: () -> Unit,
    onSave: (LiabilityEntity) -> Unit
) {
    var name by remember { mutableStateOf(liability?.name ?: "") }
    var creditor by remember { mutableStateOf(liability?.creditor ?: "") }
    var originalText by remember { mutableStateOf(liability?.original?.toString() ?: "") }
    var remainingText by remember { mutableStateOf(liability?.remaining?.toString() ?: "") }
    var dueDate by remember { mutableStateOf(liability?.dueDate ?: "") }
    var status by remember { mutableStateOf(liability?.status ?: "جاري") }
    var notes by remember { mutableStateOf(liability?.notes ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (liability == null) "التزام جديد" else "تعديل الالتزام", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("اسم الالتزام / القرض") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = creditor, onValueChange = { creditor = it }, label = { Text("الدائن / الجهة المقرضة") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = originalText, onValueChange = { originalText = it }, label = { Text("المبلغ الأصلي") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = remainingText, onValueChange = { remainingText = it }, label = { Text("المتبقي للسداد") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = dueDate, onValueChange = { dueDate = it }, label = { Text("تاريخ الاستحقاق (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("ملاحظات") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val orig = originalText.toDoubleOrNull()
                    val rem = remainingText.toDoubleOrNull() ?: orig
                    if (name.isBlank() || orig == null || orig <= 0 || rem == null) {
                        error = "يرجى ملء الاسم والمبالغ بشكل صحيح"
                        return@Button
                    }
                    onSave(
                        LiabilityEntity(
                            id = liability?.id ?: 0L,
                            name = name.trim(),
                            creditor = creditor.trim(),
                            original = orig,
                            remaining = rem,
                            dueDate = dueDate.trim(),
                            status = if (rem <= 0) "مسدد" else status,
                            notes = notes.trim()
                        )
                    )
                }
            ) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
fun BudgetDialog(
    budget: BudgetEntity?,
    onDismiss: () -> Unit,
    onSave: (BudgetEntity) -> Unit
) {
    val currentMonth = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
    var month by remember { mutableStateOf(budget?.month ?: currentMonth) }
    var category by remember { mutableStateOf(budget?.category ?: "طعام") }
    var limitText by remember { mutableStateOf(budget?.limit?.toString() ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    val expenseCategories = listOf(
        "إيجار", "مرافق", "مواصلات", "طعام", "تسوق", "تسويق",
        "رواتب", "شحن", "صيانة", "برامج", "تعليم", "رعاية صحية", "معدات", "أخرى"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (budget == null) "ميزانية جديدة" else "تعديل الميزانية", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
                OutlinedTextField(value = month, onValueChange = { month = it }, label = { Text("الشهر (YYYY-MM)") }, modifier = Modifier.fillMaxWidth())

                var catExpanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("الفئة") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, Modifier.clickable { catExpanded = true }) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                        expenseCategories.forEach { c ->
                            DropdownMenuItem(text = { Text(c) }, onClick = { category = c; catExpanded = false })
                        }
                    }
                }

                OutlinedTextField(
                    value = limitText,
                    onValueChange = { limitText = it },
                    label = { Text("سقف الميزانية الشهري") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limit = limitText.toDoubleOrNull()
                    if (limit == null || limit <= 0) {
                        error = "يرجى تحديد حد ميزانية صحيح"
                        return@Button
                    }
                    onSave(BudgetEntity(id = budget?.id ?: 0L, month = month.trim(), category = category, limit = limit))
                }
            ) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
fun GoalDialog(
    goal: GoalEntity?,
    onDismiss: () -> Unit,
    onSave: (GoalEntity) -> Unit
) {
    var name by remember { mutableStateOf(goal?.name ?: "") }
    var targetText by remember { mutableStateOf(goal?.target?.toString() ?: "") }
    var currentText by remember { mutableStateOf(goal?.current?.toString() ?: "0.0") }
    var deadline by remember { mutableStateOf(goal?.deadline ?: "") }
    var notes by remember { mutableStateOf(goal?.notes ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (goal == null) "هدف مالي جديد" else "تعديل الهدف المالي", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("اسم الهدف") }, placeholder = { Text("مثال: شراء سيارة، ادخار طوارئ") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = targetText, onValueChange = { targetText = it }, label = { Text("المبلغ المستهدف") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = currentText, onValueChange = { currentText = it }, label = { Text("المبلغ المدخر حالياً") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = deadline, onValueChange = { deadline = it }, label = { Text("الموعد النهائي (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("ملاحظات") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = targetText.toDoubleOrNull()
                    val cur = currentText.toDoubleOrNull() ?: 0.0
                    if (name.isBlank() || target == null || target <= 0) {
                        error = "يرجى إدخال اسم وهدف مالي صحيح"
                        return@Button
                    }
                    onSave(
                        GoalEntity(
                            id = goal?.id ?: 0L,
                            name = name.trim(),
                            target = target,
                            current = cur,
                            deadline = deadline.trim(),
                            notes = notes.trim()
                        )
                    )
                }
            ) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("حذف")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
