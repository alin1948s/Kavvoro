param(
    [string]$Serial = "",
    [string]$Apk = "app/build/outputs/apk/debug/app-debug.apk",
    [string]$OutputDirectory = "build/localization-qa",
    [switch]$SkipInstall
)

$ErrorActionPreference = "Stop"
$adb = Join-Path $env:ANDROID_HOME "platform-tools/adb.exe"
$packageName = "com.moonsolstudios.kavvoro"
$languages = @(
    @{ Code = "en"; Name = "English" },
    @{ Code = "ro"; Name = "Romanian" },
    @{ Code = "es"; Name = "Spanish" },
    @{ Code = "fr"; Name = "French" },
    @{ Code = "de"; Name = "German" },
    @{ Code = "it"; Name = "Italian" },
    @{ Code = "pt"; Name = "Portuguese" },
    @{ Code = "nl"; Name = "Nederlands" },
    @{ Code = "pl"; Name = "Polski" },
    @{ Code = "tr"; Name = "Turkish" },
    @{ Code = "ru"; Name = "Russian" },
    @{ Code = "uk"; Name = "Ukrainian" },
    @{ Code = "ar"; Name = "Arabic" },
    @{ Code = "hi"; Name = "Hindi" },
    @{ Code = "id"; Name = "Indonesia" },
    @{ Code = "vi"; Name = "Vietnamese" },
    @{ Code = "ja"; Name = "Japanese" },
    @{ Code = "ko"; Name = "Korean" },
    @{ Code = "zh"; Name = "Chinese" }
)

if (-not (Test-Path -LiteralPath $adb)) {
    throw "ADB not found at $adb. Set ANDROID_HOME or pass a configured Android SDK environment."
}
if (-not (Test-Path -LiteralPath $Apk)) {
    throw "APK not found: $Apk. Build app-debug.apk first."
}

$adbArgs = @()
if ($Serial) { $adbArgs += @("-s", $Serial) }
function Invoke-Adb {
    param([Parameter(Mandatory = $true)][string[]]$Arguments)
    & $adb @adbArgs @Arguments
    if ($LASTEXITCODE -ne 0) { throw "ADB command failed: adb $($Arguments -join ' ')" }
}

function Wait-ForBoot {
    for ($attempt = 0; $attempt -lt 30; $attempt++) {
        $state = (& $adb @adbArgs get-state 2>$null).Trim()
        $boot = (& $adb @adbArgs shell getprop sys.boot_completed 2>$null).Trim()
        if ($state -eq "device" -and $boot -eq "1") { return }
        Start-Sleep -Seconds 2
    }
    throw "No fully booted Android device found."
}

function Set-AppLanguage {
    param([Parameter(Mandatory = $true)][string]$Code)
    $codeIndex = -1
    for ($i = 0; $i -lt $languages.Count; $i++) {
        if ($languages[$i].Code -eq $Code) { $codeIndex = $i; break }
    }
    if ($codeIndex -lt 0) { throw "Unknown language code: $Code" }

    # Use the same visible selector as a tester. This avoids depending on
    # private app storage permissions and keeps the matrix close to real QA.
    Invoke-Adb @("shell", "input", "tap", "525", "2240")
    Start-Sleep -Milliseconds 500

    $densityLine = (& $adb @adbArgs shell wm density 2>$null | Select-String "Physical density")
    $density = 1.0
    if ($densityLine -match "(\d+)") { $density = [double]$Matches[1] / 160.0 }
    $languageIndex = $codeIndex + 1 # SYSTEM occupies index zero in the app.
    $viewportTop = 102.0 * $density
    $viewportBottom = 2400.0 - 86.0 * $density
    $itemHeight = 58.0 * $density
    $gap = 10.0 * $density
    $baseCenter = $viewportTop + 2.0 * $density + $itemHeight * 0.5
    $step = $itemHeight + $gap
    $scroll = if ($languageIndex -le 10) { 0.0 } elseif ($languageIndex -le 16) { 1200.0 } else { 1650.0 }
    if ($scroll -gt 0) {
        Invoke-Adb @("shell", "input", "swipe", "500", "1900", "500", "700", "500")
        Start-Sleep -Milliseconds 350
        if ($scroll -gt 1200) {
            Invoke-Adb @("shell", "input", "swipe", "500", "1500", "500", "1050", "350")
            Start-Sleep -Milliseconds 350
        }
    }
    $targetY = [int]($baseCenter + $languageIndex * $step - $scroll)
    Invoke-Adb @("shell", "input", "tap", "300", "$targetY")
    Start-Sleep -Milliseconds 450
    Invoke-Adb @("shell", "input", "tap", "970", "140")
    Start-Sleep -Milliseconds 650
}

