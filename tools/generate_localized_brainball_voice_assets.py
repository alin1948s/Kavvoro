#!/usr/bin/env python3
"""Generate localized Brainball selection voice assets.

The app ships these OGG files from res/raw. It does not use runtime TTS.
Each generated file keeps the Brainball name audible, then adds a short native
brainrot-style line for the selected locale.
"""

from __future__ import annotations

import argparse
import asyncio
import shutil
import tempfile
from pathlib import Path

import edge_tts

from brainball_voice_processing import process_voice
from generate_brainball_voice_assets import RAW_DIR, RAW_LINES, VoiceLine


SUPPORTED_LOCALES = [
    "ro",
    "es",
    "fr",
    "de",
    "it",
    "pt",
    "nl",
    "pl",
    "tr",
    "ru",
    "uk",
    "ar",
    "hi",
    "id",
    "vi",
    "ja",
    "ko",
    "zh",
]

SPOKEN_NAMES = [
    "Voro Prime",
    "Kav Zero",
    "Chrome Voro",
    "Nova Kav",
    "Kavvoro",
    "Voro Grad",
    "Blop Voro",
    "Fizz Kav",
    "Lala Voro",
    "Womp Kav",
    "Mimi Voro",
    "Zaza Kav",
    "Tikkav Rift",
    "Byte Voro",
    "Globo Kav",
    "Elder Voro",
    "King Kav",
    "Nibbi Kav",
    "Voro Rizz",
    "Bongo Kav",
    "Glitch Nona",
    "Sloppi Voro",
    "Kav Kaboom",
    "Drippi Mim",
    "Nappa Voro",
    "Yappa Kav",
    "Turbo Blob",
    "Wi-Fi Voro",
    "Cringe Kav",
    "Kav four oh four",
    "Pasta Voro",
    "Laggi Kav",
    "Moggo Voro",
    "Brain Bean",
    "Aura Thief",
    "Gigi Glitch",
    "Noodle Kav",
    "Sleepy Voro",
    "Panic Bean",
    "Bossy Blop",
    "Quantum Kav",
    "Wobble CEO",
    "Error Voro",
    "Golden Yap",
    "Void Junior",
    "Kav Max",
    "Rift Rizzler",
    "Ultra Nona",
    "Aura Titan",
    "Final Voro",
]

VOICE_PROFILES = {
    "ro": {"male": "ro-RO-EmilNeural", "female": "ro-RO-AlinaNeural"},
    "es": {"male": "es-ES-AlvaroNeural", "female": "es-ES-XimenaNeural"},
    "fr": {"male": "fr-FR-RemyMultilingualNeural", "female": "fr-FR-VivienneMultilingualNeural"},
    "de": {"male": "de-DE-FlorianMultilingualNeural", "female": "de-DE-SeraphinaMultilingualNeural"},
    "it": {"male": "it-IT-GiuseppeMultilingualNeural", "female": "it-IT-IsabellaNeural"},
    "pt": {"male": "pt-BR-AntonioNeural", "female": "pt-BR-ThalitaMultilingualNeural"},
    "nl": {"male": "nl-NL-MaartenNeural", "female": "nl-NL-ColetteNeural"},
    "pl": {"male": "pl-PL-MarekNeural", "female": "pl-PL-ZofiaNeural"},
    "tr": {"male": "tr-TR-AhmetNeural", "female": "tr-TR-EmelNeural"},
    "ru": {"male": "ru-RU-DmitryNeural", "female": "ru-RU-SvetlanaNeural"},
    "uk": {"male": "uk-UA-OstapNeural", "female": "uk-UA-PolinaNeural"},
    "ar": {"male": "ar-SA-HamedNeural", "female": "ar-SA-ZariyahNeural"},
    "hi": {"male": "hi-IN-MadhurNeural", "female": "hi-IN-SwaraNeural"},
    "id": {"male": "id-ID-ArdiNeural", "female": "id-ID-GadisNeural"},
    "vi": {"male": "vi-VN-NamMinhNeural", "female": "vi-VN-HoaiMyNeural"},
    "ja": {"male": "ja-JP-KeitaNeural", "female": "ja-JP-NanamiNeural"},
    "ko": {"male": "ko-KR-InJoonNeural", "female": "ko-KR-SunHiNeural"},
    "zh": {"male": "zh-CN-YunxiNeural", "female": "zh-CN-XiaoxiaoNeural"},
}

DEFAULT_ROTATION = [
    ("male", "+14%", "+3Hz"),
    ("female", "+16%", "+7Hz"),
    ("male", "+12%", "-4Hz"),
    ("female", "+18%", "+10Hz"),
]

