package com.moonsolstudios.kavvoro.i18n

import com.moonsolstudios.kavvoro.i18n.catalog.ArTranslations
import com.moonsolstudios.kavvoro.i18n.catalog.CsTranslations
import com.moonsolstudios.kavvoro.i18n.catalog.DeTranslations
import com.moonsolstudios.kavvoro.i18n.catalog.EnTranslations
import com.moonsolstudios.kavvoro.i18n.catalog.EsTranslations
import com.moonsolstudios.kavvoro.i18n.catalog.FiTranslations
import com.moonsolstudios.kavvoro.i18n.catalog.FrTranslations
import com.moonsolstudios.kavvoro.i18n.catalog.HiTranslations
import com.moonsolstudios.kavvoro.i18n.catalog.IdTranslations
import com.moonsolstudios.kavvoro.i18n.catalog.ItTranslations
import com.moonsolstudios.kavvoro.i18n.catalog.JaTranslations
import com.moonsolstudios.kavvoro.i18n.catalog.KoTranslations
import com.moonsolstudios.kavvoro.i18n.catalog.NlTranslations
import com.moonsolstudios.kavvoro.i18n.catalog.PlTranslations
import com.moonsolstudios.kavvoro.i18n.catalog.PtTranslations
import com.moonsolstudios.kavvoro.i18n.catalog.RoTranslations
import com.moonsolstudios.kavvoro.i18n.catalog.RuTranslations
import com.moonsolstudios.kavvoro.i18n.catalog.SvTranslations
import com.moonsolstudios.kavvoro.i18n.catalog.ThTranslations
import com.moonsolstudios.kavvoro.i18n.catalog.TrTranslations
import com.moonsolstudios.kavvoro.i18n.catalog.UkTranslations
import com.moonsolstudios.kavvoro.i18n.catalog.ViTranslations
import com.moonsolstudios.kavvoro.i18n.catalog.ZhTranslations
import com.moonsolstudios.kavvoro.i18n.catalog.ZhTwTranslations

object LocalizationCatalog {
    val supportedLanguages: Set<KavvoroLanguage> =
        KavvoroLanguage.entries.filterNot { it == KavvoroLanguage.SYSTEM }.toSet()

    val allowlistedEnglishValues: Set<String> = setOf(
        "Kavvoro", "Brainball", "Rift", "RIFT", "HYPE", "Classic", "CLASSIC", "Chaos", "CHAOS",
        "Google Play", "Firebase", "AdMob", "AURA", "Aura", "MAX AURA", "TUTORIAL", "Tutorial",
        "TRAINING", "Training", "PORTAL BRAINROT", "PORTAL RIFT", "PORTAL", "Portal", "PORTAL IN",
        "PORTAL OUT", "IN", "OUT", "BOOST", "Boost", "CODE", "Code", "START", "Start", "RESET",
        "Reset", "CHECKPOINT", "Checkpoint", "FILTER", "Filter", "FOCUS", "Focus", "STORM", "Storm",
        "CHARGE", "Charge", "COLLECTION", "Collection", "COSMETIC", "Cosmetic", "PRIVACY", "Privacy",
        "START LEVEL", "START LEVEL 01", "LEVEL", "Level", "RANK", "Rank", "SYSTEM", "System",
        "PREMIUM", "Premium", "CRASH", "Crash", "ONLINE", "Online", "User",
        // Home navigation vocabulary: gameplay loanwords that are the standard term in
        // these languages (French/German/Portuguese/Dutch "Skins", French "Missions").
        "Skins", "Missions", "TOP", "DIVISION 1", "LEVELS", "Synthwave soundtrack"
    )

