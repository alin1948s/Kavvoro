param()

$root = Split-Path -Parent $PSScriptRoot
$out = Join-Path $root 'figma-assets\editable-svg'
New-Item -ItemType Directory -Force -Path $out | Out-Null

function DataUri([string]$path) {
    $ext = [IO.Path]::GetExtension($path).ToLowerInvariant()
    $mime = if ($ext -eq '.jpg' -or $ext -eq '.jpeg') { 'image/jpeg' } else { 'image/png' }
    return "data:$mime;base64,$([Convert]::ToBase64String([IO.File]::ReadAllBytes($path)))"
}

$r = Join-Path $root 'app\src\main\res\drawable-nodpi'
$a = @{
    menuBg = DataUri (Join-Path $r 'world_bg_menu.jpg')
    classicBg = DataUri (Join-Path $r 'world_bg_classic.jpg')
    chaosBg = DataUri (Join-Path $r 'world_bg_chaos.jpg')
    endgameBg = DataUri (Join-Path $r 'world_bg_endgame.jpg')
    tutorialClassicBg = DataUri (Join-Path $r 'world_bg_tutorial_classic.jpg')
    tutorialChaosBg = DataUri (Join-Path $r 'world_bg_tutorial_chaos.jpg')
    launchLogo = DataUri (Join-Path $r 'moonsol_studios_splash_logo.png')
    ball = DataUri (Join-Path $r 'brainball_nodlo.png')
    portal = DataUri (Join-Path $r 'world_portal_goal.png')
    classicPlatform = DataUri (Join-Path $r 'world_platform_classic.png')
    chaosPlatform = DataUri (Join-Path $r 'world_platform_chaos.png')
    riftPull = DataUri (Join-Path $r 'boost_rift_pull.png')
    goal = DataUri (Join-Path $r 'boost_goal.png')
    chain = DataUri (Join-Path $r 'boost_chain.png')
    prism = DataUri (Join-Path $r 'brainball_prism_king.png')
    void = DataUri (Join-Path $r 'brainball_void_zero.png')
    chrome = DataUri (Join-Path $r 'brainball_chrome_lux.png')
    plasma = DataUri (Join-Path $r 'brainball_plasma_crown.png')
}

function E([string]$value) { return [System.Net.WebUtility]::HtmlEncode($value) }
function Img([string]$id, [string]$href, [int]$x, [int]$y, [int]$w, [int]$h, [string]$extra = '') {
    return "<image id='$id' x='$x' y='$y' width='$w' height='$h' preserveAspectRatio='xMidYMid slice' href='$href' $extra />"
}
function Rect([string]$id, [int]$x, [int]$y, [int]$w, [int]$h, [string]$fill, [string]$stroke = 'none', [int]$radius = 0, [string]$extra = '') {
    return "<rect id='$id' x='$x' y='$y' width='$w' height='$h' rx='$radius' fill='$fill' stroke='$stroke' stroke-width='3' $extra />"
}
function Txt([string]$id, [int]$x, [int]$y, [string]$value, [int]$size, [string]$color = '#F7F4FF', [int]$weight = 700, [string]$anchor = 'start', [string]$extra = '') {
    return "<text id='$id' x='$x' y='$y' fill='$color' font-family='Arial, sans-serif' font-size='$size' font-weight='$weight' text-anchor='$anchor' $extra>$(E $value)</text>"
}
function Line([string]$id, [int]$x1, [int]$y1, [int]$x2, [int]$y2, [string]$color, [int]$width = 4, [string]$extra = '') {
    return "<line id='$id' x1='$x1' y1='$y1' x2='$x2' y2='$y2' stroke='$color' stroke-width='$width' stroke-linecap='round' $extra />"
}
function Circle([string]$id, [int]$cx, [int]$cy, [int]$radius, [string]$fill, [string]$stroke = 'none', [int]$width = 0) {
    return "<circle id='$id' cx='$cx' cy='$cy' r='$radius' fill='$fill' stroke='$stroke' stroke-width='$width' />"
}
function Panel([string]$id, [int]$x, [int]$y, [int]$w, [int]$h, [string]$accent, [string]$fill = '#101722') {
    return (Rect "$id-bg" $x $y $w $h $fill $accent 22) + (Rect "$id-bar" $x $y 8 $h $accent 'none' 0)
}
function Svg([string]$name, [string]$body) {
    $content = @("<svg xmlns='http://www.w3.org/2000/svg' width='1080' height='2400' viewBox='0 0 1080 2400'>", $body, '</svg>') -join "`n"
    Set-Content -LiteralPath (Join-Path $out $name) -Value $content -Encoding utf8
}

