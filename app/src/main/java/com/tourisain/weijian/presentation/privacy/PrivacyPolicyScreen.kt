package com.tourisain.weijian.presentation.privacy

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tourisain.weijian.R
import com.tourisain.weijian.presentation.common.navigateStable
import com.tourisain.weijian.presentation.common.popBackStackStable
import com.tourisain.weijian.presentation.icons.Lucide
import com.tourisain.weijian.presentation.navigation.Screen
import com.tourisain.weijian.presentation.note.components.AppleNotesStyle
import java.util.Locale

data class ComplianceDocument(
    val id: String,
    val title: String,
    val summary: String,
    val body: String
)

private data class ConsentCopy(
    val title: String,
    val description: String,
    val acceptance: String,
    val choiceHint: String
)

private const val DOCUMENT_PRIVACY = "privacy"
private const val DOCUMENT_AGREEMENT = "agreement"
private const val DOCUMENT_PERSONAL_INFO = "personal_info"
private const val DOCUMENT_PERMISSIONS = "permissions"
private const val DOCUMENT_SHARING = "sharing"
private const val DOCUMENT_CHILDREN = "children"
private const val DOCUMENT_DEVELOPER = "developer"

object ComplianceDocuments {
    fun all(language: String = Locale.getDefault().language): List<ComplianceDocument> = when (language) {
        "en" -> englishDocuments()
        "fr" -> frenchDocuments()
        else -> chineseDocuments()
    }

    fun find(id: String, language: String = Locale.getDefault().language): ComplianceDocument {
        return all(language).firstOrNull { it.id == id } ?: all(language).first()
    }

