package com.moonsolstudios.kavvoro.privacy

/** Canonical in-app destinations for legal and studio information. */
enum class LegalDocumentPage(
    val title: String,
    val url: String?
) {
    PRIVACY("Privacy Policy", "https://brainroot-chaos-kavaroo.web.app/privacy/"),
    TERMS("Terms & Conditions", "https://brainroot-chaos-kavaroo.web.app/terms/"),
    DATA_DELETION("Data Deletion", "https://brainroot-chaos-kavaroo.web.app/data-deletion/"),
    ABOUT("About MoonSol Studios", null)
}