$dark = '#07090F'
$ink = '#101722'
$white = '#F7F4FF'
$muted = '#AAB0BE'
$teal = '#1DE8C8'
$pink = '#FF4D8D'
$gold = '#FFCF4A'
$blue = '#8AA6FF'

# Launch
$b = Rect 'background' 0 0 1080 2400 $dark
$b += Img 'studio-logo' $a.launchLogo 275 720 530 530
$b += Txt 'brand-moonsol' 540 1395 'MOONSOL' 30 $white 600 'middle' 'letter-spacing="7"'
$b += Txt 'brand-studios' 540 1440 'STUDIOS' 16 $gold 700 'middle' 'letter-spacing="5"'
$b += Rect 'progress-teal' 420 1492 120 6 $teal 'none' 3
$b += Rect 'progress-pink' 560 1492 120 6 $pink 'none' 3
$b += Rect 'loader-panel' 180 1900 720 90 '#0B101C' $teal 18
$b += Txt 'loader-label' 215 1938 'INITIALIZING KAVVORO' 16 $white 700
$b += Rect 'loader-track' 215 1960 650 8 '#303745' 'none' 4
$b += Rect 'loader-value' 215 1960 260 8 $teal 'none' 4
Svg '01-launch-editable.svg' $b

# Age gate
$b = Rect 'background' 0 0 1080 2400 $dark
$b += Txt 'brand-1' 64 150 'BRAINROT' 56 $white 800
$b += Txt 'brand-2' 64 225 'CHAOS' 56 $pink 800
$b += Txt 'brand-3' 64 300 'KAVVORO' 56 $pink 800
$b += Txt 'setup' 540 440 'PLAYER SETUP' 24 $blue 800 'middle'
$b += Txt 'title' 540 540 'AGE CHECK' 48 $white 800 'middle'
$b += Txt 'subtitle' 540 615 'Enter your age in years.' 22 $muted 600 'middle'
$b += Rect 'age-input' 64 690 952 150 '#161D29' '#45506B' 10
$b += Txt 'age-placeholder' 540 785 'AGE' 42 '#8790A0' 700 'middle'
$b += Rect 'continue' 64 920 952 132 $teal 'none' 10
$b += Txt 'continue-text' 540 1003 'CONTINUE' 28 $dark 800 'middle'
$b += Txt 'local-note' 540 1135 'Only the age group is saved locally.' 18 $muted 600 'middle'
$b += Txt 'groups' 540 1190 'CHILD  /  TEEN  /  ADULT' 18 $gold 800 'middle'
Svg '02-age-gate-editable.svg' $b

