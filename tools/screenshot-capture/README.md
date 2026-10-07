# Screenshot capture tools

Acesta este folderul permanent pentru scripturile reproductibile de captură ale
aplicației.

`capture_support.py` este sursa comună pentru calea SDK-ului Android, package,
APK și matricea celor 11 rezoluții. SDK-ul se rezolvă din `ANDROID_HOME` sau
`ANDROID_SDK_ROOT`, fără căi absolute către o stație de lucru.

Păstrăm aici doar automatizări care pot fi reluate pe alt calculator. Scripturile
exploratorii de crop/PSD și outputurile lor intermediare nu se versioneză;
asset-urile Android canonice sunt în `app/src/main/res`, iar master-ele editabile
sunt în `art/`. Referințele aprobate pentru redesign sunt în
`docs/ui-redesign/references/`.

## Captura Age Check

```powershell
python .\tools\screenshot-capture\retake_age_check_11.py
```

Scriptul capturează setul standard actual în `screenshots/age-check`, în
portrait pe telefoane și tablete. Captura verifică și nodul accesibil al
selectorului, ca să nu accepte din greșeală ecranul Home.

## Captura Settings

```powershell
python .\tools\screenshot-capture\retake_settings_11.py
```

Scriptul capturează setul standard actual în `screenshots/settings`, pe tab-ul SYSTEM.
Numele include DPI-ul, de exemplu `phone-360x800-160dpi.png`, `phone-1080x2400-420dpi.png`, `tablet-1600x2560-320dpi.png`.

- telefoane: `360x800@160`, `412x915@160`, `480x854@160`, `720x1280@320`, `1080x2400@420`
- tablete: `600x1024@160`, `800x1280@160`, `1024x1366@160`, `1200x1920@240`, `1536x2048@240`, `1600x2560@320`

Lansează debug extra `screen=settings` și `tab=system`. Densitatea este per-rezoluție, nu 160 dpi global: telefoanele 360/412/480 rămân 160 dpi (1 px = 1 dp), iar 720/1080/1200/1536/1600 folosesc DPI realist. Altfel 1600×2560 ar fi tratat ca 1600 dp (card centrat, text mic) în loc de ~800 dp cât are un Pixel Tablet.

## Captura Home Screen

```powershell
python .\tools\screenshot-capture\retake_home_11.py
```

Scriptul capturează setul standard actual în `screenshots/home`.
Numele include DPI-ul, de exemplu `phone-360x800-160dpi.png`.

- telefoane: `360x800@160`, `412x915@160`, `480x854@160`, `720x1280@320`, `1080x2400@420`
- tablete: `600x1024@160`, `800x1280@160`, `1024x1366@160`, `1200x1920@240`, `1536x2048@240`, `1600x2560@320`

Funcționalități și mecanisme de siguranță:
1. **Bypass automat Age Gate**: Injectează fixture-ul sintetic
   `fixtures/privacy_profile.xml` în `shared_prefs`. Fixture-ul aparține acestor
   scripturi și nu trebuie mutat în rădăcina repository-ului.
2. **Orientare naturală Pixel Tablet**: Configurează `user_rotation = 1` și inversează `wm size` (`{height}x{width}`) pentru randare portrait pe emulator landscape.
3. **Detecție vizuală în memorie (`exec-out screencap`)**: Elimină I/O lent pe disk și detectează tranziția de la splash (1.45s) direct la Home complet randat.
4. **Verificare post-captură și retry automat**: Verifică integritatea imaginii, dimensiunile exacte, lipsa ecranelor negre/splash/age-gate și prezența elementelor de UI active (header + footer neon). Dacă verificarea eșuează, relansează automat procesul până la capturarea unui cadru valid.
5. **Restaurare automată**: Restaurează `wm size` și `wm density` la finalul rulării.

## Captura Missions

```powershell
python .\tools\screenshot-capture\retake_missions_phone.py
```

Scriptul capturează ambele categorii Missions la profilul telefon
`1080×2400@420dpi`: `screenshots/missions/phone-1080x2400-420dpi.png` și
`screenshots/missions/rift-challenges-phone-1080x2400-420dpi.png`. Lansează
direct ecranul Missions pe build-ul debug, folosește profilul de confidențialitate
sintetic și verifică dimensiunea, conținutul și accentele fiecărei categorii.
Setările de rezoluție și densitate ale emulatorului sunt restaurate la final.

## Captura Home landscape

```powershell
python .\tools\screenshot-capture\retake_home_landscape.py
```

Scriptul capturează profilul tabletă landscape aprobat de `1920×1200@240dpi`
în `screenshots/home/tablet-landscape-1920x1200-240dpi.png`. Așteaptă viewport-ul
landscape, închide promptul Android de ecran complet numai după ce îi detectează
textul și verifică dimensiunea capturii. Densitatea și orientarea emulatorului
sunt restaurate la final.
