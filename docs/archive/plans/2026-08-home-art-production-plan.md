# Kavvoro — Plan profesional pentru înlocuirea artei din Home

> Document istoric păstrat pentru contextul producției vizuale din august
> 2026. README-ul repository-ului și codul curent sunt sursele de adevăr pentru
> structură, status și implementare.

**Proiect:** `C:\Users\Alin\Desktop\MoonsolStudios\Kavvoro`  
**Referințe Figma:** `C:\Users\Alin\Desktop\figma\assets`  
**Generator local propus:** FLUX.2 Klein 4B Distilled prin ComfyUI  
**Țintă:** artă premium, localizabilă, fără text imprimat în imagini, fără stretching și adaptivă pe minimum 11 rezoluții.

> Acest document este planul de producție. Toate statusurile pornesc ca nefinalizate. Un asset este considerat final numai după integrarea în APK și verificarea vizuală pe toate profilele de test.

## 1. Principii obligatorii

- Nu folosim `home_reference_flattened.png` ca UI final. Este numai moodboard/referință.
- Nu folosim PNG-uri cu texte precum `PLAY`, `LEADERBOARDS` sau `KAVVORO` în APK.
- FLUX este folosit pentru ilustrații, atmosferă, materiale și referințe vizuale. Nu îi cerem să producă SVG-uri finale sau text UI precis.
- Textul este randat nativ în Android și vine din `strings.xml`, pentru traduceri, valori dinamice și accesibilitate.
- Iconurile simple, ramele, cardurile, săgețile și barele de progres sunt SVG, `VectorDrawable`, Canvas sau 9-patch — nu bitmap-uri generate.
- Personajul, portalul și fundalul complex pot rămâne WebP/PNG, dar trebuie să aibă aspect ratio păstrat și zone sigure pentru crop.
- Nu creăm câte un PNG pentru fiecare rezoluție. Creăm asset-uri robuste și un layout care le afișează corect.
- Nu suprascriem asset-urile vechi până când înlocuitorul nu este verificat în APK.
- Nu păstrăm copii full-resolution inutile. Referințele, fișierele de lucru și asset-urile finale trebuie delimitate clar.

## 2. Inventarul asset-urilor existente

### 2.1. Referințe, nu asset-uri de producție

| Fișier | Rol | Decizie |
|---|---|---|
| `home_reference_flattened.png` | Screenshot complet cu text și UI aplatizat | Nu se livrează în APK |
| `hero_reference.png` | Referință pentru portal, platformă și personaj | Se folosește pentru recreare, nu se livrează ca un singur bloc |

### 2.2. Asset-uri aplatizate care trebuie reconstruite

| Fișier | Ce conține | Înlocuitor profesional |
|---|---|---|
| `brand_header.png` | Logo, tagline și tipografie | mark/vector + text Android separat |
| `best_streak.png` | card, icon, titlu și valoare | card comun + icon SVG + text dinamic |
| `hype.png` | card, icon, titlu și valoare | card comun + icon SVG + text dinamic |
| `level.png` | card, icon, titlu, valoare și progres | card comun + icon SVG + progress bar Canvas |
| `daily_rift.png` | card, icon, titlu și status | card comun + icon SVG + text/status dinamic |
| `rift_online_status.png` | capsulă și text | capsulă Canvas/SVG + text dinamic |
| `play_cta.png` | ramă, text, icon și glows | chassis vector/Canvas + text Android + chevron |
| `leaderboards.png` | card, icon, text și chevron | card comun + icon SVG + text Android |
| `vault_maxed.png` | card, icon, text și chevron | card comun + icon SVG + text Android |
| `collection_unlocks.png` | card, ring, cube, text și divider | card comun + iconuri SVG + text Android |
| `footer_note.png` | text | `strings.xml` și TextPaint/TextView |

### 2.3. Asset-uri care trebuie refăcute ca vector/UI nativ

| Fișier | Decizie |
|---|---|
| `settings_button.png` | Nu se folosește ca bitmap; buton Canvas + gear `VectorDrawable` |
| `sound_button.png` | Nu se folosește ca bitmap; buton Canvas + speaker `VectorDrawable` |

## 3. Structura finală recomandată

Asset-urile finale trebuie să existe într-un singur loc consumat de APK, iar imaginile de lucru să nu fie copiate inutil în proiect:

```text
app/src/main/res/
├── drawable-nodpi/
│   ├── home_background.webp
│   ├── home_portal_back.webp
│   ├── home_platform.webp
│   ├── home_portal_front_fx.webp
│   ├── brainball_kavvoro.webp
│   └── ...alte personaje WebP/PNG cu transparență...
├── drawable/
│   ├── ic_settings.xml
│   ├── ic_sound.xml
│   ├── ic_best_streak.xml
│   ├── ic_hype.xml
│   ├── ic_level.xml
│   ├── ic_daily_rift.xml
│   ├── ic_leaderboards.xml
│   ├── ic_vault.xml
│   ├── ic_collection.xml
│   ├── ic_next_unlock.xml
│   └── ic_chevron.xml
├── values/
│   ├── colors.xml
│   ├── dimens.xml
│   ├── strings.xml
│   └── styles.xml
└── values-ro/
    └── strings.xml
```

Asset-urile FLUX brute și imaginile respinse se păstrează în afara `app/src/main/res`, într-un folder de lucru separat, numai cât timp sunt necesare pentru producție. Nu se includ în APK.

## 4. Standard FLUX.2 Klein 4B

Pentru fiecare generare se păstrează:

- modelul exact și workflow-ul ComfyUI;
- seed-ul;
- rezoluția;
- numărul de pași;
- promptul folosit;
- imaginea/imaginile de referință;
- varianta acceptată și motivul acceptării.

Reguli:

- pentru artă finală: image-to-image sau multi-reference, nu text-to-image complet aleator;
- pentru păstrarea personajului: aceeași referință principală la fiecare iterație;
- pentru retuș: schimbăm un singur lucru per iterație;
- generăm fără texte, litere, watermark-uri sau logo-uri false;
- pentru transparență, verificăm efectiv canalul alpha; dacă workflow-ul nu îl produce corect, folosim un pass separat de background removal/matting;
- generăm inițial la rezoluție moderată, apoi facem upscale controlat numai după aprobarea compoziției;
- nu acceptăm asset-uri doar pentru că arată bine într-un preview. Trebuie să se potrivească cu restul pachetului Kavvoro.

## 5. Matricea de verificare responsive

Fiecare element final trebuie verificat în APK pe aceste 11 profile logice:

| ID | Rezoluție logică | Tip |
|---:|---:|---|
| R01 | 320×568 dp | phone compact |
| R02 | 360×640 dp | phone 16:9 |
| R03 | 360×800 dp | phone înalt |
| R04 | 375×812 dp | phone standard |
| R05 | 390×844 dp | baseline |
| R06 | 393×852 dp | phone standard înalt |
| R07 | 412×915 dp | phone mare |
| R08 | 480×960 dp | phone foarte mare |
| R09 | 600×1024 dp | tablet compact |
| R10 | 800×1280 dp | tablet 7 inch |
| R11 | 1200×1920 dp | tablet 10 inch |

Pentru fiecare rezoluție se verifică:

- `PASS` — vizibil integral;
- `PASS` — fără stretching sau aspect ratio incorect;
- `PASS` — focal point-ul personajului nu este tăiat;
- `PASS` — textul nu este imprimat în bitmap și nu este tăiat;
- `PASS` — minimum 48 dp pentru zonele interactive;
- `PASS` — fără overlap între elemente;
- `PASS` — contrast și glow lizibile;
- `PASS` — layout-ul respectă safe insets;
- `PASS` — nu există referințe la vechile PNG-uri aplatizate.

## 6. Plan detaliat pe fiecare element din Home

Pentru fiecare secțiune, checkbox-urile sunt statusuri de execuție, nu recomandări. Se bifează numai după verificare.

---

## H01 — Background principal

**Fișier actual de referință:** `home_reference_flattened.png`  
**Fișier final propus:** `app/src/main/res/drawable-nodpi/home_background.webp`  
**Tip final:** WebP fără text și fără UI imprimat.

### Decizii

- SVG final: ☐ DA  ☒ NU — este fundal complex, nu grafică vectorială.
- Generat cu FLUX.2 Klein 4B: ☐ DA  ☐ NU
- FLUX folosit ca referință vizuală: ☐ DA  ☐ NU
- Integrat în APK: ☐ DA  ☐ NU
- Fără text imprimat: ☐ DA  ☐ NU
- Aspect ratio păstrat: ☐ DA  ☐ NU
- Adaptiv pe R01–R11: ☐ DA  ☐ NU
- Verificat fără crop distructiv: ☐ DA  ☐ NU

### Prompt FLUX.2 Klein 4B

```text
Use case: stylized-concept
Asset type: premium mobile game home-screen background
Primary request: Create a high-end vertical sci-fi game background for the Kavvoro universe, inspired by a neon dimensional rift. The image must work as a clean background layer behind separate UI, portal, platform, and character assets.
Scene/backdrop: Deep midnight navy space, subtle black-violet gradients, a large circular dimensional rift centered slightly below the vertical midpoint, cyan energy on the left and magenta energy on the right, restrained particles and atmospheric haze, a dark futuristic floor area near the bottom.
Composition/framing: Portrait composition with generous clean negative space at the top for a logo and statistics, controlled negative space near the bottom for a CTA and cards, central focal area reserved for a separate portal and character. Keep the main energy ring inside the central safe area so it can be cropped on multiple portrait aspect ratios.
Lighting/mood: Premium cinematic game UI atmosphere, controlled bloom, crisp neon edges, deep blacks, no muddy gray wash.
Color palette: Midnight navy, near-black, electric cyan, ultraviolet, hot magenta, very limited white highlights.
Materials/textures: Subtle sci-fi circuitry, glass-like energy, restrained particles, clean professional game art.
Constraints: Background only. No character, no mascot, no crown, no platform centerpiece, no panels, no buttons, no UI, no logo, no readable text, no letters, no watermark. Keep the central focal area usable for compositing.
Avoid: generic cyberpunk city, excessive noise, random symbols, fake interface, overexposed white bloom, stretched geometry, visible seams.
```

### Criterii de acceptare

- Fundalul nu conține niciun text sau element UI.
- `centerCrop` nu taie energia principală pe profilele R01–R11.
- Personajul și portalul pot fi compuse separat fără margini vizibile.
- Contrastul rămâne suficient pentru text alb, cyan și magenta.

---

## H02 — Portal / rift back layer

**Fișier actual:** `home_portal_back` și referința din `hero_reference.png`  
**Fișier final:** `home_portal_back.webp`  
**Tip final:** WebP/PNG cu transparență sau fundal eliminat curat.

### Decizii

- SVG final: ☐ DA  ☒ NU — este VFX complex.
- Generat cu FLUX.2 Klein 4B: ☐ DA  ☐ NU
- Integrat în APK: ☐ DA  ☐ NU
- Canal alpha valid: ☐ DA  ☐ NU
- Fără stretching: ☐ DA  ☐ NU
- Adaptiv pe R01–R11: ☐ DA  ☐ NU

### Prompt FLUX.2 Klein 4B

```text
Use case: stylized-concept
Asset type: isolated sci-fi dimensional portal VFX layer for a mobile game
Primary request: Create a clean circular dimensional rift made of concentric mechanical energy rings, intended to be composited behind a separate character. The rift must feel premium, designed, and readable rather than chaotic.
Scene/backdrop: Isolated portal effect with transparent-ready dark background, no environment and no floor.
Subject: Symmetrical circular portal with layered rings, segmented metallic technology, cyan energy arcs on the left, magenta energy arcs on the right, subtle electric filaments and controlled particles.
Composition/framing: Centered square composition, complete outer ring visible, generous margin around the effect, no important detail touching the edges.
Lighting/mood: Crisp neon emission, deep navy shadows, cinematic but controlled bloom.
Color palette: Electric cyan, blue-violet, hot magenta, small white highlights, near-black metallic details.
Materials/textures: Brushed dark metal, glass energy, precise segmented geometry.
Constraints: Portal only. No character, no crown, no text, no symbols, no buttons, no UI, no watermark. Preserve bilateral cyan/magenta balance. The result must be suitable for background removal and alpha compositing.
Avoid: random decorative clutter, soft blurry rings, melted geometry, asymmetrical accidents, fake text, excessive lens flare.
```

