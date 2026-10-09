param(
    [string]$PhoneSerial = "emulator-5554",
    [string]$PhoneAvd = "Medium_Phone",
    [string]$TabletSerial = "emulator-5556",
    [string]$TabletAvd = "Pixel_Tablet",
    [string]$FromPortraitProfile = ""
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $projectRoot

$sdkRoot = if ($env:ANDROID_HOME) {
    $env:ANDROID_HOME
} elseif ($env:ANDROID_SDK_ROOT) {
    $env:ANDROID_SDK_ROOT
} else {
    Join-Path $env:LOCALAPPDATA "Android/Sdk"
}
$adb = Join-Path $sdkRoot "platform-tools/adb.exe"
$emulator = Join-Path $sdkRoot "emulator/emulator.exe"
$apk = Join-Path $projectRoot "app/build/outputs/apk/debug/app-debug.apk"

if (-not (Test-Path -LiteralPath $adb)) { throw "ADB not found: $adb" }
if (-not (Test-Path -LiteralPath $emulator)) { throw "Android emulator not found: $emulator" }
if (-not (Test-Path -LiteralPath $apk)) { throw "Debug APK not found: $apk. Build app-debug.apk first." }

function Test-DeviceConnected {
    param([Parameter(Mandatory = $true)][string]$Serial)
    $state = (& $adb -s $Serial get-state 2>$null | Out-String).Trim()
    return $state -eq "device"
}

function Wait-ForDeviceBoot {
    param([Parameter(Mandatory = $true)][string]$Serial)
    for ($attempt = 0; $attempt -lt 120; $attempt++) {
        if (Test-DeviceConnected $Serial) {
            $boot = (& $adb -s $Serial shell getprop sys.boot_completed 2>$null | Out-String).Trim()
            if ($boot -eq "1") { return }
        }
        Start-Sleep -Seconds 2
    }
    throw "Android emulator did not finish booting: $Serial"
}

function Invoke-Python {
    param([Parameter(Mandatory = $true)][string[]]$Arguments)
    & python @Arguments
    if ($LASTEXITCODE -ne 0) { throw "Python task failed ($LASTEXITCODE): python $($Arguments -join ' ')" }
}

function Start-EmulatorHidden {
    param(
        [Parameter(Mandatory = $true)][string]$Avd,
        [Parameter(Mandatory = $true)][int]$Port
    )
    $logBase = Join-Path $env:TEMP "kavvoro-capture-$Port"
    Start-Process -FilePath $emulator `
        -ArgumentList @("-avd", $Avd, "-port", "$Port", "-no-audio", "-no-snapshot-load", "-no-snapshot-save") `
        -WindowStyle Hidden -RedirectStandardOutput "$logBase.out.log" -RedirectStandardError "$logBase.err.log"
}

function Reset-EmulatorDisplay {
    param([Parameter(Mandatory = $true)][string]$Serial)
    if (-not (Test-DeviceConnected $Serial)) { return }
    & $adb -s $Serial shell wm size reset | Out-Null
    & $adb -s $Serial shell wm density reset | Out-Null
    & $adb -s $Serial shell settings put system accelerometer_rotation 1 | Out-Null
    & $adb -s $Serial shell settings put system user_rotation 0 | Out-Null
    & $adb -s $Serial shell cmd window set-ignore-orientation-request false 2>$null | Out-Null
}

$previousSerial = $env:ANDROID_SERIAL
try {
    if (-not (Test-DeviceConnected $PhoneSerial)) {
        if ($PhoneSerial -notmatch "emulator-(\d+)") {
            throw "Phone emulator $PhoneSerial is not connected. Start $PhoneAvd or pass -PhoneSerial."
        }
        Start-EmulatorHidden -Avd $PhoneAvd -Port ([int]$Matches[1])
    }
    Wait-ForDeviceBoot $PhoneSerial

    $profileOutput = & python -c "from tools.screenshot_matrix import PORTRAIT_TARGETS; print('\n'.join(profile[0] for profile in PORTRAIT_TARGETS))"
    if ($LASTEXITCODE -ne 0) { throw "Could not read the approved portrait profiles." }
    $portraitProfiles = @($profileOutput | Where-Object { $_.Trim() -ne "" })
    $startIndex = 0
    if ($FromPortraitProfile) {
        $startIndex = [Array]::IndexOf($portraitProfiles, $FromPortraitProfile)
        if ($startIndex -lt 0) { throw "Unknown portrait profile: $FromPortraitProfile" }
    }

    $env:ANDROID_SERIAL = $PhoneSerial
    & $adb -s $PhoneSerial install -r $apk
    if ($LASTEXITCODE -ne 0) { throw "Could not install the debug APK on $PhoneSerial" }
    for ($index = $startIndex; $index -lt $portraitProfiles.Count; $index++) {
        $profile = $portraitProfiles[$index].Trim()
        Write-Output "Portrait profile $($index + 1)/$($portraitProfiles.Count): $profile"
        Invoke-Python @("tools/screenshot-capture/retake_age_check_matrix.py", "--profile", $profile, "--keep-display", "--skip-install")
        Invoke-Python @("tools/screenshot-capture/retake_ui_pages_matrix.py", "--orientation", "portrait", "--profile", $profile, "--display-ready", "--keep-display")
    }

    if (-not (Test-DeviceConnected $TabletSerial)) {
        if ($TabletSerial -notmatch "emulator-(\d+)") { throw "Tablet serial must identify an emulator: $TabletSerial" }
        Start-EmulatorHidden -Avd $TabletAvd -Port ([int]$Matches[1])
    }
    Wait-ForDeviceBoot $TabletSerial

    $env:ANDROID_SERIAL = $TabletSerial
    & $adb -s $TabletSerial install -r $apk
    if ($LASTEXITCODE -ne 0) { throw "Could not install the debug APK on $TabletSerial" }
    Invoke-Python @("tools/screenshot-capture/retake_age_check_landscape.py", "--keep-display", "--skip-install")
    Invoke-Python @("tools/screenshot-capture/retake_home_landscape.py", "--display-ready", "--keep-display")
    Invoke-Python @("tools/screenshot-capture/retake_ui_pages_matrix.py", "--orientation", "landscape", "--display-ready", "--keep-display")

    Invoke-Python @("tools/normalize_screenshot_matrix.py", "--apply")
    Invoke-Python @("tools/sync_screenshot_views.py")
    Invoke-Python @("tools/verify_screenshot_matrix.py")
} finally {
    Reset-EmulatorDisplay -Serial $PhoneSerial
    Reset-EmulatorDisplay -Serial $TabletSerial
    $env:ANDROID_SERIAL = $previousSerial
}
