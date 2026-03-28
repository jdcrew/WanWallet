package com.jdcrew.wanwallet.ui.viewmodel

import com.jdcrew.wanwallet.data.model.Budget
import com.jdcrew.wanwallet.data.model.BudgetPeriod
import com.jdcrew.wanwallet.data.model.Category
import com.jdcrew.wanwallet.data.model.TransactionType
import com.jdcrew.wanwallet.data.repository.BudgetRepository
import com.jdcrew.wanwallet.data.repository.CategoryRepository
import com.jdcrew.wanwallet.testdata.TestDataFactory
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetViewModelTest {
    
    @MockK
    private lateinit var budgetRepository: BudgetRepository
    
    @MockK
    private lateinit var categoryRepository: CategoryRepository
    
    private lateinit var viewModel: BudgetViewModel
    
    private val testDispatcher = UnconfinedTestDispatcher()
    
    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        
        // Mock 分类数据
        val categories = TestDataFactory.makePresetCategories()
        coEvery { categoryRepository.allCategories } returns MutableStateFlow(categories)
        
        // Mock 预算数据
        val budgets = TestDataFactory.makeBudgetList(3)
        coEvery { budgetRepository.allBudgets } returns MutableStateFlow(budgets)
        
        viewModel = BudgetViewModel(budgetRepository, categoryRepository)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun budgetViewModel_initialState_isCorrect() = runTest {
        // 给定
        val initialState = viewModel.uiState.value
        
        // 则
        assertFalse(initialState.isLoading)
        assertTrue(initialState.budgets.isNotEmpty())
        assertTrue(initialState.budgetWithCategories.isNotEmpty())
    }
    
    @Test
    fun budgetUiState_defaultValues() {
        // 当
        val state = BudgetUiState()
        
        // 则
        assertEquals(0.0, state.totalBudget, 0.01)
        assertEquals(0.0, state.totalSpent, 0.01)
        assertEquals(0.0, state.remaining, 0.01)
        assertEquals(0f, state.progress, 0.01f)
        assertFalse(state.isOverBudget)
        assertTrue(state.budgets.isEmpty())
        assertTrue(state.budgetWithCategories.isEmpty())
    }
    
    @Test
    fun budgetViewModel_loadBudgets_calculatesTotals() = runTest {
        // 给定
        val budgets = listOf(
            TestDataFactory.makeBudget(id = 1, amount = 1000.0, spent = 500.0),
            TestDataFactory.makeBudget(id = 2, amount = 2000.0, spent = 1500.0)
        )
        coEvery { budgetRepository.allBudgets } returns MutableStateFlow(budgets)
        
        // 重新创建 ViewModel 以触发加载
        viewModel = BudgetViewModel(budgetRepository, categoryRepository)
        
        // 当
        val state = viewModel.uiState.value
        
        // 则
        assertEquals(3000.0, state.totalBudget, 0.01)
        assertEquals(2000.0, state.totalSpent, 0.01)
        assertEquals(1000.0, state.remaining, 0.01)
        assertEquals(0.67f, state.progress, 0.01f)
        assertFalse(state.isOverBudget)
    }
    
    @Test
    fun budgetViewModel_budgetExceeded_setsOverBudget() = runTest {
        // 给定
        val budgets = listOf(
            TestDataFactory.makeBudget(id = 1, amount = 1000.0, spent = 1500.0)
        )
        coEvery { budgetRepository.allBudgets } returns MutableStateFlow(budgets)
        
        // 重新创建 ViewModel
        viewModel = BudgetViewModel(budgetRepository, categoryRepository)
        
        // 当
        val state = viewModel.uiState.value
        
        // 则
        assertTrue(state.isOverBudget)
        assertEquals(-500.0, state.remaining, 0.01)
    }
    
    @Test
    fun budgetViewModel_showsCategoryNames_notIds() = runTest {
        // 给定
        val categories = TestDataFactory.makePresetCategories()
        val budgets = listOf(
            TestDataFactory.makeBudget(id = 1, categoryId = 1, amount = 1000.0) // categoryId=1 是"餐饮"
        )
        
        coEvery { categoryRepository.allCategories } returns MutableStateFlow(categories)
        coEvery { budgetRepository.allBudgets } returns MutableStateFlow(budgets)
        
        // 重新创建 ViewModel
        viewModel = BudgetViewModel(budgetRepository, categoryRepository)
        
        // 当
        val state = viewModel.uiState.value
        val budgetWithCategory = state.budgetWithCategories.first()
        
        // 则
        assertEquals("餐饮", budgetWithCategory.categoryName)
        assertNotEquals("分类 1", budgetWithCategory.categoryName)
        assertEquals("🍜", budgetWithCategory.categoryIcon)
    }
    
    @Test
    fun budgetViewModel_totalBudget_showsCategoryIcon() = runTest {
        // 给定
        val budgets = listOf(
            TestDataFactory.makeBudget(id = 1, categoryId = 0, amount = 5000.0) // categoryId=0 是总预算
        )
        val categories = TestDataFactory.makePresetCategories()
        
        coEvery { categoryRepository.allCategories } returns MutableStateFlow(categories)
        coEvery { budgetRepository.allBudgets } returns MutableStateFlow(budgets)
        
        // 重新创建 ViewModel
        viewModel = BudgetViewModel(budgetRepository, categoryRepository)
        
        // 当
        val state = viewModel.uiState.value
        val budgetWithCategory = state.budgetWithCategories.first()
        
        // 则
        assertEquals("总预算", budgetWithCategory.categoryName)
        assertEquals("📊", budgetWithCategory.categoryIcon)
    }
    
    @Test
    fun budgetViewModel_createBudget_addsToRepository() = runTest {
        // 给定
        var createdBudget: Budget? = null
        coEvery { budgetRepository.createBudget(any()) } answers {
            createdBudget = firstArg()
        }
        
        // 当
        viewModel.createBudget(
            amount = 2000.0,
            period = BudgetPeriod.MONTH,
            categoryId = 1
        )
        
        // 则
        assertNotNull(createdBudget)
        assertEquals(2000.0, createdBudget?.amount, 0.01)
        assertEquals(BudgetPeriod.MONTH, createdBudget?.period)
        assertEquals(1, createdBudget?.categoryId)
    }
    
    @Test
    fun budgetViewModel_updateBudget_callsRepository() = runTest {
        // 给定
        val budget = TestDataFactory.makeBudget(id = 1, amount = 1500.0)
        var updatedBudget: Budget? = null
        coEvery { budgetRepository.updateBudget(any()) } answers {
            updatedBudget = firstArg()
        }
        
        // 当
        viewModel.updateBudget(budget)
        
        // 则
        assertNotNull(updatedBudget)
        assertEquals(1, updatedBudget?.id)
        assertEquals(1500.0, updatedBudget?.amount, 0.01)
    }
    
    @Test
    fun budgetViewModel_deleteBudget_callsRepository() = runTest {
        // 给定
        val budget = TestDataFactory.makeBudget(id = 1)
        var deletedBudget: Budget? = null
        coEvery { budgetRepository.deleteBudget(any()) } answers {
            deletedBudget = firstArg()
        }
        
        // 当
        viewModel.deleteBudget(budget)
        
        // 则
        assertNotNull(deletedBudget)
        assertEquals(1, deletedBudget?.id)
    }
}
