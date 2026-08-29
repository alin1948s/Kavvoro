package com.moonsolstudios.kavvoro.telemetry

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KavvoroTelemetryTest {
    @Test
    fun eventAllowlistContainsOnlyTheAgreedLifecycleAndGameplayEvents() {
        assertEquals(
            setOf(
                "app_open",
                "age_gate_completed",
                "tutorial_level_completed",
                "run_started",
                "run_finished",
                "run_failed",
                "brainball_selected",
                "brainball_unlocked",
                "replay_shared",
                "purchase_restore_tapped"
            ),
            KavvoroTelemetry.Event.entries.map { it.wireName }.toSet()
        )
    }

    @Test
    fun unknownEventNamesAreRejected() {
        assertTrue(KavvoroTelemetry.Event.fromWireName("run_started") != null)
        assertFalse(KavvoroTelemetry.Event.fromWireName("crash") != null)
        assertFalse(KavvoroTelemetry.Event.fromWireName("raw_user_action") != null)
    }

    @Test
    fun piiAndUnboundedParametersAreNotAccepted() {
        listOf(
            "age",
            "raw_age",
            "date_of_birth",
            "email",
            "account_id",
            "dob",
            "free_text"
        ).forEach { key ->
            assertFalse("PII key must be rejected: $key", KavvoroTelemetry.isAllowedParamKey(key))
        }

        assertTrue(KavvoroTelemetry.isAllowedParamKey("mode"))
        assertTrue(KavvoroTelemetry.isAllowedParamKey("level"))
        assertTrue(KavvoroTelemetry.isAllowedParamKey("language"))
        assertTrue(KavvoroTelemetry.isAllowedParamKey("result"))
        assertTrue(KavvoroTelemetry.isAllowedParamKey("brainball"))
    }
}
