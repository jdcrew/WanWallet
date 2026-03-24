package com.jdcrew.wanwallet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jdcrew.wanwallet.data.model.Category
import com.jdcrew.wanwallet.data.model.TransactionType
import com.jdcrew.wanwallet.data.repository.CategoryRepository
import com.jdcrew.wanwallet.data.repository.CategoryRuleEngine
import com.jdcrew.wanwallet.data.repository.MerchantProcessor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AutoClassifyState(
    val merchant: String = "",
    val amount: Double = 0.0,
    val predictedCategory: Category? = null,
    val confidence: Double = 0.0,
    val allCategories: List<Category> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class AutoClassifyViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val ruleEngine: CategoryRuleEngine,
    private val merchantProcessor: MerchantProcessor
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AutoClassifyState())
    val uiState: StateFlow<AutoClassifyState> = _uiState.asStateFlow()
    
    init {
        loadCategories()
    }
    
    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.allCategories.collect { categories ->
                _uiState.value = _uiState.value.copy(
                    allCategories = categories
                )
            }
        }
    }
    
    /**
     * 预测交易分类
     */
    fun predictCategory(merchant: String, amount: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                merchant = merchant,
                amount = amount,
                isLoading = true
            )
            
            // 处理商户名称
            val normalizedMerchant = merchantProcessor.normalizeMerchant(merchant)
            
            // 使用规则引擎匹配分类
            val categories = _uiState.value.allCategories
            val matchedCategory = ruleEngine.matchCategory(normalizedMerchant, categories)
            
            // 计算置信度 (简化版本)
            val confidence = if (matchedCategory != null) {
                0.85 // 规则匹配的置信度
            } else {
                0.5 // 默认分类的置信度
            }
            
            _uiState.value = AutoClassifyState(
                merchant = merchant,
                amount = amount,
                predictedCategory = matchedCategory,
                confidence = confidence,
                allCategories = categories,
                isLoading = false
            )
        }
    }
    
    /**
     * 用户确认分类 (用于学习)
     */
    fun confirmCategory(selectedCategory: Category) {
        viewModelScope.launch {
            // TODO: 记录用户选择，用于优化规则
            // 保存用户反馈到学习数据库
        }
    }
    
    /**
     * 用户修改分类 (用于学习)
     */
    fun updateCategory(originalCategory: Category?, newCategory: Category) {
        viewModelScope.launch {
            if (originalCategory != null && originalCategory != newCategory) {
                // TODO: 记录用户修正，优化规则
                // 添加新的匹配规则
            }
        }
    }
}
