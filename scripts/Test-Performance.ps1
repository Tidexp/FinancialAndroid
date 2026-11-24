param(
    [ValidateSet("baseline", "optimized", "both")]
    [string]$Version = "both",

    [int[]]$DatasetSizes = @(1000, 5000, 10000),

    [ValidateRange(1, 20)]
    [int]$Repeats = 5,

    [switch]$IncludeAi,

    [switch]$ConfirmDataReset,

    [string]$DeviceSerial,

    [string]$OutputPath = (Join-Path $PSScriptRoot "automated-performance-results.csv")
)

$ErrorActionPreference = "Stop"
$optimizedRoot = Split-Path -Parent $PSScriptRoot
$projectsRoot = Split-Path -Parent $optimizedRoot
$baselineRoot = Join-Path $projectsRoot "FinancialAndroid-main"
$switchScript = Join-Path $PSScriptRoot "Run-PerformanceVersion.ps1"

if ($DatasetSizes.Count -eq 0 -or @($DatasetSizes | Where-Object { $_ -notin @(1000, 5000, 10000) }).Count -gt 0) {
    throw "DatasetSizes must contain one or more of: 1000, 5000, 10000."
}
if (!$ConfirmDataReset) {
    throw "This benchmark uninstalls each version and deletes its local app data. Use a test emulator and pass -ConfirmDataReset."
}
if (!(Test-Path $switchScript)) {
    throw "Version switch script not found: $switchScript"
}
if (!(Test-Path (Join-Path $baselineRoot "gradlew.bat"))) {
    throw "Baseline worktree not found at '$baselineRoot'."
}

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

$resolvedOutputPath = [System.IO.Path]::GetFullPath($OutputPath)
$outputDirectory = Split-Path -Parent $resolvedOutputPath
if (!(Test-Path $outputDirectory)) {
    New-Item -ItemType Directory -Path $outputDirectory -Force | Out-Null
}
$rawDirectory = Join-Path $outputDirectory "performance-raw"
if (!(Test-Path $rawDirectory)) {
    New-Item -ItemType Directory -Path $rawDirectory -Force | Out-Null
}

if (Test-Path -LiteralPath $resolvedOutputPath) {
    Remove-Item -LiteralPath $resolvedOutputPath -Force
}
Get-ChildItem -LiteralPath $rawDirectory -File |
    Where-Object { $_.Extension -eq ".log" } |
    Remove-Item -Force
Write-Host "Previous benchmark CSV and raw logs cleared."

$versions = if ($Version -eq "both") { @("baseline", "optimized") } else { @($Version) }
$oldAndroidSerial = $env:ANDROID_SERIAL
$env:ANDROID_SERIAL = $DeviceSerial
$runId = [Guid]::NewGuid().ToString("N")
$datasetArgument = ($DatasetSizes -join ",")
$includeAiArgument = $IncludeAi.IsPresent.ToString().ToLowerInvariant()

Write-Host "Automated benchmark run: $runId"
Write-Host "Device: $DeviceSerial | Datasets: $datasetArgument | Repeats: $Repeats"
Write-Host "CSV output: $resolvedOutputPath"
Write-Warning "The app is uninstalled between versions; all local app data on this device will be removed."
if ($IncludeAi) {
    Write-Warning "AI tests send synthetic benchmark data to Gemini and may incur API usage."
}

