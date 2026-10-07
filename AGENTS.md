# KAVVORO — AI & Engineering Regulament (`AGENTS.md`)

> **CRITICAL FOR ALL AI AGENTS (Cursor, Antigravity, Claude Code, Copilot, Windsurf):**
> Acest fișier definește regulamentul arhitectural obligatoriu al proiectului **Kavvoro**.
> Orice modificare de cod este verificată automat de suitele de teste de gardă (`ProjectArchitectureRulesTest`, `ScreenModuleSeparationTest`, `HomeCopyCatalogCoverageTest`, `LocalizationCatalogTest`).
> **Nu încălca niciodată regulile de mai jos.**

---

## 1. Arhitectura pe Pachete & Separarea Ecranelor (`ui/screens/`)

Codul sursă (`app/src/main/java/com/moonsolstudios/kavvoro/`) este împărțit strict pe domenii și pe ecrane:

- `ads/` — `AdBridge`, `AdPolicyController`, `InterstitialAdController`, `RewardedAdController`
- `audio/` — `KavvoroSoundEngine`, `MusicTransition`, `SelectionPreviewFade`
- `billing/` — `PurchaseBridge`, `PlayBillingController`, `PremiumCatalog`
- `engine/` — Fizică pură, geometrie, scoruri și generare de nivele (`PhysicsEngine`, `LevelDirector`, `AdvancedLevelDirector`, `GameplayScoreCalculator`, `Geometry`, `ReplayRecorder`)
- `i18n/` — Infrastructura de localizare (`KavvoroI18n`, `LocalizationCatalog`, `HomeCopy`, `TutorialCopy`, `UiTranslations`) + `i18n/catalog/*Translations.kt` (cele 24 de cataloage de limbă)
- `model/` — Modele de date imutabile și enum-uri de stare (`BallSkinModels`, `DisplayModels`, `GameSessionModels`, `UiStateModels`)
- `playgames/` — `AccountBridge`, `LeaderboardBridge`, `LeaderboardScoreGuard`, `PlayGamesAccountController`, `PlayGamesLeaderboardController`
- `privacy/` — `PrivacyBridge`, `PrivacyAdsController`, `AgeProfile`, `LegalDocumentActivity`, `LegalDocumentPage`
- `repository/` — `AccountProgressStore`, `BallSkinCatalog`, `GameProgressRepository`
- `share/` — `ReplayShareController`, `ReplayVideoExporter`
- `startup/` — `FirstFrameStartupGate`
- `ui/`
  - `ChaosGameView.kt` — **Doar orchestrator** (`SurfaceView`, bucla de joc, starea sesiunii curente și delegare către ecrane).
  - `HapticFeedbackCompat.kt` — Helper partajat pentru feedback haptic compatibil Android.
  - `controller/` — `AdaptiveQualityController`, `GameLoopDirector`
  - `layout/` — `LocaleLayoutPolicy` (politică pură de încadrare a textului, fără dependențe de `ui/screens/*`)
  - `render/` — Primitive și resurse grafice partajate între toate ecranele (`AssetResourceManager`, `AtmosphereRenderer`, `BrandTitleRenderer`, `CyberShapeRenderer`, `LayoutRect`, `RenderColor`, `UiWidgetRenderer`)
  - `tutorial/` — `TutorialCardLayout`, `TutorialInputGate`, `TutorialRenderer`, `TutorialTouchController`
  - `screens/` — **Fiecare ecran are propriul sub-pachet izolat:**
    - `screens/home/` — Ecranul Home + Mode Picker (`HomeLayoutCalculator`, `HomeMenuRenderer`, `HomeMenuTouchController`, `HomeAccessibilityTouchHelper`, `ModePickerLayoutCalculator`, `ModePickerRenderer`, `SciFiCtaButtonRenderer`)
    - `screens/agecheck/` — Ecranul de selecție a vârstei la prima pornire (`AgeCheckScreenView`)
    - `screens/missions/` — Misiuni zilnice (`MissionsLayoutCalculator`, `MissionsUiRenderer`, `MissionsTouchController`, `MissionsScreenController`, `MissionsCompletionPopupRenderer`)
    - `screens/gameplay/` — Arena de joc și HUD-ul (`GameplayArenaRenderer`, `GameplayHudRenderer`, `GameplayTouchController`)
    - `screens/outcome/` — Ecranul de Victorie / Înfrângere (`OutcomeUiRenderer`, `OutcomeTouchController`)
    - `screens/collection/` — Ecranul Skins / Colecție (`CollectionLayoutCalculator`, `CollectionUiRenderer`, `CollectionTouchController`, `BallSkinRenderer`)
    - `screens/leaderboards/` — Ecranul Clasamente (`LeaderboardLayoutCalculator`, `LeaderboardUiRenderer`, `LeaderboardTouchController`)
    - `screens/settings/` — Ecranul Setări (`SettingsLayoutCalculator`, `SettingsUiRenderer`, `SettingsTouchController`, `SettingsIconRenderer`)
    - `screens/language/` — Selectorul de Limbă (`LanguageSelectorLayoutCalculator`, `LanguageSelectorRenderer`, `LanguageTouchController`, `FlagDrawableManager`, `LanguageSelectorLayout`, `LanguageSelectorMetrics`)
    - `screens/ad/` — Ecranul de tranziție Ad (`AdScreenRenderer`, `AdScreenTouchController`)
    - `screens/modals/` — Modale și overlay-uri (`DailyRiftRewardRenderer`, `ShareExportOverlayRenderer`, `TabletOrientationPromptRenderer`, `TabletOrientationPromptController`)

