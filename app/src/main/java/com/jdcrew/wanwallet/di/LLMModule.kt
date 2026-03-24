package com.jdcrew.wanwallet.di

import com.jdcrew.wanwallet.ml.BailianLLMClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.io.File
import java.util.Properties
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LLMModule {
    
    @Provides
    @Singleton
    fun provideBailianLLMClient(): BailianLLMClient {
        val properties = Properties()
        
        // 从 local.properties 读取配置
        try {
            val localPropertiesFile = File("local.properties")
            if (localPropertiesFile.exists()) {
                localPropertiesFile.inputStream().use { input ->
                    properties.load(input)
                }
            }
        } catch (e: Exception) {
            // 文件不存在或读取失败
        }
        
        // 优先级：环境变量 > local.properties > 默认值
        val apiKey = System.getenv("BAILOUAN_API_KEY")
            ?: properties.getProperty("bailian.api.key")
            ?: "sk-placeholder"
        
        val model = System.getenv("BAILOUAN_MODEL")
            ?: properties.getProperty("bailian.model")
            ?: "qwen-coding-plan"
        
        return BailianLLMClient(apiKey = apiKey, model = model)
    }
}