### Criterii de acceptare

- Inelul este complet și nu are geometrie tăiată.
- VFX-ul poate fi redimensionat uniform.
- Nu interferează cu conturul personajului.
- Se poate dezactiva sau anima separat în Android.

---

## H03 — Platforma de sub portal

**Fișier actual:** `home_platform` și platforma din `hero_reference.png`  
**Fișier final:** `home_platform.webp`  
**Tip final:** WebP/PNG cu transparență sau compusă procedural.

### Decizii

- SVG final: ☐ DA  ☒ NU — platforma are reflexii și glow complex.
- Generat cu FLUX.2 Klein 4B: ☐ DA  ☐ NU
- Integrat în APK: ☐ DA  ☐ NU
- Transparent/decupat curat: ☐ DA  ☐ NU
- Fără stretching: ☐ DA  ☐ NU
- Adaptiv pe R01–R11: ☐ DA  ☐ NU

### Prompt FLUX.2 Klein 4B

```text
Use case: stylized-concept
Asset type: isolated futuristic portal landing platform for a mobile game hero scene
Primary request: Create a wide, low, symmetrical sci-fi landing platform that sits beneath a dimensional portal. It must be a separate compositing asset with no character and no text.
Scene/backdrop: Isolated platform on a transparent-ready dark background.
Subject: Low-profile circular mechanical dais, concentric rings, dark gunmetal panels, cyan illumination on the left, magenta illumination on the right, subtle reflective floor surfaces, clean central landing zone.
Composition/framing: Wide landscape asset, centered and fully visible, generous transparent margins, no perspective distortion that prevents horizontal resizing.
Lighting/mood: Premium cinematic neon, restrained bloom, sharp edge highlights.
Color palette: Gunmetal, midnight navy, cyan, violet, hot magenta, small white reflections.
Constraints: Platform only. No character, no portal ring, no text, no UI, no watermark. Keep the silhouette clean for alpha compositing.
Avoid: spaceship, city, random objects, excessive smoke, unreadable details, fake letters, melted symmetry.
```

---

## H04 — Personajul / selected Brainball

**Fișier actual:** `brainball_kavvoro` și personaje din repository  
**Fișier final:** `brainball_kavvoro.webp` plus câte un WebP/PNG pentru fiecare personaj valid.

**Notă:** Pentru personaj folosim image-to-image cu referința existentă. Nu schimbăm identitatea vizuală fără aprobare. Dacă Home permite selectarea altui Brainball, fiecare variantă trebuie să respecte aceeași scară, poziție și iluminare.

### Decizii

- SVG final: ☐ DA  ☒ NU — personajul este ilustrație complexă.
- Generat cu FLUX.2 Klein 4B: ☐ DA  ☐ NU
- Folosită referință de identitate: ☐ DA  ☐ NU
- Canal alpha valid: ☐ DA  ☐ NU
- Integrat în APK: ☐ DA  ☐ NU
- Fără deformare: ☐ DA  ☐ NU
- Adaptiv pe R01–R11: ☐ DA  ☐ NU
- Verificate toate variantele Brainball: ☐ DA  ☐ NU

### Prompt FLUX.2 Klein 4B — variantă Kavvoro

```text
Use case: precise-object-edit
Asset type: premium stylized game character cutout for a mobile game home screen
Input images: Image 1 is the existing Kavvoro Brainball character reference; preserve its identity, face structure, split cyan/magenta body design, crown, eyes, grin, and recognizable silhouette.
Primary request: Upgrade the existing Kavvoro Brainball into a polished, production-quality 3D-stylized game character render while preserving the exact character identity and proportions. Improve materials, edge quality, lighting, facial clarity, and color separation without redesigning the character.
Subject: Round split-color Brainball mascot, cyan left side and magenta right side, crown, expressive eyes, playful but premium game personality.
Composition/framing: Centered square cutout, complete character visible with comfortable transparent padding on every side, character facing camera, no stretching, no crop.
Lighting/mood: Controlled cyan key light from the left, magenta rim light from the right, warm highlight on the crown, clean studio-quality rim separation.
Materials/textures: High-quality smooth stylized material, subtle surface detail, polished crown metal, crisp eyes and teeth.
Constraints: Keep the character identity, crown shape, split-color layout, facial expression, and overall silhouette. Transparent-ready background. No text, no logo, no UI, no extra limbs, no extra characters, no watermark.
Avoid: realistic human anatomy, horror, muddy colors, altered face, different mascot, duplicated eyes, melted teeth, distorted crown, stretched sphere.
```

### Criterii de acceptare

- Silueta este recognoscibilă față de referință.
- Personajul este decupat fără halo negru/magenta.
- `drawBitmap` păstrează proporția originală.
- Personajul are safe margin suficient pentru R01 și R02.
- Varianta selectată poate fi schimbată fără să schimbe layout-ul.

---

## H05 — Portal front FX / foreground overlay

**Fișier actual:** `home_portal_front_fx`  
**Fișier final:** `home_portal_front_fx.webp`  
**Tip final:** WebP/PNG cu transparență.

### Decizii

- SVG final: ☐ DA  ☒ NU — glow și particulele sunt raster/VFX.
- Generat cu FLUX.2 Klein 4B: ☐ DA  ☐ NU
- Integrat în APK: ☐ DA  ☐ NU
- Alpha valid: ☐ DA  ☐ NU
- Nu acoperă fața personajului: ☐ DA  ☐ NU
- Adaptiv pe R01–R11: ☐ DA  ☐ NU