function Menu([string]$mode, [string]$bg, [string]$accent, [string]$modeLabel) {
    $classicChipAccent = if ($mode -eq 'CLASSIC') { $teal } else { '#45506B' }
    $chaosChipAccent = if ($mode -eq 'CHAOS') { $pink } else { '#45506B' }
    $classicTextAccent = if ($mode -eq 'CLASSIC') { $teal } else { $muted }
    $chaosTextAccent = if ($mode -eq 'CHAOS') { $pink } else { $muted }
    $b = Img 'world-background' $bg 0 0 1080 2400
    $b += Rect 'overlay' 0 0 1080 2400 '#05070D' 'none' 0 'fill-opacity="0.42"'
    $b += Txt 'eyebrow' 64 84 'BRAINROT CHAOS' 24 $pink 800
    $b += Txt 'title' 64 150 'KAVVORO' 72 $white 900
    $b += Rect 'title-teal' 64 182 200 8 $teal 'none' 4
    $b += Rect 'title-gold' 280 182 100 8 $gold 'none' 4
    $b += Txt 'best-label' 720 78 'BEST STREAK' 18 $muted 700 'middle'
    $b += Txt 'best-value' 720 130 'x1' 34 $teal 900 'middle'
    $b += Txt 'hype-label' 980 78 'HYPE' 18 $muted 700 'middle'
    $b += Txt 'hype-value' 980 130 '2.7K' 34 $gold 900 'middle'
    $b += Panel 'equipped' 64 250 480 140 $teal
    $b += Img 'equipped-ball' $a.ball 86 272 92 92
    $b += Txt 'equipped-label' 205 290 'EQUIPPED' 18 $teal 800
    $b += Txt 'equipped-name' 205 340 'KAVVORO' 30 $white 900
    $b += Txt 'equipped-meta' 205 372 'SELECTED BRAINBALL' 16 $muted 700
    $b += Panel 'unlock' 568 250 448 140 '#45F2FF'
    $b += Img 'unlock-icon' $a.chain 600 278 70 70
    $b += Txt 'unlock-label' 685 290 'NEXT UNLOCK' 18 '#45F2FF' 800
    $b += Txt 'unlock-name' 685 340 'VAULT MAXED' 30 $white 900
    $b += Txt 'unlock-meta' 685 372 'ALL FREE REWARDS UNLOCKED' 16 $muted 700
    $b += Panel 'daily' 64 410 480 140 '#45F2FF'
    $b += Img 'daily-icon' $a.riftPull 86 432 82 82
    $b += Txt 'daily-label' 185 452 'DAILY RIFT' 18 '#45F2FF' 800
    $b += Txt 'daily-value' 185 505 '+420 HYPE' 32 $white 900
    $b += Txt 'daily-meta' 185 535 'RESET 8H 44M' 15 $muted 700
    $b += Panel 'streak' 568 410 448 140 $teal
    $b += Img 'streak-icon' $a.chain 600 438 72 72
    $b += Txt 'streak-label' 685 452 'STREAK VAULT' 18 $teal 800
    $b += Txt 'streak-value' 685 505 'x0' 32 $white 900
    $b += Txt 'streak-meta' 685 535 'VAULT MAXED' 15 $muted 700
    $b += Circle 'preview-ring' 540 1050 145 '#0E927E' 'none' 0
    $b += Img 'preview-ball' $a.ball 405 915 270 270
    $b += Rect 'preview-caption' 372 1195 336 64 '#0B101C' $accent 28
    $b += Txt 'preview-caption-text' 540 1237 'RIFT ONLINE  /  KAVVORO' 18 $white 800 'middle'
    $b += Panel 'classic-chip' 64 1680 472 140 $classicChipAccent
    $b += Img 'classic-icon' $a.portal 90 1718 72 72
    $b += Txt 'classic-label' 185 1738 'CLASSIC' 28 $white 900
    $b += Txt 'classic-meta' 185 1782 'START LEVEL 01' 17 $classicTextAccent 800
    $b += Panel 'chaos-chip' 544 1680 472 140 $chaosChipAccent
    $b += Img 'chaos-icon' $a.chaosPlatform 570 1718 72 72
    $b += Txt 'chaos-label' 665 1738 'CHAOS' 28 $white 900
    $b += Txt 'chaos-meta' 665 1782 'SELECTED / L02 / x0' 17 $chaosTextAccent 800
    $b += Rect 'play' 64 1845 952 150 $accent 'none' 14
    $b += Img 'play-icon' $a.goal 95 1880 88 88
    $b += Txt 'play-label' 250 1910 "PLAY $modeLabel" 36 $dark 900
    $b += Txt 'play-meta' 250 1952 'START LEVEL 01  /  FIRST RUN' 18 $dark 800
    $b += Panel 'leaderboards' 64 2020 472 140 $blue
    $b += Img 'leaderboard-icon' $a.chain 90 2052 72 72
    $b += Txt 'leaderboard-label' 185 2072 'LEADERBOARDS' 24 $white 900
    $b += Txt 'leaderboard-meta' 185 2110 'GLOBAL RANKS' 16 $muted 800
    $b += Panel 'collection' 544 2020 472 140 $teal
    $b += Img 'collection-icon' $a.ball 575 2052 72 72
    $b += Txt 'collection-label' 670 2072 'COLLECTION' 24 $white 900
    $b += Txt 'collection-meta' 670 2110 '50/50 UNLOCKED COUNT' 16 $muted 800
    $b += Rect 'privacy' 64 2200 250 110 '#101722' '#45506B' 12
    $b += Txt 'privacy-label' 195 2265 'PRIVACY' 20 $white 800 'middle'
    $b += Rect 'language' 330 2200 440 110 '#101722' '#45506B' 12
    $b += Txt 'language-label' 550 2242 'LANGUAGE' 16 $muted 800 'middle'
    $b += Txt 'language-value' 550 2278 'AUTO   /   EN' 20 $white 800 'middle'
    $b += Rect 'sfx' 786 2200 112 110 '#101722' '#45506B' 12
    $b += Img 'sfx-icon' $a.goal 800 2218 82 82
    $b += Rect 'music' 908 2200 108 110 '#101722' '#45506B' 12
    $b += Img 'music-icon' $a.chain 920 2218 82 82
    return $b
}
Svg '03-menu-classic-editable.svg' (Menu 'CLASSIC' $a.menuBg $teal 'CLASSIC')
Svg '04-menu-chaos-editable.svg' (Menu 'CHAOS' $a.chaosBg $pink 'CHAOS')

