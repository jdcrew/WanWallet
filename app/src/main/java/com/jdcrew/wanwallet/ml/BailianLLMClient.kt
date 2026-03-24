package com.jdcrew.wanwallet.ml

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * 百炼 LLM API 客户端
 * 
 * 用于智能分类交易
 */
class BailianLLMClient(
    private val apiKey: String,
    private val model: String = "qwen-coding-plan"
) {
    
    companion object {
        private const val API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation"
        private const val TIMEOUT = 10000 // 10 秒
    }
    
    /**
     * 分类交易
     * 
     * @param merchant 商户名称
     * @param amount 金额
     * @param existingCategories 现有分类列表
     * @return 分类名称和置信度
     */
    suspend fun classifyTransaction(
        merchant: String,
        amount: Double,
        existingCategories: List<String>
    ): Result<TransactionClassification> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildClassificationPrompt(merchant, amount, existingCategories)
            val response = callLLM(prompt)
            
            val classification = parseClassification(response, existingCategories)
            Result.success(classification)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 构建分类提示词
     */
    private fun buildClassificationPrompt(
        merchant: String,
        amount: Double,
        categories: List<String>
    ): String {
        return """
            你是一个智能记账助手，请根据商户名称判断消费分类。
            
            商户名称：$merchant
            交易金额：¥$amount
            可选分类：${categories.joinToString(", ")}
            
            请只返回 JSON 格式：
            {
                "category": "分类名称",
                "confidence": 0.95,
                "reason": "判断理由"
            }
            
            注意：只返回 JSON，不要其他内容。
        """.trimIndent()
    }
    
    /**
     * 调用 LLM API
     */
    private fun callLLM(prompt: String): String {
        val connection = URL(API_URL).openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.connectTimeout = TIMEOUT
        connection.readTimeout = TIMEOUT
        
        // 设置请求头
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Authorization", "Bearer $apiKey")
        
        // 构建请求体
        val requestBody = JSONObject().apply {
            put("model", model)
            put("input", JSONObject().apply {
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", "你是一个专业的记账分类助手。")
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                })
            })
            put("parameters", JSONObject().apply {
                put("temperature", 0.1) // 低温度，更确定
                put("max_tokens", 200)
            })
        }
        
        // 发送请求
        connection.doOutput = true
        OutputStreamWriter(connection.outputStream).use { writer ->
            writer.write(requestBody.toString())
        }
        
        // 读取响应
        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            return connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "Unknown error"
            throw RuntimeException("API call failed: $responseCode - $errorBody")
        }
    }
    
    /**
     * 解析分类结果
     */
    private fun parseClassification(
        response: String,
        categories: List<String>
    ): TransactionClassification {
        val jsonResponse = JSONObject(response)
        val output = jsonResponse.getJSONObject("output")
        val text = output.getString("text")
        
        // 提取 JSON 部分
        val jsonStart = text.indexOf('{')
        val jsonEnd = text.lastIndexOf('}') + 1
        val jsonStr = if (jsonStart >= 0 && jsonEnd > jsonStart) {
            text.substring(jsonStart, jsonEnd)
        } else {
            text
        }
        
        val result = JSONObject(jsonStr)
        val category = result.getString("category")
        val confidence = result.getDouble("confidence")
        val reason = result.optString("reason", "")
        
        // 验证分类是否在列表中
        val matchedCategory = categories.find { it.equals(category, ignoreCase = true) }
            ?: categories.firstOrNull() ?: "其他"
        
        return TransactionClassification(
            category = matchedCategory,
            confidence = confidence,
            reason = reason
        )
    }
}

/**
 * 交易分类结果
 */
data class TransactionClassification(
    val category: String,
    val confidence: Double,
    val reason: String
)
