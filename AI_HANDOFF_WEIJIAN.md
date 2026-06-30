# 微简项目 AI 交接文档

更新时间：2026-06-16  
项目路径：`D:\bianchen\xiangmu\first`  
应用名称：微简  
包名：`com.tourisain.weijian`  
开发者：Tourisain  
联系邮箱：`tourisain@163.com`  
备案号：青ICP备案026000119号-2A

## 1. 当前状态

微简是一个 Android 原生 Kotlin + Jetpack Compose 应用，定位为离线优先的笔记、记账、本地生活记录工具。当前主要方向是保持苹果备忘录风格，强化本地数据、会员激活、安全检测、隐私合规、备份恢复、分类管理、回收站和远程更新。

当前已归档发行包：

- `release/weijian-2.0.83-release.apk`
- `release/weijian-2.0.83-mapping.txt`
- `release/weijian-2.0.83-r8-usage.txt`
- `release/weijian-2.0.83-r8-seeds.txt`
- `release/weijian-2.0.83-r8-resources.txt`
- `release/weijian-2.0.83-r8-configuration.txt`

`version.properties` 已在上次 release 构建后自动推进到下一版：

```properties
VERSION_NAME=2.0.84
VERSION_CODE=20084
```

因此下一次执行 `assembleRelease` 时，产出的 APK 应是 `2.0.84 / 20084`，构建成功后版本文件会自动推进到 `2.0.85 / 20085`。

签名证书信息保持为：

- MD5：`1f8bfe16b1986ccacfc43cba31ccb919`
- SHA1：`9e7c1ac1199de74248d02e7c3b5ec9bf50a1862b`

## 2. 构建规则

每次修改软件功能或代码后，都要执行并确认：

```powershell
.\gradlew.bat --no-daemon assembleDebug testDebugUnitTest assembleRelease
```

构建完成后验证 APK：

```powershell
$buildTools = Get-ChildItem 'C:\Users\35962\AppData\Local\Android\Sdk\build-tools' -Directory | Sort-Object Name -Descending | Select-Object -First 1
& (Join-Path $buildTools.FullName 'apksigner.bat') verify --verbose --print-certs app\build\outputs\apk\release\app-release.apk
& (Join-Path $buildTools.FullName 'aapt.exe') dump badging app\build\outputs\apk\release\app-release.apk | Select-String "package:|application-label:|sdkVersion:|targetSdkVersion:|uses-permission"
```

然后把 APK、idsig、metadata 和 R8/mapping 产物复制到 `release/`，命名规则沿用：

```text
weijian-版本号-release.apk
weijian-版本号-release.apk.idsig
weijian-版本号-output-metadata.json
weijian-版本号-mapping.txt
weijian-版本号-r8-usage.txt
weijian-版本号-r8-seeds.txt
weijian-版本号-r8-resources.txt
weijian-版本号-r8-configuration.txt
```

注意：文档-only 变更不必强制构建 APK；代码、资源、功能、Manifest、Gradle、隐私政策内置文案变更需要构建。

## 3. 技术栈和结构

主要技术：

- Kotlin
- Jetpack Compose
- Hilt
- Room
- DataStore
- WorkManager
- OkHttp / Retrofit
- Gson / Kotlin Serialization
- AndroidX Security Crypto
- R8 minify + shrinkResources

关键路径：

