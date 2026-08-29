package com.moonsolstudios.kavvoro.i18n

import android.content.Context
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
    TR("tr", "Türkçe", "TR"),
    RU("ru", "Русский", "RU"),
    UK("uk", "Українська", "UK"),
    AR("ar", "العربية", "AR"),
    HI("hi", "हिन्दी", "HI"),
    ID("id", "Indonesia", "ID"),
    VI("vi", "Tiếng Việt", "VI"),
    JA("ja", "日本語", "JA"),
    KO("ko", "한국어", "KO"),
    ZH("zh", "中文", "ZH");

    companion object {
        fun fromCode(code: String?): KavvoroLanguage {
            return entries.firstOrNull { it.code == code } ?: SYSTEM
        }
    }
}

object KavvoroI18n {
    const val PREF_KEY = "ui_language"

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
            "tr" -> KavvoroLanguage.TR
            "ru" -> KavvoroLanguage.RU
            "uk" -> KavvoroLanguage.UK
            "ar" -> KavvoroLanguage.AR
            "hi" -> KavvoroLanguage.HI
            "in", "id" -> KavvoroLanguage.ID
            "vi" -> KavvoroLanguage.VI
            "ja" -> KavvoroLanguage.JA
            "ko" -> KavvoroLanguage.KO
            "zh" -> KavvoroLanguage.ZH
            else -> KavvoroLanguage.EN
        }
    }

    fun setSelected(context: Context, language: KavvoroLanguage) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(PREF_KEY, language.code)
            .apply()
    }

    fun label(context: Context, language: KavvoroLanguage): String {
        if (language != KavvoroLanguage.SYSTEM) return language.nativeName
        return "${t(context, "SYSTEM")} (${active(context).shortCode})"
    }

    fun t(context: Context, english: String): String {
        return t(active(context), english)
    }

    fun t(language: KavvoroLanguage, english: String): String {
        val resolvedLanguage = if (language == KavvoroLanguage.SYSTEM) {
            KavvoroLanguage.EN
        } else {
            language
        }
        return LocalizationCatalog.locale(resolvedLanguage)[english]
            ?: sourceTranslation(resolvedLanguage, english)
            ?: english
    }

    /**
     * Reads the pre-catalog sources without calling the public lookup API.
     *
     * LocalizationCatalog uses this adapter while assembling its immutable
     * snapshots. Keeping this path separate prevents catalog construction from
     * re-entering [t] and makes the legacy maps an explicit migration source.
     */
    internal fun sourceTranslation(language: KavvoroLanguage, english: String): String? {
        val resolvedLanguage = if (language == KavvoroLanguage.SYSTEM) {
            KavvoroLanguage.EN
        } else {
            language
        }

        if (resolvedLanguage == KavvoroLanguage.EN) {
            return englishSourceTranslation(english)
        }

        return TutorialCopy.translation(resolvedLanguage, english)
            ?: copyOverrides[english]?.get(resolvedLanguage)
            ?: phrases[english]?.get(resolvedLanguage)
                ?.takeUnless { it == english && english !in LocalizationCatalog.allowlistedEnglishValues }
            ?: englishSourceTranslation(english)
    }

    private fun englishSourceTranslation(english: String): String? {
        return englishShareTemplates[english]
            ?: copyOverrides[english]?.get(KavvoroLanguage.EN)
            ?: if (english in sourceKeyInventory) english else null
    }

    fun audioLanguageCode(context: Context): String {
        return when (active(context)) {
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
            else -> "en"
        }
    }

    private fun row(
        en: String,
        ro: String,
        es: String,
        fr: String,
        de: String,
        it: String,
        pt: String,
        nl: String = en,
        pl: String = en,
        tr: String = en,
        ru: String = en,
        uk: String = en,
        ar: String = en,
        hi: String = en,
        id: String = en,
        vi: String = en,
        ja: String = en,
        ko: String = en,
        zh: String = en
    ): Pair<String, Map<KavvoroLanguage, String>> {
        return en to mapOf(
            KavvoroLanguage.RO to ro,
            KavvoroLanguage.ES to es,
            KavvoroLanguage.FR to fr,
            KavvoroLanguage.DE to de,
            KavvoroLanguage.IT to it,
            KavvoroLanguage.PT to pt,
            KavvoroLanguage.NL to nl,
            KavvoroLanguage.PL to pl,
            KavvoroLanguage.TR to tr,
            KavvoroLanguage.RU to ru,
            KavvoroLanguage.UK to uk,
            KavvoroLanguage.AR to ar,
            KavvoroLanguage.HI to hi,
            KavvoroLanguage.ID to id,
            KavvoroLanguage.VI to vi,
            KavvoroLanguage.JA to ja,
            KavvoroLanguage.KO to ko,
            KavvoroLanguage.ZH to zh
        )
    }

    private fun copyRow(
        key: String,
        en: String,
        ro: String,
        es: String? = null,
        fr: String? = null,
        de: String? = null,
        it: String? = null,
        pt: String? = null,
        nl: String? = null,
        pl: String? = null,
        tr: String? = null,
        ru: String? = null,
        uk: String? = null,
        ar: String? = null,
        hi: String? = null,
        id: String? = null,
        vi: String? = null,
        ja: String? = null,
        ko: String? = null,
        zh: String? = null
    ): Pair<String, Map<KavvoroLanguage, String>> {
        val values = mutableMapOf(
            KavvoroLanguage.EN to en,
            KavvoroLanguage.RO to ro
        )
        es?.let { values[KavvoroLanguage.ES] = it }
        fr?.let { values[KavvoroLanguage.FR] = it }
        de?.let { values[KavvoroLanguage.DE] = it }
        it?.let { values[KavvoroLanguage.IT] = it }
        pt?.let { values[KavvoroLanguage.PT] = it }
        nl?.let { values[KavvoroLanguage.NL] = it }
        pl?.let { values[KavvoroLanguage.PL] = it }
        tr?.let { values[KavvoroLanguage.TR] = it }
        ru?.let { values[KavvoroLanguage.RU] = it }
        uk?.let { values[KavvoroLanguage.UK] = it }
        ar?.let { values[KavvoroLanguage.AR] = it }
        hi?.let { values[KavvoroLanguage.HI] = it }
        id?.let { values[KavvoroLanguage.ID] = it }
        vi?.let { values[KavvoroLanguage.VI] = it }
        ja?.let { values[KavvoroLanguage.JA] = it }
        ko?.let { values[KavvoroLanguage.KO] = it }
        zh?.let { values[KavvoroLanguage.ZH] = it }
        return key to values
    }

    private const val PREFS_NAME = "kavvoro_locale"

    /**
     * Share bodies use named replacements at runtime. Their source labels are
     * intentionally short UI keys, so the English runtime value must be kept
     * separately from the key itself.
     */
    private val englishShareTemplates = mapOf(
        "Can you beat my Kavvoro rift?" to
            "Can you beat my Kavvoro rift? %mode %level, %ball, rank %rank, HYPE %hype, chain x%chain, streak x%streak. Code %code. #Kavvoro #BrainrotChaos #MobileGame",
        "Trying Brainrot Chaos: Kavvoro" to
            "Trying Brainrot Chaos: Kavvoro %mode %level with %ball. Code %code. #Kavvoro #BrainrotChaos"
    )

    private val copyOverrides = mapOf(
        copyRow(
            "GAME TEXT + COLLECTION VOICE",
            "Game Text + Collection Voices",
            "Textul jocului + vocile colecției",
            "Texto del juego + voces de colección",
            "Texte du jeu + voix de collection",
            "Spieltext + Sammlungsstimmen",
            "Testi di gioco + voci collezione",
            "Texto do jogo + vozes da coleção",
            "Speltekst + collectiestemmen",
            "Tekst gry + głosy kolekcji",
            "Oyun metni + koleksiyon sesleri",
            "Текст игры + голоса коллекции",
            "Текст гри + голоси колекції",
            "نص اللعبة + أصوات المجموعة",
            "गेम टेक्स्ट + कलेक्शन आवाज़ें",
            "Teks game + suara koleksi",
            "Chữ trong game + giọng bộ sưu tập",
            "ゲーム表示 + コレクション音声",
            "게임 텍스트 + 컬렉션 음성",
            "游戏文字 + 收藏语音"
        ),
        copyRow("INITIALIZING KAVVORO", "Booting Kavvoro", "Pornim Kavvoro"),
        copyRow("PRIVACY", "Privacy", "Confidențialitate"),
        copyRow("BEST STREAK", "Best Streak", "Cea mai bună serie"),
        copyRow("EQUIPPED", "Equipped", "Activ"),
        copyRow("NEXT UNLOCK", "Next Unlock", "Următoarea deblocare"),
        copyRow("DAILY RIFT", "Daily Rift", "Riftul zilei"),
        copyRow("NEW CHAOS SEED", "New Chaos Code", "Cod Chaos nou"),
        copyRow("STREAK VAULT", "Streak Vault", "Seiful seriei"),
        copyRow("VAULT MAXED", "Vault Maxed", "Seiful e complet"),
        copyRow("START NEW", "Start Over", "Începe din nou"),
        copyRow("RESET TO LEVEL 01", "Return to Level 01", "Revii la nivelul 01"),
        copyRow("CHOOSE MODE", "Choose Mode", "Alege modul"),
        copyRow("SELECTED", "Selected", "Selectat"),
        copyRow("PLAY", "Play", "Joacă"),
        copyRow("STREAK", "Streak", "Serie"),
        copyRow("AD CHECK", "Ad Check", "Verificare reclamă"),
        copyRow("FIRST RUN", "First Run", "Prima încercare"),
        copyRow("START LEVEL 01", "Start Level 01", "Începe nivelul 01"),
        copyRow("GLOBAL SYNC OFFLINE", "Global Sync Offline", "Sincronizarea globală e offline"),
        copyRow("SELECT A BOARD", "Select a Board", "Alege un clasament"),
        copyRow("OPENING GOOGLE PLAY", "Opening Google Play", "Se deschide Google Play"),
        copyRow("PLAY GAMES UNAVAILABLE", "Play Games Unavailable", "Play Games nu este disponibil"),
        copyRow("LOCAL RECORDS", "Local Records", "Recorduri locale"),
        copyRow("GOOGLE PLAY / NO POWERS", "Google Play / No Powers", "Google Play / fără puteri"),
        copyRow("HIGHEST LEVEL", "Highest Level", "Cel mai mare nivel"),
        copyRow("LONGEST STREAK", "Longest Streak", "Cea mai lungă serie"),
        copyRow("PERSONAL BEST", "Personal Best", "Record personal"),
        copyRow("OPEN FAIR GLOBAL RANKING", "Open Fair Global Ranking", "Deschide clasamentul global corect"),
        copyRow("STATUS UPDATE", "Status Update", "Status actualizat"),
        copyRow("NEXT SIGNAL", "Next Signal", "Următorul semnal"),
        copyRow("OWNED", "Owned", "Ai"),
        copyRow("HYPE BANK", "Hype Bank", "Hype strâns"),
        copyRow("POWER", "Power", "Putere"),
        copyRow("FILTER", "Filter", "Filtru"),
        copyRow("ALL", "All", "Toate"),
        copyRow("SUPERPOWER", "Superpower", "Superputere"),
        copyRow("PREMIUM", "Premium", "Premium"),
        copyRow("COSMETIC", "Cosmetic", "Cosmetice"),
        copyRow("MYTHIC SUPERPOWER", "Mythic Superpower", "Superputere mitică"),
        copyRow("EARNED SUPERPOWER", "Earned Superpower", "Superputere câștigată"),
        copyRow("LITE SUPERPOWER", "Lite Superpower", "Superputere lite"),
        copyRow("SUPERPOWER ONLINE", "Superpower Online", "Superputere activă"),
        copyRow("SUPERPOWER READY", "Superpower Ready", "Superputere pregătită"),
        copyRow("UNLOCK", "Unlock", "Deblochează"),
        copyRow("UNLOCK WITH", "Unlock with", "Deblochează cu"),
        copyRow("TAP TO UNLOCK", "Tap to Unlock", "Atinge ca să deblochezi"),
        copyRow("READY TO MUTATE", "Ready to Mutate", "Gata de mutație"),
        copyRow("WANTS MORE HYPE", "wants more Hype", "mai cere Hype"),
        copyRow("RESTORE", "Restore", "Restaurează"),
        copyRow("EQUIPPED BRAINBALL", "Equipped Brainball", "Brainball activ"),
        copyRow("INSPECTING", "Inspecting", "Inspectezi"),
        copyRow("ACTIVE", "Active", "Activ"),
        copyRow("TAP TO EQUIP", "Tap to Equip", "Atinge ca să echipezi"),
        copyRow("EQUIP", "Equip", "Echipează"),
        copyRow("GET", "Get", "Cumpără"),
        copyRow("NEEDS", "Needs", "Necesită"),
        copyRow("MYTHIC BRAINROT", "Mythic Brainrot", "Brainrot mitic"),
        copyRow("AWAKENED", "Awakened", "Trezit"),
        copyRow("ORIGINAL SPECIMEN", "Original Specimen", "Specimen original"),
        copyRow("MAX AURA", "Max Aura", "Aură maximă"),
        copyRow("GLITCHED", "Glitched", "Glitch"),
        copyRow("FORBIDDEN", "Forbidden", "Interzis"),
        copyRow("OVERCLOCKED", "Overclocked", "Supraturat"),
        copyRow("GOOFY CLASS", "Goofy Class", "Clasă goofy"),
        copyRow("RARE THOUGHT", "Rare Thought", "Gând rar"),
        copyRow("NO POWER", "No Power", "Fără putere"),
        copyRow("COSMETIC LOADOUT", "Cosmetic Loadout", "Set cosmetic"),
        copyRow("SELECTED BRAINBALL", "Selected Brainball", "Brainball selectat"),
        copyRow("MYTHIC BRAINBALL", "Mythic Brainball", "Brainball mitic"),
        copyRow("UNLOCKED COUNT", "Unlocked", "Deblocate"),
        copyRow("VAULT", "Vault", "Seif"),
        copyRow("LOADOUT", "Loadout", "Set"),
        copyRow("SHARE", "Share", "Distribuie"),
        copyRow("POWERED", "Powered", "Putere activă"),
        copyRow("CHECKPOINT", "Checkpoint", "Punct sigur"),
        copyRow("FAILED", "Failed", "Eșuat"),
        copyRow("FOCUS", "Focus", "Concentrare"),
        copyRow("HEAT", "Heat", "Căldură"),
        copyRow("HOLD + DRAG", "Hold + Drag", "Ține apăsat + trage"),
        copyRow("STORM", "Storm", "Furtună"),
        copyRow("BOOST FIELD", "Boost Field", "Câmp de boost"),
        copyRow("VORTEX FIELD", "Vortex Field", "Câmp vortex"),
        copyRow("PRISM DENIAL", "Prism Shield", "Scut prismă"),
        copyRow("VOID PHASE", "Void Phase", "Fază în vid"),
        copyRow("CHROME REBOUND", "Chrome Rebound", "Ricochet cromat"),
        copyRow("PLASMA SURGE", "Plasma Surge", "Impuls plasmă"),
        copyRow("PHASE LITE", "Phase Lite", "Fază minoră"),
        copyRow("REBOUND LITE", "Rebound Lite", "Ricochet minor"),
        copyRow("SURGE LITE", "Surge Lite", "Impuls minor"),
        copyRow("BLOCKS THE FIRST HAZARD HIT", "Blocks the first hazard hit", "Blochează prima lovitură de obstacol"),
        copyRow("SLIPS CLOSER TO HAZARDS", "Slips closer to hazards", "Trece mai aproape de obstacole"),
        copyRow("HARDER BOUNCES AND MORE SPEED", "Harder rebounds and more speed", "Ricoșeuri mai puternice și viteză mai mare"),
        copyRow("STRONGER PULL AND 35% FASTER RECHARGE", "Stronger pull and 35% faster recharge", "Atracție mai puternică și reîncărcare cu 35% mai rapidă"),
        copyRow("SMALL HAZARD HITBOX REDUCTION", "Slightly smaller hazard hitbox", "Zonă de lovire puțin mai mică la obstacole"),
        copyRow("SMALL BOUNCE BOOST", "Small rebound boost", "Mic bonus la ricoșeu"),
        copyRow("10% PULL AND 15% RECHARGE BOOST", "10% pull and 15% recharge boost", "10% atracție și 15% reîncărcare"),
        copyRow("ALL FREE REWARDS UNLOCKED", "All free rewards unlocked", "Toate recompensele gratuite sunt deblocate"),
        copyRow("SHARE SHORT", "Share Short", "Distribuie short"),
        copyRow("NEXT LEVEL", "Next Level", "Nivelul următor"),
        copyRow("WATCH AD", "Watch Ad", "Vezi reclama"),
        copyRow("CONTINUE FREE", "Free Continue", "Continuă gratuit", pl = "Kontynuuj bezpłatnie"),
        copyRow("RIFT ENERGY", "Rift Energy", "Energie Rift"),
        copyRow("RIFT BREAK", "Rift Break", "Rift spart"),
        copyRow("LOW ENERGY FINISH", "Low Energy Finish", "Finish pe ultima energie"),
        copyRow("CHAIN SPIKE", "Chain Spike", "Chain în creștere"),
        copyRow("LAST SECOND CLUTCH", "Last-Second Clutch", "Scăpare la limită"),
        copyRow("CHAOS CONTROL", "Chaos Control", "Control în haos"),
        copyRow("CLEAN RIFT SNAP", "Clean Rift Snap", "Rift curat"),
        copyRow("DAILY RIFT BONUS", "Daily Rift Bonus", "Bonusul Riftului zilnic"),
        copyRow("STREAK SURGE", "Streak Surge", "Explozie de serie"),
        copyRow("BONUS CLAIMED TODAY", "Bonus Claimed Today", "Bonus luat azi"),
        copyRow("FIRST CLEAR BONUS", "First Clear Bonus", "Bonus la primul clear"),
        copyRow("CLAIMED", "Claimed", "Luat"),
        copyRow("READY", "Ready", "Disponibil"),
        copyRow("NEXT CLEAR", "Next Clear", "Următorul clear"),
        copyRow("RESET", "Reset", "Reset"),
        copyRow("RESETS IN", "Resets In", "Reset în"),
        copyRow("PORTAL SLING", "Portal Sling", "Lansare portal"),
        copyRow("Teleport timing and launch control", "Teleport timing and launch control", "Timing pe teleport și lansare controlată"),
        copyRow("WIND TUNNEL", "Wind Tunnel", "Tunel de vânt"),
        copyRow("Short bursts beat the gust", "Short bursts beat the gust", "Impulsurile scurte bat rafala"),
        copyRow("ENERGY TAX", "Energy Tax", "Taxă pe energie"),
        copyRow("Spend Rift in tiny snaps", "Spend Rift in tiny snaps", "Folosește Riftul în impulsuri mici"),
        copyRow("CONTROL LAB", "Control Lab", "Laborator de control"),
        copyRow("Hold timing changes the pull", "Hold timing changes the pull", "Momentul apăsării schimbă atracția"),
        copyRow("Tap timing changes the pull", "Tap timing changes the pull", "Momentul atingerii schimbă atracția"),
        copyRow("PULSE MAZE", "Pulse Maze", "Labirint pulse"),
        copyRow("Fields bend speed and direction", "Fields bend speed and direction", "Câmpurile curbează viteza și direcția"),
        copyRow("MOVING DANGER", "Moving Danger", "Pericol în mișcare"),
        copyRow("Read the lanes before committing", "Read the lanes before committing", "Citește culoarele înainte să intri"),
        copyRow("GATE STACK", "Gate Stack", "Porți în serie"),
        copyRow("Bounce angles matter", "Bounce angles matter", "Unghiurile de ricoșeu contează"),
        copyRow("CHAOS TOUCH", "Chaos Touch", "Atingere Chaos"),
        copyRow("Fast reactions, no sleepy holds", "Fast reactions, no sleepy holds", "Reacții rapide, fără hold leneș"),
        copyRow("RIFT PATH", "Rift Path", "Traseu Rift"),
        copyRow("Clean control and smooth release", "Clean control and smooth release", "Control curat și eliberare lină"),
        copyRow("Brainball rebooting. Try a shorter hold.", "Brainball rebooting. Try a shorter hold.", "Brainball se resetează. Încearcă un hold mai scurt.", pl = "Brainball resetuje się. Przytrzymaj krócej."),
        copyRow("Brainball rebooting. Try cleaner taps.", "Brainball rebooting. Try cleaner taps.", "Brainball se resetează. Încearcă atingeri mai curate.", pl = "Brainball resetuje się. Stukaj czyściej."),
        copyRow("Rift snapped. Braincell promoted.", "Rift snapped. Braincell promoted.", "Riftul a pocnit. Neuronul a primit promovare."),
        copyRow("Prism brain approved this nonsense.", "Prism brain approved this nonsense.", "Creierul prismă aprobă prostia asta."),
        copyRow("Void walked through the bad idea.", "Void walked through the bad idea.", "Vidul a trecut prin ideea proastă."),
        copyRow("Chrome bounce paid rent today.", "Chrome bounce paid rent today.", "Ricoșeul cromat și-a plătit chiria azi."),
        copyRow("Plasma cooked the route.", "Plasma cooked the route.", "Plasma a gătit traseul."),
        copyRow("Blop survived on pure vibes.", "Blop survived on pure vibes.", "Blop a supraviețuit pe vibe pur."),
        copyRow("Glitch found the illegal angle.", "Glitch found the illegal angle.", "Glitch a găsit unghiul ilegal."),
        copyRow("Zap arrived before the plan.", "Zap arrived before the plan.", "Zap a ajuns înaintea planului."),
        copyRow("Loop did it twice for no reason.", "Loop did it twice for no reason.", "Loop a făcut-o de două ori fără motiv."),
        copyRow("Static stared the level down.", "Static stared the level down.", "Static s-a uitat urât la nivel până a cedat."),
        copyRow("Rift brain knew the shortcut.", "Rift brain knew the shortcut.", "Creierul Rift știa scurtătura."),
        copyRow("Byte uploaded the win.", "Byte uploaded the win.", "Byte a încărcat victoria."),
        copyRow("Wobble made physics look confused.", "Wobble made physics look confused.", "Wobble a făcut fizica să pară confuză."),
        copyRow("Crown behavior, no debate.", "Crown behavior, no debate.", "Comportament de coroană, fără discuții."),
        copyRow("Original brainball still has aura.", "Original brainball still has aura.", "Brainball-ul original încă are aură."),
        copyRow("SUPERPOWER TRIGGERED", "Superpower Triggered", "Superputere activată"),
        copyRow("RIFT ONLINE", "Rift Online", "Rift activ"),
        copyRow("REACH THE EXIT", "Reach the exit", "Ajungi la ieșire"),
        copyRow("TRAINING", "Training", "Antrenament"),
        copyRow("NO ADS IN TRAINING", "No ads in training", "Fără reclame în antrenament"),
        copyRow("L10 UNLOCKS VORO GRAD", "L10 unlocks Voro Grad", "L10 deblochează Voro Grad"),
        copyRow("TRAINING REWARD READY", "Training reward ready", "Recompensa de antrenament e gata"),
        copyRow("RIFT MODULE", "Rift Module", "Modul Rift"),
        copyRow("START", "Start", "Start"),
        copyRow("HOLD", "Hold", "Ține apăsat"),
        copyRow("SHORT HOLD", "Short Hold", "Ține scurt"),
        copyRow("TAP BURST", "Tap Burst", "Atingeri rapide"),
        copyRow("TAP", "Tap", "Atinge"),
        copyRow("SHORT TAP", "Short Tap", "Atingere scurtă"),
        copyRow("SLOW TAP", "Slow Tap", "Atingere lentă"),
        copyRow("POWER TAP", "Power Tap", "Atingere power"),
        copyRow("TAP TO PULL", "Tap to Pull", "Atinge ca să tragi"),
        copyRow("CHARGE", "Charge", "Încarcă"),
        copyRow("SLOW", "Slow", "Încetinește"),
        copyRow("WALL", "Wall", "Perete"),
        copyRow("TINY EXIT", "Tiny Exit", "Ieșire mică"),
        copyRow("GLIDE", "Glide", "Alunecare"),
        copyRow("EXIT", "Exit", "Ieșire"),
        copyRow("AVOID", "Avoid", "Evită"),
        copyRow("BOUNCE WALL", "Bounce Wall", "Perete de ricoșeu"),
        copyRow("PORTAL IN", "Portal In", "Intrare portal"),
        copyRow("PORTAL OUT", "Portal Out", "Ieșire portal"),
        copyRow("PRISM SHIELD SAID NOT TODAY", "Prism shield said not today", "Scutul prismă a zis: nu azi"),
        copyRow("PORTAL SLINGSHOT", "Portal Slingshot", "Lansare prin portal"),
        copyRow("PULSE STORM GRABBED YOU", "Pulse storm grabbed you", "Furtuna pulse te-a prins"),
        copyRow("WIND THREW YOU OFFLINE", "Wind threw you offline", "Vântul te-a scos de pe traseu"),
        copyRow("HEAVY CORE DRAGGED YOU DOWN", "Heavy core dragged you down", "Miezul greu te-a tras în jos"),
        copyRow("OVERHEAT BURNED THE RIFT", "Overheat burned the rift", "Supraîncălzirea a ars Riftul"),
        copyRow("TOUCH THE RIFT AND GUIDE THE BALL.", "Touch the rift and guide the ball.", "Atinge Riftul și ghidează mingea."),
        copyRow("HOLD TO PULL. RELEASE TO COAST.", "Hold to pull. Release to coast.", "Ține apăsat ca să tragi. Eliberează ca să alunece."),
        copyRow("DON'T HOLD FOREVER. ENERGY IS LIMITED.", "Don't hold forever. Energy is limited.", "Nu ține apăsat la nesfârșit. Energia este limitată."),
        copyRow("AIM FOR THE EXIT PORTAL.", "Aim for the exit portal.", "Țintește portalul de ieșire."),
        copyRow("USE BOOST FIELDS FOR EXTRA SPEED.", "Use boost fields for extra speed.", "Folosește câmpurile de boost pentru viteză."),
        copyRow("DODGE CRASH NODES. THEY END THE RUN.", "Dodge crash nodes. They end the run.", "Evită nodurile de crash. Îți opresc încercarea."),
        copyRow("CHAIN FAST MOVES FOR MORE HYPE.", "Chain fast moves for more hype.", "Leagă mișcări rapide ca să câștigi mai mult Hype."),
        copyRow("PORTALS TELEPORT AND LAUNCH YOU.", "Portals teleport and launch you.", "Portalurile te teleportează și te lansează."),
        copyRow("WIND PUSHES SIDEWAYS. COUNTER IT EARLY.", "Wind pushes sideways. Counter it early.", "Vântul împinge lateral. Corectează din timp."),
        copyRow("SPECIAL RULES STACK AFTER TRAINING.", "Special rules stack after training.", "După antrenament, regulile speciale se combină."),
        copyRow("TAP AGAINST WIND / ENERGY DRAINS FAST", "Tap against wind / energy drains fast", "Atinge contra vântului / energia scade rapid"),
        copyRow("TAP TO SLOW / GRAVITY IS HEAVY", "Tap to slow / gravity is heavy", "Atinge ca să încetinești / gravitația e grea"),
        copyRow("POWER TAP / GLIDE AFTER BURST", "Power tap / glide after burst", "Power tap / alunecă după impuls"),
        copyRow("TAP AGAINST THE GUST", "Tap against the gust", "Atinge contra rafalei"),
        copyRow("TAP TO DAMPEN PULSE FORCE", "Tap to dampen pulse force", "Atinge ca să calmezi forța pulse"),
        copyRow("TAP TO SLOW FOR PRECISION", "Tap to slow for precision", "Atinge ca să încetinești precis"),
        copyRow("RAPID TAPS BUILD FORCE", "Rapid taps build force", "Atingerile rapide încarcă forța"),
        copyRow("POWER TAP", "Power Tap", "Atingere power"),
        copyRow("Tap to fire a short Rift tether.", "Tap to fire a short Rift tether.", "Atinge ca să lansezi un tether Rift scurt."),
        copyRow("The ball accelerates toward the tap point.", "The ball accelerates toward the tap point.", "Mingea accelerează spre punctul atins."),
        copyRow("Chain clean taps to steer without wasting energy.", "Chain clean taps to steer without wasting energy.", "Leagă atingeri curate ca să controlezi fără risipă de energie."),
        copyRow("Hold creates a rift tether.", "Holding creates a Rift tether.", "Când ții apăsat, creezi o legătură Rift."),
        copyRow("The ball accelerates toward your finger.", "The ball accelerates toward your finger.", "Mingea accelerează spre degetul tău."),
        copyRow("Release when the ball is already aimed.", "Release when the ball is already aimed.", "Eliberează când mingea este deja aliniată."),
        copyRow("Pulse zones are not decoration.", "Pulse zones are not decoration.", "Zonele pulse nu sunt decorative."),
        copyRow("They push and swirl the ball inside the circle.", "They push and swirl the ball inside the circle.", "Împing și rotesc mingea în interiorul cercului."),
        copyRow("BOOST means the field is affecting you.", "BOOST means the field is affecting you.", "BOOST înseamnă că acel câmp te influențează."),
        copyRow("Tap behind the ball to brake.", "Tap behind the ball to brake.", "Atinge în spatele mingii ca să frânezi."),
        copyRow("Wait between taps to coast and save rift energy.", "Wait between taps to coast and save Rift energy.", "Lasă pauză între atingeri ca să aluneci și să economisești energie Rift."),
        copyRow("Hold behind the ball to brake.", "Hold behind the ball to brake.", "Ține apăsat în spatele mingii ca să frânezi."),
        copyRow("Release early to coast and save rift energy.", "Release early to coast and save Rift energy.", "Eliberează devreme ca să aluneci și să economisești energie Rift."),
        copyRow("Less rift used gives more HYPE.", "Using less Rift gives more Hype.", "Cu cât folosești mai puțin Rift, cu atât primești mai mult Hype."),
        copyRow("Pink crash nodes end the run.", "Pink crash nodes end the run.", "Nodurile roz de crash opresc încercarea."),
        copyRow("Short tap bursts dodge better than panic spam.", "Short tap bursts dodge better than panic spam.", "Atingerile scurte evită mai bine decât spamul panicat."),
        copyRow("Short tether bursts dodge better than long holds.", "Short tether bursts dodge better than long holds.", "Impulsurile scurte te ajută mai mult decât ținutul lung."),
        copyRow("Clean dodges keep your streak alive.", "Clean dodges keep your streak alive.", "Evitările curate îți păstrează seria."),
        copyRow("CHAIN is your live combo.", "Chain is your live combo.", "Chain este combo-ul tău activ."),
        copyRow("It grows during fast rift control or boost fields.", "It grows during fast Rift control or boost fields.", "Crește când controlezi rapid Riftul sau intri în câmpuri de boost."),
        copyRow("Max chain adds big HYPE at finish.", "Max chain adds a big Hype bonus at the finish.", "Chain maxim adaugă un bonus mare de Hype la final."),
        copyRow("Rift energy is limited.", "Rift energy is limited.", "Energia Rift este limitată."),
        copyRow("Rift Drain spends energy faster during tap bursts.", "Rift Drain spends energy faster during tap bursts.", "Rift Drain consumă energia mai repede în timpul atingerilor."),
        copyRow("Pause between taps to recharge.", "Pause between taps to recharge.", "Fă pauză între atingeri ca să se reîncarce."),
        copyRow("Rift Drain spends energy faster while holding.", "Rift Drain spends energy faster while holding.", "Rift Drain consumă energia mai repede când ții apăsat."),
        copyRow("Use short holds, then release to recharge.", "Use short holds, then release to recharge.", "Folosește apăsări scurte, apoi eliberează ca să se reîncarce."),
        copyRow("Tap through the pulse when it gets wild.", "Tap through the pulse when it gets wild.", "Atinge prin pulse când devine haotic."),
        copyRow("Use the storm for speed, not panic.", "Use the storm for speed, not panic.", "Folosește furtuna pentru viteză, nu intra în panică."),
        copyRow("Focus Field slows the ball during tap bursts.", "Focus Field slows the ball during tap bursts.", "Focus Field încetinește mingea în timpul atingerilor."),
        copyRow("Focus Field slows the ball while holding.", "Focus Field slows the ball while holding.", "Focus Field încetinește mingea când ții apăsat."),
        copyRow("Heavy Core pulls down harder.", "Heavy Core pulls down harder.", "Heavy Core trage mai puternic în jos."),
        copyRow("Use precision taps to fight gravity.", "Use precision taps to fight gravity.", "Folosește atingeri precise ca să lupți cu gravitația."),
        copyRow("Use precision holds to fight gravity.", "Use precision holds to fight gravity.", "Folosește apăsări precise ca să lupți cu gravitația."),
        copyRow("Power Tap charges a stronger pull.", "Power Tap charges a stronger pull.", "Power Tap încarcă o atracție mai puternică."),
        copyRow("Power Hold charges stronger pull.", "Power Hold charges a stronger pull.", "Power Hold încarcă o atracție mai puternică."),
        copyRow("Moon Glide keeps momentum after release.", "Moon Glide keeps momentum after release.", "Moon Glide păstrează avântul după eliberare."),
        copyRow("Tap, glide, then coast into the exit.", "Tap, glide, then coast into the exit.", "Atinge, alunecă, apoi intră lin în ieșire."),
        copyRow("Charge, release, then coast into the exit.", "Charge, release, then coast into the exit.", "Încarcă, eliberează, apoi alunecă spre ieșire."),
        copyRow("Wind pushes the ball sideways.", "Wind pushes the ball sideways.", "Vântul împinge mingea lateral."),
        copyRow("Overheat punishes tap spam.", "Overheat punishes tap spam.", "Overheat pedepsește spamul de atingeri."),
        copyRow("Overheat punishes long holds.", "Overheat punishes long holds.", "Overheat pedepsește apăsările lungi."),
        copyRow("Use short bursts for the tiny gate.", "Use short bursts for the tiny gate.", "Folosește impulsuri scurte pentru poarta mică."),
        copyRow("Obstacle: portals change position and speed instantly.", "Obstacle: portals change position and speed instantly.", "Obstacol: portalurile schimbă instant poziția și viteza."),
        copyRow("Obstacle: pink crash nodes instantly fail the run.", "Obstacle: pink crash nodes instantly fail the run.", "Obstacol: nodurile roz de crash opresc încercarea instant."),
        copyRow("Obstacle: tiny gate makes the exit much smaller.", "Obstacle: tiny gate makes the exit much smaller.", "Obstacol: tiny gate micșorează mult ieșirea."),
        copyRow("Obstacle: platforms bounce you; pulse fields bend speed.", "Obstacle: platforms bounce you; pulse fields bend speed.", "Obstacol: platformele te ricoșează; câmpurile pulse curbează viteza."),
        copyRow("Obstacle: platforms bounce and redirect the ball.", "Obstacle: platforms rebound and redirect the ball.", "Obstacol: platformele ricoșează și redirecționează mingea."),
        copyRow("Obstacle: screen edges and timer can still end the run.", "Obstacle: screen edges and timer can still end the run.", "Obstacol: marginile ecranului și cronometrul pot opri încercarea."),
        copyRow("PLAYER SETUP", "Player Setup", "Configurare jucător"),
        copyRow("AGE CHECK", "Age Check", "Verificare vârstă"),
        copyRow("Enter your age in years.", "Enter your age in years.", "Introdu vârsta în ani."),
        copyRow("Only the age group is saved locally.", "Only the age group is saved locally.", "Se salvează local doar grupa de vârstă."),
        copyRow("CHILD  /  TEEN  /  ADULT", "Child / Teen / Adult", "Copil / Adolescent / Adult"),
        copyRow("ENTER YOUR AGE", "Enter Your Age", "Introdu vârsta"),
        copyRow("CHECK THE AGE", "Check the Age", "Verifică vârsta"),
        copyRow("Player age", "Player age", "Vârsta jucătorului"),
        copyRow("Privacy options are not required for this profile.", "Privacy options are not required for this profile.", "Nu sunt necesare opțiuni de confidențialitate pentru acest profil."),
        copyRow("Privacy options are temporarily unavailable.", "Privacy options are temporarily unavailable.", "Opțiunile de confidențialitate sunt temporar indisponibile."),
        copyRow(
            "PRIVACY POLICY", "Privacy Policy", "Politica de confidențialitate",
            "Política de privacidad", "Politique de confidentialité", "Datenschutzerklärung",
            "Informativa sulla privacy", "Política de privacidade", "Privacybeleid",
            "Polityka prywatności", "Gizlilik Politikası", "Политика конфиденциальности",
            "Політика конфіденційності", "سياسة الخصوصية", "गोपनीयता नीति",
            "Kebijakan Privasi", "Chính sách quyền riêng tư", "プライバシーポリシー",
            "개인정보처리방침", "隐私政策"
        ),
        copyRow(
            "AD PRIVACY CHOICES", "Ad Privacy Choices", "Opțiuni de confidențialitate pentru reclame",
            "Opciones de privacidad de anuncios", "Choix de confidentialité publicitaire", "Datenschutzoptionen für Werbung",
            "Scelte privacy per gli annunci", "Opções de privacidade dos anúncios", "Privacykeuzes voor advertenties",
            "Ustawienia prywatności reklam", "Reklam gizliliği seçenekleri", "Настройки конфиденциальности рекламы",
            "Налаштування конфіденційності реклами", "خيارات خصوصية الإعلانات", "विज्ञापन गोपनीयता विकल्प",
            "Pilihan privasi iklan", "Lựa chọn quyền riêng tư quảng cáo", "広告プライバシー設定",
            "광고 개인정보 설정", "广告隐私选项"
        ),
        copyRow(
            "CLOSE", "Close", "Închide", "Cerrar", "Fermer", "Schließen", "Chiudi", "Fechar",
            "Sluiten", "Zamknij", "Kapat", "Закрыть", "Закрити", "إغلاق", "बंद करें",
            "Tutup", "Đóng", "閉じる", "닫기", "关闭"
        ),
        copyRow(
            "Privacy policy is temporarily unavailable.",
            "Privacy policy is temporarily unavailable.",
            "Politica de confidențialitate este temporar indisponibilă."
        ),
        copyRow("CONNECTING TO GOOGLE PLAY", "Connecting to Google Play", "Conectare la Google Play"),
        copyRow("LOADING LOCAL PRICE", "Loading local price", "Se încarcă prețul local"),
        copyRow("PURCHASE RESTORED TO THE VAULT", "Purchase restored to the vault", "Achiziția a fost restaurată în seif"),
        copyRow("PURCHASE CANCELLED", "Purchase cancelled", "Achiziție anulată"),
        copyRow("ALREADY OWNED / RESTORING", "Already owned / restoring", "Deja cumpărat / se restaurează"),
        copyRow("GOOGLE PLAY ERROR", "Google Play error", "Eroare Google Play"),
        copyRow("GOOGLE PLAY BILLING UNAVAILABLE", "Google Play Billing unavailable", "Google Play Billing nu este disponibil"),
        copyRow("PRODUCT NOT ACTIVE IN PLAY CONSOLE", "Product is not active in Play Console", "Produsul nu este activ în Play Console"),
        copyRow("RESTORE FAILED / CHECK CONNECTION", "Restore failed / check connection", "Restaurarea a eșuat / verifică conexiunea"),
        copyRow("NO PREMIUM BRAINBALLS FOUND", "No premium Brainballs found", "Nu am găsit Brainballs premium"),
        copyRow("RESTORED PREMIUM BRAINBALLS", "Restored %d premium Brainballs", "S-au restaurat %d Brainballs premium"),
        copyRow("PURCHASE COULD NOT START", "Purchase could not start", "Achiziția nu a putut porni"),
        copyRow("PURCHASE SAVED / CONFIRMATION RETRYING", "Purchase saved / confirmation retrying", "Achiziție salvată / reîncerc confirmarea")
    )

    private val phrases = mapOf(
        row("SYSTEM", "Sistem", "Sistema", "Système", "System", "Sistema", "Sistema", "Systeem", "System", "Sistem", "Система", "Система", "النظام", "सिस्टम", "Sistem", "Hệ thống", "システム", "시스템", "系统"),
        row("LANGUAGE", "Limbă", "Idioma", "Langue", "Sprache", "Lingua", "Idioma", "Taal", "Język", "Dil", "Язык", "Мова", "اللغة", "भाषा", "Bahasa", "Ngôn ngữ", "言語", "언어", "语言"),
        row("CHOOSE LANGUAGE", "Alege limba", "Elige idioma", "Choisir la langue", "Sprache wählen", "Scegli lingua", "Escolher idioma", "Kies taal", "Wybierz język", "Dil seç", "Выберите язык", "Оберіть мову", "اختر اللغة", "भाषा चुनें", "Pilih bahasa", "Chọn ngôn ngữ", "言語を選択", "언어 선택", "选择语言"),
        row("GAME TEXT + COLLECTION VOICE", "Text joc + voce colecție", "Texto + voz de colección", "Texte + voix collection", "Spieltext + Sammlungsstimme", "Testi + voce collezione", "Texto + voz da coleção", "Speltekst + collectiestem", "Tekst gry + głos kolekcji", "Oyun yazısı + koleksiyon sesi", "Текст игры + голос коллекции", "Текст гри + голос колекції", "نص اللعبة + صوت المجموعة", "गेम टेक्स्ट + कलेक्शन आवाज़", "Teks game + suara koleksi", "Chữ game + giọng bộ sưu tập", "ゲーム表示 + コレクション音声", "게임 텍스트 + 컬렉션 음성", "游戏文字 + 收藏语音"),
        row("INITIALIZING KAVVORO", "Inițializez Kavvoro", "Inicializando Kavvoro", "Initialisation de Kavvoro", "Kavvoro startet", "Avvio Kavvoro", "A iniciar Kavvoro", "Kavvoro starten", "Uruchamianie Kavvoro", "Kavvoro başlatılıyor", "Запуск Kavvoro", "Запуск Kavvoro", "تشغيل Kavvoro", "Kavvoro शुरू हो रहा है", "Memulai Kavvoro", "Đang khởi động Kavvoro", "Kavvoro 起動中", "Kavvoro 시작 중", "正在启动 Kavvoro"),
        row("CURRENT", "Curent", "Actual", "Actuel", "Aktuell", "Attuale", "Atual", "Huidig", "Bieżący", "Mevcut", "Текущий", "Поточна", "الحالي", "वर्तमान", "Saat ini", "Hiện tại", "現在", "현재", "当前"),
        row("BACK", "Înapoi", "Atrás", "Retour", "Zurück", "Indietro", "Voltar", "Terug", "Wstecz", "Geri", "Назад", "Назад", "رجوع", "वापस", "Kembali", "Quay lại", "戻る", "뒤로", "返回"),
        row("PRIVACY", "Date", "Privacidad", "Confidentialité", "Datenschutz", "Privacy", "Privacidade", "Privacy", "Prywatność", "Gizlilik", "Приватность", "Приватність", "الخصوصية", "प्राइवेसी", "Privasi", "Riêng tư", "プライバシー", "개인정보", "隐私"),
        row("HYPE", "Hype", "Hype", "Hype", "Hype", "Hype", "Hype", "Hype", "Hype", "Hype", "Хайп", "Хайп", "هايب", "हाइप", "Hype", "Hype", "ハイプ", "하이프", "热度"),
        row("BEST STREAK", "Cel mai bun streak", "Mejor racha", "Meilleure série", "Beste Serie", "Miglior serie", "Melhor série", "Beste reeks", "Najlepsza seria", "En iyi seri", "Лучшая серия", "Найкраща серія", "أفضل سلسلة", "सबसे लंबी स्ट्रीक", "Streak terbaik", "Chuỗi tốt nhất", "最高ストリーク", "최고 연속", "最佳连胜"),
        row("EQUIPPED", "Echipat", "Equipado", "Équipé", "Ausgerüstet", "Equipaggiato", "Equipado", "Actief", "Założone", "Takılı", "Выбрано", "Обрано", "مجهز", "लगा हुआ", "Dipakai", "Đã trang bị", "装備中", "장착됨", "已装备"),
        row("NEXT UNLOCK", "Următoarea deblocare", "Siguiente desbloqueo", "Prochain déblocage", "Nächste Freischaltung", "Prossimo sblocco", "Próximo desbloqueio", "Volgende vrijgave", "Następne odblokowanie", "Sonraki açılış", "Следующая разблокировка", "Наступне відкриття", "الفتح التالي", "अगला अनलॉक", "Unlock berikutnya", "Mở khóa tiếp theo", "次の解放", "다음 해금", "下个解锁"),
        row("DAILY RIFT", "Rift zilnic", "Rift diario", "Rift du jour", "Tages-Rift", "Rift giornaliero", "Rift diário", "Dagelijkse Rift", "Dzienny Rift", "Günlük Rift", "Ежедневный Rift", "Щоденний Rift", "Rift يومي", "डेली Rift", "Rift harian", "Rift hằng ngày", "デイリー Rift", "일일 Rift", "每日 Rift"),
        row("NEW CHAOS SEED", "Seed nou Chaos", "Nueva seed Chaos", "Nouvelle seed Chaos", "Neuer Chaos-Seed", "Nuovo seed Chaos", "Nova seed Chaos", "Nieuwe Chaos-seed", "Nowy seed Chaos", "Yeni Chaos seed", "Новый сид Chaos", "Новий сід Chaos", "بذرة Chaos جديدة", "नई Chaos seed", "Seed Chaos baru", "Seed Chaos mới", "新しい Chaos シード", "새 Chaos 시드", "新 Chaos 种子"),
        row("STREAK VAULT", "Seif de streak", "Bóveda de racha", "Coffre de série", "Serien-Vault", "Vault serie", "Cofre de série", "Streak-kluis", "Skarbiec serii", "Seri kasası", "Хранилище серии", "Сховище серії", "خزنة السلسلة", "स्ट्रीक वॉल्ट", "Vault streak", "Kho streak", "ストリーク保管庫", "연속 보관함", "连胜库"),
        row("VAULT MAXED", "Seif la maxim", "Bóveda al máximo", "Coffre max", "Vault voll", "Vault massimo", "Cofre máximo", "Kluis vol", "Skarbiec pełny", "Kasa dolu", "Хранилище заполнено", "Сховище повне", "الخزنة ممتلئة", "वॉल्ट फुल", "Vault penuh", "Kho đã đầy", "保管庫最大", "보관함 최대", "库已满"),
        row("START NEW", "Start nou", "Nuevo inicio", "Nouveau départ", "Neu starten", "Nuova partita", "Novo início", "Nieuw starten", "Nowy start", "Yeni başlat", "Новый старт", "Новий старт", "ابدأ من جديد", "नई शुरुआत", "Mulai baru", "Bắt đầu mới", "新規開始", "새로 시작", "重新开始"),
        row("RESET TO LEVEL 01", "Reset la nivel 01", "Reinicia a nivel 01", "Retour niveau 01", "Zurück auf Level 01", "Reset al livello 01", "Reinicia no nível 01", "Reset naar level 01", "Reset do poziomu 01", "Seviye 01'e sıfırla", "Сброс на уровень 01", "Скинути до рівня 01", "إعادة إلى المستوى 01", "लेवल 01 पर रीसेट", "Reset ke level 01", "Đặt lại cấp 01", "レベル01へリセット", "레벨 01로 초기화", "重置到关卡 01"),
        row("CONTINUE", "Continuă", "Continuar", "Continuer", "Weiter", "Continua", "Continuar", "Doorgaan", "Kontynuuj", "Devam", "Продолжить", "Продовжити", "متابعة", "जारी रखें", "Lanjut", "Tiếp tục", "続ける", "계속", "继续"),
        row("CHOOSE MODE", "Alege modul", "Elige modo", "Choisir mode", "Modus wählen", "Scegli modalità", "Escolher modo", "Kies modus", "Wybierz tryb", "Mod seç", "Выберите режим", "Оберіть режим", "اختر الوضع", "मोड चुनें", "Pilih mode", "Chọn chế độ", "モード選択", "모드 선택", "选择模式"),
        row("CLASSIC", "Clasic", "Clásico", "Classique", "Klassisch", "Classico", "Clássico", "Klassiek", "Klasyczny", "Klasik", "Классика", "Класика", "كلاسيكي", "क्लासिक", "Klasik", "Cổ điển", "クラシック", "클래식", "经典"),
        row("CHAOS", "Chaos", "Caos", "Chaos", "Chaos", "Caos", "Caos", "Chaos", "Chaos", "Kaos", "Хаос", "Хаос", "فوضى", "अराजक", "Chaos", "Hỗn loạn", "カオス", "카오스", "混沌"),
        row("SELECTED", "Selectat", "Seleccionado", "Sélectionné", "Gewählt", "Selezionato", "Selecionado", "Geselecteerd", "Wybrane", "Seçildi", "Выбрано", "Обрано", "محدد", "चुना गया", "Dipilih", "Đã chọn", "選択中", "선택됨", "已选择"),
        row("SELECTED BRAINBALL", "Brainball selectat", "Brainball seleccionado", "Brainball sélectionné", "Ausgewählter Brainball", "Brainball selezionata", "Brainball selecionada", pl = "WYBRANY BRAINBALL"),
        row("READY", "Gata", "Listo", "Prêt", "Bereit", "Pronto", "Pronto", pl = "GOTOWE"),
        row("NEXT CLEAR", "Următorul clear", "Siguiente clear", "Prochain clear", "Nächster Clear", "Prossimo clear", "Próximo clear", pl = "ODNOWA"),
        row("PLAY", "Joacă", "Jugar", "Jouer", "Spielen", "Gioca", "Jogar", "Spelen", "Graj", "Oyna", "Играть", "Грати", "العب", "खेलें", "Main", "Chơi", "プレイ", "플레이", "开始"),
        row("LEVEL", "Nivel", "Nivel", "Niveau", "Level", "Livello", "Nível", "Level", "Poziom", "Seviye", "Уровень", "Рівень", "المستوى", "लेवल", "Level", "Cấp", "レベル", "레벨", "关卡"),
        row("STREAK", "Streak", "Racha", "Série", "Serie", "Serie", "Série", "Reeks", "Seria", "Seri", "Серия", "Серія", "سلسلة", "स्ट्रीक", "Streak", "Chuỗi", "ストリーク", "연속", "连胜"),
        row("AD CHECK", "Verificare reclamă", "Control anuncio", "Contrôle pub", "Ad-Check", "Controllo annuncio", "Verificar anúncio", "Advertentiecheck", "Kontrola reklamy", "Reklam kontrolü", "Проверка рекламы", "Перевірка реклами", "فحص الإعلان", "विज्ञापन जांच", "Cek iklan", "Kiểm tra quảng cáo", "広告チェック", "광고 확인", "广告检查"),
        row("FIRST RUN", "Prima tură", "Primera partida", "Première run", "Erster Run", "Prima run", "Primeira run", "Eerste run", "Pierwsza gra", "İlk koşu", "Первый забег", "Перший забіг", "أول جولة", "पहला रन", "Run pertama", "Lượt đầu", "初回ラン", "첫 런", "首次挑战"),
        row("START LEVEL 01", "Start nivel 01", "Inicio nivel 01", "Départ niveau 01", "Start Level 01", "Avvia livello 01", "Iniciar nível 01", "Start level 01", "Zacznij poziom 01", "Seviye 01 başlat", "Старт уровень 01", "Старт рівень 01", "ابدأ المستوى 01", "लेвел 01 शुरू", "Mulai level 01", "Bắt đầu cấp 01", "レベル01開始", "레벨 01 시작", "开始关卡 01"),
        row("LEADERBOARDS", "Clasamente", "Clasificaciones", "Classements", "Ranglisten", "Classifiche", "Classificações", "Ranglijsten", "Rankingi", "Liderlik", "Рейтинги", "Рейтинги", "لوحات الصدارة", "लीडरबोर्ड", "Papan skor", "Bảng xếp hạng", "ランキング", "순위표", "排行榜"),
        row("GLOBAL RANKS", "Clasament global", "Rank global", "Rang global", "Globale Ränge", "Rank globali", "Ranks globais", "Wereldrang", "Ranking globalny", "Global sıralama", "Глобальный рейтинг", "Глобальний рейтинг", "ترتيب عالمي", "वैश्विक रैंक", "Rank global", "Xếp hạng toàn cầu", "グローバル順位", "전세계 순위", "全球排名"),
        row("COLLECTION", "Colecție", "Colección", "Collection", "Sammlung", "Collezione", "Coleção", "Collectie", "Kolekcja", "Koleksiyon", "Коллекция", "Колекція", "المجموعة", "कलेक्शन", "Koleksi", "Bộ sưu tập", "コレクション", "컬렉션", "收藏"),
        row("GLOBAL SYNC OFFLINE", "Sync global offline", "Sync global offline", "Sync globale hors ligne", "Global-Sync offline", "Sync globale offline", "Sync global offline"),
        row("SELECT A BOARD", "Alege un clasament", "Elige tabla", "Choisis un tableau", "Rangliste wählen", "Scegli classifica", "Escolha tabela"),
        row("OPENING GOOGLE PLAY", "Deschid Google Play", "Abriendo Google Play", "Ouverture Google Play", "Öffne Google Play", "Apro Google Play", "Abrindo Google Play"),
        row("PLAY GAMES UNAVAILABLE", "Play Games indisponibil", "Play Games no disponible", "Play Games indisponible", "Play Games nicht verfügbar", "Play Games non disponibile", "Play Games indisponível"),
        row("LOCAL RECORDS", "Recorduri locale", "Récords locales", "Records locaux", "Lokale Rekorde", "Record locali", "Recordes locais"),
        row("GOOGLE PLAY / NO POWERS", "Google Play / fără puteri", "Google Play / sin poderes", "Google Play / sans pouvoirs", "Google Play / keine Kräfte", "Google Play / no poteri", "Google Play / sem poderes"),
        row("HIGHEST LEVEL", "Cel mai mare nivel", "Nivel máximo", "Niveau max", "Höchstes Level", "Livello massimo", "Nível máximo"),
        row("LONGEST STREAK", "Cel mai lung streak", "Racha máxima", "Plus longue série", "Längste Serie", "Serie più lunga", "Maior série"),
        row("PERSONAL BEST", "Record personal", "Marca personal", "Record perso", "Persönlich best", "Record personale", "Recorde pessoal"),
        row("OPEN FAIR GLOBAL RANKING", "Deschide rank global corect", "Abrir ranking global justo", "Ouvrir classement global fair", "Faire globale Rangliste öffnen", "Apri ranking globale fair", "Abrir ranking global justo"),
        row("STATUS UPDATE", "Update status", "Estado actualizado", "Mise à jour", "Status-Update", "Stato aggiornato", "Status atualizado"),
        row("NEXT SIGNAL", "Următorul semnal", "Siguiente señal", "Prochain signal", "Nächstes Signal", "Prossimo segnale", "Próximo sinal"),
        row("OWNED", "Deținute", "Tienes", "Possédés", "Besitz", "Possedute", "Possuídas"),
        row("AURA", "Aură", "Aura", "Aura", "Aura", "Aura", "Aura"),
        row("RESTORE", "Restaurează", "Restaurar", "Restaurer", "Wiederherstellen", "Ripristina", "Restaurar"),
        row("EQUIPPED BRAINBALL", "Brainball echipat", "Brainball equipado", "Brainball équipé", "Brainball aktiv", "Brainball equipaggiata", "Brainball equipado"),
        row("INSPECTING", "Inspectez", "Inspeccionando", "Inspection", "Inspektion", "Ispezione", "Inspecionando"),
        row("ACTIVE", "Activ", "Activo", "Actif", "Aktiv", "Attivo", "Ativo"),
        row("TAP TO EQUIP", "Tap ca să echipezi", "Toca para equipar", "Tape pour équiper", "Tippen zum Ausrüsten", "Tocca per equipaggiare", "Toque para equipar"),
        row("EQUIP", "Echipează", "Equipar", "Équiper", "Ausrüsten", "Equipaggia", "Equipar"),
        row("GET", "Ia", "Comprar", "Obtenir", "Holen", "Ottieni", "Obter"),
        row("NEEDS", "Cere", "Requiere", "Requiert", "Braucht", "Richiede", "Precisa"),
        row("MYTHIC BRAINROT", "Brainrot mitic", "Brainrot mítico", "Brainrot mythique", "Mythischer Brainrot", "Brainrot mitico", "Brainrot mítico"),
        row("AWAKENED", "Trezit", "Despierto", "Éveillé", "Erwacht", "Risvegliato", "Desperto"),
        row("ORIGINAL SPECIMEN", "Specimen original", "Espécimen original", "Spécimen original", "Original-Probe", "Esemplare originale", "Espécime original"),
        row("MAX AURA", "Aură maximă", "Aura máxima", "Aura max", "Max Aura", "Aura max", "Aura máxima"),
        row("GLITCHED", "Glitchuit", "Glitcheado", "Glitché", "Glitched", "Glitchato", "Com glitch"),
        row("FORBIDDEN", "Interzis", "Prohibido", "Interdit", "Verboten", "Proibito", "Proibido"),
        row("OVERCLOCKED", "Overclockat", "Overclock", "Surcadencé", "Overclocked", "Overclock", "Overclock"),
        row("GOOFY CLASS", "Clasă goofy", "Clase goofy", "Classe goofy", "Goofy-Klasse", "Classe goofy", "Classe goofy"),
        row("RARE THOUGHT", "Gând rar", "Pensamiento raro", "Pensée rare", "Seltener Gedanke", "Pensiero raro", "Pensamento raro"),
        row("NO POWER", "Fără putere", "Sin poder", "Sans pouvoir", "Keine Kraft", "Nessun potere", "Sem poder"),
        row("COSMETIC LOADOUT", "Loadout cosmetic", "Loadout cosmético", "Loadout cosmétique", "Kosmetik-Loadout", "Loadout cosmetico", "Loadout cosmético"),
        row("UNLOCKED", "Deblocat", "Desbloqueado", "Débloqué", "Freigeschaltet", "Sbloccato", "Desbloqueado"),
        row("UNLOCKED COUNT", "Deblocate", "Desbloqueadas", "Débloqués", "Freigeschaltet", "Sbloccate", "Desbloqueadas", "Vrij", "Odblokowane", "Açıldı", "Открыто", "Відкрито", "مفتوحة", "अनलॉक", "Terbuka", "Đã mở", "解放済み", "해금됨", "已解锁"),
        row("VAULT", "Seif", "Bóveda", "Coffre", "Vault", "Vault", "Cofre", "Kluis", "Skarbiec", "Kasa", "Хранилище", "Сховище", "الخزنة", "वॉल्ट", "Vault", "Kho", "保管庫", "보관함", "库"),
        row("LOADOUT", "Loadout", "Equipo", "Loadout", "Loadout", "Loadout", "Loadout", "Loadout", "Zestaw", "Loadout", "Набор", "Набір", "العتاد", "लोडआउट", "Loadout", "Bộ đồ", "ロードアウト", "로드아웃", "配置"),
        row("AD", "Reclamă", "Anuncio", "Pub", "Ad", "Annuncio", "Anúncio", "Advertentie", "Reklama", "Reklam", "Реклама", "Реклама", "إعلان", "विज्ञापन", "Iklan", "Quảng cáo", "広告", "광고", "广告"),
        row("CODE", "Cod", "Código", "Code", "Code", "Codice", "Código", "Code", "Kod", "Kod", "Код", "Код", "رمز", "कोड", "Kode", "Mã", "コード", "코드", "代码"),
        row("ALL FREE REWARDS UNLOCKED", "Toate recompensele gratuite sunt deblocate", "Todas las recompensas gratis desbloqueadas", "Toutes les récompenses gratuites sont débloquées", "Alle Gratis-Belohnungen freigeschaltet", "Tutte le ricompense gratis sbloccate", "Todas as recompensas grátis desbloqueadas", pl = "DARMOWE NAGRODY ODBLOKOWANE"),
        row("NEXT", "Următor", "Siguiente", "Suivant", "Nächste", "Prossimo", "Próximo"),
        row("AT", "la", "en", "à", "bei", "a", "em"),
        row("IN", "în", "en", "dans", "in", "tra", "em"),
        row("SHARE", "Share", "Compartir", "Partager", "Teilen", "Condividi", "Partilhar"),
        row("TUTORIAL", "Tutorial", "Tutorial", "Tutoriel", "Tutorial", "Tutorial", "Tutorial"),
        row("POWERED", "Cu putere", "Con poder", "Boosté", "Powered", "Potenziato", "Com poder"),
        row("CHECKPOINT", "checkpoint", "checkpoint", "checkpoint", "Checkpoint", "checkpoint", "checkpoint"),
        row("FAILED", "Eșuat", "Fallado", "Échoué", "Fehlgeschlagen", "Fallito", "Falhado"),
        row("RIFT", "Rift", "Rift", "Rift", "Rift", "Rift", "Rift"),
        row("FOCUS", "Focus", "Focus", "Focus", "Fokus", "Focus", "Foco"),
        row("POWER", "Putere", "Poder", "Puissance", "Power", "Potenza", "Poder"),
        row("HEAT", "Căldură", "Calor", "Chaleur", "Hitze", "Calore", "Calor"),
        row("HOLD + DRAG", "Ține + trage", "Mantén + arrastra", "Maintiens + glisse", "Halten + ziehen", "Tieni + trascina", "Segura + arrasta"),
        row("STORM", "Furtună", "Tormenta", "Tempête", "Sturm", "Tempesta", "Tempestade"),
        row("BOOST FIELD", "Câmp boost", "Campo boost", "Champ boost", "Boost-Feld", "Campo boost", "Campo boost"),
        row("VORTEX FIELD", "Câmp vortex", "Campo vórtice", "Champ vortex", "Vortex-Feld", "Campo vortice", "Campo vórtice"),
        row("PRISM DENIAL", "Prism denial", "Bloqueo prisma", "Refus prisme", "Prisma-Abwehr", "Rifiuto prisma", "Bloqueio prisma"),
        row("VOID PHASE", "Fază void", "Fase void", "Phase void", "Void-Phase", "Fase void", "Fase void"),
        row("CHROME REBOUND", "Rebound cromat", "Rebote cromo", "Rebond chrome", "Chrome-Rebound", "Rimbalzo chrome", "Rebote chrome"),
        row("PLASMA SURGE", "Surge plasmă", "Subida plasma", "Surtension plasma", "Plasma-Schub", "Impulso plasma", "Surto plasma"),
        row("PHASE LITE", "Fază lite", "Fase lite", "Phase lite", "Phase lite", "Fase lite", "Fase lite"),
        row("REBOUND LITE", "Rebound lite", "Rebote lite", "Rebond lite", "Rebound lite", "Rimbalzo lite", "Rebote lite"),
        row("SURGE LITE", "Surge lite", "Surge lite", "Surge lite", "Surge lite", "Surge lite", "Surge lite"),
        row("BLOCKS THE FIRST HAZARD HIT", "Blochează prima lovitură de hazard", "Bloquea el primer golpe peligroso", "Bloque le premier choc dangereux", "Blockt den ersten Hazard-Treffer", "Blocca il primo colpo hazard", "Bloqueia o primeiro impacto"),
        row("SLIPS CLOSER TO HAZARDS", "Alunecă mai aproape de hazarduri", "Pasa más cerca de peligros", "Glisse plus près des dangers", "Schlüpft näher an Gefahren vorbei", "Scivola più vicino ai pericoli", "Passa mais perto dos perigos"),
        row("HARDER BOUNCES AND MORE SPEED", "Bounce mai tare și mai multă viteză", "Rebotes más fuertes y más velocidad", "Rebonds plus forts et plus de vitesse", "Härtere Bounces und mehr Tempo", "Rimbalzi più forti e più velocità", "Rebotes mais fortes e mais velocidade"),
        row("STRONGER PULL AND 35% FASTER RECHARGE", "Pull mai puternic și recharge cu 35% mai rapid", "Tirón más fuerte y recarga 35% más rápida", "Attraction plus forte et recharge 35% plus rapide", "Stärkerer Zug und 35% schnellere Aufladung", "Tiro più forte e ricarica 35% più veloce", "Puxão mais forte e recarga 35% mais rápida"),
        row("SMALL HAZARD HITBOX REDUCTION", "Hitbox hazard puțin redus", "Hitbox de peligro reducida", "Hitbox danger un peu réduite", "Kleinere Hazard-Hitbox", "Hitbox pericolo ridotta", "Hitbox de perigo reduzida"),
        row("SMALL BOUNCE BOOST", "Mic boost la bounce", "Pequeño boost de rebote", "Petit boost de rebond", "Kleiner Bounce-Boost", "Piccolo boost rimbalzo", "Pequeno boost de rebote"),
        row("10% PULL AND 15% RECHARGE BOOST", "10% pull și 15% recharge boost", "10% tirón y 15% recarga", "10% attraction et 15% recharge", "10% Zug und 15% Aufladung", "10% tiro e 15% ricarica", "10% puxão e 15% recarga"),
        row("CHECKING GOOGLE PLAY PURCHASES", "Verific achizițiile Google Play", "Comprobando compras de Google Play", "Vérification des achats Google Play", "Prüfe Google-Play-Käufe", "Controllo acquisti Google Play", "A verificar compras Google Play"),
        row("IS NOW IN YOUR HEAD", "este acum în capul tău", "está ahora en tu cabeza", "est maintenant dans ta tête", "ist jetzt in deinem Kopf", "è ora nella tua testa", "está agora na tua cabeça"),
        row("REFUSES YOU", "te refuză", "te rechaza", "te refuse", "lehnt dich ab", "ti rifiuta", "recusa-te"),
        row("local price from Play Billing", "preț local din Play Billing", "precio local de Play Billing", "prix local Play Billing", "lokaler Preis aus Play Billing", "prezzo locale Play Billing", "preço local do Play Billing"),
        row("NEXT MUTATION", "Următoarea mutație", "Siguiente mutación", "Prochaine mutation", "Nächste Mutation", "Prossima mutazione", "Próxima mutação"),
        row("VAULT COMPLETE / MAXIMUM BRAIN ACHIEVED", "Vault complet / creier maxim atins", "Bóveda completa / cerebro máximo", "Coffre complet / cerveau maximum", "Vault komplett / maximales Brain erreicht", "Vault completo / cervello massimo", "Cofre completo / cérebro máximo"),
        row("RUN COMPLETE", "Run complet", "Run completada", "Run terminée", "Run abgeschlossen", "Run completata", "Run completa", pl = "Gra ukończona"),
        row("RUN INTERRUPTED", "Run întrerupt", "Run interrumpida", "Run interrompue", "Run unterbrochen", "Run interrotta", "Run interrompida", pl = "Gra przerwana"),
        row("RIFT COLLAPSED", "Rift prăbușit", "Rift colapsado", "Rift effondré", "Rift kollabiert", "Rift collassato", "Rift colapsado", pl = "Rift zapadł się"),
        row("RANK", "Rang", "Rango", "Rang", "Rang", "Grado", "Rank"),
        row("TIME", "Timp", "Tiempo", "Temps", "Zeit", "Tempo", "Tempo"),
        row("CHAIN", "Chain", "Cadena", "Chaîne", "Kette", "Catena", "Corrente"),
        row("BEST", "Best", "Mejor", "Meilleur", "Best", "Miglior", "Melhor"),
        row("REWARD SIGNAL", "Semnal recompensă", "Señal recompensa", "Signal récompense", "Belohnungs-Signal", "Segnale ricompensa", "Sinal recompensa", pl = "SYGNAŁ NAGRODY"),
        row("SHARE SHORT", "Share short", "Compartir short", "Partager short", "Short teilen", "Condividi short", "Partilhar short"),
        row("NEXT LEVEL", "Nivel următor", "Siguiente nivel", "Niveau suivant", "Nächstes Level", "Prossimo livello", "Próximo nível"),
        row("WATCH AD", "Vezi reclamă", "Ver anuncio", "Voir pub", "Ad ansehen", "Guarda annuncio", "Ver anúncio"),
        row("CONTINUE FREE", "Continuă gratis", "Continuar gratis", "Continuer gratuit", "Gratis weiter", "Continua gratis", "Continuar grátis", pl = "Kontynuuj bezpłatnie"),
        row("RIFT ENERGY", "Energie Rift", "Energía Rift", "Énergie Rift", "Rift-Energie", "Energia Rift", "Energia Rift"),
        row("SUPERPOWER TRIGGERED", "Superputere activată", "Superpoder activado", "Super-pouvoir activé", "Superkraft aktiv", "Superpotere attivato", "Superpoder ativado"),
        row("RIFT ONLINE", "Rift online", "Rift online", "Rift en ligne", "Rift online", "Rift online", "Rift online", pl = "RIFT AKTYWNY"),
        row("REACH THE EXIT", "Ajungi la ieșire", "Llega a la salida", "Atteins la sortie", "Zum Ausgang", "Raggiungi l'uscita", "Chega à saída"),
        row("TRAINING", "Training", "Entreno", "Entraînement", "Training", "Training", "Treino"),
        row("RIFT MODULE", "Modul Rift", "Módulo Rift", "Module Rift", "Rift-Modul", "Modulo Rift", "Módulo Rift"),
        row("PORTAL", "Portal", "Portal", "Portail", "Portal", "Portale", "Portal"),
        row("START", "Start", "Inicio", "Départ", "Start", "Start", "Início"),
        row(
            "START LEVEL",
            "Începe nivelul",
            "Iniciar nivel",
            "Commencer le niveau",
            "Level starten",
            "Avvia livello",
            "Iniciar nível",
            "Level starten",
            "Rozpocznij poziom",
            "Seviyeyi başlat",
            "Начать уровень",
            "Почати рівень",
            "ابدأ المستوى",
            "लेवल शुरू करें",
            "Mulai level",
            "Bắt đầu cấp độ",
            "レベル開始",
            "레벨 시작",
            "开始关卡"
        ),
        row("HOLD", "Ține", "Mantén", "Maintiens", "Halten", "Tieni", "Segura"),
        row("SHORT HOLD", "Ține scurt", "Toque corto", "Maintien court", "Kurz halten", "Tieni poco", "Segura curto"),
        row("TAP BURST", "Tap burst", "Toques rápidos", "Tap rapide", "Burst tippen", "Tap rapido", "Toque rápido"),
        row("CHARGE", "Încarcă", "Cargar", "Charger", "Laden", "Carica", "Carregar"),
        row("SLOW", "Încetinește", "Frenar", "Ralentir", "Bremsen", "Rallenta", "Abrandar"),
        row("BOOST", "Boost", "Boost", "Boost", "Boost", "Boost", "Boost"),
        row("CRASH", "Crash", "Choque", "Crash", "Crash", "Crash", "Crash"),
        row("WALL", "Perete", "Muro", "Mur", "Wand", "Muro", "Parede"),
        row("TINY EXIT", "Ieșire mică", "Salida pequeña", "Petite sortie", "Kleiner Ausgang", "Uscita piccola", "Saída pequena"),
        row("GLIDE", "Glide", "Planeo", "Glisse", "Gleiten", "Planata", "Deslize"),
        row("EXIT", "Ieșire", "Salida", "Sortie", "Ausgang", "Uscita", "Saída"),
        row("AVOID", "Evită", "Evita", "Évite", "Meiden", "Evita", "Evita"),
        row("BOUNCE WALL", "Perete bounce", "Muro rebote", "Mur rebond", "Bounce-Wand", "Muro rimbalzo", "Parede bounce"),
        row("PORTAL IN", "Portal IN", "Portal IN", "Portail IN", "Portal IN", "Portale IN", "Portal IN"),
        row("PORTAL OUT", "Portal OUT", "Portal OUT", "Portail OUT", "Portal OUT", "Portale OUT", "Portal OUT"),
        row("PRISM SHIELD SAID NOT TODAY", "Scutul prismă a zis nu azi", "El escudo prisma dijo hoy no", "Le bouclier prisme dit pas aujourd'hui", "Prisma-Schild sagt heute nicht", "Lo scudo prisma dice non oggi", "O escudo prisma disse hoje não"),
        row("PORTAL SLINGSHOT", "Slingshot de portal", "Tirachinas portal", "Propulsion portail", "Portal-Schleuder", "Fionda portale", "Estilingue portal"),
        row("PULSE STORM GRABBED YOU", "Furtuna pulse te-a prins", "La tormenta pulse te atrapó", "La tempête pulse t'a attrapé", "Pulse-Sturm hat dich gepackt", "La tempesta pulse ti ha preso", "A tempestade pulse apanhou-te"),
        row("BOOST FIELD ONLINE", "Câmp boost online", "Campo boost online", "Champ boost en ligne", "Boost-Feld online", "Campo boost online", "Campo boost online"),
        row("REWARDED AD NOT READY - TRY AGAIN", "Reclama reward nu e gata - încearcă iar", "Anuncio con recompensa no listo - prueba otra vez", "Pub récompensée pas prête - réessaie", "Rewarded Ad nicht bereit - erneut versuchen", "Annuncio reward non pronto - riprova", "Anúncio reward não pronto - tenta de novo"),
        row("RIFT ENERGY RESETS / LEVEL RESTARTS", "Energia Rift se resetează / nivelul reîncepe", "Energía Rift reinicia / nivel reinicia", "Énergie Rift remise à zéro / niveau relancé", "Rift-Energie reset / Level startet neu", "Energia Rift reset / livello riavvia", "Energia Rift reinicia / nível reinicia", pl = "Energia Rift / restart poziomu"),
        row("Keep streak %s with one ad.", "Păstrezi streak %s cu o reclamă.", "Mantén racha %s con un anuncio.", "Garde la série %s avec une pub.", "Serie %s mit einer Ad halten.", "Mantieni serie %s con un annuncio.", "Mantém série %s com um anúncio.", pl = "Zachowaj serię %s dzięki reklamie."),
        row("Free recovery available. Streak %s stays active.", "Recovery gratis disponibil. Streak %s rămâne activ.", "Recuperación gratis disponible. Racha %s sigue activa.", "Récupération gratuite. Série %s reste active.", "Gratis-Rettung verfügbar. Serie %s bleibt aktiv.", "Recupero gratis disponibile. Serie %s resta attiva.", "Recuperação grátis disponível. Série %s continua ativa.", pl = "Darmowe wznowienie. Seria %s trwa."),
        row("CRASH REPLAY", "Replay crash", "Replay choque", "Replay crash", "Crash-Replay", "Replay crash", "Replay crash"),
        row("CHASE CLEAN RUNS", "Vânează run-uri curate", "Busca runs limpias", "Cherche des runs propres", "Jage saubere Runs", "Cerca run pulite", "Procura runs limpas"),
        row("PORTAL RIFT", "Rift portal", "Rift portal", "Rift portail", "Portal-Rift", "Rift portale", "Rift portal"),
        row("WIND + OVERHEAT", "Vânt + overheat", "Viento + sobrecalor", "Vent + surchauffe", "Wind + Overheat", "Vento + surriscaldamento", "Vento + sobreaquecimento"),
        row("FOCUS HEAVY", "Focus greu", "Focus pesado", "Focus lourd", "Schwerer Fokus", "Focus pesante", "Foco pesado"),
        row("POWER MOON", "Power moon", "Power moon", "Power moon", "Power Moon", "Power moon", "Power moon"),
        row("WIND GUARD", "Guardă vânt", "Guardia viento", "Garde-vent", "Windschutz", "Guardia vento", "Guarda vento"),
        row("OVERHEAT", "Overheat", "Sobrecalor", "Surchauffe", "Overheat", "Surriscaldamento", "Sobreaquecimento"),
        row("RIFT DRAIN", "Drain Rift", "Drenaje Rift", "Drain Rift", "Rift-Drain", "Drain Rift", "Dreno Rift"),
        row("PULSE GUARD", "Guardă pulse", "Guardia pulse", "Garde pulse", "Pulse-Schutz", "Guardia pulse", "Guarda pulse"),
        row("FOCUS FIELD", "Câmp focus", "Campo focus", "Champ focus", "Fokus-Feld", "Campo focus", "Campo foco"),
        row("POWER HOLD", "Hold putere", "Hold poder", "Hold puissance", "Power-Hold", "Hold potenza", "Hold poder"),
        row("HEAVY CORE", "Core greu", "Núcleo pesado", "Noyau lourd", "Schwerer Kern", "Core pesante", "Núcleo pesado"),
        row("MOON GLIDE", "Glide lunar", "Planeo lunar", "Glisse lunaire", "Mondgleiten", "Glide lunare", "Deslize lunar"),
        row("TINY GATE", "Poartă mică", "Puerta pequeña", "Petite porte", "Kleines Tor", "Porta piccola", "Porta pequena"),
        row("ENTER IN / EXIT OUT WITH EXTRA SPEED", "Intră IN / ieși OUT cu viteză extra", "Entra IN / sal OUT con velocidad extra", "Entre IN / sors OUT avec vitesse bonus", "IN rein / OUT raus mit Extra-Speed", "Entra IN / esci OUT con velocità extra", "Entra IN / sai OUT com velocidade extra"),
        row("HOLD BLOCKS WIND / ENERGY DRAINS FAST", "Hold blochează vântul / energia se scurge rapid", "Hold bloquea viento / energía baja rápido", "Hold bloque le vent / énergie fond vite", "Halten blockt Wind / Energie sinkt schnell", "Hold blocca vento / energia cala veloce", "Hold bloqueia vento / energia desce rápido"),
        row("HOLD SLOWS THE BALL / GRAVITY IS HEAVY", "Hold încetinește mingea / gravitația e grea", "Hold frena la bola / gravedad pesada", "Hold ralentit la balle / gravité lourde", "Halten bremst Ball / schwere Gravität", "Hold rallenta palla / gravità pesante", "Hold abranda bola / gravidade pesada"),
        row("BUILD POWER / RELEASE TO GLIDE", "Încarcă putere / eliberează pentru glide", "Carga poder / suelta para planear", "Charge la puissance / relâche pour glisser", "Power laden / loslassen zum Gleiten", "Carica potenza / rilascia per planare", "Carrega poder / solta para deslizar"),
        row("HOLD TO CUT THE GUST", "Ține ca să tai rafala", "Mantén para cortar ráfaga", "Maintiens pour couper la rafale", "Halten gegen die Böe", "Tieni per tagliare la raffica", "Segura para cortar rajada"),
        row("POWER RISES / ENERGY MELTS FAST", "Puterea crește / energia se topește rapid", "Poder sube / energía se derrite", "Puissance monte / énergie fond vite", "Power steigt / Energie schmilzt", "Potenza sale / energia fonde", "Poder sobe / energia derrete"),
        row("USE SHORT CONTROL BURSTS", "Folosește burst-uri scurte", "Usa ráfagas cortas", "Utilise des bursts courts", "Kurze Kontroll-Bursts nutzen", "Usa burst brevi", "Usa bursts curtos"),
        row("HOLD TO DAMPEN PULSE FORCE", "Ține ca să calmezi forța pulse", "Mantén para bajar fuerza pulse", "Maintiens pour calmer la force pulse", "Halten dämpft Pulse-Kraft", "Tieni per smorzare pulse", "Segura para reduzir pulse"),
        row("HOLD TO SLOW FOR PRECISION", "Ține ca să încetinești pentru precizie", "Mantén para precisión", "Maintiens pour ralentir avec précision", "Halten für Präzision", "Tieni per rallentare con precisione", "Segura para precisão"),
        row("HOLD LONGER TO BUILD FORCE", "Ține mai mult ca să încarci forța", "Mantén más para cargar fuerza", "Maintiens plus pour charger la force", "Länger halten für Kraft", "Tieni di più per caricare forza", "Segura mais para carregar força"),
        row("GRAVITY PULLS HARDER", "Gravitația trage mai tare", "Gravedad tira más fuerte", "La gravité tire plus fort", "Gravität zieht stärker", "Gravità tira più forte", "Gravidade puxa mais forte"),
        row("RELEASE KEEPS MOMENTUM", "Release păstrează momentum", "Soltar conserva impulso", "Relâcher garde l'élan", "Loslassen hält Momentum", "Rilascio conserva slancio", "Soltar mantém impulso"),
        row("THE EXIT WINDOW IS SMALLER", "Fereastra de ieșire e mai mică", "La salida es más pequeña", "La fenêtre de sortie est plus petite", "Exit-Fenster ist kleiner", "La finestra uscita è più piccola", "A janela de saída é menor"),
        row("STREAK PROTECTION", "Protecție streak", "Protección de racha", "Protection série", "Serienschutz", "Protezione serie", "Proteção de série"),
        row("AD CONTINUE", "Continue cu reclamă", "Continuar con anuncio", "Continuer avec pub", "Weiter mit Ad", "Continua con annuncio", "Continuar com anúncio"),
        row("WATCH TO KEEP THE RUN ALIVE", "Uită-te ca să păstrezi run-ul", "Mira para mantener la run", "Regarde pour garder la run", "Ansehen, um den Run zu halten", "Guarda per salvare la run", "Vê para manter a run"),
        row("THE RUN RESUMES AFTER THE INTERSTITIAL", "Run-ul continuă după reclamă", "La run sigue tras el intersticial", "La run reprend après la pub", "Run läuft nach der Ad weiter", "La run riprende dopo l'annuncio", "A run continua após o anúncio"),
        row("LOADING", "Se încarcă", "Cargando", "Chargement", "Lädt", "Caricamento", "A carregar"),
        row("CONTINUE WITH AD", "Continuă cu reclamă", "Continuar con anuncio", "Continuer avec pub", "Weiter mit Ad", "Continua con annuncio", "Continuar com anúncio"),
        row("BUILDING SHORT", "Construiesc short", "Creando short", "Création du short", "Short wird gebaut", "Creo short", "A criar short"),
        row("SHARE COUNTS UNLOCK BYTE / KABOOM / 404", "Share-ul deblochează BYTE / KABOOM / 404", "Compartir desbloquea BYTE / KABOOM / 404", "Le partage débloque BYTE / KABOOM / 404", "Teilen schaltet BYTE / KABOOM / 404 frei", "Le condivisioni sbloccano BYTE / KABOOM / 404", "Partilhas desbloqueiam BYTE / KABOOM / 404"),
        row("Can you beat my Kavvoro rift?", "Poți bate rift-ul meu Kavvoro? %mode %level, %ball, rang %rank, HYPE %hype, chain x%chain, streak x%streak. Cod %code. #Kavvoro #BrainrotChaos #MobileGame", "¿Puedes superar mi rift Kavvoro? %mode %level, %ball, rango %rank, HYPE %hype, cadena x%chain, racha x%streak. Código %code. #Kavvoro #BrainrotChaos #MobileGame", "Peux-tu battre mon rift Kavvoro ? %mode %level, %ball, rang %rank, HYPE %hype, chaîne x%chain, série x%streak. Code %code. #Kavvoro #BrainrotChaos #MobileGame", "Schlägst du meinen Kavvoro-Rift? %mode %level, %ball, Rang %rank, HYPE %hype, Kette x%chain, Serie x%streak. Code %code. #Kavvoro #BrainrotChaos #MobileGame", "Batti il mio rift Kavvoro? %mode %level, %ball, rank %rank, HYPE %hype, catena x%chain, serie x%streak. Codice %code. #Kavvoro #BrainrotChaos #MobileGame", "Consegues bater o meu rift Kavvoro? %mode %level, %ball, rank %rank, HYPE %hype, corrente x%chain, série x%streak. Código %code. #Kavvoro #BrainrotChaos #MobileGame"),
        row("Trying Brainrot Chaos: Kavvoro", "Încerc Brainrot Chaos: Kavvoro %mode %level cu %ball. Cod %code. #Kavvoro #BrainrotChaos", "Probando Brainrot Chaos: Kavvoro %mode %level con %ball. Código %code. #Kavvoro #BrainrotChaos", "J'essaie Brainrot Chaos: Kavvoro %mode %level avec %ball. Code %code. #Kavvoro #BrainrotChaos", "Teste Brainrot Chaos: Kavvoro %mode %level mit %ball. Code %code. #Kavvoro #BrainrotChaos", "Provo Brainrot Chaos: Kavvoro %mode %level con %ball. Codice %code. #Kavvoro #BrainrotChaos", "A tentar Brainrot Chaos: Kavvoro %mode %level com %ball. Código %code. #Kavvoro #BrainrotChaos"),
        row("Kavvoro 9:16 replay", "Replay Kavvoro 9:16", "Replay Kavvoro 9:16", "Replay Kavvoro 9:16", "Kavvoro 9:16 Replay", "Replay Kavvoro 9:16", "Replay Kavvoro 9:16"),
        row("Beat my Kavvoro rift", "Bate rift-ul meu Kavvoro", "Supera mi rift Kavvoro", "Bats mon rift Kavvoro", "Schlag meinen Kavvoro-Rift", "Batti il mio rift Kavvoro", "Bate o meu rift Kavvoro"),
        row("Kavvoro replay", "Replay Kavvoro", "Replay Kavvoro", "Replay Kavvoro", "Kavvoro Replay", "Replay Kavvoro", "Replay Kavvoro"),
        row("Kavvoro challenge", "Challenge Kavvoro", "Reto Kavvoro", "Défi Kavvoro", "Kavvoro Challenge", "Sfida Kavvoro", "Desafio Kavvoro"),
        row("Share Kavvoro short", "Trimite short-ul Kavvoro", "Compartir short Kavvoro", "Partager le short Kavvoro", "Kavvoro-Short teilen", "Condividi short Kavvoro", "Partilhar short Kavvoro"),
        row("RIFT TOUCH", "Atingere Rift", "Toque Rift", "Toucher Rift", "Rift-Touch", "Tocco Rift", "Toque Rift"),
        row("ORBIT CURVE", "Curbă orbită", "Curva órbita", "Courbe orbite", "Orbit-Kurve", "Curva orbita", "Curva órbita"),
        row("BRAKE & COAST", "Frână și coast", "Frena y desliza", "Freine et glisse", "Bremsen & Rollen", "Frena e scorri", "Trava e desliza"),
        row("HAZARD DODGE", "Dodge hazard", "Esquiva peligro", "Esquive danger", "Hazard-Dodge", "Schiva hazard", "Desvia perigo"),
        row("PULSE CHAIN", "Chain pulse", "Cadena pulse", "Chaîne pulse", "Pulse-Kette", "Catena pulse", "Corrente pulse"),
        row("WIND CONTROL", "Control vânt", "Control viento", "Contrôle vent", "Windkontrolle", "Controllo vento", "Controlo vento"),
        row("CHAOS TOUCH", "Touch chaos", "Toque caos", "Toucher chaos", "Chaos-Touch", "Tocco caos", "Toque caos"),
        row("CHAOS ORBIT", "Orbită chaos", "Órbita caos", "Orbite chaos", "Chaos-Orbit", "Orbita caos", "Órbita caos"),
        row("CHAOS COAST", "Coast chaos", "Desliz caos", "Glisse chaos", "Chaos-Rollen", "Scorrimento caos", "Deslize caos"),
        row("CRASH DODGE", "Dodge crash", "Esquiva choque", "Esquive crash", "Crash-Dodge", "Schiva crash", "Desvia crash"),
        row("RIFT COMBO", "Combo Rift", "Combo Rift", "Combo Rift", "Rift-Kombo", "Combo Rift", "Combo Rift"),
        row("WIND OVERHEAT", "Vânt overheat", "Viento sobrecalor", "Vent surchauffe", "Wind-Overheat", "Vento overheat", "Vento sobreaquecido"),
        row("PORTAL SLING", "Sling portal", "Sling portal", "Sling portail", "Portal-Sling", "Sling portale", "Sling portal"),
        row("PORTAL BRAINROT", "Portal brainrot", "Portal brainrot", "Portail brainrot", "Portal Brainrot", "Portale brainrot", "Portal brainrot"),
        row("SWITCHBACK PROTOCOL", "Protocol switchback", "Protocolo switchback", "Protocole switchback", "Switchback-Protokoll", "Protocollo switchback", "Protocolo switchback"),
        row("PENDULUM RUN", "Run pendul", "Run péndulo", "Run pendule", "Pendel-Run", "Run pendolo", "Run pêndulo"),
        row("CROSSING SIGNAL", "Semnal crossing", "Señal cruce", "Signal croisé", "Kreuzsignal", "Segnale incrocio", "Sinal cruzado"),
        row("NEEDLE THREAD", "Fir de ac", "Hilo de aguja", "Fil d'aiguille", "Nadelöhr", "Filo d'ago", "Fio da agulha"),
        row("PULSE RELAY", "Relay pulse", "Relay pulse", "Relais pulse", "Pulse-Relais", "Relay pulse", "Relé pulse"),
        row("SPLIT DECISION", "Decizie split", "Decisión split", "Décision split", "Split-Entscheidung", "Decisione split", "Decisão split"),
        row("ORBIT VAULT", "Vault orbită", "Bóveda órbita", "Vault orbite", "Orbit-Vault", "Vault orbita", "Vault órbita"),
        row("CROSS CURRENT", "Curent încrucișat", "Corriente cruzada", "Courant croisé", "Querströmung", "Corrente incrociata", "Corrente cruzada"),
        row("PINBALL LADDER", "Scară pinball", "Escalera pinball", "Échelle pinball", "Pinball-Leiter", "Scala pinball", "Escada pinball"),
        row("TWIN CRUSHERS", "Crushere duble", "Trituradores gemelos", "Broyeurs jumeaux", "Zwillingscrusher", "Crusher gemelli", "Crushers gémeos"),
        row("MASTER CIRCUIT", "Circuit master", "Circuito master", "Circuit maître", "Master-Schaltung", "Circuito master", "Circuito master"),
        row("VORO ORBIT RIOT", "Revoltă orbită Voro", "Disturbio órbita Voro", "Riot orbite Voro", "Voro Orbit Riot", "Rivolta orbita Voro", "Motim órbita Voro"),
        row("KAV CROSSFIRE", "Crossfire Kav", "Fuego cruzado Kav", "Feu croisé Kav", "Kav Crossfire", "Fuoco incrociato Kav", "Fogo cruzado Kav"),
        row("INFINITY SLOP", "Slop infinit", "Slop infinito", "Slop infini", "Infinity Slop", "Slop infinito", "Slop infinito"),
        row("GRAVITY ROULETTE", "Ruletă gravitație", "Ruleta gravedad", "Roulette gravité", "Gravity-Roulette", "Roulette gravità", "Roleta gravidade"),
        row("KAV OVERLOAD", "Overload Kav", "Sobrecarga Kav", "Surcharge Kav", "Kav Overload", "Sovraccarico Kav", "Overload Kav"),
        row("Portal IN teleports the ball to OUT.", "Portal IN teleportează mingea la OUT.", "Portal IN teletransporta la bola a OUT.", "Portal IN téléporte la balle vers OUT.", "Portal IN teleportiert den Ball zu OUT.", "Portal IN teletrasporta la palla a OUT.", "Portal IN teleporta a bola para OUT."),
        row("The exit launches with extra speed toward goal.", "Ieșirea lansează cu viteză extra spre goal.", "La salida lanza con velocidad extra al objetivo.", "La sortie propulse vers l'objectif avec vitesse bonus.", "Der Ausgang gibt Extra-Speed zum Ziel.", "L'uscita lancia verso il goal con velocità extra.", "A saída lança com velocidade extra para o alvo."),
        row("Aim before entering; it has a short cooldown.", "Țintește înainte să intri; are cooldown scurt.", "Apunta antes de entrar; tiene cooldown corto.", "Vise avant d'entrer; cooldown court.", "Vor dem Eintritt zielen; kurzer Cooldown.", "Mira prima di entrare; ha cooldown breve.", "Aponta antes de entrar; cooldown curto."),
        row("Hold creates a rift tether.", "Hold creează tether Rift.", "Mantener crea un enlace Rift.", "Maintenir crée un lien Rift.", "Halten erzeugt einen Rift-Tether.", "Hold crea un tether Rift.", "Hold cria um tether Rift."),
        row("The ball accelerates toward your finger.", "Mingea accelerează spre degetul tău.", "La bola acelera hacia tu dedo.", "La balle accélère vers ton doigt.", "Der Ball beschleunigt zum Finger.", "La palla accelera verso il dito.", "A bola acelera para o dedo."),
        row("Release when the ball is already aimed.", "Eliberează când mingea e deja țintită.", "Suelta cuando la bola ya apunta bien.", "Relâche quand la balle est déjà alignée.", "Loslassen, wenn der Ball zielt.", "Rilascia quando la palla è già mirata.", "Solta quando a bola já está apontada."),
        row("Pulse zones are not decoration.", "Zonele pulse nu sunt decor.", "Las zonas pulse no son decoración.", "Les zones pulse ne sont pas décoratives.", "Pulse-Zonen sind keine Deko.", "Le zone pulse non sono decorazione.", "Zonas pulse não são decoração."),
        row("They push and swirl the ball inside the circle.", "Împing și rotesc mingea în cerc.", "Empujan y giran la bola dentro del círculo.", "Elles poussent et font tourner la balle.", "Sie schieben und wirbeln den Ball.", "Spingono e vorticano la palla.", "Empurram e giram a bola no círculo."),
        row("BOOST means the field is affecting you.", "BOOST înseamnă că field-ul te afectează.", "BOOST significa que el campo te afecta.", "BOOST signifie que le champ t'affecte.", "BOOST heißt: Feld wirkt auf dich.", "BOOST significa che il campo ti influenza.", "BOOST significa que o campo te afeta."),
        row("Hold behind the ball to brake.", "Ține în spatele mingii ca să frânezi.", "Mantén detrás de la bola para frenar.", "Maintiens derrière la balle pour freiner.", "Hinter dem Ball halten zum Bremsen.", "Tieni dietro la palla per frenare.", "Segura atrás da bola para travar."),
        row("Release early to coast and save rift energy.", "Eliberează devreme ca să aluneci și să salvezi energie.", "Suelta pronto para deslizar y ahorrar energía.", "Relâche tôt pour glisser et économiser.", "Früh loslassen zum Rollen und Energiesparen.", "Rilascia presto per scorrere e salvare energia.", "Solta cedo para deslizar e poupar energia."),
        row("Less rift used gives more HYPE.", "Mai puțin Rift folosit dă mai mult HYPE.", "Menos Rift usado da más HYPE.", "Moins de Rift utilisé donne plus de HYPE.", "Weniger Rift gibt mehr HYPE.", "Meno Rift usato dà più HYPE.", "Menos Rift usado dá mais HYPE."),
        row("Pink crash nodes end the run.", "Nodurile roz de crash termină run-ul.", "Los nodos rosas acaban la run.", "Les nœuds roses terminent la run.", "Pinke Crash-Knoten beenden den Run.", "I nodi crash rosa finiscono la run.", "Nós rosa de crash acabam a run."),
        row("Short tether bursts dodge better than long holds.", "Burst-urile scurte evită mai bine decât hold-urile lungi.", "Ráfagas cortas esquivan mejor que holds largos.", "Les bursts courts esquivent mieux que les longs holds.", "Kurze Tether-Bursts dodgen besser als langes Halten.", "Burst brevi evitano meglio degli hold lunghi.", "Bursts curtos desviam melhor que holds longos."),
        row("Clean dodges keep your streak alive.", "Dodge-urile curate îți țin streak-ul viu.", "Esquivas limpias mantienen la racha.", "Les esquives propres gardent la série.", "Saubere Dodges halten die Serie.", "Schivate pulite mantengono la serie.", "Desvios limpos mantêm a série."),
        row("CHAIN is your live combo.", "CHAIN este combo-ul tău live.", "CHAIN es tu combo activo.", "CHAIN est ton combo live.", "CHAIN ist deine Live-Kombo.", "CHAIN è il tuo combo live.", "CHAIN é o teu combo ao vivo."),
        row("It grows during fast rift control or boost fields.", "Crește când controlezi Rift rapid sau prin câmpuri boost.", "Crece con control Rift rápido o campos boost.", "Elle monte avec contrôle Rift rapide ou champs boost.", "Wächst durch schnelle Rift-Kontrolle oder Boost-Felder.", "Cresce con controllo Rift veloce o campi boost.", "Cresce com controlo Rift rápido ou campos boost."),
        row("Max chain adds big HYPE at finish.", "Chain maxim adaugă HYPE mare la final.", "Cadena máxima suma mucho HYPE al final.", "Chaîne max ajoute gros HYPE à l'arrivée.", "Max Chain bringt viel HYPE am Ende.", "Catena max aggiunge HYPE finale.", "Corrente máxima dá muito HYPE no fim."),
        row("Rift energy is limited.", "Energia Rift este limitată.", "La energía Rift es limitada.", "L'énergie Rift est limitée.", "Rift-Energie ist begrenzt.", "Energia Rift limitata.", "Energia Rift é limitada."),
        row("Rift Drain spends energy faster while holding.", "Rift Drain consumă energia mai rapid la hold.", "Rift Drain gasta energía más rápido al mantener.", "Rift Drain dépense plus vite pendant hold.", "Rift Drain verbraucht Energie schneller beim Halten.", "Rift Drain consuma energia più veloce in hold.", "Rift Drain gasta energia mais rápido em hold."),
        row("Use short holds, then release to recharge.", "Folosește hold-uri scurte, apoi eliberează pentru recharge.", "Usa holds cortos y suelta para recargar.", "Utilise des holds courts puis relâche pour recharger.", "Kurze Holds nutzen, dann loslassen zum Aufladen.", "Usa hold brevi, poi rilascia per ricaricare.", "Usa holds curtos e solta para recarregar."),
        row("Pulse Storm makes fields stronger.", "Pulse Storm face câmpurile mai puternice.", "Pulse Storm hace campos más fuertes.", "Pulse Storm renforce les champs.", "Pulse Storm macht Felder stärker.", "Pulse Storm rende i campi più forti.", "Pulse Storm torna campos mais fortes."),
        row("Hold can dampen the pulse when it gets wild.", "Hold poate calma pulse-ul când devine nebun.", "Hold puede calmar el pulse cuando se descontrola.", "Hold peut calmer le pulse quand il devient fou.", "Halten kann Pulse dämpfen, wenn es wild wird.", "Hold può smorzare pulse quando impazzisce.", "Hold pode reduzir pulse quando fica intenso."),
        row("Use the storm for speed, not panic.", "Folosește furtuna pentru viteză, nu panică.", "Usa la tormenta para velocidad, no pánico.", "Utilise la tempête pour la vitesse, pas la panique.", "Sturm für Tempo nutzen, nicht für Panik.", "Usa la tempesta per velocità, non panico.", "Usa a tempestade para velocidade, não pânico."),
        row("Focus Field slows the ball while holding.", "Focus Field încetinește mingea când ții.", "Focus Field frena la bola al mantener.", "Focus Field ralentit la balle en hold.", "Focus Field bremst den Ball beim Halten.", "Focus Field rallenta la palla in hold.", "Focus Field abranda a bola em hold."),
        row("Heavy Core pulls down harder.", "Heavy Core trage mai tare în jos.", "Heavy Core tira más fuerte hacia abajo.", "Heavy Core tire plus fort vers le bas.", "Heavy Core zieht stärker nach unten.", "Heavy Core tira più forte giù.", "Heavy Core puxa mais para baixo."),
        row("Use precision holds to fight gravity.", "Folosește hold-uri precise ca să lupți cu gravitația.", "Usa holds precisos contra la gravedad.", "Utilise des holds précis contre la gravité.", "Präzise Holds gegen Gravität nutzen.", "Usa hold precisi contro gravità.", "Usa holds precisos contra gravidade."),
        row("Power Hold charges stronger pull.", "Power Hold încarcă un pull mai puternic.", "Power Hold carga un tirón más fuerte.", "Power Hold charge une attraction plus forte.", "Power Hold lädt stärkeren Zug.", "Power Hold carica un tiro più forte.", "Power Hold carrega puxão mais forte."),
        row("Moon Glide keeps momentum after release.", "Moon Glide păstrează momentum după release.", "Moon Glide mantiene impulso al soltar.", "Moon Glide garde l'élan après relâche.", "Moon Glide hält Momentum nach Loslassen.", "Moon Glide mantiene slancio dopo rilascio.", "Moon Glide mantém impulso depois de soltar."),
        row("Charge, release, then coast into the exit.", "Încarcă, eliberează, apoi alunecă spre ieșire.", "Carga, suelta y desliza hacia la salida.", "Charge, relâche, puis glisse vers la sortie.", "Laden, loslassen, in den Ausgang rollen.", "Carica, rilascia, poi scivola all'uscita.", "Carrega, solta e desliza para a saída."),
        row("Wind pushes the ball sideways.", "Vântul împinge mingea lateral.", "El viento empuja la bola lateralmente.", "Le vent pousse la balle de côté.", "Wind schiebt den Ball seitlich.", "Il vento spinge la palla di lato.", "O vento empurra a bola de lado."),
        row("Overheat punishes long holds.", "Overheat pedepsește hold-urile lungi.", "Overheat castiga holds largos.", "Overheat punit les longs holds.", "Overheat bestraft langes Halten.", "Overheat punisce hold lunghi.", "Overheat pune holds longos."),
        row("Use short bursts for the tiny gate.", "Folosește burst-uri scurte pentru poarta mică.", "Usa ráfagas cortas para la puerta pequeña.", "Utilise des bursts courts pour la petite porte.", "Kurze Bursts für das kleine Tor.", "Usa burst brevi per la porta piccola.", "Usa bursts curtos para a porta pequena."),
        row("Obstacle: portals change position and speed instantly.", "Obstacol: portalurile schimbă poziția și viteza instant.", "Obstáculo: portales cambian posición y velocidad al instante.", "Obstacle : les portails changent position et vitesse instantanément.", "Hindernis: Portale ändern Position und Tempo sofort.", "Ostacolo: portali cambiano posizione e velocità subito.", "Obstáculo: portais mudam posição e velocidade instantaneamente."),
        row("Obstacle: pink crash nodes instantly fail the run.", "Obstacol: nodurile roz de crash termină run-ul instant.", "Obstáculo: nodos rosas fallan la run al instante.", "Obstacle : les nœuds roses crash font échouer la run.", "Hindernis: pinke Crash-Knoten beenden den Run.", "Ostacolo: nodi crash rosa falliscono la run.", "Obstáculo: nós rosa de crash falham a run."),
        row("Obstacle: tiny gate makes the exit much smaller.", "Obstacol: tiny gate face ieșirea mult mai mică.", "Obstáculo: tiny gate hace la salida más pequeña.", "Obstacle : tiny gate réduit fortement la sortie.", "Hindernis: Tiny Gate macht den Ausgang kleiner.", "Ostacolo: tiny gate riduce l'uscita.", "Obstáculo: tiny gate torna a saída menor."),
        row("Obstacle: platforms redirect you; pulse fields bend speed.", "Obstacol: platformele te redirecționează; câmpurile pulse curbează viteza.", "Obstáculo: plataformas redirigen; pulse curva la velocidad.", "Obstacle : plateformes redirigent; pulse courbe la vitesse.", "Hindernis: Plattformen lenken um; Pulse biegt Tempo.", "Ostacolo: piattaforme deviano; pulse piega velocità.", "Obstáculo: plataformas redirecionam; pulse dobra velocidade."),
        row("Obstacle: metal platforms block, bounce and redirect the ball.", "Obstacol: platformele metalice blochează, dau bounce și redirecționează mingea.", "Obstáculo: plataformas metálicas bloquean, rebotan y redirigen.", "Obstacle : plateformes métal bloquent, rebondissent et redirigent.", "Hindernis: Metallplattformen blocken, bouncen und lenken.", "Ostacolo: piattaforme metal bloccano, rimbalzano e deviano.", "Obstáculo: plataformas metálicas bloqueiam, rebatem e redirecionam."),
        row("Obstacle: screen edges and timer can still end the run.", "Obstacol: marginile ecranului și timerul pot încă opri run-ul.", "Obstáculo: bordes y tiempo aún pueden acabar la run.", "Obstacle : bords et chrono peuvent finir la run.", "Hindernis: Ränder und Timer können den Run beenden.", "Ostacolo: bordi e timer possono finire la run.", "Obstáculo: bordas e timer ainda podem acabar a run."),
        row("PLAYER SETUP", "Setup player", "Setup jugador", "Setup joueur", "Player-Setup", "Setup giocatore", "Setup jogador"),
        row("AGE CHECK", "Verificare vârstă", "Control de edad", "Vérification âge", "Alterscheck", "Controllo età", "Verificação de idade", pl = "WERYFIKACJA WIEKU"),
        row("Enter your age in years.", "Introdu vârsta în ani.", "Introduce tu edad en años.", "Entre ton âge en années.", "Gib dein Alter in Jahren ein.", "Inserisci l'età in anni.", "Insere a tua idade em anos.", pl = "Wpisz swój wiek w latach."),
        row("CONTINUE", "Continuă", "Continuar", "Continuer", "Weiter", "Continua", "Continuar", "Doorgaan", "Kontynuuj", "Devam", "Продолжить", "Продовжити", "متابعة", "जारी रखें", "Lanjut", "Tiếp tục", "続ける", "계속", "继续"),
        row("Only the age group is saved locally.", "Se salvează local doar grupa de vârstă.", "Solo se guarda localmente el grupo de edad.", "Seul le groupe d'âge est stocké localement.", "Nur die Altersgruppe wird lokal gespeichert.", "Si salva solo il gruppo età.", "Só o grupo etário é guardado localmente.", pl = "Lokalnie zapisywana jest tylko grupa wiekowa."),
        row("CHILD  /  TEEN  /  ADULT", "Copil / Teen / Adult", "Niño / Teen / Adulto", "Enfant / Ado / Adulte", "Kind / Teen / Erwachsen", "Bambino / Teen / Adulto", "Criança / Teen / Adulto"),
        row("ENTER YOUR AGE", "Introdu vârsta", "Introduce edad", "Entre ton âge", "Alter eingeben", "Inserisci età", "Insere a idade"),
        row("CHECK THE AGE", "Verifică vârsta", "Revisa edad", "Vérifie l'âge", "Alter prüfen", "Controlla età", "Verifica idade"),
        row("AGE", "Vârstă", "Edad", "Âge", "Alter", "Età", "Idade"),
        row("Player age", "Vârsta playerului", "Edad del jugador", "Âge du joueur", "Spieleralter", "Età giocatore", "Idade do jogador"),
        row("Privacy options are not required for this profile.", "Opțiunile privacy nu sunt necesare pentru acest profil.", "Las opciones de privacidad no son necesarias para este perfil.", "Les options de confidentialité ne sont pas requises.", "Datenschutzoptionen sind nicht erforderlich.", "Opzioni privacy non richieste.", "Opções de privacidade não são necessárias."),
        row("Privacy options are temporarily unavailable.", "Opțiunile privacy sunt temporar indisponibile.", "Privacidad no disponible temporalmente.", "Options de confidentialité indisponibles.", "Datenschutzoptionen temporär nicht verfügbar.", "Opzioni privacy temporaneamente non disponibili.", "Opções de privacidade indisponíveis."),
        row("CONNECTING TO GOOGLE PLAY", "Conectare la Google Play", "Conectando a Google Play", "Connexion à Google Play", "Verbindung zu Google Play", "Connessione a Google Play", "A ligar ao Google Play"),
        row("LOADING LOCAL PRICE", "Încarc prețul local", "Cargando precio local", "Chargement du prix local", "Lokaler Preis wird geladen", "Carico prezzo locale", "A carregar preço local"),
        row("PURCHASE RESTORED TO THE VAULT", "Achiziție restaurată în seif", "Compra restaurada en la bóveda", "Achat restauré dans le coffre", "Kauf im Vault wiederhergestellt", "Acquisto ripristinato nel vault", "Compra restaurada no cofre"),
        row("PURCHASE CANCELLED", "Achiziție anulată", "Compra cancelada", "Achat annulé", "Kauf abgebrochen", "Acquisto annullato", "Compra cancelada"),
        row("ALREADY OWNED / RESTORING", "Deja deținut / restaurez", "Ya lo tienes / restaurando", "Déjà possédé / restauration", "Bereits im Besitz / Wiederherstellung", "Già posseduto / ripristino", "Já possuído / a restaurar"),
        row("GOOGLE PLAY ERROR", "Eroare Google Play", "Error Google Play", "Erreur Google Play", "Google-Play-Fehler", "Errore Google Play", "Erro Google Play"),
        row("GOOGLE PLAY BILLING UNAVAILABLE", "Google Play Billing indisponibil", "Pagos Google Play no disponibles", "Paiement Google Play indisponible", "Google Play Billing nicht verfügbar", "Billing Google Play non disponibile", "Faturação Google Play indisponível"),
        row("PRODUCT NOT ACTIVE IN PLAY CONSOLE", "Produsul nu este activ în Play Console", "Producto no activo en Play Console", "Produit non actif dans Play Console", "Produkt nicht aktiv in Play Console", "Prodotto non attivo in Play Console", "Produto não ativo na Play Console"),
        row("RESTORE FAILED / CHECK CONNECTION", "Restaurarea a eșuat / verifică conexiunea", "Restauración fallida / revisa conexión", "Restauration échouée / vérifie connexion", "Wiederherstellung fehlgeschlagen / Verbindung prüfen", "Ripristino fallito / controlla connessione", "Restauro falhou / verifica ligação"),
        row("NO PREMIUM BRAINBALLS FOUND", "Nu am găsit Brainballs premium", "No hay Brainballs premium", "Aucune Brainball premium trouvée", "Keine Premium-Brainballs gefunden", "Nessuna Brainball premium trovata", "Nenhuma Brainball premium encontrada"),
        row("RESTORED PREMIUM BRAINBALLS", "Restaurate %d Brainballs premium", "%d Brainballs premium restauradas", "%d Brainballs premium restaurées", "%d Premium-Brainballs wiederhergestellt", "%d Brainball premium ripristinate", "%d Brainballs premium restauradas"),
        row("PURCHASE COULD NOT START", "Achiziția nu a putut porni", "No se pudo iniciar la compra", "Impossible de lancer l'achat", "Kauf konnte nicht starten", "Impossibile avviare acquisto", "Não foi possível iniciar compra"),
        row("PURCHASE SAVED / CONFIRMATION RETRYING", "Achiziție salvată / reîncerc confirmarea", "Compra guardada / reintentando confirmación", "Achat enregistré / confirmation relancée", "Kauf gespeichert / Bestätigung erneut", "Acquisto salvato / ritento conferma", "Compra guardada / nova confirmação")
    )

    /** Source-backed inventory used to detect drift from the frozen strict catalog. */
    internal val sourceKeyInventory: Set<String> by lazy {
        (copyOverrides.keys + phrases.keys).toSortedSet()
    }
}