function Game([string]$mode, [string]$bg, [string]$accent, [string]$title, [string]$platform) {
    $hypeValue = if ($mode -eq 'CHAOS') { '1.3K' } else { '1.1K' }
    $b = Img 'world-background' $bg 0 0 1080 2400
    $b += Rect 'hud-scrim' 0 0 1080 320 '#07090F' 'none' 0 'fill-opacity="0.72"'
    $b += Panel 'hud' 32 28 650 82 '#101722' '#45506B'
    $b += Txt 'time-label' 70 58 'TIME' 16 $muted 800
    $b += Txt 'time-value' 70 94 '11.5s' 25 $white 900
    $b += Txt 'chain-label' 270 58 'CHAIN' 16 $muted 800
    $b += Txt 'chain-value' 270 94 '-' 25 $white 900
    $b += Txt 'hype-label' 460 58 'HYPE' 16 $muted 800
    $b += Txt 'hype-value' 460 94 $hypeValue 25 $gold 900
    $b += Rect 'energy-track' 240 164 600 34 '#4B5362' 'none' 16
    $b += Rect 'energy-fill' 240 164 580 34 $accent 'none' 16
    $b += Txt 'energy-label' 540 150 'RIFT ENERGY                                      100%' 16 $white 800 'middle'
    $b += Rect 'level-ribbon' 64 230 952 58 '#101722' $accent 12
    $b += Txt 'level-title' 540 267 "$mode  -  L01 $title" 22 $white 900 'middle'
    $b += Img 'ball' $a.ball 110 360 150 150
    $b += Line 'rift-line' 185 435 765 2010 $accent 8 'stroke-opacity="0.75"'
    $b += Circle 'tap-target' 350 540 54 '#0B101C' $accent 6
    $b += Circle 'tap-dot' 350 540 14 $accent
    $b += Txt 'tap-label' 350 640 'TAP' 20 $white 900 'middle'
    $b += Img 'platform' $platform 340 1450 420 150
    $b += Txt 'platform-label' 540 1410 'BOUNCE WALL' 20 $white 800 'middle'
    $b += Img 'portal' $a.portal 730 1980 230 230
    $b += Rect 'exit' 805 1900 140 64 '#101722' '#64E572' 16
    $b += Txt 'exit-label' 875 1942 'EXIT' 18 $white 800 'middle'
    return $b
}
Svg '05-tutorial-chaos-editable.svg' ((Game 'CHAOS' $a.tutorialChaosBg $pink 'CHAOS TOUCH' $a.chaosPlatform) + (Panel 'tutorial-card' 48 1835 984 480 $pink '#1A0E1B') + (Txt 'tutorial-kicker' 210 1910 'TRAINING 1/10  /  TAP' 22 $pink 900) + (Txt 'tutorial-copy-1' 210 1970 'Tap to fire a short Rift tether.' 22 $white 700) + (Txt 'tutorial-copy-2' 210 2010 'The ball accelerates toward the tap point.' 22 $white 700) + (Txt 'tutorial-copy-3' 210 2050 'Chain clean taps to steer without wasting energy.' 22 $white 700) + (Txt 'tutorial-copy-4' 210 2090 'Obstacle: platforms rebound and redirect the ball.' 22 $white 700) + (Txt 'tutorial-note' 210 2140 'NO ADS IN TRAINING  /  L10 UNLOCKS VORO GRAD' 18 $gold 800) + (Rect 'got-it' 84 2200 912 82 '#1A0E1B' $white 14) + (Txt 'got-it-label' 540 2252 'GOT IT' 24 $white 900 'middle'))
Svg '06-tutorial-classic-editable.svg' ((Game 'CLASSIC' $a.tutorialClassicBg $teal 'RIFT TOUCH' $a.classicPlatform) + (Panel 'tutorial-card' 48 1835 984 480 $teal '#071B1B') + (Txt 'tutorial-kicker' 210 1910 'TRAINING 1/10  /  TAP' 22 $teal 900) + (Txt 'tutorial-copy-1' 210 1970 'Tap to fire a short Rift tether.' 22 $white 700) + (Txt 'tutorial-copy-2' 210 2010 'The ball accelerates toward the tap point.' 22 $white 700) + (Txt 'tutorial-copy-3' 210 2050 'Chain clean taps to steer without wasting energy.' 22 $white 700) + (Txt 'tutorial-copy-4' 210 2090 'Obstacle: platforms rebound and redirect the ball.' 22 $white 700) + (Txt 'tutorial-note' 210 2140 'NO ADS IN TRAINING  /  L10 UNLOCKS VORO GRAD' 18 $gold 800) + (Rect 'got-it' 84 2200 912 82 '#071B1B' $white 14) + (Txt 'got-it-label' 540 2252 'GOT IT' 24 $white 900 'middle'))
Svg '07-classic-mode-editable.svg' (Game 'CLASSIC' $a.classicBg $teal 'RIFT TOUCH' $a.classicPlatform)
Svg '08-chaos-mode-editable.svg' (Game 'CHAOS' $a.chaosBg $pink 'CHAOS TOUCH' $a.chaosPlatform)

