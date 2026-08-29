# Google Play upload guide

## Default store listing

- Default language: English (United States)
- App name: `Brainrot Chaos: Kavvoro`
- Short description: `Tap the Rift in a fast physics arcade with 50 Brainballs and endless chaos.`
- Full description: use `STORE_LISTING_EN.md`
- Romanian localization: use `STORE_LISTING_RO.md`

## Main graphics

- App icon: `graphics/kavvoro-play-icon-512.png`
  - 512 x 512 PNG
- Feature graphic: `graphics/kavvoro-feature-1024x500.jpg`
  - 1024 x 500 JPEG
- Key-art master: `graphics/kavvoro-key-art-master.png`
  - Source only; do not upload in place of a required size

## Phone screenshots

Upload these in this order:

1. `phone/01-meet-kavvoro.png`
2. `phone/02-tap-the-rift.png`
3. `phone/03-choose-your-chaos.png`
4. `phone/04-collect-50.png`
5. `phone/06-trigger-powers.png`
6. `phone/07-bend-space.png`
7. `phone/05-advanced-chaos.png`

All seven files are 1242 x 2208 PNG, exact 9:16, and under 8 MB. The app screens come from the S26 Ultra emulator running the current APK. The headline area is promotional framing; the gameplay itself was not reconstructed.

## Play Store video

Upload this file to YouTube:

- `video/kavvoro-play-trailer-youtube-1920x1080.mp4`
- Resolution: 1920 x 1080
- Duration: about 25 seconds
- Audio: original in-game Chaos music

YouTube settings:

- Visibility: Public or Unlisted
- Embedding: enabled
- Age restriction: off
- Paid promotion: off, unless a real sponsorship is added later
- Monetization/ads: off for this trailer
- Audience: answer truthfully according to the final target-audience decision in Play Console

After upload, paste the normal YouTube watch URL into Play Console. Do not paste a playlist or channel URL.

The social-first version is:

- `video/kavvoro-play-trailer-vertical-1080x1920.mp4`

Use it for YouTube Shorts, TikTok and Instagram Reels.

## YouTube thumbnail

- `video/kavvoro-youtube-thumbnail-1280x720.jpg`
- 1280 x 720 JPEG

## Play Games on PC

Only use this section if Play Games on PC is enabled for this release.

- Transparent game logo: `pc/kavvoro-pc-logo-600x400.png`
- Text-free cover: `pc/kavvoro-pc-cover-1920x1080.jpg`
- Screenshots: upload the four files from `pc/screenshots/` in numeric order
- Video: reuse `video/kavvoro-play-trailer-youtube-1920x1080.mp4`

## Tablet status

- 7-inch portrait set: upload the seven files from `tablet-7/` in numeric order.
  - All seven files are 1200 x 1920 PNG.
- 10-inch portrait set: upload the seven files from `tablet-10/` in numeric order.
  - All seven files are 1600 x 2560 PNG.

These are dedicated large-screen captures from the current tablet layout review;
they are not enlarged phone screenshots. Recheck the final signed release APK
visually before uploading if the release build changes after these captures.

Upload status: all seven images from each tablet set were added to the default
Google Play listing and saved as a draft on 2026-08-22. Publishing that draft
remains a separate Play Console action.

## Final checks before publishing

- Confirm that every screenshot matches the final release APK.
- Upload the signed AAB, not the debug APK used for captures.
- Keep the feature graphic text-free for localization.
- Verify that the YouTube trailer plays without an age gate.
- Confirm the privacy policy and data-deletion URLs remain publicly accessible.
- Do not claim tablet or PC optimization beyond the targets that have passed the
  corresponding device/emulator review.
