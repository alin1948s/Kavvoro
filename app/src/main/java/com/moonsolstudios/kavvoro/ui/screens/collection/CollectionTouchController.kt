package com.moonsolstudios.kavvoro.ui.screens.collection

import android.graphics.RectF
import androidx.core.content.edit
import com.moonsolstudios.kavvoro.billing.PremiumCatalog
import com.moonsolstudios.kavvoro.billing.PurchaseBridge
import com.moonsolstudios.kavvoro.engine.BallPower
import com.moonsolstudios.kavvoro.model.BallSkin
import com.moonsolstudios.kavvoro.model.CollectionFilter
import com.moonsolstudios.kavvoro.model.CollectionSort
import com.moonsolstudios.kavvoro.model.SkinStyle
import com.moonsolstudios.kavvoro.model.UnlockType
import com.moonsolstudios.kavvoro.repository.GameProgressRepository
import com.moonsolstudios.kavvoro.ui.HapticFeedbackCompat

object CollectionTouchController {

    const val COLLECTION_BACK_INDEX = -2
    const val COLLECTION_RESTORE_INDEX = -3
    const val COLLECTION_HERO_ACTION_INDEX = -4
    const val COLLECTION_SORT_INDEX = -5
    const val COLLECTION_FILTER_BASE_INDEX = -10

    val heroStageRect = RectF()
    val heroActionRect = RectF()
    val sortButtonRect = RectF()
    val backButtonRect = RectF()
    val restoreButtonRect = RectF()
    val filterRects = MutableList(CollectionFilter.entries.size) { RectF() }
    val itemRects = mutableListOf<RectF>()

    fun filterMatches(
        filter: CollectionFilter,
        skin: BallSkin,
        isSkinUnlocked: (BallSkin) -> Boolean = { true }
    ): Boolean = when (filter) {
        CollectionFilter.ALL -> true
        CollectionFilter.OWNED -> isSkinUnlocked(skin)
        CollectionFilter.SUPERPOWER -> skin.power != BallPower.NONE
        CollectionFilter.HYPE -> skin.unlock.type != UnlockType.PREMIUM && skin.unlock.type != UnlockType.DEFAULT
        CollectionFilter.PREMIUM -> skin.unlock.type == UnlockType.PREMIUM
        CollectionFilter.COSMETIC -> skin.power == BallPower.NONE && skin.unlock.type != UnlockType.PREMIUM
    }

    private var cachedFilter: CollectionFilter? = null
    private var cachedSort: CollectionSort? = null
    private var cachedUnlockedMask: Long = -1L
    private var cachedSortedIndexes: List<Int> = emptyList()

