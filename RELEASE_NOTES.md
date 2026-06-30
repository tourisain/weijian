# 更新日志 | Changelog

本文档记录微简 (Weijian) 的版本更新历史。每个版本包含中文和英文说明。

This document records the version history of Weijian. Each version includes both Chinese and English notes.

---

## v2.0.162 (2026-06-29)

### 中文

- 写笔记页面工具栏插入区域新增图片、链接和表格按钮的实际功能
- 图片插入后自动在文本中生成 Markdown 图片引用，并同步添加为附件
- 链接插入支持选中文字自动作为显示文本，未选中时提供默认占位
- 表格插入在光标位置生成标准三列表格模板，自动处理换行衔接
- 标题按钮增强为六级循环切换，连续点击可在 H1-H6 与普通文本间切换
- 全面检查并修复工具栏各按钮功能，撤销、重做、字体调节和格式操作均验证通过

### English

- Added working image, link, and table buttons to the note editor toolbar insert area
- Image insert now creates a Markdown image reference in text and adds it as an attachment
- Link insert uses selected text as display text, or provides a default placeholder when nothing is selected
- Table insert generates a standard three-column template at the cursor with proper line-break handling
- Heading button now cycles through H1-H6 and back to normal text on repeated taps
- Fully inspected and fixed all toolbar button functions: undo, redo, font sizing, and formatting actions

---

## v2.0.161 (2026-06-29)

### 中文

- 写笔记页面工具栏各按钮功能全面检查并修复，确保格式操作稳定可用
- 优化图片选择器逻辑，避免与附件选择器代码重复
- 分隔线、代码块、待办列表等格式按钮功能验证通过

### English

- Comprehensive inspection and fixes for all toolbar buttons in the note editor
- Optimized image picker logic to avoid duplication with attachment picker
- Verified divider, code block, and checklist formatting actions

---

## v2.0.160 (2026-06-28)

### 中文

- 写笔记页面工具栏结构梳理，插入区域新增图片、链接和表格按钮
- 为链接和表格按钮添加对应图标映射，保持工具栏视觉一致性
- 编辑器支持链接插入和表格插入的基础文本操作

### English

- Restructured note editor toolbar and added image, link, and table buttons to the insert area
- Added icon mappings for link and table buttons to maintain toolbar visual consistency
- Editor now supports basic text operations for link and table insertion

---

## v2.0.159 (2026-06-28)

### 中文

- 笔记编辑器工具栏格式按钮结构优化，分组显示更清晰
- 为图片插入功能准备独立的文件选择器

### English

- Improved toolbar format button grouping for clearer visual organization
- Prepared a dedicated file picker for image insertion

---

## v2.0.158 (2026-06-27)

### 中文

- 笔记列表新增真实待办筛选，首页在存在待办时显示待办入口，分类、全部和待办切换更清晰
- 桌面笔记和记账小组件接入快捷入口，点击即可进入新建笔记或新建记账
- 笔记编辑器补强当前待办行切换能力，减少大段文字中勾选待办时的重排负担
- 备份与恢复页新增备份健康提示，区分未备份、本地备份可用和 WebDAV 自动备份可用状态
- 更新弹窗与权限说明继续收紧明确按钮文案，减少审核误判并保持三语展示一致

### English

- Added a real todo filter for notes, with a home todo entry shown only when todo notes exist
- Note and accounting widgets now open the matching creation flows directly
- The note editor can now toggle the current checklist line more steadily in long text
- Backup and restore now shows backup health: no backup, local backup ready, or WebDAV auto backup ready
- Update dialogs and permission copy use clearer action labels to reduce review ambiguity across three languages

---

## v2.0.156 (2026-06-26)

### 中文

- 首页空状态新增写笔记、记一笔、建分类和启备份四个真实入口，新用户不再面对空白页
- 笔记详情页主工具栏继续减噪，待办移入格式层，书写时更安静
- 备份与恢复页新增本地优先信任说明，突出恢复前预览和安全备份
- 会员权益补充无广告统计 SDK 说明，强化纯粹离线体验
- 新增应用商店展示方案文档，围绕截图、卖点和转化文案整理上架表达

### English

- Home empty state now offers first actions for notes, accounting, folders, and backup
- The note detail primary toolbar is quieter, with checklist moved into the formatting layer
- Backup and restore now highlights local-first trust, restore preview, and safety backup protection
- Membership benefits now show the no ad or analytics SDK promise
- Added an app store presentation plan for screenshots, selling points, and conversion copy

---

## v2.0.155 (2026-06-25)

### 中文

