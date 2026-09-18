package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RecurringEntity
import com.example.domain.FinancialEngine
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.theme.FinanceRed
import com.example.ui.viewmodel.FinancialUiState

@Composable
fun SettingsScreen(
    state: FinancialUiState,
    onUpdateBusinessName: (String) -> Unit,
    onUpdateCurrency: (String) -> Unit,
    onUpdatePin: (String) -> Unit,
    onTogglePreventNegative: (Boolean) -> Unit,
    onAddRecurring: () -> Unit,
    onDeleteRecurring: (RecurringEntity) -> Unit,
    onExecuteDueRecurring: () -> Unit,
    onLoadDemoData: () -> Unit,
    onClearAllData: () -> Unit,
    onExportCsv: () -> String,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var businessNameInput by remember(state.businessName) { mutableStateOf(state.businessName) }
    var pinInput by remember(state.pin) { mutableStateOf(state.pin) }
    var currencyExpanded by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showDemoConfirm by remember { mutableStateOf(false) }

    val currencies = listOf("EGP", "SAR", "AED", "USD", "EUR", "KWD", "QAR", "JOD", "BHD", "OMR")

    if (showClearConfirm) {
        ConfirmDeleteDialog(
            title = "مسح جميع البيانات",
            message = "تحذير: سيتم حذف جميع المعاملات والحسابات والاستثمارات والأصول والالتزامات بشكل نهائي. هل ترغب في المتابعة؟",
            onDismiss = { showClearConfirm = false },
            onConfirm = {
                showClearConfirm = false
                onClearAllData()
            }
        )
    }

    if (showDemoConfirm) {
        ConfirmDeleteDialog(
            title = "تحميل بيانات تجريبية",
            message = "سيتم استبدال البيانات الحالية بنموذج متكامل من الحسابات والمعاملات التجريبية. هل أنت متأكد؟",
            onDismiss = { showDemoConfirm = false },
            onConfirm = {
                showDemoConfirm = false
                onLoadDemoData()
            }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "الإعدادات العامة وإدارة البيانات",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        // Project / Business Name & Currency
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("بيانات النشاط والعملة", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                    OutlinedTextField(
                        value = businessNameInput,
                        onValueChange = {
                            businessNameInput = it
                            onUpdateBusinessName(it)
                        },
                        label = { Text("اسم النشاط / المحفظة") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Box {
                        OutlinedTextField(
                            value = state.currency,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("العملة الرئيسية") },
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, null, Modifier.clickable { currencyExpanded = true })
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(expanded = currencyExpanded, onDismissRequest = { currencyExpanded = false }) {
                            currencies.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c) },
                                    onClick = {
                                        onUpdateCurrency(c)
                                        currencyExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Prevent Negative Balance Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("منع الرصيد السالب", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                "رفض أي معاملة تؤدي إلى سحب أكثر من الرصيد المتوفر في النقدية أو البنوك",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.preventNegative,
                            onCheckedChange = onTogglePreventNegative
                        )
                    }
                }
            }
        }

        // Privacy & PIN Lock
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("قفل التطبيق برمز PIN محلي", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Text(
                        text = "يمكنك حماية التطبيق برمز PIN لا يفتح إلا بإدخاله. اتركه فارغاً لإلغاء القفل.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            if (it.length <= 8) {
                                pinInput = it
                                onUpdatePin(it)
                            }
                        },
                        label = { Text("رمز PIN (أرقام)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        placeholder = { Text("مثال: 1234") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Recurring Transactions
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("المعاملات المتكررة (${state.recurring.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Button(onClick = onAddRecurring) {
                            Text("＋ إضافة")
                        }
                    }

                    Text(
                        "مثل الإيجارات أو الرواتب أو الاشتراكات التي تدفع شهرياً",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (state.recurring.isNotEmpty()) {
                        Button(
                            onClick = onExecuteDueRecurring,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PlayArrow, null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تنفيذ المعاملات المتكررة المستحقة لهذا الشهر")
                        }

                        state.recurring.forEach { rec ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(rec.description, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text(
                                        "${rec.category} • ${FinancialEngine.formatCurrency(rec.amount, state.currency)} • يوم ${rec.day} شهرياً",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { onDeleteRecurring(rec) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Delete, "حذف", tint = FinanceRed, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Data Management & Backups
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("إدارة البيانات والنسخ الاحتياطي", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                    // Export CSV
                    OutlinedButton(
                        onClick = {
                            val csv = onExportCsv()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Transactions CSV", csv))
                            onShowToast("تم نسخ بيانات المعاملات CSV إلى الحافظة بنجاح")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.FileDownload, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تصدير المعاملات (نسخ CSV إلى الحافظة)")
                    }

                    // Load Demo Data
                    Button(
                        onClick = { showDemoConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Science, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تحميل بيانات تجريبية (Demo Data)")
                    }

                    // Clear All Data
                    Button(
                        onClick = { showClearConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = FinanceRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Delete, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("مسح جميع البيانات والبدء من جديد")
                    }
                }
            }
        }
    }
}
