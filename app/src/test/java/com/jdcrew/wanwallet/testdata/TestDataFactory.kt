package com.jdcrew.wanwallet.testdata

import com.jdcrew.wanwallet.data.model.*
import java.util.*

/**
 * 测试数据工厂
 * 
 * 用于统一创建测试数据，避免硬编码
 */
object TestDataFactory {
    
    /**
     * 创建测试分类
     */
    fun makeCategory(
        id: Long = 1,
        name: String = "餐饮",
        type: TransactionType = TransactionType.EXPENSE,
        icon: String = "🍜",
        keywords: String = "餐厅，饭店",
        parentId: Long = 0,
        order: Int = 0,
        isEnabled: Boolean = true
    ): Category {
        return Category(
            id = id,
            name = name,
            type = type,
            icon = icon,
            keywords = keywords,
            parentId = parentId,
            order = order,
            isEnabled = isEnabled
        )
    }
    
    /**
     * 创建预设分类列表
     */
    fun makePresetCategories(): List<Category> {
        return listOf(
            makeCategory(id = 1, name = "餐饮", icon = "🍜", keywords = "餐厅，饭店，外卖"),
            makeCategory(id = 2, name = "交通", icon = "🚗", keywords = "地铁，公交，打车"),
            makeCategory(id = 3, name = "购物", icon = "🛍️", keywords = "淘宝，京东，商场"),
            makeCategory(id = 4, name = "娱乐", icon = "🎬", keywords = "电影，KTV"),
            makeCategory(id = 5, name = "住房", icon = "🏠", keywords = "房租，物业"),
            makeCategory(id = 6, name = "其他", icon = "📦", keywords = "")
        )
    }
    
    /**
     * 创建测试交易
     */
    fun makeTransaction(
        id: Long = 1,
        amount: Double = 100.0,
        type: TransactionType = TransactionType.EXPENSE,
        categoryId: Long = 1,
        merchant: String = "星巴克",
        time: Long = System.currentTimeMillis(),
        channel: PaymentChannel = PaymentChannel.WECHAT,
        note: String = "",
        isAuto: Boolean = true
    ): Transaction {
        return Transaction(
            id = id,
            amount = if (type == TransactionType.EXPENSE) -kotlin.math.abs(amount) else amount,
            type = type,
            categoryId = categoryId,
            merchant = merchant,
            time = time,
            channel = channel,
            note = note,
            isAuto = isAuto
        )
    }
    
    /**
     * 创建测试交易列表
     */
    fun makeTransactionList(count: Int = 10): List<Transaction> {
        return (1..count).map { i ->
            makeTransaction(
                id = i.toLong(),
                amount = (10..500).random().toDouble(),
                time = System.currentTimeMillis() - (i * 24 * 60 * 60 * 1000), // 每天一笔
                categoryId = (1..6).random().toLong(),
                merchant = listOf("星巴克", "滴滴出行", "淘宝", "电影院", "超市").random()
            )
        }
    }
    
    /**
     * 创建测试预算
     */
    fun makeBudget(
        id: Long = 1,
        categoryId: Long = 0,
        amount: Double = 1000.0,
        period: BudgetPeriod = BudgetPeriod.MONTH,
        spent: Double = 0.0,
        startDate: Long = System.currentTimeMillis(),
        endDate: Long = System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000,
        isEnabled: Boolean = true
    ): Budget {
        return Budget(
            id = id,
            categoryId = categoryId,
            amount = amount,
            period = period,
            spent = spent,
            startDate = startDate,
            endDate = endDate,
            isEnabled = isEnabled
        )
    }
    
    /**
     * 创建测试预算列表
     */
    fun makeBudgetList(count: Int = 5): List<Budget> {
        val categories = makePresetCategories()
        return (1..count).map { i ->
            makeBudget(
                id = i.toLong(),
                categoryId = if (i == 1) 0 else categories[i % categories.size].id,
                amount = (500..5000).random().toDouble(),
                spent = (0..3000).random().toDouble()
            )
        }
    }
    
    /**
     * 创建指定日期的交易
     */
    fun makeTransactionForDate(
        date: String, // "yyyy-MM-dd"
        amount: Double = 100.0,
        type: TransactionType = TransactionType.EXPENSE
    ): Transaction {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
        val time = dateFormat.parse(date)?.time ?: System.currentTimeMillis()
        
        return makeTransaction(
            amount = amount,
            type = type,
            time = time
        )
    }
    
    /**
     * 创建每日趋势测试数据
     */
    fun makeDailyTrendData(
        startDate: String,
        days: Int = 7
    ): List<Pair<String, Pair<Double, Double>>> {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
        val calendar = Calendar.getInstance()
        calendar.time = dateFormat.parse(startDate) ?: Date()
        
        return (0 until days).map { day ->
            val dateStr = dateFormat.format(calendar.time)
            val income = (0..500).random().toDouble()
            val expense = (0..500).random().toDouble()
            
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            dateStr to (income to expense)
        }
    }
}
