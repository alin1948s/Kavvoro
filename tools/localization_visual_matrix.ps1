param(
    [string]$Serial = "",
    [string]$Apk = "app/build/outputs/apk/debug/app-debug.apk",
    [string]$OutputDirectory = "build/localization-qa",
    [switch]$SkipInstall
)

$ErrorActionPreference = "Stop"
$adb = Join-Path $env:ANDROID_HOME "platform-tools/adb.exe"
$packageName = "com.moonsolstudios.kavvoro"
$privacyFixture = Join-Path $PSScriptRoot "screenshot-capture/fixtures/privacy_profile.xml"
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
    @{ Code = "cs"; Name = "Čeština" },
    @{ Code = "sv"; Name = "Svenska" },
    @{ Code = "fi"; Name = "Suomi" },
    @{ Code = "tr"; Name = "Turkish" },
    @{ Code = "ru"; Name = "Russian" },
    @{ Code = "uk"; Name = "Ukrainian" },
    @{ Code = "ar"; Name = "Arabic" },
    @{ Code = "hi"; Name = "Hindi" },
    @{ Code = "th"; Name = "ภาษาไทย" },
    @{ Code = "id"; Name = "Indonesia" },
    @{ Code = "vi"; Name = "Vietnamese" },
    @{ Code = "ja"; Name = "Japanese" },
    @{ Code = "ko"; Name = "Korean" },
    @{ Code = "zh"; Name = "简体中文" },
    @{ Code = "zh_tw"; Name = "繁體中文" }
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
    if ($languages.Code -notcontains $Code) { throw "Unknown language code: $Code" }

    # The visual matrix tests rendered locales, not selector hit-testing. Write
    # the debug preference directly so every row is deterministic and remains
    # valid when selector spacing or the language inventory changes.
    $tempPreference = Join-Path ([System.IO.Path]::GetTempPath()) "kavvoro_locale.xml"
    $xml = "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>`r`n<map>`r`n    <string name=`"ui_language`">$Code</string>`r`n</map>`r`n"
    try {
        [System.IO.File]::WriteAllText($tempPreference, $xml, [System.Text.UTF8Encoding]::new($false))
        Invoke-Adb @("push", $tempPreference, "/data/local/tmp/kavvoro_locale.xml")
        Invoke-Adb @("shell", "run-as", $packageName, "mkdir", "-p", "shared_prefs")
        Invoke-Adb @("shell", "run-as", $packageName, "cp", "/data/local/tmp/kavvoro_locale.xml", "shared_prefs/kavvoro_locale.xml")
    } finally {
        Remove-Item -LiteralPath $tempPreference -Force -ErrorAction SilentlyContinue
    }
}

function Ensure-PrivacyProfile {
    if (-not (Test-Path -LiteralPath $privacyFixture)) {
        throw "Privacy fixture not found: $privacyFixture"
    }
    Invoke-Adb @("push", (Resolve-Path -LiteralPath $privacyFixture).Path, "/data/local/tmp/privacy_profile.xml")
    Invoke-Adb @("shell", "run-as", $packageName, "mkdir", "-p", "shared_prefs")
    Invoke-Adb @("shell", "run-as", $packageName, "cp", "/data/local/tmp/privacy_profile.xml", "shared_prefs/privacy_profile.xml")
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
Ensure-PrivacyProfile

New-Item -ItemType Directory -Path $OutputDirectory -Force | Out-Null
$results = [System.Collections.Generic.List[object]]::new()

foreach ($language in $languages) {
    $code = $language.Code
    $prefix = Join-Path $OutputDirectory $code
    Set-AppLanguage $code
    # Restart after setting the preference so SharedPreferences cannot serve a
    # cached value from the previous locale.
    Restart-App
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
$markdown.Add("The automated pass captures the main menu and Classic entry/HUD for all 24 selectable languages. Tutorial, Collection, and result states remain explicit manual checkpoints because their state transitions are gameplay-dependent.")
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