- 主入口：`app/src/main/java/com/tourisain/weijian/MainActivity.kt`
- 应用初始化：`app/src/main/java/com/tourisain/weijian/MemoApplication.kt`
- 导航：`app/src/main/java/com/tourisain/weijian/presentation/navigation/AppNavigation.kt`
- 稳定导航工具：`app/src/main/java/com/tourisain/weijian/presentation/common/NavControllerExt.kt`
- 外部 URL 打开：`app/src/main/java/com/tourisain/weijian/presentation/common/ExternalUrlOpener.kt`
- 设置页：`app/src/main/java/com/tourisain/weijian/presentation/settings/SettingsScreen.kt`
- 官方网站页：`app/src/main/java/com/tourisain/weijian/presentation/settings/OfficialWebsitesScreen.kt`
- 笔记列表：`app/src/main/java/com/tourisain/weijian/presentation/note/list/NoteListScreen.kt`
- 笔记详情：`app/src/main/java/com/tourisain/weijian/presentation/note/detail/NoteDetailScreen.kt`
- 记账：`app/src/main/java/com/tourisain/weijian/presentation/account/`
- 分类管理：`app/src/main/java/com/tourisain/weijian/presentation/category/`
- 回收站：`app/src/main/java/com/tourisain/weijian/presentation/recyclebin/`
- 备份恢复：`app/src/main/java/com/tourisain/weijian/presentation/backup/`
- 会员中心：`app/src/main/java/com/tourisain/weijian/presentation/profile/MembershipScreen.kt`
- 会员/激活：`app/src/main/java/com/tourisain/weijian/util/PremiumManager.kt`、`ActivationCodeManager.kt`
- 隐私政策/合规中心：`app/src/main/java/com/tourisain/weijian/presentation/privacy/PrivacyPolicyScreen.kt`
- 远程更新：`app/src/main/java/com/tourisain/weijian/util/RemoteUpdateChecker.kt`
- 版本配置：`version.properties`
- Gradle 配置：`app/build.gradle.kts`
- 激活码工具：`tools/`

## 4. 当前重要功能说明

### 隐私合规

新用户必须先同意隐私政策和用户协议。用户未同意前，不应读取 Android ID，不应生成设备码，不应执行会员安全检测，不应联网检查远程更新。

相关逻辑：

- `PrivacyConsentScreen`
- `ComplianceDocuments`
- `MemoApplication.runPostConsentInitialization()`
- `MainActivity` 中的隐私同意 gating

注意：如果新增权限、联网、文件访问、设备标识、安全检测、第三方 SDK，必须同步修改内置隐私政策和 `release/weijian_privacy_policy_vivo_2026-06-15.md/html`。

### 会员体系

当前只支持终身会员，不支持试用、不支持一年会员。普通用户限制：

- 笔记最多 3 个
- 记账记录最多 3 个

会员激活为本地加密激活码流程：用户输入用户名、邮箱，结合本机设备指纹生成设备码；开发者用工具生成激活码；用户本地输入激活码激活。

注意：不要恢复旧会员信息、不要重新加入试用、不要加入在线支付或第三方登录。

### 远程更新

App 从以下地址读取官网 JSON：

```text
https://tourisain.cn/weijian/update.json
```

只允许 HTTPS 且域名为 `tourisain.cn` 或其子域名。只显示更新提示并打开下载页，不做热更新、不执行远程代码。

官网配置模板：

- `release/tourisain_update_config_template.json`
- `release/remote_update_config_guide_2026-06-16.md`

如果下次发布新版，必须同步修改模板中的：

- `latestVersionCode`
- `latestVersionName`
- `downloadUrl`
- `releaseNotes`
- `publishedAt`

### 官方网站页

设置页“检查更新”下方有“官方网站”入口，进入页面后显示：

- `tourisain.cn`：官方网站
- `xyster.xyz`：开发者网站
- `gratia.top`：开发者网站

点击会用系统浏览器打开，失败时提示“无法打开网站，请稍后重试”。

### 稳定性加固

最近一轮已做：

- `navigateStable`
- `navigateTopLevelStable`
- `popBackStackStable`
- 防重复点击导航
- 常用页面返回 fallback
- 外部 URL 打开失败提示
- 本地错误日志记录真实版本号

后续继续做稳定性时，应优先处理：

- ViewModel 层异常统一封装
- Room/Flow 数据为空或损坏时的兜底
- 长文本编辑性能
- 大列表 key 稳定性
- 备份恢复的错误恢复路径
- 隐私锁和生命周期切换

## 5. UI 风格要求

用户明确要求整体对标苹果备忘录风格。请保持：

- 背景柔和、纸张感
- 卡片圆角克制
- 图标轻量
- 黄色作为主强调色
- 页面不要做营销落地页
- 功能页就是功能页
- 弹窗必须统一为当前苹果风格
- 设置类页面使用 `SimpleSettingsPage`、`SettingsGroup`、`SettingItem`
- 不要随意大改主视觉

如果新增页面，优先复用：

