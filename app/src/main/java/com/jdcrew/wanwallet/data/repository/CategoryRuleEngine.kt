package com.jdcrew.wanwallet.data.repository

import com.jdcrew.wanwallet.data.model.Category
import com.jdcrew.wanwallet.data.model.TransactionType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 分类规则引擎
 * 
 * 根据商户名称、金额等信息自动匹配分类
 */
@Singleton
class CategoryRuleEngine @Inject constructor() {
    
    companion object {
        // 商户关键词到分类的映射
        private val MERCHANT_RULES = mapOf(
            // 餐饮
            "餐饮" to listOf("星巴克", "肯德基", "麦当劳", "海底捞", "必胜客", "餐厅", "饭店", "外卖", "美团", "饿了么"),
            // 交通
            "交通" to listOf("滴滴", "Uber", "出租车", "地铁", "公交", "加油", "中石化", "中石油", "停车"),
            // 购物
            "购物" to listOf("淘宝", "京东", "拼多多", "超市", "商场", "沃尔玛", "家乐福", "天猫"),
            // 娱乐
            "娱乐" to listOf("电影", "KTV", "游戏", "Netflix", "Spotify", "视频", "音乐"),
            // 住房
            "住房" to listOf("房租", "房贷", "物业", "水电", "燃气", "宽带"),
            // 医疗
            "医疗" to listOf("医院", "药店", "诊所", "体检", "药房"),
            // 教育
            "教育" to listOf("培训", "学费", "学校", "课程", "书籍", "图书"),
            // 工资
            "工资" to listOf("工资", "薪水", "奖金", "提成", "津贴"),
            // 理财
            "理财" to listOf("利息", "分红", "收益", "基金", "股票")
        )
    }
    
    /**
     * 根据商户名称自动匹配分类
     */
    fun matchCategory(merchant: String, categories: List<Category>): Category? {
        val merchantLower = merchant.lowercase()
        
        // 遍历规则，查找匹配
        for ((categoryName, keywords) in MERCHANT_RULES) {
            if (keywords.any { keyword -> merchantLower.contains(keyword.lowercase()) }) {
                // 在分类列表中查找对应分类
                return categories.find { 
                    it.name == categoryName && it.isEnabled 
                }
            }
        }
        
        // 无匹配，返回默认分类
        return categories.find { 
            it.name == "其他" && it.type == TransactionType.EXPENSE && it.isEnabled 
        }
    }
    
    /**
     * 批量匹配分类
     */
    fun matchCategories(
        merchants: List<String>,
        categories: List<Category>
    ): Map<String, Category?> {
        return merchants.associateWith { merchant ->
            matchCategory(merchant, categories)
        }
    }
    
    /**
     * 添加自定义规则
     */
    fun addRule(categoryName: String, keywords: List<String>) {
        // TODO: 保存到用户自定义规则库
    }
    
    /**
     * 移除规则
     */
    fun removeRule(categoryName: String, keyword: String) {
        // TODO: 从用户自定义规则库移除
    }
}