# Result
$b = Img 'world-background' $a.chaosBg 0 0 1080 2400
$b += Rect 'overlay' 0 0 1080 2400 '#05070D' 'none' 0 'fill-opacity="0.40"'
$b += Txt 'result-kicker' 90 1370 'RUN COMPLETE  /  CHAOS' 22 $pink 900
$b += Txt 'result-title' 90 1450 'CHAOS TOUCH' 50 $white 900
$b += Panel 'result-stats' 90 1530 900 180 $pink '#160F1B'
$b += Txt 'rank-label' 160 1605 'RANK' 18 $muted 800
$b += Txt 'rank-value' 160 1680 'S' 76 $pink 900
$b += Txt 'time-label' 380 1605 'TIME' 18 $muted 800
$b += Txt 'time-value' 380 1680 '2.3s' 36 $white 900
$b += Txt 'hype-label' 620 1605 'HYPE' 18 $muted 800
$b += Txt 'hype-value' 620 1680 '2702' 36 $white 900
$b += Txt 'chain-label' 820 1605 'CHAIN' 18 $muted 800
$b += Txt 'chain-value' 820 1680 'x0' 36 $white 900
$b += Panel 'reward' 90 1760 900 160 $pink '#101722'
$b += Txt 'reward-label' 300 1820 'REWARD SIGNAL' 18 $muted 800
$b += Txt 'reward-value' 300 1870 'DAILY RIFT BONUS +420  /  ALL FREE REWARDS UNLOCKED' 20 $gold 900
$b += Rect 'share' 90 2020 420 100 '#101722' $gold 14
$b += Txt 'share-label' 300 2082 'SHARE SHORT' 24 $gold 900 'middle'
$b += Rect 'next' 550 2020 440 100 $pink 'none' 14
$b += Txt 'next-label' 770 2082 'NEXT LEVEL' 24 $dark 900 'middle'
Svg '09-result-chaos-editable.svg' $b

