with open(r"app\src\main\java\com\moonsolstudios\kavvoro\i18n\KavvoroI18n.kt", "r", encoding="utf-8") as f:
    content = f.read()

# Let's check where `englishSourceTranslation` is
print("Found englishSourceTranslation:", "englishSourceTranslation" in content)
print("Found copyOverrides:", "copyOverrides" in content)
