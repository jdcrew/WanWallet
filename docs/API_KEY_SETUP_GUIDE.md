# ⚠️ 百炼 API Key 无效 - 获取指南

**测试时间:** 2026-03-24 21:38  
**状态:** ❌ API Key 无效 (401 错误)

---

## ❌ 当前问题

**API Key:** `sk-sp-8dd0de13d53b4e5fa0a092dd8e9b751a`  
**错误:** `InvalidApiKey`  
**原因:** API Key 无效、过期或未激活

---

## 🔑 获取正确的百炼 API Key

### 步骤 1: 访问百炼控制台

**URL:** https://bailian.console.aliyun.com/

---

### 步骤 2: 登录阿里云账号

- 使用阿里云账号登录
- 如果没有账号，先注册 (免费)

---

### 步骤 3: 开通百炼服务

1. 在控制台找到"百炼"服务
2. 点击"开通服务"
3. 同意服务协议
4. 完成实名认证 (需要)

---

### 步骤 4: 创建 API Key

1. 进入"API-KEY 管理"页面
2. 点击"创建新密钥"
3. 复制生成的 API Key
   - 格式：`sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`
   - **重要:** 只显示一次，立即保存！

---

### 步骤 5: 充值/绑定支付方式

1. 进入"费用中心"
2. 充值或绑定支付宝/银行卡
3. 确保有可用额度 (至少¥10)

---

### 步骤 6: 验证 API Key

**测试命令:**
```bash
curl -X POST "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_NEW_API_KEY" \
  -d '{
    "model": "qwen3.5-plus",
    "input": {
      "messages": [
        {"role": "user", "content": "你好"}
      ]
    }
  }'
```

**预期响应:**
```json
{
  "output": {
    "text": "你好！有什么可以帮助你的吗？"
  }
}
```

---

## 🔧 常见问题

### Q1: API Key 无效
**原因:** 
- Key 复制错误 (多了空格)
- Key 已过期
- Key 未激活

**解决:**
- 重新复制，确保无空格
- 创建新的 API Key
- 确认服务已开通

---

### Q2: 余额不足
**原因:** 账户余额为 0

**解决:**
- 充值至少¥10
- 或绑定支付方式

---

### Q3: 模型不可用
**原因:** 该模型未开通

**解决:**
- 在模型列表开通对应模型
- 或切换到可用模型 (如 qwen3-coder-plus)

---

## 📋 可用模型列表

根据您提供的订阅套餐，可用模型：

| 模型 | 推荐场景 |
|------|----------|
| qwen3.5-plus ⭐ | 通用任务 (推荐) |
| qwen3-max-2026-01-23 | 复杂推理 |
| qwen3-coder-next | 代码生成 |
| qwen3-coder-plus | 代码生成 |
| glm-5 | 通用任务 |
| glm-4.7 | 通用任务 |
| kimi-k2.5 | 多模态 |
| MiniMax-M2.5 | 通用任务 |

---

## ✅ 配置新 API Key

获取新 API Key 后：

### 方式 1: 更新 local.properties
```properties
bailian.api.key=sk-你的新 APIKey
bailian.model=qwen3.5-plus
```

### 方式 2: 直接告诉我
提供新的 API Key，我帮您更新配置

---

## 📞 需要帮助？

**官方文档:**
- 百炼控制台：https://bailian.console.aliyun.com/
- API 文档：https://help.aliyun.com/zh/dashscope/

**获取帮助:**
1. 检查上述步骤
2. 查看错误信息
3. 联系阿里云客服

---

**获取新 API Key 后告诉我，我立即更新配置！** 🔑