- 记账解析从编辑页拆成独立模型，修复中英法金额、币种、分类和商户识别不稳定的问题
- 头像、通知和精确提醒入口接入统一权限说明，开启前提供明确同意和拒绝选项
- 笔记详情页键盘与工具栏避让规则统一，底部文字不再容易被工具栏遮挡
- 删除未调用的 OCR 旧布局、绘图文案、头像框工具和旧通知接口，继续收紧核心范围
- 质量门禁新增解析、权限、键盘布局、旧文件和更新日志检查，降低后续回归风险

### English

- Moved accounting note parsing into a focused model and improved amount, currency, category, and merchant detection across Chinese, English, and French
- Avatar, notification, and exact-alarm entry points now use unified permission disclosures with explicit accept and reject choices
- Note detail keyboard and toolbar avoidance now use one shared layout model so bottom text stays visible while writing
- Removed unused OCR layout, drawing copy, avatar-frame utilities, and an old notification interface to keep the core app lean
- Quality gates now cover parsing, permissions, keyboard layout, deleted legacy files, and current release notes

---

## v2.0.154 (2026-06-24)

### 中文

- 编辑器预览符号重新打磨，清单、列表和引用不再出现问号占位
- 普通笔记搜索下沉到数据库查询，减少列表页全量内存过滤压力
- 备份后台任务合并为单一 Worker，通过模式区分本地备份和 WebDAV 备份
- 文件导入、导出、附件和 WebDAV 操作统一明确权限说明与拒绝选项
- 删除未调用的旧格式入口和标签缓存路径，旧功能清理迁移改为显式兼容命名

### English

- Editor preview glyphs were polished so checklists, bullets, and quotes no longer fall back to question marks
- Normal note search now uses database queries to reduce full-list in-memory filtering
- Backup background work now uses one mode-based Worker for local and WebDAV backups
- File import, export, attachments, and WebDAV actions now share explicit permission disclosures and reject choices
- Removed unused formatting and tag-cache paths, and renamed old cleanup migration as explicit compatibility code

---

## v2.0.153 (2026-06-23)

### 中文

- 隐私政策文案按当前笔记、记账、分类和备份范围重新收紧，移除旧功能披露
- Android ID 直接读取集中到 DeviceUtil，质量门阻止未同意前或散落读取回归
- 笔记详情页拆分为更聚焦的编辑部件文件，降低单页复杂度并提升维护稳定性
- WebDAV 恢复增加恢复前预览，可先查看来源文件和可恢复数据再执行
- 旧兼容标记不再作为独立内容显示，分类与全部列表继续保持真实笔记范围

### English

- Privacy policy copy now matches the current notes, accounting, category, and backup scope
- Direct Android ID access is centralized in DeviceUtil, with a quality gate to prevent scattered reads
- The note detail screen was split into focused editor parts to reduce complexity and improve stability
- WebDAV restore now supports a preview showing the source file and restorable data before recovery
- Legacy compatibility markers no longer appear as standalone content in note lists

---

## v2.0.152 (2026-06-22)

### 中文

- 修复本次版本更新弹窗目录，确保打开后显示的就是当前版本改动
- 隐私合规门禁继续收紧，备份路径未同意前使用无害标识，阻止设备码提前读取
- 超长笔记中等规模修改进入保护性延迟同步，增强大段文字书写流畅度
- 本地备份恢复加入恢复前预览，先看笔记、记账、分类和设置数量再执行恢复
- 补充合规、编辑器和备份预览回归测试，发行前质量检查已覆盖

### English

- Corrected the local update dialog catalog so the installed version shows the exact changes for this build
- Privacy gates remain stricter: backup paths use a harmless id before consent and block early device-id access
- Moderate edits in ultra-long notes now use protective deferred sync for smoother long-form writing
- Local backup restore now shows a pre-restore preview of notes, accounting records, categories, and settings
- Added regression coverage for compliance, editor sync, and backup preview behavior before release

---

## v2.0.151 (2026-06-21)

### 中文

- 增强隐私合规门禁：备份路径在未同意前保持无害用户标识，质量检查会阻止设备码回归风险
- 超长笔记中等规模修改改为保护性延迟同步，降低大段文字书写和格式化时的卡顿
- 本地备份恢复新增恢复前预览，可先查看笔记、记账、分类和设置数量再执行恢复
- 备份校验和恢复预览共用同一套解析逻辑，减少空备份或元数据异常导致的失败
- 补充回归测试，锁定合规门禁、超长文本策略和备份预览行为

### English

- Strengthened privacy compliance gates so backup paths keep a harmless pre-consent user id and quality checks block device-id regressions
- Ultra-long notes now defer moderate edits more protectively, reducing stalls while writing or applying formatting to large text
- Local backup restore now includes a pre-restore preview for notes, accounting records, categories, and settings before recovery starts
- Backup verification and restore preview now share the same parsing checks to reject empty or invalid backup metadata earlier
- Added regression coverage for compliance gates, ultra-long editor sync, and backup preview behavior

