package com.moonsolstudios.kavvoro.model

enum class Screen {
    MENU,
    GAME,
    COLLECTION,
    LEADERBOARDS,
    LANGUAGE,
    SETTINGS,
    AD
}

enum class MenuState {
    MODES,
    MODE_ACTION
}

enum class MenuButton {
    NONE,
    PLAY,
    CLASSIC,
    CLASSIC_CONTINUE,
    CLASSIC_START,
    START,
    CHAOS,
    CHAOS_START,
    LEADERBOARDS,
    VAULT,
    COLLECTION,
    SETTINGS,
    PRIVACY,
    LANGUAGE,
    SFX,
    MUSIC,
    CONTINUE,
    BACK
}

enum class ButtonId {
    NONE,
    HOME,
    RESTART,
    SHARE,
    NEXT,
    SFX,
    MUSIC,
    CONTINUE,
    AD_CONTINUE
}

enum class SettingsButton {
    NONE,
    BACK,
    HEADER_GEAR,
    MASTER_VOLUME,
    MUSIC_VOLUME,
    SFX_VOLUME,
    HAPTIC,
    SCREEN_SHAKE,
    PERFORMANCE,
    LANGUAGE,
    ACCOUNT,
    PRIVACY,
    TERMS,
    DATA_DELETION,
    ABOUT,
    RESET
}

enum class AdAction {
    NONE,
    NEXT_LEVEL,
    CONTINUE_AFTER_FAIL,
    RESUME_RUN
}
