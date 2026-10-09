---
trigger: always_on
description: Mandatory Kavvoro architecture, per-screen modularization, and 24-language i18n catalog rules
---

# Kavvoro Architecture & i18n Rules

1. **Per-Screen Modularity (`ui/screens/<screen>/`)**:
   - Every UI screen belongs in its dedicated package under `app/src/main/java/com/moonsolstudios/kavvoro/ui/screens/` (`home`, `launch`, `agecheck`, `missions`, `gameplay`, `outcome`, `collection`, `leaderboards`, `settings`, `language`, `ad`, `modals`) or `ui/tutorial/`.
   - Files in `ui/screens/<screen>/` (Renderers, TouchControllers, LayoutCalculators, Helpers) MUST NEVER import from another `ui/screens/<other>/` package, and shared layers (`ui/render/`, `ui/layout/`, `ui/controller/`, `ui/tutorial/`) MUST NEVER import upward from `ui/screens/*`.
   - `ChaosGameView.kt` MUST NOT allocate screen button `RectF` fields (only its single `private val scratch = RectF()`). All screen touch/layout rects belong in their screen's `*TouchController` or `*LayoutCalculator`.
   - `ChaosGameView.kt` must stay below `3,350` lines. Never add new screen rendering or layout calculation blocks directly inside `ChaosGameView.kt`.

2. **Domain Isolation (`engine/`, `model/`, `*Bridge`)**:
   - `engine/` and `model/` packages must remain UI-independent and NEVER import from `com.moonsolstudios.kavvoro.ui.*`.
   - All runtime bridges (`AdBridge`, `AccountBridge`, `LeaderboardBridge`, `PrivacyBridge`, `PurchaseBridge`) must live in their respective domain packages (`ads/`, `playgames/`, `privacy/`, `billing/`), never nested inside `ChaosGameView` or `MainActivity`.

3. **Strict 24-Language Catalog i18n (`i18n/catalog/*Translations.kt`)**:
   - All translations live exclusively in the 24 per-language files under `app/src/main/java/com/moonsolstudios/kavvoro/i18n/catalog/*Translations.kt`.
   - Never embed translation maps in `HomeCopy.kt`, `TutorialCopy.kt`, `UiTranslations.kt`, or `LocalizationCatalog.kt`.
   - Every new user-visible string key must be added to the appropriate required key set (`HomeCopy.requiredKeys`, `TutorialCopy.requiredKeys`, `UiTranslations.requiredKeys`, or `LocalizationCatalog.renderedSourceKeys`) and translated across all 24 `*Translations.kt` files with zero missing or extra keys.

4. **Verification**:
   - Always run `./gradlew testDebugUnitTest` (with `JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"`) to verify `ProjectArchitectureRulesTest`, `ScreenModuleSeparationTest`, and `LocalizationCatalogTest`.