---

## v2.0.150 (2026-06-20)

### 中文

- 收紧安全环境检测，不再查询其他应用包名，降低应用商店对安装列表采集的误判风险
- 清理启动层乱码、空延迟任务和强制 GC，低内存时改为轻量清理监控缓存
- 详情页富文本渲染独立成文件，降低大段文字编辑和后续维护风险
- 设置页无动作行不再显示可点击状态，开关行支持点击整行切换
- 新增本地质量门禁脚本，构建前可检查破坏性迁移、旧依赖、隐私文案和备份策略

### English

- Tightened security-environment checks so the app no longer queries other app packages, reducing store-review risk around installed-app collection
- Cleaned startup mojibake, removed an empty delayed task, and replaced forced GC with lightweight monitor-cache cleanup
- Moved rich note rendering into its own file to reduce risk while editing long notes and maintaining the detail page
- Settings rows without actions no longer look clickable, and switch rows can now be toggled by tapping the full row
- Added a local quality gate script for destructive migrations, old dependencies, privacy copy, and backup policy checks before release builds

---

## v2.0.149 (2026-06-19)

### 中文

- 移除数据库破坏性迁移入口，降低版本升级时静默清空数据的风险
- 删除未调用的旧数据库构建器，数据库入口收敛为一处
- 继续清理旧功能文案和日记兼容过滤，笔记列表只按归档状态隐藏内容
- 移除未使用的 Markdown、图表和图片预览依赖，并同步隐私合规说明

### English

- Removed destructive database migration fallbacks to reduce the risk of silent data loss during upgrades
- Deleted the unused legacy database builder so the app has a single production database entry
- Cleaned more old feature copy and removed diary compatibility filtering; notes are hidden only when archived
- Removed unused Markdown, chart, and photo preview dependencies, and updated privacy disclosures accordingly

---

## v2.0.148 (2026-06-18)

### 中文

- 关于微简页面新增用户反馈入口，可直接调起邮箱发送问题或建议
- 反馈邮件同时支持 tourisain@163.com 与 grllq458@gmail.com 两个收件地址
- 补充中英法三语反馈文案，语言切换后保持一致体验
- 新增反馈渠道回归测试，确保邮箱入口配置稳定

### English

- Added a User Feedback entry in About Weijian for sending issues or suggestions by email
- Feedback email now includes both tourisain@163.com and grllq458@gmail.com as recipients
- Added Chinese, English, and French feedback text for a consistent localized experience
- Added regression coverage to keep the email feedback channel configured correctly

---

## v2.0.147 (2026-06-17)

### 中文

- 增强详情页编号列表书写体验，输入 1. 后回车会自动延续到下一项
- 空编号行再次回车会自动退出列表，减少多余标记和盲删
- 补充详情页书写稳定性回归测试，覆盖编号列表延续和退出场景
- 保持轻量本地优化路线，不增加后台任务和额外依赖

### English

- Improved numbered-list writing in note details: pressing Enter after 1. now continues to the next item
- Pressing Enter on an empty numbered item exits the list, reducing stray markers and manual cleanup
- Added writing-stability regression coverage for continuing and exiting numbered lists
- Kept the optimization lightweight with no added background tasks or extra dependencies

---

## v2.0.146 (2026-06-16)

### 中文

- 修复会员中心开通会员弹窗长文本被遮挡的问题，联系信息现在可滚动查看
- 官方网站页面新增 aureate.vip 与 axutongxue.com.cn 两个开发者网站
- 会员状态票据新增 nonce 并加强永久会员形态校验，继续提升本地加密稳定性
- 优化动态取色策略：仅在 Android 12 及以上的经典风格中启用，其他风格保持稳定配色

### English

- Fixed clipped purchase text in Membership Center so contact details can be scrolled and read
- Added aureate.vip and axutongxue.com.cn to the official websites page
- Added a membership ticket nonce and stricter lifetime-membership shape checks for stronger local protection
- Improved dynamic color: it now applies only on Android 12+ in Classic style while other styles keep stable palettes

---

## v2.0.145 (2026-06-15)

### 中文

- 修复写笔记时底部文字进入工具栏下方导致只能盲打的问题
- 正文输入区会随键盘高度预留底部空间，让光标和新输入内容保持可见
- 保留工具栏贴近键盘的紧凑体验，同时减少底部遮挡
- 补充键盘避让回归测试，继续增强详情页书写稳定性

### English

