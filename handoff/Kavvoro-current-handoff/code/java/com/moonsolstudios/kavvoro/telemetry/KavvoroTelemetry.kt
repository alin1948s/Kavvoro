package com.moonsolstudios.kavvoro.telemetry

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

object KavvoroTelemetry {
    enum class Event(val wireName: String) {
        APP_OPEN("app_open"),
        AGE_GATE_COMPLETED("age_gate_completed"),
        TUTORIAL_LEVEL_COMPLETED("tutorial_level_completed"),
        RUN_STARTED("run_started"),
        RUN_FINISHED("run_finished"),
        RUN_FAILED("run_failed"),
        BRAINBALL_SELECTED("brainball_selected"),
        BRAINBALL_UNLOCKED("brainball_unlocked"),
        REPLAY_SHARED("replay_shared"),
        PURCHASE_RESTORE_TAPPED("purchase_restore_tapped");

        companion object {
            fun fromWireName(name: String): Event? = entries.firstOrNull { it.wireName == name }
        }
    }

    private val allowedParamKeys = setOf("mode", "level", "language", "result", "brainball")
    private var analytics: FirebaseAnalytics? = null
    private var crashlytics: FirebaseCrashlytics? = null

    fun initialize(context: Context) {
        runCatching {
            analytics = FirebaseAnalytics.getInstance(context.applicationContext)
            crashlytics = FirebaseCrashlytics.getInstance()
        }
    }

    fun logEvent(name: Event, params: Map<String, String> = emptyMap()) {
        val firebaseAnalytics = analytics ?: return
        val safeParams = params
            .filterKeys(::isAllowedParamKey)
            .mapValues { (_, value) -> value.take(MAX_PARAM_LENGTH) }
        val bundle = Bundle().apply {
            safeParams.forEach { (key, value) -> putString(key, value) }
        }
        firebaseAnalytics.logEvent(name.wireName, bundle)
    }

    fun recordNonFatal(error: Throwable, context: String) {
        val firebaseCrashlytics = crashlytics ?: return
        firebaseCrashlytics.setCustomKey("context", context.take(MAX_CONTEXT_LENGTH))
        firebaseCrashlytics.recordException(error)
    }

    fun isAllowedParamKey(key: String): Boolean = key in allowedParamKeys

    private const val MAX_PARAM_LENGTH = 64
    private const val MAX_CONTEXT_LENGTH = 96
}
