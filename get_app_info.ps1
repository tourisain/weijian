# Set encoding to UTF-8 to handle any potential special characters in output
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "APP Info Retrieval Tool (Fixed Version)" -ForegroundColor Cyan
Write-Host "========================"

# 1. Find Keytool
$keytool = "keytool"
if (Get-Command "keytool" -ErrorAction SilentlyContinue) {
    Write-Host "[+] keytool found in system PATH" -ForegroundColor Green
} else {
    $javaHomes = @(
        $env:JAVA_HOME,
        "C:\Program Files\Java\jdk-17",
        "C:\Program Files\Java\jdk1.8.0_*"
    )
    
    $found = $false
    foreach ($jh in $javaHomes) {
        if ($jh -and (Test-Path "$jh\bin\keytool.exe")) {
            $keytool = "$jh\bin\keytool.exe"
            Write-Host "[+] keytool found in JAVA_HOME: $keytool" -ForegroundColor Green
            $found = $true
            break
        }
    }
    
    if (-not $found) {
        Write-Host "[-] keytool not found. Please ensure JDK is installed and configured." -ForegroundColor Red
        Read-Host "Press Enter to exit..."
        exit
    }
}

# 2. Find Keystore File
Write-Host "`nSearching for keystore files (.jks / .keystore / tourisain)..."
$keystores = Get-ChildItem -Path . -Include *.jks,*.keystore,tourisain -Recurse -ErrorAction SilentlyContinue | Where-Object { -not $_.PSIsContainer }

if ($keystores.Count -eq 0) {
    # Try parent directory
    $keystores = Get-ChildItem -Path .. -Include *.jks,*.keystore,tourisain -Recurse -ErrorAction SilentlyContinue | Where-Object { -not $_.PSIsContainer }
}

$keystorePath = ""
if ($keystores.Count -eq 1) {
    $keystorePath = $keystores[0].FullName
    Write-Host "[+] Found keystore: $keystorePath" -ForegroundColor Green
} elseif ($keystores.Count -gt 1) {
    Write-Host "[!] Multiple keystores found, please select:" -ForegroundColor Yellow
    for ($i=0; $i -lt $keystores.Count; $i++) {
        Write-Host "   [$($i+1)] $($keystores[$i].FullName)"
    }
    $selection = Read-Host "Enter number (1-$($keystores.Count))"
    if ($selection -match "^\d+$" -and [int]$selection -le $keystores.Count) {
        $keystorePath = $keystores[[int]$selection - 1].FullName
    }
}

if (-not $keystorePath) {
    Write-Host "[-] No keystore file found automatically." -ForegroundColor Yellow
    $keystorePath = Read-Host "Please manually enter the full path to the keystore file (e.g. D:\path\to\key.jks)"
    $keystorePath = $keystorePath -replace '"', ''
}

if (-not (Test-Path $keystorePath)) {
    Write-Host "[-] Invalid path: $keystorePath" -ForegroundColor Red
    Read-Host "Press Enter to exit..."
    exit
}

# 3. Get Alias
Write-Host "`nReading keystore alias..."
Write-Host "Please enter keystore password below (input will be hidden):" -ForegroundColor Yellow
$listOutput = & $keytool -list -v -keystore $keystorePath
$aliasLine = $listOutput | Select-String "Alias name:" | Select-Object -First 1
$alias = ""

if ($aliasLine) {
    $alias = $aliasLine.ToString().Split(":")[1].Trim()
    Write-Host "[+] Alias detected: $alias" -ForegroundColor Green
} else {
    # Check if list output contains any entries
    if ($listOutput) {
            # Try to parse the first line which might be "Alias name: ..." or "别名: ..."
            $possibleAlias = $listOutput | Where-Object { $_ -match "Alias name:" -or $_ -match "别名:" } | Select-Object -First 1
            if ($possibleAlias) {
                $alias = $possibleAlias.ToString().Split(":")[1].Trim()
                Write-Host "[+] Alias detected: $alias" -ForegroundColor Green
            } else {
                # If list succeeded but regex failed, it might be verbose output difference
                Write-Host "[-] Could not parse alias from output. Here is the raw output:" -ForegroundColor Yellow
                $listOutput | Select-Object -First 10 | Write-Host
                Write-Host "`n[!] Hint: The alias is usually 'key0', 'tourisain', or 'androiddebugkey'." -ForegroundColor Cyan
                $alias = Read-Host "Please enter Alias name manually (NOT the file path)"
            }
    } else {
        Write-Host "[-] Failed to read keystore. Password might be incorrect." -ForegroundColor Red
        $alias = Read-Host "Please enter Alias manually (if you are sure password was correct)"
    }
} 

# Get Fingerprint and Public Key
Write-Host "`nExtracting info..." -ForegroundColor Cyan

