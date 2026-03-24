package com.jdcrew.wanwallet.data.repository

import com.jdcrew.wanwallet.data.local.TransactionDao
import com.jdcrew.wanwallet.data.model.Transaction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 交易捕获处理器
 * 
 * 处理来自通知监听和无障碍服务的交易数据
 */
@Singleton
class TransactionCaptureHandler @Inject constructor(
    private val transactionDao: TransactionDao
) {
    
    /**
     * 保存交易记录
     */
    suspend fun saveTransaction(transaction: Transaction): Long {
        return transactionDao.insert(transaction)
    }
    
    /**
     * 批量保存交易
     */
    suspend fun saveTransactions(transactions: List<Transaction>): List<Long> {
        return transactions.map { transaction ->
            transactionDao.insert(transaction)
        }
    }
    
    /**
     * 获取所有交易
     */
    fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions()
    }
    
    /**
     * 根据 ID 获取交易
     */
    suspend fun getTransactionById(id: Long): Transaction? {
        return transactionDao.getTransactionById(id)
    }
    
    /**
     * 更新交易
     */
    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.update(transaction)
    }
    
    /**
     * 删除交易
     */
    suspend fun deleteTransaction(id: Long) {
        transactionDao.deleteById(id)
    }
}
