package com.jdcrew.wanwallet.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.jdcrew.wanwallet.data.model.Budget
import com.jdcrew.wanwallet.data.model.BudgetPeriod
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
class BudgetDaoTest {
    
    private lateinit var budgetDao: BudgetDao
    private lateinit var database: WanWalletDatabase
    
    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, WanWalletDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        budgetDao = database.budgetDao()
    }
    
    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }
    
    @Test
    fun budgetDao_insertAndGetById() = runTest {
        // 给定
        val budget = Budget(
            amount = 1000.0,
            period = BudgetPeriod.MONTH,
            categoryId = 1,
            startDate = System.currentTimeMillis(),
            endDate = System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000
        )
        
        // 当
        val id = budgetDao.insert(budget)
        val retrieved = budgetDao.getBudgetById(id)
        
        // 则
        assertNotNull(retrieved)
        assertEquals(id, retrieved?.id)
        assertEquals(1000.0, retrieved?.amount, 0.01)
        assertEquals(BudgetPeriod.MONTH, retrieved?.period)
        assertEquals(1, retrieved?.categoryId)
    }
    
    @Test
    fun budgetDao_insert_updatesAllBudgets() = runTest {
        // 给定
        val budget1 = Budget(
            amount = 1000.0,
            period = BudgetPeriod.MONTH,
            categoryId = 1,
            startDate = System.currentTimeMillis(),
            endDate = System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000
        )
        
        val budget2 = Budget(
            amount = 2000.0,
            period = BudgetPeriod.MONTH,
            categoryId = 2,
            startDate = System.currentTimeMillis(),
            endDate = System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000
        )
        
        // 当
        budgetDao.insert(budget1)
        budgetDao.insert(budget2)
        
        val allBudgets = budgetDao.getAllBudgets().first()
        
        // 则
        assertEquals(2, allBudgets.size)
        assertTrue(allBudgets.any { it.amount == 1000.0 })
        assertTrue(allBudgets.any { it.amount == 2000.0 })
    }
    
    @Test
    fun budgetDao_getActiveBudgets_returnsOnlyActive() = runTest {
        // 给定
        val now = System.currentTimeMillis()
        val future = now + 30 * 24 * 60 * 60 * 1000
        val past = now - 30 * 24 * 60 * 60 * 1000
        
        val activeBudget = Budget(
            amount = 1000.0,
            period = BudgetPeriod.MONTH,
            categoryId = 1,
            startDate = now,
            endDate = future,
            isEnabled = true
        )
        
        val expiredBudget = Budget(
            amount = 2000.0,
            period = BudgetPeriod.MONTH,
            categoryId = 2,
            startDate = past,
            endDate = now,
            isEnabled = true
        )
        
        val disabledBudget = Budget(
            amount = 3000.0,
            period = BudgetPeriod.MONTH,
            categoryId = 3,
            startDate = now,
            endDate = future,
            isEnabled = false
        )
        
        budgetDao.insert(activeBudget)
        budgetDao.insert(expiredBudget)
        budgetDao.insert(disabledBudget)
        
        // 当
        val activeBudgets = budgetDao.getActiveBudgets(now).first()
        
        // 则
        assertEquals(1, activeBudgets.size)
        assertEquals(1000.0, activeBudgets.first().amount, 0.01)
    }
    
    @Test
    fun budgetDao_update_changesBudget() = runTest {
        // 给定
        val budget = Budget(
            amount = 1000.0,
            period = BudgetPeriod.MONTH,
            categoryId = 1,
            startDate = System.currentTimeMillis(),
            endDate = System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000
        )
        
        val id = budgetDao.insert(budget)
        
        // 当 - 更新预算
        val updated = budget.copy(amount = 1500.0, spent = 500.0)
        budgetDao.update(updated)
        
        val retrieved = budgetDao.getBudgetById(id)
        
        // 则
        assertNotNull(retrieved)
        assertEquals(1500.0, retrieved?.amount, 0.01)
        assertEquals(500.0, retrieved?.spent, 0.01)
    }
    
    @Test
    fun budgetDao_updateSpent_updatesSpentAmount() = runTest {
        // 给定
        val budget = Budget(
            amount = 1000.0,
            period = BudgetPeriod.MONTH,
            categoryId = 1,
            startDate = System.currentTimeMillis(),
            endDate = System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000,
            spent = 0.0
        )
        
        val id = budgetDao.insert(budget)
        
        // 当
        budgetDao.updateSpent(id, 300.0)
        
        val retrieved = budgetDao.getBudgetById(id)
        
        // 则
        assertNotNull(retrieved)
        assertEquals(300.0, retrieved?.spent, 0.01)
    }
    
    @Test
    fun budgetDao_delete_removesBudget() = runTest {
        // 给定
        val budget = Budget(
            amount = 1000.0,
            period = BudgetPeriod.MONTH,
            categoryId = 1,
            startDate = System.currentTimeMillis(),
            endDate = System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000
        )
        
        val id = budgetDao.insert(budget)
        
        // 当
        budgetDao.delete(budget)
        
        val retrieved = budgetDao.getBudgetById(id)
        val allBudgets = budgetDao.getAllBudgets().first()
        
        // 则
        assertNull(retrieved)
        assertTrue(allBudgets.isEmpty())
    }
    
    @Test
    fun budgetDao_deleteById_removesBudget() = runTest {
        // 给定
        val budget = Budget(
            amount = 1000.0,
            period = BudgetPeriod.MONTH,
            categoryId = 1,
            startDate = System.currentTimeMillis(),
            endDate = System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000
        )
        
        val id = budgetDao.insert(budget)
        
        // 当
        budgetDao.deleteById(id)
        
        val retrieved = budgetDao.getBudgetById(id)
        
        // 则
        assertNull(retrieved)
    }
    
    @Test
    fun budgetDao_insert_returnsPositiveId() = runTest {
        // 给定
        val budget = Budget(
            amount = 1000.0,
            period = BudgetPeriod.MONTH,
            categoryId = 1,
            startDate = System.currentTimeMillis(),
            endDate = System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000
        )
        
        // 当
        val id = budgetDao.insert(budget)
        
        // 则
        assertTrue("插入应该返回正的 ID", id > 0)
    }
    
    @Test
    fun budgetDao_totalBudget_returnsZero() = runTest {
        // 给定
        val budgets = listOf(
            Budget(amount = 1000.0, period = BudgetPeriod.MONTH, categoryId = 1,
                   startDate = System.currentTimeMillis(), endDate = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000),
            Budget(amount = 2000.0, period = BudgetPeriod.MONTH, categoryId = 2,
                   startDate = System.currentTimeMillis(), endDate = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)
        )
        
        budgets.forEach { budgetDao.insert(it) }
        
        // 当
        val total = budgetDao.getTotalBudget().first()
        
        // 则
        assertEquals(3000.0, total, 0.01)
    }
    
    @Test
    fun budgetDao_period_types() = runTest {
        // 给定
        val weekBudget = Budget(
            amount = 500.0,
            period = BudgetPeriod.WEEK,
            categoryId = 1,
            startDate = System.currentTimeMillis(),
            endDate = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000
        )
        
        val monthBudget = Budget(
            amount = 2000.0,
            period = BudgetPeriod.MONTH,
            categoryId = 1,
            startDate = System.currentTimeMillis(),
            endDate = System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000
        )
        
        val yearBudget = Budget(
            amount = 24000.0,
            period = BudgetPeriod.YEAR,
            categoryId = 1,
            startDate = System.currentTimeMillis(),
            endDate = System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000
        )
        
        // 当
        budgetDao.insert(weekBudget)
        budgetDao.insert(monthBudget)
        budgetDao.insert(yearBudget)
        
        val allBudgets = budgetDao.getAllBudgets().first()
        
        // 则
        assertEquals(3, allBudgets.size)
        assertTrue(allBudgets.any { it.period == BudgetPeriod.WEEK })
        assertTrue(allBudgets.any { it.period == BudgetPeriod.MONTH })
        assertTrue(allBudgets.any { it.period == BudgetPeriod.YEAR })
    }
}