VOICE_OVERRIDES = {
    6: ("female", "+15%", "+4Hz"),    # BLOP VORO - female
    8: ("female", "+8%", "-24Hz"),    # LALA VORO - deep female
    11: ("male", "+24%", "+46Hz"),    # ZAZA KAV - very thin male
    13: ("female", "+25%", "+48Hz"),  # BYTE VORO - very thin female
    14: ("male", "+20%", "+32Hz"),    # GLOBO KAV - thin male
    16: ("male", "+12%", "-7Hz"),     # KING KAV - male
    21: ("male", "+12%", "-9Hz"),     # SLOPPI VORO - male
    37: ("female", "+8%", "-5Hz"),    # SLEEPY VORO - female
}

RO_LINES = [
    ("VORO PRIME", "Voro Prime! Aură de boss, fără plafon!"),
    ("KAV ZERO", "Kav Zero! Zero neuroni, dar accelerează blană!"),
    ("CHROME VORO", "Chrome Voro! Oglindă sigma, bonk de lux!"),
    ("NOVA KAV", "Nova Kav! Rizz galactic, fizica ia pauză!"),
    ("KAVVORO", "Kavvoro! Brainrot original, certificat pe bune!"),
    ("VORO GRAD", "Voro Grad! Diplomă în vorbit mult și aiurea!"),
    ("BLOP VORO", "Blop Voro! Emoțional, rotund și periculos de pufos!"),
    ("FIZZ KAV", "Fizz Kav! A mușcat încărcătorul și tot zboară!"),
    ("LALA VORO", "Lala Voro! Parolă greșită, aură corectă!"),
    ("WOMP KAV", "Womp Kav! Gânduri în buclă, bounce gălăgios!"),
    ("MIMI VORO", "Mimi Voro! Se uită agresiv în patru ka!"),
    ("ZAZA KAV", "Zaza Kav! Tema s-a șters, zoom-ul a rămas!"),
    ("TIKKAV RIFT", "Tikkav Rift! Taxa pe vânt a fost colectată!"),
    ("BYTE VORO", "Byte Voro! Născut direct în butonul de share!"),
    ("GLOBO KAV", "Globo Kav! Gravitația și-a dat demisia!"),
    ("ELDER VORO", "Elder Voro! Yap antic deblocat solemn!"),
    ("KING KAV", "King Kav! Încoronat de aură pură!"),
    ("NIBBI KAV", "Nibbi Kav! A uitat planul, dar tot el e!"),
    ("VORO RIZZ", "Voro Rizz! Dealer de aură la datorie!"),
    ("BONGO KAV", "Bongo Kav! Creierul bate bonk bonk!"),
    ("GLITCH NONA", "Glitch Nona! Buffering în viața reală!"),
    ("SLOPPI VORO", "Sloppi Voro! Legendă cu apă la microunde!"),
    ("KAV KABOOM", "Kav Kaboom! Volum ilegal, vibrație legală!"),
    ("DRIPPI MIM", "Drippi Mim! Teniși invizibili, drip zgomotos!"),
    ("NAPPA VORO", "Nappa Voro! Doarme la viteză maximă!"),
    ("YAPPA KAV", "Yappa Kav! Podcast fără microfon!"),
    ("TURBO BLOB", "Turbo Blob! Fără frâne, cu multe păreri!"),
    ("WIFI VORO", "Wi-Fi Voro! Conectat, dar fără internet!"),
    ("CRINGE KAV", "Cringe Kav! Stânjeneală militarizată!"),
    ("KAV 404", "Kav patru zero patru! Creier negăsit!"),
    ("PASTA VORO", "Pasta Voro! Departamentul de sos e online!"),
    ("LAGGI KAV", "Laggi Kav! Întârzie fix trei cadre!"),
    ("MOGGO VORO", "Moggo Voro! Maxilar randat separat!"),
    ("BRAIN BEAN", "Brain Bean! Un bob, două gânduri, niciun plan!"),
    ("AURA THIEF", "Aura Thief! Vibul tău e acum al meu!"),
    ("GIGI GLITCH", "Gigi Glitch! Update-ul realității a picat!"),
    ("NOODLE KAV", "Noodle Kav! Construit diferit, dar abia!"),
    ("SLEEPY VORO", "Sleepy Voro! Visează leaderboard-ul!"),
    ("PANIC BEAN", "Panic Bean! Modul calm lipsește din meniu!"),
    ("BOSSY BLOP", "Bossy Blop! S-a promovat singur din nou!"),
    ("QUANTUM KAV", "Quantum Kav! Câștigă și crapă simultan!"),
    ("WOBBLE CEO", "Wobble CEO! A cumpărat o firmă imaginară!"),
    ("ERROR VORO", "Error Voro! Prea blestemat pentru compilare!"),
    ("GOLDEN YAP", "Golden Yap! Gură legendară, DLC inclus!"),
    ("VOID JUNIOR", "Void Junior! Void mic, ego uriaș!"),
    ("KAV MAXX", "Kav Max! Slidere ilegale activate!"),
    ("RIFT RIZZLER", "Rift Rizzler! Gravitația s-a abonat!"),
    ("ULTRA NONA", "Ultra Nona! Baterie plină de yap!"),
    ("AURA TITAN", "Aura Titan! Stânjeneala a fost ștearsă!"),
    ("FINAL VORO", "Final Voro! Lore-ul s-a terminat, bounce-ul nu!"),
]

