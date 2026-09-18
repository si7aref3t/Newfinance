package com.example.data.repository

import com.example.data.local.FinancialDao
import com.example.data.model.AppSettingEntity
import com.example.data.model.AssetEntity
import com.example.data.model.BankAccountEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.GoalEntity
import com.example.data.model.InvestmentEntity
import com.example.data.model.LiabilityEntity
import com.example.data.model.RecurringEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

class FinancialRepository(private val dao: FinancialDao) {

    val transactions: Flow<List<TransactionEntity>> = dao.getAllTransactions()
    val bankAccounts: Flow<List<BankAccountEntity>> = dao.getAllBankAccounts()
    val investments: Flow<List<InvestmentEntity>> = dao.getAllInvestments()
    val assets: Flow<List<AssetEntity>> = dao.getAllAssets()
    val liabilities: Flow<List<LiabilityEntity>> = dao.getAllLiabilities()
    val budgets: Flow<List<BudgetEntity>> = dao.getAllBudgets()
    val goals: Flow<List<GoalEntity>> = dao.getAllGoals()
    val recurring: Flow<List<RecurringEntity>> = dao.getAllRecurring()
    val settings: Flow<List<AppSettingEntity>> = dao.getAllSettings()

    suspend fun insertTransaction(tx: TransactionEntity): Long = dao.insertTransaction(tx)
    suspend fun updateTransaction(tx: TransactionEntity) = dao.updateTransaction(tx)
    suspend fun deleteTransaction(tx: TransactionEntity) = dao.deleteTransaction(tx)
    suspend fun deleteTransactionById(id: Long) = dao.deleteTransactionById(id)
    suspend fun deleteTransactionsByInvId(invId: Long) = dao.deleteTransactionsByInvId(invId)

    suspend fun insertBankAccount(account: BankAccountEntity): Long = dao.insertBankAccount(account)
    suspend fun updateBankAccount(account: BankAccountEntity) = dao.updateBankAccount(account)
    suspend fun deleteBankAccount(account: BankAccountEntity) = dao.deleteBankAccount(account)

    suspend fun insertInvestment(inv: InvestmentEntity): Long = dao.insertInvestment(inv)
    suspend fun updateInvestment(inv: InvestmentEntity) = dao.updateInvestment(inv)
    suspend fun deleteInvestment(inv: InvestmentEntity) = dao.deleteInvestment(inv)

    suspend fun insertAsset(asset: AssetEntity): Long = dao.insertAsset(asset)
    suspend fun updateAsset(asset: AssetEntity) = dao.updateAsset(asset)
    suspend fun deleteAsset(asset: AssetEntity) = dao.deleteAsset(asset)

    suspend fun insertLiability(liability: LiabilityEntity): Long = dao.insertLiability(liability)
    suspend fun updateLiability(liability: LiabilityEntity) = dao.updateLiability(liability)
    suspend fun deleteLiability(liability: LiabilityEntity) = dao.deleteLiability(liability)

    suspend fun insertBudget(budget: BudgetEntity): Long = dao.insertBudget(budget)
    suspend fun updateBudget(budget: BudgetEntity) = dao.updateBudget(budget)
    suspend fun deleteBudget(budget: BudgetEntity) = dao.deleteBudget(budget)

    suspend fun insertGoal(goal: GoalEntity): Long = dao.insertGoal(goal)
    suspend fun updateGoal(goal: GoalEntity) = dao.updateGoal(goal)
    suspend fun deleteGoal(goal: GoalEntity) = dao.deleteGoal(goal)

    suspend fun insertRecurring(rec: RecurringEntity): Long = dao.insertRecurring(rec)
    suspend fun updateRecurring(rec: RecurringEntity) = dao.updateRecurring(rec)
    suspend fun deleteRecurring(rec: RecurringEntity) = dao.deleteRecurring(rec)

    suspend fun setSetting(key: String, value: String) {
        dao.setSetting(AppSettingEntity(key, value))
    }

    suspend fun getSetting(key: String): String? = dao.getSetting(key)?.value

    suspend fun clearAllData() {
        dao.deleteAllTransactions()
        dao.deleteAllBankAccounts()
        dao.deleteAllInvestments()
        dao.deleteAllAssets()
        dao.deleteAllLiabilities()
        dao.deleteAllBudgets()
        dao.deleteAllGoals()
        dao.deleteAllRecurring()
    }

