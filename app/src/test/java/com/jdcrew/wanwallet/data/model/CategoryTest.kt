package com.jdcrew.wanwallet.data.model

import org.junit.Assert.*
import org.junit.Test

class CategoryTest {
    
    @Test
    fun categoryCreation_defaultValues() {
        val category = Category(
            name = "餐饮",
            type = TransactionType.EXPENSE,
            icon = "🍜"
        )
        
        assertEquals("餐饮", category.name)
        assertEquals(TransactionType.EXPENSE, category.type)
        assertEquals("🍜", category.icon)
        assertEquals("", category.keywords)
        assertEquals(0, category.parentId)
        assertEquals(0, category.order)
        assertTrue(category.isEnabled)
    }
    
    @Test
    fun categoryWithKeywords() {
        val category = Category(
            name = "交通",
            type = TransactionType.EXPENSE,
            icon = "🚗",
            keywords = "地铁，公交，打车"
        )
        
        assertEquals("地铁，公交，打车", category.keywords)
    }
    
    @Test
    fun presetCategories_expenseNotEmpty() {
        assertTrue(PresetCategories.EXPENSE_CATEGORIES.isNotEmpty())
        assertTrue(PresetCategories.EXPENSE_CATEGORIES.size >= 5)
    }
    
    @Test
    fun presetCategories_incomeNotEmpty() {
        assertTrue(PresetCategories.INCOME_CATEGORIES.isNotEmpty())
        assertTrue(PresetCategories.INCOME_CATEGORIES.size >= 3)
    }
    
    @Test
    fun presetCategories_haveFoodCategory() {
        val foodCategory = PresetCategories.EXPENSE_CATEGORIES.find { it.name == "餐饮" }
        assertNotNull(foodCategory)
        assertEquals("🍜", foodCategory?.icon)
    }
}
