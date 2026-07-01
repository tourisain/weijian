<div align="center">

# 微简 Weijian

### 极简生活记录 | Minimalist Life Log

[![Version](https://img.shields.io/badge/version-2.0.162-blue.svg)](https://github.com/tourisain/weijian/releases)
[![Platform](https://img.shields.io/badge/platform-Android_8.0+-green.svg)](https://github.com/tourisain/weijian/releases)
[![Language](https://img.shields.io/badge/language-Kotlin-purple.svg)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/license-Proprietary-orange.svg)](LICENSE)
[![Downloads](https://img.shields.io/github/downloads/tourisain/weijian/total.svg)](https://github.com/tourisain/weijian/releases)
[![Last Update](https://img.shields.io/badge/updated-2026--06--29-brightgreen.svg)](https://github.com/tourisain/weijian/releases)

**笔记 + 记账二合一 | 无广告 · 无追踪 · 仅 3 项权限 · 数据本地优先**

**Note-taking + Accounting in one | No ads · No tracking · Only 3 permissions · Local-first**

</div>

---

## 为什么选择微简 | Why Weijian

<!-- 截图占位：请添加 4 张界面截图 -->
<!-- ![截图1](screenshots/01.png) ![截图2](screenshots/02.png) -->
<!-- ![截图3](screenshots/03.png) ![截图4](screenshots/04.png) -->

### 仅 3 项权限 | Only 3 Permissions

| 权限 | 用途 | 是否必须 |
|------|------|----------|
| `INTERNET` | WebDAV 备份、版本检查 | 否（仅备份时使用） |
| `POST_NOTIFICATIONS` | 提醒通知 | 否 |
| `SCHEDULE_EXACT_ALARM` | 精确闹钟提醒 | 否 |

> **无存储权限、无位置权限、无相机权限、无读取应用列表权限。** 文件访问全部通过系统 SAF 选择器实现，你完全掌控自己的文件。

> **No storage, location, camera, or app-list permissions.** All file access goes through the system file picker (SAF). You stay in control.

### 与同类应用对比 | Comparison

| 特性 | 微简 Weijian | 主流笔记应用 | 主流记账应用 |
|------|:---:|:---:|:---:|
| 笔记 + 记账二合一 | ✅ | ❌ | ❌ |
| 无广告 SDK | ✅ | 多数有 | 多数有 |
| 无统计追踪 SDK | ✅ | 多数有 | 多数有 |
| 权限数量 | **3** | 10-20+ | 10-20+ |
| 数据本地存储 | ✅ | 部分云端 | 多数云端 |
| WebDAV 自主云备份 | ✅ | 少数 | 少数 |
| 会员定价 | **¥35 永久** | 月付/年付 | 月付/年付 |
| 离线会员验证 | ✅ | 需登录 | 需登录 |
| 开源 | 代码闭源，文档开放 | 部分开源 | 多数闭源 |

---

## 中文介绍

### 核心功能

#### 笔记
- **富文本编辑器** — 标题（H1-H6 六级循环切换）、粗体、斜体、删除线、代码块、引用、无序列表、有序列表、待办清单、分隔线
- **插入功能** — 图片（自动生成 Markdown 引用并添加附件）、链接（选中文本自动作为显示文字）、表格（三列模板）、附件
- **编辑操作** — 撤销/重做、字体大小调节（14-28sp）、事务化提交（仅内容变化时才保存）
- **笔记管理** — 分类文件夹、标签、置顶、归档、版本历史（误改可恢复）
- **智能筛选** — 按置顶、已锁定、有附件、待办状态筛选
- **单篇锁定** — 基于隐私锁的细粒度内容保护
- **导出** — 支持 Markdown 和 HTML 格式导出
- **外部打开** — 支持从文件管理器直接打开 `.md` 文件
- **桌面小组件** — 快速新建便签，无需打开应用

#### 记账
- **收支记录** — 快速记录收入和支出，支持多币种
- **智能整理** — 从备注自动识别金额、分类和收支类型
- **分类管理** — 自定义收支分类
- **分类统计** — 按分类查看收支分布
- **趋势分析** — 近 7 天收支概览、每日收支、7 天结余
- **凭证图片** — 为记账条目附加凭证图片
- **回收站** — 误删可恢复
- **桌面小组件** — 一键记一笔

#### 备份与恢复
- **本地备份** — 一键导出/导入，保留最近 10 个历史版本
- **WebDAV 云备份** — 自动/手动备份到你自己的 WebDAV 服务器，配置仅存本机
- **恢复前预览** — 恢复前可查看笔记、记账、分类、设置数量明细
- **恢复前安全备份** — 恢复前自动创建当前数据快照，防误操作
- **备份加密** — 可选密码加密备份文件
- **备份健康提示** — 区分未备份、本地可用、WebDAV 可用三种状态

#### 隐私与安全
- **无广告** — 不含任何广告 SDK
- **无追踪** — 不包含统计分析 SDK，不采集用户行为
- **本地优先** — 所有数据存储在设备本地，会员状态离线可用
- **权限最小化** — 仅 3 项权限，拒绝后不影响核心功能
- **隐私锁** — 4-6 位 PIN 码应用锁
- **安全检测** — 内置 Xposed/Frida/Hook 注入检测、内存监视、可调试构建检测

#### 个性化
- **5 种主题风格** — 经典、Apple、纸张、青植、石墨
- **明暗模式** — 跟随系统 / 浅色 / 深色
- **动态取色** — Android 12+ 经典风格支持壁纸取色
- **字体选择** — 系统字体 / 衬线 / 等宽
- **文字大小** — 紧凑 / 标准 / 较大
- **多语言** — 中文、英文、法文

### 会员说明

微简提供免费基础功能和会员高级功能。**会员 ¥35 永久买断**，无订阅、无续费。会员状态存储在本地设备，离线可用，无需登录账号。

| 功能 | 免费版 | 会员版（¥35 永久） |
|------|--------|--------|
| 笔记数量 | 3 篇 | 无限制 |
| 记账条目 | 3 条 | 无限制 |
| 备份与同步 | 基础导出 | 本地 + WebDAV 自动备份 |
| 统计图表 | 基础概览 | 完整趋势分析 |
| 个性化样式 | 基础主题 | 全部 5 种主题与高级设置 |
| 自定义分类 | 基础分类 | 无限制自定义 |

### 下载安装

从 [Releases 页面](https://github.com/tourisain/weijian/releases) 下载最新 APK，直接安装即可。

**最低系统要求：Android 8.0 (API 26)**

---

## English Introduction

### Core Features

#### Notes
- **Rich Text Editor** — Headings (H1-H6 cycling), bold, italic, strikethrough, code blocks, quotes, unordered/ordered lists, checklists, dividers
- **Insert Actions** — Image (auto Markdown reference + attachment), link (uses selected text as display), table (3-column template), attachments
- **Editing** — Undo/redo, font size (14-28sp), transactional commits (saves only when content changes)
- **Note Management** — Folders, tags, pin, archive, version history (restore accidental edits)
- **Smart Filters** — Filter by pinned, locked, has-attachment, or todo status
- **Per-note Lock** — Granular content protection via privacy lock
- **Export** — Markdown and HTML formats
- **External Open** — Open `.md` files directly from file managers
- **Home Screen Widget** — Quick note creation without opening the app

#### Accounting
- **Income & Expense Tracking** — Fast entry with multi-currency support
- **Smart Parsing** — Auto-detect amount, category, and type from notes
- **Category Management** — Custom income/expense categories
- **Category Statistics** — View income/expense distribution by category
- **Trend Analysis** — 7-day overview, daily breakdown, 7-day balance
- **Receipt Images** — Attach receipt photos to entries
- **Recycle Bin** — Recover accidentally deleted entries
- **Home Screen Widget** — One-tap expense recording

#### Backup & Restore
- **Local Backup** — One-click export/import, keeps last 10 history versions
- **WebDAV Cloud Backup** — Auto/manual backup to your own WebDAV server, credentials stored locally only
- **Pre-restore Preview** — View notes, accounting, categories, and settings count before restoring
- **Pre-restore Safety Backup** — Auto-snapshot current data before any restore
- **Backup Encryption** — Optional password-protected backups
- **Backup Health** — Visual status: no backup, local ready, or WebDAV ready

#### Privacy & Security
- **No Ads** — Zero ad SDKs
- **No Tracking** — No analytics SDKs, no behavioral data collection
- **Local-First** — All data on-device, membership works offline
- **Minimal Permissions** — Only 3 permissions, core features work without any granted
- **Privacy Lock** — 4-6 digit PIN app lock
- **Security Detection** — Built-in Xposed/Frida/Hook injection detection, memory monitoring, debuggable build checks

#### Personalization
- **5 Theme Styles** — Classic, Apple, Paper, Sage, Graphite
- **Dark Mode** — Follow system / Light / Dark
- **Dynamic Color** — Android 12+ wallpaper-based coloring (Classic style)
- **Font Choice** — System / Serif / Monospace
- **Text Size** — Compact / Standard / Large
- **Multi-Language** — Chinese, English, French

### Membership

Weijian offers free basic features and premium membership. **¥35 one-time purchase, lifetime access** — no subscriptions, no renewals. Membership is stored locally and works offline — no account login required.

| Feature | Free | Premium (¥35 lifetime) |
|---------|------|---------|
| Notes | 3 | Unlimited |
| Accounting entries | 3 | Unlimited |
| Backup & Sync | Basic export | Local + WebDAV auto backup |
| Charts | Basic overview | Full trend analysis |
| Customization | Basic themes | All 5 themes & advanced settings |
| Custom categories | Basic | Unlimited custom categories |

### Download & Install

Download the latest APK from the [Releases page](https://github.com/tourisain/weijian/releases) and install directly.

**Minimum requirement: Android 8.0 (API 26)**

---

## 常见问题 | FAQ

**Q: 微简是开源的吗？| Is Weijian open source?**

代码闭源，但文档（README、更新日志）在 GitHub 公开。APK 安装包在 Releases 页面免费下载。

The source code is closed, but documentation (README, changelog) is public on GitHub. APK packages are freely available on the Releases page.

**Q: 数据存储在哪里？| Where is my data stored?**

所有数据存储在你的设备本地。WebDAV 备份使用你自己的服务器，数据不经任何第三方。

All data is stored locally on your device. WebDAV backup uses your own server — no third-party involved.

**Q: 会员需要联网验证吗？| Does membership require online verification?**

不需要。会员激活码在本地生成和验证，离线永久可用。激活时需要提供用户名和邮箱生成设备码。

No. Membership activation codes are generated and verified locally. Only a username and email are needed to generate a device code during activation.

**Q: 免费版有什么限制？| What are the free version limits?**

免费版可创建 3 篇笔记和 3 条记账记录，使用基础备份和基础图表。核心编辑功能全部免费。

The free version allows 3 notes and 3 accounting entries, with basic backup and basic charts. All core editing features are free.

**Q: 支持哪些语言？| What languages are supported?**

中文、英文、法文三种语言完整本地化。

Chinese, English, and French are fully localized.

---

## 路线图 | Roadmap

- [ ] 应用图标适配（自适应图标 + monochrome）
- [ ] 更多桌面小组件样式
- [ ] Markdown 实时预览模式
- [ ] 记账月度/年度报表
- [ ] 笔记全文搜索增强
- [ ] 更多语言支持

---

## 技术栈 | Tech Stack

| 技术 | 版本 | 说明 |
|------|------|------|
| Kotlin | JVM 17 | Primary language |
| Jetpack Compose | BOM 2024.06.00 | Declarative UI framework |
| Hilt | 2.51.1 | Dependency injection |
| Room | 2.6.1 | Local database |
| WorkManager | 2.9.0 | Background tasks (auto backup) |
| Coil | 2.5.0 | Image loading |
| OkHttp | 4.12.0 | HTTP client (WebDAV) |
| Gson | 2.10.1 | JSON serialization |
| kotlinx-serialization | 1.6.3 | Backup data serialization |
| Security Crypto | 1.1.0-alpha06 | Membership ticket encryption |
| DataStore | 1.0.0 | Preferences storage |
| Navigation Compose | 2.7.7 | Screen navigation |
| Google Fonts | — | Typography |
| compileSdk / targetSdk | 36 | Android 16 |
| minSdk | 26 | Android 8.0 |

---

## 更新日志 | Changelog

详见 [RELEASE_NOTES.md](RELEASE_NOTES.md)。

See [RELEASE_NOTES.md](RELEASE_NOTES.md) for detailed changelog.

---

## 反馈 | Feedback

- **邮箱 | Email**: tourisain@163.com / grllq458@gmail.com
- **官网 | Websites**: [tourisain.cn](https://tourisain.cn) · [aureate.vip](https://aureate.vip) · [axutongxue.com.cn](https://axutongxue.com.cn)
- **问题反馈 | Issues**: [GitHub Issues](https://github.com/tourisain/weijian/issues)

---

## 许可证 | License

版权所有 © 2024-2026 Tourisain. 保留所有权利。

Copyright © 2024-2026 Tourisain. All rights reserved.

本软件为专有软件，未经授权不得复制、修改或分发。

This software is proprietary. Unauthorized copying, modification, or distribution is prohibited.

---

<div align="center">

**[⬇ 下载最新版本 | Download Latest Release](https://github.com/tourisain/weijian/releases/latest)**

**⭐ 如果微简对你有帮助，请给个 Star | If Weijian helps you, please give it a Star ⭐**

</div>
