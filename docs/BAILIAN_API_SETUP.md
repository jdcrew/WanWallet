# 百炼 LLM API 配置说明

## 1. 获取 API Key

### 步骤
1. 访问阿里云百炼平台：https://bailian.console.aliyun.com/
2. 登录账号
3. 进入"API 管理"页面
4. 创建或复制 API Key

### 模型选择
- **推荐:** `qwen-coding-plan` (编程计划专用模型)
- **备选:** `qwen-max`, `qwen-plus`

## 2. 配置方式

### 方式 1: 环境变量 (推荐开发环境)
```bash
export BAILOUAN_API_KEY="sk-your-api-key"
export BAILOUAN_MODEL="qwen-coding-plan"
```

### 方式 2: local.properties (推荐团队开发)
```properties
bailian.api.key=sk-your-api-key
bailian.model=qwen-coding-plan
```

### 方式 3: BuildConfig (推荐生产环境)
在 `build.gradle.kts` 中:
```kotlin
android {
    defaultConfig {
        buildConfigField("String", "BAILIAN_API_KEY", "\"sk-your-api-key\"")
        buildConfigField("String", "BAILIAN_MODEL", "\"qwen-coding-plan\"")
    }
}
```

## 3. 测试连接

### 测试脚本
```bash
curl -X POST https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation \
  -H "Authorization: Bearer sk-your-api-key" \
  -H "Content-Type: application/json" \
  -d '{
    "model": "qwen-coding-plan",
    "input": {
      "messages": [
        {"role": "user", "content": "你好"}
      ]
    }
  }'
```

### 预期响应
```json
{
  "output": {
    "text": "你好！有什么可以帮助你的吗？"
  }
}
```

## 4. 成本估算

### 定价 (参考)
- `qwen-coding-plan`: ¥0.002 / 1K tokens
- `qwen-max`: ¥0.04 / 1K tokens
- `qwen-plus`: ¥0.008 / 1K tokens

### 月度成本预估
| 场景 | 日调用量 | 月成本 |
|------|----------|--------|
| 轻度使用 | 100 次 | ¥6 |
| 中度使用 | 500 次 | ¥30 |
| 重度使用 | 2000 次 | ¥120 |

## 5. 优化建议

### 降低成本
1. **混合策略:** 规则引擎优先，LLM 辅助
2. **缓存结果:** 相同商户不重复调用
3. **批量处理:** 合并多个分类请求

### 提升准确率
1. **提示词优化:** 提供分类示例
2. **用户反馈:** 记录修正行为
3. **持续学习:** 定期优化提示词

## 6. 错误处理

### 常见错误
| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| 401 | API Key 无效 | 检查 Key 是否正确 |
| 429 | 请求限流 | 降低调用频率 |
| 500 | 服务器错误 | 重试或降级 |
| 503 | 服务不可用 | 使用本地规则引擎 |

### 降级策略
```kotlin
try {
    // 尝试 LLM 分类
    val result = llmClient.classify(...)
} catch (e: Exception) {
    // 降级到规则引擎
    val result = ruleEngine.classify(...)
}
```

## 7. 安全建议

1. **不要硬编码:** API Key 不要提交到 Git
2. **使用环境变量:** 开发环境使用环境变量
3. **定期轮换:** 定期更换 API Key
4. **访问控制:** 限制 API Key 使用范围

---

**配置完成后，运行测试验证连接！**
