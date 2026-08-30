# Screenshot capture tools

Acesta este folderul permanent pentru scripturile reproductibile de captură ale
aplicației.

`capture_support.py` este sursa comună pentru calea SDK-ului Android, package,
APK și matricea celor 11 rezoluții. SDK-ul se rezolvă din `ANDROID_HOME` sau
`ANDROID_SDK_ROOT`, fără căi absolute către o stație de lucru.

Păstrăm aici doar automatizări care pot fi reluate pe alt calculator. Scripturile
exploratorii de crop/PSD și outputurile lor intermediare nu se versioneză;
asset-urile canonice sunt în `app/src/main/res`, `art/` și `figma-assets/`.

## Captura Age Check

```powershell
python .\tools\screenshot-capture\retake_age_check_11.py
```

Scriptul capturează setul standard actual în `screenshots/age-check`.

## Captura Home Screen

```powershell
python .\tools\screenshot-capture\retake_home_11.py
```

Scriptul capturează setul standard actual în `screenshots/home`:

- telefoane: `360x800`, `412x915`, `480x854`, `720x1280`, `1080x2400`
- tablete: `600x1024`, `800x1280`, `1024x1366`, `1200x1920`, `1536x2048`, `1600x2560`

Funcționalități și mecanisme de siguranță:
1. **Bypass automat Age Gate**: Injectează fixture-ul sintetic
   `fixtures/privacy_profile.xml` în `shared_prefs`. Fixture-ul aparține acestor
   scripturi și nu trebuie mutat în rădăcina repository-ului.
2. **Orientare naturală Pixel Tablet**: Configurează `user_rotation = 1` și inversează `wm size` (`{height}x{width}`) pentru randare portrait pe emulator landscape.
3. **Detecție vizuală în memorie (`exec-out screencap`)**: Elimină I/O lent pe disk și detectează tranziția de la splash (1.45s) direct la Home complet randat.
4. **Verificare post-captură și retry automat**: Verifică integritatea imaginii, dimensiunile exacte, lipsa ecranelor negre/splash/age-gate și prezența elementelor de UI active (header + footer neon). Dacă verificarea eșuează, relansează automat procesul până la capturarea unui cadru valid.
5. **Restaurare automată**: Restaurează `wm size` și `wm density` la finalul rulării.
