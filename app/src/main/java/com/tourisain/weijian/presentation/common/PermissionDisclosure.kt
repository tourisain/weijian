package com.tourisain.weijian.presentation.common

enum class PermissionDisclosureScenario {
    FileImport,
    FileExport,
    Attachment,
    Avatar,
    Notification,
    ExactAlarm,
    WebDavNetwork
}

data class PermissionDisclosure(
    val title: String,
    val message: String,
    val confirmLabel: String,
    val dismissLabel: String
)

fun permissionDisclosureFor(scenario: PermissionDisclosureScenario): PermissionDisclosure {
    return when (scenario) {
        PermissionDisclosureScenario.FileImport -> PermissionDisclosure(
            title = "选择文件权限说明",
            message = "用途：仅用于读取您主动选择的备份文件并恢复到本机。\n范围：微简不会扫描其他文件，也不会上传您的文件内容。\n拒绝后：本次恢复会取消，其他功能不受影响。",
            confirmLabel = "同意并选择",
            dismissLabel = "拒绝并取消"
        )
        PermissionDisclosureScenario.FileExport -> PermissionDisclosure(
            title = "保存文件权限说明",
            message = "用途：仅用于把您主动导出的笔记或备份保存到指定位置。\n范围：微简只写入您确认的文件，不会读取目录中的其他内容。\n拒绝后：本次导出会取消，原数据不受影响。",
            confirmLabel = "同意并保存",
            dismissLabel = "拒绝并取消"
        )
        PermissionDisclosureScenario.Attachment -> PermissionDisclosure(
            title = "附件访问说明",
            message = "用途：仅用于把您主动选择的文件作为当前笔记附件。\n范围：微简只保存该文件的访问地址，不会扫描相册、音视频或其他文件。\n拒绝后：本次添加附件会取消，笔记内容不受影响。",
            confirmLabel = "同意并选择",
            dismissLabel = "拒绝并取消"
        )
        PermissionDisclosureScenario.Avatar -> PermissionDisclosure(
            title = "头像访问说明",
            message = "用途：仅用于读取您主动选择的图片作为本地头像。\n范围：微简不会扫描相册，也不会上传头像。\n拒绝后：头像保持不变。",
            confirmLabel = "同意并选择",
            dismissLabel = "拒绝并取消"
        )
        PermissionDisclosureScenario.Notification -> PermissionDisclosure(
            title = "通知权限说明",
            message = "用途：仅用于您主动开启的本地提醒和必要状态通知。\n范围：微简不会用于广告推送。\n拒绝后：提醒可能无法按时显示，其他记录功能不受影响。",
            confirmLabel = "同意并继续",
            dismissLabel = "拒绝并取消"
        )
        PermissionDisclosureScenario.ExactAlarm -> PermissionDisclosure(
            title = "精确提醒权限说明",
            message = "用途：仅用于在您设定的时间触发本地提醒。\n范围：不会收集位置、联系人或其他个人信息。\n拒绝后：提醒可能延迟或无法触发。",
            confirmLabel = "同意并继续",
            dismissLabel = "拒绝并取消"
        )
        PermissionDisclosureScenario.WebDavNetwork -> PermissionDisclosure(
            title = "WebDAV 网络访问说明",
            message = "用途：仅用于连接您配置的 WebDAV 服务器，执行备份、预览和恢复。\n范围：微简不会连接非您配置的服务器，不用于广告、画像或营销。\n拒绝后：WebDAV 相关操作会取消，本地记录功能不受影响。",
            confirmLabel = "同意并继续",
            dismissLabel = "拒绝并取消"
        )
    }
}
