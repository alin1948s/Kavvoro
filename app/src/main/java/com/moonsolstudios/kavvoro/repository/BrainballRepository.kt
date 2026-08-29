package com.moonsolstudios.kavvoro.repository

import com.moonsolstudios.kavvoro.R
import com.moonsolstudios.kavvoro.model.Brainball

object BrainballRepository {
    private val brainballs = listOf(
        Brainball("kavvoro", "Kavvoro", R.drawable.brainball_kavvoro),
        Brainball("nodlo", "Kavvoro", R.drawable.brainball_nodlo),
        Brainball("prism_king", "Prism King", R.drawable.brainball_prism_king),
        Brainball("void_zero", "Void Zero", R.drawable.brainball_void_zero),
        Brainball("chrome_lux", "Chrome Lux", R.drawable.brainball_chrome_lux),
        Brainball("plasma_crown", "Plasma Paparino", R.drawable.brainball_plasma_crown),
        Brainball("curse_grad", "Dottore Malocchio", R.drawable.brainball_curse_grad),
        Brainball("blop_13", "Blop 13", R.drawable.brainball_blop_13),
        Brainball("fizz_nana", "Moka Matto", R.drawable.brainball_fizz_nana),
        Brainball("lala_glitch", "Lala Glitch", R.drawable.brainball_lala_glitch),
        Brainball("womp_loop", "Womp Loop", R.drawable.brainball_womp_loop),
        Brainball("mimi_static", "Mimi Static", R.drawable.brainball_mimi_static),
        Brainball("zaza_volt", "Zaza Kav", R.drawable.brainball_zaza_volt),
        Brainball("tik_rift", "Tikkav Rift", R.drawable.brainball_tik_rift),
        Brainball("byte_baba", "Byte Baba", R.drawable.brainball_byte_baba),
        Brainball("globo_wobble", "Globo Wobble", R.drawable.brainball_globo_wobble),
        Brainball("rift_baba", "Elder Voro", R.drawable.brainball_rift_baba),
        Brainball("king_static", "King Static", R.drawable.brainball_king_static),
        Brainball("nibbi_kav", "Nibbi Kav", R.drawable.brainball_nibbi_kav),
        Brainball("voro_rizz", "Rizzardo Voro", R.drawable.brainball_voro_rizz),
        Brainball("bongo_kav", "Bongo Kav", R.drawable.brainball_bongo_kav),
        Brainball("glitch_nona", "Glitch Nona", R.drawable.brainball_glitch_nona),
        Brainball("sloppi_voro", "Sloppi Voro", R.drawable.brainball_sloppi_voro),
        Brainball("kav_kaboom", "Kav Kaboom", R.drawable.brainball_kav_kaboom),
        Brainball("drippi_mim", "Drippi Mim", R.drawable.brainball_drippi_mim),
        Brainball("nappa_voro", "Nappa Voro", R.drawable.brainball_nappa_voro),
        Brainball("yappa_kav", "Yappa Kav", R.drawable.brainball_yappa_kav),
        Brainball("turbo_blob", "Turbo Blob", R.drawable.brainball_turbo_blob),
        Brainball("wifi_voro", "Wifi Voro", R.drawable.brainball_wifi_voro),
        Brainball("cringe_kav", "Cringe Kav", R.drawable.brainball_cringe_kav),
        Brainball("kav_404", "Kav 404", R.drawable.brainball_kav_404),
        Brainball("pasta_voro", "Pasta Voro", R.drawable.brainball_pasta_voro),
        Brainball("laggi_kav", "Laggi Kav", R.drawable.brainball_laggi_kav),
        Brainball("moggo_voro", "Moggo Voro", R.drawable.brainball_moggo_voro),
        Brainball("brain_bean", "Brain Bean", R.drawable.brainball_brain_bean),
        Brainball("aura_thief", "Aura Thief", R.drawable.brainball_aura_thief),
        Brainball("gigi_glitch", "Gigi Buffer", R.drawable.brainball_gigi_glitch),
        Brainball("noodle_kav", "Noodle Kav", R.drawable.brainball_noodle_kav),
        Brainball("sleepy_voro", "Sleepy Voro", R.drawable.brainball_sleepy_voro),
        Brainball("panic_bean", "Panic Bean", R.drawable.brainball_panic_bean),
        Brainball("bossy_blop", "Ceo Bloppini", R.drawable.brainball_bossy_blop),
        Brainball("quantum_kav", "Quantum Kav", R.drawable.brainball_quantum_kav),
        Brainball("wobble_ceo", "Wobble Ceo", R.drawable.brainball_wobble_ceo),
        Brainball("error_voro", "Error Voro", R.drawable.brainball_error_voro),
        Brainball("golden_yap", "Golden Yap", R.drawable.brainball_golden_yap),
        Brainball("void_junior", "Void Junior", R.drawable.brainball_void_junior),
        Brainball("kav_maxx", "Kav Maxx", R.drawable.brainball_kav_maxx),
        Brainball("rift_rizzler", "Rift Rizzler", R.drawable.brainball_rift_rizzler),
        Brainball("ultra_nona", "Ultra Nona", R.drawable.brainball_ultra_nona),
        Brainball("aura_titan", "Aura Titan", R.drawable.brainball_aura_titan),
        Brainball("final_voro", "Final Voro", R.drawable.brainball_final_voro)
    )

    private val brainballMap = brainballs.associateBy { it.id }

    fun getAll(): List<Brainball> = brainballs

    fun getById(id: String): Brainball {
        return brainballMap[id]
            ?: if (id == "kavvoro") brainballMap["nodlo"] ?: defaultBrainball()
            else if (id == "nodlo") brainballMap["kavvoro"] ?: defaultBrainball()
            else defaultBrainball()
    }

    fun defaultBrainball(): Brainball = brainballs.first()
}
