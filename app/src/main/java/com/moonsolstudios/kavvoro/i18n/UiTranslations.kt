package com.moonsolstudios.kavvoro.i18n

/**
 * Small, cross-screen UI vocabulary that is rendered by procedural surfaces
 * rather than by the gameplay/tutorial catalog files. Keeping this vocabulary
 * in one audited table prevents settings and navigation chrome from silently
 * falling back to English in a non-English install.
 */
internal object UiTranslations {
    val requiredKeys: Set<String> = listOf(
        "ACTIVE RUN", "BACK TO HOME", "CHOOSE YOUR MODE", "HIGHER INTENSITY", "MAXED",
        "NEW GAME", "NO ACTIVE RUN", "NO CURSE", "STANDARD RULES", "START FRESH WHEN READY",
        "START NEW GAME", "STEADY PROGRESSION", "UNLOCKS", "WILD MODIFIERS", "RESET PROGRESS?",
        "This cannot be undone.", "CANCEL", "RESET PROGRESS", "Clears gameplay only", "SETTINGS",
        "AUDIO", "MASTER VOLUME", "MUSIC VOLUME", "SOUND EFFECTS", "HAPTIC FEEDBACK",
        "Vibration on actions", "GAMEPLAY", "SCREEN SHAKE", "Shake the screen on impact",
        "PERFORMANCE MODE", "Reduce effects for smoother gameplay", "ACCOUNT & CLOUD", "ACCOUNT",
        "Manage your account and progress", "INFO & LEGAL", "LANGUAGE", "TERMS OF SERVICE", "DATA DELETION",
        "Erase all local app data", "ABOUT MOONSOL STUDIOS",
        "Collection, language and privacy are available from Settings.", "TERMS", "DATA"
    ).toSet()

    private val keys = listOf(
        "ACTIVE RUN", "ACCOUNT", "ACCOUNT & CLOUD", "AUDIO", "ABOUT MOONSOL STUDIOS",
        "BACK TO HOME", "CANCEL", "CHOOSE YOUR MODE", "Clears gameplay only", "Collection, language and privacy are available from Settings.",
        "DATA", "DATA DELETION", "Erase all local app data", "GAMEPLAY", "HAPTIC FEEDBACK",
        "HIGHER INTENSITY", "INFO & LEGAL", "Manage your account and progress", "LANGUAGE", "MASTER VOLUME", "MAXED", "MUSIC VOLUME",
        "NEW GAME", "NO ACTIVE RUN", "NO CURSE", "PERFORMANCE MODE", "Reduce effects for smoother gameplay", "RESET PROGRESS", "RESET PROGRESS?",
        "SCREEN SHAKE", "SETTINGS", "SOUND EFFECTS", "START FRESH WHEN READY", "START NEW GAME",
        "STANDARD RULES", "STEADY PROGRESSION", "TERMS", "TERMS OF SERVICE", "UNLOCKS", "Vibration on actions",
        "WILD MODIFIERS", "Shake the screen on impact", "This cannot be undone."
    )

    private fun bundle(vararg values: String): Map<String, String> {
        require(values.size == keys.size) {
            "UI translation bundle has ${values.size} values for ${keys.size} keys"
        }
        return keys.zip(values).toMap()
    }

    private val english = bundle(
        "ACTIVE RUN", "ACCOUNT", "ACCOUNT & CLOUD", "AUDIO", "ABOUT MOONSOL STUDIOS",
        "BACK TO HOME", "CANCEL", "CHOOSE YOUR MODE", "Clears gameplay only", "Collection, language and privacy are available from Settings.",
        "DATA", "DATA DELETION", "Erase all local app data", "GAMEPLAY", "HAPTIC FEEDBACK",
        "HIGHER INTENSITY", "INFO & LEGAL", "Manage your account and progress", "LANGUAGE", "MASTER VOLUME", "MAXED", "MUSIC VOLUME",
        "NEW GAME", "NO ACTIVE RUN", "NO CURSE", "PERFORMANCE MODE", "Reduce effects for smoother gameplay", "RESET PROGRESS", "RESET PROGRESS?",
        "SCREEN SHAKE", "SETTINGS", "SOUND EFFECTS", "START FRESH WHEN READY", "START NEW GAME",
        "STANDARD RULES", "STEADY PROGRESSION", "TERMS", "TERMS OF SERVICE", "UNLOCKS", "Vibration on actions",
        "WILD MODIFIERS", "Shake the screen on impact", "This cannot be undone."
    )

