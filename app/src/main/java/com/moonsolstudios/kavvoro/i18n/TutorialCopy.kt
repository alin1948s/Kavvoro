package com.moonsolstudios.kavvoro.i18n

import com.moonsolstudios.kavvoro.engine.BallPower
import com.moonsolstudios.kavvoro.engine.CurseType

object TutorialCopy {
    val chromeKeys = setOf(
        "TIME",
        "CHAIN",
        "RIFT ENERGY",
        "TRAINING",
        "RIFT MODULE",
        "NO ADS IN TRAINING",
        "L10 UNLOCKS VORO GRAD",
        "TRAINING REWARD READY",
        "GOT IT",
        "RIFT ONLINE",
        "BOOST FIELD",
        "VORTEX FIELD",
        "BOOST FIELD ONLINE",
        "SUPERPOWER ONLINE",
        "SUPERPOWER TRIGGERED"
    )

    val fieldLabelKeys = setOf(
        "TAP",
        "SHORT TAP",
        "SLOW TAP",
        "TAP BURST",
        "POWER TAP",
        "START",
        "TAP TO PULL",
        "FOCUS",
        "POWER",
        "HEAT",
        "WIND GUARD",
        "STORM",
        "BOOST",
        "CRASH",
        "AVOID",
        "WALL",
        "BOUNCE WALL",
        "EXIT",
        "TINY EXIT",
        "GLIDE",
        "PORTAL",
        "PORTAL IN",
        "PORTAL OUT"
    )

    val levelTitleKeys = setOf(
        "RIFT TOUCH",
        "ORBIT CURVE",
        "BRAKE & COAST",
        "HAZARD DODGE",
        "PULSE CHAIN",
        "RIFT DRAIN",
        "PULSE GUARD",
        "FOCUS HEAVY",
        "POWER MOON",
        "WIND CONTROL",
        "CHAOS TOUCH",
        "CHAOS ORBIT",
        "CHAOS COAST",
        "CRASH DODGE",
        "RIFT COMBO",
        "WIND OVERHEAT"
    )

    val portalLessonKeys = setOf(
        "Portal IN teleports the ball to OUT.",
        "The exit launches with extra speed toward goal.",
        "Aim before entering; it has a short cooldown."
    )

    val lessonKeys = portalLessonKeys + setOf(
        "Tap to fire a short Rift tether.",
        "The ball accelerates toward the tap point.",
        "Chain clean taps to steer without wasting energy.",
        "Pulse zones are not decoration.",
        "They push and swirl the ball inside the circle.",
        "BOOST means the field is affecting you.",
        "Tap behind the ball to brake.",
        "Wait between taps to coast and save rift energy.",
        "Less rift used gives more HYPE.",
        "Pink crash nodes end the run.",
        "Short tap bursts dodge better than panic spam.",
        "Clean dodges keep your streak alive.",
        "CHAIN is your live combo.",
        "It grows during fast rift control or boost fields.",
        "Max chain adds big HYPE at finish.",
        "Rift energy is limited.",
        "Rift Drain spends energy faster during tap bursts.",
        "Pause between taps to recharge.",
        "Pulse Storm makes fields stronger.",
        "Tap through the pulse when it gets wild.",
        "Use the storm for speed, not panic.",
        "Focus Field slows the ball during tap bursts.",
        "Heavy Core pulls down harder.",
        "Use precision taps to fight gravity.",
        "Power Tap charges a stronger pull.",
        "Moon Glide keeps momentum after release.",
        "Tap, glide, then coast into the exit.",
        "Wind pushes the ball sideways.",
        "Overheat punishes tap spam.",
        "Use short bursts for the tiny gate."
    )

    val obstacleKeys = setOf(
        "Obstacle: portals change position and speed instantly.",
        "Obstacle: pink crash nodes instantly fail the run.",
        "Obstacle: tiny gate makes the exit much smaller.",
        "Obstacle: platforms bounce you; pulse fields bend speed.",
        "Obstacle: platforms bounce and redirect the ball.",
        "Obstacle: screen edges and timer can still end the run."
    )

    val curseRibbonKeys = setOf(
        "WIND GUARD",
        "RIFT DRAIN",
        "HEAVY CORE",
        "MOON GLIDE",
        "FOCUS FIELD",
        "POWER HOLD",
        "PULSE GUARD",
        "TINY GATE",
        "OVERHEAT"
    )

    val ballPowerRibbonKeys = setOf(
        "PRISM DENIAL",
        "VOID PHASE",
        "CHROME REBOUND",
        "PLASMA SURGE",
        "PHASE LITE",
        "REBOUND LITE",
        "SURGE LITE"
    )

    val ribbonKeys: Set<String> = curseRibbonKeys + ballPowerRibbonKeys

