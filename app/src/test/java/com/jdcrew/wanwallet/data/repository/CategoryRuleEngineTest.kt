package com.jdcrew.wanwallet.data.repository

import org.junit.Assert.*
import org.junit.Test

class CategoryRuleEngineTest {
    
    private val ruleEngine = CategoryRuleEngine()
    
    @Test
    fun matchCategory_foodMerchant() {
        val categories = createTestCategories()
        val category = ruleEngine.matchCategory("星巴克", categories)
        assertEquals("餐饮", category?.name)
    }
    
    @Test
    fun matchCategory_transportMerchant() {
        val categories = createTestCategories()
        val category = ruleEngine.matchCategory("滴滴出行", categories)
        assertEquals("交通", category?.name)
    }
    
    @Test
    fun matchCategory_shoppingMerchant() {
        val categories = createTestCategories()
        val category = ruleEngine.matchCategory("淘宝", categories)
        assertEquals("购物", category?.name)
    }
    
    @Test
    fun matchCategory_unknownMerchant() {
        val categories = createTestCategories()
        val category = ruleEngine.matchCategory("未知商户 XYZ", categories)
        assertEquals("其他", category?.name)
    }
    
    @Test
    fun matchCategory_caseInsensitive() {
        val categories = createTestCategories()
        val category = ruleEngine.matchCategory("STARBUCKS", categories)
        assertEquals("餐饮", category?.name)
    }
    
    private fun createTestCategories(): List<Category> {
        return listOf(
            Category(name = "餐饮", type = TransactionType.EXPENSE, icon = "🍜"),
            Category(name = "交通", type = TransactionType.EXPENSE, icon = "🚗"),
            Category(name = "购物", type = TransactionType.EXPENSE, icon = "🛍️"),
            Category(name = "其他", type = TransactionType.EXPENSE, icon = "📦")
        )
    }
}
