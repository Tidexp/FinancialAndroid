param(
    [Parameter(Mandatory = $true, Position = 0)]
    [ValidateSet("baseline", "optimized")]
    [string]$Version,

    [string]$DeviceSerial
)

$ErrorActionPreference = "Stop"

$optimizedRoot = Split-Path -Parent $PSScriptRoot
$projectsRoot = Split-Path -Parent $optimizedRoot
$baselineRoot = Join-Path $projectsRoot "FinancialAndroid-main"
$packageName = "com.example.financial"

if (!(Test-Path (Join-Path $baselineRoot "gradlew.bat"))) {
    throw "Baseline worktree not found at '$baselineRoot'. Create it with git worktree first."
}

$sourceConfigRoot = $optimizedRoot
foreach ($relativePath in @("local.properties", "app/google-services.json")) {
    $targetPath = Join-Path $baselineRoot $relativePath
    $sourcePath = Join-Path $sourceConfigRoot $relativePath

    if (!(Test-Path $targetPath)) {
        if (!(Test-Path $sourcePath)) {
            throw "Required local config '$relativePath' is missing from both worktrees."
        }

        $targetDirectory = Split-Path -Parent $targetPath
        if (!(Test-Path $targetDirectory)) {
            New-Item -ItemType Directory -Path $targetDirectory -Force | Out-Null
        }
        Copy-Item -LiteralPath $sourcePath -Destination $targetPath
        Write-Host "Copied local-only config for baseline: $relativePath"
    }
}

$selectedRoot = if ($Version -eq "baseline") { $baselineRoot } else { $optimizedRoot }
$apkPath = Join-Path $selectedRoot "app\build\outputs\apk\debug\app-debug.apk"

if ([string]::IsNullOrWhiteSpace($env:JAVA_HOME) -or !(Test-Path (Join-Path $env:JAVA_HOME "bin\java.exe"))) {
    $jdkRoot = Join-Path $HOME ".jdks"
    $jdk = Get-ChildItem -Path $jdkRoot -Directory -ErrorAction SilentlyContinue |
        Where-Object { Test-Path (Join-Path $_.FullName "bin\java.exe") } |
        Sort-Object Name -Descending |
        Select-Object -First 1

    if ($null -eq $jdk) {
        throw "Java was not found. Set JAVA_HOME to a valid JDK."
    }

    $env:JAVA_HOME = $jdk.FullName
}
$env:PATH = "$(Join-Path $env:JAVA_HOME 'bin');$env:PATH"

$adbCommand = Get-Command "adb" -ErrorAction SilentlyContinue
if ($null -ne $adbCommand) {
    $adb = $adbCommand.Source
} else {
    $adb = Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"
}
if (!(Test-Path $adb)) {
    throw "ADB was not found. Add platform-tools to PATH or install Android SDK platform-tools."
}

$connectedDevices = @(& $adb devices | Select-String "^\S+\s+device$")
if ($connectedDevices.Count -eq 0) {
    throw "No Android device or emulator is connected."
}
if ([string]::IsNullOrWhiteSpace($DeviceSerial)) {
    if ($connectedDevices.Count -gt 1) {
        throw "Multiple devices are connected. Re-run with -DeviceSerial <serial>."
    }
    $DeviceSerial = ($connectedDevices[0].Line -split "\s+")[0]
} elseif (!($connectedDevices | Where-Object { $_.Line -match "^$([regex]::Escape($DeviceSerial))\s+device$" })) {
    throw "Device '$DeviceSerial' is not connected or is unauthorized."
}

Write-Host "Building $Version from '$selectedRoot'..."
Push-Location $selectedRoot
try {
    & .\gradlew.bat :app:assembleDebug
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle build failed with exit code $LASTEXITCODE."
    }
} finally {
    Pop-Location
}

if (!(Test-Path $apkPath)) {
    throw "Expected APK was not created: $apkPath"
}

Write-Host "Removing previously installed $packageName..."
& $adb -s $DeviceSerial uninstall $packageName | Out-Null

Write-Host "Installing $Version on device $DeviceSerial..."
& $adb -s $DeviceSerial install $apkPath
if ($LASTEXITCODE -ne 0) {
    throw "APK installation failed with exit code $LASTEXITCODE."
}

Write-Host "Launching $Version..."
& $adb -s $DeviceSerial shell monkey -p $packageName -c android.intent.category.LAUNCHER 1
if ($LASTEXITCODE -ne 0) {
    throw "App launch failed with exit code $LASTEXITCODE."
}

Write-Host "$Version is running. Sign in as Guest and prepare the test data before measuring."
