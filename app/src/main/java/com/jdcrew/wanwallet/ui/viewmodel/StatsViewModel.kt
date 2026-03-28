package com.jdcrew.wanwallet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jdcrew.wanwallet.data.model.Category
import com.jdcrew.wanwallet.data.model.Transaction
import com.jdcrew.wanwallet.data.model.TransactionType
import com.jdcrew.wanwallet.data.repository.CategoryRepository
import com.jdcrew.wanwallet.data.repository.TransactionRepository
import com.jdcrew.wanwallet.ui.components.PieChartSlice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class StatsUiState(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val categoryData: List<PieChartSlice> = emptyList(),
    val dailyTrend: List<DailyTrendItem> = emptyList(),
    val selectedPeriod: StatsPeriod = StatsPeriod.MONTH,
    val isLoading: Boolean = false
)

enum class StatsPeriod {
    WEEK, MONTH, YEAR
}

data class DailyTrendItem(
    val date: String,
    val income: Double,
    val expense: Double
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()
    
    // 分类缓存 (ID -> Name)
    private var categoryCache: Map<Long, String> = emptyMap()
    
    init {
        loadCategories()
        loadStats()
    }
    
    /**
     * 加载分类缓存
     */
    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.allCategories.collect { categories ->
                categoryCache = categories.associate { it.id to it.name }
                // 分类更新后重新加载统计
                if (_uiState.value.categoryData.isNotEmpty()) {
                    loadStats()
                }
            }
        }
    }
    
    /**
     * 根据分类 ID 获取分类名称
     */
    private fun getCategoryName(categoryId: Long): String {
        return categoryCache[categoryId] ?: "未分类"
    }
    
    fun updatePeriod(period: StatsPeriod) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
        loadStats()
    }
    
    private fun loadStats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val now = System.currentTimeMillis()
            val calendar = Calendar.getInstance()
            calendar.time = Date(now)
            
            val startTime = when (_uiState.value.selectedPeriod) {
                StatsPeriod.WEEK -> {
                    calendar.add(Calendar.DAY_OF_WEEK, -7)
                    calendar.timeInMillis
                }
                StatsPeriod.MONTH -> {
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.timeInMillis
                }
                StatsPeriod.YEAR -> {
                    calendar.set(Calendar.MONTH, Calendar.JANUARY)
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.timeInMillis
                }
            }
            
            val transactions = transactionRepository.getTransactionsByTimeRange(startTime, now)
                .firstOrNull() ?: emptyList()
            
            // 计算总额
            val income = transactions
                .filter { it.type == TransactionType.INCOME }
                .sumOf { it.amount }
            
            val expense = transactions
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { kotlin.math.abs(it.amount) }
            
            // 分类统计
            val categoryMap = mutableMapOf<String, Double>()
            transactions.filter { it.type == TransactionType.EXPENSE }
                .forEach { transaction ->
                    val categoryName = getCategoryName(transaction.categoryId)
                    categoryMap[categoryName] = (categoryMap[categoryName] ?: 0.0) + kotlin.math.abs(transaction.amount)
                }
            
            val categoryData = categoryMap.map { (category, amount) ->
                PieChartSlice(
                    label = category,
                    value = amount,
                    color = getRandomColor(category.hashCode())
                )
            }.sortedByDescending { it.value }
            
            // 每日趋势 (简化版)
            val dailyTrend = generateDailyTrend(transactions, startTime, now)
            
            _uiState.value = StatsUiState(
                totalIncome = income,
                totalExpense = expense,
                balance = income - expense,
                categoryData = categoryData,
                dailyTrend = dailyTrend,
                selectedPeriod = _uiState.value.selectedPeriod,
                isLoading = false
            )
        }
    }
    
    private fun generateDailyTrend(
        transactions: List<Transaction>,
        startTime: Long,
        endTime: Long
    ): List<DailyTrendItem> {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.CHINA)
        val displayFormat = java.text.SimpleDateFormat("MM/dd", java.util.Locale.CHINA)
        
        // 按日期聚合交易数据
        val dailyMap = mutableMapOf<String, Pair<Double, Double>>() // date -> (income, expense)
        
        transactions.forEach { transaction ->
            val dateKey = dateFormat.format(Date(transaction.time))
            val (currentIncome, currentExpense) = dailyMap[dateKey] ?: (0.0 to 0.0)
            
            val newIncome = if (transaction.type == TransactionType.INCOME) {
                currentIncome + transaction.amount
            } else {
                currentIncome
            }
            
            val newExpense = if (transaction.type == TransactionType.EXPENSE) {
                currentExpense + kotlin.math.abs(transaction.amount)
            } else {
                currentExpense
            }
            
            dailyMap[dateKey] = newIncome to newExpense
        }
        
        // 生成完整的日期序列 (填充缺失的日期)
        val result = mutableListOf<DailyTrendItem>()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = startTime
        
        while (calendar.timeInMillis <= endTime) {
            val dateKey = dateFormat.format(calendar.time)
            val displayDate = displayFormat.format(calendar.time)
            val (income, expense) = dailyMap[dateKey] ?: (0.0 to 0.0)
            
            result.add(
                DailyTrendItem(
                    date = displayDate,
                    income = income,
                    expense = expense
                )
            )
            
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        return result
    }
    
    private fun getRandomColor(seed: Int): Color {
        val colors = listOf(
            Color(0xFF4CAF50),
            Color(0xFF2196F3),
            Color(0xFFFFC107),
            Color(0xFFE91E63),
            Color(0xFF9C27B0),
            Color(0xFFFF5722),
            Color(0xFF00BCD4),
            Color(0xFF795548)
        )
        return colors[kotlin.math.abs(seed) % colors.size]
    }
}
