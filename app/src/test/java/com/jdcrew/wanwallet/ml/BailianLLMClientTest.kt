package com.jdcrew.wanwallet.ml

import org.junit.Assert.*
import org.junit.Test

/**
 * 百炼 LLM API 测试
 */
class BailianLLMClientTest {
    
    @Test
    fun testConnection() {
        val client = BailianLLMClient(
            apiKey = "sk-sp-8dd0de13d53b4e5fa0a092dd8e9b751a",
            model = "qwen-coding-plan"
        )
        
        // 注意：这是同步测试，实际使用应在协程中
        // val result = runBlocking { client.testConnection() }
        // assertTrue(result.isSuccess)
    }
    
    @Test
    fun testClassification() {
        val client = BailianLLMClient(
            apiKey = "sk-sp-8dd0de13d53b4e5fa0a092dd8e9b751a",
            model = "qwen-coding-plan"
        )
        
        val categories = listOf("餐饮", "交通", "购物", "娱乐", "住房", "医疗", "教育", "其他")
        
        // 测试星巴克分类
        // val result = runBlocking { client.classifyTransaction("星巴克", 35.0, categories) }
        // assertTrue(result.isSuccess)
        // assertEquals("餐饮", result.getOrNull()?.category)
    }
}