### Reguli Stricte pentru UI & `ChaosGameView.kt`:
1. **Zero Cross-Screen Imports & Zero Upward Imports**:
   - **Niciun fișier `.kt`** (`*Renderer.kt`, `*TouchController.kt`, `*LayoutCalculator.kt`, `*Helper.kt`) din `ui/screens/<ecranA>/` nu are voie să importe din `ui/screens/<ecranB>/`.
   - Pachetele de infrastructură UI partajată (`ui/render/`, `ui/layout/`, `ui/controller/`, `ui/tutorial/`) nu au voie să importe niciodată din `ui/screens/*`. Orice helper vizual sau de layout partajat pe mai multe ecrane stă în `ui/render/` sau `ui/layout/`, iar enum-urile de stare stau în `model/UiStateModels.kt`.
2. **Zero `RectF` de Ecran în `ChaosGameView.kt`**: `ChaosGameView.kt` are voie să aloce **doar** `private val scratch = RectF()`. Toate dreptunghiurile de hit-testing (`RectF`) pentru butoane sau carduri trebuie deținute de `*TouchController` sau `*LayoutCalculator` din pachetul ecranului respectiv.
3. **Interdicție de Umflare a `ChaosGameView.kt`**: Nu adăuga metode de desenare (`Canvas`), calcule de layout sau hit-testing direct în `ChaosGameView.kt`. Orice funcționalitate nouă pe un ecran se implementează în `ui/screens/<screen>/` și este apelată din coordonatorul potrivit: `ChaosGameView.kt` pentru ecranele jocului, respectiv `MainActivity` pentru Age Check la pornire (plafon maxim `3,350` linii pentru `ChaosGameView.kt`).
4. **Izolarea `engine/` și `model/`**: Pachetele `engine/` și `model/` nu au voie să importe absolut nimic din `com.moonsolstudios.kavvoro.ui.*`.
5. **Bridges în Pachetele de Domeniu**: Toate interfețele Bridge (`AdBridge`, `AccountBridge`, `LeaderboardBridge`, `PrivacyBridge`, `PurchaseBridge`) stau în pachetele lor de domeniu (`ads/`, `playgames/`, `privacy/`, `billing/`), niciodată imbricate în `ChaosGameView` sau `MainActivity`.
6. **Alinierea Pachetelor de Teste (`app/src/test/java/`)**: Toate fișierele `.kt` din `src/main/java` și `src/test/java` trebuie să aibă declarația `package` identică cu calea directorului, iar testele pe ecrane stau în `ui/screens/<screen>/`.

---

## 2. Regulamentul de Localizare (i18n — 24 Limbi)

1. **Sursă Unică de Adevăr (`i18n/catalog/*Translations.kt`)**:
   - Toate traducerile trăiesc **exclusiv** în cele 24 de fișiere din `app/src/main/java/com/moonsolstudios/kavvoro/i18n/catalog/*Translations.kt` (`ArTranslations.kt` ... `ZhTwTranslations.kt`).
   - Este **interzisă** definirea de dicționare/hărți de traduceri în `HomeCopy.kt`, `TutorialCopy.kt`, `UiTranslations.kt`, `LocalizationCatalog.kt` sau în foldere Android `app/src/main/res/values-<lang>/strings.xml`. Aceste 4 fișiere Kotlin definesc doar seturile de chei (`requiredKeys` / `renderedSourceKeys`) și funcții helper de interogare.
2. **Partiționare Exactă a Cheilor**:
   - O cheie de traducere trebuie să aparțină unui singur set de chei (`TutorialCopy.requiredKeys`, `UiTranslations.requiredKeys`, `HomeCopy.requiredKeys`, `LocalizationCatalog.renderedSourceKeys`), fără duplicate între ele.
   - Fiecare fișier `*Translations.kt` trebuie să conțină **exact** uniunea tuturor cheilor cerute (0 chei lipsă, 0 chei orfane).
3. **Adăugarea unui Text Nou în UI**:
   - Orice text vizibil utilizatorului trebuie trecut prin `t("KEY")` / `KavvoroI18n.t(context, "KEY")`.
   - Adaugă cheia în setul corespunzător (`HomeCopy.requiredKeys`, `TutorialCopy.requiredKeys`, `UiTranslations.requiredKeys`, sau `LocalizationCatalog.renderedSourceKeys`) și adaugă traducerea în toate cele **24** de fișiere `*Translations.kt`.

---

## 3. Igiena Codului & Verificarea Obligatorie

1. **Zero Fișiere Temporare / Scratch**:
   - Nu lăsa niciodată directoare sau fișiere temporare (`scratch/`, `temp_crops/`, `implementation_pack/`, `preview/`, `.superpowers/`, `scratch_*.png`, `temp_*.webp`, scripturi Python one-off) în root sau în `app/src/`.
   - Nu lăsa pachete goale sau interfețe/clase nefolosite (dead code) în `app/src/main/java/`.
2. **Rularea Testelor înainte de Finalizare**:
   - Pe această mașină Windows, `JAVA_HOME` este `"C:\Program Files\Android\Android Studio\jbr"`.
   - Înainte de a încheia orice task, rulează suita de teste unitare și de arhitectură:
     ```powershell
     $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
     .\gradlew testDebugUnitTest
     ```