- `AppleNotesStyle`
- `SimpleSettingsPage`
- `SettingsGroup`
- `SettingItem`
- `AppleAlertDialog`

## 6. 我的判断和建议

我认为这个项目下一阶段最应该做的不是继续堆功能，而是继续把已有功能“做稳、做顺、做一致”：

1. 优先稳定核心路径：启动、隐私同意、解锁、首页、笔记新建/保存/删除、记账新建/保存、会员激活、备份恢复。
2. 每次只解决一个小范围问题，不要再做大规模重构。这个项目历史改动很多，大重构很容易把已经可用的部分打乱。
3. 对用户侧，要减少“点了没反应”“保存后首页不显示”“返回错页”“弹窗突兀”这类体验损耗。
4. 对上架侧，要优先保护隐私合规：未同意前不读取 Android ID、不联网、不检测安全环境；权限申请必须有明确同意/拒绝。
5. 对会员侧，保持本地终身会员这个模型，不要接广告 SDK、统计 SDK、第三方支付 SDK，除非用户明确再次要求并愿意同步隐私政策与商店说明。
6. 对远程更新，只做版本提示和官网跳转，不做热更新。国内商店对热更新、隐私、SDK 都很敏感。
7. 构建产物和 mapping 一定要留档。R8 后没有 mapping，后续崩溃排查会非常痛苦。

## 7. 后续 AI 工作流程

后续 AI 接手时建议遵循：

1. 先读本文件。
2. 再读 `version.properties`、`app/build.gradle.kts`、`AppNavigation.kt`、`SettingsScreen.kt`、当前任务涉及页面。
3. 使用 `rg` 搜索，不要盲改。
4. 小步修改，优先低风险。
5. 修改前确认是否会影响隐私合规、会员、数据保存、版本构建。
6. 修改代码后运行：

```powershell
.\gradlew.bat --no-daemon assembleDebug testDebugUnitTest assembleRelease
```

7. 验证签名和包信息。
8. 复制发行产物到 `release/`。
9. 如果版本变了，同步 `release/tourisain_update_config_template.json` 和官网更新说明。
10. 最终回复用户时说明改了什么、APK 路径、验证结果。

## 8. 可直接复制给后续 AI 的提示词

```text
你正在接手 Android 项目“微简”，项目路径是 D:\bianchen\xiangmu\first。请先完整阅读根目录的 AI_HANDOFF_WEIJIAN.md，然后再行动。

你必须遵守：
1. 使用中文与用户沟通。
2. 先用 rg 搜索和阅读相关代码，不要凭空猜。
3. 不要大规模重构，按用户要求从最小问题开始一步步修。
4. 保持苹果备忘录风格，复用 AppleNotesStyle、SimpleSettingsPage、SettingsGroup、SettingItem、AppleAlertDialog。
5. 不要恢复本地试用，不要恢复一年会员；会员只有本地加密激活码的终身会员。
6. 普通用户最多 3 个笔记、3 条记账记录；会员无限制。
7. 新用户未同意隐私政策前，不得读取 Android ID、不得生成设备码、不得联网检查更新、不得执行安全环境检测。
8. 任何新增权限、联网、SDK、设备标识或文件访问，都必须同步隐私政策和用户明确同意/拒绝流程。
9. 远程更新只读取 https://tourisain.cn/weijian/update.json 并提示用户跳转官网，不允许热更新或远程执行代码。
10. 每次代码/资源/功能修改后都要运行：
   .\gradlew.bat --no-daemon assembleDebug testDebugUnitTest assembleRelease
11. 构建后验证 APK 签名、包名、版本、权限，并把 APK、idsig、metadata、mapping、R8 usage/seeds/resources/configuration 复制到 release/。
12. 每次 release 后根据实际版本同步 release/tourisain_update_config_template.json 和 remote_update_config_guide_2026-06-16.md。

当前已归档发行版是 2.0.83 / 20083，version.properties 已推进到 2.0.84 / 20084。下一次 release 应产出 2.0.84 / 20084。

请先确认当前用户的新需求，再制定低风险修改方案并直接执行，最后给出修改摘要、构建验证结果和发行包路径。
```
