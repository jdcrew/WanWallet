package com.jdcrew.wanwallet.data.repository

import com.jdcrew.wanwallet.data.model.Category
import com.jdcrew.wanwallet.data.model.Transaction
import com.jdcrew.wanwallet.data.model.TransactionType
import org.junit.Assert.*
import org.junit.Test

/**
 * Repository 层测试
 */
class RepositoryTest {
    
    @Test
    fun transactionCreation() {
        val transaction = Transaction(
            amount = -100.0,
            type = TransactionType.EXPENSE,
            categoryId = 1,
            merchant = "Test Merchant",
            time = System.currentTimeMillis(),
            channel = com.jdcrew.wanwallet.data.model.PaymentChannel.WECHAT,
            isAuto = false
        )
        
        assertEquals(-100.0, transaction.amount, 0.01)
        assertEquals(TransactionType.EXPENSE, transaction.type)
        assertEquals(1, transaction.categoryId)
        assertEquals("Test Merchant", transaction.merchant)
        assertFalse(transaction.isAuto)
    }
    
    @Test
    fun categoryCreation() {
        val category = Category(
            name = "餐饮",
            type = TransactionType.EXPENSE,
            icon = "🍜",
            keywords = "餐厅，饭店，外卖"
        )
        
        assertEquals("餐饮", category.name)
        assertEquals(TransactionType.EXPENSE, category.type)
        assertEquals("🍜", category.icon)
        assertEquals("餐厅，饭店，外卖", category.keywords)
        assertTrue(category.isEnabled)
    }
    
    @Test
    fun budgetCreation() {
        val budget = com.jdcrew.wanwallet.data.model.Budget(
            categoryId = 0,
            amount = 5000.0,
            period = com.jdcrew.wanwallet.data.model.BudgetPeriod.MONTH,
            startDate = System.currentTimeMillis(),
            endDate = System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000
        )
        
        assertEquals(0, budget.categoryId)
        assertEquals(5000.0, budget.amount, 0.01)
        assertEquals(com.jdcrew.wanwallet.data.model.BudgetPeriod.MONTH, budget.period)
        assertTrue(budget.isEnabled)
    }
}
