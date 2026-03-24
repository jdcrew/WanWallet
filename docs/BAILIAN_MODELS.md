# 百炼 LLM API 配置说明

**更新时间:** 2026-03-24 21:31

---

## 🔑 当前配置

| 项目 | 值 |
|------|-----|
| API Key | sk-sp-8dd0de13d53b4e5fa0a092dd8e9b751a |
| Model | qwen3.5-plus (推荐) |
| 状态 | ✅ 已配置 |

---

## 📊 可用模型对比

### 千问系列 (推荐)

| 模型 | 能力 | 推荐场景 | 成本 |
|------|------|----------|------|
| **qwen3.5-plus** ⭐ | 文本 + 深度思考 + 视觉 | 通用任务 | 中 |
| qwen3-max-2026-01-23 | 文本 + 深度思考 | 复杂推理 | 高 |
| qwen3-coder-next | 文本生成 | 代码生成 | 低 |
| qwen3-coder-plus | 文本生成 | 代码生成 | 中 |

### 第三方模型

| 模型 | 能力 | 推荐场景 |
|------|------|----------|
| glm-5 | 文本 + 深度思考 | 复杂任务 |
| glm-4.7 | 文本 + 深度思考 | 通用任务 |
| kimi-k2.5 | 文本 + 深度思考 + 视觉 | 多模态 |
| MiniMax-M2.5 | 文本 + 深度思考 | 通用任务 |

---

## 🎯 分类任务推荐配置

### 场景 1: 简单分类 (低成本)
```properties
bailian.model=qwen3-coder-next
```
- 适用：常见商户 (星巴克、肯德基等)
- 成本：低
- 准确率：85%+

### 场景 2: 通用分类 (推荐) ⭐
```properties
bailian.model=qwen3.5-plus
```
- 适用：大部分场景
- 成本：中
- 准确率：90%+

### 场景 3: 复杂分类 (高准确率)
```properties
bailian.model=qwen3-max-2026-01-23
```
- 适用：模糊商户、大额交易
- 成本：高
- 准确率：95%+

---

## 💰 成本估算

### 分类任务 Token 消耗
- **输入:** ~100 tokens (提示词 + 商户信息)
- **输出:** ~50 tokens (JSON 响应)
- **单次:** ~150 tokens

### 月度成本 (按 qwen3.5-plus 估算)
| 日调用量 | 月调用量 | 月成本 (估算) |
|----------|----------|---------------|
| 100 次 | 3,000 次 | ¥1-2 |
| 500 次 | 15,000 次 | ¥5-10 |
| 2000 次 | 60,000 次 | ¥20-40 |

---

## 🔧 切换模型

### 方式 1: 修改 local.properties
```properties
bailian.model=qwen3.5-plus
```

### 方式 2: 代码中指定
```kotlin
val client = BailianLLMClient(
    apiKey = "sk-xxx",
    model = "qwen3.5-plus"  // 或其他模型
)
```

---

## 🧪 测试建议

### 测试用例
```kotlin
// 测试常见商户
val testCases = listOf(
    "星巴克" to "餐饮",
    "滴滴出行" to "交通",
    "淘宝" to "购物",
    "电影院" to "娱乐"
)

for ((merchant, expected) in testCases) {
    val result = client.classifyTransaction(merchant, 50.0, categories)
    println("$merchant -> ${result.getOrNull()?.category}")
}
```

---

## ⚙️ 优化建议

### 混合策略 (推荐)
```kotlin
// 1. 先用规则引擎 (免费)
val ruleCategory = ruleEngine.match(merchant)

// 2. 规则匹配失败再用 LLM
if (ruleCategory == null) {
    val llmCategory = client.classify(merchant)
}

// 3. 大额交易用更强模型
if (amount > 1000) {
    val accurateCategory = client.classify(merchant, model="qwen3-max")
}
```

---

**当前配置:** ✅ qwen3.5-plus (性价比最优)