    internal val renderedSourceKeys: Set<String> = sortedSetOf(
        "10% PULL AND 15% RECHARGE BOOST",
        "ACTIVE",
        "AD",
        "AD CHECK",
        "AD CONTINUE",
        "AD PRIVACY CHOICES",
        "AGE",
        "AGE CHECK",
        "AIM FOR THE EXIT PORTAL.",
        "ALL",
        "ALL FREE REWARDS UNLOCKED",
        "ALREADY OWNED / RESTORING",
        "AT",
        "AURA",
        "AWAKENED",
        "BACK",
        "BEST",
        "BEST STREAK",
        "BLOCKS THE FIRST HAZARD HIT",
        "BONUS CLAIMED TODAY",
        "BUILD POWER / RELEASE TO GLIDE",
        "BUILDING SHORT",
        "Beat my Kavvoro rift",
        "Blop survived on pure vibes.",
        "Bounce angles matter",
        "Brainball rebooting. Try a shorter hold.",
        "Brainball rebooting. Try cleaner taps.",
        "Byte uploaded the win.",
        "CHAIN FAST MOVES FOR MORE HYPE.",
        "CHAIN SPIKE",
        "CHAOS",
        "CHAOS CONTROL",
        "CHARGE",
        "CHASE CLEAN RUNS",
        "CHECK THE AGE",
        "CHECKING GOOGLE PLAY PURCHASES",
        "CHECKPOINT",
        "CHILD  /  TEEN  /  ADULT",
        "CHOOSE LANGUAGE",
        "CHOOSE MODE",
        "CLAIMED",
        "CLASSIC",
        "CLEAN RIFT SNAP",
        "CLOSE",
        "CODE",
        "COLLECTION",
        "CONNECTING TO GOOGLE PLAY",
        "CONTINUE",
        "CONTINUE FREE",
        "CONTINUE WITH AD",
        "CONTROL LAB",
        "COSMETIC",
        "COSMETIC LOADOUT",
        "CRASH REPLAY",
        "CROSS CURRENT",
        "CROSSING SIGNAL",
        "CURRENT",
        "Can you beat my Kavvoro rift?",
        "Charge, release, then coast into the exit.",
        "Chrome bounce paid rent today.",
        "Clean control and smooth release",
        "Crown behavior, no debate.",
        "DAILY RIFT",
        "DAILY RIFT BONUS",
        "DODGE CRASH NODES. THEY END THE RUN.",
        "DON'T HOLD FOREVER. ENERGY IS LIMITED.",
        "EARNED SUPERPOWER",
        "ENERGY TAX",
        "ENTER IN / EXIT OUT WITH EXTRA SPEED",
        "ENTER YOUR AGE",
        "EQUIP",
        "EQUIPPED",
        "EQUIPPED BRAINBALL",
        "Enter your age in years.",
        "FAILED",
        "FILTER",
        "FIRST CLEAR BONUS",
        "FIRST RUN",
        "FORBIDDEN",
        "Fast reactions, no sleepy holds",
        "Fields bend speed and direction",
        "Focus Field slows the ball while holding.",
        "Free recovery available. Streak %s stays active.",
        "GAME TEXT + COLLECTION VOICE",
        "GATE STACK",
        "GET",
        "GLITCHED",
        "GLOBAL RANKS",
        "GLOBAL SYNC OFFLINE",
        "GOOFY CLASS",
        "GOOGLE PLAY / NO POWERS",
        "GOOGLE PLAY BILLING UNAVAILABLE",
        "GOOGLE PLAY ERROR",
        "GRAVITY PULLS HARDER",
        "GRAVITY ROULETTE",
        "Glitch found the illegal angle.",
        "HARDER BOUNCES AND MORE SPEED",
        "HEAVY CORE DRAGGED YOU DOWN",
        "HIGHEST LEVEL",
        "HOLD",
        "HOLD + DRAG",
        "HOLD BLOCKS WIND / ENERGY DRAINS FAST",
        "HOLD LONGER TO BUILD FORCE",
        "HOLD SLOWS THE BALL / GRAVITY IS HEAVY",
        "HOLD TO CUT THE GUST",
        "HOLD TO DAMPEN PULSE FORCE",
        "HOLD TO PULL. RELEASE TO COAST.",
        "HOLD TO SLOW FOR PRECISION",
        "HYPE",
        "HYPE BANK",
        "Hold behind the ball to brake.",
        "Hold can dampen the pulse when it gets wild.",
        "Hold creates a rift tether.",
        "Hold timing changes the pull",
        "IN",
        "INFINITY SLOP",
        "INITIALIZING KAVVORO",
        "INSPECTING",
        "IS NOW IN YOUR HEAD",
        "KAV CROSSFIRE",
        "KAV OVERLOAD",
        "Kavvoro 9:16 replay",
        "Kavvoro challenge",
        "Kavvoro replay",
        "Keep streak %s with one ad.",
        "LAST SECOND CLUTCH",
        "LEADERBOARDS",
        "LITE SUPERPOWER",
        "LOADING",
        "LOADING LOCAL PRICE",
        "LOADOUT",
        "LOCAL RECORDS",
        "LONGEST STREAK",
        "LOW ENERGY FINISH",
        "Loop did it twice for no reason.",
        "MASTER CIRCUIT",
        "MAX AURA",
        "MOVING DANGER",
        "MYTHIC BRAINBALL",
        "MYTHIC BRAINROT",
        "MYTHIC SUPERPOWER",
        "NEEDLE THREAD",
        "NEW CHAOS SEED",
        "NEXT",
        "NEXT CLEAR",
        "NEXT LEVEL",
        "NEXT MUTATION",
        "NEXT SIGNAL",
        "NEXT UNLOCK",
        "NO POWER",
        "NO PREMIUM BRAINBALLS FOUND",
        "OPEN FAIR GLOBAL RANKING",
        "OPENING GOOGLE PLAY",
        "ORBIT VAULT",
        "ORIGINAL SPECIMEN",
        "OVERCLOCKED",
        "OVERHEAT BURNED THE RIFT",
        "OWNED",
        "Obstacle: metal platforms block, bounce and redirect the ball.",
        "Obstacle: platforms redirect you; pulse fields bend speed.",
        "Only the age group is saved locally.",
        "Original brainball still has aura.",
        "Overheat punishes long holds.",
        "PENDULUM RUN",
        "PERSONAL BEST",
        "PINBALL LADDER",
        "PLAY GAMES UNAVAILABLE",
        "PLAYER SETUP",
        "PORTAL BRAINROT",
        "PORTAL RIFT",
        "PORTAL SLING",
        "PORTAL SLINGSHOT",
        "PORTALS TELEPORT AND LAUNCH YOU.",
        "POWER RISES / ENERGY MELTS FAST",
        "POWER TAP / GLIDE AFTER BURST",
        "POWERED",
        "PREMIUM",
        "PRISM SHIELD SAID NOT TODAY",
        "PRIVACY",
        "PRIVACY POLICY",
        "PRODUCT NOT ACTIVE IN PLAY CONSOLE",
        "PULSE MAZE",
        "PULSE RELAY",
        "PULSE STORM GRABBED YOU",
        "PURCHASE CANCELLED",
        "PURCHASE COULD NOT START",
        "PURCHASE RESTORED TO THE VAULT",
        "PURCHASE SAVED / CONFIRMATION RETRYING",
        "Plasma cooked the route.",
        "Player age",
        "Power Hold charges stronger pull.",
        "Prism brain approved this nonsense.",
        "Privacy options are not required for this profile.",
        "Privacy options are temporarily unavailable.",
        "Privacy policy is temporarily unavailable.",
        "RANK",
        "RAPID TAPS BUILD FORCE",
        "RARE THOUGHT",
        "REACH THE EXIT",
        "READY",
        "READY TO MUTATE",
        "REFUSES YOU",
        "RELEASE KEEPS MOMENTUM",
        "RESET",
        "RESET TO LEVEL 01",
        "RESETS IN",
        "RESTORE",
        "RESTORE FAILED / CHECK CONNECTION",
        "RESTORED PREMIUM BRAINBALLS",
        "REWARD SIGNAL",
        "REWARDED AD NOT READY - TRY AGAIN",
        "RIFT",
        "RIFT BREAK",
        "RIFT COLLAPSED",
        "RIFT ENERGY RESETS / LEVEL RESTARTS",
        "RIFT PATH",
        "RUN COMPLETE",
        "RUN INTERRUPTED",
        "Read the lanes before committing",
        "Release early to coast and save rift energy.",
        "Release when the ball is already aimed.",
        "Rift Drain spends energy faster while holding.",
        "Rift brain knew the shortcut.",
        "Rift snapped. Braincell promoted.",
        "SELECT A BOARD",
        "SELECTED",
        "SELECTED BRAINBALL",
        "SHARE",
        "SHARE COUNTS UNLOCK BYTE / KABOOM / 404",
        "SHARE SHORT",
        "SHORT HOLD",
        "SLIPS CLOSER TO HAZARDS",
        "SLOW",
        "SMALL BOUNCE BOOST",
        "SMALL HAZARD HITBOX REDUCTION",
        "SPECIAL RULES STACK AFTER TRAINING.",
        "SPLIT DECISION",
        "START LEVEL",
        "START LEVEL 01",
        "START NEW",
        "STATUS UPDATE",
        "STREAK PROTECTION",
        "STREAK SURGE",
        "STREAK VAULT",
        "STRONGER PULL AND 35% FASTER RECHARGE",
        "SUPERPOWER",
        "SUPERPOWER READY",
        "SWITCHBACK PROTOCOL",
        "SYSTEM",
        "Share Kavvoro short",
        "Short bursts beat the gust",
        "Short tether bursts dodge better than long holds.",
        "Spend Rift in tiny snaps",
        "Static stared the level down.",
        "TAP AGAINST THE GUST",
        "TAP AGAINST WIND / ENERGY DRAINS FAST",
        "TAP TO DAMPEN PULSE FORCE",
        "TAP TO EQUIP",
        "TAP TO SLOW / GRAVITY IS HEAVY",
        "TAP TO SLOW FOR PRECISION",
        "TAP TO UNLOCK",
        "THE EXIT WINDOW IS SMALLER",
        "THE RUN RESUMES AFTER THE INTERSTITIAL",
        "TOUCH THE RIFT AND GUIDE THE BALL.",
        "TUTORIAL",
        "TWIN CRUSHERS",
        "Tap timing changes the pull",
        "Teleport timing and launch control",
        "The ball accelerates toward your finger.",
        "Trying Brainrot Chaos: Kavvoro",
        "UNLOCK",
        "UNLOCK WITH",
        "UNLOCKED",
        "UNLOCKED COUNT",
        "USE BOOST FIELDS FOR EXTRA SPEED.",
        "USE SHORT CONTROL BURSTS",
        "Use precision holds to fight gravity.",
        "Use short holds, then release to recharge.",
        "VAULT",
        "VAULT COMPLETE / MAXIMUM BRAIN ACHIEVED",
        "VAULT MAXED",
        "VORO ORBIT RIOT",
        "Void walked through the bad idea.",
        "WANTS MORE HYPE",
        "WATCH AD",
        "WATCH TO KEEP THE RUN ALIVE",
        "WIND + OVERHEAT",
        "WIND PUSHES SIDEWAYS. COUNTER IT EARLY.",
        "WIND THREW YOU OFFLINE",
        "WIND TUNNEL",
        "Wobble made physics look confused.",
        "Zap arrived before the plan.",
        "local price from Play Billing"
    )