# Collection
$b = Img 'world-background' $a.menuBg 0 0 1080 2400
$b += Rect 'overlay' 0 0 1080 2400 '#07090F' 'none' 0 'fill-opacity="0.78"'
$b += Txt 'breadcrumb' 54 82 'KAVVORO  /  VAULT' 20 $pink 900
$b += Txt 'title' 54 165 'COLLECTION' 58 $white 900
$b += Rect 'back' 920 70 100 100 '#101722' $blue 18
$b += Txt 'back-label' 970 136 '‹' 64 $blue 900 'middle'
$b += Rect 'owned' 54 200 240 80 '#101722' $teal 12
$b += Txt 'owned-label' 80 235 'OWNED' 14 $muted 800
$b += Txt 'owned-value' 80 270 '50/50' 24 $teal 900
$b += Rect 'streak' 320 200 240 80 '#101722' '#C15CFF' 12
$b += Txt 'streak-label' 346 235 'STREAK' 14 $muted 800
$b += Txt 'streak-value' 346 270 'x1' 24 '#C15CFF' 900
$b += Panel 'equipped' 48 330 984 200 $teal
$b += Img 'equipped-ball' $a.ball 86 356 140 140
$b += Txt 'equipped-kicker' 270 388 'EQUIPPED BRAINBALL' 18 $teal 900
$b += Txt 'equipped-name' 270 450 'KAVVORO' 36 $white 900
$b += Txt 'equipped-meta' 270 488 'THE ORIGINAL BRAINROT SPECIMEN / ACTIVE' 18 $muted 700
$b += Rect 'filter-all' 48 570 190 72 '#F7F4FF' 'none' 16
$b += Txt 'filter-all-label' 143 615 'ALL' 18 $dark 900 'middle'
$b += Rect 'filter-power' 250 570 240 72 '#101722' $gold 16
$b += Txt 'filter-power-label' 370 615 'SUPERPOWER' 16 $white 800 'middle'
$b += Rect 'filter-hype' 502 570 190 72 '#101722' $teal 16
$b += Txt 'filter-hype-label' 597 615 'HYPE' 16 $white 800 'middle'
$b += Rect 'filter-premium' 704 570 190 72 '#101722' $pink 16
$b += Txt 'filter-premium-label' 799 615 'PREMIUM' 16 $white 800 'middle'
foreach ($item in @(@{name='PRISM KING'; art=$a.prism; y=690; accent=$gold}, @{name='VOID ZERO'; art=$a.void; y=910; accent='#9A68FF'}, @{name='CHROME LUX'; art=$a.chrome; y=1130; accent=$pink}, @{name='PLASMA PAPARINO'; art=$a.plasma; y=1350; accent=$gold})) {
    $b += Panel ("card-$($item.name)") 48 $item.y 984 190 $item.accent '#191A22'
    $b += Img ("art-$($item.name)") $item.art 82 ($item.y + 20) 138 138
    $b += Txt ("name-$($item.name)") 260 ($item.y + 82) $item.name 28 $white 900
    $b += Txt ("meta-$($item.name)") 260 ($item.y + 122) 'MYTHIC SUPERPOWER' 16 $item.accent 900
    $b += Rect ("equip-$($item.name)") 850 ($item.y + 112) 130 52 '#3A2F27' 'none' 12
    $b += Txt ("equip-label-$($item.name)") 915 ($item.y + 146) 'EQUIP' 15 $gold 900 'middle'
}
Svg '10-collection-editable.svg' $b

