param(
    [switch]$SkipTests
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

function Fail($message) {
    Write-Host "[FAIL] $message" -ForegroundColor Red
    exit 1
}

function Pass($message) {
    Write-Host "[OK] $message" -ForegroundColor Green
}

function Assert-NoMatch {
    $pattern = [string]$args[0]
    $message = [string]$args[$args.Count - 1]
    $pathList = New-Object System.Collections.Generic.List[string]
    if ($args.Count -gt 2) {
        for ($index = 1; $index -lt ($args.Count - 1); $index++) {
            foreach ($path in @($args[$index])) {
                if (-not [string]::IsNullOrWhiteSpace([string]$path)) {
                    $pathList.Add([string]$path)
                }
            }
        }
    }
    $searchPaths = [string[]]$pathList.ToArray()
    if ($searchPaths.Count -eq 0) {
        Fail "Assert-NoMatch has no search paths for pattern: $pattern"
    }
    $rgArgs = @("-n", "--fixed-strings", "--", $pattern) + $searchPaths
    $previousErrorActionPreference = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    $result = & rg @rgArgs 2>$null
    $exitCode = $LASTEXITCODE
    $ErrorActionPreference = $previousErrorActionPreference
    if ($exitCode -eq 2) {
        Fail "rg failed for Assert-NoMatch args: $($rgArgs -join ' | ')"
    }
    if ($exitCode -eq 0) {
        Write-Host $result
        Fail $message
    }
}

if (-not (Get-Command rg -ErrorAction SilentlyContinue)) {
    Fail "ripgrep (rg) is required for quality_check.ps1"
}

Assert-NoMatch "fallbackToDestructiveMigration" @("app/src/main/java", "app/src/main/res", "app/build.gradle.kts") "Destructive Room migration must not return."
Assert-NoMatch "<queries>" @("app/src/main/AndroidManifest.xml") "Manifest package visibility queries must not return."
Assert-NoMatch "System.gc" @("app/src/main/java") "Forced garbage collection must not return."
$privacyInstalledAppsToken = [string]::Concat(
    [char]0x8F6F,
    [char]0x4EF6,
    [char]0x5B89,
    [char]0x88C5,
    [char]0x5217,
    [char]0x8868
)
Assert-NoMatch $privacyInstalledAppsToken @("app/src/main/java", "app/src/main/res") "Installed-app-list privacy wording must not return."
Assert-NoMatch "android:allowBackup=`"true`"" @("app/src/main/AndroidManifest.xml") "Platform auto backup must remain disabled."

$backupRepositoryText = Get-Content "app/src/main/java/com/tourisain/weijian/data/repository/BackupRepository.kt" -Raw
if (-not $backupRepositoryText.Contains("PRE_CONSENT_USER_ID")) {
    Fail "BackupRepository must keep a harmless pre-consent user id."
}
if (-not $backupRepositoryText.Contains("isPrivacyPolicyAccepted.first()") -or -not $backupRepositoryText.Contains("isUserAgreementAccepted.first()")) {
    Fail "BackupRepository must check privacy policy and user agreement before device id fallback."
}
if (-not $backupRepositoryText.Contains("if (hasRequiredConsent) DeviceUtil.getDeviceId(context) else PRE_CONSENT_USER_ID")) {
    Fail "BackupRepository must not call DeviceUtil.getDeviceId before consent."
}

$deviceUtilText = Get-Content "app/src/main/java/com/tourisain/weijian/util/DeviceUtil.kt" -Raw
if (-not $deviceUtilText.Contains("Settings.Secure.ANDROID_ID")) {
    Fail "DeviceUtil Android ID access moved; re-audit consent gates before release."
}

$androidIdSources = & rg -l --fixed-strings "Settings.Secure.ANDROID_ID" "app/src/main/java" 2>$null
$unexpectedAndroidIdSources = @($androidIdSources) |
    ForEach-Object { $_.Replace('\', '/') } |
    Where-Object { $_ -ne "app/src/main/java/com/tourisain/weijian/util/DeviceUtil.kt" }
if ($unexpectedAndroidIdSources.Count -gt 0) {
    Write-Host $unexpectedAndroidIdSources
    Fail "Android ID must be accessed only through DeviceUtil."
}

# Removed privacy scope tokens: 日记 预算 倒数日 待办
$legacyPrivacyScopeTokens = @(
    [string]::Concat([char]0x65E5, [char]0x8BB0),
    [string]::Concat([char]0x9884, [char]0x7B97),
    [string]::Concat([char]0x5012, [char]0x6570, [char]0x65E5),
    [string]::Concat([char]0x5F85, [char]0x529E)
)
foreach ($token in $legacyPrivacyScopeTokens) {
    Assert-NoMatch $token @("app/src/main/java/com/tourisain/weijian/presentation/privacy/PrivacyPolicyScreen.kt") "Privacy policy must not disclose removed standalone scope: $token"
}

$legacyTokens = @(
    "Markwon",
    "PhotoView",
    "MPAndroidChart",
    "io.noties.markwon",
    "advanced_pomodoro",
    "study_room",
    "theme_store",
    "team_notification",
    "no_team_notifications"
)
foreach ($token in $legacyTokens) {
    Assert-NoMatch $token @("app/src/main/java", "app/src/main/res", "app/build.gradle.kts", "app/proguard-rules.pro") "Legacy token must not return: $token"
}

$deletedLegacyFiles = @(
    "app/src/main/res/layout/ocr_result_dialog.xml",
    "app/src/main/java/com/tourisain/weijian/util/AvatarUtil.kt",
    "app/src/main/java/com/tourisain/weijian/util/AvatarFrameUtil.kt",
    "app/src/main/java/com/tourisain/weijian/data/model/AvatarFrame.kt",
    "app/src/main/java/com/tourisain/weijian/domain/model/Notification.kt",
    "app/src/main/java/com/tourisain/weijian/domain/repository/NotificationRepository.kt"
)
foreach ($path in $deletedLegacyFiles) {
    if (Test-Path $path) {
        Fail "Unused legacy file must stay deleted: $path"
    }
}
Assert-NoMatch 'name="drawing"' @("app/src/main/res/values", "app/src/main/res/values-en", "app/src/main/res/values-fr") "Drawing feature copy must not return."
Assert-NoMatch 'name="drawing_list"' @("app/src/main/res/values", "app/src/main/res/values-en", "app/src/main/res/values-fr") "Drawing list copy must not return."

$accountEditText = Get-Content "app/src/main/java/com/tourisain/weijian/presentation/account/edit/AccountEditViewModel.kt" -Raw
if (-not (Test-Path "app/src/main/java/com/tourisain/weijian/presentation/account/edit/AccountNoteParser.kt")) {
    Fail "AccountNoteParser.kt must keep account note parsing outside the ViewModel."
}
foreach ($token in @("parseAmountAndCurrencyClean", "extractAmountFromTextClean", "buildStandardNote(")) {
    if ($accountEditText.Contains($token)) {
        Fail "Duplicate account parsing path must not return: $token"
    }
}

$profileSource = Get-Content "app/src/main/java/com/tourisain/weijian/presentation/profile/ProfileScreen.kt" -Raw
$behaviorSource = Get-Content "app/src/main/java/com/tourisain/weijian/presentation/settings/BehaviorSettingsScreen.kt" -Raw
if (-not $profileSource.Contains("PermissionDisclosureScenario.Avatar")) {
    Fail "Avatar picker must use unified permission disclosure."
}
if (-not $behaviorSource.Contains("PermissionDisclosureScenario.Notification") -or -not $behaviorSource.Contains("PermissionDisclosureScenario.ExactAlarm")) {
    Fail "Reminder settings must disclose notification and exact alarm use before enabling."
}

$detailScreenSource = Get-Content "app/src/main/java/com/tourisain/weijian/presentation/note/detail/NoteDetailScreen.kt" -Raw
$layoutModelSource = Get-Content "app/src/main/java/com/tourisain/weijian/presentation/note/detail/NoteDetailLayoutModel.kt" -Raw
if (-not $detailScreenSource.Contains("editorContentBottomPaddingDp")) {
    Fail "Note detail content must use the shared keyboard/toolbar padding model."
}
if (-not $layoutModelSource.Contains("fun editorContentBottomPaddingDp")) {
    Fail "Keyboard/toolbar content padding model must stay in NoteDetailLayoutModel."
}

$stringsText = Get-Content "app/src/main/res/values/strings.xml" -Raw
if (-not $stringsText.Contains("whats_new_current_notes") -or -not $stringsText.Contains("<item>")) {
    Fail "Current release notes must remain populated for the after-update dialog."
}

$backupWorkerClasses = & rg -n "class\s+BackupWorker\b" "app/src/main/java" 2>$null
if (@($backupWorkerClasses).Count -ne 1) {
    Write-Host $backupWorkerClasses
    Fail "Backup work must keep a single mode-based BackupWorker implementation."
}

Assert-NoMatch 'TODO_UNCHECKED = ?' @("app/src/main/java/com/tourisain/weijian/presentation/note/detail/RichNoteVisualTransformation.kt") "Rich checklist glyphs must not fall back to question marks."
Assert-NoMatch 'BULLET_PREFIX = ?' @("app/src/main/java/com/tourisain/weijian/presentation/note/detail/RichNoteVisualTransformation.kt") "Rich list glyphs must not fall back to question marks."
Assert-NoMatch "fun formatText(" @("app/src/main/java/com/tourisain/weijian/presentation/note/detail/NoteDetailViewModel.kt") "Old whole-content formatting entry must not return."
Assert-NoMatch "fun getAllTags(" @(
    "app/src/main/java/com/tourisain/weijian/presentation/note/list/NoteListViewModel.kt",
    "app/src/main/java/com/tourisain/weijian/data/repository/NoteRepository.kt",
    "app/src/main/java/com/tourisain/weijian/data/database/dao/NoteDao.kt"
) "Unused tag cache path must not return."

$requiredFocusTokens = @(
    "NoteListDataSource.SearchAllNotes",
    "PermissionDisclosureScenario.WebDavNetwork",
    "LEGACY_FEATURE_CLEANUP_MIGRATION_78_79"
)
foreach ($token in $requiredFocusTokens) {
    $result = & rg -n --fixed-strings $token "app/src/main/java" 2>$null
    if ($LASTEXITCODE -ne 0) {
        Fail "Focused cleanup guard token missing: $token"
    }
}

$mojibakeTokens = @(
    [char]0x9352,
    [char]0x7ED4,
    [char]0x95C8,
    [char]0x6D93,
    [char]0x6434,
    [char]0x7477,
    [char]0x7BE3
)
foreach ($token in $mojibakeTokens) {
    Assert-NoMatch $token @("app/src/main/java/com/tourisain/weijian/MemoApplication.kt", "app/src/main/java/com/tourisain/weijian/MainActivity.kt") "Startup mojibake must not return: $token"
}

Pass "Static quality checks passed."

if (-not $SkipTests) {
    & .\gradlew.bat :app:testReleaseUnitTest
    if ($LASTEXITCODE -ne 0) {
        Fail "Release unit tests failed."
    }
    Pass "Release unit tests passed."
}