    private fun chineseDocuments(): List<ComplianceDocument> = listOf(
        ComplianceDocument(
            id = DOCUMENT_PRIVACY,
            title = "隐私政策",
            summary = "说明微简如何处理个人信息、设备权限、本地数据和备份数据。",
            body = """
                生效日期：2026年6月13日
                应用名称：微简
                开发者名称：Tourisain
                联系邮箱：tourisain@163.com
                备案信息：青ICP备案026000119号-2A

                一、产品定位
                微简是一款离线优先的笔记、记账和本地生活记录工具。核心内容默认存储在您的设备本地。除非您主动使用分享、导出、WebDAV 备份或问题反馈等功能，微简不会主动上传您的笔记、账目、附件或错误日志。
                在您点击“同意隐私政策和用户协议并继续”之前，微简不会读取 Android ID，不会生成本机设备码，不会读取 IMEI、IMSI、MAC 地址、OAID、MEID、ICCID、SN、SUPI、SUCI、本机电话号码、联系人、通话记录、日历、短信、位置、剪切板、图片、音视频内容，也不会进行安全环境检测。

                二、我们处理的信息
                1. 您主动创建的内容：笔记、账目、分类、附件路径、头像路径、应用设置。
                2. 本地会员与激活信息：会员状态、激活码使用记录，均用于本地功能判断。
                3. 备份与同步配置：当您主动配置 WebDAV 时，会在本机保存服务器地址、用户名和密码，用于您发起的备份或恢复。
                4. 错误日志：仅在本地生成，用于定位崩溃或异常。只有您主动点击分享或导出时才会离开设备。
                5. 设备标识信息：仅在您同意隐私政策和用户协议后，应用才会读取 Android ID 并生成本地设备指纹，用于建立本机用户 ID、本地会员激活码绑定、会员状态校验和数据归属识别。该信息默认只保存在本机，不用于广告、画像或营销。
                6. 安全环境信息：仅在您同意后，应用可能通过本机运行时迹象检测 Hook、Root、Frida、调试器等风险环境，用于保护本地会员激活和数据安全。检测结果默认只在本机使用，不上传至开发者服务器。

                三、使用目的
                我们处理上述信息仅用于保存和展示您的记录、执行本地搜索、备份恢复、提醒通知、本机用户识别、会员激活、本地会员状态校验、本地隐私锁、安全风险识别和问题排查。

                四、共享、转让和公开披露
                微简默认不向第三方共享、转让或公开披露您的个人信息。以下情况由您主动触发：使用系统分享面板、导出备份文件、配置 WebDAV 服务器、向开发者发送错误日志或反馈邮件。法律法规另有要求时，我们会在必要范围内配合。

                五、保存期限与删除
                本地数据由您控制，直到您在应用内删除、清空应用数据或卸载应用。备份文件由您自行保存和删除。回收站、备份和导出文件请您自行定期清理。

                六、您的权利
                您可以在应用内访问、更正、删除、导出和恢复数据。您可以关闭权限、删除本地数据、清除应用数据或卸载应用来撤回授权。需要协助时可发送邮件至 tourisain@163.com。

                七、未成年人保护
                微简不面向未满 14 周岁的儿童提供专门服务。未成年人使用前应取得监护人同意，并由监护人指导合理使用。

                八、政策更新
                如隐私政策发生重要变化，应用会在合适位置提示您重新阅读并确认。
            """.trimIndent()
        ),
        ComplianceDocument(
            id = DOCUMENT_AGREEMENT,
            title = "用户协议",
            summary = "说明使用微简时的基本规则、用户责任和服务边界。",
            body = """
                欢迎使用微简。使用本应用前，请您阅读并同意本协议和隐私政策。

                一、服务内容
                微简提供本地笔记、记账、分类、搜索、备份恢复、提醒和本地会员激活等功能。部分功能可能依赖系统权限或您自行配置的 WebDAV 服务。

                二、用户责任
                您应合法使用本应用，不得利用微简记录、传播违法违规内容，不得攻击、破解、逆向或干扰应用正常运行。您应自行妥善保管本地设备、隐私锁密码、备份文件和 WebDAV 账号。

                三、数据与备份
                微简默认将数据保存在本机。卸载应用、清除数据、设备损坏或误删文件可能导致数据丢失。建议您定期导出或备份重要内容。

                四、本地会员与激活
                微简当前采用本地会员和激活流程。激活状态用于解锁本机功能，不构成任何线上账号权益承诺。

                五、服务变更
                开发者可在不影响您本地数据控制权的前提下优化、调整或下线部分功能。涉及重要权益变化时，将尽量在应用内提示。

                六、联系我们
                开发者名称：Tourisain
                邮箱：tourisain@163.com
                备案信息：青ICP备案026000119号-2A
            """.trimIndent()
        ),
        ComplianceDocument(
            id = DOCUMENT_PERSONAL_INFO,
            title = "个人信息收集清单",
            summary = "列明微简可能处理的数据类型、使用场景和保存位置。",
            body = """
                1. 笔记、账目、分类：用于展示、编辑、搜索和统计，默认保存在本机数据库。
                2. 图片、音频或附件路径：用于在记录中引用附件，默认保存在本机或您选择的位置。
                3. 用户名、头像路径、头像框、本地会员状态：用于个人中心和本地权益展示。
                4. 隐私锁密码：用于本地隐私保护。请妥善保存，开发者无法找回。
                5. WebDAV 地址、用户名和密码：仅在您主动配置云备份时保存，用于连接您指定的服务器。
                6. 错误日志：用于排查崩溃和异常，默认只保存在本机。
                7. 应用设置：语言、主题、布局、提醒、字体、退出确认等，用于提供个性化体验。
                8. Android ID / 本地设备指纹：仅在您点击“同意隐私政策和用户协议并继续”后读取和生成，用于本机用户 ID、会员激活码绑定和本地会员状态校验；默认只保存在本机。
                9. 安全环境检测结果：仅在您同意后用于检测 Hook、Root、调试器或专业分析工具风险；不查询其他应用包名，不上传检测结果。
            """.trimIndent()
        ),
        ComplianceDocument(
            id = DOCUMENT_PERMISSIONS,
            title = "权限使用说明",
            summary = "说明上架审核常关注的系统权限、触发场景和关闭影响。",
            body = """
                网络权限：用于访问您配置的 WebDAV 服务器、检查官网版本更新、打开帮助网站或执行您主动触发的网络功能。
                文件查看与修改授权：当您选择头像图片、导入备份文件或保存备份文件时，微简会先以弹窗明确说明访问目的、访问范围和拒绝影响；只有您点击同意继续后，才会打开系统文件选择器或保存界面。微简只读取您主动选择的文件，或只写入您主动选择的保存位置，不扫描相册、目录或其他文件。
                通知权限：用于提醒。关闭后不会影响记录功能，但提醒可能无法显示。
                精确闹钟权限：用于按您设置的时间触发本地提醒。关闭后提醒可能延迟。

                微简不会在未告知的情况下申请与当前功能无关的权限。不同 Android 版本的权限名称和授权入口可能不同，请以系统弹窗为准。
            """.trimIndent()
        ),
        ComplianceDocument(
            id = DOCUMENT_SHARING,
            title = "第三方共享与 SDK 清单",
            summary = "说明默认不共享个人信息，并列明功能依赖。",
            body = """
                默认共享情况：微简默认不向广告、统计或画像平台共享您的个人信息。

                您主动触发的共享：
                1. 系统分享面板：您分享笔记、日志或文件时，内容会交给您选择的目标应用处理。
                2. WebDAV：您配置并执行备份时，备份文件会传输到您指定的服务器。
                3. 问题反馈：您主动发送邮件或分享错误日志时，相关内容会发送给您选择的邮箱或应用。

                主要技术组件：
                AndroidX、Jetpack Compose、Room、DataStore、Hilt、WorkManager、Coil、Gson、OkHttp。这些组件用于界面、数据库、依赖注入、本地图片显示、备份和官网版本更新网络请求，不用于广告投放或用户画像。
            """.trimIndent()
        ),
        ComplianceDocument(
            id = DOCUMENT_CHILDREN,
            title = "儿童隐私保护说明",
            summary = "说明微简不面向儿童提供专门服务，未成年人需监护人同意。",
            body = """
                微简不是专门面向未满 14 周岁儿童的产品，也不会主动识别儿童身份或面向儿童建立画像。

                未成年人使用微简前，应由监护人阅读并同意用户协议和隐私政策。监护人如发现儿童在未获同意的情况下使用或保存了不适宜内容，可在设备上删除相关内容、清除应用数据或卸载应用。

                如需开发者协助，请联系 tourisain@163.com。
            """.trimIndent()
        ),
        ComplianceDocument(
            id = DOCUMENT_DEVELOPER,
            title = "开发者信息",
            summary = "Tourisain，tourisain@163.com。",
            body = """
                应用名称：微简
                开发者名称：Tourisain
                联系邮箱：tourisain@163.com
                备案信息：青ICP备案026000119号-2A
                应用定位：离线优先的笔记、记账与本地生活记录工具
                数据默认保存位置：用户设备本地
            """.trimIndent()
        )
    )

