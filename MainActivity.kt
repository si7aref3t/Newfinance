package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AssetEntity
import com.example.data.model.BankAccountEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.GoalEntity
import com.example.data.model.InvestmentEntity
import com.example.data.model.LiabilityEntity
import com.example.data.model.RecurringEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.components.AssetDialog
import com.example.ui.components.BankAccountDialog
import com.example.ui.components.BudgetDialog
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.GoalDialog
import com.example.ui.components.InvestmentDialog
import com.example.ui.components.LiabilityDialog
import com.example.ui.components.TransactionDialog
import com.example.ui.screens.AccountsScreen
import com.example.ui.screens.AssetsLiabilitiesScreen
import com.example.ui.screens.BudgetsGoalsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.InvestmentsScreen
import com.example.ui.screens.PinLockScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FinancialViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: FinancialViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
                val filterType by viewModel.filterType.collectAsStateWithLifecycle()
                val filterAccount by viewModel.filterAccount.collectAsStateWithLifecycle()
                val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()

                // Toast notifications
                LaunchedEffect(Unit) {
                    viewModel.toastMessages.collectLatest { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                }

                // Force Arabic RTL layout direction for natural reading flow
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    if (uiState.isLocked) {
                        PinLockScreen(
                            onUnlock = { pin -> viewModel.unlockWithPin(pin) }
                        )
                    } else {
                        MainAppContent(
                            viewModel = viewModel,
                            uiState = uiState,
                            searchQuery = searchQuery,
                            filterType = filterType,
                            filterAccount = filterAccount,
                            sortOrder = sortOrder
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(
    viewModel: FinancialViewModel,
    uiState: com.example.ui.viewmodel.FinancialUiState,
    searchQuery: String,
    filterType: TransactionType?,
    filterAccount: String?,
    sortOrder: com.example.ui.viewmodel.TxSortOrder
) {
    val context = LocalContext.current
    var currentTab by remember { mutableIntStateOf(0) }

    // Dialog state holders
    var showTxDialog by remember { mutableStateOf(false) }
    var selectedTx by remember { mutableStateOf<TransactionEntity?>(null) }
    var presetTxType by remember { mutableStateOf<TransactionType?>(null) }

    var showBankDialog by remember { mutableStateOf(false) }
    var selectedBank by remember { mutableStateOf<BankAccountEntity?>(null) }

    var showTransferDialog by remember { mutableStateOf(false) }

    var showInvDialog by remember { mutableStateOf(false) }
    var selectedInv by remember { mutableStateOf<InvestmentEntity?>(null) }

    var showAssetDialog by remember { mutableStateOf(false) }
    var selectedAsset by remember { mutableStateOf<AssetEntity?>(null) }

    var showLiabDialog by remember { mutableStateOf(false) }
    var selectedLiab by remember { mutableStateOf<LiabilityEntity?>(null) }

    var showBudgetDialog by remember { mutableStateOf(false) }
    var selectedBudget by remember { mutableStateOf<BudgetEntity?>(null) }

    var showGoalDialog by remember { mutableStateOf(false) }
    var selectedGoal by remember { mutableStateOf<GoalEntity?>(null) }

    var deleteConfirmTx by remember { mutableStateOf<TransactionEntity?>(null) }
    var deleteConfirmBank by remember { mutableStateOf<BankAccountEntity?>(null) }
    var deleteConfirmInv by remember { mutableStateOf<InvestmentEntity?>(null) }
    var deleteConfirmAsset by remember { mutableStateOf<AssetEntity?>(null) }
    var deleteConfirmLiab by remember { mutableStateOf<LiabilityEntity?>(null) }
    var deleteConfirmBudget by remember { mutableStateOf<BudgetEntity?>(null) }
    var deleteConfirmGoal by remember { mutableStateOf<GoalEntity?>(null) }

    // Dialogs
    if (showTxDialog || showTransferDialog) {
        TransactionDialog(
            transaction = selectedTx,
            bankAccounts = uiState.bankAccounts,
            presetType = if (showTransferDialog) TransactionType.TRANSFER else presetTxType,
            onDismiss = {
                showTxDialog = false
                showTransferDialog = false
                selectedTx = null
                presetTxType = null
            },
            onSave = { tx ->
                viewModel.saveTransaction(tx)
                showTxDialog = false
                showTransferDialog = false
                selectedTx = null
                presetTxType = null
            }
        )
    }

    if (showBankDialog) {
        BankAccountDialog(
            account = selectedBank,
            currency = uiState.currency,
            onDismiss = {
                showBankDialog = false
                selectedBank = null
            },
            onSave = { acc ->
                viewModel.saveBankAccount(acc)
                showBankDialog = false
                selectedBank = null
            }
        )
    }

    if (showInvDialog) {
        InvestmentDialog(
            investment = selectedInv,
            onDismiss = {
                showInvDialog = false
                selectedInv = null
            },
            onSave = { inv ->
                viewModel.saveInvestment(inv)
                showInvDialog = false
                selectedInv = null
            }
        )
    }

    if (showAssetDialog) {
        AssetDialog(
            asset = selectedAsset,
            onDismiss = {
                showAssetDialog = false
                selectedAsset = null
            },
            onSave = { asset ->
                viewModel.saveAsset(asset)
                showAssetDialog = false
                selectedAsset = null
            }
        )
    }

    if (showLiabDialog) {
        LiabilityDialog(
            liability = selectedLiab,
            onDismiss = {
                showLiabDialog = false
                selectedLiab = null
            },
            onSave = { liab ->
                viewModel.saveLiability(liab)
                showLiabDialog = false
                selectedLiab = null
            }
        )
    }

    if (showBudgetDialog) {
        BudgetDialog(
            budget = selectedBudget,
            onDismiss = {
                showBudgetDialog = false
                selectedBudget = null
            },
            onSave = { b ->
                viewModel.saveBudget(b)
                showBudgetDialog = false
                selectedBudget = null
            }
        )
    }

    if (showGoalDialog) {
        GoalDialog(
            goal = selectedGoal,
            onDismiss = {
                showGoalDialog = false
                selectedGoal = null
            },
            onSave = { g ->
                viewModel.saveGoal(g)
                showGoalDialog = false
                selectedGoal = null
            }
        )
    }

    // Confirmation delete dialogs
    deleteConfirmTx?.let { tx ->
        ConfirmDeleteDialog(
            title = "حذف المعاملة",
            message = "هل أنت متأكد من حذف هذه المعاملة (${tx.description})؟",
            onDismiss = { deleteConfirmTx = null },
            onConfirm = {
                viewModel.deleteTransaction(tx)
                deleteConfirmTx = null
            }
        )
    }

    deleteConfirmBank?.let { b ->
        ConfirmDeleteDialog(
            title = "حذف الحساب البنكي",
            message = "هل أنت متأكد من حذف حساب ${b.bankName}؟",
            onDismiss = { deleteConfirmBank = null },
            onConfirm = {
                viewModel.deleteBankAccount(b)
                deleteConfirmBank = null
            }
        )
    }

    deleteConfirmInv?.let { inv ->
        ConfirmDeleteDialog(
            title = "حذف الاستثمار",
            message = "هل أنت متأكد من حذف ${inv.name}؟",
            onDismiss = { deleteConfirmInv = null },
            onConfirm = {
                viewModel.deleteInvestment(inv)
                deleteConfirmInv = null
            }
        )
    }

    deleteConfirmAsset?.let { a ->
        ConfirmDeleteDialog(
            title = "حذف الأصل",
            message = "هل أنت متأكد من حذف ${a.name}؟",
            onDismiss = { deleteConfirmAsset = null },
            onConfirm = {
                viewModel.deleteAsset(a)
                deleteConfirmAsset = null
            }
        )
    }

    deleteConfirmLiab?.let { l ->
        ConfirmDeleteDialog(
            title = "حذف الالتزام",
            message = "هل أنت متأكد من حذف ${l.name}؟",
            onDismiss = { deleteConfirmLiab = null },
            onConfirm = {
                viewModel.deleteLiability(l)
                deleteConfirmLiab = null
            }
        )
    }

    deleteConfirmBudget?.let { bg ->
        ConfirmDeleteDialog(
            title = "حذف الميزانية",
            message = "هل أنت متأكد من حذف ميزانية ${bg.category}؟",
            onDismiss = { deleteConfirmBudget = null },
            onConfirm = {
                viewModel.deleteBudget(bg)
                deleteConfirmBudget = null
            }
        )
    }

    deleteConfirmGoal?.let { gl ->
        ConfirmDeleteDialog(
            title = "حذف الهدف المالي",
            message = "هل أنت متأكد من حذف هدف ${gl.name}؟",
            onDismiss = { deleteConfirmGoal = null },
            onConfirm = {
                viewModel.deleteGoal(gl)
                deleteConfirmGoal = null
            }
        )
    }

    val tabTitles = listOf(
        "الرئيسية" to Icons.Default.Dashboard,
        "المعاملات" to Icons.Default.ReceiptLong,
        "الحسابات" to Icons.Default.AccountBalance,
        "الاستثمارات" to Icons.Default.ShowChart,
        "الأصول والديون" to Icons.Default.Home,
        "الميزانيات" to Icons.Default.PieChart,
        "التقارير" to Icons.Default.Assessment,
        "الإعدادات" to Icons.Default.Settings
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.businessName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                // Bottom 5 key tabs
                val bottomItems = listOf(
                    Triple(0, "الرئيسية", Icons.Default.Dashboard),
                    Triple(1, "المعاملات", Icons.Default.ReceiptLong),
                    Triple(2, "الحسابات", Icons.Default.AccountBalance),
                    Triple(5, "الميزانيات", Icons.Default.PieChart),
                    Triple(7, "الإعدادات", Icons.Default.Settings)
                )

                bottomItems.forEach { (index, title, icon) ->
                    NavigationBarItem(
                        selected = currentTab == index,
                        onClick = { currentTab = index },
                        icon = { Icon(icon, contentDescription = title) },
                        label = { Text(title, fontSize = 11.sp, fontWeight = FontWeight.Medium) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Secondary horizontal tab bar to easily access all modules
                ScrollableTabRow(
                    selectedTabIndex = currentTab,
                    edgePadding = 12.dp,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    tabTitles.forEachIndexed { index, (title, icon) ->
                        Tab(
                            selected = currentTab == index,
                            onClick = { currentTab = index },
                            text = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            icon = { Icon(icon, null) }
                        )
                    }
                }

                // Active View
                when (currentTab) {
                    0 -> DashboardScreen(
                        state = uiState,
                        onAddTransaction = { type ->
                            presetTxType = type
                            selectedTx = null
                            showTxDialog = true
                        },
                        onNavigateTab = { tab -> currentTab = tab }
                    )
                    1 -> TransactionsScreen(
                        state = uiState,
                        searchQuery = searchQuery,
                        filterType = filterType,
                        filterAccount = filterAccount,
                        sortOrder = sortOrder,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onFilterTypeChange = { viewModel.setFilterType(it) },
                        onFilterAccountChange = { viewModel.setFilterAccount(it) },
                        onSortChange = { viewModel.setSortOrder(it) },
                        onClearFilters = { viewModel.clearFilters() },
                        onAddTransaction = {
                            selectedTx = null
                            presetTxType = null
                            showTxDialog = true
                        },
                        onEditTransaction = { tx ->
                            selectedTx = tx
                            showTxDialog = true
                        },
                        onDeleteTransaction = { tx ->
                            deleteConfirmTx = tx
                        }
                    )
                    2 -> AccountsScreen(
                        state = uiState,
                        onAddAccount = {
                            selectedBank = null
                            showBankDialog = true
                        },
                        onEditAccount = { b ->
                            selectedBank = b
                            showBankDialog = true
                        },
                        onDeleteAccount = { b ->
                            deleteConfirmBank = b
                        },
                        onTransferFunds = {
                            selectedTx = null
                            showTransferDialog = true
                        }
                    )
                    3 -> InvestmentsScreen(
                        state = uiState,
                        onAddInvestment = {
                            selectedInv = null
                            showInvDialog = true
                        },
                        onEditInvestment = { inv ->
                            selectedInv = inv
                            showInvDialog = true
                        },
                        onDeleteInvestment = { inv ->
                            deleteConfirmInv = inv
                        }
                    )
                    4 -> AssetsLiabilitiesScreen(
                        state = uiState,
                        onAddAsset = {
                            selectedAsset = null
                            showAssetDialog = true
                        },
                        onEditAsset = { a ->
                            selectedAsset = a
                            showAssetDialog = true
                        },
                        onDeleteAsset = { a ->
                            deleteConfirmAsset = a
                        },
                        onAddLiability = {
                            selectedLiab = null
                            showLiabDialog = true
                        },
                        onEditLiability = { l ->
                            selectedLiab = l
                            showLiabDialog = true
                        },
                        onDeleteLiability = { l ->
                            deleteConfirmLiab = l
                        }
                    )
                    5 -> BudgetsGoalsScreen(
                        state = uiState,
                        onAddBudget = {
                            selectedBudget = null
                            showBudgetDialog = true
                        },
                        onEditBudget = { b ->
                            selectedBudget = b
                            showBudgetDialog = true
                        },
                        onDeleteBudget = { b ->
                            deleteConfirmBudget = b
                        },
                        onAddGoal = {
                            selectedGoal = null
                            showGoalDialog = true
                        },
                        onEditGoal = { g ->
                            selectedGoal = g
                            showGoalDialog = true
                        },
                        onDeleteGoal = { g ->
                            deleteConfirmGoal = g
                        }
                    )
                    6 -> ReportsScreen(state = uiState)
                    7 -> SettingsScreen(
                        state = uiState,
                        onUpdateBusinessName = { viewModel.updateBusinessName(it) },
                        onUpdateCurrency = { viewModel.updateCurrency(it) },
                        onUpdatePin = { viewModel.updatePin(it) },
                        onTogglePreventNegative = { viewModel.setPreventNegative(it) },
                        onAddRecurring = {
                            // Quick add standard recurring transaction
                            val newRec = RecurringEntity(
                                type = "EXPENSE",
                                amount = 1000.0,
                                category = "إيجار",
                                description = "إيجار شهري",
                                day = 1
                            )
                            viewModel.saveRecurring(newRec)
                        },
                        onDeleteRecurring = { rec -> viewModel.deleteRecurring(rec) },
                        onExecuteDueRecurring = { viewModel.executeDueRecurring() },
                        onLoadDemoData = { viewModel.loadDemoData() },
                        onClearAllData = { viewModel.clearAllData() },
                        onExportCsv = { viewModel.exportTransactionsCsv() },
                        onShowToast = { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

// Backwards-compatible Greeting function for screenshot and unit tests
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
