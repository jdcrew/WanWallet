package com.jdcrew.wanwallet.ui.viewmodel

import com.jdcrew.wanwallet.data.model.Category
import com.jdcrew.wanwallet.data.model.PaymentChannel
import com.jdcrew.wanwallet.data.model.TransactionType
import com.jdcrew.wanwallet.data.repository.CategoryRepository
import com.jdcrew.wanwallet.data.repository.CategoryRuleEngine
import com.jdcrew.wanwallet.data.repository.MerchantProcessor
import com.jdcrew.wanwallet.data.repository.TransactionRepository
import com.jdcrew.wanwallet.testdata.TestDataFactory
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
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
class AddTransactionViewModelTest {
    
    @MockK
    private lateinit var transactionRepository: TransactionRepository
    
    @MockK
    private lateinit var categoryRepository: CategoryRepository
    
    @MockK
    private lateinit var ruleEngine: CategoryRuleEngine
    
    @MockK
    private lateinit var merchantProcessor: MerchantProcessor
    
    private lateinit var viewModel: AddTransactionViewModel
    
    private val testDispatcher = UnconfinedTestDispatcher()
    
    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        
        // Mock 分类数据
        val categories = TestDataFactory.makePresetCategories()
        coEvery { categoryRepository.getCategoriesByType(any()) } returns MutableStateFlow(categories)
        
        // Mock 商户处理
        coEvery { merchantProcessor.normalizeMerchant(any()) } answers { firstArg() }
        
        // Mock 规则引擎
        coEvery { ruleEngine.matchCategory(any(), any()) } returns categories.first()
        
        viewModel = AddTransactionViewModel(
            transactionRepository,
            categoryRepository,
            ruleEngine,
            merchantProcessor
        )
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun addTransactionUiState_defaultValues() {
        // 当
        val state = AddTransactionUiState()
        
        // 则
        assertEquals("", state.amount)
        assertEquals(TransactionType.EXPENSE, state.type)
        assertNull(state.selectedCategory)
        assertEquals("", state.merchant)
        assertEquals("", state.note)
        assertEquals(PaymentChannel.WECHAT, state.channel)
        assertTrue(state.categories.isEmpty())
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertFalse(state.saveSuccess)
    }
    
    @Test
    fun addTransactionViewModel_initialState_loadsCategories() = runTest {
        // 给定
        val categories = TestDataFactory.makePresetCategories()
        coEvery { categoryRepository.getCategoriesByType(any()) } returns MutableStateFlow(categories)
        
        // 重新创建 ViewModel
        viewModel = AddTransactionViewModel(
            transactionRepository,
            categoryRepository,
            ruleEngine,
            merchantProcessor
        )
        
        // 则
        val state = viewModel.uiState.value
        assertTrue(state.categories.isNotEmpty())
        assertNotNull(state.selectedCategory)
    }
    
    @Test
    fun addTransactionViewModel_updateAmount_updatesState() = runTest {
        // 当
        viewModel.updateAmount("100.50")
        
        // 则
        assertEquals("100.50", viewModel.uiState.value.amount)
    }
    
    @Test
    fun addTransactionViewModel_updateType_updatesState() = runTest {
        // 当
        viewModel.updateType(TransactionType.INCOME)
        
        // 则
        assertEquals(TransactionType.INCOME, viewModel.uiState.value.type)
    }
    
    @Test
    fun addTransactionViewModel_updateCategory_updatesState() = runTest {
        // 给定
        val category = TestDataFactory.makeCategory(name = "测试分类")
        
        // 当
        viewModel.updateCategory(category)
        
        // 则
        assertEquals("测试分类", viewModel.uiState.value.selectedCategory?.name)
    }
    
    @Test
    fun addTransactionViewModel_updateMerchant_updatesState() = runTest {
        // 当
        viewModel.updateMerchant("星巴克")
        
        // 则
        assertEquals("星巴克", viewModel.uiState.value.merchant)
    }
    
    @Test
    fun addTransactionViewModel_updateMerchant_triggersAutoClassify() = runTest {
        // 给定
        viewModel.updateAmount("100")
        
        // 当
        viewModel.updateMerchant("星巴克")
        
        // 则
        coVerify { merchantProcessor.normalizeMerchant("星巴克") }
        coVerify { ruleEngine.matchCategory(any(), any()) }
    }
    
    @Test
    fun addTransactionViewModel_updateNote_updatesState() = runTest {
        // 当
        viewModel.updateNote("测试备注")
        
        // 则
        assertEquals("测试备注", viewModel.uiState.value.note)
    }
    
    @Test
    fun addTransactionViewModel_updateChannel_updatesState() = runTest {
        // 当
        viewModel.updateChannel(PaymentChannel.ALIPAY)
        
        // 则
        assertEquals(PaymentChannel.ALIPAY, viewModel.uiState.value.channel)
    }
    
    @Test
    fun addTransactionViewModel_saveTransaction_validData_success() = runTest {
        // 给定
        viewModel.updateAmount("100")
        viewModel.updateMerchant("星巴克")
        viewModel.updateNote("测试")
        
        coEvery { transactionRepository.insert(any()) } returns 1
        
        // 当
        viewModel.saveTransaction()
        
        // 则
        coVerify { transactionRepository.insert(any()) }
        assertTrue(viewModel.uiState.value.saveSuccess)
        assertNull(viewModel.uiState.value.error)
    }
    
