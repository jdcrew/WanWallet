# WanWallet 应用截图准备清单

**创建日期:** 2026-03-28  
**用途:** Google Play 发布所需截图

---

## 📱 Google Play 截图要求

### 基本要求
| 项目 | 要求 |
|------|------|
| 最少数量 | 2 张 |
| 推荐数量 | 5-8 张 |
| 分辨率 | 至少 320px，不超过 3840px |
| 宽高比 | 16:9 或 9:16 (手机竖屏) |
| 格式 | PNG 或 JPG |
| 语言 | 与应用语言一致 (中文 + 英文) |

### 手机截图 (必需)
- **尺寸:** 1080 x 1920 (推荐)
- **方向:** 竖屏
- **数量:** 5-8 张

### 平板截图 (可选)
- **尺寸:** 1920 x 1080 或更高
- **方向:** 横屏或竖屏
- **数量:** 0-8 张

---

## 📸 推荐截图清单

### 必选截图 (5 张)

| 序号 | 页面 | 说明 | 重点展示 |
|------|------|------|----------|
| 1 | 主页 | 应用主界面 | 简洁设计、收支概览 |
| 2 | 交易列表 | 交易记录列表 | 自动记账、分类显示 |
| 3 | 添加交易 | 手动记账页面 | 快速输入、智能分类 |
| 4 | 统计图表 | 收支分析页面 | 分类占比、趋势图 |
| 5 | 预算管理 | 预算设置页面 | 预算进度、超支提醒 |

### 可选截图 (3 张)

| 序号 | 页面 | 说明 | 重点展示 |
|------|------|------|----------|
| 6 | 权限设置 | 权限引导页面 | 无障碍服务、通知监听 |
| 7 | 数据导出 | 导出功能页面 | CSV/Excel 导出 |
| 8 | 设置页面 | 应用设置 | 主题、备份、关于 |

---

## 🎨 截图美化建议

### 设备框架
- 使用手机外框增加专业感
- 推荐工具: [Mockup Generator](https://mockupgenerator.com/)
- 或 Android Studio 内置框架

### 标注说明
- 添加简短文字说明 (可选)
- 使用箭头/高亮突出功能点
- 保持风格统一

### 背景
- 使用纯色或渐变背景
- 避免复杂图案干扰
- 与应用主题色协调 (绿色 #4CAF50)

---

## 📷 截图步骤

### 方法 1: 模拟器截图 (推荐)

1. **启动 Android 模拟器**
   ```bash
   # Android Studio → AVD Manager → 启动模拟器
   ```

2. **安装应用**
   ```bash
   adb install app/build/outputs/apk/release/app-release.apk
   ```

3. **导航到目标页面**

4. **截取截图**
   ```bash
   # 命令行截图
   adb shell screencap -p /sdcard/screenshot.png
   adb pull /sdcard/screenshot.png ~/Desktop/wanwallet-home.png
   
   # 或使用 Ctrl+S (模拟器快捷键)
   ```

### 方法 2: 真机截图

1. **连接设备**
   ```bash
   adb devices
   ```

2. **安装应用**
   ```bash
   adb install app-release.apk
   ```

3. **手动截图** (音量下 + 电源键)

4. **传输到电脑**
   ```bash
   adb pull /sdcard/Pictures/Screenshots/
   ```

### 方法 3: Android Studio 截图工具

1. **打开 Logcat**
2. **点击相机图标** 📷
3. **保存截图**

---

## 🖼️ 截图文件命名

推荐命名规范:
```
wanwallet-01-home.png
wanwallet-02-transactions.png
wanwallet-03-add-transaction.png
wanwallet-04-stats.png
wanwallet-05-budget.png
wanwallet-06-permissions.png (可选)
wanwallet-07-export.png (可选)
wanwallet-08-settings.png (可选)
```

---

## 📋 截图检查清单

### 内容检查
- [ ] 主页显示收支概览
- [ ] 交易列表显示分类名称 (不是 ID)
- [ ] 统计图表显示真实数据
- [ ] 预算页面显示分类图标
- [ ] 所有页面使用中文
- [ ] 无敏感数据 (使用测试数据)

### 技术检查
- [ ] 分辨率 1080 x 1920
- [ ] PNG 格式
- [ ] 文件大小 < 5MB/张
- [ ] 无水印
- [ ] 无其他应用界面

### 美观检查
- [ ] 界面整洁
- [ ] 数据合理 (测试数据)
- [ ] 颜色协调
- [ ] 文字清晰

---

## 🎯 快速截图脚本

```bash
#!/bin/bash
# save_screenshots.sh

OUTPUT_DIR=~/Desktop/wanwallet-screenshots
mkdir -p $OUTPUT_DIR

echo "请依次打开以下页面，每页按回车继续..."

read -p "1. 主页 - 打开后按回车"
adb shell screencap -p /sdcard/screenshot.png
adb pull /sdcard/screenshot.png $OUTPUT_DIR/01-home.png

read -p "2. 交易列表 - 打开后按回车"
adb shell screencap -p /sdcard/screenshot.png
adb pull /sdcard/screenshot.png $OUTPUT_DIR/02-transactions.png

read -p "3. 添加交易 - 打开后按回车"
adb shell screencap -p /sdcard/screenshot.png
adb pull /sdcard/screenshot.png $OUTPUT_DIR/03-add-transaction.png

read -p "4. 统计图表 - 打开后按回车"
adb shell screencap -p /sdcard/screenshot.png
adb pull /sdcard/screenshot.png $OUTPUT_DIR/04-stats.png

read -p "5. 预算管理 - 打开后按回车"
adb shell screencap -p /sdcard/screenshot.png
adb pull /sdcard/screenshot.png $OUTPUT_DIR/05-budget.png

echo "截图完成！保存在: $OUTPUT_DIR"
```

---

## 📤 上传到 Google Play

1. 访问 [Google Play Console](https://play.google.com/console)
2. 选择 WanWallet 应用
3. 进入 **Store listing** → **Graphic assets**
4. 上传截图:
   - Phone screenshots: 上传 5-8 张
   - Tablet screenshots: 可选
5. 调整顺序 (拖拽)
6. 点击 **Save**

---

## 🎨 设计资源

### 应用主题色
- **主色:** #4CAF50 (绿色)
- **辅色:** #2196F3 (蓝色)
- **强调色:** #FFC107 (黄色)

### 图标
- 位置: `docs/icon_designs/`
- 选择: 5 套设计方案
- 推荐: Design 3 (钱包 + 绿色)

---

**预计时间:** 30-60 分钟  
**工具:** Android 模拟器 + adb

---

*准备好截图后，即可提交 Google Play 审核*
