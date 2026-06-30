<#
.SYNOPSIS
Terminal-friendly Weijian WJ5 activation code generator.

.EXAMPLE
.\tools\generate_activation_code.ps1 -DeviceCode WJDC-ABCD-EF12-3456-7890-ABCD-EF12 -Level lifetime -Details

.EXAMPLE
.\tools\generate_activation_code.ps1 -DeviceCodeFile .\devices.txt -Level lifetime -Count 2 -Save -Format csv
#>
[CmdletBinding()]
param(
    [string]$DeviceCode,
    [string]$DeviceCodeFile,
    [switch]$FromClipboard,
    [ValidateSet("lifetime")]
    [string]$Level = "lifetime",
    [string]$Nonce,
    [int]$Count = 1,
    [ValidateSet("text", "json", "csv")]
    [string]$Format = "text",
    [string]$Output,
    [string]$OutputDir,
    [switch]$Save,
    [switch]$Details,
    [switch]$Copy,
    [switch]$OpenOutput,
    [switch]$Interactive,
    [switch]$Quiet,
    [switch]$NoPause,
    [switch]$NoBanner,
    [switch]$SelfTest,
    [switch]$Examples
)

Set-StrictMode -Version 2.0
$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$PythonScript = Join-Path $ScriptDir "generate_activation_code.py"

function Write-Banner {
    if ($NoBanner -or $Quiet) { return }
    Write-Host ""
    Write-Host "Weijian WJ5 activation code tool" -ForegroundColor Cyan
    Write-Host "Protocol: WJ5 only. Legacy WJ3/WJ4 codes are not generated." -ForegroundColor DarkGray
    Write-Host ""
}

function Write-Examples {
    Write-Host "Examples:"
    Write-Host "  .\tools\generate_activation_code.ps1 -DeviceCode WJDC-ABCD-EF12-3456-7890-ABCD-EF12 -Level lifetime -Details"
    Write-Host "  .\tools\generate_activation_code.ps1 -FromClipboard -Level lifetime -Save -Format json -Copy"
    Write-Host "  .\tools\generate_activation_code.ps1 -DeviceCodeFile .\devices.txt -Level lifetime -Count 2 -Save -Format csv"
    Write-Host "  .\tools\generate_activation_code.ps1 -SelfTest"
}

function Wait-BeforeExit {
    if ($Quiet -or $NoPause) { return }
    Write-Host ""
    Write-Host "Press Enter to exit..." -ForegroundColor DarkGray
    [void](Read-Host)
}

function Get-PythonCommand {
    $python = Get-Command python -ErrorAction SilentlyContinue
    if ($python) {
        return @{ Command = $python.Source; Prefix = @() }
    }

    $pyLauncher = Get-Command py -ErrorAction SilentlyContinue
    if ($pyLauncher) {
        return @{ Command = $pyLauncher.Source; Prefix = @("-3") }
    }

    throw "Python was not found. Install Python or add python.exe to PATH."
}

function Invoke-PythonGenerator {
    param(
        [string[]]$Arguments,
        [switch]$Capture
    )

    if (-not (Test-Path -LiteralPath $PythonScript)) {
        throw "Missing generator: $PythonScript"
    }

    $python = Get-PythonCommand
    $toolArgs = @()
    $toolArgs += $python.Prefix
    $toolArgs += $PythonScript
    $toolArgs += $Arguments

    if ($Capture) {
        $output = & $python.Command @toolArgs 2>&1
        $exitCode = $LASTEXITCODE
        $text = ($output | Out-String).Trim()
        if ($exitCode -ne 0) {
            throw $text
        }
        return $text
    }

    & $python.Command @toolArgs
    if ($LASTEXITCODE -ne 0) {
        throw "Generator failed with exit code $LASTEXITCODE."
    }
}

function Get-ClipboardText {
    $command = Get-Command Get-Clipboard -ErrorAction SilentlyContinue
    if (-not $command) {
        throw "Get-Clipboard is not available in this PowerShell session."
    }

    if ($command.Parameters.ContainsKey("Raw")) {
        return (Get-Clipboard -Raw)
    }
    return ((Get-Clipboard) -join [Environment]::NewLine)
}

function Resolve-OutputPath {
    param([string]$Path)
    if ([System.IO.Path]::IsPathRooted($Path)) {
        return [System.IO.Path]::GetFullPath($Path)
    }
    return [System.IO.Path]::GetFullPath((Join-Path (Get-Location) $Path))
}

function New-AutoOutputPath {
    $baseDir = $OutputDir
    if ([string]::IsNullOrWhiteSpace($baseDir)) {
        $baseDir = Join-Path $ScriptDir "activation_exports"
    }
    $resolvedDir = Resolve-OutputPath $baseDir
    New-Item -ItemType Directory -Force -Path $resolvedDir | Out-Null

    $stamp = Get-Date -Format "yyyyMMdd_HHmmss"
    $extension = $Format.ToLowerInvariant()
    return Join-Path $resolvedDir "weijian_wj5_${Level}_${stamp}.${extension}"
}

