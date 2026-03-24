package com.jdcrew.wanwallet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jdcrew.wanwallet.data.model.Category
import com.jdcrew.wanwallet.data.model.PaymentChannel
import com.jdcrew.wanwallet.data.model.PresetCategories
import com.jdcrew.wanwallet.data.model.Transaction
import com.jdcrew.wanwallet.data.model.TransactionType
import com.jdcrew.wanwallet.data.repository.CategoryRepository
import com.jdcrew.wanwallet.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddTransactionUiState(
    val amount: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val selectedCategory: Category? = null,
    val merchant: String = "",
    val note: String = "",
    val channel: PaymentChannel = PaymentChannel.WECHAT,
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()
    
    init {
        loadCategories()
    }
    
    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getCategoriesByType(TransactionType.EXPENSE)
                .collect { categories ->
                    _uiState.value = _uiState.value.copy(
                        categories = categories,
                        selectedCategory = categories.firstOrNull()
                    )
                }
        }
    }
    
    fun updateAmount(amount: String) {
        _uiState.value = _uiState.value.copy(amount = amount)
    }
    
    fun updateType(type: TransactionType) {
        _uiState.value = _uiState.value.copy(type = type)
        loadCategoriesForType(type)
    }
    
    private fun loadCategoriesForType(type: TransactionType) {
        viewModelScope.launch {
            categoryRepository.getCategoriesByType(type)
                .collect { categories ->
                    _uiState.value = _uiState.value.copy(
                        categories = categories,
                        selectedCategory = categories.firstOrNull()
                    )
                }
        }
    }
    
    fun updateCategory(category: Category) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }
    
    fun updateMerchant(merchant: String) {
        _uiState.value = _uiState.value.copy(merchant = merchant)
    }
    
    fun updateNote(note: String) {
        _uiState.value = _uiState.value.copy(note = note)
    }
    
    fun updateChannel(channel: PaymentChannel) {
        _uiState.value = _uiState.value.copy(channel = channel)
    }
    
    fun saveTransaction() {
        viewModelScope.launch {
            val state = _uiState.value
            
            // 验证
            val amount = state.amount.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                _uiState.value = _uiState.value.copy(error = "请输入有效金额")
                return@launch
            }
            
            if (state.merchant.isBlank()) {
                _uiState.value = _uiState.value.copy(error = "请输入商户名称")
                return@launch
            }
            
            if (state.selectedCategory == null) {
                _uiState.value = _uiState.value.copy(error = "请选择分类")
                return@launch
            }
            
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val transaction = Transaction(
                    amount = if (state.type == TransactionType.EXPENSE) -amount else amount,
                    type = state.type,
                    categoryId = state.selectedCategory.id,
                    merchant = state.merchant,
                    time = System.currentTimeMillis(),
                    channel = state.channel,
                    note = state.note,
                    isAuto = false
                )
                
                transactionRepository.insert(transaction)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    saveSuccess = true,
                    error = null
                )
                
                // 重置表单
                resetForm()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "保存失败"
                )
            }
        }
    }
    
    private fun resetForm() {
        _uiState.value = AddTransactionUiState(
            categories = _uiState.value.categories,
            selectedCategory = _uiState.value.categories.firstOrNull()
        )
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }
}