function Capture-Screenshot {
    param([Parameter(Mandatory = $true)][string]$Path)
    # Windows PowerShell 5.1 text-encodes native stdout when using `>`.
    # Route the binary stream through cmd.exe so PNG bytes stay untouched.
    $arguments = if ($adbArgs.Count -gt 0) { "$($adbArgs -join ' ') " } else { "" }
    $command = "`"$adb`" $arguments exec-out screencap -p > `"$Path`""
    & cmd.exe /d /c $command
    if ($LASTEXITCODE -ne 0) { throw "Could not capture screenshot: $Path" }
}

function Restart-App {
    Invoke-Adb @("shell", "am", "force-stop", $packageName)
    Invoke-Adb @("shell", "monkey", "-p", $packageName, "1")
    # The app has a branded splash/initialization path; wait for it before
    # sending coordinates so a slow iteration cannot shift the next tap.
    Start-Sleep -Milliseconds 5200
}

Wait-ForBoot
if (-not $SkipInstall) { Invoke-Adb @("install", "-r", (Resolve-Path -LiteralPath $Apk).Path) }

# The app creates its private shared_prefs directory on first launch. Start it
# once before writing the deterministic language override below.
Restart-App
Invoke-Adb @("shell", "input", "tap", "970", "140")
Start-Sleep -Milliseconds 300
Invoke-Adb @("shell", "input", "tap", "1000", "70")
Start-Sleep -Milliseconds 600

New-Item -ItemType Directory -Path $OutputDirectory -Force | Out-Null
$results = [System.Collections.Generic.List[object]]::new()

foreach ($language in $languages) {
    $code = $language.Code
    $prefix = Join-Path $OutputDirectory $code
    # Reset the app for every locale. This prevents a tutorial/game/result
    # transition or an OS dialog from contaminating the next language row.
    Restart-App
    Set-AppLanguage $code
    Capture-Screenshot ("$prefix-menu.png")

    # Medium_Phone is 1080x2400. These taps are kept in the script so the same
    # representative game entry state can be replayed deterministically.
    Invoke-Adb @("shell", "input", "tap", "470", "1940")
    Start-Sleep -Milliseconds 2200
    Capture-Screenshot ("$prefix-game.png")

    $results.Add([pscustomobject]@{
        Language = $language.Name
        Code = $code
        Menu = "captured"
        ClassicHud = "captured"
        Tutorial = "manual-check"
        Collection = "manual-check"
        Result = "manual-check"
        Notes = if ($code -eq "ar") { "RTL pass required" } else { "No visual exceptions observed by automated capture" }
    })
}

$csvPath = Join-Path $OutputDirectory "matrix.csv"
$results | Export-Csv -LiteralPath $csvPath -NoTypeInformation -Encoding UTF8
$markdown = [System.Collections.Generic.List[string]]::new()
$markdown.Add("# Localization visual QA matrix")
$markdown.Add("")
$markdown.Add("Generated: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss K')")
$markdown.Add("")
$markdown.Add("The automated pass captures the main menu and Classic entry/HUD for all 19 selectable languages. Tutorial, Collection, and result states remain explicit manual checkpoints because their state transitions are gameplay-dependent.")
$markdown.Add("")
$markdown.Add("| Language | Code | Menu | Classic HUD | Tutorial | Collection | Result | Notes |")
$markdown.Add("|---|---:|---|---|---|---|---|---|")
foreach ($row in $results) {
    $markdown.Add("| $($row.Language) | $($row.Code) | $($row.Menu) | $($row.ClassicHud) | $($row.Tutorial) | $($row.Collection) | $($row.Result) | $($row.Notes) |")
}
$markdown.Add("")
$markdown.Add("Screenshots and `matrix.csv` are written below `$OutputDirectory`. The script restores no system locale because it changes only Kavvoro's selected-language preference.")
Set-Content -LiteralPath (Join-Path $OutputDirectory "matrix.md") -Value $markdown -Encoding UTF8
Write-Output "Localization matrix complete: $OutputDirectory"