# Leaderboards
$b = Img 'world-background' $a.endgameBg 0 0 1080 2400
$b += Rect 'overlay' 0 0 1080 2400 '#07090F' 'none' 0 'fill-opacity="0.38"'
$b += Txt 'title' 60 150 'LEADERBOARDS' 58 $white 900
$b += Txt 'subtitle' 60 210 'GOOGLE PLAY / NO POWERS' 20 '#64E572' 900
$b += Rect 'back' 920 70 100 100 '#101722' $blue 18
$b += Txt 'back-label' 970 136 '‹' 64 $blue 900 'middle'
$b += Txt 'highest-label' 60 310 'HIGHEST LEVEL' 18 $muted 800
$b += Txt 'highest-value' 60 385 'L02' 56 $teal 900
$b += Txt 'streak-label' 590 310 'LONGEST STREAK' 18 $muted 800
$b += Txt 'streak-value' 590 385 'x1' 56 $pink 900
foreach ($item in @(@{mode='CLASSIC'; label='HIGHEST LEVEL'; value='L01'; y=470; accent=$teal}, @{mode='CHAOS'; label='HIGHEST LEVEL'; value='L02'; y=720; accent=$pink}, @{mode='CLASSIC'; label='LONGEST STREAK'; value='x0'; y=970; accent=$teal}, @{mode='CHAOS'; label='LONGEST STREAK'; value='x1'; y=1220; accent=$pink})) {
    $b += Panel ("board-$($item.mode)-$($item.label)-$($item.y)") 48 $item.y 984 210 $item.accent '#101722'
    $b += Txt ("mode-$($item.y)") 230 ($item.y + 58) $item.mode 18 $item.accent 900
    $b += Txt ("label-$($item.y)") 230 ($item.y + 120) $item.label 30 $white 900
    $b += Txt ("value-$($item.y)") 900 ($item.y + 120) $item.value 48 $item.accent 900 'end'
    $b += Txt ("meta-$($item.y)") 230 ($item.y + 165) 'OPEN FAIR GLOBAL RANKING' 16 $muted 700
}
Svg '11-leaderboards-editable.svg' $b

# Language selector
$b = Img 'world-background' $a.endgameBg 0 0 1080 2400
$b += Rect 'overlay' 0 0 1080 2400 '#07090F' 'none' 0 'fill-opacity="0.42"'
$b += Txt 'title' 60 150 'CHOOSE LANGUAGE' 58 $white 900
$b += Txt 'subtitle' 60 210 'GAME TEXT + COLLECTION VOICE' 20 $muted 800
$b += Rect 'back' 920 70 100 100 '#101722' $blue 18
$b += Txt 'back-label' 970 136 '‹' 64 $blue 900 'middle'
$languages = @('SYSTEM (EN)', 'English', 'Română', 'Español', 'Français', 'Deutsch', 'Italiano', 'Português', 'Nederlands', 'Polski', 'Türkçe')
for($i=0; $i -lt $languages.Count; $i++) {
    $y = 280 + ($i * 118)
    $selected = $i -eq 0
    $accent = if ($selected) { $blue } else { '#45506B' }
    $fill = if ($selected) { '#3D4B78' } else { '#101722' }
    $b += Rect "language-$i" 48 $y 984 96 $fill $accent 14
    $b += Txt "language-label-$i" 84 ($y + 45) $languages[$i] 24 $white 900
    $languageCode = if ($i -eq 0) { 'SYSTEM / English' } else { @('','EN','RO','ES','FR','DE','IT','PT','NL','PL','TR')[$i] }
    $languageAccent = if ($selected) { $blue } else { $muted }
    $b += Txt "language-code-$i" 84 ($y + 76) $languageCode 16 $languageAccent 800
    if ($selected) { $b += Circle "selected-$i" 970 ($y + 48) 16 $blue }
}
Svg '12-language-editable.svg' $b