- Fixed note text being hidden under the bottom toolbar while typing near the end of a note
- The editor now reserves space based on keyboard height so the cursor and new input stay visible
- Kept the toolbar compact near the keyboard while reducing bottom obstruction
- Added a keyboard avoidance regression test to improve note detail writing stability

---

## v2.0.144 (2026-06-14)

### 中文

- 优化笔记详情页工具栏格式化，列表、引用和标题不再污染空白行
- 详情页信息入口改为三语资源文案，减少硬编码和不自然字样
- 降低 Gradle 构建默认内存占用，减少低内存环境下构建失败
- 补充资源与格式化回归测试，继续增强书写稳定性

### English

- Improved note detail toolbar formatting so list, quote, and heading actions no longer dirty blank lines
- Moved the note info label to localized resources to remove hardcoded visible copy
- Reduced default Gradle build memory usage to avoid low-memory build failures
- Added resource and formatting regression tests to keep writing behavior stable

---

## v2.0.141 (2026-06-11)

### 中文

- 完成三刀收缩：移除预算、日记、倒数日、课程、森林等旧功能文件和入口
- 数据库迁移会清理旧表，备份范围收敛为笔记、记账、分类、设置和用户资料
- 隐私与合规文档同步核心功能范围，减少上架审核中的旧功能歧义
- 首页更多区改为常用，保留现有界面风格但削弱杂项感
- 删除未调用的 Markdown 文档表和旧列表函数，降低维护噪音

### English

- Completed the focused cleanup: removed old budget, diary, countdown, course, and forest feature files and entries
- Database migration now clears old tables, and backup scope is reduced to notes, accounting, categories, settings, and profile data
- Privacy and compliance documents now match the core feature scope to reduce store-review ambiguity
- The home More section is now Common, keeping the visual style while reducing miscellaneous clutter
- Removed the unused Markdown document table and old list function to lower maintenance noise

---

## v2.0.140 (2026-06-10)

### 中文

- 新增编辑状态机，区分输入、选择、格式化和恢复状态，减少详情页误触发重渲染
- 工具栏操作改为事务提交，只有内容或选区真实变化时才同步保存
- 超长文本启用更稳的延迟同步策略，大段书写和选择后应用工具更流畅
- 富文本预览避开选择和格式化过程，优先保证光标和键盘响应
- 完善详情页性能测试，覆盖编辑状态、同步延迟和工具栏提交边界

### English

- Added a lightweight editor state machine for typing, selection, formatting, and recovery states
- Toolbar formatting now commits as a transaction only when text or selection actually changes
- Ultra-long notes use a steadier deferred sync path for smoother writing and selection formatting
- Rich previews stay off during selection and formatting so cursor and keyboard response remain first
- Expanded detail page performance tests around editor state, sync delay, and toolbar commit boundaries

---

## v2.0.138 (2026-06-08)

### 中文

- 全局视觉节奏统一，圆角、行高、图标尺寸更克制
- 设置页行项收紧并统一图标尺寸，页面更像系统设置
- 弹窗使用统一最大高度和轻量容器，减少遮挡感
- 笔记列表行项更轻，时间和分类改用中点分隔
- 置顶和普通笔记状态更低调，减少列表视觉干扰

### English

- Global visual rhythm is now more consistent across corners, row height, and icon sizing
- Settings rows are tighter and use unified icons, making settings feel calmer and more system-like
- Dialogs use a shared maximum height and lighter container behavior to reduce visual blocking
- Note list rows are lighter, with time and folder separated by a quiet middle dot
- Pinned and normal note states are more restrained, reducing visual noise in long lists

---

## v2.0.137 (2026-06-07)

### 中文

- 更新弹窗现在会按当前版本显示本次真实更新内容
- 极简写作页继续收缩，正文区更像干净纸面
- 笔记信息入口变为轻量单行，减少编辑区视觉干扰
- 底部工具栏继续变薄，格式工具按需展开
- 增加本地版本更新说明目录，避免后续版本重复显示旧内容

### English

- The update dialog now shows the real release notes for the installed version
- The minimal writing page is cleaner, with the editor feeling more like a quiet sheet of paper
- Note metadata is shown as a lightweight single line to reduce visual noise while writing
- The bottom toolbar is slimmer, and formatting tools expand only when needed
- A local versioned release notes catalog prevents future versions from reusing old update text

---

## v2.0.136 (2026-06-06)

### 中文

- 笔记详情页改为极简写作模式
- 将分类、标签和附件收拢到信息入口
- 底部工具栏分为常用行和格式行，降低书写干扰

### English

- The note detail page now uses a minimal writing mode
- Folders, tags, and attachments are grouped under a single info entry
- The bottom toolbar is split into common actions and formatting actions to reduce distraction

---

<div align="center">

**[返回首页 | Back to Home](README.md)**

</div>