    private fun englishDocuments(): List<ComplianceDocument> = listOf(
        ComplianceDocument(
            id = DOCUMENT_PRIVACY,
            title = "Privacy Policy",
            summary = "How Weijian handles personal information, permissions, local data, and backups.",
            body = """
                Effective date: June 13, 2026
                App name: 微简
                Developer name: Tourisain
                Contact email: tourisain@163.com
                Filing record: 青ICP备案026000119号-2A

                1. Product positioning
                Weijian is an offline-first notes and local accounting app. Your core content is stored locally on your device by default. Unless you actively use sharing, export, WebDAV backup, or feedback features, Weijian does not actively upload your notes, accounting records, attachments, or error logs.
                Before you tap "Agree to Privacy Policy and Continue", Weijian does not read Android ID, generate a device code, read IMEI, IMSI, MAC address, OAID, MEID, ICCID, SN, SUPI, SUCI, phone number, contacts, call logs, calendar, SMS, location, clipboard, photos, audio, or video content, and does not perform security-environment checks.

                2. Information we process
                Content you create: notes, accounting records, note categories, accounting categories, attachment paths, avatar paths, and app settings.
                Local membership and activation data: membership status and activation records for local feature checks.
                Backup and sync configuration: if you configure WebDAV, the server address, username, and password are saved locally for backup and restore actions you initiate.
                Error logs: logs are generated locally for crash and issue diagnosis. They leave your device only when you actively share or export them.
                Device identifier data: only after you agree to the Privacy Policy and User Agreement, the app reads Android ID and generates a local device fingerprint for local user ID creation, local membership activation binding, membership validation, and data ownership matching. It is stored locally by default and is not used for ads, profiling, or marketing.
                Security environment data: only after your agreement, the app may locally check runtime signs of Hook, root, Frida, or debuggers to protect local membership activation and data security. Results are used locally by default and are not uploaded to the developer server.

                3. Purposes
                We process this information only to save and display your records, run local search, perform backup and restore, show reminders, identify the local device user, handle local membership activation and validation, protect local privacy lock features, identify local security risks, and troubleshoot issues.

                4. Sharing, transfer, and disclosure
                Weijian does not share, transfer, or publicly disclose your personal information by default. Sharing may occur only when you actively use the system share sheet, export backup files, configure WebDAV, or send feedback and logs to the developer. Where laws and regulations require disclosure, we will cooperate within the necessary scope.

                5. Retention and deletion
                Local data is controlled by you and remains until you delete it in the app, clear app data, or uninstall the app. Backup files are stored and deleted by you. Please clean recycle bin items, backups, and exported files as needed.

                6. Your rights
                You can access, correct, delete, export, and restore your data in the app. You may disable permissions, delete local data, clear app data, or uninstall the app to withdraw authorization. For assistance, contact tourisain@163.com.

                7. Minors
                Weijian is not specifically directed to children under 14. Minors should use it with guardian consent and guidance.

                8. Updates
                If this privacy policy changes materially, the app will prompt you to read and confirm it again where appropriate.
            """.trimIndent()
        ),
        ComplianceDocument(
            id = DOCUMENT_AGREEMENT,
            title = "User Agreement",
            summary = "Basic rules, user responsibility, and service boundaries.",
            body = """
                Welcome to Weijian. Before using this app, please read and agree to this agreement and the Privacy Policy.

                1. Service content
                Weijian provides local notes, accounting records, categories, search, backup and restore, privacy lock, and local membership activation. Some features may require system permissions or a WebDAV service configured by you.

                2. User responsibility
                You must use this app legally and may not use Weijian to record or distribute illegal content, attack the app, crack it, reverse engineer it, or interfere with normal operation. You are responsible for protecting your device, privacy lock password, backup files, and WebDAV account.

                3. Data and backups
                Weijian stores data locally by default. Uninstalling the app, clearing data, device damage, or accidental deletion may cause data loss. Please export or back up important content regularly.

                4. Local membership and activation
                Weijian currently uses a local membership and activation flow. Activation status unlocks local app features and does not represent an online account entitlement.

                5. Service changes
                The developer may optimize, adjust, or remove some features while respecting your control over local data. Important changes affecting your rights will be shown in the app when possible.

                6. Contact
                Developer name: Tourisain
                Email: tourisain@163.com
                Filing record: 青ICP备案026000119号-2A
            """.trimIndent()
        ),
        ComplianceDocument(
            id = DOCUMENT_PERSONAL_INFO,
            title = "Personal Information List",
            summary = "Data categories Weijian may process, use cases, and storage location.",
            body = """
                1. Notes, accounting records, note categories, and accounting categories: used for display, editing, search, classification, recycle bin, backup, restore, and basic statistics; stored in the local database by default.
                2. Image, audio, or attachment paths: used to reference attachments in records; stored locally or in the location you choose.
                3. Username, avatar path, avatar frame, and local membership status: used for the profile page and local entitlement display.
                4. Privacy lock password: used for local privacy protection. Please keep it safe; the developer cannot recover it.
                5. WebDAV address, username, and password: saved only when you configure cloud backup, and used to connect to the server you choose.
                6. Error logs: used for crash and exception diagnosis; stored locally by default.
                7. App settings: language, theme, layout, reminders, font, exit confirmation, and similar preferences used to personalize the app.
                8. Android ID / local device fingerprint: read and generated only after you tap "Agree to Privacy Policy and Continue"; used for local user ID, membership activation binding, and local membership validation; stored locally by default.
                9. Security environment check result: used only after agreement to detect Hook, root, debugger, or analysis-tool risks; it does not query other app packages or upload the result.
            """.trimIndent()
        ),
        ComplianceDocument(
            id = DOCUMENT_PERMISSIONS,
            title = "Permission Usage",
            summary = "System permissions, trigger scenarios, and impact when disabled.",
            body = """
                Network permission: used for WebDAV servers you configure, checking official website version updates, opening help pages, or network actions you actively trigger.
                File view and modification authorization: when you choose an avatar image, import a backup file, or save a backup file, Weijian first shows a dialog explaining the purpose, scope, and effect of rejection. Only after you agree will the app open the system file picker or save screen. Weijian reads only the file you choose or writes only to the location you choose; it does not scan albums, folders, or other files.
                Notification permission: reserved for local app notices that you actively enable. Turning it off does not affect writing notes or accounting records.
                Exact alarm permission: used to trigger local reminders at the time you set. Turning it off may delay reminders.

                Weijian will not request permissions unrelated to the current feature without explanation. Permission names and system dialogs vary by Android version; please follow the system prompt.
            """.trimIndent()
        ),
        ComplianceDocument(
            id = DOCUMENT_SHARING,
            title = "Third-Party Sharing and SDK List",
            summary = "Weijian does not share data by default; user-triggered sharing is listed here.",
            body = """
                Default sharing: Weijian does not share your personal information with advertising, analytics, or profiling platforms by default.

                Sharing you initiate:
                1. System share sheet: when you share notes, logs, or files, the selected target app processes the content.
                2. WebDAV: when you configure and run backup, backup files are transferred to the server you choose.
                3. Feedback: when you send email or share error logs, the related content is sent through the email client or app you choose.

                Main technical components:
                AndroidX, Jetpack Compose, Room, DataStore, Hilt, WorkManager, Coil, Gson, and OkHttp. These components support UI, database, dependency injection, local image display, backup, and official update network requests. They are not used for advertising or profiling.
            """.trimIndent()
        ),
        ComplianceDocument(
            id = DOCUMENT_CHILDREN,
            title = "Children's Privacy",
            summary = "Weijian is not directed to children under 14.",
            body = """
                Weijian is not a product specifically directed to children under 14, and it does not actively identify children or build profiles for children.

                Minors should use Weijian only after a guardian has read and agreed to the User Agreement and Privacy Policy. If a guardian finds that a child has used the app without consent or saved unsuitable content, the guardian may delete the content, clear app data, or uninstall the app on the device.

                For assistance, contact tourisain@163.com.
            """.trimIndent()
        ),
        ComplianceDocument(
            id = DOCUMENT_DEVELOPER,
            title = "Developer Information",
            summary = "Tourisain, tourisain@163.com.",
            body = """
                App name: 微简
                Developer name: Tourisain
                Contact email: tourisain@163.com
                Filing record: 青ICP备案026000119号-2A
                App positioning: offline-first notes and local accounting tool
                Default data location: user's local device
            """.trimIndent()
        )
    )

