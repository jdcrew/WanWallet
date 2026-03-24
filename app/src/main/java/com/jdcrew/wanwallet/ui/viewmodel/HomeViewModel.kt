package com.jdcrew.wanwallet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jdcrew.wanwallet.data.model.Transaction
import com.jdcrew.wanwallet.data.model.TransactionType
import com.jdcrew.wanwallet.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class HomeUiState(
    val totalExpense: Double = 0.0,
    val totalIncome: Double = 0.0,
    val balance: Double = 0.0,
    val recentTransactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadTransactions()
    }
    
    private fun loadTransactions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                transactionRepository.allTransactions.collect { transactions ->
                    val now = System.currentTimeMillis()
                    val calendar = Calendar.getInstance()
                    calendar.time = Date(now)
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    val monthStart = calendar.timeInMillis
                    
                    val monthTransactions = transactions.filter { it.time >= monthStart }
                    
                    val expense = monthTransactions
                        .filter { it.type == TransactionType.EXPENSE }
                        .sumOf { it.amount }
                    
                    val income = monthTransactions
                        .filter { it.type == TransactionType.INCOME }
                        .sumOf { it.amount }
                    
                    _uiState.value = HomeUiState(
                        totalExpense = expense,
                        totalIncome = income,
                        balance = income - expense,
                        recentTransactions = transactions.take(10),
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "加载失败"
                )
            }
        }
    }
}