### Prompt FLUX.2 Klein 4B

```text
Use case: stylized-concept
Asset type: isolated foreground energy FX overlay for a sci-fi portal
Primary request: Create a sparse foreground layer of cyan and magenta electrical arcs, small shards, and luminous particles that can be composited in front of a portal and character.
Composition/framing: Centered square overlay with the strongest effects near the outer ring, leaving the exact center mostly clear for the character face. Keep all particles inside safe margins.
Lighting/mood: Crisp energetic neon, premium VFX, controlled bloom, transparent-ready edges.
Color palette: Cyan, blue, violet, magenta, tiny white sparks.
Constraints: FX overlay only. No character, no portal body, no platform, no text, no UI, no watermark. Sparse enough not to hide the character or reduce readability.
Avoid: giant explosion, opaque smoke, full-screen white flare, random symbols, fake text, dirty halo.
```

---

## H06 — Brand header: BRAINROT CHAOS / KAVVORO

**Fișier actual:** `brand_header.png`  
**Fișier final propus:** mark SVG/VectorDrawable + text randat Android.

### Decizii

- SVG final: ☐ DA  ☒ NU pentru text generat de FLUX; ☐ DA pentru mark reconstruit manual.
- Generat cu FLUX.2 Klein 4B pentru final: ☐ DA  ☒ NU
- Generat cu FLUX ca referință de stil: ☐ DA  ☐ NU
- Integrat în APK: ☐ DA  ☐ NU
- Localizabil: ☐ DA  ☐ NU
- Fără crop pe R01–R11: ☐ DA  ☐ NU

### Prompt FLUX.2 Klein 4B — numai referință de mark

```text
Use case: logo-brand
Asset type: visual identity mark reference for a premium sci-fi mobile game brand
Primary request: Explore a distinctive abstract dimensional-rift emblem for the Kavvoro universe, suitable for reconstruction as a clean vector mark. The emblem should feel intentional, premium, geometric, and recognizable at small sizes.
Composition/framing: Single centered emblem on a clean dark background, strong silhouette, minimal internal detail, balanced cyan and magenta energy accents.
Style/medium: Refined sci-fi game branding, engineered geometry, restrained metallic and neon treatment.
Constraints: Do not render any words, letters, readable text, fake logo typography, watermark, or UI. The output is only a visual reference for a manually rebuilt vector mark.
Avoid: generic planet icon, random wings, excessive detail, stock gaming logo, AI-looking gradients, illegible pseudo-text.
```

### Implementare

- `BRAINROT CHAOS` și `KAVVORO` devin strings separate.
- Fontul, tracking-ul, gradientul și glow-ul se aplică nativ.
- Dacă este nevoie de un simbol, se reconstruiește manual în SVG sau Canvas.
- Se verifică lizibilitatea la R01, nu doar aspectul la R05.

---

## H07 — Buton Settings

**Fișier actual:** `settings_button.png`  
**Fișier final:** buton Canvas + `ic_settings.xml`.

### Decizii

- SVG final: ☐ DA  ☐ NU
- Generat cu FLUX.2 Klein 4B pentru final: ☐ DA  ☒ NU
- Integrat în APK: ☐ DA  ☐ NU
- Touch target minimum 48 dp: ☐ DA  ☐ NU
- Stări normal/pressed/disabled: ☐ DA  ☐ NU
- Adaptiv pe R01–R11: ☐ DA  ☐ NU

### Prompt FLUX.2 Klein 4B — referință, nu output final

```text
Use case: logo-brand
Asset type: icon style reference for a premium sci-fi settings button
Primary request: Create a minimal geometric settings gear icon reference with six robust teeth, a clear circular center, and a refined futuristic game UI appearance.
Composition/framing: Single centered white line icon on a plain dark background, high contrast, no surrounding button.
Style/medium: Clean vector-like line art, consistent stroke weight, professional premium mobile game UI.
Constraints: No text, no letters, no extra symbols, no fake UI screenshot, no watermark. This will be manually recreated as SVG/VectorDrawable.
```

### Implementare

- Gear-ul se reconstruiește ca vector, nu se trasează automat dintr-un PNG.
- Containerul, border-ul și state-urile rămân Canvas/shape drawable.
- Iconul trebuie să rămână nedeformat și centrat în buton.

---

## H08 — Buton Sound / mute

**Fișier actual:** `sound_button.png`  
**Fișier final:** buton Canvas + `ic_sound.xml` și variantă mute.

### Decizii

- SVG final: ☐ DA  ☐ NU
- Generat cu FLUX.2 Klein 4B pentru final: ☐ DA  ☒ NU
- Integrat în APK: ☐ DA  ☐ NU
- Variante sound/mute: ☐ DA  ☐ NU
- Touch target minimum 48 dp: ☐ DA  ☐ NU
- Adaptiv pe R01–R11: ☐ DA  ☐ NU

### Prompt FLUX.2 Klein 4B — referință, nu output final

```text
Use case: logo-brand
Asset type: icon style reference for a premium sci-fi sound toggle
Primary request: Create a minimal geometric speaker icon reference with clean sound waves, consistent line weight, and a futuristic premium mobile game UI style.
Composition/framing: Single centered white line icon on a plain dark background, no surrounding button.
Constraints: No text, no letters, no pseudo-interface, no watermark. The final icon will be rebuilt as SVG/VectorDrawable, including a separate mute slash state.
```

---

## H09 — Shared statistic card shell

**Fișiere actuale:** `best_streak.png`, `hype.png`, `level.png`, `daily_rift.png`  
**Fișier final:** componentă comună Canvas/shape drawable; nu patru PNG-uri.

### Decizii

- SVG final: ☐ DA  ☐ NU — recomandat Canvas/shape drawable pentru gradient și state-uri.
- Generat cu FLUX.2 Klein 4B pentru final: ☐ DA  ☒ NU
- Generat cu FLUX ca referință de material: ☐ DA  ☐ NU
- Integrat în APK: ☐ DA  ☐ NU
- Text separat și localizabil: ☐ DA  ☐ NU
- Valori dinamice: ☐ DA  ☐ NU
- Adaptiv pe R01–R11: ☐ DA  ☐ NU

