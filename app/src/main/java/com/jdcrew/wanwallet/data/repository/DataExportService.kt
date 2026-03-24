package com.jdcrew.wanwallet.data.repository

import android.content.Context
import com.jdcrew.wanwallet.data.model.Transaction
import com.jdcrew.wanwallet.data.model.TransactionType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 数据导出服务
 * 
 * 导出交易数据为 CSV/Excel 格式
 */
@Singleton
class DataExportService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val FILENAME_PREFIX = "wanwallet_export_"
        private const val DATE_FORMAT = "yyyy-MM-dd_HHmmss"
    }
    
    /**
     * 导出交易数据为 CSV
     */
    suspend fun exportToCsv(
        transactions: List<Transaction>,
        filename: String? = null
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val exportFilename = filename ?: generateFilename()
            val file = File(context.getExternalFilesDir(null), exportFilename)
            
            FileWriter(file).use { writer ->
                // 写入表头
                writer.appendLine("日期，类型，金额，分类，商户，支付渠道，备注")
                
                // 写入数据
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
                transactions.forEach { transaction ->
                    val date = dateFormat.format(Date(transaction.time))
                    val type = if (transaction.type == TransactionType.INCOME) "收入" else "支出"
                    val amount = kotlin.math.abs(transaction.amount)
                    val channel = when (transaction.channel) {
                        com.jdcrew.wanwallet.data.model.PaymentChannel.WECHAT -> "微信"
                        com.jdcrew.wanwallet.data.model.PaymentChannel.ALIPAY -> "支付宝"
                        com.jdcrew.wanwallet.data.model.PaymentChannel.UNIONPAY -> "云闪付"
                        com.jdcrew.wanwallet.data.model.PaymentChannel.CASH -> "现金"
                        com.jdcrew.wanwallet.data.model.PaymentChannel.CARD -> "银行卡"
                        else -> "其他"
                    }
                    
                    writer.appendLine("$date,$type,$amount,分类${transaction.categoryId},${transaction.merchant},$channel,${transaction.note}")
                }
            }
            
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 导出为 Excel 格式 (简化版本，实际可使用 Apache POI)
     */
    suspend fun exportToExcel(
        transactions: List<Transaction>,
        filename: String? = null
    ): Result<File> = withContext(Dispatchers.IO) {
        // 简化版本：实际导出 CSV，但使用.xls 扩展名
        exportToCsv(transactions, filename ?: generateFilename().replace(".csv", ".xls"))
    }
    
    private fun generateFilename(): String {
        val timestamp = SimpleDateFormat(DATE_FORMAT, Locale.CHINA).format(Date())
        return "$FILENAME_PREFIX$timestamp.csv"
    }
    
    /**
     * 获取导出文件路径
     */
    fun getExportDirectory(): File {
        return context.getExternalFilesDir(null) ?: context.filesDir
    }
    
    /**
     * 清理旧导出文件
     */
    suspend fun cleanupOldExports(daysToKeep: Int = 30): Int = withContext(Dispatchers.IO) {
        val directory = getExportDirectory()
        val cutoffTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
        var deletedCount = 0
        
        directory.listFiles { file ->
            file.name.startsWith(FILENAME_PREFIX)
        }?.forEach { file ->
            if (file.lastModified() < cutoffTime) {
                file.delete()
                deletedCount++
            }
        }
        
        deletedCount
    }
}
