# WanWallet Signed APK 构建指南

**创建日期:** 2026-03-28  
**用途:** 构建用于测试和发布的签名 APK

---

## 📋 前置要求

1. **Android Studio** 或 **命令行构建工具**
2. **Keystore 文件** (用于签名)
3. **签名密钥信息**:
   - Keystore 路径
   - Keystore 密码
   - 密钥别名
   - 密钥密码

---

## 🔑 生成签名密钥 (首次)

如果是首次发布，需要生成新的 Keystore:

### 使用 keytool 生成
```bash
keytool -genkey -v -keystore wanwallet-release-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias wanwallet
```

**按提示输入:**
- Keystore 密码
- 姓名
- 组织名称
- 组织单位
- 城市
- 省份
- 国家代码

**⚠️ 重要:** 妥善保管 Keystore 文件和密码！丢失后无法恢复。

---

## 🏗️ 配置 Gradle 签名

### 步骤 1: 创建 keystore.properties
在项目根目录创建 `keystore.properties`:

```properties
storePassword=<你的 Keystore 密码>
keyPassword=<你的密钥密码>
keyAlias=wanwallet
storeFile=<Keystore 文件路径>
```

**示例:**
```properties
storePassword=mySecurePassword123
keyPassword=mySecurePassword123
keyAlias=wanwallet
storeFile=/Users/username/keystores/wanwallet-release-key.jks
```

### 步骤 2: 添加到 .gitignore
```bash
echo "keystore.properties" >> .gitignore
echo "*.jks" >> .gitignore
```

### 步骤 3: 配置 build.gradle.kts
编辑 `app/build.gradle.kts`:

```kotlin
// 读取签名配置
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(keystorePropertiesFile.inputStream())
}

android {
    // ... 其他配置
    
    signingConfigs {
        create("release") {
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
        }
    }
    
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

---

## 📦 构建 Signed APK

### 方法 1: 使用 Android Studio

1. 打开项目
2. **Build** → **Generate Signed Bundle / APK**
3. 选择 **APK**
4. 选择 **release** 构建类型
5. 输入 Keystore 信息
6. 点击 **Finish**

**输出位置:** `app/release/app-release.apk`

### 方法 2: 使用命令行

```bash
cd /root/.openclaw/workspace-director/projects/WanWallet

# 清理项目
./gradlew clean

# 构建 Signed APK
./gradlew assembleRelease

# 输出位置
ls -lh app/build/outputs/apk/release/
```

**输出文件:**
- `app-release.apk` - 签名后的 APK
- `app-release.apk.idsig` - 签名信息 (可选)

---

## ✅ 验证 APK

### 检查签名
```bash
apksigner verify --verbose app/build/outputs/apk/release/app-release.apk
```

### 检查版本信息
```bash
aapt dump badging app/build/outputs/apk/release/app-release.apk | grep -E "package:|version"
```

### 安装测试
```bash
adb install app/build/outputs/apk/release/app-release.apk
```

---

## 📊 构建输出示例

```
> Task :app:packageRelease
> Task :app:assembleRelease

BUILD SUCCESSFUL in 45s
34 actionable tasks: 34 executed

APK generated: app/build/outputs/apk/release/app-release.apk
Size: 15.2 MB
Version: 0.1.1 (1)
```

---

## 🔍 故障排除

### 问题 1: Keystore 文件未找到
```
Error: Keystore file does not exist
```
**解决:** 检查 `keystore.properties` 中的路径是否正确

### 问题 2: 密码错误
```
Error: Keystore was tampered with, or password was incorrect
```
**解决:** 确认密码正确，注意大小写

### 问题 3: 构建失败
```
Error: Build failed
```
**解决:** 
1. 运行 `./gradlew clean`
2. 检查 `build.gradle.kts` 配置
3. 查看详细错误日志

---

## 📱 测试分发

### 本地测试
```bash
adb install app/build/outputs/apk/release/app-release.apk
```

### 分享给测试人员
1. 上传 APK 到云存储 (Google Drive, Dropbox 等)
2. 发送下载链接给测试人员
3. 提供安装说明

### 封闭测试轨道 (Google Play)
1. 访问 Google Play Console
2. 选择 **Testing** → **Closed testing**
3. 创建测试轨道
4. 上传 APK
5. 添加测试人员邮箱

---

## 🎯 快速命令

```bash
# 完整构建流程
cd /root/.openclaw/workspace-director/projects/WanWallet
./gradlew clean assembleRelease

# 验证 APK
apksigner verify app/build/outputs/apk/release/app-release.apk

# 查看 APK 信息
aapt dump badging app/build/outputs/apk/release/app-release.apk | head -20
```

---

## 📝 安全提醒

1. **永远不要**将 Keystore 文件提交到 Git
2. **永远不要**将密码明文写在代码中
3. **务必备份** Keystore 文件到安全位置
4. 考虑使用 **环境变量** 或 **CI/CD 密钥管理**

---

**预期构建时间:** 1-2 分钟 (首次编译 3-5 分钟)  
**APK 大小:** 约 15-20 MB

---

*Signed APK 用于封闭测试和 Google Play 发布*