### Prompt FLUX.2 Klein 4B — material de referință pentru card

```text
Use case: ui-mockup
Asset type: textless premium sci-fi statistics card material reference
Primary request: Design a single reusable dark futuristic stat-card surface with a thin engineered outline, subtle glass-metal depth, and a controlled cyan-to-magenta energy accent suitable for a mobile game HUD.
Composition/framing: One empty horizontal rounded card, no icon and no text, clear inner padding, consistent border thickness, neutral center area for dynamic content.
Lighting/mood: Premium restrained neon, readable at small size, dark navy interior.
Constraints: No text, no numbers, no letters, no icons, no logo, no watermark. The final component will be rebuilt with Canvas/shape drawables so it can scale and localize correctly.
Avoid: busy background, fake UI copy, excessive bevel, rasterized text, random symbols.
```

### Implementare comună

- Titlul, valoarea și accentul sunt date de `StatSpec`.
- Cardul are constrângeri min/max și nu se micșorează sub lizibilitate.
- Pentru telefoane compacte se folosește layout `2×2` dacă un rând nu mai oferă lățime utilă.
- Progress bar-ul nu se exportă ca imagine.

---

## H10 — Stat card: Best Streak

**Fișier actual:** `best_streak.png`  
**Fișier final:** card comun + `ic_best_streak.xml` + strings/value dinamic.

### Decizii

- SVG final pentru icon: ☐ DA  ☐ NU
- Generat cu FLUX.2 Klein 4B pentru icon final: ☐ DA  ☒ NU
- Integrat în APK: ☐ DA  ☐ NU
- Localizabil (`BEST STREAK`): ☐ DA  ☐ NU
- Valoarea `xN` dinamică: ☐ DA  ☐ NU
- Adaptiv pe R01–R11: ☐ DA  ☐ NU

### Prompt FLUX.2 Klein 4B — referință icon

```text
Use case: logo-brand
Asset type: vector icon style reference for a best-streak statistic
Primary request: Create a clean upward trend arrow combined with a sharp energetic pulse, expressing progress and a winning streak in a premium sci-fi mobile game UI.
Composition/framing: Single centered cyan line icon, bold enough to remain readable at small size, no card and no text.
Style/medium: Geometric vector-like icon, engineered corners, consistent stroke weight.
Constraints: No text, no numbers, no letters, no watermark. Final icon will be manually rebuilt as SVG/VectorDrawable.
```

---

## H11 — Stat card: Hype

**Fișier actual:** `hype.png`  
**Fișier final:** card comun + `ic_hype.xml` + strings/value dinamic.

### Decizii

- SVG final pentru icon: ☐ DA  ☐ NU
- Generat cu FLUX.2 Klein 4B pentru icon final: ☐ DA  ☒ NU
- Integrat în APK: ☐ DA  ☐ NU
- Localizabil (`HYPE`): ☐ DA  ☐ NU
- Valoarea dinamică: ☐ DA  ☐ NU
- Adaptiv pe R01–R11: ☐ DA  ☐ NU

### Prompt FLUX.2 Klein 4B — referință icon

```text
Use case: logo-brand
Asset type: vector icon style reference for a hype statistic
Primary request: Create a bold angular energy-bolt icon representing excitement, momentum, and hype in a premium sci-fi mobile game UI.
Composition/framing: Single centered hot-magenta icon, simple silhouette, readable at small size, no card and no text.
Style/medium: Clean vector-like geometric symbol, strong negative space, consistent visual weight with a trend-arrow icon.
Constraints: No text, no numbers, no letters, no watermark. Final icon will be manually rebuilt as SVG/VectorDrawable.
```

---

## H12 — Stat card: Level + progress

**Fișier actual:** `level.png`  
**Fișier final:** card comun + `ic_level.xml` + Canvas progress bar.

### Decizii

- SVG final pentru icon: ☐ DA  ☐ NU
- Generat cu FLUX.2 Klein 4B pentru icon final: ☐ DA  ☒ NU
- Integrat în APK: ☐ DA  ☐ NU
- Progress bar nativ și dinamic: ☐ DA  ☐ NU
- Localizabil (`LEVEL`): ☐ DA  ☐ NU
- Adaptiv pe R01–R11: ☐ DA  ☐ NU

### Prompt FLUX.2 Klein 4B — referință icon

```text
Use case: logo-brand
Asset type: vector icon style reference for a player level statistic
Primary request: Create a precise sci-fi targeting reticle icon expressing level, progression, and focus.
Composition/framing: Single centered cyan line icon, circular reticle with a clean center point and four directional marks, no card and no text.
Style/medium: Refined geometric vector-like icon with consistent stroke weight.
Constraints: No text, no numbers, no letters, no watermark. Final icon will be manually rebuilt as SVG/VectorDrawable.
```

### Implementare

- Progress background, fill, radius și glow sunt desenate nativ.
- Progresul este limitat între `0f` și `1f`.
- Valoarea nu poate produce overflow sau text suprapus.

---

## H13 — Stat card: Daily Rift

**Fișier actual:** `daily_rift.png`  
**Fișier final:** card comun + `ic_daily_rift.xml` + status dinamic.

### Decizii

- SVG final pentru icon: ☐ DA  ☐ NU
- Generat cu FLUX.2 Klein 4B pentru icon final: ☐ DA  ☒ NU
- Integrat în APK: ☐ DA  ☐ NU
- Statusuri READY/CLAIMED localizabile: ☐ DA  ☐ NU
- Adaptiv pe R01–R11: ☐ DA  ☐ NU

### Prompt FLUX.2 Klein 4B — referință icon

