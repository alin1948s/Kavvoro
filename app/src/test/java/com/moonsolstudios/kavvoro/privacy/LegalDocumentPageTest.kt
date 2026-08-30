package com.moonsolstudios.kavvoro.privacy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegalDocumentPageTest {

    @Test
    fun legalPagesUseThePublishedMoonSolHost() {
        LegalDocumentPage.entries
            .filter { it != LegalDocumentPage.ABOUT }
            .forEach { page ->
                assertTrue(page.url!!.startsWith("https://brainroot-chaos-kavaroo.web.app/"))
                assertTrue(page.url.endsWith("/"))
            }
    }

    @Test
    fun aboutIsAnOfflineInGamePage() {
        assertNull(LegalDocumentPage.ABOUT.url)
        assertEquals("About MoonSol Studios", LegalDocumentPage.ABOUT.title)
    }
}
