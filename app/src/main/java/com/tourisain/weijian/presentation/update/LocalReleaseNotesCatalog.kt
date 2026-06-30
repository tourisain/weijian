package com.tourisain.weijian.presentation.update

data class LocalReleaseNotes(
    val versionCode: Int,
    val versionName: String,
    val notes: List<String>
)

private data class LocalReleaseNotesEntry(
    val versionCode: Int,
    val versionName: String,
    val zh: List<String>,
    val en: List<String>,
    val fr: List<String>
)

fun localReleaseNotesFor(
    versionCode: Int,
    language: String
): LocalReleaseNotes {
    val entry = releaseNotesCatalog
        .filter { it.versionCode <= versionCode }
        .maxByOrNull { it.versionCode }
        ?: releaseNotesCatalog.maxBy { it.versionCode }
    return LocalReleaseNotes(
        versionCode = entry.versionCode,
        versionName = entry.versionName,
        notes = when (language.lowercase().substringBefore("-")) {
            "en" -> entry.en
            "fr" -> entry.fr
            else -> entry.zh
        }
    )
}

fun hasExactLocalReleaseNotesFor(versionCode: Int): Boolean {
    return releaseNotesCatalog.any { it.versionCode == versionCode }
}

private val releaseNotesCatalog = listOf(
    LocalReleaseNotesEntry(
        versionCode = 20162,
        versionName = "2.0.162",
        zh = listOf(
            "写笔记页面工具栏插入区域新增图片、链接和表格按钮的实际功能",
            "图片插入后自动在文本中生成 Markdown 图片引用，并同步添加为附件",
            "链接插入支持选中文字自动作为显示文本，未选中时提供默认占位",
            "表格插入在光标位置生成标准三列表格模板，自动处理换行衔接",
            "标题按钮增强为六级循环切换，连续点击可在 H1-H6 与普通文本间切换",
            "全面检查并修复工具栏各按钮功能，撤销、重做、字体调节和格式操作均验证通过"
        ),
        en = listOf(
            "Added working image, link, and table buttons to the note editor toolbar insert area.",
            "Image insert now creates a Markdown image reference in text and adds it as an attachment.",
            "Link insert uses selected text as display text, or provides a default placeholder when nothing is selected.",
            "Table insert generates a standard three-column template at the cursor with proper line-break handling.",
            "Heading button now cycles through H1-H6 and back to normal text on repeated taps.",
            "Fully inspected and fixed all toolbar button functions: undo, redo, font sizing, and formatting actions."
        ),
        fr = listOf(
            "Ajout des boutons image, lien et tableau fonctionnels dans la zone insertion de la barre d'outils.",
            "L'insertion d'image cree une reference Markdown dans le texte et l'ajoute en piece jointe.",
            "L'insertion de lien utilise le texte selectionne, ou propose un texte par defaut.",
            "L'insertion de tableau genere un modele a trois colonnes au curseur avec gestion des retours a la ligne.",
            "Le bouton titre fait maintenant boucler entre H1-H6 et le texte normal.",
            "Verification et correction completes de toutes les fonctions de la barre d'outils."
        )
    ),
    LocalReleaseNotesEntry(
        versionCode = 20161,
        versionName = "2.0.161",
        zh = listOf(
            "写笔记页面工具栏各按钮功能全面检查并修复，确保格式操作稳定可用",
            "优化图片选择器逻辑，避免与附件选择器代码重复",
            "分隔线、代码块、待办列表等格式按钮功能验证通过"
        ),
        en = listOf(
            "Comprehensive inspection and fixes for all toolbar buttons in the note editor.",
            "Optimized image picker logic to avoid duplication with attachment picker.",
            "Verified divider, code block, and checklist formatting actions."
        ),
        fr = listOf(
            "Inspection complete et corrections de tous les boutons de la barre d'outils.",
            "Optimisation du selecteur d'image pour eviter la duplication.",
            "Verification des actions de formatage : separateur, bloc de code et liste de taches."
        )
    ),
    LocalReleaseNotesEntry(
        versionCode = 20160,
        versionName = "2.0.160",
        zh = listOf(
            "写笔记页面工具栏结构梳理，插入区域新增图片、链接和表格按钮",
            "为链接和表格按钮添加对应图标映射，保持工具栏视觉一致性",
            "编辑器支持链接插入和表格插入的基础文本操作"
        ),
        en = listOf(
            "Restructured note editor toolbar and added image, link, and table buttons to the insert area.",
            "Added icon mappings for link and table buttons to maintain toolbar visual consistency.",
            "Editor now supports basic text operations for link and table insertion."
        ),
        fr = listOf(
            "Restructuration de la barre d'outils avec ajout des boutons image, lien et tableau.",
            "Ajout des icones pour les boutons lien et tableau.",
            "L'editeur supporte les operations de base pour l'insertion de liens et de tableaux."
        )
    ),
    LocalReleaseNotesEntry(
        versionCode = 20159,
        versionName = "2.0.159",
        zh = listOf(
            "笔记编辑器工具栏格式按钮结构优化，分组显示更清晰",
            "为图片插入功能准备独立的文件选择器"
        ),
        en = listOf(
            "Improved toolbar format button grouping for clearer visual organization.",
            "Prepared a dedicated file picker for image insertion."
        ),
        fr = listOf(
            "Optimisation du regroupement des boutons de formatage de la barre d'outils.",
            "Preparation d'un selecteur de fichiers dedie pour l'insertion d'images."
        )
    ),
    LocalReleaseNotesEntry(
        versionCode = 20158,
        versionName = "2.0.158",
        zh = listOf(
            "笔记列表新增真实待办筛选，首页在存在待办时显示待办入口，分类、全部和待办切换更清晰",
            "桌面笔记和记账小组件接入快捷入口，点击即可进入新建笔记或新建记账",
            "笔记编辑器补强当前待办行切换能力，减少大段文字中勾选待办时的重排负担",
            "备份与恢复页新增备份健康提示，区分未备份、本地备份可用和 WebDAV 自动备份可用状态",
            "更新弹窗与权限说明继续收紧明确按钮文案，减少审核误判并保持三语展示一致"
        ),
        en = listOf(
            "Added a real todo filter for notes, with a home todo entry shown only when todo notes exist.",
            "Note and accounting widgets now open the matching creation flows directly.",
            "The note editor can now toggle the current checklist line more steadily in long text.",
            "Backup and restore now shows backup health: no backup, local backup ready, or WebDAV auto backup ready.",
            "Update dialogs and permission copy use clearer action labels to reduce review ambiguity across three languages."
        ),
        fr = listOf(
            "Ajout d'un vrai filtre de taches pour les notes, avec entree d'accueil si des taches existent.",
            "Les widgets note et comptabilite ouvrent directement les ecrans de creation correspondants.",
            "L'editeur peut basculer la ligne de tache courante plus stablement dans les longs textes.",
            "Sauvegarde et restauration affichent l'etat de sante : aucune sauvegarde, locale prete ou WebDAV pret.",
            "Les fenetres de mise a jour et permissions utilisent des libelles plus explicites en trois langues."
        )
    ),
    LocalReleaseNotesEntry(
        versionCode = 20156,
        versionName = "2.0.156",
        zh = listOf(
            "首页空状态新增写笔记、记一笔、建分类和启备份四个真实入口，新用户不再面对空白页",
            "笔记详情页主工具栏继续减噪，待办移入格式层，书写时更安静",
            "备份与恢复页新增本地优先信任说明，突出恢复前预览和安全备份",
            "会员权益补充无广告统计 SDK 说明，强化纯粹离线体验",
            "新增应用商店展示方案文档，围绕截图、卖点和转化文案整理上架表达"
        ),
        en = listOf(
            "Home empty state now offers first actions for notes, accounting, folders, and backup.",
            "The note detail primary toolbar is quieter, with checklist moved into the formatting layer.",
            "Backup and restore now highlights local-first trust, restore preview, and safety backup protection.",
            "Membership benefits now show the no ad or analytics SDK promise.",
            "Added an app store presentation plan for screenshots, selling points, and conversion copy."
        ),
        fr = listOf(
            "L'accueil vide propose des actions pour notes, comptes, dossiers et sauvegarde.",
            "La barre principale du detail de note est plus calme, avec les taches dans la couche format.",
            "Sauvegarde et restauration mettent en avant le local, l'apercu et la sauvegarde de securite.",
            "Les avantages membre affichent l'absence de SDK publicite ou analyse.",
            "Ajout d'un plan de presentation boutique pour captures, points forts et conversion."
        )
    ),
    LocalReleaseNotesEntry(
        versionCode = 20155,
        versionName = "2.0.155",
        zh = listOf(
            "记账解析从编辑页拆成独立模型，修复中英法金额、币种、分类和商户识别不稳定的问题",
            "头像、通知和精确提醒入口接入统一权限说明，开启前提供明确同意和拒绝选项",
            "笔记详情页键盘与工具栏避让规则统一，底部文字不再容易被工具栏遮挡",
            "删除未调用的 OCR 旧布局、绘图文案、头像框工具和旧通知接口，继续收紧核心范围",
            "质量门禁新增解析、权限、键盘布局、旧文件和更新日志检查，降低后续回归风险"
        ),
        en = listOf(
            "Moved accounting note parsing into a focused model and improved amount, currency, category, and merchant detection across Chinese, English, and French.",
            "Avatar, notification, and exact-alarm entry points now use unified permission disclosures with explicit accept and reject choices.",
            "Note detail keyboard and toolbar avoidance now use one shared layout model so bottom text stays visible while writing.",
            "Removed unused OCR layout, drawing copy, avatar-frame utilities, and an old notification interface to keep the core app lean.",
            "Quality gates now cover parsing, permissions, keyboard layout, deleted legacy files, and current release notes."
        ),
        fr = listOf(
            "L'analyse des comptes est isolee dans un modele dedie avec une meilleure detection des montants, devises, categories et commercants.",
            "Les entrees avatar, notification et rappel precis utilisent maintenant des explications de permission unifiees avec accord ou refus explicite.",
            "Le clavier et la barre d'outils de l'editeur utilisent un meme modele de disposition pour garder le bas du texte visible.",
            "Suppression de l'ancien layout OCR, des textes de dessin, des utilitaires de cadre d'avatar et d'une ancienne interface de notification.",
            "Les controles qualite couvrent maintenant l'analyse, les permissions, la disposition clavier, les anciens fichiers supprimes et les notes de version."
        )
    ),
    LocalReleaseNotesEntry(
        versionCode = 20154,
        versionName = "2.0.154",
        zh = listOf(
            "编辑器预览符号重新打磨，清单、列表和引用不再出现问号占位",
            "普通笔记搜索下沉到数据库查询，减少列表页全量内存过滤压力",
            "备份后台任务合并为单一 Worker，通过模式区分本地备份和 WebDAV 备份",
            "文件导入、导出、附件和 WebDAV 操作统一明确权限说明与拒绝选项",
            "删除未调用的旧格式入口和标签缓存路径，旧功能清理迁移改为显式兼容命名"
        ),
        en = listOf(
            "Editor preview glyphs were polished so checklists, bullets, and quotes no longer fall back to question marks.",
            "Normal note search now uses database queries to reduce full-list in-memory filtering.",
            "Backup background work now uses one mode-based Worker for local and WebDAV backups.",
            "File import, export, attachments, and WebDAV actions now share explicit permission disclosures and reject choices.",
            "Removed unused formatting and tag-cache paths, and renamed old cleanup migration as explicit compatibility code."
        ),
        fr = listOf(
            "Les symboles d'apercu de l'editeur sont corriges pour les listes, cases et citations.",
            "La recherche de notes utilise maintenant la base de donnees pour reduire le filtrage complet en memoire.",
            "Les sauvegardes utilisent un seul Worker avec un mode local ou WebDAV.",
            "L'import, l'export, les pieces jointes et WebDAV partagent des explications de permission explicites.",
            "Suppression d'anciens chemins inutilises et renommage clair de la migration de compatibilite."
        )
    ),
    LocalReleaseNotesEntry(
        versionCode = 20153,
        versionName = "2.0.153",
        zh = listOf(
            "隐私政策文案按当前笔记、记账、分类和备份范围重新收紧，移除旧功能披露",
            "Android ID 直接读取集中到 DeviceUtil，质量门阻止未同意前或散落读取回归",
            "笔记详情页拆分为更聚焦的编辑部件文件，降低单页复杂度并提升维护稳定性",
            "WebDAV 恢复增加恢复前预览，可先查看来源文件和可恢复数据再执行",
            "旧兼容标记不再作为独立内容显示，分类与全部列表继续保持真实笔记范围"
        ),
        en = listOf(
            "Privacy policy copy now matches the current notes, accounting, category, and backup scope.",
            "Direct Android ID access is centralized in DeviceUtil, with a quality gate to prevent scattered reads.",
            "The note detail screen was split into focused editor parts to reduce complexity and improve stability.",
            "WebDAV restore now supports a preview showing the source file and restorable data before recovery.",
            "Legacy compatibility markers no longer appear as standalone content in note lists."
        ),
        fr = listOf(
            "La politique de confidentialite correspond maintenant aux notes, comptes, categories et sauvegardes actuels.",
            "L'acces direct a Android ID est centralise dans DeviceUtil avec un controle qualite anti-regression.",
            "L'ecran de detail de note est separe en composants d'edition plus cibles pour plus de stabilite.",
            "La restauration WebDAV affiche maintenant un apercu du fichier source et des donnees restaurables.",
            "Les anciens marqueurs de compatibilite n'apparaissent plus comme contenus autonomes."
        )
    ),
    LocalReleaseNotesEntry(
        versionCode = 20152,
        versionName = "2.0.152",
        zh = listOf(
            "\u4fee\u6b63\u672c\u6b21\u7248\u672c\u66f4\u65b0\u5f39\u7a97\u76ee\u5f55\uff0c\u786e\u4fdd\u6253\u5f00\u540e\u663e\u793a\u7684\u5c31\u662f\u5f53\u524d\u7248\u672c\u6539\u52a8",
            "\u9690\u79c1\u5408\u89c4\u95e8\u7981\u7ee7\u7eed\u6536\u7d27\uff0c\u5907\u4efd\u8def\u5f84\u672a\u540c\u610f\u524d\u4f7f\u7528\u65e0\u5bb3\u6807\u8bc6\uff0c\u963b\u6b62\u8bbe\u5907\u7801\u63d0\u524d\u8bfb\u53d6",
            "\u8d85\u957f\u7b14\u8bb0\u4e2d\u7b49\u89c4\u6a21\u4fee\u6539\u8fdb\u5165\u4fdd\u62a4\u6027\u5ef6\u8fdf\u540c\u6b65\uff0c\u589e\u5f3a\u5927\u6bb5\u6587\u5b57\u4e66\u5199\u6d41\u7545\u5ea6",
            "\u672c\u5730\u5907\u4efd\u6062\u590d\u52a0\u5165\u6062\u590d\u524d\u9884\u89c8\uff0c\u5148\u770b\u7b14\u8bb0\u3001\u8bb0\u8d26\u3001\u5206\u7c7b\u548c\u8bbe\u7f6e\u6570\u91cf\u518d\u6267\u884c\u6062\u590d",
            "\u8865\u5145\u5408\u89c4\u3001\u7f16\u8f91\u5668\u548c\u5907\u4efd\u9884\u89c8\u56de\u5f52\u6d4b\u8bd5\uff0c\u53d1\u884c\u524d\u8d28\u91cf\u68c0\u67e5\u5df2\u8986\u76d6"
        ),
        en = listOf(
            "Corrected the local update dialog catalog so the installed version shows the exact changes for this build.",
            "Privacy gates remain stricter: backup paths use a harmless id before consent and block early device-id access.",
            "Moderate edits in ultra-long notes now use protective deferred sync for smoother long-form writing.",
            "Local backup restore now shows a pre-restore preview of notes, accounting records, categories, and settings.",
            "Added regression coverage for compliance, editor sync, and backup preview behavior before release."
        ),
        fr = listOf(
            "Correction du catalogue local afin que la fenetre de mise a jour affiche les changements exacts de cette version.",
            "Les controles de confidentialite restent renforces : les sauvegardes utilisent un identifiant inoffensif avant consentement.",
            "Les modifications moyennes dans les tres longues notes utilisent une synchronisation differee plus fluide.",
            "La restauration locale affiche un apercu des notes, comptes, categories et reglages avant execution.",
            "Ajout de tests de regression pour la conformite, la synchronisation de l'editeur et l'apercu de sauvegarde."
        )
    ),
    LocalReleaseNotesEntry(
        versionCode = 20151,
        versionName = "2.0.151",
        zh = listOf(
            "\u589e\u5f3a\u9690\u79c1\u5408\u89c4\u95e8\u7981\uff1a\u5907\u4efd\u8def\u5f84\u5728\u672a\u540c\u610f\u524d\u4fdd\u6301\u65e0\u5bb3\u7528\u6237\u6807\u8bc6\uff0c\u8d28\u91cf\u68c0\u67e5\u4f1a\u963b\u6b62\u8bbe\u5907\u7801\u56de\u5f52\u98ce\u9669",
            "\u8d85\u957f\u7b14\u8bb0\u4e2d\u7b49\u89c4\u6a21\u4fee\u6539\u6539\u4e3a\u4fdd\u62a4\u6027\u5ef6\u8fdf\u540c\u6b65\uff0c\u964d\u4f4e\u5927\u6bb5\u6587\u5b57\u4e66\u5199\u548c\u683c\u5f0f\u5316\u65f6\u7684\u5361\u987f",
            "\u672c\u5730\u5907\u4efd\u6062\u590d\u65b0\u589e\u6062\u590d\u524d\u9884\u89c8\uff0c\u53ef\u5148\u67e5\u770b\u7b14\u8bb0\u3001\u8bb0\u8d26\u3001\u5206\u7c7b\u548c\u8bbe\u7f6e\u6570\u91cf\u518d\u6267\u884c\u6062\u590d",
            "\u5907\u4efd\u6821\u9a8c\u548c\u6062\u590d\u9884\u89c8\u5171\u7528\u540c\u4e00\u5957\u89e3\u6790\u903b\u8f91\uff0c\u51cf\u5c11\u7a7a\u5907\u4efd\u6216\u5143\u6570\u636e\u5f02\u5e38\u5bfc\u81f4\u7684\u5931\u8d25",
            "\u8865\u5145\u56de\u5f52\u6d4b\u8bd5\uff0c\u9501\u5b9a\u5408\u89c4\u95e8\u7981\u3001\u8d85\u957f\u6587\u672c\u7b56\u7565\u548c\u5907\u4efd\u9884\u89c8\u884c\u4e3a"
        ),
        en = listOf(
            "Strengthened privacy compliance gates so backup paths keep a harmless pre-consent user id and quality checks block device-id regressions.",
            "Ultra-long notes now defer moderate edits more protectively, reducing stalls while writing or applying formatting to large text.",
            "Local backup restore now includes a pre-restore preview for notes, accounting records, categories, and settings before recovery starts.",
            "Backup verification and restore preview now share the same parsing checks to reject empty or invalid backup metadata earlier.",
            "Added regression coverage for compliance gates, ultra-long editor sync, and backup preview behavior."
        ),
        fr = listOf(
            "Renforcement des controles de confidentialite : les sauvegardes gardent un identifiant inoffensif avant consentement et le controle qualite bloque les regressions.",
            "Les tres longues notes differents davantage les modifications moyennes afin de reduire les blocages pendant l'ecriture ou le formatage.",
            "La restauration des sauvegardes locales affiche maintenant un apercu des notes, comptes, categories et reglages avant l'action.",
            "La verification et l'apercu des sauvegardes partagent la meme logique de lecture pour rejeter plus tot les donnees invalides.",
            "Ajout de tests de regression pour les controles de conformite, la synchronisation des longues notes et l'apercu de sauvegarde."
        )
    ),
    LocalReleaseNotesEntry(
        versionCode = 20150,
        versionName = "2.0.150",
        zh = listOf(
            "收紧安全环境检测，不再查询其他应用包名，降低应用商店对安装列表采集的误判风险",
            "清理启动层乱码、空延迟任务和强制 GC，低内存时改为轻量清理监控缓存",
            "详情页富文本渲染独立成文件，降低大段文字编辑和后续维护风险",
            "设置页无动作行不再显示可点击状态，开关行支持点击整行切换",
            "新增本地质量门禁脚本，构建前可检查破坏性迁移、旧依赖、隐私文案和备份策略"
        ),
        en = listOf(
            "Tightened security-environment checks so the app no longer queries other app packages, reducing store-review risk around installed-app collection.",
            "Cleaned startup mojibake, removed an empty delayed task, and replaced forced GC with lightweight monitor-cache cleanup.",
            "Moved rich note rendering into its own file to reduce risk while editing long notes and maintaining the detail page.",
            "Settings rows without actions no longer look clickable, and switch rows can now be toggled by tapping the full row.",
            "Added a local quality gate script for destructive migrations, old dependencies, privacy copy, and backup policy checks before release builds."
        ),
        fr = listOf(
            "Verification de securite resserree : l'application ne consulte plus les paquets d'autres applications, ce qui reduit le risque de revue lie aux applications installees.",
            "Nettoyage du demarrage : textes corrompus retires, tache differee vide supprimee et GC force remplace par un nettoyage leger du cache de suivi.",
            "Le rendu riche des notes est deplace dans un fichier dedie afin de reduire le risque lors de l'edition de longs textes.",
            "Les lignes de reglages sans action ne paraissent plus cliquables, et les lignes avec interrupteur se basculent sur toute la ligne.",
            "Ajout d'un script local de controle qualite pour verifier migrations destructives, anciennes dependances, textes de confidentialite et politique de sauvegarde."
        )
    ),
    LocalReleaseNotesEntry(
        versionCode = 20149,
        versionName = "2.0.149",
        zh = listOf(
            "移除数据库破坏性迁移入口，降低版本升级时静默清空数据的风险",
            "删除未调用的旧数据库构建器，数据库入口收敛为一处",
            "继续清理旧功能文案和日记兼容过滤，笔记列表只按归档状态隐藏内容",
            "移除未使用的 Markdown、图表和图片预览依赖，并同步隐私合规说明"
        ),
        en = listOf(
            "Removed destructive database migration fallbacks to reduce the risk of silent data loss during upgrades.",
            "Deleted the unused legacy database builder so the app has a single production database entry.",
            "Cleaned more old feature copy and removed diary compatibility filtering; notes are hidden only when archived.",
            "Removed unused Markdown, chart, and photo preview dependencies, and updated privacy disclosures accordingly."
        ),
        fr = listOf(
            "Suppression des migrations destructives afin de reduire le risque d'effacement silencieux des donnees lors des mises a jour.",
            "Suppression de l'ancien constructeur de base de donnees inutilise pour garder une seule entree de production.",
            "Nettoyage de textes d'anciennes fonctions et retrait du filtrage de compatibilite journal ; seules les notes archivees sont masquees.",
            "Suppression de dependances Markdown, graphique et apercu photo inutilisees, avec mise a jour des informations de confidentialite."
        )
    ),
    LocalReleaseNotesEntry(
        versionCode = 20148,
        versionName = "2.0.148",
        zh = listOf(
            "关于微简页面新增用户反馈入口，可直接调起邮箱发送问题或建议",
            "反馈邮件同时支持 tourisain@163.com 与 grllq458@gmail.com 两个收件地址",
            "补充中英法三语反馈文案，语言切换后保持一致体验",
            "新增反馈渠道回归测试，确保邮箱入口配置稳定"
        ),
        en = listOf(
            "Added a User Feedback entry in About Weijian for sending issues or suggestions by email.",
            "Feedback email now includes both tourisain@163.com and grllq458@gmail.com as recipients.",
            "Added Chinese, English, and French feedback text for a consistent localized experience.",
            "Added regression coverage to keep the email feedback channel configured correctly."
        ),
        fr = listOf(
            "Ajout d'une entree Commentaires dans A propos de Weijian pour envoyer problemes ou suggestions par e-mail.",
            "L'e-mail de feedback inclut tourisain@163.com et grllq458@gmail.com comme destinataires.",
            "Ajout des textes chinois, anglais et francais pour une experience localisee coherente.",
            "Ajout d'un test de regression pour stabiliser le canal de feedback par e-mail."
        )
    ),
    LocalReleaseNotesEntry(
        versionCode = 20147,
        versionName = "2.0.147",
        zh = listOf(
            "增强详情页编号列表书写体验，输入 1. 后回车会自动延续到下一项",
            "空编号行再次回车会自动退出列表，减少多余标记和盲删",
            "补充详情页书写稳定性回归测试，覆盖编号列表延续和退出场景",
            "保持轻量本地优化路线，不增加后台任务和额外依赖"
        ),
        en = listOf(
            "Improved numbered-list writing in note details: pressing Enter after 1. now continues to the next item.",
            "Pressing Enter on an empty numbered item exits the list, reducing stray markers and manual cleanup.",
            "Added writing-stability regression coverage for continuing and exiting numbered lists.",
            "Kept the optimization lightweight with no added background tasks or extra dependencies."
        ),
        fr = listOf(
            "Amelioration de l'ecriture des listes numerotees : Entree apres 1. continue maintenant l'element suivant.",
            "Entree sur un element numerote vide quitte la liste, avec moins de marqueurs inutiles.",
            "Ajout de tests de stabilite couvrant la continuation et la sortie des listes numerotees.",
            "Optimisation locale et legere, sans tache en arriere-plan ni dependance supplementaire."
        )
    ),
    LocalReleaseNotesEntry(
        versionCode = 20146,
        versionName = "2.0.146",
        zh = listOf(
            "修复会员中心开通会员弹窗长文本被遮挡的问题，联系信息现在可滚动查看",
            "官方网站页面新增 aureate.vip 与 axutongxue.com.cn 两个开发者网站",
            "会员状态票据新增 nonce 并加强永久会员形态校验，继续提升本地加密稳定性",
            "优化动态取色策略：仅在 Android 12 及以上的经典风格中启用，其他风格保持稳定配色"
        ),
        en = listOf(
            "Fixed clipped purchase text in Membership Center so contact details can be scrolled and read.",
            "Added aureate.vip and axutongxue.com.cn to the official websites page.",
            "Added a membership ticket nonce and stricter lifetime-membership shape checks for stronger local protection.",
            "Improved dynamic color: it now applies only on Android 12+ in Classic style while other styles keep stable palettes."
        ),
        fr = listOf(
            "Correction du texte d'achat coupe dans le centre d'abonnement ; les contacts sont maintenant consultables par defilement.",
            "Ajout de aureate.vip et axutongxue.com.cn a la page des sites officiels.",
            "Ajout d'un nonce au ticket d'abonnement et controle plus strict de l'abonnement a vie.",
            "Amelioration de la couleur dynamique : activee seulement sur Android 12+ en style classique."
        )
    ),
    LocalReleaseNotesEntry(
        versionCode = 20145,
        versionName = "2.0.145",
        zh = listOf(
            "修复写笔记时底部文字进入工具栏下方导致只能盲打的问题",
            "正文输入区会随键盘高度预留底部空间，让光标和新输入内容保持可见",
            "保留工具栏贴近键盘的紧凑体验，同时减少底部遮挡",
            "补充键盘避让回归测试，继续增强详情页书写稳定性"
        ),
        en = listOf(
            "Fixed note text being hidden under the bottom toolbar while typing near the end of a note.",
            "The editor now reserves space based on keyboard height so the cursor and new input stay visible.",
            "Kept the toolbar compact near the keyboard while reducing bottom obstruction.",
            "Added a keyboard avoidance regression test to improve note detail writing stability."
        ),
        fr = listOf(
            "Correction du texte masque sous la barre d'outils lors de l'ecriture en bas d'une note.",
            "La zone d'edition reserve maintenant de l'espace selon la hauteur du clavier pour garder le curseur visible.",
            "La barre d'outils reste compacte pres du clavier tout en reduisant le masquage en bas.",
            "Ajout d'un test de regression pour l'evitement du clavier et la stabilite de l'ecriture."
        )
    ),
    LocalReleaseNotesEntry(
        versionCode = 20144,
        versionName = "2.0.144",
        zh = listOf(
            "优化笔记详情页工具栏格式化，列表、引用和标题不再污染空白行",
            "详情页信息入口改为三语资源文案，减少硬编码和不自然字样",
            "降低 Gradle 构建默认内存占用，减少低内存环境下构建失败",
            "补充资源与格式化回归测试，继续增强书写稳定性"
        ),
        en = listOf(
            "Improved note detail toolbar formatting so list, quote, and heading actions no longer dirty blank lines.",
            "Moved the note info label to localized resources to remove hardcoded visible copy.",
            "Reduced default Gradle build memory usage to avoid low-memory build failures.",
            "Added resource and formatting regression tests to keep writing behavior stable."
        ),
        fr = listOf(
            "Amelioration du formatage dans le detail de note : listes, citations et titres ne salissent plus les lignes vides.",
            "Le libelle d'information de note utilise maintenant les ressources traduites.",
            "Memoire Gradle par defaut reduite pour eviter les echecs en environnement limite.",
            "Ajout de tests de regression pour les ressources et le formatage."
        )
    ),
    LocalReleaseNotesEntry(
        versionCode = 20143,
        versionName = "2.0.143",
        zh = listOf(
            "继续删除旧功能文案，清理旅行记忆、时间胶囊、短信读取和旧隐私短文案残留",
            "新增资源范围测试，防止日记、预算、倒数日等旧功能文案重新进入核心版本",
            "修复删除多行旧隐私文案后可能留下裸文本导致资源编译失败的问题",
            "清理无内容空目录，让工程结构继续向笔记与本地记账核心收敛"
        ),
        en = listOf(
            "Removed more old feature copy, including travel memory, time capsule, SMS reading, and legacy short privacy text.",
            "Added a resource scope test to keep removed diary, budget, countdown, and similar copy out of the core app.",
            "Fixed resource compilation risk from leftover plain text after deleting legacy multiline privacy strings.",
            "Cleaned empty directories so the project structure keeps converging around notes and local accounting."
        ),
        fr = listOf(
            "Suppression de textes residuels lies aux anciennes fonctions et aux anciens courts textes de confidentialite.",
            "Ajout d'un test de portee des ressources pour eviter le retour des textes d'anciennes fonctions.",
            "Correction du risque de compilation cause par des fragments de texte nus apres suppression d'anciennes chaines multilignes.",
            "Nettoyage des dossiers vides afin de recentrer le projet sur les notes et la comptabilite locale."
        )
    ),
    LocalReleaseNotesEntry(
        versionCode = 20142,
        versionName = "2.0.142",
        zh = listOf(
            "继续收紧资源文案，删除未调用的日记、预算、倒数日和记录森林旧字符串",
            "会员权益和记账描述不再提及预算功能，统一为收支分析和分类统计",
            "重新验证资源编译，避免旧文案残留影响应用商店审核",
            "保持核心界面不变，只做残留清理和稳定性验证"
        ),
        en = listOf(
            "Further tightened resource text by removing unused diary, budget, countdown, and record-forest strings.",
            "Membership and accounting descriptions no longer mention budgets, using income-expense analysis and category stats instead.",
            "Revalidated resource compilation to avoid store-review ambiguity from stale copy.",
            "Kept the core interface unchanged while cleaning residual resources and verifying stability."
        ),
        fr = listOf(
            "Textes de ressources encore resserres en retirant les anciennes chaines inutilisees.",
            "Les descriptions d'abonnement et de comptabilite ne mentionnent plus les budgets.",
            "Compilation des ressources reverifiee pour eviter toute ambiguite en revue boutique.",
            "Interface principale conservee, avec nettoyage des restes et verification de stabilite."
        )
    ),
    LocalReleaseNotesEntry(
        versionCode = 20141,
        versionName = "2.0.141",
        zh = listOf(
            "完成三刀收缩：移除预算、日记、倒数日、课程、森林等旧功能文件和入口",
            "数据库迁移会清理旧表，备份范围收敛为笔记、记账、分类、设置和用户资料",
            "隐私与合规文档同步核心功能范围，减少上架审核中的旧功能歧义",
            "首页更多区改为常用，保留现有界面风格但削弱杂项感",
            "删除未调用的 Markdown 文档表和旧列表函数，降低维护噪音"
        ),
        en = listOf(
            "Completed the focused cleanup: removed old budget, diary, countdown, course, and forest feature files and entries.",
            "Database migration now clears old tables, and backup scope is reduced to notes, accounting, categories, settings, and profile data.",
            "Privacy and compliance documents now match the core feature scope to reduce store-review ambiguity.",
            "The home More section is now Common, keeping the visual style while reducing miscellaneous clutter.",
            "Removed the unused Markdown document table and old list function to lower maintenance noise."
        ),
        fr = listOf(
            "Nettoyage cible termine : anciens fichiers et entrees de budget, journal, compte a rebours, cours et foret retires.",
            "La migration nettoie les anciennes tables, et la sauvegarde se limite aux notes, comptes, categories, reglages et profil.",
            "Les documents de confidentialite et de conformite correspondent maintenant au coeur fonctionnel.",
            "La section Plus de l'accueil devient Courant, avec moins de bruit visuel.",
            "Suppression de la table Markdown inutilisee et d'une ancienne fonction de liste."
        )
    ),
    LocalReleaseNotesEntry(
        versionCode = 20140,
        versionName = "2.0.140",
        zh = listOf(
            "\u65b0\u589e\u7f16\u8f91\u72b6\u6001\u673a\uff0c\u533a\u5206\u8f93\u5165\u3001\u9009\u62e9\u3001\u683c\u5f0f\u5316\u548c\u6062\u590d\u72b6\u6001\uff0c\u51cf\u5c11\u8be6\u60c5\u9875\u8bef\u89e6\u53d1\u91cd\u6e32\u67d3",
            "\u5de5\u5177\u680f\u64cd\u4f5c\u6539\u4e3a\u4e8b\u52a1\u63d0\u4ea4\uff0c\u53ea\u6709\u5185\u5bb9\u6216\u9009\u533a\u771f\u5b9e\u53d8\u5316\u65f6\u624d\u540c\u6b65\u4fdd\u5b58",
            "\u8d85\u957f\u6587\u672c\u542f\u7528\u66f4\u7a33\u7684\u5ef6\u8fdf\u540c\u6b65\u7b56\u7565\uff0c\u5927\u6bb5\u4e66\u5199\u548c\u9009\u62e9\u540e\u5e94\u7528\u5de5\u5177\u66f4\u6d41\u7545",
            "\u5bcc\u6587\u672c\u9884\u89c8\u907f\u5f00\u9009\u62e9\u548c\u683c\u5f0f\u5316\u8fc7\u7a0b\uff0c\u4f18\u5148\u4fdd\u8bc1\u5149\u6807\u548c\u952e\u76d8\u54cd\u5e94",
            "\u5b8c\u5584\u8be6\u60c5\u9875\u6027\u80fd\u6d4b\u8bd5\uff0c\u8986\u76d6\u7f16\u8f91\u72b6\u6001\u3001\u540c\u6b65\u5ef6\u8fdf\u548c\u5de5\u5177\u680f\u63d0\u4ea4\u8fb9\u754c"
        ),
        en = listOf(
            "Added a lightweight editor state machine for typing, selection, formatting, and recovery states.",
            "Toolbar formatting now commits as a transaction only when text or selection actually changes.",
            "Ultra-long notes use a steadier deferred sync path for smoother writing and selection formatting.",
            "Rich previews stay off during selection and formatting so cursor and keyboard response remain first.",
            "Expanded detail page performance tests around editor state, sync delay, and toolbar commit boundaries."
        ),
        fr = listOf(
            "Ajout d'une machine d'etat legere pour la saisie, la selection, le formatage et la recuperation.",
            "La barre d'outils valide maintenant les actions en transaction uniquement si le texte ou la selection change.",
            "Les tres longues notes utilisent une synchronisation differee plus stable pour une ecriture plus fluide.",
            "Les apercus riches restent desactives pendant la selection et le formatage pour garder le curseur reactif.",
            "Tests de performance etendus pour l'etat d'edition, le delai de synchronisation et les actions de barre d'outils."
        )
    ),
    LocalReleaseNotesEntry(
        versionCode = 20139,
        versionName = "2.0.139",
        zh = listOf(
            "\u589e\u5f3a\u7b14\u8bb0\u8be6\u60c5\u9875\u8f93\u5165\u7a33\u5b9a\u6027\uff0c\u957f\u6587\u672c\u8f93\u5165\u65f6\u66f4\u5c11\u5361\u987f",
            "\u5bcc\u6587\u672c\u6e32\u67d3\u6539\u4e3a\u505c\u987f\u540e\u518d\u542f\u7528\uff0c\u6253\u5b57\u8fc7\u7a0b\u4f18\u5148\u4fdd\u6301\u6d41\u7545",
            "\u957f\u6587\u672c\u5c0f\u5e45\u8f93\u5165\u4f1a\u5ef6\u8fdf\u540c\u6b65\u5230\u5e95\u5c42\u72b6\u6001\uff0c\u51cf\u5c11\u9891\u7e41\u91cd\u7ec4\u548c\u4fdd\u5b58\u538b\u529b",
            "\u4fdd\u5b58\u3001\u8fd4\u56de\u548c\u5207\u540e\u53f0\u524d\u4f1a\u5148\u540c\u6b65\u672a\u63d0\u4ea4\u7684\u8f93\u5165\u5185\u5bb9\uff0c\u964d\u4f4e\u4e22\u5b57\u98ce\u9669",
            "\u4f18\u5316\u5de5\u5177\u680f\u683c\u5f0f\u64cd\u4f5c\u540e\u7684\u9009\u533a\u4f4d\u7f6e\uff0c\u659c\u4f53\u548c\u4ee3\u7801\u5757\u66f4\u7a33\u5b9a"
        ),
        en = listOf(
            "Improved note detail input stability so long text editing causes fewer stalls.",
            "Rich text rendering now waits until typing settles, keeping active typing smooth first.",
            "Small edits in large notes defer syncing to the lower state layer, reducing recomposition and save pressure.",
            "Save, back, and background transitions flush pending input before persisting to reduce lost-character risk.",
            "Formatting actions keep selections more stable after italic and code block operations."
        ),
        fr = listOf(
            "Stabilite de saisie amelioree dans le detail de note, avec moins de ralentissements sur les longs textes.",
            "Le rendu riche attend une pause de saisie afin de privilegier la fluidite pendant l'ecriture.",
            "Les petites modifications dans les grandes notes sont synchronisees avec delai pour reduire les recompositions.",
            "L'enregistrement, le retour et le passage en arriere-plan synchronisent d'abord la saisie en attente.",
            "Les actions de mise en forme gardent une selection plus stable apres l'italique et les blocs de code."
        )
    ),
    LocalReleaseNotesEntry(
        versionCode = 20138,
        versionName = "2.0.138",
        zh = listOf(
            "\u5168\u5c40\u89c6\u89c9\u8282\u594f\u7edf\u4e00\uff0c\u5706\u89d2\u3001\u884c\u9ad8\u3001\u56fe\u6807\u5c3a\u5bf8\u66f4\u514b\u5236",
            "\u8bbe\u7f6e\u9875\u884c\u9879\u6536\u7d27\u5e76\u7edf\u4e00\u56fe\u6807\u5c3a\u5bf8\uff0c\u9875\u9762\u66f4\u50cf\u7cfb\u7edf\u8bbe\u7f6e",
            "\u5f39\u7a97\u4f7f\u7528\u7edf\u4e00\u6700\u5927\u9ad8\u5ea6\u548c\u8f7b\u91cf\u5bb9\u5668\uff0c\u51cf\u5c11\u906e\u6321\u611f",
            "\u7b14\u8bb0\u5217\u8868\u884c\u9879\u66f4\u8f7b\uff0c\u65f6\u95f4\u548c\u5206\u7c7b\u6539\u7528\u4e2d\u70b9\u5206\u9694",
            "\u7f6e\u9876\u548c\u666e\u901a\u7b14\u8bb0\u72b6\u6001\u66f4\u4f4e\u8c03\uff0c\u51cf\u5c11\u5217\u8868\u89c6\u89c9\u5e72\u6270"
        ),
        en = listOf(
            "Global visual rhythm is now more consistent across corners, row height, and icon sizing.",
            "Settings rows are tighter and use unified icons, making settings feel calmer and more system-like.",
            "Dialogs use a shared maximum height and lighter container behavior to reduce visual blocking.",
            "Note list rows are lighter, with time and folder separated by a quiet middle dot.",
            "Pinned and normal note states are more restrained, reducing visual noise in long lists."
        ),
        fr = listOf(
            "Le rythme visuel global est plus coherent pour les angles, les hauteurs de ligne et les icones.",
            "Les lignes de reglages sont plus compactes avec des icones unifiees, pour un rendu plus calme.",
            "Les fenetres utilisent une hauteur maximale commune et un conteneur plus leger.",
            "Les lignes de la liste de notes sont plus legeres, avec l'heure et le dossier separes par un point median.",
            "Les etats epingle et normal sont plus discrets afin de reduire le bruit visuel."
        )
    ),
    LocalReleaseNotesEntry(
        versionCode = 20137,
        versionName = "2.0.137",
        zh = listOf(
            "\u66f4\u65b0\u5f39\u7a97\u73b0\u5728\u4f1a\u6309\u5f53\u524d\u7248\u672c\u663e\u793a\u672c\u6b21\u771f\u5b9e\u66f4\u65b0\u5185\u5bb9",
            "\u6781\u7b80\u5199\u4f5c\u9875\u7ee7\u7eed\u6536\u675f\uff0c\u6b63\u6587\u533a\u66f4\u50cf\u5e72\u51c0\u7eb8\u9762",
            "\u7b14\u8bb0\u4fe1\u606f\u5165\u53e3\u53d8\u4e3a\u8f7b\u91cf\u5355\u884c\uff0c\u51cf\u5c11\u7f16\u8f91\u533a\u89c6\u89c9\u5e72\u6270",
            "\u5e95\u90e8\u5de5\u5177\u680f\u7ee7\u7eed\u53d8\u8584\uff0c\u683c\u5f0f\u5de5\u5177\u6309\u9700\u5c55\u5f00",
            "\u589e\u52a0\u672c\u5730\u7248\u672c\u66f4\u65b0\u8bf4\u660e\u76ee\u5f55\uff0c\u907f\u514d\u540e\u7eed\u7248\u672c\u91cd\u590d\u663e\u793a\u65e7\u5185\u5bb9"
        ),
        en = listOf(
            "The update dialog now shows the real release notes for the installed version.",
            "The minimal writing page is cleaner, with the editor feeling more like a quiet sheet of paper.",
            "Note metadata is shown as a lightweight single line to reduce visual noise while writing.",
            "The bottom toolbar is slimmer, and formatting tools expand only when needed.",
            "A local versioned release notes catalog prevents future versions from reusing old update text."
        ),
        fr = listOf(
            "La fenetre de mise a jour affiche maintenant les notes de version reelles de la version installee.",
            "La page d'ecriture minimale est plus propre, avec une zone de texte plus proche d'une feuille calme.",
            "Les informations de note sont affichees sur une ligne legere afin de reduire le bruit visuel.",
            "La barre d'outils du bas est plus fine, et les outils de mise en forme ne s'ouvrent qu'au besoin.",
            "Un catalogue local de notes de version evite de reutiliser les anciens textes lors des prochaines mises a jour."
        )
    ),
    LocalReleaseNotesEntry(
        versionCode = 20136,
        versionName = "2.0.136",
        zh = listOf(
            "\u7b14\u8bb0\u8be6\u60c5\u9875\u6539\u4e3a\u6781\u7b80\u5199\u4f5c\u6a21\u5f0f",
            "\u5c06\u5206\u7c7b\u3001\u6807\u7b7e\u548c\u9644\u4ef6\u6536\u62e2\u5230\u4fe1\u606f\u5165\u53e3",
            "\u5e95\u90e8\u5de5\u5177\u680f\u5206\u4e3a\u5e38\u7528\u884c\u548c\u683c\u5f0f\u884c\uff0c\u964d\u4f4e\u4e66\u5199\u5e72\u6270"
        ),
        en = listOf(
            "The note detail page now uses a minimal writing mode.",
            "Folders, tags, and attachments are grouped under a single info entry.",
            "The bottom toolbar is split into common actions and formatting actions to reduce distraction."
        ),
        fr = listOf(
            "La page de detail de note utilise maintenant un mode d'ecriture minimal.",
            "Les dossiers, etiquettes et pieces jointes sont regroupes dans une entree d'information.",
            "La barre d'outils du bas separe les actions courantes et les actions de mise en forme."
        )
    )
)