```text
Use case: logo-brand
Asset type: vector icon style reference for a daily dimensional rift status
Primary request: Create a clean concentric portal-ring icon representing a daily rift availability state in a premium sci-fi mobile game UI.
Composition/framing: Single centered cyan line icon, two or three precise concentric rings, no card and no text.
Style/medium: Minimal geometric vector-like icon, readable at small size, consistent with a reticle icon.
Constraints: No text, no letters, no numbers, no watermark. Final icon will be manually rebuilt as SVG/VectorDrawable.
```

---

## H14 — Rift status pill

**Fișier actual:** `rift_online_status.png`  
**Fișier final:** Canvas/shape drawable + indicator + strings dinamice.

### Decizii

- SVG final: ☐ DA  ☐ NU — recomandat Canvas pentru stări și animație.
- Generat cu FLUX.2 Klein 4B pentru final: ☐ DA  ☒ NU
- Integrat în APK: ☐ DA  ☐ NU
- Text separat: ☐ DA  ☐ NU
- Status dot animabil: ☐ DA  ☐ NU
- Adaptiv pe R01–R11: ☐ DA  ☐ NU

### Prompt FLUX.2 Klein 4B — material de referință

```text
Use case: ui-mockup
Asset type: textless sci-fi status capsule material reference
Primary request: Create a premium compact rounded status-pill surface for a mobile game home screen, with a dark glass interior, thin cyan outline, and one small cyan indicator point.
Composition/framing: Empty horizontal capsule, generous inner padding, no text and no logo.
Lighting/mood: Calm active-system indicator, subtle cyan bloom, high contrast against a dark navy background.
Constraints: No text, no letters, no numbers, no UI screenshot, no watermark. The final capsule and indicator will be recreated natively.
```

---

## H15 — Butonul principal Play

**Fișier actual:** `play_cta.png` și desenare existentă în `HomeView.kt`  
**Fișier final:** componentă Canvas/SVG/9-patch fără text imprimat.

### Decizii

- SVG final pentru chassis: ☐ DA  ☐ NU
- Generat cu FLUX.2 Klein 4B pentru final: ☐ DA  ☒ NU
- Generat cu FLUX ca referință de design: ☐ DA  ☐ NU
- Text `PLAY`/subtitle separat: ☐ DA  ☐ NU
- Integrat în APK: ☐ DA  ☐ NU
- Stări normal/pressed/disabled: ☐ DA  ☐ NU
- Touch target minimum 48 dp: ☐ DA  ☐ NU
- Adaptiv pe R01–R11: ☐ DA  ☐ NU

### Prompt FLUX.2 Klein 4B — chassis fără text

```text
Use case: ui-mockup
Asset type: premium textless sci-fi primary action button reference
Primary request: Design a distinctive wide futuristic game CTA chassis for the Kavvoro home screen. It must communicate premium engineered technology and dimensional energy without relying on text.
Composition/framing: Wide horizontal button, symmetrical dark gunmetal chassis with controlled chamfered corners, cyan energy rail on the left half and hot-magenta energy rail on the right half, empty center area for native text, circular action pod on the right with an empty center for a chevron.
Lighting/mood: Strong but controlled neon edge lighting, readable dark interior, crisp metal bevels.
Materials/textures: Gunmetal, carbon-fiber hints, restrained honeycomb detail only inside the left panel, glass-like neon rails.
Constraints: No words, no letters, no numbers, no fake arrow glyph, no logo, no watermark. The final text and chevron will be rendered natively. Preserve a clean scalable silhouette.
Avoid: giant unreadable lettering, random symbols, overly complex micro-detail, rasterized text, excessive bloom that hides the border.
```

### Implementare

- `PLAY` și `CHOOSE YOUR MODE` sunt strings separate.
- Chevon-ul este `ic_chevron.xml` sau Canvas path.
- Culoarea și intensitatea rail-urilor se schimbă în pressed state.
- Chassis-ul trebuie să poată fi redimensionat pe lățime fără deformarea colțurilor; se preferă vector/Canvas/9-patch.

---

## H16 — Card Leaderboards

**Fișier actual:** `leaderboards.png`  
**Fișier final:** card comun + `ic_leaderboards.xml` + text/chevron nativ.

### Decizii

- SVG final pentru card: ☐ DA  ☐ NU — recomandat componentă Canvas.
- SVG final pentru icon: ☐ DA  ☐ NU
- Generat cu FLUX.2 Klein 4B pentru final: ☐ DA  ☒ NU
- Text separat/localizabil: ☐ DA  ☐ NU
- Integrat în APK: ☐ DA  ☐ NU
- Adaptiv pe R01–R11: ☐ DA  ☐ NU

### Prompt FLUX.2 Klein 4B — icon podium

```text
Use case: logo-brand
Asset type: vector icon style reference for a sci-fi leaderboard
Primary request: Create a clean three-column podium icon with an engineered futuristic treatment, expressing ranking and competition.
Composition/framing: Single centered cyan and magenta line icon, strong silhouette, no card and no text.
Style/medium: Minimal vector-like geometry, consistent stroke weight, readable at small size.
Constraints: No words, no numbers, no letters, no watermark. Final icon will be rebuilt as SVG/VectorDrawable.
```

### Implementare

- Cardul folosește aceeași bază de material ca celelalte carduri.
- Iconul, titlul și chevron-ul sunt independente.
- Pe tabletă poate ocupa rând complet; pe phone rămâne card vertical.

---

## H17 — Card Vault Maxed

**Fișier actual:** `vault_maxed.png`  
**Fișier final:** card comun + `ic_vault.xml` + text/chevron nativ.

### Decizii

- SVG final pentru card: ☐ DA  ☐ NU — recomandat componentă Canvas.
- SVG final pentru icon: ☐ DA  ☐ NU
- Generat cu FLUX.2 Klein 4B pentru final: ☐ DA  ☒ NU
- Text separat/localizabil: ☐ DA  ☐ NU
- Integrat în APK: ☐ DA  ☐ NU
- Stări maxed/not maxed: ☐ DA  ☐ NU
- Adaptiv pe R01–R11: ☐ DA  ☐ NU

### Prompt FLUX.2 Klein 4B — icon vault