    private val localized: Map<KavvoroLanguage, Map<String, String>> = mapOf(
        KavvoroLanguage.EN to english,
        KavvoroLanguage.RO to bundle(
            "RUN ACTIV", "CONT", "CONT ȘI CLOUD", "SUNET", "DESPRE MOONSOL STUDIOS",
            "ÎNAPOI ACASĂ", "ANULEAZĂ", "ALEGE MODUL", "Șterge doar progresul de joc", "Colecția, limba și opțiunile de confidențialitate sunt disponibile în Setări.",
            "DATE", "ȘTERGERE DATE", "Șterge toate datele locale ale aplicației", "JOC", "FEEDBACK HAPTIC",
            "INTENSITATE MAI MARE", "INFORMAȚII ȘI LEGAL", "Gestionează contul și progresul", "LIMBĂ", "VOLUM PRINCIPAL", "MAXIM", "VOLUM MUZICĂ",
            "JOC NOU", "FĂRĂ RUN ACTIV", "FĂRĂ BLESTEM", "MOD PERFORMANȚĂ", "Reduce efectele pentru un joc mai fluid", "RESETARE PROGRES", "RESETEZI PROGRESUL?",
            "ZGUDUIRE ECRAN", "SETĂRI", "EFECTE SONORE", "ÎNCEPE DE LA ZERO CÂND EȘTI GATA", "JOC NOU",
            "REGULI STANDARD", "PROGRES CONSTANT", "TERMENI", "TERMENI ȘI CONDIȚII", "DEBLOCĂRI", "Vibrații la acțiuni",
            "MODIFICATORI HAOS", "Scutură ecranul la impact", "Acest lucru nu poate fi anulat."
        ),
        KavvoroLanguage.ES to bundle(
            "PARTIDA ACTIVA", "CUENTA", "CUENTA Y NUBE", "SONIDO", "SOBRE MOONSOL STUDIOS",
            "VOLVER AL INICIO", "CANCELAR", "ELIGE EL MODO", "Borra solo el progreso de juego", "La colección, el idioma y la privacidad están disponibles en Ajustes.",
            "DATOS", "ELIMINAR DATOS", "Borra todos los datos locales de la app", "JUGABILIDAD", "VIBRACIÓN HÁPTICA",
            "MAYOR INTENSIDAD", "INFO Y LEGAL", "Gestiona tu cuenta y progreso", "IDIOMA", "VOLUMEN PRINCIPAL", "AL MÁXIMO", "VOLUMEN DE MÚSICA",
            "NUEVA PARTIDA", "SIN PARTIDA ACTIVA", "SIN MALDICIÓN", "MODO RENDIMIENTO", "Reduce los efectos para una experiencia más fluida", "RESTABLECER PROGRESO", "¿RESTABLECER PROGRESO?",
            "VIBRACIÓN DE PANTALLA", "AJUSTES", "EFECTOS DE SONIDO", "EMPIEZA DE NUEVO CUANDO ESTÉS LISTO", "NUEVA PARTIDA",
            "REGLAS ESTÁNDAR", "PROGRESIÓN CONSTANTE", "TÉRMINOS", "TÉRMINOS Y CONDICIONES", "DESBLOQUEOS", "Vibración en las acciones",
            "MODIFICADORES SALVAJES", "Agita la pantalla al impactar", "Esta acción no se puede deshacer."
        ),
        KavvoroLanguage.FR to bundle(
            "PARTIE EN COURS", "COMPTE", "COMPTE ET CLOUD", "SON", "À PROPOS DE MOONSOL STUDIOS",
            "RETOUR À L’ACCUEIL", "ANNULER", "CHOISIS TON MODE", "Efface uniquement la progression de jeu", "La collection, la langue et la confidentialité sont accessibles depuis les Réglages.",
            "DONNÉES", "SUPPRESSION DES DONNÉES", "Efface toutes les données locales de l’application", "JOUABILITÉ", "RETOUR HAPTIQUE",
            "INTENSITÉ SUPÉRIEURE", "INFOS ET MENTIONS LÉGALES", "Gérer ton compte et ta progression", "LANGUE", "VOLUME PRINCIPAL", "MAXIMUM", "VOLUME DE LA MUSIQUE",
            "NOUVELLE PARTIE", "AUCUNE PARTIE EN COURS", "AUCUNE MALÉDICTION", "MODE PERFORMANCE", "Réduit les effets pour un jeu plus fluide", "RÉINITIALISER LA PROGRESSION", "RÉINITIALISER LA PROGRESSION ?",
            "VIBRATION DE L’ÉCRAN", "RÉGLAGES", "EFFETS SONORES", "REPARTIR DE ZÉRO QUAND TU ES PRÊT", "NOUVELLE PARTIE",
            "RÈGLES STANDARD", "PROGRESSION RÉGULIÈRE", "TERMES", "TERMES ET CONDITIONS", "DÉBLOCAGES", "Vibrations lors des actions",
            "MODIFICATEURS SAUVAGES", "Secoue l’écran à l’impact", "Cette action est irréversible."
        ),
        KavvoroLanguage.DE to bundle(
            "AKTIVER LAUF", "KONTO", "KONTO UND CLOUD", "TON", "ÜBER MOONSOL STUDIOS",
            "ZURÜCK ZUM START", "ABBRECHEN", "MODUS AUSWÄHLEN", "Löscht nur den Spielfortschritt", "Sammlung, Sprache und Datenschutz findest du in den Einstellungen.",
            "DATEN", "DATEN LÖSCHEN", "Alle lokalen App-Daten löschen", "SPIEL", "HAPTISCHES FEEDBACK",
            "HÖHERE INTENSITÄT", "INFO UND RECHTLICHES", "Konto und Fortschritt verwalten", "SPRACHE", "MASTER-LAUTSTÄRKE", "MAXIMAL", "MUSIKLAUTSTÄRKE",
            "NEUES SPIEL", "KEIN AKTIVER LAUF", "KEIN FLUCH", "LEISTUNGSMODUS", "Effekte für flüssigeres Spielen reduzieren", "FORTSCHRITT ZURÜCKSETZEN", "FORTSCHRITT ZURÜCKSETZEN?",
            "BILDSCHIRMRUCKELN", "EINSTELLUNGEN", "SOUNDEFFEKTE", "STARTE NEU, WENN DU BEREIT BIST", "NEUES SPIEL",
            "STANDARDREGELN", "STETIGER FORTSCHRITT", "BEGRIFFE", "NUTZUNGSBEDINGUNGEN", "FREISCHALTUNGEN", "Vibration bei Aktionen",
            "WILDE MODIFIKATOREN", "Bildschirm bei Aufprall bewegen", "Das kann nicht rückgängig gemacht werden."
        ),
        KavvoroLanguage.IT to bundle(
            "PARTITA ATTIVA", "PROFILO", "ACCOUNT E CLOUD", "SUONO", "CHI SIAMO",
            "TORNA ALLA HOME", "ANNULLA", "SCEGLI LA MODALITÀ", "Cancella solo i progressi di gioco", "Collezione, lingua e privacy sono disponibili nelle Impostazioni.",
            "DATI", "ELIMINA DATI", "Cancella tutti i dati locali dell’app", "GIOCO", "FEEDBACK APTICO",
            "INTENSITÀ MAGGIORE", "INFO E NOTE LEGALI", "Gestisci account e progressi", "LINGUA", "VOLUME PRINCIPALE", "AL MASSIMO", "VOLUME MUSICA",
            "NUOVA PARTITA", "NESSUNA PARTITA ATTIVA", "NESSUNA MALEDIZIONE", "MODALITÀ PRESTAZIONI", "Riduci gli effetti per un gioco più fluido", "AZZERA PROGRESSI", "AZZERARE I PROGRESSI?",
            "SCOSSA SCHERMO", "IMPOSTAZIONI", "EFFETTI SONORI", "RICOMINCIA DA ZERO QUANDO SEI PRONTO", "NUOVA PARTITA",
            "REGOLE STANDARD", "PROGRESSIONE COSTANTE", "TERMINI", "TERMINI E CONDIZIONI", "SBLOCCABILI", "Vibrazione nelle azioni",
            "MODIFICATORI SELVAGGI", "Scuoti lo schermo all’impatto", "Questa operazione non può essere annullata."
        ),
        KavvoroLanguage.PT to bundle(
            "PARTIDA ATIVA", "CONTA", "CONTA E NUVEM", "ÁUDIO", "SOBRE A MOONSOL STUDIOS",
            "VOLTAR AO INÍCIO", "CANCELAR", "ESCOLHE O MODO", "Apaga apenas o progresso do jogo", "Coleção, idioma e privacidade estão disponíveis nas Definições.",
            "DADOS", "ELIMINAR DADOS", "Apaga todos os dados locais da aplicação", "JOGABILIDADE", "FEEDBACK HÁPTICO",
            "MAIOR INTENSIDADE", "INFO E LEGAL", "Gerir conta e progresso", "IDIOMA", "VOLUME PRINCIPAL", "NO MÁXIMO", "VOLUME DA MÚSICA",
            "NOVO JOGO", "SEM PARTIDA ATIVA", "SEM MALDIÇÃO", "MODO DE DESEMPENHO", "Reduz os efeitos para uma experiência mais suave", "REPOR PROGRESSO", "REPOR O PROGRESSO?",
            "ABANÃO DO ECRÃ", "DEFINIÇÕES", "EFEITOS SONOROS", "COMEÇA DE NOVO QUANDO ESTIVERES PRONTO", "NOVO JOGO",
            "REGRAS PADRÃO", "PROGRESSÃO ESTÁVEL", "TERMOS", "TERMOS E CONDIÇÕES", "DESBLOQUEIOS", "Vibração nas ações",
            "MODIFICADORES SELVAGENS", "Abanar o ecrã no impacto", "Esta ação não pode ser anulada."
        ),
        KavvoroLanguage.NL to bundle(
            "ACTIEVE RUN", "PROFIEL", "ACCOUNT EN CLOUD", "GELUID", "OVER MOONSOL STUDIOS",
            "TERUG NAAR HOME", "ANNULEREN", "KIES JE MODUS", "Wist alleen de spelvoortgang", "Collectie, taal en privacy vind je in Instellingen.",
            "GEGEVENS", "GEGEVENS WISSEN", "Wis alle lokale appgegevens", "SPELVERLOOP", "HAPTISCHE FEEDBACK",
            "HOGERE INTENSITEIT", "INFO EN JURIDISCH", "Beheer account en voortgang", "TAAL", "HOOFDVOLUME", "MAXIMAAL", "MUZIEKVOLUME",
            "NIEUW SPEL", "GEEN ACTIEVE RUN", "GEEN VLOEK", "PRESTATIEMODUS", "Verminder effecten voor vloeiender spel", "VOORTGANG RESETTEN", "VOORTGANG RESETTEN?",
            "SCHERMBEWEGING", "INSTELLINGEN", "GELUIDSEFFECTEN", "BEGIN OPNIEUW ALS JE KLAAR BENT", "NIEUW SPEL",
            "STANDAARDREGELS", "GELIJKMATIGE VOORTGANG", "VOORWAARDEN", "ALGEMENE VOORWAARDEN", "ONTGRENDELINGEN", "Trillen bij acties",
            "WILDE MODIFIERS", "Schud het scherm bij impact", "Dit kan niet ongedaan worden gemaakt."
        ),
        KavvoroLanguage.PL to bundle(
            "AKTYWNA RUNDA", "KONTO", "KONTO I CHMURA", "DŹWIĘK", "O MOONSOL STUDIOS",
            "WRÓĆ DO MENU", "ANULUJ", "WYBIERZ TRYB", "Usuwa tylko postęp rozgrywki", "Kolekcja, język i prywatność są dostępne w Ustawieniach.",
            "DANE", "USUŃ DANE", "Usuń wszystkie lokalne dane aplikacji", "ROZGRYWKA", "WIBRACJE",
            "WIĘKSZA INTENSYWNOŚĆ", "INFORMACJE I PRAWO", "Zarządzaj kontem i postępem", "JĘZYK", "GŁOŚNOŚĆ GŁÓWNA", "MAKSIMUM", "GŁOŚNOŚĆ MUZYKI",
            "NOWA GRA", "BRAK AKTYWNEJ RUNDY", "BRAK KLĄTWY", "TRYB WYDAJNOŚCI", "Ogranicz efekty dla płynniejszej gry", "RESETUJ POSTĘP", "ZRESETOWAĆ POSTĘP?",
            "WSTRZĄSY EKRANU", "USTAWIENIA", "EFEKTY DŹWIĘKOWE", "ZACZNIJ OD NOWA, GDY BĘDZIESZ GOTOWY", "NOWA GRA",
            "ZASADY STANDARDOWE", "STAŁY POSTĘP", "WARUNKI", "REGULAMIN", "ODBLOKOWANIA", "Wibracje podczas akcji",
            "DZIKIE MODYFIKATORY", "Wstrząs ekranu przy uderzeniu", "Tej operacji nie można cofnąć."
        ),
        KavvoroLanguage.CS to bundle(
            "AKTIVNÍ BĚH", "ÚČET", "ÚČET A CLOUD", "ZVUK", "O MOONSOL STUDIOS",
            "ZPĚT DOMŮ", "ZRUŠIT", "VYBER REŽIM", "Vymaže pouze herní postup", "Kolekce, jazyk a soukromí jsou v Nastavení.",
            "ÚDAJE", "SMAZAT DATA", "Smaže všechna místní data aplikace", "HRA", "HAPTICKÁ ODEZVA",
            "VYŠŠÍ INTENZITA", "INFO A PRÁVO", "Spravovat účet a postup", "JAZYK", "HLAVNÍ HLASITOST", "MAXIMUM", "HLASITOST HUDBY",
            "NOVÁ HRA", "ŽÁDNÝ AKTIVNÍ BĚH", "ŽÁDNÁ KLETBA", "VÝKONNOSTNÍ REŽIM", "Omezit efekty pro plynulejší hru", "RESETOVAT POSTUP", "RESETOVAT POSTUP?",
            "OTŘES OBRAZOVKY", "NASTAVENÍ", "ZVUKOVÉ EFEKTY", "ZAČNI ZNOVU, AŽ BUDEŠ PŘIPRAVEN", "NOVÁ HRA",
            "STANDARDNÍ PRAVIDLA", "STÁLÝ POSTUP", "PODMÍNKY", "OBCHODNÍ PODMÍNKY", "ODBLOKOVÁNÍ", "Vibrace při akcích",
            "DIVOKÉ MODIFIKÁTORY", "Otřes obrazovky při nárazu", "Tuto akci nelze vrátit."
        ),
        KavvoroLanguage.SV to bundle(
            "AKTIV RUNDA", "KONTO", "KONTO OCH MOLN", "LJUD", "OM MOONSOL STUDIOS",
            "TILLBAKA HEM", "AVBRYT", "VÄLJ LÄGE", "Rensar bara spelframsteg", "Samling, språk och sekretess finns i Inställningar.",
            "UPPGIFTER", "RADERA DATA", "Radera all lokal appdata", "SPEL", "HAPTISK FEEDBACK",
            "HÖGRE INTENSITET", "INFO OCH JURIDIK", "Hantera konto och framsteg", "SPRÅK", "HUVUDVOLYM", "MAXAT", "MUSIKVOLYM",
            "NYTT SPEL", "INGEN AKTIV RUNDA", "INGEN FÖRBANNELSE", "PRESTANDALÄGE", "Minska effekter för smidigare spel", "ÅTERSTÄLL FRAMSTEG", "ÅTERSTÄLLA FRAMSTEG?",
            "SKÄRMSKAKNING", "INSTÄLLNINGAR", "LJUDEFFEKTER", "BÖRJA OM NÄR DU ÄR REDO", "NYTT SPEL",
            "STANDARDREGLER", "JÄMN UTVECKLING", "VILLKOR", "VILLKOR", "UPPLÅSNINGAR", "Vibration vid åtgärder",
            "VILDA MODIFIERARE", "Skaka skärmen vid kollision", "Detta kan inte ångras."
        ),
        KavvoroLanguage.FI to bundle(
            "AKTIIVINEN PELI", "TILI", "TILI JA PILVI", "ÄÄNI", "MOONSOL STUDIOS - TIETOJA",
            "TAKAISIN KOTIIN", "PERUUTA", "VALITSE TILA", "Tyhjentää vain pelin edistymisen", "Kokoelma, kieli ja yksityisyys löytyvät asetuksista.",
            "TIEDOT", "POISTA TIEDOT", "Poista kaikki sovelluksen paikalliset tiedot", "PELI", "HAPTINEN PALAUTE",
            "SUUREMPI INTENSITEETTI", "INFO JA LAKI", "Hallitse tiliä ja edistymistä", "KIELI", "PÄÄÄÄNENVOIMAKKUUS", "TÄYSI", "MUSIIKIN ÄÄNENVOIMAKKUUS",
            "UUSI PELI", "EI AKTIIVISTA PELIÄ", "EI KIROUSTA", "SUORITUSKYKYTILA", "Vähennä tehosteita sujuvampaa peliä varten", "NOLLAA EDISTYMINEN", "NOLLATAANKO EDISTYMINEN?",
            "NÄYTÖN TÄRINÄ", "ASETUKSET", "ÄÄNITEHOSTEET", "ALOITA ALUSTA, KUN OLET VALMIS", "UUSI PELI",
            "VAKIOSÄÄNNÖT", "TASAINEN EDISTYMINEN", "EHDOT", "KÄYTTÖEHDOT", "AVAUKSET", "Värinä toimissa",
            "VILLIÄ MUUNTAJIA", "Tärisytä näyttöä törmäyksessä", "Tätä ei voi kumota."
        ),
        KavvoroLanguage.TR to bundle(
            "AKTİF OYUN", "HESAP", "HESAP VE BULUT", "SES", "MOONSOL STUDIOS HAKKINDA",
            "ANA MENÜYE DÖN", "İPTAL", "MODUNU SEÇ", "Yalnızca oyun ilerlemesini temizler", "Koleksiyon, dil ve gizlilik Ayarlar'da bulunur.",
            "VERİ", "VERİLERİ SİL", "Tüm yerel uygulama verilerini sil", "OYUN", "HAPTİK GERİ BİLDİRİM",
            "DAHA YÜKSEK YOĞUNLUK", "BİLGİ VE HUKUK", "Hesabı ve ilerlemeyi yönet", "DİL", "ANA SES", "MAKSİMUM", "MÜZİK SESİ",
            "YENİ OYUN", "AKTİF OYUN YOK", "LANET YOK", "PERFORMANS MODU", "Daha akıcı oyun için efektleri azalt", "İLERLEMEYİ SIFIRLA", "İLERLEME SIFIRLANSIN MI?",
            "EKRAN SARSINTISI", "AYARLAR", "SES EFEKTLERİ", "HAZIR OLDUĞUNDA YENİDEN BAŞLA", "YENİ OYUN",
            "STANDART KURALLAR", "DÜZENLİ İLERLEME", "ŞARTLAR", "HİZMET ŞARTLARI", "KİLİT AÇMALAR", "Eylemlerde titreşim",
            "ÇILGIN DEĞİŞTİRİCİLER", "Darbede ekranı salla", "Bu işlem geri alınamaz."
        ),
        KavvoroLanguage.RU to bundle(
            "АКТИВНАЯ ИГРА", "АККАУНТ", "АККАУНТ И ОБЛАКО", "ЗВУК", "О MOONSOL STUDIOS",
            "НАЗАД НА ГЛАВНУЮ", "ОТМЕНА", "ВЫБЕРИТЕ РЕЖИМ", "Очищает только игровой прогресс", "Коллекция, язык и конфиденциальность доступны в настройках.",
            "ДАННЫЕ", "УДАЛЕНИЕ ДАННЫХ", "Удалить все локальные данные приложения", "ИГРА", "ТАКТИЛЬНАЯ ОТДАЧА",
            "ВЫСОКАЯ ИНТЕНСИВНОСТЬ", "ИНФОРМАЦИЯ И ПРАВО", "Управление аккаунтом и прогрессом", "ЯЗЫК", "ОБЩАЯ ГРОМКОСТЬ", "МАКСИМУМ", "ГРОМКОСТЬ МУЗЫКИ",
            "НОВАЯ ИГРА", "НЕТ АКТИВНОЙ ИГРЫ", "БЕЗ ПРОКЛЯТИЯ", "РЕЖИМ ПРОИЗВОДИТЕЛЬНОСТИ", "Уменьшить эффекты для плавной игры", "СБРОСИТЬ ПРОГРЕСС", "СБРОСИТЬ ПРОГРЕСС?",
            "ДРОЖАНИЕ ЭКРАНА", "НАСТРОЙКИ", "ЗВУКОВЫЕ ЭФФЕКТЫ", "НАЧНИТЕ ЗАНОВО, КОГДА БУДЕТЕ ГОТОВЫ", "НОВАЯ ИГРА",
            "СТАНДАРТНЫЕ ПРАВИЛА", "РАВНОМЕРНЫЙ ПРОГРЕСС", "УСЛОВИЯ", "УСЛОВИЯ ИСПОЛЬЗОВАНИЯ", "РАЗБЛОКИРОВКИ", "Вибрация при действиях",
            "ДИКИЕ МОДИФИКАТОРЫ", "Трясти экран при ударе", "Это действие нельзя отменить."
        ),
        KavvoroLanguage.UK to bundle(
            "АКТИВНА ГРА", "ОБЛІКОВИЙ ЗАПИС", "ОБЛІКОВИЙ ЗАПИС І ХМАРА", "ЗВУК", "ПРО MOONSOL STUDIOS",
            "НАЗАД ДО ГОЛОВНОЇ", "СКАСУВАТИ", "ОБЕРІТЬ РЕЖИМ", "Очищає лише ігровий прогрес", "Колекція, мова та приватність доступні в Налаштуваннях.",
            "ДАНІ", "ВИДАЛЕННЯ ДАНИХ", "Видалити всі локальні дані застосунку", "ГРА", "ТАКТИЛЬНИЙ ВІДГУК",
            "ВИЩА ІНТЕНСИВНІСТЬ", "ІНФОРМАЦІЯ ТА ПРАВО", "Керувати обліковим записом і прогресом", "МОВА", "ГОЛОВНА ГУЧНІСТЬ", "МАКСИМУМ", "ГУЧНІСТЬ МУЗИКИ",
            "НОВА ГРА", "НЕМАЄ АКТИВНОЇ ГРИ", "БЕЗ ПРОКЛЯТТЯ", "РЕЖИМ ПРОДУКТИВНОСТІ", "Зменшити ефекти для плавнішої гри", "СКИНУТИ ПРОГРЕС", "СКИНУТИ ПРОГРЕС?",
            "ТРУСІННЯ ЕКРАНА", "НАЛАШТУВАННЯ", "ЗВУКОВІ ЕФЕКТИ", "ПОЧНІТЬ СПОЧАТКУ, КОЛИ БУДЕТЕ ГОТОВІ", "НОВА ГРА",
            "СТАНДАРТНІ ПРАВИЛА", "СТАЛИЙ ПРОГРЕС", "УМОВИ", "УМОВИ КОРИСТУВАННЯ", "РОЗБЛОКУВАННЯ", "Вібрація під час дій",
            "ДИКІ МОДИФІКАТОРИ", "Трусіть екран під час удару", "Цю дію неможливо скасувати."
        ),
        KavvoroLanguage.AR to bundle(
            "جولة نشطة", "الحساب", "الحساب والسحابة", "الصوت", "حول MOONSOL STUDIOS",
            "العودة إلى الرئيسية", "إلغاء", "اختر الوضع", "يمسح تقدم اللعب فقط", "المجموعة واللغة والخصوصية متاحة من الإعدادات.",
            "البيانات", "حذف البيانات", "مسح كل بيانات التطبيق المحلية", "اللعب", "الاهتزاز اللمسي",
            "شدة أعلى", "معلومات وقانون", "إدارة الحساب والتقدم", "اللغة", "مستوى الصوت الرئيسي", "مكتمل", "مستوى صوت الموسيقى",
            "لعبة جديدة", "لا توجد جولة نشطة", "بلا لعنة", "وضع الأداء", "تقليل المؤثرات للعب أكثر سلاسة", "إعادة ضبط التقدم", "إعادة ضبط التقدم؟",
            "اهتزاز الشاشة", "الإعدادات", "المؤثرات الصوتية", "ابدأ من جديد عندما تكون مستعدًا", "لعبة جديدة",
            "القواعد القياسية", "تقدم ثابت", "الشروط", "الشروط والأحكام", "عمليات الفتح", "اهتزاز عند الإجراءات",
            "معدلات فوضوية", "اهتز الشاشة عند الاصطدام", "لا يمكن التراجع عن هذا الإجراء."
        ),
        KavvoroLanguage.HI to bundle(
            "सक्रिय रन", "खाता", "खाता और क्लाउड", "ऑडियो", "MOONSOL STUDIOS के बारे में",
            "होम पर वापस", "रद्द करें", "मोड चुनें", "सिर्फ गेमप्ले प्रगति साफ़ करता है", "कलेक्शन, भाषा और प्राइवेसी सेटिंग्स में उपलब्ध हैं।",
            "डेटा", "डेटा हटाएँ", "सारा स्थानीय ऐप डेटा मिटाएँ", "गेमप्ले", "हैप्टिक फीडबैक",
            "अधिक तीव्रता", "जानकारी और कानूनी", "खाता और प्रगति प्रबंधित करें", "भाषा", "मास्टर वॉल्यूम", "अधिकतम", "संगीत वॉल्यूम",
            "नया गेम", "कोई सक्रिय रन नहीं", "कोई अभिशाप नहीं", "परफॉर्मेंस मोड", "गेम को सुगम बनाने के लिए प्रभाव कम करें", "प्रगति रीसेट करें", "प्रगति रीसेट करें?",
            "स्क्रीन हिलना", "सेटिंग्स", "साउंड इफेक्ट्स", "तैयार होने पर नए सिरे से शुरू करें", "नया गेम",
            "मानक नियम", "स्थिर प्रगति", "शर्तें", "नियम और शर्तें", "अनलॉक", "क्रियाओं पर कंपन",
            "वाइल्ड मॉडिफ़ायर", "टक्कर पर स्क्रीन हिलाएँ", "इसे पूर्ववत नहीं किया जा सकता।"
        ),
        KavvoroLanguage.TH to bundle(
            "รอบที่กำลังเล่น", "บัญชี", "บัญชีและคลาวด์", "เสียง", "เกี่ยวกับ MOONSOL STUDIOS",
            "กลับหน้าหลัก", "ยกเลิก", "เลือกโหมด", "ล้างเฉพาะความคืบหน้าในเกม", "คอลเลกชัน ภาษา และความเป็นส่วนตัวอยู่ในการตั้งค่า",
            "ข้อมูล", "ลบข้อมูล", "ลบข้อมูลแอปในเครื่องทั้งหมด", "เกมเพลย์", "การตอบสนองแบบสั่น",
            "ความเข้มข้นสูงขึ้น", "ข้อมูลและกฎหมาย", "จัดการบัญชีและความคืบหน้า", "ภาษา", "ระดับเสียงหลัก", "เต็มแล้ว", "ระดับเสียงเพลง",
            "เกมใหม่", "ไม่มีรอบที่กำลังเล่น", "ไม่มีคำสาป", "โหมดประสิทธิภาพ", "ลดเอฟเฟกต์เพื่อการเล่นที่ลื่นไหล", "รีเซ็ตความคืบหน้า", "รีเซ็ตความคืบหน้าไหม",
            "สั่นหน้าจอ", "การตั้งค่า", "เอฟเฟกต์เสียง", "เริ่มใหม่เมื่อพร้อม", "เกมใหม่",
            "กฎมาตรฐาน", "ความคืบหน้าแบบต่อเนื่อง", "ข้อกำหนด", "ข้อกำหนดและเงื่อนไข", "การปลดล็อก", "สั่นเมื่อทำงาน",
            "ตัวปรับแต่งสุดโหด", "สั่นหน้าจอเมื่อกระแทก", "การดำเนินการนี้ย้อนกลับไม่ได้"
        ),
        KavvoroLanguage.ID to bundle(
            "PERMAINAN AKTIF", "AKUN", "AKUN DAN CLOUD", "SUARA", "TENTANG MOONSOL STUDIOS",
            "KEMBALI KE BERANDA", "BATAL", "PILIH MODE", "Hanya menghapus progres permainan", "Koleksi, bahasa, dan privasi tersedia di Pengaturan.",
            "DATA PRIBADI", "HAPUS DATA", "Hapus semua data aplikasi lokal", "PERMAINAN", "UMPAN BALIK HAPTIK",
            "INTENSITAS LEBIH TINGGI", "INFO DAN HUKUM", "Kelola akun dan progres", "BAHASA", "VOLUME UTAMA", "MAKSIMAL", "VOLUME MUSIK",
            "PERMAINAN BARU", "TIDAK ADA PERMAINAN AKTIF", "TANPA KUTUKAN", "MODE PERFORMA", "Kurangi efek agar permainan lebih lancar", "RESET PROGRES", "RESET PROGRES?",
            "GOYANGAN LAYAR", "PENGATURAN", "EFEK SUARA", "MULAI DARI AWAL SAAT SIAP", "PERMAINAN BARU",
            "ATURAN STANDAR", "PROGRES STABIL", "KETENTUAN", "SYARAT DAN KETENTUAN", "PEMBUKAAN", "Getaran saat beraksi",
            "MODIFIKATOR LIAR", "Goyangkan layar saat benturan", "Tindakan ini tidak dapat dibatalkan."
        ),
        KavvoroLanguage.VI to bundle(
            "VÁN ĐANG CHƠI", "TÀI KHOẢN", "TÀI KHOẢN VÀ ĐÁM MÂY", "ÂM THANH", "VỀ MOONSOL STUDIOS",
            "VỀ TRANG CHỦ", "HỦY", "CHỌN CHẾ ĐỘ", "Chỉ xóa tiến trình chơi", "Bộ sưu tập, ngôn ngữ và quyền riêng tư nằm trong Cài đặt.",
            "DỮ LIỆU", "XÓA DỮ LIỆU", "Xóa mọi dữ liệu cục bộ của ứng dụng", "TRÒ CHƠI", "PHẢN HỒI RUNG",
            "CƯỜNG ĐỘ CAO HƠN", "THÔNG TIN VÀ PHÁP LÝ", "Quản lý tài khoản và tiến trình", "NGÔN NGỮ", "ÂM LƯỢNG CHÍNH", "TỐI ĐA", "ÂM LƯỢNG NHẠC",
            "VÁN MỚI", "KHÔNG CÓ VÁN ĐANG CHƠI", "KHÔNG CÓ LỜI NGUYỀN", "CHẾ ĐỘ HIỆU NĂNG", "Giảm hiệu ứng để chơi mượt hơn", "ĐẶT LẠI TIẾN TRÌNH", "ĐẶT LẠI TIẾN TRÌNH?",
            "RUNG MÀN HÌNH", "CÀI ĐẶT", "HIỆU ỨNG ÂM THANH", "BẮT ĐẦU LẠI KHI SẴN SÀNG", "VÁN MỚI",
            "LUẬT TIÊU CHUẨN", "TIẾN TRÌNH ỔN ĐỊNH", "ĐIỀU KHOẢN", "ĐIỀU KHOẢN DỊCH VỤ", "MỞ KHÓA", "Rung khi thao tác",
            "BỘ BIẾN ĐỔI HỖN LOẠN", "Rung màn hình khi va chạm", "Thao tác này không thể hoàn tác."
        ),
        KavvoroLanguage.JA to bundle(
            "プレイ中", "アカウント", "アカウントとクラウド", "オーディオ", "MOONSOL STUDIOSについて",
            "ホームに戻る", "キャンセル", "モードを選択", "ゲームプレイのみ消去します", "コレクション・言語・プライバシーは設定から利用できます。",
            "データ", "データを削除", "アプリのローカルデータをすべて消去", "ゲームプレイ", "触覚フィードバック",
            "高い強度", "情報と法務", "アカウントと進行状況を管理", "言語", "マスター音量", "最大", "音楽音量",
            "新しいゲーム", "アクティブなプレイなし", "呪いなし", "パフォーマンスモード", "滑らかなプレイのため効果を減らす", "進行状況をリセット", "進行状況をリセットしますか？",
            "画面揺れ", "設定", "サウンドエフェクト", "準備ができたら最初から開始", "新しいゲーム",
            "標準ルール", "安定した進行", "規約", "利用規約", "アンロック", "操作時の振動",
            "ワイルドモディファイア", "衝撃時に画面を揺らす", "この操作は取り消せません。"
        ),
        KavvoroLanguage.KO to bundle(
            "진행 중인 게임", "계정", "계정 및 클라우드", "오디오", "MOONSOL STUDIOS 소개",
            "홈으로 돌아가기", "취소", "모드 선택", "게임플레이만 삭제합니다", "컬렉션, 언어, 개인정보 보호는 설정에서 이용할 수 있습니다.",
            "데이터", "데이터 삭제", "앱의 모든 로컬 데이터 삭제", "게임플레이", "햅틱 피드백",
            "더 높은 강도", "정보 및 법률", "계정 및 진행 상황 관리", "언어", "마스터 볼륨", "최대", "음악 볼륨",
            "새 게임", "진행 중인 게임 없음", "저주 없음", "성능 모드", "더 부드러운 플레이를 위해 효과 줄이기", "진행 상황 초기화", "진행 상황을 초기화할까요?",
            "화면 흔들림", "설정", "음향 효과", "준비되면 새로 시작", "새 게임",
            "기본 규칙", "꾸준한 진행", "약관", "서비스 약관", "잠금 해제", "동작 시 진동",
            "와일드 변형", "충돌 시 화면 흔들기", "이 작업은 취소할 수 없습니다."
        ),
        KavvoroLanguage.ZH to bundle(
            "进行中的游戏", "账户", "账户与云端", "音频", "关于 MOONSOL STUDIOS",
            "返回主页", "取消", "选择模式", "仅清除游戏进度", "收藏、语言和隐私选项可在设置中使用。",
            "数据", "删除数据", "删除应用的所有本地数据", "游戏玩法", "触觉反馈",
            "更高强度", "信息与法律", "管理账户和进度", "语言", "主音量", "已达上限", "音乐音量",
            "新游戏", "没有进行中的游戏", "无诅咒", "性能模式", "减少效果以获得更流畅的游戏", "重置进度", "要重置进度吗？",
            "屏幕震动", "设置", "音效", "准备好后重新开始", "新游戏",
            "标准规则", "稳定推进", "条款", "服务条款", "解锁内容", "操作时振动",
            "狂野修正", "碰撞时震动屏幕", "此操作无法撤销。"
        ),
        KavvoroLanguage.ZH_TW to bundle(
            "進行中的遊戲", "帳戶", "帳戶與雲端", "音訊", "關於 MOONSOL STUDIOS",
            "返回首頁", "取消", "選擇模式", "僅清除遊戲進度", "收藏、語言與隱私選項可在設定中使用。",
            "資料", "刪除資料", "刪除應用程式的所有本機資料", "遊戲玩法", "觸覺回饋",
            "更高強度", "資訊與法律", "管理帳戶與進度", "語言", "主音量", "已達上限", "音樂音量",
            "新遊戲", "沒有進行中的遊戲", "無詛咒", "效能模式", "減少效果以獲得更流暢的遊戲", "重設進度", "要重設進度嗎？",
            "螢幕震動", "設定", "音效", "準備好後重新開始", "新遊戲",
            "標準規則", "穩定推進", "條款", "服務條款", "解鎖內容", "操作時震動",
            "狂野修正", "碰撞時震動螢幕", "此操作無法復原。"
        )
    )

    fun forLanguage(language: KavvoroLanguage): Map<String, String> =
        localized[language] ?: english
}
