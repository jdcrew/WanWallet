package com.jdcrew.wanwallet.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.jdcrew.wanwallet.data.model.Category
import com.jdcrew.wanwallet.data.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class CategoryDaoTest {
    
    private lateinit var categoryDao: CategoryDao
    private lateinit var database: WanWalletDatabase
    
    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, WanWalletDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        categoryDao = database.categoryDao()
    }
    
    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }
    
    @Test
    fun categoryDao_insertAndGetById() = runTest {
        // 给定
        val category = Category(
            name = "餐饮",
            type = TransactionType.EXPENSE,
            icon = "🍜"
        )
        
        // 当
        val id = categoryDao.insert(category)
        val retrieved = categoryDao.getCategoryById(id)
        
        // 则
        assertNotNull(retrieved)
        assertEquals(id, retrieved?.id)
        assertEquals("餐饮", retrieved?.name)
        assertEquals(TransactionType.EXPENSE, retrieved?.type)
        assertEquals("🍜", retrieved?.icon)
    }
    
    @Test
    fun categoryDao_insert_updatesAllCategories() = runTest {
        // 给定
        val category1 = Category(name = "餐饮", type = TransactionType.EXPENSE, icon = "🍜")
        val category2 = Category(name = "交通", type = TransactionType.EXPENSE, icon = "🚗")
        
        // 当
        categoryDao.insert(category1)
        categoryDao.insert(category2)
        
        val allCategories = categoryDao.getAllCategories().first()
        
        // 则
        assertEquals(2, allCategories.size)
        assertTrue(allCategories.any { it.name == "餐饮" })
        assertTrue(allCategories.any { it.name == "交通" })
    }
    
    @Test
    fun categoryDao_getByType_returnsCorrectCategories() = runTest {
        // 给定
        val expenseCategory = Category(name = "餐饮", type = TransactionType.EXPENSE, icon = "🍜")
        val incomeCategory = Category(name = "工资", type = TransactionType.INCOME, icon = "💰")
        
        categoryDao.insert(expenseCategory)
        categoryDao.insert(incomeCategory)
        
        // 当
        val expenseCategories = categoryDao.getCategoriesByType(TransactionType.EXPENSE).first()
        val incomeCategories = categoryDao.getCategoriesByType(TransactionType.INCOME).first()
        
        // 则
        assertEquals(1, expenseCategories.size)
        assertEquals("餐饮", expenseCategories.first().name)
        assertEquals(1, incomeCategories.size)
        assertEquals("工资", incomeCategories.first().name)
    }
    
    @Test
    fun categoryDao_update_changesCategory() = runTest {
        // 给定
        val category = Category(name = "旧名称", type = TransactionType.EXPENSE, icon = "🍜")
        val id = categoryDao.insert(category)
        
        // 当 - 更新分类
        val updated = category.copy(name = "新名称", icon = "🍕")
        categoryDao.update(updated)
        
        val retrieved = categoryDao.getCategoryById(id)
        
        // 则
        assertNotNull(retrieved)
        assertEquals("新名称", retrieved?.name)
        assertEquals("🍕", retrieved?.icon)
    }
    
    @Test
    fun categoryDao_delete_removesCategory() = runTest {
        // 给定
        val category = Category(name = "待删除", type = TransactionType.EXPENSE, icon = "🍜")
        val id = categoryDao.insert(category)
        
        // 当
        categoryDao.delete(category)
        
        val retrieved = categoryDao.getCategoryById(id)
        val allCategories = categoryDao.getAllCategories().first()
        
        // 则
        assertNull(retrieved)
        assertTrue(allCategories.isEmpty())
    }
    
    @Test
    fun categoryDao_deleteById_removesCategory() = runTest {
        // 给定
        val category = Category(name = "待删除", type = TransactionType.EXPENSE, icon = "🍜")
        val id = categoryDao.insert(category)
        
        // 当
        categoryDao.deleteById(id)
        
        val retrieved = categoryDao.getCategoryById(id)
        
        // 则
        assertNull(retrieved)
    }
    
    @Test
    fun categoryDao_insert_returnsPositiveId() = runTest {
        // 给定
        val category = Category(name = "测试", type = TransactionType.EXPENSE, icon = "🍜")
        
        // 当
        val id = categoryDao.insert(category)
        
        // 则
        assertTrue("插入应该返回正的 ID", id > 0)
    }
    
    @Test
    fun categoryDao_getByType_emptyList() = runTest {
        // 当
        val expenseCategories = categoryDao.getCategoriesByType(TransactionType.EXPENSE).first()
        
        // 则
        assertTrue(expenseCategories.isEmpty())
    }
    
    @Test
    fun categoryDao_keywords_preserved() = runTest {
        // 给定
        val category = Category(
            name = "餐饮",
            type = TransactionType.EXPENSE,
            icon = "🍜",
            keywords = "餐厅，饭店，外卖"
        )
        
        // 当
        val id = categoryDao.insert(category)
        val retrieved = categoryDao.getCategoryById(id)
        
        // 则
        assertNotNull(retrieved)
        assertEquals("餐厅，饭店，外卖", retrieved?.keywords)
    }
    
    @Test
    fun categoryDao_parentId_preserved() = runTest {
        // 给定
        val parentCategory = Category(name = "餐饮", type = TransactionType.EXPENSE, icon = "🍜")
        val parentId = categoryDao.insert(parentCategory)
        
        val childCategory = Category(
            name = "快餐",
            type = TransactionType.EXPENSE,
            icon = "🍔",
            parentId = parentId
        )
        
        // 当
        val childId = categoryDao.insert(childCategory)
        val retrieved = categoryDao.getCategoryById(childId)
        
        // 则
        assertNotNull(retrieved)
        assertEquals(parentId, retrieved?.parentId)
    }
    
    @Test
    fun categoryDao_order_preserved() = runTest {
        // 给定
        val category1 = Category(name = "餐饮", type = TransactionType.EXPENSE, icon = "🍜", order = 1)
        val category2 = Category(name = "交通", type = TransactionType.EXPENSE, icon = "🚗", order = 2)
        val category3 = Category(name = "购物", type = TransactionType.EXPENSE, icon = "🛍️", order = 3)
        
        // 当
        categoryDao.insert(category1)
        categoryDao.insert(category2)
        categoryDao.insert(category3)
        
        val allCategories = categoryDao.getAllCategories().first()
        
        // 则
        assertEquals(3, allCategories.size)
        assertTrue(allCategories.any { it.order == 1 })
        assertTrue(allCategories.any { it.order == 2 })
        assertTrue(allCategories.any { it.order == 3 })
    }
    
    @Test
    fun categoryDao_isEnabled_preserved() = runTest {
        // 给定
        val enabledCategory = Category(name = "启用", type = TransactionType.EXPENSE, icon = "🍜", isEnabled = true)
        val disabledCategory = Category(name = "禁用", type = TransactionType.EXPENSE, icon = "🚗", isEnabled = false)
        
        // 当
        categoryDao.insert(enabledCategory)
        categoryDao.insert(disabledCategory)
        
        val allCategories = categoryDao.getAllCategories().first()
        
        // 则
        assertEquals(2, allCategories.size)
        assertTrue(allCategories.any { it.isEnabled && it.name == "启用" })
        assertTrue(allCategories.any { !it.isEnabled && it.name == "禁用" })
    }
}
