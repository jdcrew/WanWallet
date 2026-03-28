package com.jdcrew.wanwallet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jdcrew.wanwallet.data.model.Budget
import com.jdcrew.wanwallet.data.model.BudgetPeriod
import com.jdcrew.wanwallet.data.model.Category
import com.jdcrew.wanwallet.data.repository.BudgetRepository
import com.jdcrew.wanwallet.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class BudgetUiState(
    val budgets: List<Budget> = emptyList(),
    val budgetWithCategories: List<BudgetWithCategory> = emptyList(),
    val totalBudget: Double = 0.0,
    val totalSpent: Double = 0.0,
    val remaining: Double = 0.0,
    val progress: Float = 0f,
    val isOverBudget: Boolean = false,
    val isLoading: Boolean = false
)

/**
 * 预算与分类的组合数据
 */
data class BudgetWithCategory(
    val budget: Budget,
    val categoryName: String,
    val categoryIcon: String
)

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()
    
    // 分类缓存
    private var categoryCache: Map<Long, Category> = emptyMap()
    
    init {
        loadCategories()
        loadBudgets()
    }
    
    /**
     * 加载分类缓存
     */
    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.allCategories.collect { categories ->
                categoryCache = categories.associateBy { it.id }
                // 分类更新后重新加载预算
                if (_uiState.value.budgets.isNotEmpty()) {
                    loadBudgets()
                }
            }
        }
    }
    
    /**
     * 获取分类名称
     */
    private fun getCategoryName(categoryId: Long): String {
        return if (categoryId == 0L) {
            "总预算"
        } else {
            categoryCache[categoryId]?.name ?: "未分类"
        }
    }
    
    /**
     * 获取分类图标
     */
    private fun getCategoryIcon(categoryId: Long): String {
        return if (categoryId == 0L) {
            "📊"
        } else {
            categoryCache[categoryId]?.icon ?: "📦"
        }
    }
    
    private fun loadBudgets() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            budgetRepository.allBudgets.collect { budgets ->
                val totalBudget = budgets.sumOf { it.amount }
                val totalSpent = budgets.sumOf { it.spent }
                
                // 构建带分类信息的预算列表
                val budgetWithCategories = budgets.map { budget ->
                    BudgetWithCategory(
                        budget = budget,
                        categoryName = getCategoryName(budget.categoryId),
                        categoryIcon = getCategoryIcon(budget.categoryId)
                    )
                }
                
                _uiState.value = BudgetUiState(
                    budgets = budgets,
                    budgetWithCategories = budgetWithCategories,
                    totalBudget = totalBudget,
                    totalSpent = totalSpent,
                    remaining = totalBudget - totalSpent,
                    progress = if (totalBudget > 0) (totalSpent / totalBudget).toFloat() else 0f,
                    isOverBudget = totalSpent > totalBudget,
                    isLoading = false
                )
            }
        }
    }
    
    fun createBudget(amount: Double, period: BudgetPeriod, categoryId: Long = 0) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val startDate = calendar.timeInMillis
            
            when (period) {
                BudgetPeriod.WEEK -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
                BudgetPeriod.MONTH -> calendar.add(Calendar.MONTH, 1)
                BudgetPeriod.YEAR -> calendar.add(Calendar.YEAR, 1)
            }
            
            val budget = Budget(
                amount = amount,
                period = period,
                categoryId = categoryId,
                startDate = startDate,
                endDate = calendar.timeInMillis
            )
            
            budgetRepository.createBudget(budget)
        }
    }
    
    fun updateBudget(budget: Budget) {
        viewModelScope.launch {
            budgetRepository.updateBudget(budget)
        }
    }
    
    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            budgetRepository.deleteBudget(budget)
        }
    }
}
