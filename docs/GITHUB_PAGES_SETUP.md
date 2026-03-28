# WanWallet GitHub Pages 部署指南

**创建日期:** 2026-03-28  
**用途:** 部署隐私政策到可公开访问的 URL

---

## 📋 部署步骤

### 方法 1: 使用 docs 文件夹 (推荐)

GitHub Pages 可以直接从 `docs` 文件夹发布，无需额外配置。

#### 步骤 1: 确保隐私政策在 docs 文件夹
```bash
cd /root/.openclaw/workspace-director/projects/WanWallet
# 隐私政策已在 docs/privacy_policy.html ✅
```

#### 步骤 2: 提交并推送
```bash
git add docs/privacy_policy.html
git commit -m "docs: 隐私政策用于 GitHub Pages"
git push origin main
```

#### 步骤 3: 启用 GitHub Pages
1. 访问 https://github.com/jdcrew/WanWallet/settings/pages
2. 在 **Source** 下选择:
   - Branch: `main`
   - Folder: `/docs`
3. 点击 **Save**

#### 步骤 4: 获取隐私政策 URL
启用后，隐私政策 URL 为:
```
https://jdcrew.github.io/WanWallet/privacy_policy.html
```

---

### 方法 2: 使用 gh-pages 分支

如果需要独立的域名或更复杂的网站结构。

#### 步骤 1: 创建 gh-pages 分支
```bash
git checkout --orphan gh-pages
git reset --hard
cp docs/privacy_policy.html .
git add privacy_policy.html
git commit -m "Deploy privacy policy to GitHub Pages"
git push origin gh-pages
```

#### 步骤 2: 配置 GitHub Pages
1. 访问 https://github.com/jdcrew/WanWallet/settings/pages
2. 在 **Source** 下选择:
   - Branch: `gh-pages`
   - Folder: `/` (root)
3. 点击 **Save**

#### 步骤 3: 获取 URL
```
https://jdcrew.github.io/WanWallet/privacy_policy.html
```

---

## ✅ 验证部署

部署完成后，访问以下 URL 验证:

| 资源 | URL |
|------|-----|
| 隐私政策 | https://jdcrew.github.io/WanWallet/privacy_policy.html |
| 项目主页 | https://jdcrew.github.io/WanWallet/ |

---

## 🔗 在 Google Play 中使用

在 Google Play Console 中配置隐私政策 URL:

1. 访问 [Google Play Console](https://play.google.com/console)
2. 选择 WanWallet 应用
3. 进入 **Policy and programs** → **Privacy policy**
4. 输入: `https://jdcrew.github.io/WanWallet/privacy_policy.html`
5. 点击 **Save**

---

## 📝 注意事项

1. **URL 稳定性:** GitHub Pages URL 一旦启用，不应随意更改
2. **HTTPS:** GitHub Pages 自动提供 HTTPS，符合 Google Play 要求
3. **更新:** 修改隐私政策后，推送代码即可自动更新
4. **自定义域名 (可选):** 可配置自定义域名提升专业度

---

## 🎯 快速命令

```bash
# 一键部署 (方法 1)
cd /root/.openclaw/workspace-director/projects/WanWallet
git add docs/privacy_policy.html
git commit -m "docs: privacy policy for GitHub Pages"
git push origin main

# 然后手动在 GitHub 设置中启用 Pages
# https://github.com/jdcrew/WanWallet/settings/pages
```

---

**预期完成时间:** 5-10 分钟 (不包括 GitHub Pages 生效时间)  
**GitHub Pages 生效时间:** 通常 1-2 分钟

---

*部署完成后，隐私政策将可通过公开 URL 访问*