    private fun frenchDocuments(): List<ComplianceDocument> = listOf(
        ComplianceDocument(
            id = DOCUMENT_PRIVACY,
            title = "Politique de confidentialite",
            summary = "Explique comment Weijian traite les informations, autorisations, donnees locales et sauvegardes.",
            body = """
                Date d'entree en vigueur : 13 juin 2026
                Nom de l'application : 微简
                Nom du developpeur : Tourisain
                E-mail : tourisain@163.com
                Enregistrement : 青ICP备案026000119号-2A

                1. Positionnement du produit
                Weijian est un outil local de notes et de comptabilite personnelle, concu d'abord pour un usage hors ligne. Le contenu principal est stocke localement sur votre appareil par defaut. Sauf si vous utilisez volontairement le partage, l'exportation, la sauvegarde WebDAV ou le retour d'information, Weijian ne televerse pas activement vos notes, comptes, pieces jointes ou journaux d'erreur.
                Avant que vous touchiez "Accepter la confidentialite et continuer", Weijian ne lit pas l'Android ID, ne genere pas de code appareil, ne lit pas l'IMEI, l'IMSI, l'adresse MAC, l'OAID, le MEID, l'ICCID, le SN, le SUPI, le SUCI, le numero de telephone, les contacts, appels, calendrier, SMS, position, presse-papiers, photos, contenus audio ou video, et ne realise pas de verification de l'environnement de securite.

                2. Informations traitees
                Contenu cree par vous : notes, comptes, categories de notes, categories comptables, chemins de pieces jointes, chemins d'avatar et parametres de l'application.
                Abonnement local et activation : etat d'abonnement et historique d'activation, utilises pour verifier les fonctionnalites locales.
                Configuration de sauvegarde : lorsque vous configurez WebDAV, l'adresse du serveur, le nom d'utilisateur et le mot de passe sont enregistres localement pour les sauvegardes ou restaurations que vous lancez.
                Journaux d'erreur : generes localement pour diagnostiquer les incidents. Ils quittent l'appareil uniquement si vous les partagez ou les exportez volontairement.
                Identifiant Android / empreinte locale : lus et generes uniquement apres votre accord, pour l'identifiant utilisateur local, la liaison d'activation et la validation locale de l'abonnement.
                Environnement de securite : uniquement apres votre accord, l'application peut verifier localement les signes d'execution Hook, Root, Frida ou debogueur afin de proteger l'activation locale et la securite des donnees. Les resultats restent locaux par defaut.

                3. Finalites
                Ces informations servent uniquement a enregistrer et afficher vos donnees, effectuer la recherche locale, classer les notes, sauvegarder et restaurer, identifier l'utilisateur local, gerer l'activation et la verification locale, proteger le verrouillage prive, identifier les risques locaux et diagnostiquer les problemes.

                4. Partage, transfert et divulgation
                Weijian ne partage, ne transfere et ne divulgue pas publiquement vos informations personnelles par defaut. Le partage peut avoir lieu lorsque vous utilisez la feuille de partage systeme, exportez une sauvegarde, configurez WebDAV ou envoyez des retours et journaux au developpeur. Lorsque la loi l'exige, nous cooperons dans la limite necessaire.

                5. Conservation et suppression
                Les donnees locales sont sous votre controle jusqu'a suppression dans l'application, effacement des donnees de l'application ou desinstallation. Les fichiers de sauvegarde sont conserves et supprimes par vous. Veuillez nettoyer regulierement la corbeille, les sauvegardes et les fichiers exportes.

                6. Vos droits
                Vous pouvez consulter, corriger, supprimer, exporter et restaurer vos donnees dans l'application. Vous pouvez desactiver les autorisations, supprimer les donnees locales, effacer les donnees de l'application ou desinstaller l'application pour retirer votre autorisation. Pour obtenir de l'aide, contactez tourisain@163.com.

                7. Mineurs
                Weijian n'est pas specialement destine aux enfants de moins de 14 ans. Les mineurs doivent l'utiliser avec l'accord et l'accompagnement d'un responsable legal.

                8. Mise a jour de la politique
                En cas de modification importante de cette politique, l'application vous invitera a la relire et a la confirmer lorsque necessaire.
            """.trimIndent()
        ),
        ComplianceDocument(
            id = DOCUMENT_AGREEMENT,
            title = "Conditions d'utilisation",
            summary = "Regles d'utilisation, responsabilites et limites du service.",
            body = """
                Bienvenue dans Weijian. Avant d'utiliser l'application, veuillez lire et accepter ces conditions ainsi que la Politique de confidentialite.

                1. Service fourni
                Weijian fournit des notes, comptes, categories, recherche, corbeille, sauvegarde et restauration, verrouillage prive et activation locale. Certaines fonctions peuvent dependre des autorisations systeme ou d'un service WebDAV que vous configurez.

                2. Responsabilite de l'utilisateur
                Vous devez utiliser l'application legalement. Vous ne devez pas l'utiliser pour enregistrer ou diffuser du contenu illegal, attaquer, contourner, decompiler ou perturber l'application. Vous etes responsable de votre appareil, du mot de passe du verrouillage prive, des sauvegardes et du compte WebDAV.

                3. Donnees et sauvegardes
                Weijian stocke les donnees localement par defaut. La desinstallation, l'effacement des donnees, une panne d'appareil ou une suppression accidentelle peuvent entrainer une perte de donnees. Sauvegardez regulierement les contenus importants.

                4. Abonnement local et activation
                Weijian utilise actuellement un flux local d'abonnement a vie et d'activation. L'etat d'activation deverrouille des fonctions locales et ne constitue pas un droit associe a un compte en ligne.

                5. Changements de service
                Le developpeur peut optimiser, ajuster ou retirer certaines fonctions tout en respectant votre controle des donnees locales. Les changements importants seront signales dans l'application lorsque possible.

                6. Contact
                Nom du developpeur : Tourisain
                E-mail : tourisain@163.com
                Enregistrement : 青ICP备案026000119号-2A
            """.trimIndent()
        ),
        ComplianceDocument(
            id = DOCUMENT_PERSONAL_INFO,
            title = "Liste des informations personnelles",
            summary = "Categories de donnees traitees par Weijian, usages et lieux de stockage.",
            body = """
                1. Notes, comptes, categories de notes et categories comptables : utilises pour l'affichage, l'edition, la recherche, le classement, la corbeille, la sauvegarde, la restauration et les statistiques de base ; stockes dans la base locale par defaut.
                2. Chemins d'images ou pieces jointes : utilises pour referencer les pieces jointes dans les enregistrements ; stockes localement ou a l'emplacement choisi.
                3. Nom d'utilisateur, chemin d'avatar, cadre d'avatar et statut d'abonnement local : utilises dans le profil et l'affichage des droits locaux.
                4. Mot de passe du verrouillage prive : utilise pour la protection locale. Conservez-le soigneusement ; le developpeur ne peut pas le recuperer.
                5. Adresse WebDAV, nom d'utilisateur et mot de passe : enregistres uniquement si vous configurez la sauvegarde cloud, pour se connecter au serveur choisi.
                6. Journaux d'erreur : utilises pour diagnostiquer les incidents ; stockes localement par defaut.
                7. Parametres : langue, theme, disposition, police, confirmation de sortie et preferences similaires.
                8. Android ID / empreinte locale : lus et generes uniquement apres "Accepter la confidentialite et continuer" ; utilises pour l'identifiant utilisateur local, la liaison de l'activation et la verification locale de l'abonnement.
                9. Resultat de verification de securite : utilise uniquement apres accord pour detecter les risques Hook, Root, debogueur ou outils d'analyse ; l'application ne consulte pas les paquets d'autres applications et ne televerse pas le resultat.
            """.trimIndent()
        ),
        ComplianceDocument(
            id = DOCUMENT_PERMISSIONS,
            title = "Utilisation des autorisations",
            summary = "Autorisations systeme, scenarios de declenchement et impact en cas de refus.",
            body = """
                Autorisation reseau : utilisee pour les serveurs WebDAV que vous configurez, verifier les mises a jour sur le site officiel, les pages d'aide ou les actions reseau declenchees par vous.
                Autorisation de consultation et modification de fichiers : lorsque vous choisissez une image d'avatar, importez une sauvegarde ou enregistrez une sauvegarde, Weijian affiche d'abord une fenetre expliquant la finalite, la portee et l'effet du refus. Le selecteur ou l'ecran d'enregistrement du systeme ne s'ouvre qu'apres votre accord. Weijian lit uniquement le fichier choisi ou ecrit uniquement a l'emplacement choisi ; il ne parcourt pas les albums, dossiers ou autres fichiers.
                Notifications : reservees aux avis locaux que vous activez explicitement. La desactivation n'empeche pas l'ecriture de notes ou de comptes.
                Alarmes exactes : reservees aux avis locaux que vous activez explicitement. La desactivation peut retarder ces avis.

                Weijian ne demandera pas d'autorisations sans rapport avec la fonction utilisee sans explication. Les noms et fenetres systeme varient selon la version d'Android.
            """.trimIndent()
        ),
        ComplianceDocument(
            id = DOCUMENT_SHARING,
            title = "Partage tiers et liste des SDK",
            summary = "Weijian ne partage pas les donnees par defaut ; seuls les partages declenches par vous sont listes.",
            body = """
                Partage par defaut : Weijian ne partage pas vos informations personnelles avec des plateformes publicitaires, statistiques ou de profilage par defaut.

                Partages que vous declenchez :
                1. Feuille de partage systeme : lorsque vous partagez des notes ou fichiers, l'application cible choisie traite le contenu.
                2. WebDAV : lorsque vous configurez et lancez une sauvegarde, les fichiers sont transferes vers le serveur choisi.
                3. Retour d'information : lorsque vous envoyez un e-mail ou partagez des journaux d'erreur, le contenu est transmis via l'application ou le client de messagerie choisi.

                Principaux composants techniques :
                AndroidX, Jetpack Compose, Room, DataStore, Hilt, WorkManager, Coil, Gson et OkHttp. Ils servent a l'interface, la base de donnees, l'injection de dependances, l'affichage local des images, les sauvegardes et les requetes de mise a jour officielle. Ils ne servent pas a la publicite ni au profilage.
            """.trimIndent()
        ),
        ComplianceDocument(
            id = DOCUMENT_CHILDREN,
            title = "Protection des enfants",
            summary = "Weijian n'est pas destine aux enfants de moins de 14 ans.",
            body = """
                Weijian n'est pas un produit specialement destine aux enfants de moins de 14 ans et ne cherche pas activement a identifier les enfants ni a creer des profils d'enfants.

                Les mineurs doivent utiliser Weijian apres lecture et accord des conditions et de la politique de confidentialite par un responsable legal. Si un responsable constate une utilisation sans accord ou un contenu inadapte, il peut supprimer le contenu, effacer les donnees de l'application ou desinstaller l'application.

                Pour toute aide, contactez tourisain@163.com.
            """.trimIndent()
        ),
        ComplianceDocument(
            id = DOCUMENT_DEVELOPER,
            title = "Informations developpeur",
            summary = "Tourisain, tourisain@163.com.",
            body = """
                Nom de l'application : 微简
                Nom du developpeur : Tourisain
                E-mail : tourisain@163.com
                Enregistrement : 青ICP备案026000119号-2A
                Positionnement : outil local de notes et de comptabilite personnelle, hors ligne d'abord
                Emplacement par defaut des donnees : appareil local de l'utilisateur
            """.trimIndent()
        )
    )
}

