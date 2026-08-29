package com.moonsolstudios.kavvoro.ui.controller

import com.moonsolstudios.kavvoro.engine.BallPower
import com.moonsolstudios.kavvoro.engine.CurseType
import com.moonsolstudios.kavvoro.engine.LevelSpec
import com.moonsolstudios.kavvoro.engine.PhysicsFrame
import com.moonsolstudios.kavvoro.engine.Point2
import com.moonsolstudios.kavvoro.model.GameMode
import kotlin.math.max
import kotlin.math.min

object GameplaySimulationDirector {

    fun calculateRiftDrain(
        level: LevelSpec,
        gameMode: GameMode,
        riftHoldSeconds: Float,
        levelHasCurse: (CurseType) -> Boolean
    ): Float {
        val drain = when {
            levelHasCurse(CurseType.RIFT_DRAIN) -> 0.6f
            levelHasCurse(CurseType.OVERHEAT) -> 0.44f + min(1f, riftHoldSeconds) * 0.28f
            levelHasCurse(CurseType.POWER_HOLD) -> 0.48f
            levelHasCurse(CurseType.FOCUS_FIELD) -> 0.4f
            gameMode == GameMode.CHAOS -> 0.46f
            else -> 0.42f
        } * level.riftDrainMultiplier
        return drain
    }

    fun calculateRiftRecharge(
        gameMode: GameMode,
        skinPower: BallPower,
        levelHasCurse: (CurseType) -> Boolean
    ): Float {
        val recharge = when {
            levelHasCurse(CurseType.RIFT_DRAIN) -> 0.14f
            levelHasCurse(CurseType.OVERHEAT) -> 0.18f
            gameMode == GameMode.CHAOS -> 0.22f
            else -> 0.24f
        } * when (skinPower) {
            BallPower.PLASMA_SURGE -> 1.35f
            BallPower.MINOR_SURGE -> 1.15f
            else -> 1f
        }
        return recharge
    }

    fun calculateRiftControlStrength(
        riftHoldSeconds: Float,
        riftEnergy: Float,
        levelHasCurse: (CurseType) -> Boolean
    ): Float {
        val holdPower = when {
            levelHasCurse(CurseType.POWER_HOLD) -> 0.38f + min(1f, riftHoldSeconds / 0.9f) * 0.62f
            levelHasCurse(CurseType.OVERHEAT) -> 0.52f + min(1f, riftHoldSeconds / 0.65f) * 0.48f
            levelHasCurse(CurseType.FOCUS_FIELD) -> 0.62f + min(1f, riftHoldSeconds / 0.45f) * 0.22f
            else -> 0.54f + min(1f, riftHoldSeconds / 0.52f) * 0.34f
        }
        return holdPower * (0.42f + riftEnergy * 0.58f)
    }

    fun calculateRiftTapPulseDuration(
        gameMode: GameMode,
        levelHasCurse: (CurseType) -> Boolean
    ): Float = when {
        levelHasCurse(CurseType.POWER_HOLD) -> 0.34f
        levelHasCurse(CurseType.FOCUS_FIELD) -> 0.28f
        levelHasCurse(CurseType.OVERHEAT) -> 0.18f
        levelHasCurse(CurseType.RIFT_DRAIN) -> 0.2f
        gameMode == GameMode.CHAOS -> 0.22f
        else -> 0.24f
    }

    data class RiftEnergyUpdateResult(
        val riftEnergy: Float,
        val inkUsed: Float,
        val shouldExhaust: Boolean
    )

    fun updateRiftEnergy(
        dt: Float,
        riftActive: Boolean,
        riftEnergy: Float,
        inkUsed: Float,
        inkLimit: Float,
        drainRate: Float,
        rechargeRate: Float
    ): RiftEnergyUpdateResult {
        if (riftActive) {
            val newEnergy = max(0f, riftEnergy - dt * drainRate)
            val newInk = max(inkUsed, (1f - newEnergy) * inkLimit)
            return RiftEnergyUpdateResult(newEnergy, newInk, shouldExhaust = newEnergy <= 0f)
        } else {
            val newEnergy = min(1f, riftEnergy + dt * rechargeRate)
            return RiftEnergyUpdateResult(newEnergy, inkUsed, shouldExhaust = false)
        }
    }

    data class ChainUpdateResult(
        val chainCharge: Float,
        val chainCount: Int,
        val shouldPlaySound: Boolean
    )

    fun updateLiveChain(
        frame: PhysicsFrame,
        dt: Float,
        riftActive: Boolean,
        currentChainCharge: Float,
        currentChainCount: Int
    ): ChainUpdateResult {
        val qualifying = (riftActive && frame.speed > 2.05f) ||
            (frame.speed > 3.6f && frame.pulseIntensity > 0.5f)
        var charge = currentChainCharge
        var count = currentChainCount
        var playSound = false

        if (qualifying) {
            charge += dt * (0.78f + min(frame.speed, 5.2f) * 0.07f)
            if (charge >= 0.62f) {
                charge -= 0.62f
                count = (count + 1).coerceAtMost(99)
                if (count <= 6) {
                    playSound = true
                }
            }
        } else {
            charge = max(0f, charge - dt * 1.15f)
            if (charge <= 0f && frame.speed < 1.7f) {
                count = 0
            }
        }
        return ChainUpdateResult(charge, count, playSound)
    }

    fun addTrailPoint(
        trail: MutableList<Point2>,
        point: Point2,
        minDistance: Float,
        maxPoints: Int,
        force: Boolean = false
    ) {
        if (!force && trail.lastOrNull()?.distanceTo(point)?.let { it < minDistance } == true) return
        trail += point
        if (trail.size > maxPoints) trail.removeAt(0)
    }