```text
Use case: logo-brand
Asset type: vector icon style reference for a futuristic reward vault
Primary request: Create a clean compact vault/safe icon with a circular lock mechanism and engineered sci-fi panel lines, expressing collected rewards and completion.
Composition/framing: Single centered cyan line icon with a small magenta accent, no card and no text.
Style/medium: Premium vector-like geometry, clear outline, readable at small size.
Constraints: No words, no letters, no numbers, no watermark. Final icon will be rebuilt as SVG/VectorDrawable.
```

### Implementare

- `VAULT MAXED`, `ALL FREE REWARDS UNLOCKED` și eventual statusurile devin strings.
- Textul are fallback pentru limbi mai lungi.
- Iconul nu include în interior cuvinte sau simboluri ilizibile.

---

## H18 — Card Collection & Unlocks

**Fișier actual:** `collection_unlocks.png`  
**Fișier final:** componentă Canvas + `ic_collection.xml` + `ic_next_unlock.xml` + text dinamic.

### Decizii

- SVG final pentru card: ☐ DA  ☐ NU — recomandat Canvas.
- SVG final pentru ring: ☐ DA  ☐ NU
- SVG final pentru cube: ☐ DA  ☐ NU
- Generat cu FLUX.2 Klein 4B pentru final: ☐ DA  ☒ NU
- Text separat/localizabil: ☐ DA  ☐ NU
- Progres dinamic: ☐ DA  ☐ NU
- Adaptiv pe R01–R11: ☐ DA  ☐ NU

### Prompt FLUX.2 Klein 4B — ring progress

```text
Use case: logo-brand
Asset type: vector icon style reference for a collection completion ring
Primary request: Create a minimal circular progress-ring icon with a clean cyan arc and dark inner space, suitable for a premium sci-fi collection UI.
Composition/framing: Single centered ring, uniform stroke, no card and no text.
Style/medium: Precise vector-like geometry, readable at small size.
Constraints: No words, no numbers, no letters, no watermark. Final ring will be drawn natively so the progress value remains dynamic.
```

### Prompt FLUX.2 Klein 4B — next unlock cube

```text
Use case: logo-brand
Asset type: vector icon style reference for a next-unlock item
Primary request: Create a minimal isometric sci-fi cube icon with a clean magenta outline, expressing a collectible reward or next unlock.
Composition/framing: Single centered cube, simple three-face geometry, no card and no text.
Style/medium: Premium vector-like line art, consistent stroke weight, readable at small size.
Constraints: No words, no letters, no numbers, no watermark. Final cube will be rebuilt as SVG/VectorDrawable.
```

### Implementare

- Ring-ul trebuie desenat cu arc dinamic, nu importat ca imagine cu `36 / 50` imprimat.
- Divider-ul este Canvas/shape drawable.
- Cube-ul și chevron-ul sunt vectori independenți.
- Pe telefoane înguste, zona de next unlock poate trece sub zona de collection sau poate folosi un layout compact fără overflow.

---

## H19 — Chevron / săgeată de navigație

**Fișier actual:** desenare Canvas în `HomeView.kt`  
**Fișier final:** `ic_chevron.xml` sau Canvas path.

### Decizii

- SVG final: ☐ DA  ☐ NU
- Generat cu FLUX.2 Klein 4B pentru final: ☐ DA  ☒ NU
- Integrat în APK: ☐ DA  ☐ NU
- Stări normal/pressed: ☐ DA  ☐ NU
- Touch target separat de icon: ☐ DA  ☐ NU
- Adaptiv pe R01–R11: ☐ DA  ☐ NU

### Prompt FLUX.2 Klein 4B — referință, nu output final

```text
Use case: logo-brand
Asset type: vector icon style reference for a navigation chevron
Primary request: Create a simple sharp right-facing chevron with a premium sci-fi line treatment, clear at very small sizes.
Composition/framing: Single centered white line chevron on a plain dark background.
Style/medium: Minimal geometric vector-like icon, consistent stroke weight, no glow that destroys readability.
Constraints: No text, no letters, no extra symbols, no watermark. Final icon will be rebuilt as SVG/VectorDrawable.
```

---

## H20 — Footer note

**Fișier actual:** `footer_note.png`  
**Fișier final:** text Android, fără bitmap.

### Decizii

- SVG final: ☐ NU
- Generat cu FLUX.2 Klein 4B: ☐ NU
- Integrat în APK ca text: ☐ DA  ☐ NU
- Localizat în `strings.xml`: ☐ DA  ☐ NU
- Wrapping verificat pe R01–R11: ☐ DA  ☐ NU

### Implementare

- Textul devine string localizabil.
- Se folosește fontul și contrastul din design system.
- Pe ecrane scurte footer-ul poate fi mutat după conținut într-un scroll, nu tăiat.

---

## H21 — Decorative glows, hex grid și particles

**Surse actuale:** desenare în `HomeView.kt`, plus detalii incluse în PNG-uri.  
**Fișier final:** Canvas/shader pentru efecte simple; WebP/PNG numai pentru particule complexe.

### Decizii

- SVG final: ☐ DA  ☐ NU
- Generat cu FLUX.2 Klein 4B pentru final: ☐ DA  ☐ NU
- Implementat procedural unde este posibil: ☐ DA  ☐ NU
- Performanță verificată: ☐ DA  ☐ NU
- Adaptiv pe R01–R11: ☐ DA  ☐ NU

### Prompt FLUX.2 Klein 4B — particule de referință

```text
Use case: stylized-concept
Asset type: sparse sci-fi particle and energy accent reference
Primary request: Create a restrained set of cyan, violet, and magenta dimensional particles, tiny shards, and soft energy wisps for a premium mobile game background.
Composition/framing: Sparse isolated accents with clear separation between particles, no complete scene, no character, no portal, no UI.
Lighting/mood: Crisp but subtle neon, controlled bloom, transparent-ready look.
Constraints: No text, no letters, no symbols, no watermark. Keep the particles individually separable or easy to mask.
Avoid: confetti, excessive noise, giant flares, random UI marks, muddy smoke.
```