    fun sortedFilteredIndexes(
        ballSkins: List<BallSkin>,
        filter: CollectionFilter,
        sort: com.moonsolstudios.kavvoro.model.CollectionSort,
        isSkinUnlocked: (BallSkin) -> Boolean
    ): List<Int> {
        val useCatalogCache = ballSkins === com.moonsolstudios.kavvoro.repository.BallSkinCatalog.ALL_SKINS && ballSkins.size <= 62
        val needsUnlockedCheck = filter == CollectionFilter.OWNED || sort == com.moonsolstudios.kavvoro.model.CollectionSort.OWNED_FIRST
        var unlockedMask = 0L
        if (useCatalogCache) {
            if (needsUnlockedCheck) {
                for (i in 0 until ballSkins.size) {
                    if (isSkinUnlocked(ballSkins[i])) {
                        unlockedMask = unlockedMask or (1L shl i)
                    }
                }
            }
            if (filter == cachedFilter && sort == cachedSort && unlockedMask == cachedUnlockedMask) {
                return cachedSortedIndexes
            }
        }

        val matching = ballSkins.indices.filter { index ->
            if (useCatalogCache && filter == CollectionFilter.OWNED) {
                (unlockedMask and (1L shl index)) != 0L
            } else {
                filterMatches(filter, ballSkins[index], isSkinUnlocked)
            }
        }
        val sorted = when (sort) {
            com.moonsolstudios.kavvoro.model.CollectionSort.AURA_DESC -> matching.sortedByDescending { calculateAura(ballSkins[it], ballSkins) }
            com.moonsolstudios.kavvoro.model.CollectionSort.RARITY -> matching.sortedWith(
                compareByDescending<Int> { ballSkins[it].unlock.type == UnlockType.PREMIUM }
                    .thenByDescending { ballSkins[it].power != BallPower.NONE }
                    .thenByDescending { ballSkins[it].style == SkinStyle.CROWN }
                    .thenByDescending { calculateAura(ballSkins[it], ballSkins) }
            )
            com.moonsolstudios.kavvoro.model.CollectionSort.POWER_FIRST -> matching.sortedWith(
                compareByDescending<Int> { ballSkins[it].power != BallPower.NONE }
                    .thenByDescending { ballSkins[it].unlock.type == UnlockType.PREMIUM }
                    .thenByDescending { calculateAura(ballSkins[it], ballSkins) }
            )
            com.moonsolstudios.kavvoro.model.CollectionSort.NAME_ASC -> matching.sortedBy { ballSkins[it].name }
            com.moonsolstudios.kavvoro.model.CollectionSort.OWNED_FIRST -> matching.sortedWith(
                compareByDescending<Int> {
                    if (useCatalogCache) (unlockedMask and (1L shl it)) != 0L else isSkinUnlocked(ballSkins[it])
                }.thenByDescending { calculateAura(ballSkins[it], ballSkins) }
            )
        }
        if (useCatalogCache) {
            cachedFilter = filter
            cachedSort = sort
            cachedUnlockedMask = unlockedMask
            cachedSortedIndexes = sorted
        }
        return sorted
    }

    fun filterAt(x: Float, y: Float, filterRects: List<RectF>): Int =
        filterRects.indexOfFirst { it.contains(x, y) }

    fun filterActiveIndex(index: Int): Int = COLLECTION_FILTER_BASE_INDEX - index

    fun filterFromActiveIndex(activeIndex: Int, totalFilters: Int): Int {
        if (activeIndex > COLLECTION_FILTER_BASE_INDEX) return -1
        val index = COLLECTION_FILTER_BASE_INDEX - activeIndex
        return if (index in 0 until totalFilters) index else -1
    }

    fun itemAt(x: Float, y: Float, viewportTop: Float, viewportBottom: Float, itemRects: List<RectF>): Int {
        if (y < viewportTop || y > viewportBottom) return -1
        return itemRects.indexOfFirst { it.contains(x, y) }
    }

    fun layoutCollection(
        contentLeft: Float,
        contentRight: Float,
        safeTop22: Float,
        safeTop68: Float,
        safeTop88: Float,
        safeTop192: Float,
        viewportTop: Float,
        viewportBottom: Float,
        scroll: Float,
        ballSkins: List<BallSkin>,
        filter: CollectionFilter,
        sort: CollectionSort = CollectionSort.AURA_DESC,
        isSkinUnlocked: (BallSkin) -> Boolean = { true },
        dp: Float,
        backButton: RectF,
        restoreButton: RectF,
        filterRects: List<RectF>,
        itemRects: MutableList<RectF>
    ): Pair<Float, Float> = CollectionLayoutCalculator.layoutCollection(
        contentLeft = contentLeft,
        contentRight = contentRight,
        safeTop22 = safeTop22,
        safeTop68 = safeTop68,
        safeTop88 = safeTop88,
        safeTop192 = safeTop192,
        viewportTop = viewportTop,
        viewportBottom = viewportBottom,
        scroll = scroll,
        ballSkins = ballSkins,
        filter = filter,
        sort = sort,
        isSkinUnlocked = isSkinUnlocked,
        dp = dp,
        backButton = backButton,
        restoreButton = restoreButton,
        filterRects = filterRects,
        itemRects = itemRects,
        heroStageRect = heroStageRect,
        heroActionRect = heroActionRect,
        sortButtonRect = sortButtonRect
    )