function Get-DeviceInputs {
    $items = New-Object System.Collections.Generic.List[string]

    if ($DeviceCodeFile) {
        $path = Resolve-OutputPath $DeviceCodeFile
        if (-not (Test-Path -LiteralPath $path)) {
            throw "Device code file was not found: $path"
        }
        Get-Content -LiteralPath $path | ForEach-Object {
            $line = $_.Trim()
            if ($line.Length -gt 0 -and -not $line.StartsWith("#")) {
                [void]$items.Add($line)
            }
        }
    }

    if ($FromClipboard) {
        $clip = (Get-ClipboardText).Trim()
        if ($clip.Length -eq 0) {
            throw "Clipboard is empty."
        }
        [void]$items.Add($clip)
    }

    if ($DeviceCode) {
        [void]$items.Add($DeviceCode.Trim())
    }

    if ($items.Count -eq 0) {
        $manual = Read-Host "Paste WJDC device code"
        if (-not [string]::IsNullOrWhiteSpace($manual)) {
            [void]$items.Add($manual.Trim())
        }
    }

    if ($items.Count -eq 0) {
        throw "No device code was provided."
    }

    return @($items.ToArray())
}

function New-GeneratorArgs {
    param([string]$OneDeviceCode)

    $toolArgs = @(
        "--device-code", $OneDeviceCode,
        "--level", $Level,
        "--count", $Count.ToString(),
        "--format", "json"
    )

    if ($Nonce -and $Count -eq 1) {
        $toolArgs += @("--nonce", $Nonce)
    }

    return $toolArgs
}

function New-Records {
    param([string[]]$Devices)

    $records = New-Object System.Collections.Generic.List[object]
    foreach ($device in $Devices) {
        $json = Invoke-PythonGenerator -Arguments (New-GeneratorArgs -OneDeviceCode $device) -Capture
        $decoded = $json | ConvertFrom-Json
        foreach ($record in @($decoded)) {
            [void]$records.Add($record)
        }
    }
    return @($records.ToArray())
}

function Write-RecordsToTerminal {
    param([object[]]$Records)
    if ($Quiet) { return }

    $recordCount = @($Records).Count
    if ($Details) {
        $index = 1
        foreach ($record in $Records) {
            if ($recordCount -gt 1) {
                Write-Host ""
                Write-Host "Record $index" -ForegroundColor Cyan
            }
            Write-Host "Protocol: $($record.protocol)"
            Write-Host "Level: $($record.level)"
            Write-Host "Device: $($record.device_code)"
            Write-Host "Expires: $($record.expires)"
            Write-Host "Issued at: $($record.issued_at)"
            Write-Host "Nonce: $($record.nonce)"
            Write-Host "Code SHA-256: $($record.code_sha256)"
            Write-Host "Activation code: $($record.activation_code)"
            $index += 1
        }
        return
    }

    $Records | ForEach-Object { Write-Host $_.activation_code }
}

function Save-Records {
    param(
        [object[]]$Records,
        [string]$Path
    )

    $parent = Split-Path -Parent $Path
    if ($parent) {
        New-Item -ItemType Directory -Force -Path $parent | Out-Null
    }

    switch ($Format) {
        "json" {
            $Records | ConvertTo-Json -Depth 6 | Set-Content -LiteralPath $Path -Encoding UTF8
        }
        "csv" {
            $Records | Export-Csv -LiteralPath $Path -NoTypeInformation -Encoding UTF8
        }
        default {
            ($Records | ForEach-Object { $_.activation_code }) | Set-Content -LiteralPath $Path -Encoding UTF8
        }
    }
}

function Copy-Records {
    param([object[]]$Records)
    $codes = ($Records | ForEach-Object { $_.activation_code }) -join [Environment]::NewLine
    Set-Clipboard -Value $codes
}

try {
    if ($Examples) {
        Write-Examples
        Wait-BeforeExit
        return
    }

    Write-Banner

    if ($SelfTest) {
        Invoke-PythonGenerator -Arguments @("--self-test")
        Wait-BeforeExit
        return
    }

    if ($Count -lt 1) {
        throw "Count must be greater than 0."
    }

    if ($Interactive) {
        if (-not $PSBoundParameters.ContainsKey("Save")) {
            $saveAnswer = Read-Host "Save output file? [y/N]"
            if ($saveAnswer -match "^(y|yes)$") { $Save = $true }
        }
        if (-not $PSBoundParameters.ContainsKey("Copy")) {
            $copyAnswer = Read-Host "Copy activation code to clipboard? [Y/n]"
            if ($copyAnswer -notmatch "^(n|no)$") { $Copy = $true }
        }
    }

    $devices = Get-DeviceInputs
    $records = New-Records -Devices $devices

    $outputPath = $null
    if ($Output) {
        $outputPath = Resolve-OutputPath $Output
    } elseif ($Save) {
        $outputPath = New-AutoOutputPath
    }

    if ($outputPath) {
        Save-Records -Records $records -Path $outputPath
    }

    if ($Copy) {
        Copy-Records -Records $records
    }

    Write-RecordsToTerminal -Records $records

    if (-not $Quiet) {
        Write-Host ""
        Write-Host "Generated $(@($records).Count) WJ5 activation code(s)." -ForegroundColor Green
        if ($outputPath) {
            Write-Host "Saved: $outputPath" -ForegroundColor Green
        }
        if ($Copy) {
            Write-Host "Copied activation code(s) to clipboard." -ForegroundColor Green
        }
    }

    if ($OpenOutput -and $outputPath) {
        Invoke-Item (Split-Path -Parent $outputPath)
    }
    Wait-BeforeExit
} catch {
    Write-Error $_.Exception.Message
    Wait-BeforeExit
    exit 1
}
