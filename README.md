<div align="center">

# 微简 Weijian

### 极简生活记录 | Minimalist Life Log

[![Version](https://img.shields.io/badge/version-2.0.162-blue.svg)](https://github.com/tourisain/weijian/releases)
[![Platform](https://img.shields.io/badge/platform-Android-green.svg)](https://github.com/tourisain/weijian/releases)
[![License](https://img.shields.io/badge/license-Proprietary-orange.svg)](LICENSE)
[![Language](https://img.shields.io/badge/language-Kotlin-purple.svg)](https://kotlinlang.org)

**一款专注于笔记与本地记账的极简 Android 应用，无广告、无追踪、数据本地优先。**

**A minimalist Android app focused on note-taking and local accounting — no ads, no tracking, local-first data.**

</div>

---

## 中文介绍

### 关于微简

微简是一款极简生活记录应用，将**笔记**与**记账**融为一体。我们相信工具应该安静地服务于生活，而不是用广告和弹窗打扰你。所有数据存储在本地设备上，你拥有完整的控制权。

### 核心功能

#### 笔记
- **富文本编辑器** — 支持标题（H1-H6 六级切换）、粗体、斜体、删除线、代码块、引用、列表、待办清单
- **插入功能** — 图片插入（自动生成 Markdown 引用）、链接插入（支持选中文本作为显示文字）、表格插入（三列模板）、分隔线、附件
- **编辑操作** — 撤销/重做、字体大小调节、事务化提交（仅内容变化时才保存）
- **笔记管理** — 分类文件夹、置顶、归档、版本历史（可恢复意外修改）
- **桌面小组件** — 快速新建笔记，无需打开应用

#### 记账
- **收支记录** — 快速记录收入和支出，支持多币种
- **分类统计** — 按分类查看收支分布
- **趋势分析** — 近 7 天收支概览
- **桌面小组件** — 一键记一笔

#### 备份与恢复
- **本地备份** — 一键导出/导入，支持恢复前预览
- **WebDAV 同步** — 支持自动备份到 WebDAV 服务器
- **备份健康提示** — 区分未备份、本地可用、WebDAV 可用三种状态

#### 隐私与安全
- **无广告** — 不含任何广告 SDK
- **无追踪** — 不包含统计分析 SDK，不采集用户行为
- **本地优先** — 所有数据存储在设备本地，会员状态离线可用
- **权限最小化** — 仅在必要时请求权限，拒绝后不影响核心功能

#### 个性化
- **多语言** — 支持中文、英文、法文
- **主题风格** — 经典风格支持动态取色（Android 12+）
- **会员权益** — 解锁无限笔记、无限记账、高级备份、统计图表和个性化样式

### 下载安装

从 [Releases 页面](https://github.com/tourisain/weijian/releases) 下载最新 APK，直接安装即可。

**最低系统要求：Android 8.0 (API 26)**

### 会员说明

微简提供免费基础功能和会员高级功能。会员状态存储在本地设备，离线可用，无需登录账号。

| 功能 | 免费版 | 会员版 |
|------|--------|--------|
| 笔记数量 | 3 篇 | 无限制 |
| 记账条目 | 3 条 | 无限制 |
| 备份与同步 | 基础导出 | 本地 + WebDAV 自动备份 |
| 统计图表 | 基础概览 | 完整趋势分析 |
| 个性化样式 | 基础主题 | 全部主题与高级设置 |

---

## English Introduction

### About Weijian

Weijian is a minimalist life-logging app that combines **note-taking** and **accounting** in one place. We believe tools should quietly serve your life, not interrupt it with ads and pop-ups. All data is stored locally on your device — you have full control.

### Core Features

#### Notes
- **Rich Text Editor** — Headings (H1-H6 cycling), bold, italic, strikethrough, code blocks, quotes, lists, checklists
- **Insert Actions** — Image insert (auto Markdown reference), link insert (uses selected text as display), table insert (3-column template), divider, attachments
- **Editing** — Undo/redo, font size adjustment, transactional commits (saves only when content changes)
- **Note Management** — Folders, pin, archive, version history (restore accidental edits)
- **Home Screen Widget** — Quick note creation without opening the app

#### Accounting
- **Income & Expense Tracking** — Fast entry with multi-currency support
- **Category Statistics** — View income/expense distribution by category
- **Trend Analysis** — 7-day overview of income and expenses
- **Home Screen Widget** — One-tap expense recording

#### Backup & Restore
- **Local Backup** — One-click export/import with pre-restore preview
- **WebDAV Sync** — Automatic backup to WebDAV servers
- **Backup Health** — Visual status: no backup, local ready, or WebDAV ready

#### Privacy & Security
- **No Ads** — Zero ad SDKs included
- **No Tracking** — No analytics SDKs, no behavioral data collection
- **Local-First** — All data stored on-device, membership works offline
- **Minimal Permissions** — Requests only when necessary, core features work without granted permissions

#### Personalization
- **Multi-Language** — Chinese, English, and French
- **Theme Styles** — Classic style supports dynamic color (Android 12+)
- **Membership** — Unlock unlimited notes, unlimited accounting, advanced backup, charts, and custom styles

### Download & Install

Download the latest APK from the [Releases page](https://github.com/tourisain/weijian/releases) and install directly.

**Minimum requirement: Android 8.0 (API 26)**

### Membership

Weijian offers free basic features and premium membership features. Membership status is stored locally and works offline — no account login required.

| Feature | Free | Premium |
|---------|------|---------|
| Notes | 3 | Unlimited |
| Accounting entries | 3 | Unlimited |
| Backup & Sync | Basic export | Local + WebDAV auto backup |
| Charts | Basic overview | Full trend analysis |
| Customization | Basic themes | All themes & advanced settings |

---

## 技术栈 | Tech Stack

| 技术 | 说明 |
|------|------|
| Kotlin | Primary language |
| Jetpack Compose | Declarative UI framework |
| Hilt | Dependency injection |
| Room | Local database |
| WorkManager | Background tasks |
| Coil | Image loading |
| WebDAV | Cloud backup protocol |

## 构建 | Build

```bash
# 克隆仓库 | Clone the repository
git clone https://github.com/tourisain/weijian.git
cd weijian

# 构建 Release APK | Build Release APK
./gradlew assembleRelease
```

构建产物位于 `app/build/outputs/apk/release/app-release.apk`。

Build output is at `app/build/outputs/apk/release/app-release.apk`.

## 更新日志 | Changelog

详见 [RELEASE_NOTES.md](RELEASE_NOTES.md)。

See [RELEASE_NOTES.md](RELEASE_NOTES.md) for detailed changelog.

## 反馈 | Feedback

- **邮箱**: tourisain@163.com / grllq458@gmail.com
- **问题反馈**: [GitHub Issues](https://github.com/tourisain/weijian/issues)

## 许可证 | License

版权所有 © 2024-2026 Tourisain. 保留所有权利。

Copyright © 2024-2026 Tourisain. All rights reserved.

本软件为专有软件，未经授权不得复制、修改或分发。

This software is proprietary. Unauthorized copying, modification, or distribution is prohibited.

<div align="center">

**[下载最新版本 | Download Latest Release](https://github.com/tourisain/weijian/releases/latest)**

</div>