private fun consentCopy(language: String): ConsentCopy = when (language) {
    "en" -> ConsentCopy(
        title = "Privacy Protection and User Agreement",
        description = "Please read and clearly choose whether to agree. Before you tap \"Agree to Privacy Policy and User Agreement, Continue\", Weijian will not read Android ID, generate a device code, check installed apps, or collect your notes, photos, audio/video, location, contacts, SMS, or clipboard data. If you reject, tap \"Reject and Exit App\" and the main interface will not open.",
        acceptance = "I have read and agree to the Privacy Policy, User Agreement, Personal Information List, Permission Usage, Third-Party Sharing and SDK List, and Children's Privacy documents.",
        choiceHint = "Please make a clear choice below. If you reject, the app will exit and no personal information will be collected."
    )
    "fr" -> ConsentCopy(
        title = "Confidentialité et conditions d'utilisation",
        description = "Veuillez lire et choisir clairement si vous acceptez. Avant « Accepter la politique de confidentialité et les conditions, continuer », Weijian ne lit pas l'Android ID, ne génère pas de code appareil, ne vérifie pas l'environnement de sécurité et ne collecte pas vos notes, photos, contenus audio/vidéo, position, contacts, SMS ou presse-papiers. En cas de refus, touchez « Refuser et quitter l'application ».",
        acceptance = "J'ai lu et j'accepte la Politique de confidentialité, les Conditions d'utilisation, la Liste des informations personnelles, l'Utilisation des autorisations, le Partage tiers et la Protection des enfants.",
        choiceHint = "Veuillez faire un choix clair ci-dessous. En cas de refus, l'application se fermera et aucune information personnelle ne sera collectée."
    )
    else -> ConsentCopy(
        title = "隐私保护与用户协议",
        description = "请阅读并明确选择是否同意以下条款。点击“同意隐私政策和用户协议并继续”之前，微简不会读取 Android ID、生成设备码、检测安全环境或收集您的笔记、图片、音视频、位置、联系人、短信、剪切板等个人信息。不同意时可以点击“拒绝并退出应用”，应用不会进入主界面。",
        acceptance = "我已阅读并同意《隐私政策》《用户协议》《个人信息收集清单》《权限使用说明》《第三方共享与 SDK 清单》和《儿童隐私保护说明》。",
        choiceHint = "请在下方作出明确选择。若选择拒绝，应用将退出，且不会收集任何个人信息。"
    )
}

