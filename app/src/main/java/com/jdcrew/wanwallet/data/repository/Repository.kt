package com.jdcrew.wanwallet.data.repository

import com.jdcrew.wanwallet.data.local.CategoryDao
import com.jdcrew.wanwallet.data.local.TransactionDao
import com.jdcrew.wanwallet.data.model.Category
import com.jdcrew.wanwallet.data.model.PresetCategories
import com.jdcrew.wanwallet.data.model.Transaction
import com.jdcrew.wanwallet.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    
    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByType(type)
    }
    
    suspend fun getTransactionById(id: Long): Transaction? {
        return transactionDao.getTransactionById(id)
    }
    
    suspend fun insert(transaction: Transaction): Long {
        return transactionDao.insert(transaction)
    }
    
    suspend fun update(transaction: Transaction) {
        transactionDao.update(transaction)
    }
    
    suspend fun delete(transaction: Transaction) {
        transactionDao.delete(transaction)
    }
    
    suspend fun deleteById(id: Long) {
        transactionDao.deleteById(id)
    }
}

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()
    
    fun getCategoriesByType(type: TransactionType): Flow<List<Category>> {
        return categoryDao.getCategoriesByType(type)
    }
    
    suspend fun getCategoryById(id: Long): Category? {
        return categoryDao.getCategoryById(id)
    }
    
    suspend fun insert(category: Category): Long {
        return categoryDao.insert(category)
    }
    
    suspend fun update(category: Category) {
        categoryDao.update(category)
    }
    
    suspend fun delete(category: Category) {
        categoryDao.delete(category)
    }
    
    /**
     * 初始化预设分类
     * 首次启动时调用
     */
    suspend fun initializePresetCategories() {
        val existing = categoryDao.getAllCategories().firstOrNull()
        if (existing.isNullOrEmpty()) {
            // 插入预设分类
            (PresetCategories.EXPENSE_CATEGORIES + PresetCategories.INCOME_CATEGORIES)
                .forEach { categoryDao.insert(it) }
        }
    }
}
