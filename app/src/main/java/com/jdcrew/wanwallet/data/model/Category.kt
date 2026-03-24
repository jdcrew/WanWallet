package com.jdcrew.wanwallet.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 分类实体
 * 
 * @property id 分类 ID
 * @property name 分类名称
 * @property type 类型 (INCOME/EXPENSE)
 * @property icon 图标 (emoji 或 icon 名称)
 * @property keywords 关键词 (用于自动分类，逗号分隔)
 * @property parentId 父分类 ID (用于二级分类)
 * @property order 排序
 * @property isEnabled 是否启用
 */
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val name: String,
    
    val type: TransactionType,
    
    val icon: String,
    
    val keywords: String = "", // 逗号分隔的关键词
    
    val parentId: Long = 0, // 0 表示一级分类
    
    val order: Int = 0,
    
    val isEnabled: Boolean = true
)

// 预设分类
object PresetCategories {
    val EXPENSE_CATEGORIES = listOf(
        Category(name = "餐饮", type = TransactionType.EXPENSE, icon = "🍜", keywords = "餐厅，饭店，外卖，美食"),
        Category(name = "交通", type = TransactionType.EXPENSE, icon = "🚗", keywords = "地铁，公交，打车，加油"),
        Category(name = "购物", type = TransactionType.EXPENSE, icon = "🛍️", keywords = "淘宝，京东，商场，超市"),
        Category(name = "娱乐", type = TransactionType.EXPENSE, icon = "🎬", keywords = "电影，KTV，游戏"),
        Category(name = "住房", type = TransactionType.EXPENSE, icon = "🏠", keywords = "房租，房贷，物业"),
        Category(name = "医疗", type = TransactionType.EXPENSE, icon = "🏥", keywords = "医院，药店，体检"),
        Category(name = "教育", type = TransactionType.EXPENSE, icon = "📚", keywords = "培训，学费，书籍"),
        Category(name = "其他", type = TransactionType.EXPENSE, icon = "📦", keywords = ""),
    )
    
    val INCOME_CATEGORIES = listOf(
        Category(name = "工资", type = TransactionType.INCOME, icon = "💰", keywords = "工资，薪水，奖金"),
        Category(name = "理财", type = TransactionType.INCOME, icon = "📈", keywords = "利息，分红，收益"),
        Category(name = "兼职", type = TransactionType.INCOME, icon = "💼", keywords = "兼职，外快"),
        Category(name = "其他", type = TransactionType.INCOME, icon = "📦", keywords = ""),
    )
}
