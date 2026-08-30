package com.moonsolstudios.kavvoro.i18n

import android.content.Context
import androidx.core.content.edit
import java.util.Locale

enum class KavvoroLanguage(
    val code: String,
    val nativeName: String,
    val shortCode: String
) {
    SYSTEM("system", "System", "SYS"),
    EN("en", "English", "EN"),
    RO("ro", "Română", "RO"),
    ES("es", "Español", "ES"),
    FR("fr", "Français", "FR"),
    DE("de", "Deutsch", "DE"),
    IT("it", "Italiano", "IT"),
    PT("pt", "Português", "PT"),
    NL("nl", "Nederlands", "NL"),
    PL("pl", "Polski", "PL"),
    CS("cs", "Čeština", "CS"),
    SV("sv", "Svenska", "SV"),
    FI("fi", "Suomi", "FI"),
    TR("tr", "Türkçe", "TR"),
    RU("ru", "Русский", "RU"),
    UK("uk", "Українська", "UK"),
    AR("ar", "العربية", "AR"),
    HI("hi", "हिन्दी", "HI"),
    TH("th", "ภาษาไทย", "TH"),
    ID("id", "Indonesia", "ID"),
    VI("vi", "Tiếng Việt", "VI"),
    JA("ja", "日本語", "JA"),
    KO("ko", "한국어", "KO"),
    ZH("zh", "简体中文", "ZH"),
    ZH_TW("zh_tw", "繁體中文", "ZHT");

    companion object {
        fun fromCode(code: String?): KavvoroLanguage {
            return entries.firstOrNull { it.code == code } ?: SYSTEM
        }
    }
}

object KavvoroI18n {
    const val PREF_KEY = "ui_language"
    private const val PREFS_NAME = "kavvoro_locale"

    fun selected(context: Context): KavvoroLanguage {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return KavvoroLanguage.fromCode(prefs.getString(PREF_KEY, KavvoroLanguage.SYSTEM.code))
    }

    fun active(context: Context): KavvoroLanguage {
        val selected = selected(context)
        if (selected != KavvoroLanguage.SYSTEM) return selected
        val language = Locale.getDefault().language.lowercase(Locale.US)
        return when (language) {
            "ro" -> KavvoroLanguage.RO
            "es" -> KavvoroLanguage.ES
            "fr" -> KavvoroLanguage.FR
            "de" -> KavvoroLanguage.DE
            "it" -> KavvoroLanguage.IT
            "pt" -> KavvoroLanguage.PT
            "nl" -> KavvoroLanguage.NL
            "pl" -> KavvoroLanguage.PL
            "cs" -> KavvoroLanguage.CS
            "sv" -> KavvoroLanguage.SV
            "fi" -> KavvoroLanguage.FI
            "tr" -> KavvoroLanguage.TR
            "ru" -> KavvoroLanguage.RU
            "uk" -> KavvoroLanguage.UK
            "ar" -> KavvoroLanguage.AR
            "hi" -> KavvoroLanguage.HI
            "th" -> KavvoroLanguage.TH
            "in", "id" -> KavvoroLanguage.ID
            "vi" -> KavvoroLanguage.VI
            "ja" -> KavvoroLanguage.JA
            "ko" -> KavvoroLanguage.KO
            "zh" -> {
                val script = Locale.getDefault().script.lowercase(Locale.US)
                val country = Locale.getDefault().country.uppercase(Locale.US)
                if (script.contains("hant") || country in listOf("TW", "HK", "MO")) {
                    KavvoroLanguage.ZH_TW
                } else {
                    KavvoroLanguage.ZH
                }
            }
            else -> KavvoroLanguage.EN
        }
    }

    fun setSelected(context: Context, language: KavvoroLanguage) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putString(PREF_KEY, language.code) }
    }

    fun label(context: Context, language: KavvoroLanguage): String {
        if (language != KavvoroLanguage.SYSTEM) return language.nativeName
        return "${t(context, "SYSTEM")} (${active(context).shortCode})"
    }

    fun t(context: Context, english: String): String {
        return t(active(context), english)
    }

    fun t(language: KavvoroLanguage, english: String): String {
        val resolved = if (language == KavvoroLanguage.SYSTEM) KavvoroLanguage.EN else language
        return LocalizationCatalog.locale(resolved)[english] ?: english
    }

    fun audioLanguageCode(context: Context): String = audioLanguageCode(active(context))

    internal fun audioLanguageCode(language: KavvoroLanguage): String {
        return when (language) {
            KavvoroLanguage.RO -> "ro"
            KavvoroLanguage.ES -> "es"
            KavvoroLanguage.FR -> "fr"
            KavvoroLanguage.DE -> "de"
            KavvoroLanguage.IT -> "it"
            KavvoroLanguage.PT -> "pt"
            KavvoroLanguage.NL -> "nl"
            KavvoroLanguage.PL -> "pl"
            KavvoroLanguage.TR -> "tr"
            KavvoroLanguage.RU -> "ru"
            KavvoroLanguage.UK -> "uk"
            KavvoroLanguage.AR -> "ar"
            KavvoroLanguage.HI -> "hi"
            KavvoroLanguage.ID -> "id"
            KavvoroLanguage.VI -> "vi"
            KavvoroLanguage.JA -> "ja"
            KavvoroLanguage.KO -> "ko"
            KavvoroLanguage.ZH -> "zh"
            KavvoroLanguage.ZH_TW -> "zh"
            KavvoroLanguage.SYSTEM,
            KavvoroLanguage.EN,
            KavvoroLanguage.CS,
            KavvoroLanguage.SV,
            KavvoroLanguage.FI,
            KavvoroLanguage.TH -> "en"
        }
    }
}