try {
    foreach ($selectedVersion in $versions) {
        Write-Host "`n=== Testing $selectedVersion ==="
        & $switchScript -Version $selectedVersion -DeviceSerial $DeviceSerial
        if ($LASTEXITCODE -ne 0) {
            throw "Could not install $selectedVersion."
        }

        $selectedRoot = if ($selectedVersion -eq "baseline") { $baselineRoot } else { $optimizedRoot }
        $gradleArguments = @(
            ":app:connectedDebugAndroidTest",
            "-Pandroid.testInstrumentationRunnerArguments.class=com.example.financial.AutomatedPerformanceTest",
            "-Pandroid.testInstrumentationRunnerArguments.runId=$runId",
            "-Pandroid.testInstrumentationRunnerArguments.version=$selectedVersion",
            "-Pandroid.testInstrumentationRunnerArguments.datasetSizes=$datasetArgument",
            "-Pandroid.testInstrumentationRunnerArguments.repeats=$Repeats",
            "-Pandroid.testInstrumentationRunnerArguments.includeAi=$includeAiArgument"
        )

        Push-Location $selectedRoot
        try {
            & .\gradlew.bat @gradleArguments
            if ($LASTEXITCODE -ne 0) {
                throw "Automated tests failed for $selectedVersion with exit code $LASTEXITCODE."
            }
        } finally {
            Pop-Location
        }

        $logOutput = & $adb -s $DeviceSerial logcat -d -s "AutoPerfResult:I" 2>&1
        $rawLogPath = Join-Path $rawDirectory "$runId-$selectedVersion.log"
        Set-Content -LiteralPath $rawLogPath -Value $logOutput -Encoding UTF8

        $newRows = @()
        foreach ($line in $logOutput) {
            $marker = $line.IndexOf("AUTO_PERF|")
            if ($marker -lt 0) {
                continue
            }
            $parts = $line.Substring($marker + "AUTO_PERF|".Length).Split("|")
            if ($parts.Count -lt 7 -or $parts[0] -ne $runId -or $parts[1] -ne $selectedVersion) {
                continue
            }
            $newRows += [pscustomobject]@{
                Timestamp   = (Get-Date).ToString("o")
                RunId       = $parts[0]
                Version     = $parts[1]
                DatasetSize = [int]$parts[2]
                Sample      = [int]$parts[3]
                Metric      = $parts[4]
                Value       = [double]::Parse($parts[5], [System.Globalization.CultureInfo]::InvariantCulture)
                ItemCount   = [int]$parts[6]
                Device      = $DeviceSerial
            }
        }

        if ($newRows.Count -eq 0) {
            throw "No benchmark results were found for $selectedVersion. Review $rawLogPath."
        }
        if (Test-Path $resolvedOutputPath) {
            $newRows | Export-Csv -LiteralPath $resolvedOutputPath -NoTypeInformation -Append
        } else {
            $newRows | Export-Csv -LiteralPath $resolvedOutputPath -NoTypeInformation
        }
        Write-Host "Collected $($newRows.Count) measurements for $selectedVersion."
    }
} finally {
    if ([string]::IsNullOrWhiteSpace($oldAndroidSerial)) {
        Remove-Item Env:\ANDROID_SERIAL -ErrorAction SilentlyContinue
    } else {
        $env:ANDROID_SERIAL = $oldAndroidSerial
    }
}

if ($Version -eq "both" -and (Test-Path $resolvedOutputPath)) {
    $currentRun = @(Import-Csv -LiteralPath $resolvedOutputPath | Where-Object { $_.RunId -eq $runId })
    $summaryRows = foreach ($group in ($currentRun | Group-Object -Property DatasetSize, Metric)) {
        $baselineValues = @($group.Group | Where-Object { $_.Version -eq "baseline" } | ForEach-Object { [double]$_.Value } | Sort-Object)
        $optimizedValues = @($group.Group | Where-Object { $_.Version -eq "optimized" } | ForEach-Object { [double]$_.Value } | Sort-Object)
        if ($baselineValues.Count -eq 0 -or $optimizedValues.Count -eq 0) {
            continue
        }

        $baselineMedian = if ($baselineValues.Count % 2 -eq 1) {
            $baselineValues[[int][Math]::Floor($baselineValues.Count / 2)]
        } else {
            ($baselineValues[$baselineValues.Count / 2 - 1] + $baselineValues[$baselineValues.Count / 2]) / 2
        }
        $optimizedMedian = if ($optimizedValues.Count % 2 -eq 1) {
            $optimizedValues[[int][Math]::Floor($optimizedValues.Count / 2)]
        } else {
            ($optimizedValues[$optimizedValues.Count / 2 - 1] + $optimizedValues[$optimizedValues.Count / 2]) / 2
        }
        $metricName = $group.Group[0].Metric
        $changePercent = $null
        if (
            $baselineMedian -ne 0 -and
            $metricName -notlike "ai_*" -and
            $metricName -notlike "*no_response*" -and
            $metricName -notmatch "(^|_)total_frames$"
        ) {
            $changePercent = [Math]::Round((($baselineMedian - $optimizedMedian) / $baselineMedian) * 100, 2)
        }

        [pscustomobject]@{
            DatasetSize      = $group.Group[0].DatasetSize
            Metric           = $metricName
            BaselineMedian   = [Math]::Round($baselineMedian, 3)
            OptimizedMedian  = [Math]::Round($optimizedMedian, 3)
            ImprovementPct   = $changePercent
        }
    }

    Write-Host "`nMedian comparison for run $runId (positive improvement means better):"
    $summaryRows | Sort-Object DatasetSize, Metric | Format-Table -AutoSize
}

Write-Host "`nBenchmark complete. Results: $resolvedOutputPath"