# Privacy dialog
$b = Menu 'CHAOS' $a.chaosBg $pink 'CHAOS'
$b += Rect 'modal-scrim' 0 0 1080 2400 '#000000' 'none' 0 'fill-opacity="0.62"'
$b += Rect 'privacy-modal' 70 950 940 520 '#4B4B4B' 'none' 0
$b += Txt 'privacy-title' 132 1040 'PRIVACY' 40 $white 500
$b += Txt 'privacy-policy' 132 1150 'PRIVACY POLICY' 30 $white 400
$b += Txt 'ad-choices' 132 1280 'AD PRIVACY CHOICES' 30 $white 400
$b += Txt 'close' 132 1410 'CLOSE' 30 $white 400
Svg '13-privacy-editable.svg' $b

# Audio settings state
$b = Menu 'CHAOS' $a.chaosBg $pink 'CHAOS'
$b += Line 'sfx-slash' 802 2212 884 2296 $pink 10
$b += Line 'music-slash' 924 2212 1004 2296 $pink 10
$b += Txt 'audio-note' 540 2160 'SETTINGS / AUDIO OFF' 18 $muted 800 'middle'
Svg '14-settings-audio-editable.svg' $b

# Mode loadout
$b = Img 'world-background' $a.chaosBg 0 0 1080 2400
$b += Rect 'overlay' 0 0 1080 2400 '#07090F' 'none' 0 'fill-opacity="0.50"'
$b += Txt 'eyebrow' 64 84 'BRAINROT CHAOS' 24 $pink 800
$b += Txt 'title' 64 150 'KAVVORO' 72 $white 900
$b += Txt 'loadout' 64 1530 'CHAOS LOADOUT' 22 $pink 900
$b += Txt 'level' 64 1600 'LEVEL 02' 52 $white 900
$b += Txt 'streak' 980 1600 'STREAK 0' 22 $muted 800 'end'
$b += Panel 'start-new' 64 1660 952 150 $gold '#191A22'
$b += Img 'start-icon' $a.goal 96 1692 86 86
$b += Txt 'start-label' 210 1725 'START NEW' 30 $white 900
$b += Txt 'start-meta' 210 1770 'RESET TO LEVEL 01' 18 $muted 800
$b += Panel 'continue' 64 1830 952 170 $pink '#1A101F'
$b += Img 'continue-icon' $a.goal 96 1860 86 86
$b += Txt 'continue-label' 210 1895 'CONTINUE' 30 $white 900
$b += Txt 'continue-meta' 210 1940 'LEVEL 02  /  STREAK 0' 18 $muted 800
$b += Txt 'continue-level' 930 1895 'L02' 22 $pink 900 'end'
$b += Panel 'back' 64 2030 952 150 $blue '#101722'
$b += Txt 'back-label' 150 2120 '‹' 62 $blue 900 'middle'
$b += Txt 'back-title' 210 2095 'BACK' 30 $white 900
$b += Txt 'back-meta' 210 2140 'CHOOSE MODE' 18 $muted 800
Svg '15-chaos-loadout-editable.svg' $b

Write-Output "Generated $((Get-ChildItem $out -Filter '*.svg').Count) editable SVG screens in $out"
