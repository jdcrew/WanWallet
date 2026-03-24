package com.jdcrew.wanwallet.ui.viewmodel

import org.junit.Assert.*
import org.junit.Test

/**
 * ViewModel 层测试
 */
class ViewModelTest {
    
    @Test
    fun statsUiState_defaultValues() {
        val state = StatsUiState()
        
        assertEquals(0.0, state.totalIncome, 0.01)
        assertEquals(0.0, state.totalExpense, 0.01)
        assertEquals(0.0, state.balance, 0.01)
        assertTrue(state.categoryData.isEmpty())
        assertEquals(StatsPeriod.MONTH, state.selectedPeriod)
        assertFalse(state.isLoading)
    }
    
    @Test
    fun budgetUiState_defaultValues() {
        val state = BudgetUiState()
        
        assertEquals(0.0, state.totalBudget, 0.01)
        assertEquals(0.0, state.totalSpent, 0.01)
        assertEquals(0.0, state.remaining, 0.01)
        assertEquals(0f, state.progress, 0.01f)
        assertFalse(state.isOverBudget)
        assertTrue(state.budgets.isEmpty())
    }
    
    @Test
    fun addTransactionState_validation() {
        val state = AddTransactionUiState()
        
        assertEquals("", state.amount)
        assertEquals(TransactionType.EXPENSE, state.type)
        assertNull(state.selectedCategory)
        assertEquals("", state.merchant)
        assertEquals("", state.note)
        assertEquals(com.jdcrew.wanwallet.data.model.PaymentChannel.WECHAT, state.channel)
        assertFalse(state.saveSuccess)
    }
}