    data class SimulationOutcome(
        val state: com.moonsolstudios.kavvoro.model.GameState,
        val lastScore: com.moonsolstudios.kavvoro.engine.RunScore?,
        val streak: Int,
        val lastRiftBreak: Boolean,
        val lastRiftBreakBonus: Int,
        val lastRiftBreakReason: String,
        val lastDailyBonus: Int,
        val lastStreakMilestoneBonus: Int,
        val lastHypeScore: Int,
        val riftBreakTimer: Float,
        val powerMessage: String,
        val powerMessageTimer: Float,
        val rewardMessage: String,
        val flash: Float,
        val finishPulse: Float,
        val newlyUnlockedSkin: com.moonsolstudios.kavvoro.model.BallSkin?
    )

    fun finishSimulation(
        outcome: com.moonsolstudios.kavvoro.engine.PhysicsOutcome,
        level: LevelSpec,
        inkUsed: Float,
        simElapsed: Float,
        currentStreak: Int,
        unlockedSkinIds: Set<String>,
        ballSkins: List<com.moonsolstudios.kavvoro.model.BallSkin>,
        replay: com.moonsolstudios.kavvoro.engine.ReplayRecorder,
        shouldTriggerRiftBreak: (com.moonsolstudios.kavvoro.engine.RunScore) -> Boolean,
        calculateRiftBreakBonus: (com.moonsolstudios.kavvoro.engine.RunScore) -> Int,
        riftBreakReason: (com.moonsolstudios.kavvoro.engine.RunScore) -> String,
        claimDailyRiftBonus: () -> Int,
        calculateStreakMilestoneBonus: (Int) -> Int,
        calculateHypeScore: (com.moonsolstudios.kavvoro.engine.RunScore) -> Int,
        saveBest: (com.moonsolstudios.kavvoro.engine.RunScore) -> Unit,
        nextRewardText: (String?) -> String?,
        getUnlockedSkinIdsAfter: () -> Set<String>,
        t: (String) -> String
    ): SimulationOutcome {
        val state = if (outcome == com.moonsolstudios.kavvoro.engine.PhysicsOutcome.WON) com.moonsolstudios.kavvoro.model.GameState.WON else com.moonsolstudios.kavvoro.model.GameState.LOST
        val flash = if (outcome == com.moonsolstudios.kavvoro.engine.PhysicsOutcome.WON) 1f else 0.55f
        val finishPulse = 1f

        if (outcome == com.moonsolstudios.kavvoro.engine.PhysicsOutcome.WON) {
            val score = replay.buildScore(level, inkUsed, simElapsed)
            val newStreak = currentStreak + 1
            val isRiftBreak = shouldTriggerRiftBreak(score)
            val riftBreakBonus = if (isRiftBreak) calculateRiftBreakBonus(score) else 0
            val breakReason = if (isRiftBreak) riftBreakReason(score) else ""
            val dailyBonus = claimDailyRiftBonus()
            val streakBonus = calculateStreakMilestoneBonus(newStreak)
            val hypeScore = calculateHypeScore(score) + riftBreakBonus + dailyBonus + streakBonus
            val riftTimer = if (isRiftBreak) 2.15f else 0f
            val powMsg = if (isRiftBreak) "${t("RIFT BREAK").uppercase()}  +$riftBreakBonus" else ""
            val powMsgTimer = if (isRiftBreak) 2.35f else 0f

            saveBest(score)
            val unlockedAfter = getUnlockedSkinIdsAfter()
            val newSkin = ballSkins.firstOrNull { it.id in (unlockedAfter - unlockedSkinIds) }
            val signals = mutableListOf<String>()
            if (dailyBonus > 0) {
                signals += "${t("DAILY RIFT BONUS").uppercase()} +$dailyBonus"
            }
            if (isRiftBreak && riftBreakBonus > 0) {
                signals += "${t("RIFT BREAK").uppercase()} +$riftBreakBonus"
            }
            if (streakBonus > 0) {
                signals += "${t("STREAK SURGE").uppercase()} x$newStreak +$streakBonus"
            }
            val nextReward = nextRewardText(newSkin?.id)
            if (newSkin != null) {
                signals += "${t("UNLOCKED").uppercase()} ${newSkin.name} | ${nextReward ?: t("ALL FREE REWARDS UNLOCKED").uppercase()}"
            } else {
                signals += nextReward ?: t("ALL FREE REWARDS UNLOCKED").uppercase()
            }
            val rewardMsg = signals.joinToString(" | ")

            return SimulationOutcome(
                state = state,
                lastScore = score,
                streak = newStreak,
                lastRiftBreak = isRiftBreak,
                lastRiftBreakBonus = riftBreakBonus,
                lastRiftBreakReason = breakReason,
                lastDailyBonus = dailyBonus,
                lastStreakMilestoneBonus = streakBonus,
                lastHypeScore = hypeScore,
                riftBreakTimer = riftTimer,
                powerMessage = powMsg,
                powerMessageTimer = powMsgTimer,
                rewardMessage = rewardMsg,
                flash = flash,
                finishPulse = finishPulse,
                newlyUnlockedSkin = newSkin
            )
        } else {
            return SimulationOutcome(
                state = state,
                lastScore = null,
                streak = currentStreak,
                lastRiftBreak = false,
                lastRiftBreakBonus = 0,
                lastRiftBreakReason = "",
                lastDailyBonus = 0,
                lastStreakMilestoneBonus = 0,
                lastHypeScore = 0,
                riftBreakTimer = 0f,
                powerMessage = "",
                powerMessageTimer = 0f,
                rewardMessage = "",
                flash = flash,
                finishPulse = finishPulse,
                newlyUnlockedSkin = null
            )
        }
    }
}
