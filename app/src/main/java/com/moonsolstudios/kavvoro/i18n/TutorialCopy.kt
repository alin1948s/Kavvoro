package com.moonsolstudios.kavvoro.i18n

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
        "START LEVEL"
    )

    val fieldLabelKeys = setOf(
        "TAP",
        "SHORT TAP",
        "SLOW TAP",
        "TAP BURST",
        "POWER TAP",
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

    val renderedKeyInventory: Set<String> =
        chromeKeys + fieldLabelKeys + levelTitleKeys + lessonKeys + obstacleKeys

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

    private val rows = mapOf(
        requiredRow(
            "TIME", "Timp", "Tiempo", "Temps", "Zeit", "Tempo", "Tempo",
            "Tijd", "Czas", "Süre", "Время", "Час", "الوقت", "समय",
            "Waktu", "Thời gian", "時間", "시간", "时间"
        ),
        requiredRow(
            "CHAIN", "Lanț", "Cadena", "Chaîne", "Kette", "Catena", "Corrente",
            "Keten", "Łańcuch", "Zincir", "Цепь", "Ланцюг", "السلسلة", "श्रृंखला",
            "Rantai", "Chuỗi", "チェイン", "체인", "连锁"
        ),
        requiredRow(
            "RIFT ENERGY", "Energie Rift", "Energía Rift", "Énergie Rift", "Rift-Energie",
            "Energia Rift", "Energia Rift", "Rift-energie", "Energia Rift", "Rift enerjisi",
            "Энергия Rift", "Енергія Rift", "طاقة Rift", "Rift ऊर्जा", "Energi Rift",
            "Năng lượng Rift", "Rift エネルギー", "Rift 에너지", "Rift 能量"
        ),
        requiredRow(
            "TRAINING", "Antrenament", "Entrenamiento", "Entraînement", "Training",
            "Addestramento", "Treino", "Training", "Trening", "Eğitim", "Обучение",
            "Навчання", "التدريب", "प्रशिक्षण", "Latihan", "Huấn luyện", "トレーニング",
            "훈련", "训练"
        ),
        requiredRow(
            "RIFT MODULE", "Modul Rift", "Módulo Rift", "Module Rift", "Rift-Modul",
            "Modulo Rift", "Módulo Rift", "Rift-module", "Moduł Rift", "Rift modülü",
            "Модуль Rift", "Модуль Rift", "وحدة Rift", "Rift मॉड्यूल", "Modul Rift",
            "Mô-đun Rift", "Rift モジュール", "Rift 모듈", "Rift 模块"
        ),
        requiredRow(
            "NO ADS IN TRAINING", "Fără reclame în antrenament", "Sin anuncios en el entrenamiento",
            "Aucune pub pendant l'entraînement", "Keine Werbung im Training",
            "Nessuna pubblicità nell'addestramento", "Sem anúncios no treino",
            "Geen advertenties in training", "Bez reklam w treningu", "Eğitimde reklam yok",
            "Без рекламы в обучении", "Без реклами в навчанні", "لا إعلانات أثناء التدريب",
            "प्रशिक्षण में कोई विज्ञापन नहीं", "Tanpa iklan saat latihan", "Không quảng cáo khi huấn luyện",
            "トレーニング中は広告なし", "훈련 중 광고 없음", "训练中无广告"
        ),
        requiredRow(
            "L10 UNLOCKS VORO GRAD", "L10 deblochează Voro Grad", "L10 desbloquea Voro Grad",
            "L10 débloque Voro Grad", "L10 schaltet Voro Grad frei", "L10 sblocca Voro Grad",
            "L10 desbloqueia Voro Grad", "L10 ontgrendelt Voro Grad", "L10 odblokowuje Voro Grad",
            "L10, Voro Grad'i açar", "L10 открывает Voro Grad", "L10 відкриває Voro Grad",
            "L10 يفتح Voro Grad", "L10 पर Voro Grad अनलॉक", "L10 membuka Voro Grad",
            "L10 mở khóa Voro Grad", "L10でVoro Gradを解放", "L10에서 Voro Grad 해금", "L10 解锁 Voro Grad"
        ),
        requiredRow(
            "TRAINING REWARD READY", "Recompensa de antrenament e gata", "Recompensa de entrenamiento lista",
            "Récompense d'entraînement prête", "Trainingsbelohnung bereit", "Ricompensa addestramento pronta",
            "Recompensa de treino pronta", "Trainingsbeloning klaar", "Nagroda treningowa gotowa",
            "Eğitim ödülü hazır", "Награда за обучение готова", "Нагорода за навчання готова",
            "مكافأة التدريب جاهزة", "प्रशिक्षण इनाम तैयार", "Hadiah latihan siap",
            "Phần thưởng huấn luyện đã sẵn sàng", "トレーニング報酬の準備完了", "훈련 보상 준비 완료", "训练奖励已就绪"
        ),
        requiredRow(
            "START LEVEL", "Începe nivelul", "Iniciar nivel", "Commencer le niveau", "Level starten",
            "Avvia livello", "Iniciar nível", "Level starten", "Rozpocznij poziom", "Seviyeyi başlat",
            "Начать уровень", "Почати рівень", "ابدأ المستوى", "लेवल शुरू करें", "Mulai level",
            "Bắt đầu cấp độ", "レベル開始", "레벨 시작", "开始关卡"
        ),

        requiredRow(
            "TAP", "Atinge", "Toca", "Touche", "Tippen", "Tocca", "Toque", "Tik",
            "Dotknij", "Dokun", "Нажать", "Торкнутися", "اضغط", "टैप", "Ketuk", "Chạm",
            "タップ", "탭", "点击"
        ),
        requiredRow(
            "SHORT TAP", "Atingere scurtă", "Toque corto", "Touche brève", "Kurz tippen",
            "Tocco breve", "Toque curto", "Korte tik", "Krótkie stuknięcie", "Kısa dokun",
            "Короткое нажатие", "Короткий дотик", "ضغطة قصيرة", "छोटा टैप", "Ketuk singkat",
            "Chạm ngắn", "短くタップ", "짧게 탭", "短按"
        ),
        requiredRow(
            "SLOW TAP", "Atingere lentă", "Toque lento", "Touche lente", "Langsam tippen",
            "Tocco lento", "Toque lento", "Langzame tik", "Wolne stuknięcie", "Yavaş dokun",
            "Медленное нажатие", "Повільний дотик", "ضغطة بطيئة", "धीमा टैप", "Ketuk pelan",
            "Chạm chậm", "ゆっくりタップ", "천천히 탭", "慢点按"
        ),
        requiredRow(
            "TAP BURST", "Serie de atingeri", "Ráfaga de toques", "Rafale de touches", "Tipp-Salve",
            "Raffica di tocchi", "Rajada de toques", "Tikreeks", "Seria stuknięć", "Dokunma serisi",
            "Серия нажатий", "Серія дотиків", "دفعة نقرات", "तेज़ टैप", "Rentetan ketukan",
            "Loạt chạm", "連続タップ", "연속 탭", "连续点击"
        ),
        requiredRow(
            "POWER TAP", "Atingere puternică", "Toque potente", "Touche puissante", "Kraft-Tipp",
            "Tocco potente", "Toque potente", "Krachttik", "Mocne stuknięcie", "Güçlü dokun",
            "Сильное нажатие", "Сильний дотик", "ضغطة قوية", "शक्तिशाली टैप", "Ketuk kuat",
            "Chạm mạnh", "強力タップ", "강력 탭", "强力点击"
        ),
        requiredRow(
            "BOOST", "BOOST", "BOOST", "BOOST", "BOOST", "BOOST", "BOOST", "BOOST", "BOOST",
            "BOOST", "BOOST", "BOOST", "BOOST", "BOOST", "BOOST", "BOOST", "BOOST", "BOOST", "BOOST"
        ),
        requiredRow(
            "CRASH", "Impact", "Choque", "Impact", "Crash", "Impatto", "Colisão", "Botsing",
            "Zderzenie", "Çarpışma", "Авария", "Зіткнення", "اصطدام", "टक्कर", "Tabrakan",
            "Va chạm", "クラッシュ", "충돌", "撞击"
        ),
        requiredRow(
            "AVOID", "Evită", "Evita", "Évite", "Meiden", "Evita", "Evite", "Vermijd",
            "Unikaj", "Kaçın", "Избегать", "Уникай", "تجنب", "बचें", "Hindari", "Tránh",
            "回避", "피하기", "避开"
        ),
        requiredRow(
            "WALL", "Perete", "Muro", "Mur", "Wand", "Muro", "Parede", "Muur", "Ściana",
            "Duvar", "Стена", "Стіна", "جدار", "दीवार", "Dinding", "Tường", "壁", "벽", "墙壁"
        ),
        requiredRow(
            "BOUNCE WALL", "Perete de ricoșeu", "Muro de rebote", "Mur de rebond", "Prallwand",
            "Muro di rimbalzo", "Parede de ressalto", "Stuitermuur", "Ściana odbijająca", "Sekme duvarı",
            "Отбойная стена", "Відбивна стіна", "جدار ارتداد", "उछाल दीवार", "Dinding pantul",
            "Tường nảy", "反射壁", "반사 벽", "反弹墙"
        ),
        requiredRow(
            "EXIT", "Ieșire", "Salida", "Sortie", "Ausgang", "Uscita", "Saída", "Uitgang",
            "Wyjście", "Çıkış", "Выход", "Вихід", "المخرج", "निकास", "Keluar", "Lối ra",
            "出口", "출구", "出口"
        ),
        requiredRow(
            "TINY EXIT", "Ieșire mică", "Salida pequeña", "Petite sortie", "Kleiner Ausgang",
            "Uscita piccola", "Saída pequena", "Kleine uitgang", "Małe wyjście", "Küçük çıkış",
            "Малый выход", "Малий вихід", "مخرج صغير", "छोटा निकास", "Pintu keluar kecil",
            "Lối ra nhỏ", "小さな出口", "작은 출구", "小出口"
        ),
        requiredRow(
            "GLIDE", "Planare", "Planeo", "Glisse", "Gleiten", "Planata", "Planar", "Glijden",
            "Szybowanie", "Süzül", "Планирование", "Ковзання", "انزلاق", "फिसलें", "Meluncur",
            "Lướt", "滑空", "활공", "滑行"
        ),
        requiredRow(
            "PORTAL", "Portal", "Portal", "Portail", "Portal", "Portale", "Portal", "Portaal",
            "Portal", "Portal", "Портал", "Портал", "بوابة", "पोर्टल", "Portal", "Cổng dịch chuyển",
            "ポータル", "포털", "传送门"
        ),
        requiredRow(
            "PORTAL IN", "Portal IN", "Portal IN", "Portail IN", "Portal IN", "Portale IN", "Portal IN",
            "Portaal IN", "Portal IN", "Portal IN", "Портал IN", "Портал IN", "بوابة IN", "पोर्टल IN",
            "Portal IN", "Cổng IN", "ポータル IN", "포털 IN", "传送门 IN"
        ),
        requiredRow(
            "PORTAL OUT", "Portal OUT", "Portal OUT", "Portail OUT", "Portal OUT", "Portale OUT", "Portal OUT",
            "Portaal OUT", "Portal OUT", "Portal OUT", "Портал OUT", "Портал OUT", "بوابة OUT", "पोर्टल OUT",
            "Portal OUT", "Cổng OUT", "ポータル OUT", "포털 OUT", "传送门 OUT"
        ),

        requiredRow(
            "RIFT TOUCH", "Atingere Rift", "Toque Rift", "Toucher Rift", "Rift-Berührung", "Tocco Rift",
            "Toque Rift", "Rift-aanraking", "Dotyk Rift", "Rift dokunuşu", "Касание Rift",
            "Дотик Rift", "لمسة Rift", "Rift स्पर्श", "Sentuhan Rift", "Chạm Rift", "Rift タッチ",
            "Rift 터치", "Rift 触碰"
        ),
        requiredRow(
            "ORBIT CURVE", "Curbă orbitală", "Curva orbital", "Courbe orbitale", "Orbitkurve",
            "Curva orbitale", "Curva orbital", "Baanbocht", "Krzywa orbity", "Yörünge eğrisi",
            "Орбитальная дуга", "Орбітальна дуга", "منحنى المدار", "कक्षा वक्र", "Kurva orbit",
            "Đường cong quỹ đạo", "軌道カーブ", "궤도 곡선", "轨道曲线"
        ),
        requiredRow(
            "BRAKE & COAST", "Frânează și rulează", "Frena y desliza", "Freine et glisse",
            "Bremsen & Rollen", "Frena e scorri", "Trava e desliza", "Remmen & uitrollen",
            "Hamuj i tocz się", "Frenle ve süzül", "Тормози и катись", "Гальмуй і котись",
            "فرامل وانزلاق", "ब्रेक और फिसलें", "Rem & meluncur", "Phanh và lướt", "ブレーキ＆惰性走行",
            "감속 및 활공", "刹车并滑行"
        ),
        requiredRow(
            "HAZARD DODGE", "Evită pericolele", "Esquiva peligros", "Esquive des dangers",
            "Gefahren ausweichen", "Schiva i pericoli", "Desvia dos perigos", "Gevaren ontwijken",
            "Unikaj zagrożeń", "Tehlikeden kaç", "Уклонение от угроз", "Ухилення від загроз",
            "تفادي الخطر", "खतरे से बचें", "Hindari bahaya", "Né hiểm nguy", "危険回避", "위험 회피", "躲避危险"
        ),
        requiredRow(
            "PULSE CHAIN", "Lanț de impulsuri", "Cadena de pulsos", "Chaîne d'impulsions",
            "Impulskette", "Catena di impulsi", "Corrente de pulsos", "Pulsketen", "Łańcuch impulsów",
            "Darbe zinciri", "Цепь импульсов", "Ланцюг імпульсів", "سلسلة نبضات", "पल्स श्रृंखला",
            "Rantai pulsa", "Chuỗi xung", "パルスチェイン", "펄스 체인", "脉冲连锁"
        ),
        requiredRow(
            "RIFT DRAIN", "Consum Rift", "Drenaje Rift", "Drain Rift", "Rift-Verbrauch", "Consumo Rift",
            "Dreno Rift", "Rift-verbruik", "Wyciek Rift", "Rift tüketimi", "Расход Rift", "Витрата Rift",
            "استنزاف Rift", "Rift क्षय", "Penguras Rift", "Rút cạn Rift", "Rift 消耗", "Rift 소모", "Rift 消耗"
        ),
        requiredRow(
            "PULSE GUARD", "Gardă de impuls", "Guardia de pulso", "Garde d'impulsion", "Impulsschutz",
            "Guardia impulso", "Guarda de pulso", "Pulsbescherming", "Osłona impulsowa", "Darbe koruması",
            "Защита от импульса", "Захист від імпульсу", "حارس النبض", "पल्स सुरक्षा", "Penjaga pulsa",
            "Chắn xung", "パルスガード", "펄스 가드", "脉冲防护"
        ),
        requiredRow(
            "FOCUS HEAVY", "Focalizare grea", "Enfoque pesado", "Focus lourd", "Schwerer Fokus",
            "Focus pesante", "Foco pesado", "Zware focus", "Ciężkie skupienie", "Ağır odak",
            "Тяжёлый фокус", "Важкий фокус", "تركيز ثقيل", "भारी फोकस", "Fokus berat",
            "Tập trung nặng", "ヘビーフォーカス", "헤비 포커스", "重力聚焦"
        ),
        requiredRow(
            "POWER MOON", "Lună puternică", "Luna potente", "Lune puissante", "Kraftmond", "Luna potente",
            "Lua potente", "Krachtmaan", "Księżyc mocy", "Güç ayı", "Луна силы", "Місяць сили",
            "قمر القوة", "शक्ति चंद्रमा", "Bulan daya", "Trăng sức mạnh", "パワームーン", "파워 문", "力量之月"
        ),
        requiredRow(
            "WIND CONTROL", "Controlul vântului", "Control del viento", "Contrôle du vent", "Windkontrolle",
            "Controllo del vento", "Controlo do vento", "Windcontrole", "Kontrola wiatru", "Rüzgâr kontrolü",
            "Контроль ветра", "Контроль вітру", "التحكم بالرياح", "हवा नियंत्रण", "Kendali angin",
            "Điều khiển gió", "風の制御", "바람 제어", "风力控制"
        ),
        requiredRow(
            "CHAOS TOUCH", "Atingere Chaos", "Toque Chaos", "Toucher Chaos", "Chaos-Berührung", "Tocco Chaos",
            "Toque Chaos", "Chaos-aanraking", "Dotyk Chaos", "Chaos dokunuşu", "Касание Chaos",
            "Дотик Chaos", "لمسة Chaos", "Chaos स्पर्श", "Sentuhan Chaos", "Chạm Chaos", "Chaos タッチ",
            "Chaos 터치", "Chaos 触碰"
        ),
        requiredRow(
            "CHAOS ORBIT", "Orbită Chaos", "Órbita Chaos", "Orbite Chaos", "Chaos-Orbit", "Orbita Chaos",
            "Órbita Chaos", "Chaos-baan", "Orbita Chaos", "Chaos yörüngesi", "Орбита Chaos", "Орбіта Chaos",
            "مدار Chaos", "Chaos कक्षा", "Orbit Chaos", "Quỹ đạo Chaos", "Chaos オービット", "Chaos 궤도", "Chaos 轨道"
        ),
        requiredRow(
            "CHAOS COAST", "Planare Chaos", "Desliz Chaos", "Glisse Chaos", "Chaos-Gleiten", "Planata Chaos",
            "Deslize Chaos", "Chaos-glijvlucht", "Szybowanie Chaos", "Chaos süzülüşü", "Планирование Chaos",
            "Ковзання Chaos", "انزلاق Chaos", "Chaos फिसलन", "Luncur Chaos", "Lướt Chaos", "Chaos 滑空",
            "Chaos 활공", "Chaos 滑行"
        ),
        requiredRow(
            "CRASH DODGE", "Evită impactul", "Esquiva el choque", "Esquive l'impact", "Crash ausweichen",
            "Schiva l'impatto", "Desvia da colisão", "Botsing ontwijken", "Unikaj zderzenia", "Çarpışmadan kaç",
            "Уклонение от аварии", "Ухилення від зіткнення", "تفادي الاصطدام", "टक्कर से बचें", "Hindari tabrakan",
            "Né va chạm", "クラッシュ回避", "충돌 회피", "躲避撞击"
        ),
        requiredRow(
            "RIFT COMBO", "Combo Rift", "Combo Rift", "Combo Rift", "Rift-Kombo", "Combo Rift", "Combo Rift",
            "Rift-combo", "Kombinacja Rift", "Rift kombosu", "Комбо Rift", "Комбо Rift", "مجموعة Rift",
            "Rift कॉम्बो", "Kombo Rift", "Combo Rift", "Rift コンボ", "Rift 콤보", "Rift 连击"
        ),
        requiredRow(
            "WIND OVERHEAT", "Vânt și supraîncălzire", "Viento y sobrecalentamiento", "Vent et surchauffe",
            "Wind & Überhitzung", "Vento e surriscaldamento", "Vento e sobreaquecimento",
            "Wind & oververhitting", "Wiatr i przegrzanie", "Rüzgâr ve aşırı ısınma", "Ветер и перегрев",
            "Вітер і перегрів", "الرياح والسخونة", "हवा और अधिक ताप", "Angin dan panas berlebih",
            "Gió và quá nhiệt", "風とオーバーヒート", "바람과 과열", "风与过热"
        ),

        requiredRow(
            "Portal IN teleports the ball to OUT.", "Portalul IN teleportează mingea la OUT.",
            "El portal IN teletransporta la bola a OUT.", "Le portail IN téléporte la balle vers OUT.",
            "Portal IN teleportiert den Ball zu OUT.", "Il portale IN teletrasporta la palla a OUT.",
            "O portal IN teleporta a bola para OUT.", "Portaal IN teleporteert de bal naar OUT.",
            "Portal IN przenosi piłkę do OUT.", "IN portalı topu OUT'a ışınlar.",
            "Портал IN переносит шар к OUT.", "Портал IN переносить кулю до OUT.",
            "تنقل بوابة IN الكرة إلى OUT.", "IN पोर्टल गेंद को OUT तक पहुँचाता है।",
            "Portal IN memindahkan bola ke OUT.", "Cổng IN dịch chuyển bóng đến OUT.",
            "ポータルINはボールをOUTへ転送します。", "IN 포털은 공을 OUT으로 이동시킵니다.",
            "IN 传送门会把球传送到 OUT。"
        ),
        requiredRow(
            "The exit launches with extra speed toward goal.", "Ieșirea lansează mingea cu viteză suplimentară spre țintă.",
            "La salida lanza la bola con velocidad extra hacia la meta.", "La sortie propulse la balle plus vite vers l'objectif.",
            "Der Ausgang schleudert den Ball mit Extraschub zum Ziel.", "L'uscita lancia la palla più veloce verso l'obiettivo.",
            "A saída lança a bola com velocidade extra para o alvo.", "De uitgang lanceert de bal met extra snelheid naar het doel.",
            "Wyjście wystrzeliwuje piłkę szybciej w stronę celu.", "Çıkış, topu hedefe doğru ek hızla fırlatır.",
            "Выход ускоряет шар в сторону цели.", "Вихід прискорює кулю в бік цілі.",
            "يطلق المخرج الكرة بسرعة إضافية نحو الهدف.", "निकास गेंद को अतिरिक्त गति से लक्ष्य की ओर भेजता है।",
            "Pintu keluar melontarkan bola lebih cepat ke sasaran.", "Lối ra phóng bóng nhanh hơn về phía mục tiêu.",
            "出口はボールを加速してゴールへ飛ばします。", "출구는 공을 더 빠르게 목표로 발사합니다.",
            "出口会加速把球射向目标。"
        ),
        requiredRow(
            "Aim before entering; it has a short cooldown.", "Țintește înainte să intri; portalul are o scurtă reîncărcare.",
            "Apunta antes de entrar; el portal tarda un poco en recargarse.", "Vise avant d'entrer ; le portail se recharge brièvement.",
            "Vor dem Eintritt zielen; das Portal lädt kurz nach.", "Mira prima di entrare; il portale ha una breve ricarica.",
            "Aponta antes de entrar; o portal tem uma recarga curta.", "Richt vóór je binnengaat; het portaal laadt kort op.",
            "Wyceluj przed wejściem; portal ma krótki czas odnowienia.", "Girmeden önce nişan al; portal kısa sürede yenilenir.",
            "Целься до входа; порталу нужно немного времени на перезарядку.", "Цілься до входу; порталу потрібен короткий час на відновлення.",
            "صوّب قبل الدخول؛ للبوابة وقت إعادة قصير.", "प्रवेश से पहले निशाना लगाएँ; पोर्टल का छोटा कूलडाउन है।",
            "Bidik sebelum masuk; portal punya jeda singkat.", "Ngắm trước khi vào; cổng có thời gian hồi ngắn.",
            "入る前に狙いましょう。ポータルには短い再使用時間があります。", "들어가기 전에 조준하세요. 포털에는 짧은 재사용 시간이 있습니다.",
            "进入前先瞄准；传送门有短暂冷却。"
        ),
        requiredRow(
            "Tap to fire a short Rift tether.", "Atinge pentru a lansa o legătură Rift scurtă.",
            "Toca para lanzar un vínculo Rift corto.", "Touche pour lancer un lien Rift bref.",
            "Tippe für eine kurze Rift-Verbindung.", "Tocca per lanciare un breve legame Rift.",
            "Toca para lançar uma ligação Rift curta.", "Tik voor een korte Rift-verbinding.",
            "Dotknij, aby wystrzelić krótką więź Rift.", "Kısa bir Rift bağı atmak için dokun.",
            "Нажми, чтобы создать короткую связь Rift.", "Торкнися, щоб створити короткий зв'язок Rift.",
            "اضغط لإطلاق رابط Rift قصير.", "छोटा Rift बंधन छोड़ने के लिए टैप करें।",
            "Ketuk untuk menembakkan ikatan Rift singkat.", "Chạm để phóng một liên kết Rift ngắn.",
            "タップして短いRiftテザーを放ちます。", "탭하여 짧은 Rift 연결을 발사하세요.",
            "点击可发射短暂的 Rift 牵引线。"
        ),
        requiredRow(
            "The ball accelerates toward the tap point.", "Mingea accelerează spre punctul atins.",
            "La bola acelera hacia el punto tocado.", "La balle accélère vers le point touché.",
            "Der Ball beschleunigt zum Berührungspunkt.", "La palla accelera verso il punto toccato.",
            "A bola acelera na direção do ponto tocado.", "De bal versnelt naar het aangeraakte punt.",
            "Piłka przyspiesza w stronę dotkniętego punktu.", "Top, dokunduğun noktaya doğru hızlanır.",
            "Шар ускоряется к точке нажатия.", "Куля прискорюється до точки дотику.",
            "تتسارع الكرة نحو نقطة الضغط.", "गेंद टैप किए गए बिंदु की ओर तेज़ होती है।",
            "Bola melaju menuju titik ketukan.", "Bóng tăng tốc về phía điểm chạm.",
            "ボールはタップした地点へ加速します。", "공은 탭한 지점을 향해 가속합니다.",
            "球会朝点击位置加速。"
        ),
        requiredRow(
            "Chain clean taps to steer without wasting energy.", "Leagă atingeri precise pentru a controla fără să risipești energie.",
            "Encadena toques precisos para dirigir sin malgastar energía.", "Enchaîne des touches précises pour diriger sans gaspiller d'énergie.",
            "Verbinde präzise Tipps, um ohne Energieverlust zu steuern.", "Concatena tocchi precisi per guidare senza sprecare energia.",
            "Encadeia toques precisos para controlar sem desperdiçar energia.", "Koppel nauwkeurige tikken om te sturen zonder energie te verspillen.",
            "Łącz precyzyjne stuknięcia, aby sterować bez marnowania energii.", "Enerji harcamadan yön vermek için temiz dokunuşları zincirle.",
            "Соединяй точные нажатия, чтобы управлять без потери энергии.", "Поєднуй точні дотики, щоб керувати без втрати енергії.",
            "اربط الضغطات الدقيقة للتوجيه دون هدر الطاقة.", "ऊर्जा बचाते हुए दिशा देने के लिए सटीक टैप जोड़ें।",
            "Rangkai ketukan tepat untuk mengarahkan tanpa membuang energi.", "Nối các lần chạm chính xác để điều khiển mà không phí năng lượng.",
            "正確なタップをつないで、エネルギーを無駄にせず操作します。", "정확한 탭을 이어 에너지 낭비 없이 조종하세요.",
            "连续精准点击，在不浪费能量的情况下控制方向。"
        ),
        requiredRow(
            "Pulse zones are not decoration.", "Zonele de impuls nu sunt doar decor.",
            "Las zonas de pulso no son decoración.", "Les zones d'impulsion ne sont pas décoratives.",
            "Impulszonen sind keine Dekoration.", "Le zone a impulsi non sono decorative.",
            "As zonas de pulso não são decoração.", "Pulszones zijn geen versiering.",
            "Strefy impulsu nie są ozdobą.", "Darbe bölgeleri süs değildir.",
            "Импульсные зоны — не декорация.", "Імпульсні зони — не декорація.",
            "مناطق النبض ليست للزينة.", "पल्स ज़ोन सजावट नहीं हैं।",
            "Zona pulsa bukan hiasan.", "Vùng xung không phải vật trang trí.",
            "パルスゾーンは飾りではありません。", "펄스 구역은 장식이 아닙니다.",
            "脉冲区域并非装饰。"
        ),
        requiredRow(
            "They push and swirl the ball inside the circle.", "Acestea împing și rotesc mingea în interiorul cercului.",
            "Empujan y hacen girar la bola dentro del círculo.", "Elles poussent et font tournoyer la balle dans le cercle.",
            "Sie schieben und verwirbeln den Ball im Kreis.", "Spingono e fanno ruotare la palla nel cerchio.",
            "Empurram e fazem a bola rodar dentro do círculo.", "Ze duwen en draaien de bal binnen de cirkel.",
            "Popychają i wirują piłką wewnątrz okręgu.", "Topu çemberin içinde iter ve döndürürler.",
            "Они толкают и закручивают шар внутри круга.", "Вони штовхають і закручують кулю всередині кола.",
            "تدفع الكرة وتدوّرها داخل الدائرة.", "वे गोले के भीतर गेंद को धकेलते और घुमाते हैं।",
            "Zona itu mendorong dan memutar bola di dalam lingkaran.", "Chúng đẩy và xoáy bóng bên trong vòng tròn.",
            "円の中でボールを押し、渦巻かせます。", "원 안에서 공을 밀고 회전시킵니다.",
            "它们会在圆圈内推动并旋转球。"
        ),
        requiredRow(
            "BOOST means the field is affecting you.", "BOOST înseamnă că acel câmp te influențează.",
            "BOOST significa que el campo te está afectando.", "BOOST signifie que le champ agit sur toi.",
            "BOOST bedeutet, dass das Feld auf dich wirkt.", "BOOST significa che il campo ti sta influenzando.",
            "BOOST significa que o campo está a afetar-te.", "BOOST betekent dat het veld je beïnvloedt.",
            "BOOST oznacza, że pole na ciebie działa.", "BOOST, alanın seni etkilediğini gösterir.",
            "BOOST означает, что поле воздействует на тебя.", "BOOST означає, що поле впливає на тебе.",
            "تعني BOOST أن الحقل يؤثر عليك.", "BOOST का अर्थ है कि क्षेत्र आप पर असर डाल रहा है।",
            "BOOST berarti medan sedang memengaruhimu.", "BOOST nghĩa là trường lực đang tác động lên bạn.",
            "BOOSTはフィールドの影響を受けている印です。", "BOOST는 필드의 영향을 받고 있다는 뜻입니다.",
            "BOOST 表示力场正在影响你。"
        ),
        requiredRow(
            "Tap behind the ball to brake.", "Atinge în spatele mingii pentru a frâna.",
            "Toca detrás de la bola para frenar.", "Touche derrière la balle pour freiner.",
            "Tippe hinter den Ball, um zu bremsen.", "Tocca dietro la palla per frenare.",
            "Toca atrás da bola para travar.", "Tik achter de bal om te remmen.",
            "Dotknij za piłką, aby zahamować.", "Frenlemek için topun arkasına dokun.",
            "Нажми позади шара, чтобы затормозить.", "Торкнися позаду кулі, щоб загальмувати.",
            "اضغط خلف الكرة للفرملة.", "ब्रेक लगाने के लिए गेंद के पीछे टैप करें।",
            "Ketuk di belakang bola untuk mengerem.", "Chạm phía sau bóng để phanh.",
            "ボールの後ろをタップして減速します。", "공 뒤를 탭하여 감속하세요.",
            "点击球的后方来刹车。"
        ),
        requiredRow(
            "Wait between taps to coast and save rift energy.", "Lasă pauză între atingeri pentru a rula și a economisi energie Rift.",
            "Espera entre toques para deslizar y ahorrar energía Rift.", "Attends entre les touches pour glisser et économiser l'énergie Rift.",
            "Warte zwischen Tipps, um zu rollen und Rift-Energie zu sparen.", "Attendi tra i tocchi per scorrere e risparmiare energia Rift.",
            "Espera entre toques para deslizar e poupar energia Rift.", "Wacht tussen tikken om uit te rollen en Rift-energie te sparen.",
            "Rób przerwy między stuknięciami, by toczyć się i oszczędzać energię Rift.", "Süzülmek ve Rift enerjisi biriktirmek için dokunuşlar arasında bekle.",
            "Делай паузы между нажатиями, чтобы катиться и экономить энергию Rift.", "Роби паузи між дотиками, щоб котитися й економити енергію Rift.",
            "انتظر بين الضغطات للانزلاق وتوفير طاقة Rift.", "फिसलने और Rift ऊर्जा बचाने के लिए टैप के बीच रुकें।",
            "Beri jeda antarketukan untuk meluncur dan menghemat energi Rift.", "Chờ giữa các lần chạm để lướt và tiết kiệm năng lượng Rift.",
            "タップの間を空けて惰性で進み、Riftエネルギーを節約します。", "탭 사이에 기다려 활공하고 Rift 에너지를 아끼세요.",
            "点击之间稍作等待，借助惯性滑行并节省 Rift 能量。"
        ),
        requiredRow(
            "Less rift used gives more HYPE.", "Mai puțin Rift folosit înseamnă mai mult HYPE.",
            "Usar menos Rift da más HYPE.", "Moins de Rift utilisé donne plus de HYPE.",
            "Weniger Rift-Verbrauch gibt mehr HYPE.", "Meno Rift usato dà più HYPE.",
            "Usar menos Rift dá mais HYPE.", "Minder Rift gebruiken levert meer HYPE op.",
            "Mniejsze zużycie Rift daje więcej HYPE.", "Daha az Rift kullanmak daha çok HYPE kazandırır.",
            "Меньше расход Rift — больше HYPE.", "Менша витрата Rift — більше HYPE.",
            "استخدام Rift أقل يمنح HYPE أكثر.", "कम Rift उपयोग से अधिक HYPE मिलता है।",
            "Semakin sedikit Rift dipakai, semakin banyak HYPE.", "Dùng ít Rift hơn sẽ nhận nhiều HYPE hơn.",
            "Riftの使用量が少ないほどHYPEが増えます。", "Rift를 적게 쓸수록 HYPE가 커집니다.",
            "使用的 Rift 越少，获得的 HYPE 越多。"
        ),
        requiredRow(
            "Pink crash nodes end the run.", "Nodurile roz de impact încheie încercarea.",
            "Los nodos rosas de choque terminan la partida.", "Les nœuds d'impact roses mettent fin à la partie.",
            "Pinke Crash-Knoten beenden den Lauf.", "I nodi d'impatto rosa terminano la partita.",
            "Os nós rosa de colisão terminam a partida.", "Roze botsingsknooppunten beëindigen de poging.",
            "Różowe węzły zderzenia kończą próbę.", "Pembe çarpışma düğümleri denemeyi bitirir.",
            "Розовые узлы столкновения завершают попытку.", "Рожеві вузли зіткнення завершують спробу.",
            "عُقد الاصطدام الوردية تنهي المحاولة.", "गुलाबी क्रैश नोड प्रयास समाप्त कर देते हैं।",
            "Simpul tabrakan merah muda mengakhiri percobaan.", "Nút va chạm màu hồng kết thúc lượt chơi.",
            "ピンクのクラッシュノードに触れると終了します。", "분홍색 충돌 노드는 시도를 종료합니다.",
            "粉色撞击节点会结束本局。"
        ),
        requiredRow(
            "Short tap bursts dodge better than panic spam.", "Seriile scurte de atingeri evită mai bine decât apăsările haotice.",
            "Las ráfagas cortas esquivan mejor que tocar con pánico.", "De brèves rafales de touches esquivent mieux que des touches paniquées.",
            "Kurze Tipp-Salven weichen besser aus als hektisches Tippen.", "Brevi raffiche di tocchi schivano meglio dei tocchi nel panico.",
            "Rajadas curtas de toques desviam melhor do que tocar em pânico.", "Korte tikreeksen ontwijken beter dan paniekerig tikken.",
            "Krótkie serie stuknięć omijają lepiej niż chaotyczne klikanie.", "Kısa dokunma serileri panikle basmaktan daha iyi kaçınır.",
            "Короткие серии нажатий лучше хаотичных касаний.", "Короткі серії дотиків кращі за хаотичні натискання.",
            "دفعات الضغط القصيرة أفضل من الضغط العشوائي.", "छोटे टैप समूह घबराहट में बार-बार टैप करने से बेहतर बचाते हैं।",
            "Rentetan ketukan singkat lebih baik daripada mengetuk panik.", "Loạt chạm ngắn né tốt hơn việc chạm loạn vì hoảng.",
            "短い連続タップの方が、慌てた連打より上手に避けられます。", "짧은 연속 탭이 당황한 난타보다 더 잘 피합니다.",
            "短促连续点击比慌乱乱点更容易躲避。"
        ),
        requiredRow(
            "Clean dodges keep your streak alive.", "Evitările precise îți păstrează seria activă.",
            "Las esquivas limpias mantienen viva tu racha.", "Les esquives nettes maintiennent ta série.",
            "Saubere Ausweichmanöver halten deine Serie am Leben.", "Le schivate pulite mantengono viva la serie.",
            "Desvios limpos mantêm a tua série ativa.", "Nette ontwijkingen houden je reeks in leven.",
            "Czyste uniki podtrzymują twoją serię.", "Temiz kaçışlar serini canlı tutar.",
            "Чистые уклонения сохраняют серию.", "Чисті ухилення зберігають серію.",
            "المراوغات النظيفة تبقي سلسلتك مستمرة.", "साफ़ बचाव आपकी स्ट्रीक जारी रखते हैं।",
            "Hindaran bersih menjaga rentetanmu.", "Cú né gọn giữ chuỗi của bạn tiếp tục.",
            "きれいに避けるとストリークが続きます。", "깔끔하게 피하면 연속 기록이 유지됩니다.",
            "干净躲避可延续你的连胜。"
        ),

        requiredRow(
            "CHAIN is your live combo.", "CHAIN este combo-ul tău activ.", "CHAIN es tu combo activo.",
            "CHAIN est ton combo actif.", "CHAIN ist deine aktive Kombo.", "CHAIN è la tua combo attiva.",
            "CHAIN é o teu combo ativo.", "CHAIN is je actieve combo.", "CHAIN to twoje aktywne combo.",
            "CHAIN, aktif kombondur.", "CHAIN — твоё активное комбо.", "CHAIN — твоє активне комбо.",
            "CHAIN هي مجموعتك النشطة.", "CHAIN आपका सक्रिय कॉम्बो है।", "CHAIN adalah kombo aktifmu.",
            "CHAIN là combo đang hoạt động.", "CHAINは現在のコンボです。", "CHAIN은 현재 콤보입니다.", "CHAIN 是你的实时连击。"
        ),
        requiredRow(
            "It grows during fast rift control or boost fields.", "Crește când controlezi rapid Riftul sau intri în câmpuri BOOST.",
            "Crece al controlar Rift rápidamente o entrar en campos BOOST.", "Il augmente avec un contrôle Rift rapide ou dans les champs BOOST.",
            "Sie wächst bei schneller Rift-Steuerung oder in BOOST-Feldern.", "Cresce con un controllo Rift rapido o nei campi BOOST.",
            "Cresce com controlo Rift rápido ou em campos BOOST.", "Hij groeit bij snelle Rift-besturing of in BOOST-velden.",
            "Rośnie przy szybkim sterowaniu Rift lub w polach BOOST.", "Hızlı Rift kontrolünde veya BOOST alanlarında büyür.",
            "Оно растёт при быстром управлении Rift или в полях BOOST.", "Воно зростає за швидкого керування Rift або в полях BOOST.",
            "تنمو مع التحكم السريع في Rift أو داخل حقول BOOST.", "तेज़ Rift नियंत्रण या BOOST क्षेत्रों में यह बढ़ता है।",
            "Nilainya naik saat kendali Rift cepat atau di medan BOOST.", "Nó tăng khi điều khiển Rift nhanh hoặc trong vùng BOOST.",
            "素早いRift操作やBOOSTフィールドで増加します。", "빠른 Rift 제어 또는 BOOST 필드에서 증가합니다.",
            "快速控制 Rift 或进入 BOOST 力场时会提升。"
        ),
        requiredRow(
            "Max chain adds big HYPE at finish.", "Lanțul maxim adaugă mult HYPE la final.",
            "La cadena máxima añade mucho HYPE al final.", "La chaîne maximale ajoute beaucoup de HYPE à l'arrivée.",
            "Die maximale Kette bringt am Ziel viel HYPE.", "La catena massima aggiunge molto HYPE alla fine.",
            "A corrente máxima dá muito HYPE no final.", "Een maximale keten levert veel HYPE op bij de finish.",
            "Maksymalny łańcuch daje dużo HYPE na mecie.", "Maksimum zincir bitişte bol HYPE kazandırır.",
            "Максимальная цепь даёт много HYPE на финише.", "Максимальний ланцюг дає багато HYPE на фініші.",
            "تمنح السلسلة القصوى الكثير من HYPE عند النهاية.", "अधिकतम श्रृंखला अंत में बड़ा HYPE देती है।",
            "Rantai maksimum memberi banyak HYPE saat finis.", "Chuỗi tối đa cộng nhiều HYPE khi về đích.",
            "最大チェインはゴール時に大量のHYPEを追加します。", "최대 체인은 완료 시 큰 HYPE를 추가합니다.",
            "最大连锁会在终点增加大量 HYPE。"
        ),
        requiredRow(
            "Rift energy is limited.", "Energia Rift este limitată.", "La energía Rift es limitada.",
            "L'énergie Rift est limitée.", "Rift-Energie ist begrenzt.", "L'energia Rift è limitata.",
            "A energia Rift é limitada.", "Rift-energie is beperkt.", "Energia Rift jest ograniczona.",
            "Rift enerjisi sınırlıdır.", "Энергия Rift ограничена.", "Енергія Rift обмежена.",
            "طاقة Rift محدودة.", "Rift ऊर्जा सीमित है।", "Energi Rift terbatas.", "Năng lượng Rift có hạn.",
            "Riftエネルギーには限りがあります。", "Rift 에너지는 한정되어 있습니다.", "Rift 能量有限。"
        ),
        requiredRow(
            "Rift Drain spends energy faster during tap bursts.", "Rift Drain consumă energia mai repede în timpul seriilor de atingeri.",
            "Rift Drain gasta energía más rápido durante las ráfagas de toques.", "Rift Drain consomme l'énergie plus vite pendant les rafales de touches.",
            "Rift Drain verbraucht bei Tipp-Salven schneller Energie.", "Rift Drain consuma energia più velocemente durante le raffiche di tocchi.",
            "Rift Drain gasta energia mais depressa durante rajadas de toques.", "Rift Drain verbruikt sneller energie tijdens tikreeksen.",
            "Rift Drain szybciej zużywa energię podczas serii stuknięć.", "Rift Drain, dokunma serilerinde enerjiyi daha hızlı tüketir.",
            "Rift Drain быстрее расходует энергию во время серий нажатий.", "Rift Drain швидше витрачає енергію під час серій дотиків.",
            "يستهلك Rift Drain الطاقة أسرع أثناء دفعات الضغط.", "तेज़ टैप के दौरान Rift Drain ऊर्जा जल्दी खर्च करता है।",
            "Rift Drain menguras energi lebih cepat saat rentetan ketukan.", "Rift Drain tiêu hao năng lượng nhanh hơn khi chạm liên tục.",
            "連続タップ中はRift Drainがエネルギーを速く消費します。", "연속 탭 중에는 Rift Drain이 에너지를 더 빨리 소모합니다.",
            "连续点击时，Rift Drain 会更快消耗能量。"
        ),
        requiredRow(
            "Pause between taps to recharge.", "Fă pauză între atingeri pentru reîncărcare.",
            "Haz una pausa entre toques para recargar.", "Fais une pause entre les touches pour recharger.",
            "Pausiere zwischen Tipps zum Aufladen.", "Fai una pausa tra i tocchi per ricaricare.",
            "Faz uma pausa entre toques para recarregar.", "Pauzeer tussen tikken om op te laden.",
            "Rób przerwy między stuknięciami, aby się doładować.", "Yenilenmek için dokunuşlar arasında durakla.",
            "Делай паузы между нажатиями для перезарядки.", "Роби паузи між дотиками для відновлення.",
            "توقف بين الضغطات لإعادة الشحن.", "रिचार्ज के लिए टैप के बीच रुकें।", "Jeda antarketukan untuk mengisi ulang.",
            "Nghỉ giữa các lần chạm để hồi năng lượng.", "タップの間を空けて再充填します。", "재충전하려면 탭 사이에 쉬세요.",
            "点击之间暂停一下来充能。"
        ),
        requiredRow(
            "Pulse Storm makes fields stronger.", "Pulse Storm face câmpurile mai puternice.",
            "Pulse Storm hace que los campos sean más fuertes.", "Pulse Storm renforce les champs.",
            "Pulse Storm verstärkt die Felder.", "Pulse Storm rende i campi più forti.",
            "Pulse Storm torna os campos mais fortes.", "Pulse Storm maakt velden sterker.",
            "Pulse Storm wzmacnia pola.", "Pulse Storm alanları güçlendirir.",
            "Pulse Storm усиливает поля.", "Pulse Storm посилює поля.",
            "تجعل Pulse Storm الحقول أقوى.", "Pulse Storm क्षेत्रों को अधिक शक्तिशाली बनाता है।",
            "Pulse Storm memperkuat medan.", "Pulse Storm làm các trường lực mạnh hơn.",
            "Pulse Stormはフィールドを強化します。", "Pulse Storm은 필드를 더 강하게 만듭니다.",
            "Pulse Storm 会强化力场。"
        ),
        requiredRow(
            "Tap through the pulse when it gets wild.", "Atinge prin impuls când devine puternic.",
            "Toca a través del pulso cuando se descontrole.", "Touche à travers l'impulsion quand elle s'emballe.",
            "Tippe durch den Impuls, wenn er wild wird.", "Tocca attraverso l'impulso quando diventa intenso.",
            "Toca através do pulso quando ficar intenso.", "Tik door de puls als die wild wordt.",
            "Dotknij przez impuls, gdy staje się gwałtowny.", "Darbe kontrolden çıkınca içinden dokun.",
            "Нажимай сквозь импульс, когда он усиливается.", "Торкайся крізь імпульс, коли він посилюється.",
            "اضغط عبر النبضة عندما تشتد.", "पल्स तेज़ होने पर उसके पार टैप करें।",
            "Ketuk menembus pulsa saat mengganas.", "Chạm xuyên qua xung khi nó trở nên dữ dội.",
            "パルスが激しくなったら、その先をタップします。", "펄스가 거칠어지면 펄스 너머를 탭하세요.",
            "脉冲变强时，点击穿过它。"
        ),
        requiredRow(
            "Use the storm for speed, not panic.", "Folosește furtuna pentru viteză, nu pentru panică.",
            "Usa la tormenta para ganar velocidad, no entres en pánico.", "Utilise la tempête pour accélérer, sans paniquer.",
            "Nutze den Sturm für Tempo, nicht für Panik.", "Usa la tempesta per la velocità, senza farti prendere dal panico.",
            "Usa a tempestade para ganhar velocidade, sem pânico.", "Gebruik de storm voor snelheid, niet voor paniek.",
            "Użyj burzy do przyspieszenia, bez paniki.", "Fırtınayı hız için kullan, paniğe kapılma.",
            "Используй бурю для скорости, не паникуй.", "Використовуй бурю для швидкості, не панікуй.",
            "استخدم العاصفة للسرعة، لا للهلع.", "तूफ़ान को गति के लिए उपयोग करें, घबराएँ नहीं।",
            "Gunakan badai untuk kecepatan, bukan kepanikan.", "Dùng bão để tăng tốc, đừng hoảng loạn.",
            "ストームを加速に使い、慌てないでください。", "폭풍을 속도에 활용하고 당황하지 마세요.",
            "利用风暴加速，不要慌乱。"
        ),
        requiredRow(
            "Focus Field slows the ball during tap bursts.", "Focus Field încetinește mingea în timpul seriilor de atingeri.",
            "Focus Field frena la bola durante las ráfagas de toques.", "Focus Field ralentit la balle pendant les rafales de touches.",
            "Focus Field bremst den Ball während Tipp-Salven.", "Focus Field rallenta la palla durante le raffiche di tocchi.",
            "Focus Field abranda a bola durante rajadas de toques.", "Focus Field vertraagt de bal tijdens tikreeksen.",
            "Focus Field spowalnia piłkę podczas serii stuknięć.", "Focus Field, dokunma serilerinde topu yavaşlatır.",
            "Focus Field замедляет шар во время серий нажатий.", "Focus Field уповільнює кулю під час серій дотиків.",
            "يبطئ Focus Field الكرة أثناء دفعات الضغط.", "तेज़ टैप के दौरान Focus Field गेंद को धीमा करता है।",
            "Focus Field memperlambat bola saat rentetan ketukan.", "Focus Field làm chậm bóng khi chạm liên tục.",
            "連続タップ中はFocus Fieldがボールを減速させます。", "연속 탭 중에는 Focus Field가 공을 느리게 합니다.",
            "连续点击时，Focus Field 会让球减速。"
        ),
        requiredRow(
            "Heavy Core pulls down harder.", "Heavy Core trage mai puternic în jos.",
            "Heavy Core tira con más fuerza hacia abajo.", "Heavy Core tire plus fort vers le bas.",
            "Heavy Core zieht stärker nach unten.", "Heavy Core tira più forte verso il basso.",
            "Heavy Core puxa com mais força para baixo.", "Heavy Core trekt harder naar beneden.",
            "Heavy Core mocniej ciągnie w dół.", "Heavy Core aşağıya daha güçlü çeker.",
            "Heavy Core сильнее тянет вниз.", "Heavy Core сильніше тягне вниз.",
            "يسحب Heavy Core إلى الأسفل بقوة أكبر.", "Heavy Core नीचे की ओर अधिक खींचता है।",
            "Heavy Core menarik ke bawah lebih kuat.", "Heavy Core kéo xuống mạnh hơn.",
            "Heavy Coreは下向きに強く引きます。", "Heavy Core는 아래로 더 강하게 당깁니다.",
            "Heavy Core 会更用力地向下拉。"
        ),
        requiredRow(
            "Use precision taps to fight gravity.", "Folosește atingeri precise pentru a învinge gravitația.",
            "Usa toques precisos para vencer la gravedad.", "Utilise des touches précises pour contrer la gravité.",
            "Nutze präzise Tipps gegen die Schwerkraft.", "Usa tocchi precisi per contrastare la gravità.",
            "Usa toques precisos para contrariar a gravidade.", "Gebruik nauwkeurige tikken tegen de zwaartekracht.",
            "Używaj precyzyjnych stuknięć przeciw grawitacji.", "Yer çekimine karşı hassas dokunuşlar kullan.",
            "Используй точные нажатия против гравитации.", "Використовуй точні дотики проти гравітації.",
            "استخدم ضغطات دقيقة لمقاومة الجاذبية.", "गुरुत्वाकर्षण से लड़ने के लिए सटीक टैप करें।",
            "Gunakan ketukan presisi untuk melawan gravitasi.", "Dùng các lần chạm chính xác để chống lại trọng lực.",
            "正確なタップで重力に逆らいます。", "정밀한 탭으로 중력에 맞서세요.",
            "使用精准点击来对抗重力。"
        ),
        requiredRow(
            "Power Tap charges a stronger pull.", "Atingerea puternică încarcă o atracție mai intensă.",
            "El toque potente carga una atracción más fuerte.", "La touche puissante charge une attraction plus forte.",
            "Der Kraft-Tipp lädt einen stärkeren Zug.", "Il tocco potente carica un'attrazione più forte.",
            "O toque potente carrega uma atração mais forte.", "De krachttik laadt een sterkere trekkracht op.",
            "Mocne stuknięcie ładuje silniejsze przyciąganie.", "Güçlü dokunma daha kuvvetli bir çekim yükler.",
            "Сильное нажатие заряжает более мощное притяжение.", "Сильний дотик заряджає потужніше притягання.",
            "تشحن الضغطة القوية سحبًا أقوى.", "शक्तिशाली टैप अधिक मज़बूत खिंचाव चार्ज करता है।",
            "Ketukan kuat mengisi tarikan yang lebih kuat.", "Chạm mạnh tích lực kéo mạnh hơn.",
            "強力タップで、より強い引力をチャージします。", "강력 탭은 더 강한 끌어당김을 충전합니다.",
            "强力点击会蓄积更强的拉力。"
        ),
        requiredRow(
            "Moon Glide keeps momentum after release.", "Moon Glide păstrează avântul după eliberare.",
            "Moon Glide mantiene el impulso después de soltar.", "Moon Glide conserve l'élan après le relâchement.",
            "Moon Glide erhält den Schwung nach dem Loslassen.", "Moon Glide mantiene lo slancio dopo il rilascio.",
            "Moon Glide mantém o impulso depois de soltar.", "Moon Glide behoudt vaart na het loslaten.",
            "Moon Glide zachowuje pęd po puszczeniu.", "Moon Glide, bıraktıktan sonra ivmeyi korur.",
            "Moon Glide сохраняет импульс после отпускания.", "Moon Glide зберігає імпульс після відпускання.",
            "تحافظ Moon Glide على الزخم بعد الإفلات.", "Moon Glide छोड़ने के बाद गति बनाए रखता है।",
            "Moon Glide menjaga momentum setelah dilepas.", "Moon Glide giữ đà sau khi thả.",
            "Moon Glideは解除後も勢いを保ちます。", "Moon Glide는 손을 뗀 뒤에도 추진력을 유지합니다.",
            "Moon Glide 会在释放后保持动量。"
        ),
        requiredRow(
            "Tap, glide, then coast into the exit.", "Atinge, planează, apoi rulează spre ieșire.",
            "Toca, planea y luego deslízate hasta la salida.", "Touche, glisse, puis roule jusqu'à la sortie.",
            "Tippen, gleiten und dann in den Ausgang rollen.", "Tocca, plana e poi scorri fino all'uscita.",
            "Toca, plana e depois desliza até à saída.", "Tik, zweef en rol dan de uitgang in.",
            "Dotknij, szybuj, a potem wtocz się do wyjścia.", "Dokun, süzül, sonra çıkışa doğru ilerle.",
            "Нажми, планируй и катись к выходу.", "Торкнися, ковзай і котися до виходу.",
            "اضغط وانزلق ثم تابع إلى المخرج.", "टैप करें, फिसलें, फिर निकास तक बहें।",
            "Ketuk, meluncur, lalu masuk ke pintu keluar.", "Chạm, lướt rồi trôi vào lối ra.",
            "タップして滑空し、そのまま出口へ進みます。", "탭하고 활공한 뒤 출구로 미끄러져 들어가세요.",
            "点击、滑行，然后借惯性进入出口。"
        ),
        requiredRow(
            "Wind pushes the ball sideways.", "Vântul împinge mingea lateral.", "El viento empuja la bola de lado.",
            "Le vent pousse la balle sur le côté.", "Der Wind schiebt den Ball seitwärts.",
            "Il vento spinge la palla di lato.", "O vento empurra a bola para o lado.",
            "De wind duwt de bal opzij.", "Wiatr spycha piłkę na bok.", "Rüzgâr topu yana iter.",
            "Ветер сдвигает шар в сторону.", "Вітер штовхає кулю вбік.", "تدفع الرياح الكرة جانبًا.",
            "हवा गेंद को बगल की ओर धकेलती है।", "Angin mendorong bola ke samping.", "Gió đẩy bóng sang ngang.",
            "風がボールを横へ押します。", "바람은 공을 옆으로 밉니다.", "风会把球推向侧面。"
        ),
        requiredRow(
            "Overheat punishes tap spam.", "Supraîncălzirea penalizează atingerile repetate haotic.",
            "El sobrecalentamiento castiga los toques continuos.", "La surchauffe punit les touches répétées.",
            "Überhitzung bestraft hektisches Tippen.", "Il surriscaldamento punisce i tocchi ripetuti.",
            "O sobreaquecimento pune os toques repetidos.", "Oververhitting straft wild tikken af.",
            "Przegrzanie karze chaotyczne stukanie.", "Aşırı ısınma, sürekli dokunmayı cezalandırır.",
            "Перегрев наказывает за частые нажатия.", "Перегрів карає за часті дотики.",
            "تعاقب السخونة الزائدة الضغط المتكرر.", "अधिक ताप बार-बार टैप करने पर दंड देता है।",
            "Panas berlebih menghukum ketukan bertubi-tubi.", "Quá nhiệt phạt việc chạm liên tục.",
            "オーバーヒート中の連打は危険です。", "과열은 무분별한 연속 탭을 불리하게 만듭니다.",
            "过热会惩罚连续乱点。"
        ),
        requiredRow(
            "Use short bursts for the tiny gate.", "Folosește serii scurte pentru poarta mică.",
            "Usa ráfagas cortas para la puerta pequeña.", "Utilise de brèves rafales pour la petite porte.",
            "Nutze kurze Salven für das kleine Tor.", "Usa brevi raffiche per il piccolo varco.",
            "Usa rajadas curtas para a porta pequena.", "Gebruik korte reeksen voor de kleine poort.",
            "Używaj krótkich serii przy małej bramce.", "Küçük kapı için kısa seriler kullan.",
            "Используй короткие серии для узких ворот.", "Використовуй короткі серії для малої брами.",
            "استخدم دفعات قصيرة للبوابة الصغيرة.", "छोटे द्वार के लिए छोटे टैप समूह उपयोग करें।",
            "Gunakan rentetan singkat untuk gerbang kecil.", "Dùng loạt chạm ngắn cho cổng nhỏ.",
            "小さなゲートには短い連続タップを使います。", "작은 게이트에서는 짧은 연속 탭을 사용하세요.",
            "通过小门时使用短促连续点击。"
        ),

        requiredRow(
            "Obstacle: portals change position and speed instantly.", "Obstacol: portalurile schimbă instant poziția și viteza.",
            "Obstáculo: los portales cambian la posición y la velocidad al instante.", "Obstacle : les portails changent instantanément la position et la vitesse.",
            "Hindernis: Portale ändern Position und Tempo sofort.", "Ostacolo: i portali cambiano subito posizione e velocità.",
            "Obstáculo: os portais mudam a posição e a velocidade instantaneamente.", "Obstakel: portalen veranderen direct positie en snelheid.",
            "Przeszkoda: portale natychmiast zmieniają pozycję i prędkość.", "Engel: portallar konumu ve hızı anında değiştirir.",
            "Препятствие: порталы мгновенно меняют позицию и скорость.", "Перешкода: портали миттєво змінюють позицію та швидкість.",
            "عائق: تغيّر البوابات الموقع والسرعة فورًا.", "बाधा: पोर्टल तुरंत स्थिति और गति बदलते हैं।",
            "Rintangan: portal langsung mengubah posisi dan kecepatan.", "Chướng ngại: cổng dịch chuyển đổi vị trí và tốc độ tức thì.",
            "障害物：ポータルは位置と速度を瞬時に変えます。", "장애물: 포털은 위치와 속도를 즉시 바꿉니다.",
            "障碍：传送门会瞬间改变位置和速度。"
        ),
        requiredRow(
            "Obstacle: pink crash nodes instantly fail the run.", "Obstacol: nodurile roz de impact încheie instant încercarea.",
            "Obstáculo: los nodos rosas de choque terminan la partida al instante.", "Obstacle : les nœuds d'impact roses font échouer la partie immédiatement.",
            "Hindernis: Pinke Crash-Knoten beenden den Lauf sofort.", "Ostacolo: i nodi d'impatto rosa terminano subito la partita.",
            "Obstáculo: os nós rosa de colisão terminam a partida instantaneamente.", "Obstakel: roze botsingsknooppunten beëindigen de poging direct.",
            "Przeszkoda: różowe węzły zderzenia natychmiast kończą próbę.", "Engel: pembe çarpışma düğümleri denemeyi anında bitirir.",
            "Препятствие: розовые узлы столкновения мгновенно завершают попытку.", "Перешкода: рожеві вузли зіткнення миттєво завершують спробу.",
            "عائق: عُقد الاصطدام الوردية تنهي المحاولة فورًا.", "बाधा: गुलाबी क्रैश नोड तुरंत प्रयास समाप्त कर देते हैं।",
            "Rintangan: simpul tabrakan merah muda langsung mengakhiri percobaan.", "Chướng ngại: nút va chạm màu hồng làm lượt chơi thất bại ngay lập tức.",
            "障害物：ピンクのクラッシュノードで即失敗します。", "장애물: 분홍색 충돌 노드는 즉시 실패하게 만듭니다.",
            "障碍：粉色撞击节点会立即导致失败。"
        ),
        requiredRow(
            "Obstacle: tiny gate makes the exit much smaller.", "Obstacol: poarta mică reduce mult ieșirea.",
            "Obstáculo: la puerta pequeña reduce mucho la salida.", "Obstacle : la petite porte réduit fortement la sortie.",
            "Hindernis: Das kleine Tor verkleinert den Ausgang stark.", "Ostacolo: il piccolo varco riduce molto l'uscita.",
            "Obstáculo: a porta pequena reduz muito a saída.", "Obstakel: de kleine poort maakt de uitgang veel kleiner.",
            "Przeszkoda: mała bramka znacznie zmniejsza wyjście.", "Engel: küçük kapı çıkışı çok küçültür.",
            "Препятствие: узкие ворота сильно уменьшают выход.", "Перешкода: мала брама значно зменшує вихід.",
            "عائق: البوابة الصغيرة تصغّر المخرج كثيرًا.", "बाधा: छोटा द्वार निकास को बहुत छोटा कर देता है।",
            "Rintangan: gerbang kecil membuat pintu keluar jauh lebih sempit.", "Chướng ngại: cổng nhỏ làm lối ra hẹp hơn nhiều.",
            "障害物：小さなゲートで出口が大幅に狭くなります。", "장애물: 작은 게이트는 출구를 훨씬 작게 만듭니다.",
            "障碍：小门会让出口大幅缩小。"
        ),
        requiredRow(
            "Obstacle: platforms bounce you; pulse fields bend speed.", "Obstacol: platformele te ricoșează; câmpurile de impuls modifică viteza.",
            "Obstáculo: las plataformas te hacen rebotar; los campos de pulso alteran la velocidad.", "Obstacle : les plateformes te font rebondir ; les champs d'impulsion modifient la vitesse.",
            "Hindernis: Plattformen lassen dich abprallen; Impulsfelder verändern das Tempo.", "Ostacolo: le piattaforme ti fanno rimbalzare; i campi a impulsi alterano la velocità.",
            "Obstáculo: as plataformas fazem-te ressaltar; os campos de pulso alteram a velocidade.", "Obstakel: platforms laten je stuiten; pulsvelden buigen je snelheid af.",
            "Przeszkoda: platformy odbijają; pola impulsowe zmieniają prędkość.", "Engel: platformlar sektirir; darbe alanları hızı değiştirir.",
            "Препятствие: платформы отбрасывают; импульсные поля меняют скорость.", "Перешкода: платформи відбивають; імпульсні поля змінюють швидкість.",
            "عائق: المنصات ترتد بك؛ حقول النبض تغيّر السرعة.", "बाधा: प्लेटफ़ॉर्म उछालते हैं; पल्स क्षेत्र गति मोड़ते हैं।",
            "Rintangan: platform memantulkanmu; medan pulsa mengubah kecepatan.", "Chướng ngại: bệ đỡ làm bạn nảy; trường xung bẻ hướng tốc độ.",
            "障害物：足場で跳ね返り、パルスフィールドが速度を変えます。", "장애물: 플랫폼은 튕겨 내고 펄스 필드는 속도를 바꿉니다.",
            "障碍：平台会反弹你；脉冲力场会改变速度。"
        ),
        requiredRow(
            "Obstacle: platforms bounce and redirect the ball.", "Obstacol: platformele ricoșează și redirecționează mingea.",
            "Obstáculo: las plataformas hacen rebotar y redirigen la bola.", "Obstacle : les plateformes font rebondir et redirigent la balle.",
            "Hindernis: Plattformen lassen den Ball abprallen und lenken ihn um.", "Ostacolo: le piattaforme fanno rimbalzare e deviano la palla.",
            "Obstáculo: as plataformas fazem a bola ressaltar e mudam a sua direção.", "Obstakel: platforms laten de bal stuiten en sturen hem om.",
            "Przeszkoda: platformy odbijają i przekierowują piłkę.", "Engel: platformlar topu sektirir ve yönlendirir.",
            "Препятствие: платформы отбрасывают и перенаправляют шар.", "Перешкода: платформи відбивають і перенаправляють кулю.",
            "عائق: المنصات ترتد بالكرة وتغيّر اتجاهها.", "बाधा: प्लेटफ़ॉर्म गेंद को उछालकर दिशा बदलते हैं।",
            "Rintangan: platform memantulkan dan mengarahkan ulang bola.", "Chướng ngại: bệ đỡ làm bóng nảy và đổi hướng.",
            "障害物：足場がボールを跳ね返し、方向を変えます。", "장애물: 플랫폼은 공을 튕기고 방향을 바꿉니다.",
            "障碍：平台会反弹球并改变其方向。"
        ),
        requiredRow(
            "Obstacle: screen edges and timer can still end the run.", "Obstacol: marginile ecranului și cronometrul pot încheia încercarea.",
            "Obstáculo: los bordes de la pantalla y el tiempo aún pueden terminar la partida.", "Obstacle : les bords de l'écran et le chrono peuvent encore terminer la partie.",
            "Hindernis: Bildschirmränder und Timer können den Lauf weiterhin beenden.", "Ostacolo: i bordi dello schermo e il timer possono ancora terminare la partita.",
            "Obstáculo: as margens do ecrã e o tempo ainda podem terminar a partida.", "Obstakel: schermranden en timer kunnen de poging nog steeds beëindigen.",
            "Przeszkoda: krawędzie ekranu i czas nadal mogą zakończyć próbę.", "Engel: ekran kenarları ve süre denemeyi hâlâ bitirebilir.",
            "Препятствие: края экрана и таймер всё ещё могут завершить попытку.", "Перешкода: краї екрана й таймер усе ще можуть завершити спробу.",
            "عائق: حواف الشاشة والمؤقت قد ينهيان المحاولة أيضًا.", "बाधा: स्क्रीन के किनारे और टाइमर अभी भी प्रयास समाप्त कर सकते हैं।",
            "Rintangan: tepi layar dan timer tetap dapat mengakhiri percobaan.", "Chướng ngại: mép màn hình và bộ đếm giờ vẫn có thể kết thúc lượt chơi.",
            "障害物：画面端とタイマーでも終了することがあります。", "장애물: 화면 가장자리와 타이머도 시도를 끝낼 수 있습니다.",
            "障碍：屏幕边缘和计时器仍可能结束本局。"
        )
    )

    val requiredKeys: Set<String> = rows.keys

    fun translation(language: KavvoroLanguage, key: String): String? {
        val resolvedLanguage = if (language == KavvoroLanguage.SYSTEM) {
            KavvoroLanguage.EN
        } else {
            language
        }
        return rows[key]?.get(resolvedLanguage)
    }

    fun hasExplicitTranslation(language: KavvoroLanguage, key: String): Boolean =
        language != KavvoroLanguage.SYSTEM && rows[key]?.containsKey(language) == true

    private fun requiredRow(
        en: String,
        ro: String,
        es: String,
        fr: String,
        de: String,
        it: String,
        pt: String,
        nl: String,
        pl: String,
        tr: String,
        ru: String,
        uk: String,
        ar: String,
        hi: String,
        id: String,
        vi: String,
        ja: String,
        ko: String,
        zh: String
    ): Pair<String, Map<KavvoroLanguage, String>> = en to mapOf(
        KavvoroLanguage.EN to en,
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

    private const val LESSON_LINES_PER_LEVEL = 3
}
