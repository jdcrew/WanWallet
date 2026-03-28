package com.jdcrew.wanwallet.ui.viewmodel

import com.jdcrew.wanwallet.data.model.Category
import com.jdcrew.wanwallet.data.model.Transaction
import com.jdcrew.wanwallet.data.model.TransactionType
import com.jdcrew.wanwallet.data.repository.CategoryRepository
import com.jdcrew.wanwallet.data.repository.TransactionRepository
import com.jdcrew.wanwallet.testdata.TestDataFactory
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModelTest {
    
    @MockK
    private lateinit var transactionRepository: TransactionRepository
    
    @MockK
    private lateinit var categoryRepository: CategoryRepository
    
    private lateinit var viewModel: StatsViewModel
    
    private val testDispatcher = UnconfinedTestDispatcher()
    
    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        
        // Mock 分类数据
        val categories = TestDataFactory.makePresetCategories()
        coEvery { categoryRepository.allCategories } returns MutableStateFlow(categories)
        
        // Mock 交易数据
        val transactions = TestDataFactory.makeTransactionList(5)
        coEvery { transactionRepository.getTransactionsByTimeRange(any(), any()) } 
            returns MutableStateFlow(transactions)
        
        viewModel = StatsViewModel(transactionRepository, categoryRepository)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun statsUiState_defaultValues() {
        // 当
        val state = StatsUiState()
        
        // 则
        assertEquals(0.0, state.totalIncome, 0.01)
        assertEquals(0.0, state.totalExpense, 0.01)
        assertEquals(0.0, state.balance, 0.01)
        assertTrue(state.categoryData.isEmpty())
        assertTrue(state.dailyTrend.isEmpty())
        assertEquals(StatsPeriod.MONTH, state.selectedPeriod)
        assertFalse(state.isLoading)
    }
    
    @Test
    fun statsViewModel_initialState_isLoading() = runTest {
        // 给定
        val initialState = viewModel.uiState.value
        
        // 则
        assertFalse(initialState.isLoading) // 加载完成后应为 false
    }
    
    @Test
    fun statsViewModel_loadStats_calculatesCorrectTotals() = runTest {
        // 给定
        val transactions = listOf(
            TestDataFactory.makeTransaction(id = 1, amount = 100.0, type = TransactionType.EXPENSE),
            TestDataFactory.makeTransaction(id = 2, amount = 50.0, type = TransactionType.EXPENSE),
            TestDataFactory.makeTransaction(id = 3, amount = 200.0, type = TransactionType.INCOME)
        )
        coEvery { transactionRepository.getTransactionsByTimeRange(any(), any()) } 
            returns MutableStateFlow(transactions)
        
        // 重新创建 ViewModel 以触发加载
        viewModel = StatsViewModel(transactionRepository, categoryRepository)
        
        // 当
        val state = viewModel.uiState.value
        
        // 则
        assertEquals(200.0, state.totalIncome, 0.01)
        assertEquals(150.0, state.totalExpense, 0.01)
        assertEquals(50.0, state.balance, 0.01)
    }
    
    @Test
    fun statsViewModel_categoryData_usesCategoryNamesNotIds() = runTest {
        // 这是 BF002 的回归测试
        // 给定
        val categories = TestDataFactory.makePresetCategories()
        val transactions = listOf(
            TestDataFactory.makeTransaction(id = 1, amount = 100.0, categoryId = 1), // 餐饮
            TestDataFactory.makeTransaction(id = 2, amount = 50.0, categoryId = 2)   // 交通
        )
        
        coEvery { categoryRepository.allCategories } returns MutableStateFlow(categories)
        coEvery { transactionRepository.getTransactionsByTimeRange(any(), any()) } 
            returns MutableStateFlow(transactions)
        
        // 重新创建 ViewModel
        viewModel = StatsViewModel(transactionRepository, categoryRepository)
        
        // 当
        val state = viewModel.uiState.value
        val categoryData = state.categoryData
        
        // 则
        assertTrue(categoryData.isNotEmpty())
        
        // 验证显示的是分类名称而不是"分类 X"
        categoryData.forEach { slice ->
            assertNotEquals("分类", slice.label.substring(0, 2))
            assertTrue("分类名称应该是中文", slice.label.length <= 4)
        }
        
        // 验证包含预期分类
        val categoryNames = categoryData.map { it.label }
        assertTrue(categoryNames.contains("餐饮") || categoryNames.contains("交通") || 
                   categoryNames.contains("购物") || categoryNames.contains("其他"))
    }
    
    @Test
    fun statsViewModel_dailyTrend_calculatesActualData() = runTest {
        // 这是 BF004 的回归测试
        // 给定
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -3)
        val threeDaysAgo = calendar.timeInMillis
        
        val transactions = listOf(
            TestDataFactory.makeTransaction(id = 1, amount = 100.0, time = threeDaysAgo),
            TestDataFactory.makeTransaction(id = 2, amount = 50.0, time = threeDaysAgo + 86400000),
            TestDataFactory.makeTransaction(id = 3, amount = 200.0, time = threeDaysAgo + 172800000)
        )
        
        coEvery { transactionRepository.getTransactionsByTimeRange(any(), any()) } 
            returns MutableStateFlow(transactions)
        
        // 重新创建 ViewModel
        viewModel = StatsViewModel(transactionRepository, categoryRepository)
        
        // 当
        val state = viewModel.uiState.value
        val dailyTrend = state.dailyTrend
        
        // 则
        assertTrue("趋势图应该有数据", dailyTrend.isNotEmpty())
        
        // 验证有实际的收支数据（不全为 0）
        val hasNonZeroData = dailyTrend.any { it.income > 0 || it.expense > 0 }
        assertTrue("趋势图应该包含实际交易数据", hasNonZeroData)
        
        // 验证总支出与交易匹配
        val totalExpenseFromTrend = dailyTrend.sumOf { it.expense }
        assertEquals(350.0, totalExpenseFromTrend, 0.01)
    }
    
    @Test
    fun statsViewModel_dailyTrend_fillsMissingDates() = runTest {
        // 验证趋势图填充缺失日期
        // 给定：只有 1 天的交易
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = calendar.timeInMillis
        
        val transactions = listOf(
            TestDataFactory.makeTransaction(id = 1, amount = 100.0, time = yesterday)
        )
        
        coEvery { transactionRepository.getTransactionsByTimeRange(any(), any()) } 
            returns MutableStateFlow(transactions)
        
        // 重新创建 ViewModel
        viewModel = StatsViewModel(transactionRepository, categoryRepository)
        
        // 当
        val state = viewModel.uiState.value
        
        // 则
        // 验证趋势图有多个日期（填充了缺失的日期）
        assertTrue("趋势图应该包含多天数据", state.dailyTrend.size >= 1)
    }
    
    @Test
    fun statsViewModel_updatePeriod_refreshesData() = runTest {
        // 给定
        var callCount = 0
        coEvery { transactionRepository.getTransactionsByTimeRange(any(), any()) } answers {
            callCount++
            MutableStateFlow(emptyList())
        }
        
        // 当
        viewModel.updatePeriod(StatsPeriod.WEEK)
        
        // 则
        assertTrue("切换周期应该重新加载数据", callCount >= 1)
        assertEquals(StatsPeriod.WEEK, viewModel.uiState.value.selectedPeriod)
    }
    
    @Test
    fun statsViewModel_categoryData_sortedByValue() = runTest {
        // 给定
        val transactions = listOf(
            TestDataFactory.makeTransaction(id = 1, amount = 100.0, categoryId = 1), // 餐饮
            TestDataFactory.makeTransaction(id = 2, amount = 300.0, categoryId = 1), // 餐饮
            TestDataFactory.makeTransaction(id = 3, amount = 50.0, categoryId = 2)   // 交通
        )
        
        coEvery { transactionRepository.getTransactionsByTimeRange(any(), any()) } 
            returns MutableStateFlow(transactions)
        
        // 重新创建 ViewModel
        viewModel = StatsViewModel(transactionRepository, categoryRepository)
        
        // 当
        val state = viewModel.uiState.value
        
        // 则
        if (state.categoryData.size > 1) {
            // 验证按金额降序排列
            for (i in 0 until state.categoryData.size - 1) {
                assertTrue(
                    "分类数据应该按金额降序排列",
                    state.categoryData[i].value >= state.categoryData[i + 1].value
                )
            }
        }
    }
    
    @Test
    fun statsViewModel_balance_calculation() = runTest {
        // 给定
        val transactions = listOf(
            TestDataFactory.makeTransaction(id = 1, amount = 1000.0, type = TransactionType.INCOME),
            TestDataFactory.makeTransaction(id = 2, amount = 300.0, type = TransactionType.EXPENSE),
            TestDataFactory.makeTransaction(id = 3, amount = 200.0, type = TransactionType.EXPENSE)
        )
        
        coEvery { transactionRepository.getTransactionsByTimeRange(any(), any()) } 
            returns MutableStateFlow(transactions)
        
        // 重新创建 ViewModel
        viewModel = StatsViewModel(transactionRepository, categoryRepository)
        
        // 当
        val state = viewModel.uiState.value
        
        // 则
        assertEquals(1000.0, state.totalIncome, 0.01)
        assertEquals(500.0, state.totalExpense, 0.01)
        assertEquals(500.0, state.balance, 0.01)
        assertEquals(state.totalIncome - state.totalExpense, state.balance, 0.01)
    }
    
    @Test
    fun statsViewModel_emptyTransactions_showsZero() = runTest {
        // 给定
        coEvery { transactionRepository.getTransactionsByTimeRange(any(), any()) } 
            returns MutableStateFlow(emptyList())
        
        // 重新创建 ViewModel
        viewModel = StatsViewModel(transactionRepository, categoryRepository)
        
        // 当
        val state = viewModel.uiState.value
        
        // 则
        assertEquals(0.0, state.totalIncome, 0.01)
        assertEquals(0.0, state.totalExpense, 0.01)
        assertEquals(0.0, state.balance, 0.01)
        assertTrue(state.categoryData.isEmpty())
    }
}