    val requiredKeys: Set<String> =
        (renderedSourceKeys + TutorialCopy.requiredKeys + UiTranslations.requiredKeys + HomeCopy.requiredKeys).toSortedSet()

    /** Per-language gameplay catalogs plus the shared procedural UI vocabulary. */
    private val translationOverlays: Map<KavvoroLanguage, Map<String, String>> = mapOf(
        KavvoroLanguage.EN to EnTranslations.values,
        KavvoroLanguage.RO to RoTranslations.values,
        KavvoroLanguage.ES to EsTranslations.values,
        KavvoroLanguage.FR to FrTranslations.values,
        KavvoroLanguage.DE to DeTranslations.values,
        KavvoroLanguage.IT to ItTranslations.values,
        KavvoroLanguage.PT to PtTranslations.values,
        KavvoroLanguage.NL to NlTranslations.values,
        KavvoroLanguage.PL to PlTranslations.values,
        KavvoroLanguage.CS to CsTranslations.values,
        KavvoroLanguage.SV to SvTranslations.values,
        KavvoroLanguage.FI to FiTranslations.values,
        KavvoroLanguage.TR to TrTranslations.values,
        KavvoroLanguage.RU to RuTranslations.values,
        KavvoroLanguage.UK to UkTranslations.values,
        KavvoroLanguage.AR to ArTranslations.values,
        KavvoroLanguage.HI to HiTranslations.values,
        KavvoroLanguage.TH to ThTranslations.values,
        KavvoroLanguage.ID to IdTranslations.values,
        KavvoroLanguage.VI to ViTranslations.values,
        KavvoroLanguage.JA to JaTranslations.values,
        KavvoroLanguage.KO to KoTranslations.values,
        KavvoroLanguage.ZH to ZhTranslations.values,
        KavvoroLanguage.ZH_TW to ZhTwTranslations.values
    )

