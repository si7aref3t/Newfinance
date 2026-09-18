package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AssetEntity
import com.example.data.model.BankAccountEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.GoalEntity
import com.example.data.model.InvestmentEntity
import com.example.data.model.LiabilityEntity
import com.example.data.model.RecurringEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.repository.FinancialRepository
import com.example.domain.FinancialEngine
import com.example.domain.FinancialOverview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TxSortOrder {
    DATE_DESC,
    DATE_ASC,
    AMOUNT_DESC,
    AMOUNT_ASC
}

data class FinancialUiState(
    val transactions: List<TransactionEntity> = emptyList(),
    val bankAccounts: List<BankAccountEntity> = emptyList(),
    val investments: List<InvestmentEntity> = emptyList(),
    val assets: List<AssetEntity> = emptyList(),
    val liabilities: List<LiabilityEntity> = emptyList(),
    val budgets: List<BudgetEntity> = emptyList(),
    val goals: List<GoalEntity> = emptyList(),
    val recurring: List<RecurringEntity> = emptyList(),
    val businessName: String = "المدير المالي",
    val currency: String = "EGP",
    val pin: String = "",
    val preventNegative: Boolean = true,
    val isLocked: Boolean = false,
    val overview: FinancialOverview = FinancialOverview()
)

class FinancialViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinancialRepository
    init {
        val db = AppDatabase.getInstance(application)
        repository = FinancialRepository(db.financialDao())
    }

    // Filter states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _filterType = MutableStateFlow<TransactionType?>(null)
    val filterType = _filterType.asStateFlow()

    private val _filterCategory = MutableStateFlow<String?>(null)
    val filterCategory = _filterCategory.asStateFlow()

    private val _filterAccount = MutableStateFlow<String?>(null)
    val filterAccount = _filterAccount.asStateFlow()

    private val _sortOrder = MutableStateFlow(TxSortOrder.DATE_DESC)
    val sortOrder = _sortOrder.asStateFlow()

    private val _isUnlocked = MutableStateFlow(false)

    // User feedback messages (Snackbar / Toast)
    private val _toastMessages = MutableSharedFlow<String>()
    val toastMessages: SharedFlow<String> = _toastMessages.asSharedFlow()

    private data class CoreFinancialData(
        val transactions: List<TransactionEntity>,
        val bankAccounts: List<BankAccountEntity>,
        val investments: List<InvestmentEntity>,
        val assets: List<AssetEntity>,
        val liabilities: List<LiabilityEntity>,
        val overview: FinancialOverview
    )

    private data class SecondaryFinancialData(
        val budgets: List<BudgetEntity>,
        val goals: List<GoalEntity>,
        val recurring: List<RecurringEntity>,
        val settingsList: List<com.example.data.model.AppSettingEntity>,
        val isUnlocked: Boolean
    )

    private val coreDataFlow = combine(
        repository.transactions,
        repository.bankAccounts,
        repository.investments,
        repository.assets,
        repository.liabilities
    ) { txs, banks, invs, assets, liabs ->
        val overview = FinancialEngine.calculateOverview(txs, banks, invs, assets, liabs)
        CoreFinancialData(txs, banks, invs, assets, liabs, overview)
    }

    private val secondaryDataFlow = combine(
        repository.budgets,
        repository.goals,
        repository.recurring,
        repository.settings,
        _isUnlocked
    ) { budgets, goals, recurring, settingsList, isUnlocked ->
        SecondaryFinancialData(budgets, goals, recurring, settingsList, isUnlocked)
    }

    val uiState: StateFlow<FinancialUiState> = combine(
        coreDataFlow,
        secondaryDataFlow
    ) { core, sec ->
        val settingsMap = sec.settingsList.associate { it.key to it.value }
        val bizName = settingsMap["businessName"] ?: "المدير المالي"
        val curr = settingsMap["currency"] ?: "EGP"
        val savedPin = settingsMap["pin"] ?: ""
        val preventNeg = sec.settingsList.find { it.key == "preventNegative" }?.value != "false"
        val locked = savedPin.isNotBlank() && !sec.isUnlocked

        FinancialUiState(
            transactions = core.transactions,
            bankAccounts = core.bankAccounts,
            investments = core.investments,
            assets = core.assets,
            liabilities = core.liabilities,
            budgets = sec.budgets,
            goals = sec.goals,
            recurring = sec.recurring,
            businessName = bizName,
            currency = curr,
            pin = savedPin,
            preventNegative = preventNeg,
            isLocked = locked,
            overview = core.overview
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FinancialUiState()
    )

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setFilterType(type: TransactionType?) { _filterType.value = type }
    fun setFilterCategory(cat: String?) { _filterCategory.value = cat }
    fun setFilterAccount(acc: String?) { _filterAccount.value = acc }
    fun setSortOrder(order: TxSortOrder) { _sortOrder.value = order }

    fun clearFilters() {
        _searchQuery.value = ""
        _filterType.value = null
        _filterCategory.value = null
        _filterAccount.value = null
        _sortOrder.value = TxSortOrder.DATE_DESC
    }

    fun unlockWithPin(enteredPin: String): Boolean {
        val currentPin = uiState.value.pin
        return if (enteredPin == currentPin) {
            _isUnlocked.value = true
            true
        } else {
            false
        }
    }

    fun lock() {
        _isUnlocked.value = false
    }

    // Transaction actions
    fun saveTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            val state = uiState.value
            val validationError = FinancialEngine.validateNegativeBalance(
                newTx = tx,
                transactions = state.transactions,
                bankAccounts = state.bankAccounts,
                investments = state.investments,
                assets = state.assets,
                liabilities = state.liabilities,
                preventNegative = state.preventNegative
            )

            if (validationError != null) {
                _toastMessages.emit(validationError)
                return@launch
            }

            if (tx.id == 0L) {
                repository.insertTransaction(tx)
                _toastMessages.emit("تمت إضافة المعاملة بنجاح")
            } else {
                repository.updateTransaction(tx)
                _toastMessages.emit("تم تحديث المعاملة بنجاح")
            }
        }
    }

    fun deleteTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(tx)
            _toastMessages.emit("تم حذف المعاملة")
        }
    }

    // Bank Account actions
    fun saveBankAccount(account: BankAccountEntity) {
        viewModelScope.launch {
            if (account.id == 0L) {
                repository.insertBankAccount(account)
                _toastMessages.emit("تمت إضافة الحساب البنكي")
            } else {
                repository.updateBankAccount(account)
                _toastMessages.emit("تم تحديث الحساب البنكي")
            }
        }
    }

    fun deleteBankAccount(account: BankAccountEntity) {
        viewModelScope.launch {
            val isUsed = uiState.value.transactions.any {
                it.accountId == account.id || it.fromId == account.id.toString() || it.toId == account.id.toString()
            }
            if (isUsed) {
                _toastMessages.emit("لا يمكن حذف الحساب — توجد معاملات مسجلة عليه")
                return@launch
            }
            repository.deleteBankAccount(account)
            _toastMessages.emit("تم حذف الحساب البنكي")
        }
    }

    // Investment actions
    fun saveInvestment(inv: InvestmentEntity) {
        viewModelScope.launch {
            if (inv.id == 0L) {
                repository.insertInvestment(inv)
                _toastMessages.emit("تمت إضافة الاستثمار")
            } else {
                repository.updateInvestment(inv)
                _toastMessages.emit("تم تحديث بيانات الاستثمار")
            }
        }
    }

    fun deleteInvestment(inv: InvestmentEntity) {
        viewModelScope.launch {
            repository.deleteInvestment(inv)
            repository.deleteTransactionsByInvId(inv.id)
            _toastMessages.emit("تم حذف الاستثمار")
        }
    }

    // Asset actions
    fun saveAsset(asset: AssetEntity) {
        viewModelScope.launch {
            if (asset.id == 0L) {
                repository.insertAsset(asset)
                _toastMessages.emit("تمت إضافة الأصل بنجاح")
            } else {
                repository.updateAsset(asset)
                _toastMessages.emit("تم تحديث الأصل بنجاح")
            }
        }
    }

    fun deleteAsset(asset: AssetEntity) {
        viewModelScope.launch {
            repository.deleteAsset(asset)
            _toastMessages.emit("تم حذف الأصل")
        }
    }

    // Liability actions
    fun saveLiability(liability: LiabilityEntity) {
        viewModelScope.launch {
            if (liability.id == 0L) {
                repository.insertLiability(liability)
                _toastMessages.emit("تمت إضافة الالتزام بنجاح")
            } else {
                repository.updateLiability(liability)
                _toastMessages.emit("تم تحديث الالتزام بنجاح")
            }
        }
    }

    fun deleteLiability(liability: LiabilityEntity) {
        viewModelScope.launch {
            repository.deleteLiability(liability)
            _toastMessages.emit("تم حذف الالتزام")
        }
    }

    // Budget actions
    fun saveBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            if (budget.id == 0L) {
                repository.insertBudget(budget)
                _toastMessages.emit("تمت إضافة الميزانية")
            } else {
                repository.updateBudget(budget)
                _toastMessages.emit("تم تحديث الميزانية")
            }
        }
    }

    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
            _toastMessages.emit("تم حذف الميزانية")
        }
    }

    // Goal actions
    fun saveGoal(goal: GoalEntity) {
        viewModelScope.launch {
            if (goal.id == 0L) {
                repository.insertGoal(goal)
                _toastMessages.emit("تمت إضافة الهدف المالي")
            } else {
                repository.updateGoal(goal)
                _toastMessages.emit("تم تحديث الهدف المالي")
            }
        }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
            _toastMessages.emit("تم حذف الهدف المالي")
        }
    }

    // Recurring actions
    fun saveRecurring(rec: RecurringEntity) {
        viewModelScope.launch {
            if (rec.id == 0L) {
                repository.insertRecurring(rec)
                _toastMessages.emit("تمت إضافة المعاملة المتكررة")
            } else {
                repository.updateRecurring(rec)
                _toastMessages.emit("تم تحديث المعاملة المتكررة")
            }
        }
    }

    fun deleteRecurring(rec: RecurringEntity) {
        viewModelScope.launch {
            repository.deleteRecurring(rec)
            _toastMessages.emit("تم حذف المعاملة المتكررة")
        }
    }

    fun executeDueRecurring() {
        viewModelScope.launch {
            val currentMonth = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
            val due = uiState.value.recurring.filter { it.lastMonth == null || it.lastMonth < currentMonth }
            if (due.isEmpty()) {
                _toastMessages.emit("لا توجد معاملات متكررة مستحقة لهذا الشهر")
                return@launch
            }

            var count = 0
            for (r in due) {
                val dayStr = String.format("%02d", r.day.coerceIn(1, 28))
                val dateStr = "$currentMonth-$dayStr"
                val tx = TransactionEntity(
                    type = r.type,
                    amount = r.amount,
                    date = dateStr,
                    time = "09:00",
                    category = r.category,
                    description = r.description,
                    method = r.method,
                    accountId = r.accountId,
                    notes = "معاملة متكررة آلية"
                )
                repository.insertTransaction(tx)
                repository.updateRecurring(r.copy(lastMonth = currentMonth))
                count++
            }
            _toastMessages.emit("تم إنشاء $count معاملة متكررة لشهر $currentMonth")
        }
    }

    // Settings actions
    fun updateBusinessName(name: String) {
        viewModelScope.launch {
            repository.setSetting("businessName", name.trim().ifBlank { "المدير المالي" })
            _toastMessages.emit("تم حفظ اسم المشروع")
        }
    }

    fun updateCurrency(currency: String) {
        viewModelScope.launch {
            repository.setSetting("currency", currency.trim().ifBlank { "EGP" })
            _toastMessages.emit("تم تغيير العملة")
        }
    }

    fun updatePin(pin: String) {
        viewModelScope.launch {
            repository.setSetting("pin", pin.trim())
            _toastMessages.emit(if (pin.isBlank()) "تم إلغاء قفل PIN" else "تم تعيين رمز PIN بنجاح")
        }
    }

    fun setPreventNegative(prevent: Boolean) {
        viewModelScope.launch {
            repository.setSetting("preventNegative", prevent.toString())
            _toastMessages.emit(if (prevent) "تم تفعيل منع الرصيد السالب" else "تم إيقاف منع الرصيد السالب")
        }
    }

    fun loadDemoData() {
        viewModelScope.launch {
            repository.loadDemoData()
            _toastMessages.emit("تم تحميل البيانات التجريبية بنجاح")
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _toastMessages.emit("تم مسح جميع البيانات")
        }
    }

    fun exportTransactionsCsv(): String {
        val txs = uiState.value.transactions
        val sb = java.lang.StringBuilder()
        sb.append("ID,Type,Amount,Date,Time,Category,Description,Method,Account,Notes\n")
        for (t in txs) {
            sb.append("${t.id},${t.type},${t.amount},${t.date},${t.time},\"${t.category}\",\"${t.description}\",${t.method},${t.accountId ?: t.fromId ?: ""},\"${t.notes}\"\n")
        }
        return sb.toString()
    }
}