    suspend fun loadDemoData() {
        clearAllData()

        val b1Id = dao.insertBankAccount(
            BankAccountEntity(
                bankName = "البنك الأهلي المصري",
                accountName = "الحساب الجاري",
                last4 = "4521",
                opening = 50000.0,
                currency = "EGP",
                notes = "الحساب الرئيسي للعمليات"
            )
        )
        val b2Id = dao.insertBankAccount(
            BankAccountEntity(
                bankName = "بنك مصر",
                accountName = "حساب التوفير",
                last4 = "8890",
                opening = 20000.0,
                currency = "EGP",
                notes = "حساب احتياطي واستثماري"
            )
        )

        // Transactions
        dao.insertTransactions(
            listOf(
                TransactionEntity(
                    type = "CAPITAL",
                    amount = 100000.0,
                    date = "2026-01-05",
                    time = "10:00",
                    category = "مساهمة مؤسس",
                    description = "رأس المال الافتتاحي للنشاط",
                    method = "cash"
                ),
                TransactionEntity(
                    type = "EXPENSE",
                    amount = 10000.0,
                    date = "2026-02-01",
                    time = "11:30",
                    category = "إيجار",
                    description = "إيجار المقر الشهري - فبراير",
                    method = "cash"
                ),
                TransactionEntity(
                    type = "INCOME",
                    amount = 25000.0,
                    date = "2026-02-10",
                    time = "14:15",
                    category = "مبيعات",
                    description = "دفعة مبيعات منتجات العميل أ",
                    method = "bank",
                    accountId = b1Id
                ),
                TransactionEntity(
                    type = "INVESTMENT",
                    amount = 30000.0,
                    date = "2026-02-15",
                    time = "12:00",
                    category = "صناديق",
                    description = "استثمار في وثائق صندوق المؤشرات",
                    method = "bank",
                    accountId = b1Id
                ),
                TransactionEntity(
                    type = "TRANSFER",
                    amount = 8000.0,
                    date = "2026-02-20",
                    time = "16:00",
                    category = "",
                    description = "إيداع نقدي من الخزينة للبنك الأهلي",
                    method = "transfer",
                    fromId = "CASH",
                    toId = b1Id.toString()
                ),
                TransactionEntity(
                    type = "EXPENSE",
                    amount = 6500.0,
                    date = "2026-03-02",
                    time = "09:30",
                    category = "رواتب",
                    description = "رواتب الفريق التقني",
                    method = "bank",
                    accountId = b1Id
                ),
                TransactionEntity(
                    type = "INCOME",
                    amount = 18000.0,
                    date = "2026-03-08",
                    time = "15:00",
                    category = "خدمات",
                    description = "عقد استشارات وتطوير سنوي",
                    method = "cash"
                ),
                TransactionEntity(
                    type = "EXPENSE",
                    amount = 2200.0,
                    date = "2026-03-12",
                    time = "13:45",
                    category = "تسويق",
                    description = "حملات إعلانات رقمية ممولة",
                    method = "bank",
                    accountId = b2Id
                ),
                TransactionEntity(
                    type = "EXPENSE",
                    amount = 1800.0,
                    date = "2026-03-18",
                    time = "17:20",
                    category = "برامج",
                    description = "اشتراك خوادم وتطبيقات سحابية",
                    method = "bank",
                    accountId = b2Id
                )
            )
        )

        // Investments
        dao.insertInvestment(
            InvestmentEntity(
                name = "صندوق مؤشرات الأسهم",
                type = "صناديق",
                amount = 30000.0,
                currentValue = 34500.0,
                date = "2026-02-15",
                status = "نشط",
                notes = "عائد تراكمي متوقع 15% سنويًا"
            )
        )

        // Assets
        dao.insertAsset(
            AssetEntity(
                name = "ماكينة إنتاج وتغليف حديثة",
                category = "معدات",
                purchasePrice = 25000.0,
                currentValue = 23000.0,
                date = "2026-01-20",
                notes = "شاملة الصيانة والضمان لمدة عامين"
            )
        )

        // Liabilities
        dao.insertLiability(
            LiabilityEntity(
                name = "قرض تمويل المعدات",
                creditor = "بنك التنمية الصناعية",
                original = 40000.0,
                remaining = 24000.0,
                dueDate = "2027-06-01",
                status = "جاري",
                notes = "قسط شهري منتظم بقيمة 2000 ج.م"
            )
        )

        // Budgets
        dao.insertBudgets(
            listOf(
                BudgetEntity(month = "2026-03", category = "تسويق", limit = 5000.0),
                BudgetEntity(month = "2026-03", category = "برامج", limit = 3000.0),
                BudgetEntity(month = "2026-03", category = "رواتب", limit = 10000.0)
            )
        )

        // Goals
        dao.insertGoal(
            GoalEntity(
                name = "توسيع خط الإنتاج وشراء شاحنة توزيع",
                target = 80000.0,
                current = 28000.0,
                deadline = "2026-12-31",
                notes = "الهدف المالي السنوي للتوسع اللوجستي"
            )
        )

        // Recurring
        dao.insertRecurring(
            RecurringEntity(
                type = "EXPENSE",
                amount = 10000.0,
                category = "إيجار",
                description = "إيجار المقر الإداري الشهري",
                method = "bank",
                accountId = b1Id,
                day = 1,
                lastMonth = "2026-02",
                notes = "دفع منتظم أول كل شهر"
            )
        )
    }
}