### Implementare

- Hex grid-ul se desenează nativ dacă trebuie să rămână repetabil și responsive.
- Glow-ul nu trebuie să consume nejustificat GPU pe telefoane compacte.
- Efectele nu trebuie să reducă lizibilitatea textului sau să acopere personajul.

## 7. Design system care trebuie înghețat înainte de integrare

Înainte de a produce toate variantele, se definește o singură fișă de design:

- background: near-black/navy;
- cyan principal;
- magenta principal;
- cyan/magenta pentru pressed state;
- alb principal și alb secundar;
- opacity pentru card normal, hover/pressed și disabled;
- grosime border;
- rază card și rază pill;
- glow radius și intensitate;
- font family;
- titlu, valoare, subtitle, footer și letter spacing;
- padding minim și spacing vertical;
- dimensiuni minime pentru touch targets.

Fără aceste valori, fiecare asset va arăta bine separat, dar pachetul va părea inconsistent.

## 8. Pașii de implementare în proiect

### Pasul 1 — Audit și separare

- [ ] Confirmăm lista de asset-uri de producție.
- [ ] Mutăm mental `home_reference_flattened.png` și `hero_reference.png` în categoria reference-only.
- [ ] Identificăm toate resursele consumate de `HomeView.kt`.
- [ ] Marcăm orice PNG cu text ca temporar.

### Pasul 2 — Reconstituire UI nativ

- [ ] Creăm card shell comun.
- [ ] Creăm butoanele Settings și Sound ca vector + container nativ.
- [ ] Creăm iconurile individuale.
- [ ] Creăm CTA Play fără text bitmap.
- [ ] Creăm pill-ul de status fără text bitmap.
- [ ] Creăm cardurile inferioare fără text bitmap.
- [ ] Externalizăm toate textele în `strings.xml`.

### Pasul 3 — Generare artă FLUX

- [ ] Generăm background-ul.
- [ ] Generăm portalul back.
- [ ] Generăm platforma.
- [ ] Retușăm/generăm personajul cu image-to-image și referință.
- [ ] Generăm portal front FX.
- [ ] Verificăm alpha, aspect ratio și safe margins.
- [ ] Facem upscale numai pentru variantele aprobate.

### Pasul 4 — Integrare în Android

- [ ] Exportăm raster final în `drawable-nodpi`.
- [ ] Exportăm iconurile în `drawable` ca VectorDrawable.
- [ ] Înlocuim încărcările vechi cu noile resurse.
- [ ] Păstrăm `HomeView.kt` responsabil de randare, nu de texte aplatizate.
- [ ] Verificăm că `HomeLayoutCalculator.kt` folosește aspect ratio și bounds reale.
- [ ] Adăugăm safe insets și zone de touch.

### Pasul 5 — Verificare vizuală și funcțională

- [ ] Construim APK debug.
- [ ] Capturăm Home la R01–R11.
- [ ] Verificăm text, crop, stretching, overlap și touch targets.
- [ ] Verificăm cel puțin limba engleză și română.
- [ ] Verificăm valori lungi și statusuri alternative.
- [ ] Corectăm asset-ul sau layout-ul, nu micșorăm tot ecranul până devine ilizibil.

### Pasul 6 — Cleanup

- [ ] Eliminăm referințele la PNG-urile cu text din cod.
- [ ] Eliminăm asset-urile vechi numai după ce testele trec.
- [ ] Verificăm că nu există duplicate full-resolution în `app/src/main/res`.
- [ ] Păstrăm doar asset-ul final consumat de APK și prompturile/manifestul de producție necesare.

## 9. Definition of Done pentru fiecare asset

Un asset nu este `DONE` dacă are doar imagine generată. Trebuie să treacă toate condițiile:

- [ ] formatul final este justificat: SVG/vector, Canvas/9-patch sau WebP/PNG;
- [ ] textul nu este imprimat în imagine, dacă trebuie tradus sau schimbat dinamic;
- [ ] referința FLUX și seed-ul sunt salvate;
- [ ] asset-ul nu are watermark, artefacte sau halo nedorit;
- [ ] aspect ratio este păstrat;
- [ ] safe area și crop behavior sunt definite;
- [ ] asset-ul este integrat în APK;
- [ ] este verificat pe R01–R11;
- [ ] este verificat în engleză și română, dacă are text asociat;
- [ ] stările interactive sunt verificate, dacă elementul este clickable;
- [ ] vechiul asset nu mai este folosit în cod;
- [ ] APK-ul pornește și Home nu are erori de randare.

## 10. Ordinea optimă de producție

1. [ ] Înghețăm paleta, fontul, glow-urile și materialele.
2. [ ] Reconstituim card shell, CTA Play și pill-ul de status fără text.
3. [ ] Reconstituim toate iconurile ca vectori.
4. [ ] Generăm background-ul FLUX.
5. [ ] Generăm portalul și platforma.
6. [ ] Retușăm personajul principal și variantele Brainball.
7. [ ] Generăm front FX și particles.
8. [ ] Separăm și localizăm toate textele.
9. [ ] Integrăm în `HomeView.kt` și `HomeLayoutCalculator.kt`.
10. [ ] Testăm pe R01–R11.
11. [ ] Eliminăm PNG-urile aplatizate care nu mai sunt referențiate.

## 11. Criteriul vizual final

Home-ul final trebuie să pară construit ca o interfață de joc premium, nu ca un screenshot împărțit în bucăți:

- ilustrația are profunzime și identitate Kavvoro;
- cardurile au aceeași familie de materiale și glow;
- iconurile au aceeași grosime și geometrie;
- textul este clar și poate fi tradus;
- CTA-ul Play rămâne elementul dominant;
- personajul rămâne focal point-ul;
- niciun asset nu se deformează pe ecrane înguste sau înalte;
- interfața este aceeași ca direcție artistică pe telefon și tabletă, cu layout adaptat, nu cu imagini întinse.