    fun calculateAura(skin: BallSkin, ballSkins: List<BallSkin>): Int {
        val index = if (ballSkins.isEmpty() || ballSkins === com.moonsolstudios.kavvoro.repository.BallSkinCatalog.ALL_SKINS) {
            com.moonsolstudios.kavvoro.repository.BallSkinCatalog.indexOf(skin.id)
        } else {
            ballSkins.indexOfFirst { it.id == skin.id }.takeIf { it >= 0 }
                ?: com.moonsolstudios.kavvoro.repository.BallSkinCatalog.indexOf(skin.id)
        }
        return if (skin.unlock.type == com.moonsolstudios.kavvoro.model.UnlockType.PREMIUM) 9999 - index * 111 else 404 + index * 137
    }

    fun handleSkinTap(
        skin: BallSkin,
        ballSkins: List<BallSkin>,
        isSkinUnlocked: (BallSkin) -> Boolean,
        prefs: android.content.SharedPreferences,
        hypeBalance: () -> Int,
        spendHype: (Int) -> Unit,
        formatHypeAmount: (Int) -> String,
        unlockLongLabel: (BallSkin) -> String,
        unlockShortLabel: (BallSkin) -> String = { "" },
        skinHypePrice: (BallSkin) -> Int? = { null },
        brainballAura: (BallSkin) -> Int = { s -> calculateAura(s, ballSkins) },
        purchaseBridge: PurchaseBridge,
        performHaptic: (Int) -> Unit,
        hapticSequence: (Array<Pair<Int, Long>>) -> Unit,
        playSelection: (Int) -> Unit,
        playSoundEvent: (com.moonsolstudios.kavvoro.audio.SoundEvent, Int) -> Unit,
        t: (String) -> String,
        onSkinSelected: (String) -> Unit,
        onFocusSkin: (String) -> Unit,
        setMessage: (String, Float) -> Unit
    ) {
        performHaptic(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
        onFocusSkin(skin.id)
        val skinIndex = ballSkins.indexOf(skin).coerceAtLeast(0)
        playSelection(skinIndex)
        if (isSkinUnlocked(skin)) {
            onSkinSelected(skin.id)
            onFocusSkin(skin.id)
            prefs.edit { putString(GameProgressRepository.SELECTED_SKIN_KEY, skin.id) }
            setMessage("${skin.name} ${t("IS NOW IN YOUR HEAD")} / ${t("AURA").uppercase()} ${brainballAura(skin)}", 2.4f)
            return
        }

        if (skin.unlock.type == UnlockType.PREMIUM) {
            val productId = PremiumCatalog.skinToProductId[skin.id]
            if (productId != null) {
                setMessage("${t("OPENING GOOGLE PLAY")} / ${skin.name}", 3.4f)
                purchaseBridge.purchase(productId)
            }
            return
        }

        val hypePrice = skinHypePrice(skin)
        if (hypePrice != null && hypePrice > 0) {
            val balance = hypeBalance()
            if (balance >= hypePrice) {
                spendHype(hypePrice)
                onSkinSelected(skin.id)
                onFocusSkin(skin.id)
                prefs.edit {
                    putBoolean(GameProgressRepository.earnedSkinKey(skin.id), true)
                    putString(GameProgressRepository.SELECTED_SKIN_KEY, skin.id)
                }
                setMessage("${t("UNLOCKED").uppercase()} ${skin.name} / -${formatHypeAmount(hypePrice)} ${t("HYPE").uppercase()}", 3.2f)
                playSoundEvent(com.moonsolstudios.kavvoro.audio.SoundEvent.UNLOCK, skinIndex)
                hapticSequence(
                    arrayOf(
                        HapticFeedbackCompat.confirm to 0L,
                        android.view.HapticFeedbackConstants.LONG_PRESS to 90L
                    )
                )
            } else {
                val missing = (hypePrice - balance).coerceAtLeast(0)
                val cond = unlockShortLabel(skin)
                val condSuffix = if (cond.isNotEmpty()) " ${t("OR").uppercase()} $cond" else ""
                setMessage("${skin.name} // ${t("NEEDS").uppercase()} ${formatHypeAmount(hypePrice)} ${t("HYPE").uppercase()} (+${formatHypeAmount(missing)})$condSuffix", 3.4f)
            }
            return
        }

        setMessage("${skin.name} // ${unlockLongLabel(skin)}", 3.4f)
    }

    fun handleTouch(
        event: android.view.MotionEvent,
        layoutCollection: () -> Unit,
        collectionTouchY: Float,
        setTouchY: (Float) -> Unit,
        collectionLastY: Float,
        setLastY: (Float) -> Unit,
        collectionDragging: Boolean,
        setDragging: (Boolean) -> Unit,
        activeCollectionIndex: Int,
        setActiveIndex: (Int) -> Unit,
        collectionScroll: Float,
        setScroll: (Float) -> Unit,
        collectionMaxScroll: Float,
        collectionBackButton: RectF,
        collectionRestoreButton: RectF,
        collectionFilterRects: List<RectF>,
        collectionItemRects: List<RectF>,
        viewportTop: Float,
        viewportBottom: Float,
        ballSkins: List<BallSkin>,
        onBack: () -> Unit,
        onRestore: () -> Unit,
        onFilterSelected: (CollectionFilter) -> Unit,
        onSkinTap: (BallSkin) -> Unit,
        onHeroActionTap: () -> Unit = {},
        onSortCycle: () -> Unit = {},
        performHaptic: (Int) -> Unit,
        dp: Float
    ) {
        when (event.actionMasked) {
            android.view.MotionEvent.ACTION_DOWN -> {
                layoutCollection()
                setTouchY(event.y)
                setLastY(event.y)
                setDragging(false)
                val activeIdx = when {
                    collectionBackButton.contains(event.x, event.y) -> COLLECTION_BACK_INDEX
                    collectionRestoreButton.contains(event.x, event.y) -> COLLECTION_RESTORE_INDEX
                    heroActionRect.contains(event.x, event.y) -> COLLECTION_HERO_ACTION_INDEX
                    sortButtonRect.contains(event.x, event.y) -> COLLECTION_SORT_INDEX
                    filterAt(event.x, event.y, collectionFilterRects) >= 0 -> filterActiveIndex(filterAt(event.x, event.y, collectionFilterRects))
                    else -> itemAt(event.x, event.y, viewportTop, viewportBottom, collectionItemRects)
                }
                setActiveIndex(activeIdx)
            }

            android.view.MotionEvent.ACTION_MOVE -> {
                val dy = event.y - collectionLastY
                if (kotlin.math.abs(event.y - collectionTouchY) > 5f * dp) {
                    setDragging(true)
                    setActiveIndex(-1)
                }
                setScroll((collectionScroll - dy).coerceIn(0f, collectionMaxScroll))
                setLastY(event.y)
            }

            android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                val released = activeCollectionIndex
                setActiveIndex(-1)
                if (!collectionDragging && released == COLLECTION_BACK_INDEX && collectionBackButton.contains(event.x, event.y)) {
                    onBack()
                    performHaptic(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                    return
                }
                if (!collectionDragging && released == COLLECTION_RESTORE_INDEX && collectionRestoreButton.contains(event.x, event.y)) {
                    onRestore()
                    return
                }
                if (!collectionDragging && released == COLLECTION_HERO_ACTION_INDEX && heroActionRect.contains(event.x, event.y)) {
                    onHeroActionTap()
                    performHaptic(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                    return
                }
                if (!collectionDragging && released == COLLECTION_SORT_INDEX && sortButtonRect.contains(event.x, event.y)) {
                    onSortCycle()
                    performHaptic(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                    return
                }
                val releasedFilter = filterFromActiveIndex(released, CollectionFilter.entries.size)
                if (!collectionDragging && releasedFilter >= 0 && filterAt(event.x, event.y, collectionFilterRects) == releasedFilter) {
                    onFilterSelected(CollectionFilter.entries[releasedFilter])
                    performHaptic(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                    return
                }
                if (!collectionDragging && released >= 0 && itemAt(event.x, event.y, viewportTop, viewportBottom, collectionItemRects) == released) {
                    ballSkins.getOrNull(released)?.let { onSkinTap(it) }
                }
            }
        }
    }
}
