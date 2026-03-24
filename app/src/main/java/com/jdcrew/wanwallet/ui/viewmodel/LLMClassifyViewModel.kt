package com.jdcrew.wanwallet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jdcrew.wanwallet.data.model.Category
import com.jdcrew.wanwallet.data.model.TransactionType
import com.jdcrew.wanwallet.data.repository.CategoryRepository
import com.jdcrew.wanwallet.data.repository.CategoryRuleEngine
import com.jdcrew.wanwallet.data.repository.MerchantProcessor
import com.jdcrew.wanwallet.ml.BailianLLMClient
import com.jdcrew.wanwallet.ml.TransactionClassification
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LLMClassifyUiState(
    val merchant: String = "",
    val amount: Double = 0.0,
    val llmCategory: String? = null,
    val ruleCategory: String? = null,
    val finalCategory: String? = null,
    val confidence: Double = 0.0,
    val reason: String = "",
    val useLLM: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LLMClassifyViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val ruleEngine: CategoryRuleEngine,
    private val merchantProcessor: MerchantProcessor,
    private val llmClient: BailianLLMClient
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LLMClassifyUiState())
    val uiState: StateFlow<LLMClassifyUiState> = _uiState.asStateFlow()
    
    private var categories: List<Category> = emptyList()
    
    init {
        loadCategories()
    }
    
    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.allCategories.collect { cats ->
                categories = cats
            }
        }
    }
    
    /**
     * 使用混合引擎分类 (规则 + LLM)
     */
    fun classifyWithHybrid(merchant: String, amount: Double, useLLM: Boolean = true) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                merchant = merchant,
                amount = amount,
                useLLM = useLLM,
                isLoading = true,
                error = null
            )
            
            // 1. 规则引擎分类
            val normalizedMerchant = merchantProcessor.normalizeMerchant(merchant)
            val ruleCategory = ruleEngine.matchCategory(normalizedMerchant, categories)
            
            // 2. LLM 分类 (可选)
            var llmClassification: TransactionClassification? = null
            if (useLLM) {
                try {
                    val categoryNames = categories.map { it.name }
                    val result = llmClient.classifyTransaction(
                        merchant = normalizedMerchant,
                        amount = amount,
                        existingCategories = categoryNames
                    )
                    llmClassification = result.getOrNull()
                } catch (e: Exception) {
                    // LLM 失败，降级到规则引擎
                    _uiState.value = _uiState.value.copy(
                        error = "LLM 分类失败，使用规则引擎: ${e.message}",
                        isLoading = false
                    )
                    return@launch
                }
            }
            
            // 3. 决策：选择最终分类
            val finalCategory = decideCategory(ruleCategory, llmClassification)
            
            _uiState.value = LLMClassifyUiState(
                merchant = merchant,
                amount = amount,
                ruleCategory = ruleCategory?.name,
                llmCategory = llmClassification?.category,
                finalCategory = finalCategory?.name,
                confidence = finalCategory?.let { 
                    if (useLLM) llmClassification?.confidence ?: 0.85 else 0.85 
                } ?: 0.5,
                reason = llmClassification?.reason ?: "规则匹配",
                useLLM = useLLM,
                isLoading = false
            )
        }
    }
    
    /**
     * 决策逻辑：选择规则或 LLM 的分类
     */
    private fun decideCategory(
        ruleCategory: Category?,
        llmClassification: TransactionClassification?
    ): Category? {
        // 如果 LLM 置信度高，使用 LLM
        if (llmClassification != null && llmClassification.confidence > 0.9) {
            return categories.find { it.name == llmClassification.category }
        }
        
        // 否则使用规则引擎
        return ruleCategory
    }
    
    /**
     * 用户确认分类 (用于学习)
     */
    fun confirmCategory(selectedCategory: Category) {
        viewModelScope.launch {
            // TODO: 记录用户选择，优化 LLM 提示词
        }
    }
}
