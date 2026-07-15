# Play Console Compliance Notes

This is a preparation checklist, not legal advice.

## Required Before Publishing

- Public privacy policy URL.
- Play Console Data Safety form completed.
- Ads declaration completed.
- Target audience and content declaration completed.
- In-app purchases declaration completed if premium Brainballs remain enabled.
- UMP/ad consent message configured in AdMob Privacy & messaging.

## Data Disclosures To Review

The app currently integrates:

- Google Mobile Ads SDK / AdMob.
- Google User Messaging Platform.
- Firebase Analytics.
- Google Play Games Services.
- Google Play Billing.
- Local age group storage for ad treatment.
- Local gameplay progress, HYPE, unlocks, settings, and purchases entitlement cache.
- Share-video export through Android share sheet.

## Current App Behavior

- The app asks for age in years and stores only an age group.
- Ads are initialized only after the age gate and UMP state allow ad requests.
- Child and teen profiles use age-restricted ad treatment.
- Privacy options button opens the UMP privacy form only when required.
- Purchases can be restored from Collection.

## Store Listing Copy Draft

Short description:
Hold, release, bounce, and survive chaotic Brainballs in fast one-thumb rift levels.

Full description starter:
Brainrot Chaos: Kavvoro is a fast one-thumb physics game where every hold creates a Rift tether. Pull the Brainball, release to coast, chain clean moves, dodge crash nodes, and unlock louder, stranger Brainballs with superpowers.
