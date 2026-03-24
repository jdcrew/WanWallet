package com.jdcrew.wanwallet.di

import com.jdcrew.wanwallet.ml.BailianLLMClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LLMModule {
    
    @Provides
    @Singleton
    fun provideBailianLLMClient(): BailianLLMClient {
        // TODO: 从配置文件读取 API Key
        // 临时使用环境变量或 BuildConfig
        val apiKey = System.getenv("BAILOUAN_API_KEY") ?: "sk-your-api-key-here"
        val model = System.getenv("BAILOUAN_MODEL") ?: "qwen-coding-plan"
        
        return BailianLLMClient(apiKey = apiKey, model = model)
    }
}
