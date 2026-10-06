package com.moonsolstudios.kavvoro.ui.screens.agecheck

import com.moonsolstudios.kavvoro.privacy.AgeGroup
import org.junit.Assert.assertEquals
import org.junit.Test

class AgeCheckScreenViewTest {
    @Test
    fun ageSelectionResolvesOnlyToTheThreeStoredGroups() {
        assertEquals(AgeGroup.CHILD, ageGroupForAge(1))
        assertEquals(AgeGroup.CHILD, ageGroupForAge(12))
        assertEquals(AgeGroup.TEEN, ageGroupForAge(13))
        assertEquals(AgeGroup.TEEN, ageGroupForAge(17))
        assertEquals(AgeGroup.ADULT, ageGroupForAge(18))
        assertEquals(AgeGroup.ADULT, ageGroupForAge(120))
    }
}