    private val localeCache = java.util.concurrent.ConcurrentHashMap<KavvoroLanguage, Map<String, String>>()
    private val PLACEHOLDER_REGEX = Regex("%(?:\\d+\\$)?[a-zA-Z][a-zA-Z0-9_]*")

    fun locale(language: KavvoroLanguage): Map<String, String> {
        val resolvedLanguage = if (language == KavvoroLanguage.SYSTEM) {
            KavvoroLanguage.EN
        } else {
            language
        }

        localeCache[resolvedLanguage]?.let { return it }

        return synchronized(localeCache) {
            localeCache.getOrPut(resolvedLanguage) {
                val overlay = translationOverlays[resolvedLanguage].orEmpty()
                require(overlay.keys.all { it in requiredKeys }) {
                    "Translation overlay for ${resolvedLanguage.code} contains unknown keys"
                }
                requiredKeys.associateWith { key ->
                    // Falling back to the source key yields the English source string. This
                    // must not call KavvoroI18n.t(): that re-enters locale() for the same
                    // language while its cache entry is still being computed, which would
                    // recurse until the stack is exhausted.
                    overlay[key] ?: key
                }
            }
        }
    }

    fun placeholderSignature(value: String): List<String> =
        PLACEHOLDER_REGEX
            .findAll(value)
            .map { it.value }
            .toList()

    internal val sourceInventory: Set<String> = requiredKeys
}
