package com.jdcrew.wanwallet.ui.viewmodel

import com.jdcrew.wanwallet.data.model.Transaction
import com.jdcrew.wanwallet.data.model.TransactionType
import com.jdcrew.wanwallet.data.repository.TransactionRepository
import com.jdcrew.wanwallet.testdata.TestDataFactory
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    
    @MockK
    private lateinit var transactionRepository: TransactionRepository
    
    private lateinit var viewModel: HomeViewModel
    
    private val testDispatcher = UnconfinedTestDispatcher()
    
    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        
        // Mock 交易数据
        val transactions = TestDataFactory.makeTransactionList(5)
        coEvery { transactionRepository.allTransactions } returns MutableStateFlow(transactions)
        
        viewModel = HomeViewModel(transactionRepository)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun homeUiState_defaultValues() {
        // 当
        val state = HomeUiState()
        
        // 则
        assertTrue(state.transactions.isEmpty())
        assertEquals(0.0, state.totalIncome, 0.01)
        assertEquals(0.0, state.totalExpense, 0.01)
        assertEquals(0.0, state.balance, 0.01)
        assertFalse(state.isLoading)
    }
    
    @Test
    fun homeViewModel_initialState_loadsTransactions() = runTest {
        // 给定
        val transactions = TestDataFactory.makeTransactionList(5)
        coEvery { transactionRepository.allTransactions } returns MutableStateFlow(transactions)
        
        // 重新创建 ViewModel
        viewModel = HomeViewModel(transactionRepository)
        
        // 当
        val state = viewModel.uiState.value
        
        // 则
        assertFalse(state.isLoading)
        assertEquals(5, state.transactions.size)
    }
    
    @Test
    fun homeViewModel_loadTransactions_calculatesTotals() = runTest {
        // 给定
        val transactions = listOf(
            TestDataFactory.makeTransaction(id = 1, amount = 100.0, type = TransactionType.EXPENSE),
            TestDataFactory.makeTransaction(id = 2, amount = 200.0, type = TransactionType.EXPENSE),
            TestDataFactory.makeTransaction(id = 3, amount = 500.0, type = TransactionType.INCOME)
        )
        coEvery { transactionRepository.allTransactions } returns MutableStateFlow(transactions)
        
        // 重新创建 ViewModel
        viewModel = HomeViewModel(transactionRepository)
        
        // 当
        val state = viewModel.uiState.value
        
        // 则
        assertEquals(500.0, state.totalIncome, 0.01)
        assertEquals(300.0, state.totalExpense, 0.01)
        assertEquals(200.0, state.balance, 0.01)
    }
    
    @Test
    fun homeViewModel_balance_calculation() = runTest {
        // 给定
        val transactions = listOf(
            TestDataFactory.makeTransaction(id = 1, amount = 1000.0, type = TransactionType.INCOME),
            TestDataFactory.makeTransaction(id = 2, amount = 300.0, type = TransactionType.EXPENSE)
        )
        coEvery { transactionRepository.allTransactions } returns MutableStateFlow(transactions)
        
        // 重新创建 ViewModel
        viewModel = HomeViewModel(transactionRepository)
        
        // 当
        val state = viewModel.uiState.value
        
        // 则
        assertEquals(state.totalIncome - state.totalExpense, state.balance, 0.01)
    }
    
    @Test
    fun homeViewModel_transactions_sortedByTime() = runTest {
        // 给定
        val now = System.currentTimeMillis()
        val transactions = listOf(
            TestDataFactory.makeTransaction(id = 1, amount = 100.0, time = now - 10000),
            TestDataFactory.makeTransaction(id = 2, amount = 200.0, time = now - 5000),
            TestDataFactory.makeTransaction(id = 3, amount = 300.0, time = now)
        )
        coEvery { transactionRepository.allTransactions } returns MutableStateFlow(transactions)
        
        // 重新创建 ViewModel
        viewModel = HomeViewModel(transactionRepository)
        
        // 当
        val state = viewModel.uiState.value
        
        // 则
        // 验证按时间降序排列（最新的在前）
        for (i in 0 until state.transactions.size - 1) {
            assertTrue(
                "交易应该按时间降序排列",
                state.transactions[i].time >= state.transactions[i + 1].time
            )
        }
    }
    
    @Test
    fun homeViewModel_emptyTransactions_showsZero() = runTest {
        // 给定
        coEvery { transactionRepository.allTransactions } returns MutableStateFlow(emptyList())
        
        // 重新创建 ViewModel
        viewModel = HomeViewModel(transactionRepository)
        
        // 当
        val state = viewModel.uiState.value
        
        // 则
        assertTrue(state.transactions.isEmpty())
        assertEquals(0.0, state.totalIncome, 0.01)
        assertEquals(0.0, state.totalExpense, 0.01)
        assertEquals(0.0, state.balance, 0.01)
    }
    
    @Test
    fun homeViewModel_refreshTransactions_updatesList() = runTest {
        // 给定
        val initialTransactions = TestDataFactory.makeTransactionList(3)
        coEvery { transactionRepository.allTransactions } returns MutableStateFlow(initialTransactions)
        
        viewModel = HomeViewModel(transactionRepository)
        
        // 当 - 更新数据
        val newTransactions = TestDataFactory.makeTransactionList(10)
        coEvery { transactionRepository.allTransactions } returns MutableStateFlow(newTransactions)
        
        // 触发刷新（通过重新收集 Flow）
        viewModel = HomeViewModel(transactionRepository)
        
        // 则
        assertEquals(10, viewModel.uiState.value.transactions.size)
    }
    
    @Test
    fun homeViewModel_onlyExpense_showsCorrectTotal() = runTest {
        // 给定
        val transactions = listOf(
            TestDataFactory.makeTransaction(id = 1, amount = 100.0, type = TransactionType.EXPENSE),
            TestDataFactory.makeTransaction(id = 2, amount = 200.0, type = TransactionType.EXPENSE),
            TestDataFactory.makeTransaction(id = 3, amount = 300.0, type = TransactionType.EXPENSE)
        )
        coEvery { transactionRepository.allTransactions } returns MutableStateFlow(transactions)
        
        // 重新创建 ViewModel
        viewModel = HomeViewModel(transactionRepository)
        
        // 当
        val state = viewModel.uiState.value
        
        // 则
        assertEquals(0.0, state.totalIncome, 0.01)
        assertEquals(600.0, state.totalExpense, 0.01)
        assertEquals(-600.0, state.balance, 0.01)
    }
    
    @Test
    fun homeViewModel_onlyIncome_showsCorrectTotal() = runTest {
        // 给定
        val transactions = listOf(
            TestDataFactory.makeTransaction(id = 1, amount = 1000.0, type = TransactionType.INCOME),
            TestDataFactory.makeTransaction(id = 2, amount = 2000.0, type = TransactionType.INCOME)
        )
        coEvery { transactionRepository.allTransactions } returns MutableStateFlow(transactions)
        
        // 重新创建 ViewModel
        viewModel = HomeViewModel(transactionRepository)
        
        // 当
        val state = viewModel.uiState.value
        
        // 则
        assertEquals(3000.0, state.totalIncome, 0.01)
        assertEquals(0.0, state.totalExpense, 0.01)
        assertEquals(3000.0, state.balance, 0.01)
    }
}