@Composable
fun PrivacyConsentScreen(
    onAccept: () -> Unit,
    onExit: () -> Unit
) {
    val language = LocalConfiguration.current.locales[0]?.language ?: Locale.getDefault().language
    val documents = remember(language) { ComplianceDocuments.all(language) }
    val copy = remember(language) { consentCopy(language) }
    var accepted by remember { mutableStateOf(false) }

    BackHandler(onBack = onExit)

    Scaffold(
        containerColor = AppleNotesStyle.Background,
        bottomBar = {
            Surface(
                color = AppleNotesStyle.Background,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = copy.choiceHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppleNotesStyle.SecondaryText
                    )
                    Surface(color = AppleNotesStyle.Surface, shape = AppleNotesStyle.GroupShape) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = accepted, onCheckedChange = { accepted = it })
                            Text(
                                text = copy.acceptance,
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppleNotesStyle.PrimaryText,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Button(
                        onClick = onAccept,
                        enabled = accepted,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.agree_and_continue))
                    }
                    OutlinedButton(
                        onClick = onExit,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.disagree_and_exit), color = AppleNotesStyle.Destructive)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AppleNotesStyle.Background)
                .padding(padding),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 22.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = copy.title,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = AppleNotesStyle.PrimaryText
                    )
                    Text(
                        text = copy.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppleNotesStyle.SecondaryText
                    )
                }
            }

            item {
                Surface(color = AppleNotesStyle.Surface, shape = AppleNotesStyle.GroupShape) {
                    Column {
                        documents.forEachIndexed { index, document ->
                            ComplianceSummaryRow(document)
                            if (index != documents.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 16.dp),
                                    color = AppleNotesStyle.Separator
                                )
                            }
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    documents.forEach { document ->
                        Surface(color = AppleNotesStyle.Surface, shape = AppleNotesStyle.GroupShape) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = document.title,
                                    color = AppleNotesStyle.PrimaryText,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = document.body,
                                    color = AppleNotesStyle.PrimaryText,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplianceCenterScreen(navController: NavController) {
    val language = LocalConfiguration.current.locales[0]?.language ?: Locale.getDefault().language
    val documents = remember(language) { ComplianceDocuments.all(language) }

    Scaffold(
        containerColor = AppleNotesStyle.Background,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStackStable(Screen.Settings.route) }) {
                        Icon(Lucide.ChevronLeft, contentDescription = stringResource(R.string.back), tint = AppleNotesStyle.Accent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppleNotesStyle.Background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AppleNotesStyle.Background)
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.about_privacy),
                        color = AppleNotesStyle.PrimaryText,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold
                    )
                    val developerLine = when (language) {
                        "en" -> "Developer name: Tourisain"
                        "fr" -> "Nom du développeur : Tourisain"
                        else -> "开发者名称：Tourisain"
                    }
                    val emailLine = when (language) {
                        "en" -> "Contact email: tourisain@163.com"
                        "fr" -> "E-mail : tourisain@163.com"
                        else -> "联系邮箱：tourisain@163.com"
                    }
                    Text(developerLine, color = AppleNotesStyle.SecondaryText)
                    Text(emailLine, color = AppleNotesStyle.SecondaryText)
                    Text(stringResource(R.string.icp_record_number), color = AppleNotesStyle.TertiaryText)
                }
            }
            item {
                Surface(color = AppleNotesStyle.Surface, shape = AppleNotesStyle.GroupShape) {
                    Column {
                        documents.forEachIndexed { index, document ->
                            ComplianceSummaryRow(
                                document = document,
                                onClick = { navController.navigateStable(Screen.ComplianceDocument.createRoute(document.id)) }
                            )
                            if (index != documents.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 16.dp),
                                    color = AppleNotesStyle.Separator
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplianceDocumentScreen(navController: NavController, documentId: String) {
    val language = LocalConfiguration.current.locales[0]?.language ?: Locale.getDefault().language
    val document = remember(documentId, language) { ComplianceDocuments.find(documentId, language) }

    Scaffold(
        containerColor = AppleNotesStyle.Background,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStackStable(Screen.ComplianceCenter.route) }) {
                        Icon(Lucide.ChevronLeft, contentDescription = stringResource(R.string.back), tint = AppleNotesStyle.Accent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppleNotesStyle.Background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppleNotesStyle.Background)
                .padding(padding)
                .padding(horizontal = 18.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = document.title,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = AppleNotesStyle.PrimaryText
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AppleNotesStyle.Surface,
                shape = AppleNotesStyle.GroupShape
            ) {
                Text(
                    text = document.body,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    color = AppleNotesStyle.PrimaryText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ComplianceSummaryRow(document: ComplianceDocument, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(document.title, color = AppleNotesStyle.PrimaryText, fontWeight = FontWeight.SemiBold)
            Text(document.summary, color = AppleNotesStyle.SecondaryText, style = MaterialTheme.typography.bodySmall)
        }
        if (onClick != null) {
            Icon(Lucide.ChevronRight, contentDescription = stringResource(R.string.view), tint = AppleNotesStyle.TertiaryText)
        }
    }
}
