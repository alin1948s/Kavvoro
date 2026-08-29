# Kavvoro — current code and visual assets

This package is a curated snapshot of the current Kavvoro Android project for a designer or 3D artist.

## Included

- `code/` — Kotlin source, tests, Android manifest and Gradle project files.
- `assets/art-source/` — current art atlases, world maps, brainball sheets, UI icons and store/game-detail artwork.
- `assets/android-visual/drawable-nodpi/` — visual assets currently used by the Android app: backgrounds, brainballs, boosts, hazards, logos and UI icons.
- `assets/figma-editable/editable-svg/` — editable SVG exports for the current screens, plus the combined reference export.
- `assets/screenshots-android/` — current Android screen references for launch, age gate, menus, tutorials, modes, results and settings.
- `assets/store-graphics/` — current store key art and transparent character exports.

## Important for the 3D artist

The current visual direction is already represented by the files in `assets/art-source/` and `assets/android-visual/drawable-nodpi/`. The new deliverables should be supplied as separate background and transparent foreground assets, not as flattened screenshots.

The current phone reference is `1080 × 2400 px` (9:20). Keep important visual details away from the top and bottom system-bar areas and leave the center readable for the game UI.

For each new 3D object, please provide:

- transparent PNG render, preferably at 2× resolution;
- source file (`.blend`, `.fbx` or `.glb`) and textures;
- separate glow, shadow or particle passes when possible;
- a preview showing the intended placement on the target screen.

Figma can place the transparent renders and keep the UI layers editable. The 3D geometry itself remains editable in Blender/FBX/GLB, not inside Figma.

## Not included in this curated package

Localized audio and generated build/cache directories are intentionally omitted from the design handoff. The complete working repository is one level above this folder and contains the full Android resources and audio catalogue.