    val renderedKeyInventory: Set<String> =
        chromeKeys + fieldLabelKeys + levelTitleKeys + lessonKeys + obstacleKeys + ribbonKeys

    val requiredKeys: Set<String> = renderedKeyInventory

    fun curseRibbonKey(type: CurseType): String = when (type) {
        CurseType.RIFT_WIND -> "WIND GUARD"
        CurseType.RIFT_DRAIN -> "RIFT DRAIN"
        CurseType.HEAVY_CORE -> "HEAVY CORE"
        CurseType.MOON_GLIDE -> "MOON GLIDE"
        CurseType.FOCUS_FIELD -> "FOCUS FIELD"
        CurseType.POWER_HOLD -> "POWER HOLD"
        CurseType.PULSE_STORM -> "PULSE GUARD"
        CurseType.TINY_GATE -> "TINY GATE"
        CurseType.OVERHEAT -> "OVERHEAT"
    }

    fun ballPowerRibbonKey(power: BallPower): String = when (power) {
        BallPower.NONE -> error("BallPower.NONE has no HUD ribbon")
        BallPower.PRISM_SHIELD -> "PRISM DENIAL"
        BallPower.VOID_PHASE -> "VOID PHASE"
        BallPower.CHROME_RICOCHET -> "CHROME REBOUND"
        BallPower.PLASMA_SURGE -> "PLASMA SURGE"
        BallPower.MINOR_PHASE -> "PHASE LITE"
        BallPower.MINOR_RICOCHET -> "REBOUND LITE"
        BallPower.MINOR_SURGE -> "SURGE LITE"
    }

    fun ballPowerName(power: BallPower, t: (String) -> String): String = when (power) {
        BallPower.NONE -> t("NO POWER")
        else -> t(ballPowerRibbonKey(power))
    }.uppercase()

    fun ballPowerDescription(power: BallPower, t: (String) -> String): String = when (power) {
        BallPower.NONE -> t("COSMETIC LOADOUT")
        BallPower.PRISM_SHIELD -> t("BLOCKS THE FIRST HAZARD HIT")
        BallPower.VOID_PHASE -> t("SLIPS CLOSER TO HAZARDS")
        BallPower.CHROME_RICOCHET -> t("HARDER BOUNCES AND MORE SPEED")
        BallPower.PLASMA_SURGE -> t("STRONGER PULL AND 35% FASTER RECHARGE")
        BallPower.MINOR_PHASE -> t("SMALL HAZARD HITBOX REDUCTION")
        BallPower.MINOR_RICOCHET -> t("SMALL BOUNCE BOOST")
        BallPower.MINOR_SURGE -> t("10% PULL AND 15% RECHARGE BOOST")
    }.uppercase()

    fun lessonKeys(levelIndex: Int, hasPortals: Boolean): List<String> {
        if (hasPortals) return portalLessonKeys.toList()
        val orderedLessons = lessonKeys
            .filterNot { it in portalLessonKeys }
            .toList()
        val start = (levelIndex.coerceIn(1, 10) - 1) * LESSON_LINES_PER_LEVEL
        return orderedLessons.subList(start, start + LESSON_LINES_PER_LEVEL)
    }

    fun obstacleKey(
        hasPortals: Boolean,
        hasHazards: Boolean,
        hasTinyGate: Boolean,
        hasPulseZones: Boolean,
        hasBlocks: Boolean
    ): String = when {
        hasPortals -> "Obstacle: portals change position and speed instantly."
        hasHazards -> "Obstacle: pink crash nodes instantly fail the run."
        hasTinyGate -> "Obstacle: tiny gate makes the exit much smaller."
        hasPulseZones -> "Obstacle: platforms bounce you; pulse fields bend speed."
        hasBlocks -> "Obstacle: platforms bounce and redirect the ball."
        else -> "Obstacle: screen edges and timer can still end the run."
    }

    fun actionLabelKey(
        hasOverheat: Boolean,
        hasPowerTap: Boolean,
        hasFocusField: Boolean,
        hasRiftDrain: Boolean
    ): String = when {
        hasOverheat -> "TAP BURST"
        hasPowerTap -> "POWER TAP"
        hasFocusField -> "SLOW TAP"
        hasRiftDrain -> "SHORT TAP"
        else -> "TAP"
    }

    fun translation(language: KavvoroLanguage, key: String): String? {
        val resolvedLanguage = if (language == KavvoroLanguage.SYSTEM) {
            KavvoroLanguage.EN
        } else {
            language
        }
        return LocalizationCatalog.locale(resolvedLanguage)[key]
    }

    fun hasExplicitTranslation(language: KavvoroLanguage, key: String): Boolean {
        if (language == KavvoroLanguage.SYSTEM) return false
        return LocalizationCatalog.locale(language).containsKey(key)
    }

    private const val LESSON_LINES_PER_LEVEL = 3
}