Write-Host "--------------------------------------------------"
Write-Host "Package Name: com.tourisain.weijian"
Write-Host "--------------------------------------------------"

# Export Public Key
$certFile = "temp_cert.cer"
Write-Host "`nExporting public key (may ask for password again)..."
& $keytool -exportcert -keystore $keystorePath -alias $alias -file $certFile | Out-Null

if (Test-Path $certFile) {
    # --- New Section: Calculate Fingerprints from File ---
    Write-Host "`nCalculating Fingerprints..." -ForegroundColor Cyan
    
    function Format-Hash {
        param([string]$Hash)
        $Hash = $Hash.ToUpper()
        $formatted = ""
        for ($i = 0; $i -lt $Hash.Length; $i += 2) {
            $formatted += $Hash.Substring($i, 2)
            if ($i -lt $Hash.Length - 2) { $formatted += ":" }
        }
        return $formatted
    }

    # Calculate MD5
    try {
        $md5 = Get-FileHash $certFile -Algorithm MD5
        $md5Hex = $md5.Hash
        $md5Formatted = Format-Hash $md5Hex
        Write-Host "`nMD5 Fingerprint:" -ForegroundColor Green
        Write-Host "Standard (with colons): $md5Formatted"
        Write-Host "Continuous (no colons): $md5Hex"
    } catch {
        Write-Host "[-] Failed to calculate MD5: $_" -ForegroundColor Yellow
    }

    # Calculate SHA1
    try {
        $sha1 = Get-FileHash $certFile -Algorithm SHA1
        $sha1Hex = $sha1.Hash
        $sha1Formatted = Format-Hash $sha1Hex
        Write-Host "`nSHA1 Fingerprint:" -ForegroundColor Green
        Write-Host "Standard (with colons): $sha1Formatted"
        Write-Host "Continuous (no colons): $sha1Hex"
    } catch {
        Write-Host "[-] Failed to calculate SHA1: $_" -ForegroundColor Yellow
    }

    # Calculate SHA256
    try {
        $sha256 = Get-FileHash $certFile -Algorithm SHA256
        $sha256Hex = $sha256.Hash
        $sha256Formatted = Format-Hash $sha256Hex
        Write-Host "`nSHA256 Fingerprint:" -ForegroundColor Green
        Write-Host "Standard (with colons): $sha256Formatted"
        Write-Host "Continuous (no colons): $sha256Hex"
    } catch {
        Write-Host "[-] Failed to calculate SHA256: $_" -ForegroundColor Yellow
    }
    # -----------------------------------------------------

    Write-Host "`nAttempting to extract public key..." -ForegroundColor Cyan
    
    $success = $false
    
    # Method 1: PowerShell .NET
    try {
        $absCertPath = Convert-Path $certFile
        $cert = New-Object System.Security.Cryptography.X509Certificates.X509Certificate2
        $cert.Import($absCertPath)
        $publicKey = $cert.GetPublicKey()
        $publicKeyString = [System.BitConverter]::ToString($publicKey).Replace("-", "")
        
        Write-Host "`nPublic Key (Hex):" -ForegroundColor Green
        Write-Host $publicKeyString
        $success = $true
    } catch {
        Write-Host "[-] .NET extraction failed: $_" -ForegroundColor Yellow
    }

    # Method 2: CertUtil (Fallback)
    if (-not $success) {
        Write-Host "[-] Trying fallback method (CertUtil)..." -ForegroundColor Yellow
        try {
            $certUtilOutput = certutil -dump $certFile
            $readingKey = $false
            $keyHex = ""
            foreach ($line in $certUtilOutput) {
                if ($line -match "Public Key:") {
                    $readingKey = $true
                    continue
                }
                if ($readingKey) {
                    if ($line -notmatch "^\s*[0-9a-f]{2}\s") {
                        if ($keyHex.Length > 0) { break }
                    } else {
                        $keyHex += $line.Trim() -replace "\s", ""
                    }
                }
            }
            
            if ($keyHex) {
                Write-Host "`nPublic Key (Hex) from CertUtil:" -ForegroundColor Green
                Write-Host $keyHex
            } else {
                throw "Could not parse CertUtil output"
            }
        } catch {
            Write-Host "[-] Automatic extraction failed completely." -ForegroundColor Red
            Write-Host "[!] Please run the following command manually to view certificate details:" -ForegroundColor Yellow
            Write-Host "keytool -printcert -file $certFile" -ForegroundColor White
        }
    }
    
    Remove-Item $certFile
} else {
    Write-Host "[-] Failed to export certificate. Check password or alias." -ForegroundColor Red
}

Write-Host "`n========================"
Write-Host "Done"
Read-Host "Press Enter to exit..."
