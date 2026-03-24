# WanWallet 📱💰

一款智能钱包应用，支持自动记账、智能分类和多平台同步（Android → iOS）。

## ✨ 核心功能

- 🤖 **自动记账** - 监听支付通知，自动记录交易（微信/支付宝/云闪付）
- 🧠 **智能分类** - 大模型自动识别商户和消费类型
- 📊 **账单分析** - 多维度统计，AI 生成消费洞察
- 💳 **资产管理** - 支持多账户、多币种跟踪
- 🔒 **隐私安全** - 数据本地加密存储，可选云端同步
- 📱 **跨平台** - Android (首发) → iOS (后续)

## 🎯 项目愿景

- **零手动输入** - 支付后自动记录，无需手动记账
- **智能分析** - AI 识别消费类型，提供理财建议
- **多端同步** - Android/iOS 数据实时同步
- **开源透明** - 代码完全开源，接受社区监督

## 🛠️ 技术栈

### Android (Phase 1)
| 组件 | 技术 |
|------|------|
| 语言 | Kotlin |
| UI | Jetpack Compose |
| 数据库 | Room |
| 依赖注入 | Hilt |
| 后台服务 | AccessibilityService + NotificationListener |
| 架构 | MVVM + Clean Architecture |

### iOS (Phase 2 - 规划中)
| 组件 | 技术 |
|------|------|
| 语言 | Swift |
| UI | SwiftUI |
| 数据库 | CoreData / Realm |
| 架构 | MVVM + Clean Architecture |

### 跨平台同步 (Phase 3 - 规划中)
| 组件 | 技术选型 |
|------|----------|
| 后端 API | Node.js / Go |
| 数据库 | PostgreSQL |
| 实时同步 | WebSocket |
| 数据加密 | AES-256 |

## 📋 开发路线图

| 阶段 | 内容 | 预计时间 | 状态 |
|------|------|----------|------|
| Phase 0 | 项目初始化 | 1 天 | ✅ 完成 |
| Phase 1 | Android 基础框架 | 2 周 | 📋 待开始 |
| Phase 2 | Android 数据捕获 | 3 周 | 📋 待开始 |
| Phase 3 | Android 智能分类 | 2 周 | 📋 待开始 |
| Phase 4 | Android 分析功能 | 2 周 | 📋 待开始 |
| Phase 5 | Android 优化发布 | 1 周 | 📋 待开始 |
| Phase 6 | iOS 版本开发 | 6 周 | ⏳ 规划中 |
| Phase 7 | 跨平台同步 | 4 周 | ⏳ 规划中 |

## 🚀 快速开始

### 环境要求
- Android Studio Hedgehog 或更高版本
- JDK 17+
- Android SDK 26+ (最低), SDK 34 (目标)

### 构建步骤
```bash
# 克隆项目
git clone git@github.com:jdcrew/WanWallet.git
cd WanWallet

# 用 Android Studio 打开项目
# 或命令行构建
./gradlew assembleDebug
```

## 📱 平台支持

| 平台 | 状态 | 版本 |
|------|------|------|
| Android | 🚧 开发中 | v0.1.0-alpha |
| iOS | ⏳ 规划中 | - |

## 📄 许可证

MIT License - 详见 [LICENSE](LICENSE)

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📞 联系方式

- GitHub: [@jdcrew](https://github.com/jdcrew)
- 项目讨论：[Issues](https://github.com/jdcrew/WanWallet/issues)

---

**Star ⭐ 不迷路，开发进展第一时间通知！**
