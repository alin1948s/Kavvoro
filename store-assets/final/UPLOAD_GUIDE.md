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

Do not upload tablet screenshots yet. A real 1920 x 1080 Android tablet emulator check showed that the current phone-first UI remains functional but is too small on a low-density tablet. Reusing enlarged phone screenshots would misrepresent the tablet experience.

If Play Console allows a phone-only listing, leave the 7-inch and 10-inch sections empty. If it marks either section as required, tablet UI adaptation is a release blocker and should be completed before adding those screenshots.

## Final checks before publishing

- Confirm that every screenshot matches the final release APK.
- Upload the signed AAB, not the debug APK used for captures.
- Keep the feature graphic text-free for localization.
- Verify that the YouTube trailer plays without an age gate.
- Confirm the privacy policy and data-deletion URLs remain publicly accessible.
- Do not claim tablet or PC optimization until those targets pass device testing.