PHRASE_BANKS = {
    "es": [
        "aura máxima, sin freno",
        "cero neuronas, puro zoom",
        "rebote ilegal, flow bendecido",
        "modo caos activado",
        "la física pidió vacaciones",
        "rizz galáctico en pantalla",
        "el botón de share tiembla",
        "demasiado cursed para perder",
        "el streak acaba de despertar",
        "bonk premium con salsa",
        "la gravedad ya no manda",
        "legendario, raro y ridículo",
    ],
    "fr": [
        "aura maximale, aucun frein",
        "zéro neurone, full zoom",
        "rebond interdit, style béni",
        "mode chaos déclenché",
        "la physique prend congé",
        "rizz galactique sur l'écran",
        "le bouton partage panique",
        "trop cursed pour tomber",
        "la série vient de s'éveiller",
        "bonk premium avec sauce",
        "la gravité ne commande plus",
        "légendaire, rare et ridicule",
    ],
    "de": [
        "maximale Aura, keine Bremse",
        "null Hirnzellen, voller Zoom",
        "illegaler Bounce, gesegneter Flow",
        "Chaos-Modus aktiviert",
        "die Physik macht Urlaub",
        "galaktischer Rizz im Display",
        "der Teilen-Knopf zittert",
        "zu cursed zum Verlieren",
        "die Serie ist erwacht",
        "Premium-Bonk mit Sauce",
        "die Schwerkraft hat Pause",
        "legendär, selten und absurd",
    ],
    "it": [
        "aura massima, niente freni",
        "zero neuroni, zoom totale",
        "rimbalzo illegale, flow benedetto",
        "modalità caos attivata",
        "la fisica è in ferie",
        "rizz galattico sullo schermo",
        "il tasto condividi trema",
        "troppo cursed per cadere",
        "la serie si è svegliata",
        "bonk premium con salsa",
        "la gravità non comanda più",
        "leggendario, raro e assurdo",
    ],
    "pt": [
        "aura máxima, sem freio",
        "zero neurônios, zoom total",
        "bounce ilegal, flow abençoado",
        "modo caos ativado",
        "a física tirou férias",
        "rizz galáctico na tela",
        "o botão de share tremeu",
        "cursed demais pra cair",
        "o streak acabou de acordar",
        "bonk premium com molho",
        "a gravidade perdeu o cargo",
        "lendário, raro e absurdo",
    ],
    "nl": [
        "max aura, geen remmen",
        "nul hersencellen, volle zoom",
        "illegale bounce, gezegende flow",
        "chaosmodus staat aan",
        "de natuurkunde neemt vrij",
        "galactische rizz op het scherm",
        "de deelknop begint te trillen",
        "te cursed om te vallen",
        "de streak is wakker",
        "premium bonk met saus",
        "zwaartekracht is niet meer de baas",
        "legendarisch, zeldzaam en absurd",
    ],
    "pl": [
        "maks aura, zero hamulców",
        "zero neuronów, pełny zoom",
        "nielegalny bounce, święty flow",
        "tryb chaos włączony",
        "fizyka poszła na urlop",
        "galaktyczny rizz na ekranie",
        "przycisk share się trzęsie",
        "zbyt cursed żeby spaść",
        "seria właśnie się obudziła",
        "premium bonk z sosem",
        "grawitacja straciła pracę",
        "legendarne, rzadkie i absurdalne",
    ],
    "tr": [
        "maks aura, fren yok",
        "sıfır beyin hücresi, tam zoom",
        "illegal bounce, kutsal flow",
        "kaos modu açıldı",
        "fizik tatile çıktı",
        "ekranda galaktik rizz",
        "paylaş butonu titriyor",
        "düşmek için fazla cursed",
        "seri şimdi uyandı",
        "soslu premium bonk",
        "yer çekimi işsiz kaldı",
        "efsane, nadir ve absürt",
    ],
    "ru": [
        "макс аура, без тормозов",
        "ноль нейронов, полный зум",
        "нелегальный bounce, святой flow",
        "режим хаос включён",
        "физика ушла в отпуск",
        "галактический rizz на экране",
        "кнопка share дрожит",
        "слишком cursed чтобы упасть",
        "серия только что проснулась",
        "премиум bonk с соусом",
        "гравитация потеряла работу",
        "легендарно, редко и абсурдно",
    ],
    "uk": [
        "макс аура, без гальм",
        "нуль нейронів, повний зум",
        "нелегальний bounce, святий flow",
        "режим хаос увімкнено",
        "фізика пішла у відпустку",
        "галактичний rizz на екрані",
        "кнопка share тремтить",
        "занадто cursed щоб впасти",
        "серія щойно прокинулась",
        "преміум bonk із соусом",
        "гравітація втратила роботу",
        "легендарно, рідкісно й абсурдно",
    ],
    "ar": [
        "هالة قصوى، بلا فرامل",
        "صفر خلايا مخ، زوم كامل",
        "ارتداد ممنوع، فلو مبارك",
        "وضع الفوضى اشتغل",
        "الفيزياء أخذت إجازة",
        "ريز مجري على الشاشة",
        "زر المشاركة يرتجف",
        "ملعون أكثر من اللازم ليسقط",
        "السلسلة استيقظت الآن",
        "بونك بريميوم مع صلصة",
        "الجاذبية فقدت الوظيفة",
        "أسطوري ونادر ومضحك",
    ],
    "hi": [
        "मैक्स ऑरा, कोई ब्रेक नहीं",
        "ज़ीरो दिमाग, पूरा ज़ूम",
        "इलीगल बाउंस, धन्य फ्लो",
        "कैओस मोड चालू",
        "फिजिक्स छुट्टी पर है",
        "स्क्रीन पर गैलेक्टिक रिज़",
        "शेयर बटन कांप रहा है",
        "गिरने के लिए बहुत cursed",
        "स्ट्रीक अभी जागी है",
        "प्रीमियम bonk, extra मसाला",
        "ग्रैविटी की नौकरी गई",
        "लेजेंडरी, रेयर और अजीब",
    ],
    "id": [
        "aura maksimal, tanpa rem",
        "nol neuron, zoom penuh",
        "bounce ilegal, flow diberkati",
        "mode chaos menyala",
        "fisika sedang liburan",
        "rizz galaksi di layar",
        "tombol share gemetar",
        "terlalu cursed untuk jatuh",
        "streak baru bangun",
        "bonk premium pakai saus",
        "gravitasi kehilangan kerja",
        "legendaris, langka, dan absurd",
    ],
    "vi": [
        "aura tối đa, không phanh",
        "không nơ-ron, zoom hết cỡ",
        "bounce bất hợp pháp, flow được buff",
        "chế độ hỗn loạn bật rồi",
        "vật lý đi nghỉ phép",
        "rizz thiên hà trên màn hình",
        "nút share đang run",
        "quá cursed để rơi",
        "chuỗi vừa thức dậy",
        "bonk premium có sốt",
        "trọng lực mất việc",
        "huyền thoại, hiếm và vô lý",
    ],
    "ja": [
        "オーラ最大、ブレーキなし",
        "脳細胞ゼロ、全力ズーム",
        "違法バウンド、祝福フロー",
        "カオスモード起動",
        "物理が休暇に入った",
        "画面に銀河リズ",
        "シェアボタンが震えている",
        "落ちるには cursed すぎる",
        "ストリークが目覚めた",
        "プレミアム bonk にソース付き",
        "重力が仕事を失った",
        "伝説、レア、そして変",
    ],
    "ko": [
        "오라 최대, 브레이크 없음",
        "뇌세포 제로, 풀 줌",
        "불법 바운스, 축복받은 플로우",
        "카오스 모드 켜짐",
        "물리가 휴가 갔다",
        "화면에 은하 리즈",
        "공유 버튼이 떨린다",
        "떨어지기엔 너무 cursed",
        "연속 기록이 깨어났다",
        "프리미엄 bonk 소스 추가",
        "중력이 실직했다",
        "전설급, 희귀, 그리고 이상함",
    ],
    "zh": [
        "光环拉满，没有刹车",
        "零脑细胞，全速变焦",
        "非法弹跳，神级节奏",
        "混沌模式已启动",
        "物理学开始休假",
        "屏幕上银河级魅力",
        "分享按钮在发抖",
        "太 cursed 了，不会掉",
        "连胜刚刚醒来",
        "高级 bonk 加酱汁",
        "重力失业了",
        "传说、稀有、还很离谱",
    ],
}


