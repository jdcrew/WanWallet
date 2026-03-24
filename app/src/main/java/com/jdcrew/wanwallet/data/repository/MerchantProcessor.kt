package com.jdcrew.wanwallet.data.repository

import javax.inject.Inject
import javax.inject.Singleton

/**
 * 商户名称处理器
 * 
 * 清洗和标准化商户名称，提高分类准确率
 */
@Singleton
class MerchantProcessor @Inject constructor() {
    
    companion object {
        // 需要移除的常见后缀
        private val SUFFIXES_TO_REMOVE = listOf(
            "店", "店铺", "商家", "公司", "有限公司", "有限责任公司",
            "广场", "商城", "中心", "步行街", "专柜", "柜台"
        )
        
        // 需要标准化的词
        private val NORMALIZE_MAP = mapOf(
            "星巴克" to "星巴克",
            "Starbucks" to "星巴克",
            "星巴克咖啡" to "星巴克",
            "肯德基" to "肯德基",
            "KFC" to "肯德基",
            "麦当劳" to "麦当劳",
            "McDonald" to "麦当劳",
            "海底捞" to "海底捞",
            "海底捞火锅" to "海底捞"
        )
    }
    
    /**
     * 清洗商户名称
     */
    fun cleanMerchant(merchant: String): String {
        var cleaned = merchant.trim()
        
        // 移除常见后缀
        SUFFIXES_TO_REMOVE.forEach { suffix ->
            if (cleaned.endsWith(suffix)) {
                cleaned = cleaned.removeSuffix(suffix).trim()
            }
        }
        
        return cleaned
    }
    
    /**
     * 标准化商户名称
     */
    fun normalizeMerchant(merchant: String): String {
        val cleaned = cleanMerchant(merchant)
        
        // 查找标准化映射
        for ((variant, standard) in NORMALIZE_MAP) {
            if (cleaned.contains(variant, ignoreCase = true)) {
                return standard
            }
        }
        
        return cleaned
    }
    
    /**
     * 提取商户核心名称
     * 例如："星巴克北京路店" -> "星巴克"
     */
    fun extractCoreName(merchant: String): String {
        val normalized = normalizeMerchant(merchant)
        
        // 如果已经标准化，直接返回
        if (NORMALIZE_MAP.containsValue(normalized)) {
            return normalized
        }
        
        // 否则返回清洗后的名称
        return normalized
    }
    
    /**
     * 批量处理商户名称
     */
    fun processMerchants(merchants: List<String>): Map<String, String> {
        return merchants.associateWith { merchant ->
            normalizeMerchant(merchant)
        }
    }
}
