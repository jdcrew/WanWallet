package com.jdcrew.wanwallet.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.jdcrew.wanwallet.data.model.PaymentChannel
import com.jdcrew.wanwallet.data.model.Transaction
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
class TransactionDaoTest {
    
    private lateinit var transactionDao: TransactionDao
    private lateinit var database: WanWalletDatabase
    
    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, WanWalletDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        transactionDao = database.transactionDao()
    }
    
    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }
    
    @Test
    fun transactionDao_insertAndGetById() = runTest {
        // 给定
        val transaction = Transaction(
            amount = 100.0,
            type = TransactionType.EXPENSE,
            categoryId = 1,
            merchant = "星巴克",
            time = System.currentTimeMillis(),
            channel = PaymentChannel.WECHAT
        )
        
        // 当
        val id = transactionDao.insert(transaction)
        val retrieved = transactionDao.getTransactionById(id)
        
        // 则
        assertNotNull(retrieved)
        assertEquals(id, retrieved?.id)
        assertEquals(100.0, retrieved?.amount, 0.01)
        assertEquals("星巴克", retrieved?.merchant)
        assertEquals(PaymentChannel.WECHAT, retrieved?.channel)
    }
    
    @Test
    fun transactionDao_insert_updatesAllTransactions() = runTest {
        // 给定
        val transaction1 = Transaction(
            amount = 100.0,
            type = TransactionType.EXPENSE,
            categoryId = 1,
            merchant = "星巴克",
            time = System.currentTimeMillis()
        )
        
        val transaction2 = Transaction(
            amount = 200.0,
            type = TransactionType.INCOME,
            categoryId = 2,
            merchant = "工资",
            time = System.currentTimeMillis()
        )
        
        // 当
        transactionDao.insert(transaction1)
        transactionDao.insert(transaction2)
        
        val allTransactions = transactionDao.getAllTransactions().first()
        
        // 则
        assertEquals(2, allTransactions.size)
        assertTrue(allTransactions.any { it.merchant == "星巴克" })
        assertTrue(allTransactions.any { it.merchant == "工资" })
    }
    
    @Test
    fun transactionDao_getByTimeRange_returnsCorrectTransactions() = runTest {
        // 给定
        val now = System.currentTimeMillis()
        val threeDaysAgo = now - (3 * 24 * 60 * 60 * 1000)
        val fiveDaysAgo = now - (5 * 24 * 60 * 60 * 1000)
        
        val transaction1 = Transaction(
            amount = 100.0,
            type = TransactionType.EXPENSE,
            categoryId = 1,
            merchant = "最近交易",
            time = threeDaysAgo
        )
        
        val transaction2 = Transaction(
            amount = 200.0,
            type = TransactionType.EXPENSE,
            categoryId = 1,
            merchant = "旧交易",
            time = fiveDaysAgo
        )
        
        transactionDao.insert(transaction1)
        transactionDao.insert(transaction2)
        
        // 当 - 查询最近 7 天的交易
        val transactions = transactionDao.getTransactionsByTimeRange(threeDaysAgo, now).first()
        
        // 则
        assertEquals(1, transactions.size)
        assertEquals("最近交易", transactions.first().merchant)
    }
    
    @Test
    fun transactionDao_getByType_returnsCorrectTransactions() = runTest {
        // 给定
        val expense = Transaction(
            amount = -100.0,
            type = TransactionType.EXPENSE,
            categoryId = 1,
            merchant = "支出",
            time = System.currentTimeMillis()
        )
        
        val income = Transaction(
            amount = 500.0,
            type = TransactionType.INCOME,
            categoryId = 2,
            merchant = "收入",
            time = System.currentTimeMillis()
        )
        
        transactionDao.insert(expense)
        transactionDao.insert(income)
        
        // 当
        val expenses = transactionDao.getTransactionsByType(TransactionType.EXPENSE).first()
        val incomes = transactionDao.getTransactionsByType(TransactionType.INCOME).first()
        
        // 则
        assertEquals(1, expenses.size)
        assertEquals("支出", expenses.first().merchant)
        assertEquals(1, incomes.size)
        assertEquals("收入", incomes.first().merchant)
    }
    
    @Test
    fun transactionDao_getByCategory_returnsCorrectTransactions() = runTest {
        // 给定
        val transaction1 = Transaction(
            amount = 100.0,
            type = TransactionType.EXPENSE,
            categoryId = 1, // 餐饮
            merchant = "餐厅",
            time = System.currentTimeMillis()
        )
        
        val transaction2 = Transaction(
            amount = 200.0,
            type = TransactionType.EXPENSE,
            categoryId = 2, // 交通
            merchant = "打车",
            time = System.currentTimeMillis()
        )
        
        transactionDao.insert(transaction1)
        transactionDao.insert(transaction2)
        
        // 当
        val category1Transactions = transactionDao.getTransactionsByCategory(1).first()
        val category2Transactions = transactionDao.getTransactionsByCategory(2).first()
        
        // 则
        assertEquals(1, category1Transactions.size)
        assertEquals("餐厅", category1Transactions.first().merchant)
        assertEquals(1, category2Transactions.size)
        assertEquals("打车", category2Transactions.first().merchant)
    }
    
    @Test
    fun transactionDao_update_changesTransaction() = runTest {
        // 给定
        val transaction = Transaction(
            amount = 100.0,
            type = TransactionType.EXPENSE,
            categoryId = 1,
            merchant = "原商户",
            time = System.currentTimeMillis()
        )
        
        val id = transactionDao.insert(transaction)
        
        // 当 - 更新交易
        val updated = transaction.copy(amount = 150.0, merchant = "新商户", note = "备注")
        transactionDao.update(updated)
        
        val retrieved = transactionDao.getTransactionById(id)
        
        // 则
        assertNotNull(retrieved)
        assertEquals(150.0, retrieved?.amount, 0.01)
        assertEquals("新商户", retrieved?.merchant)
        assertEquals("备注", retrieved?.note)
    }
    
    @Test
    fun transactionDao_delete_removesTransaction() = runTest {
        // 给定
        val transaction = Transaction(
            amount = 100.0,
            type = TransactionType.EXPENSE,
            categoryId = 1,
            merchant = "待删除",
            time = System.currentTimeMillis()
        )
        
        val id = transactionDao.insert(transaction)
        
        // 当
        transactionDao.delete(transaction)
        
        val retrieved = transactionDao.getTransactionById(id)
        val allTransactions = transactionDao.getAllTransactions().first()
        
        // 则
        assertNull(retrieved)
        assertTrue(allTransactions.isEmpty())
    }
    
    @Test
    fun transactionDao_deleteById_removesTransaction() = runTest {
        // 给定
        val transaction = Transaction(
            amount = 100.0,
            type = TransactionType.EXPENSE,
            categoryId = 1,
            merchant = "待删除",
            time = System.currentTimeMillis()
        )
        
        val id = transactionDao.insert(transaction)
        
        // 当
        transactionDao.deleteById(id)
        
        val retrieved = transactionDao.getTransactionById(id)
        
        // 则
        assertNull(retrieved)
    }
    
    @Test
    fun transactionDao_insert_returnsPositiveId() = runTest {
        // 给定
        val transaction = Transaction(
            amount = 100.0,
            type = TransactionType.EXPENSE,
            categoryId = 1,
            merchant = "测试",
            time = System.currentTimeMillis()
        )
        
        // 当
        val id = transactionDao.insert(transaction)
        
        // 则
        assertTrue("插入应该返回正的 ID", id > 0)
    }
    
    @Test
    fun transactionDao_autoFlag_preserved() = runTest {
        // 给定
        val autoTransaction = Transaction(
            amount = 100.0,
            type = TransactionType.EXPENSE,
            categoryId = 1,
            merchant = "自动",
            time = System.currentTimeMillis(),
            isAuto = true
        )
        
        val manualTransaction = Transaction(
            amount = 50.0,
            type = TransactionType.EXPENSE,
            categoryId = 1,
            merchant = "手动",
            time = System.currentTimeMillis(),
            isAuto = false
        )
        
        // 当
        transactionDao.insert(autoTransaction)
        transactionDao.insert(manualTransaction)
        
        val allTransactions = transactionDao.getAllTransactions().first()
        
        // 则
        assertEquals(2, allTransactions.size)
        assertTrue(allTransactions.any { it.isAuto && it.merchant == "自动" })
        assertTrue(allTransactions.any { !it.isAuto && it.merchant == "手动" })
    }
}