def voice_for(locale: str, index: int) -> tuple[str, str, str]:
    role, rate, pitch = DEFAULT_ROTATION[index % len(DEFAULT_ROTATION)]
    if index < 4 or index >= 40:
        rate = "+22%"
        pitch = "+12Hz" if index % 2 else "-6Hz"
    if index in VOICE_OVERRIDES:
        role, rate, pitch = VOICE_OVERRIDES[index]
    return VOICE_PROFILES[locale][role], rate, pitch


def localized_voice_lines(locale: str) -> list[VoiceLine]:
    if locale not in SUPPORTED_LOCALES:
        raise ValueError(f"Unsupported locale: {locale}")

    if locale == "ro":
        source_lines = RO_LINES
    else:
        phrase_bank = PHRASE_BANKS[locale]
        source_lines = [
            (raw_name, f"{SPOKEN_NAMES[index]}! {phrase_bank[index % len(phrase_bank)]}!")
            for index, (raw_name, _) in enumerate(RAW_LINES)
        ]

    lines: list[VoiceLine] = []
    for index, (name, line) in enumerate(source_lines):
        voice, rate, pitch = voice_for(locale, index)
        lines.append(VoiceLine(name, line, voice, rate, pitch))
    return lines


async def synthesize_to_mp3(item: VoiceLine, mp3_path: Path) -> None:
    for attempt in range(3):
        communicate = edge_tts.Communicate(
            item.line,
            item.voice,
            rate=item.rate,
            pitch=item.pitch,
            volume="+0%",
        )
        try:
            await communicate.save(str(mp3_path))
            return
        except Exception:
            if attempt == 2:
                raise
            await asyncio.sleep(1.5 + attempt)


