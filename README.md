# AutoLedger 📱💰

一款高权限、低成本的 Android 自动记账应用，利用大模型能力进行智能分类和账单分析。

## ✨ 核心功能

- 🤖 **自动记账** - 监听微信/支付宝/云闪付支付通知，自动记录交易
- 🧠 **智能分类** - 大模型自动识别商户和消费类型
- 📊 **账单分析** - 多维度统计，AI 生成消费洞察
- 🔒 **隐私安全** - 数据本地存储，可选加密备份

## 🎯 项目目标

- **零手动输入** - 支付后自动记录，无需手动记账
- **智能分类** - AI 识别消费类型，准确率持续优化
- **极低成本** - 无订阅费，可选本地模型零 API 成本
- **开源透明** - 代码完全开源，接受社区监督

## 🛠️ 技术栈

| 组件 | 技术 |
|------|------|
| 语言 | Kotlin |
| UI | Jetpack Compose |
| 数据库 | Room |
| 依赖注入 | Hilt |
| 后台服务 | AccessibilityService + NotificationListener |
| 架构 | MVVM + Clean Architecture |

## 📋 开发进度

- [x] 项目规划
- [x] 仓库创建
- [ ] Phase 1: 基础框架
- [ ] Phase 2: 数据捕获
- [ ] Phase 3: 智能分类
- [ ] Phase 4: 分析功能
- [ ] Phase 5: 优化发布

## 🚀 快速开始

### 环境要求
- Android Studio Hedgehog 或更高版本
- JDK 17+
- Android SDK 26+ (最低), SDK 34 (目标)

### 构建步骤
```bash
# 克隆项目
git clone https://github.com/jdcrew/auto-ledger-android.git
cd auto-ledger-android

# 用 Android Studio 打开项目
# 或命令行构建
./gradlew assembleDebug
```

## 📄 许可证

MIT License - 详见 [LICENSE](LICENSE)

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📞 联系方式

- GitHub: [@jdcrew](https://github.com/jdcrew)
- 项目讨论：[Issues](https://github.com/jdcrew/auto-ledger-android/issues)

---

**Star ⭐ 不迷路，开发进展第一时间通知！**