    @Test
    fun addTransactionViewModel_saveTransaction_invalidAmount_showsError() = runTest {
        // 给定
        viewModel.updateAmount("invalid")
        viewModel.updateMerchant("星巴克")
        
        // 当
        viewModel.saveTransaction()
        
        // 则
        assertEquals("请输入有效金额", viewModel.uiState.value.error)
        coVerify(inverse = true) { transactionRepository.insert(any()) }
    }
    
    @Test
    fun addTransactionViewModel_saveTransaction_zeroAmount_showsError() = runTest {
        // 给定
        viewModel.updateAmount("0")
        viewModel.updateMerchant("星巴克")
        
        // 当
        viewModel.saveTransaction()
        
        // 则
        assertEquals("请输入有效金额", viewModel.uiState.value.error)
    }
    
    @Test
    fun addTransactionViewModel_saveTransaction_emptyMerchant_showsError() = runTest {
        // 给定
        viewModel.updateAmount("100")
        viewModel.updateMerchant("")
        
        // 当
        viewModel.saveTransaction()
        
        // 则
        assertEquals("请输入商户名称", viewModel.uiState.value.error)
    }
    
    @Test
    fun addTransactionViewModel_saveTransaction_nullCategory_showsError() = runTest {
        // 给定
        viewModel.updateAmount("100")
        viewModel.updateMerchant("星巴克")
        // selectedCategory 为 null
        
        // 当
        viewModel.saveTransaction()
        
        // 则
        assertEquals("请选择分类", viewModel.uiState.value.error)
    }
    
    @Test
    fun addTransactionViewModel_saveTransaction_repositoryError_showsError() = runTest {
        // 给定
        viewModel.updateAmount("100")
        viewModel.updateMerchant("星巴克")
        coEvery { transactionRepository.insert(any()) } throws Exception("数据库错误")
        
        // 当
        viewModel.saveTransaction()
        
        // 则
        assertNotNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.error?.isNotEmpty() == true)
    }
    
    @Test
    fun addTransactionViewModel_saveTransaction_resetsForm() = runTest {
        // 给定
        viewModel.updateAmount("100")
        viewModel.updateMerchant("星巴克")
        coEvery { transactionRepository.insert(any()) } returns 1
        
        // 当
        viewModel.saveTransaction()
        
        // 则
        // 表单应该被重置
        assertEquals("", viewModel.uiState.value.amount)
        assertEquals("", viewModel.uiState.value.merchant)
        assertEquals("", viewModel.uiState.value.note)
    }
    
    @Test
    fun addTransactionViewModel_clearError_clearsError() = runTest {
        // 给定
        viewModel.updateAmount("invalid")
        viewModel.updateMerchant("星巴克")
        viewModel.saveTransaction()
        
        // 验证有错误
        assertNotNull(viewModel.uiState.value.error)
        
        // 当
        viewModel.clearError()
        
        // 则
        assertNull(viewModel.uiState.value.error)
    }
    
    @Test
    fun addTransactionViewModel_clearSuccess_clearsSuccess() = runTest {
        // 给定
        viewModel.updateAmount("100")
        viewModel.updateMerchant("星巴克")
        coEvery { transactionRepository.insert(any()) } returns 1
        viewModel.saveTransaction()
        
        // 验证有成功状态
        assertTrue(viewModel.uiState.value.saveSuccess)
        
        // 当
        viewModel.clearSuccess()
        
        // 则
        assertFalse(viewModel.uiState.value.saveSuccess)
    }
    
    @Test
    fun addTransactionViewModel_saveTransaction_expense_amountIsNegative() = runTest {
        // 给定
        viewModel.updateAmount("100")
        viewModel.updateMerchant("星巴克")
        viewModel.updateType(TransactionType.EXPENSE)
        
        var capturedAmount: Double? = null
        coEvery { transactionRepository.insert(any()) } answers {
            capturedAmount = firstArg<Transaction>().amount
            1L
        }
        
        // 当
        viewModel.saveTransaction()
        
        // 则
        assertNotNull(capturedAmount)
        assertTrue("支出金额应该为负数", capturedAmount!! < 0)
        assertEquals(-100.0, capturedAmount, 0.01)
    }
    
    @Test
    fun addTransactionViewModel_saveTransaction_income_amountIsPositive() = runTest {
        // 给定
        viewModel.updateAmount("100")
        viewModel.updateMerchant("工资")
        viewModel.updateType(TransactionType.INCOME)
        
        var capturedAmount: Double? = null
        coEvery { transactionRepository.insert(any()) } answers {
            capturedAmount = firstArg<Transaction>().amount
            1L
        }
        
        // 当
        viewModel.saveTransaction()
        
        // 则
        assertNotNull(capturedAmount)
        assertTrue("收入金额应该为正数", capturedAmount!! > 0)
        assertEquals(100.0, capturedAmount, 0.01)
    }
}