async def render_item(locale: str, index: int, item: VoiceLine, temp_dir: Path, semaphore: asyncio.Semaphore) -> Path:
    basename = f"brainball_select_{locale}_{index:02d}"
    mp3_path = temp_dir / f"{basename}.mp3"
    ogg_path = temp_dir / f"{basename}.ogg"
    async with semaphore:
        print(f"{locale} {index:02d} {item.name}: {item.voice} {item.rate} {item.pitch}")
        await synthesize_to_mp3(item, mp3_path)
        await asyncio.to_thread(process_voice, mp3_path, ogg_path, index)
    return ogg_path


async def generate(locale: str, jobs: int) -> None:
    RAW_DIR.mkdir(parents=True, exist_ok=True)
    lines = localized_voice_lines(locale)
    with tempfile.TemporaryDirectory(prefix=f"kavvoro_voice_{locale}_") as temp_root:
        temp_dir = Path(temp_root)
        semaphore = asyncio.Semaphore(max(1, jobs))
        render_results = await asyncio.gather(
            *[
                render_item(locale, index, item, temp_dir, semaphore)
                for index, item in enumerate(lines)
            ],
            return_exceptions=True,
        )
        errors = [result for result in render_results if isinstance(result, Exception)]
        if errors:
            raise RuntimeError(f"{locale}: failed to generate {len(errors)} voice assets") from errors[0]
        staged_ogg_paths = [result for result in render_results if isinstance(result, Path)]

        if len(staged_ogg_paths) != 50 or any(not path.exists() for path in staged_ogg_paths):
            raise RuntimeError("Expected 50 generated voice assets before replacing raw files.")

        for index, staged_path in enumerate(staged_ogg_paths):
            target = RAW_DIR / f"brainball_select_{locale}_{index:02d}.ogg"
            shutil.copy2(staged_path, target)


async def generate_many(locales: list[str], jobs: int) -> None:
    for locale in locales:
        await generate(locale, jobs)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--locale", default="ro", choices=SUPPORTED_LOCALES + ["all"])
    parser.add_argument("--jobs", type=int, default=4)
    args = parser.parse_args()
    locales = SUPPORTED_LOCALES if args.locale == "all" else [args.locale]
    asyncio.run(generate_many(locales, args.jobs))


if __name__ == "__main__":
    main()
