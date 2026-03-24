package com.jdcrew.wanwallet.util

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

/**
 * 性能监控工具
 */
object PerformanceMonitor {
    
    private const val TAG = "WanWallet_Perf"
    private val enableLogging = BuildConfig.DEBUG
    
    /**
     * 测量代码块执行时间
     */
    suspend fun <T> measureAsync(
        operation: String,
        block: suspend () -> T
    ): T {
        var result: T
        val time = measureTimeMillis {
            result = block()
        }
        
        if (enableLogging && time > 16) { // 超过一帧时间才记录
            Log.d(TAG, "$operation took ${time}ms")
        }
        
        return result
    }
    
    /**
     * 测量代码块执行时间 (同步)
     */
    fun <T> measure(
        operation: String,
        block: () -> T
    ): T {
        var result: T
        val time = measureTimeMillis {
            result = block()
        }
        
        if (enableLogging && time > 16) {
            Log.d(TAG, "$operation took ${time}ms")
        }
        
        return result
    }
    
    /**
     * 检查是否在主线程
     */
    fun checkMainThread() {
        if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) {
            Log.w(TAG, "Called from main thread!")
        }
    }
}

/**
 * 列表分页加载助手
 */
class PaginationHelper<T>(
    private val pageSize: Int = 20,
    private val loadPage: suspend (Int) -> List<T>
) {
    private val allItems = mutableListOf<T>()
    private var currentPage = 0
    private var hasMore = true
    
    suspend fun loadNextPage(): List<T> {
        if (!hasMore) return emptyList()
        
        val newItems = loadPage(currentPage)
        if (newItems.size < pageSize) {
            hasMore = false
        }
        
        allItems.addAll(newItems)
        currentPage++
        
        return newItems
    }
    
    fun reset() {
        allItems.clear()
        currentPage = 0
        hasMore = true
    }
    
    fun getAllItems(): List<T> = allItems.toList()
}
