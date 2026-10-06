# Plan de redesign UI Kavvoro

## Referințe aprobate

Acest plan folosește numai cele două pachete indicate pentru redesign:

- Home portrait: [referință aprobată](ui-redesign/references/home-portrait-approved.png), din `KAVVORO_Final_Home_Implementation_Pack.zip`.
- Home landscape: [referință aprobată](ui-redesign/references/home-landscape-approved.png), din `KAVVORO_Landscape_Home_Upgrade_Pack_v1.zip`.

Specificația portrait definește compoziția telefonului; pachetul landscape
adaugă un layout widescreen nativ și spune explicit să nu modifice portrait-ul
în acel pas. Capturile QA din `screenshots/` rămân baseline-uri ale aplicației,
nu surse de design.

## Principii de implementare

- Brainball-ul și logo-ul folosesc master-ele din pachetul aprobat; nu generăm
  artă înlocuitoare pentru Home.
- Butoanele, cardurile, chips și textele se desenează la runtime. Niciun text UI
  nu se bake-uiește într-un PNG/WebP.
- Home are o acțiune dominantă `PLAY NOW` și trei destinații: Skins, Missions,
  Leaderboard. Daily Rift apare ca stare/badge în Missions. Callout-ul generic
  din referința portrait poate rămâne informativ; nu adăugăm promoții temporare.
- Layout-ul folosește safe insets, dp/sp, hit-target-uri de minimum 48dp și
  păstrează rect-urile interactive în afara overlap-urilor vizuale.
- Toate textele noi trec prin cataloagele de localizare pentru cele 24 de limbi.
- Rendererele rămân în `ui/screens/<screen>/`; paleta și politicile comune stau
  în infrastructura UI. `ChaosGameView` rămâne orchestrator.
- Refacem sau generăm asset-uri numai când pachetul aprobat nu acoperă o nevoie
  reală. Pentru asset-urile noi păstrăm sursa editabilă și exportul Android
  optimizat; nu duplicăm mascotul aprobat.

## Ordinea de lucru

### 1. Home — prima implementare finalizată

1. Master-ele aprobate pentru logo/Brainball sunt folosite în aplicație;
   decorul asteroid este adăugat numai în layout-ul landscape.
2. CTA-ul este desenat procedural, iar textul, iconurile și chevron-ul rămân
   elemente runtime.
3. Layout-ul landscape folosește scena Hero + Navigation Deck, cu CTA lângă
   portal, leaderboard lat și Daily Rift în Missions; nu adaugă promoție sau
   prompt de rotire.
4. Layout-urile portrait și landscape au capturi QA versionate la
   `1080×2400@420dpi` și `1920×1200@240dpi`; testele de layout acoperă limitele
   CTA-ului și header-ul compact.
5. Watermark-ul landscape este localizat în toate cele 24 de cataloage.
6. Rămâne polish-ul manual pentru TalkBack, navigare, orientare, traduceri
   lungi și matricea compact/standard/tabletă; capturile actuale nu reprezintă
   aprobarea întregii matrice de dispozitive.

### 2. Age Check — prima implementare finalizată

- `MainActivity` afișează acum ecranul la prima pornire când nu există profil;
  nu mai salvează automat `ADULT`.
- Selectorul păstrează vârsta doar în memorie până la Continue, apoi salvează
  numai `AgeGroup`: 1–12 `CHILD`, 13–17 `TEEN`, 18–120 `ADULT`.
- Compoziția folosește tokens Home, textul existent localizat și selectorul are
  acțiuni de accesibilitate pentru increment/decrement.
- Cele 11 capturi portrait sunt actualizate în `screenshots/age-check/`.
  Rămâne verificarea manuală cu TalkBack, text scaling și RTL pe dispozitive
  fizice.

### 3. Settings — aliniere inițială finalizată

- Fundalul și accentele cyan, magenta și gold folosesc acum `KavvoroPalette`;
  separarea taburilor și stările accentuate rămân clare.
- Acțiunile, stocarea și confirmările existente nu au fost schimbate.
- Urmează revizuirea manuală a dialogurilor, text scaling-ului și accesibilității
  cu TalkBack pe ecrane mici și tablete.

### 4. Language selector

- Aplicăm paleta, selecția vizibilă și aceeași familie de carduri; verificăm
  texte lungi, RTL și accesarea cu TalkBack.

### 5. Collection și Leaderboards

- Uniformizăm cardurile, stările locked/equipped, filtrele și ierarhia scorurilor;
  culorile de stare au și diferențe de formă/icon, nu doar de hue.

### 6. Gameplay, Tutorial și Outcome

- Facem HUD-ul, telegramele de hazard, reward-urile, tutorialele și ecranele de
  rezultat să folosească aceiași tokens și semnale redundante.
- Legăm suportul `Reduce Motion` de efectele neesențiale și păstrăm mișcarea
  necesară înțelegerii fizicii jocului.

### 7. Modals, Ad și polish de sistem

- Uniformizăm dialogs, ecranul de tranziție Ad și overlay-urile; completăm
  provider-ele de noduri virtuale pentru fiecare ecran, nu doar Home.

## Bază tehnică și puncte deschise

- `HomeLayoutCalculator` separă portrait/landscape, iar CTA-ul și watermark-ul
  Home au acum teste pentru contractele actualizate.
- `AgeCheckScreenView` deține interfața startup; `MainActivity` pornește
  serviciile de joc, reclame și billing numai după alegerea categoriei.
- Setările folosesc `KavvoroPalette`; ecranele rămase trebuie migrate gradual
  la aceiași tokens.
- Asset-urile aprobate au surse versionate în `art/ui/home-approved/` și
  exporturi optimizate în `app/src/main/res/drawable-nodpi/`.
- Auditul anterior a identificat și puncte cross-screen încă deschise:
  accessibility nodes pentru Settings/Language/gameplay/Collection/Results,
  suport `Reduce Motion`, tokens de paletă și codare redundantă a semnificației
  pericol/recompensă. Acestea rămân parte din fazele de mai sus.

## Criterii de acceptare pentru fiecare ecran

- Comparație vizuală pe cel puțin un profil compact, unul standard și tabletă;
  Home adaugă și un profil landscape.
- Teste de layout/hit-testing și suita de protecție din `AGENTS.md` trec.
- Nicio regresie în navigare, state, lifecycle sau localizare.
- Niciun pachet gol, asset orfan sau fișier temporar introdus.
- Capturile finale sunt organizate descriptiv sub `screenshots/`.
