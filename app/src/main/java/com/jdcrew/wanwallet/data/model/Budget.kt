package com.jdcrew.wanwallet.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 预算实体
 * 
 * @property id 预算 ID
 * @property categoryId 分类 ID (0 表示总预算)
 * @property amount 预算金额
 * @property period 周期 (WEEK/MONTH/YEAR)
 * @property spent 已花费金额
 * @property startDate 开始日期
 * @property endDate 结束日期
 * @property isEnabled 是否启用
 */
@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val categoryId: Long = 0, // 0 表示总预算
    
    val amount: Double,
    
    val period: BudgetPeriod,
    
    val spent: Double = 0.0,
    
    val startDate: Long,
    
    val endDate: Long,
    
    val isEnabled: Boolean = true
)

enum class BudgetPeriod {
    WEEK,   // 周
    MONTH,  // 月
    YEAR    // 年
}
