package com.moonsolstudios.kavvoro.ui.controller

import android.graphics.RectF
import com.moonsolstudios.kavvoro.billing.PremiumCatalog
import com.moonsolstudios.kavvoro.billing.PurchaseBridge
import com.moonsolstudios.kavvoro.engine.BallPower
import com.moonsolstudios.kavvoro.model.BallSkin
import com.moonsolstudios.kavvoro.model.CollectionFilter
import com.moonsolstudios.kavvoro.model.UnlockType
import com.moonsolstudios.kavvoro.repository.GameProgressRepository
import com.moonsolstudios.kavvoro.ui.HapticFeedbackCompat

object CollectionTouchController {

    const val COLLECTION_BACK_INDEX = -2
    const val COLLECTION_RESTORE_INDEX = -3
    const val COLLECTION_FILTER_BASE_INDEX = -10

    fun filterMatches(filter: CollectionFilter, skin: BallSkin): Boolean = when (filter) {
        CollectionFilter.ALL -> true
        CollectionFilter.SUPERPOWER -> skin.power != BallPower.NONE
        CollectionFilter.HYPE -> skin.unlock.type == UnlockType.HYPE_COST
        CollectionFilter.PREMIUM -> skin.unlock.type == UnlockType.PREMIUM
        CollectionFilter.COSMETIC -> skin.power == BallPower.NONE && skin.unlock.type != UnlockType.PREMIUM
    }

    fun filteredIndexes(ballSkins: List<BallSkin>, filter: CollectionFilter): List<Int> =
        ballSkins.indices.filter { index -> filterMatches(filter, ballSkins[index]) }

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
        dp: Float,
        backButton: RectF,
        restoreButton: RectF,
        filterRects: List<RectF>,
        itemRects: MutableList<RectF>
    ): Pair<Float, Float> {
        val size = 40f * dp
        backButton.set(contentRight - size, safeTop22, contentRight, safeTop22 + size)
        restoreButton.set(contentRight - 96f * dp, safeTop68, contentRight, safeTop88)

        com.moonsolstudios.kavvoro.layout.ScreenLayoutManager.layoutCollectionFilters(
            side = contentLeft,
            contentWidth = contentRight - contentLeft,
            top = safeTop192,
            filterCount = CollectionFilter.entries.size,
            dp = dp,
            filterRects = filterRects
        )

        val side = contentLeft
        val gap = 10f * dp
        val contentWidth = contentRight - contentLeft
        val columns = when {
            contentWidth >= 840f * dp -> 3
            contentWidth >= 560f * dp -> 2
            else -> 1
        }
        val itemWidth = (contentWidth - gap * (columns - 1)) / columns
        val itemHeight = when (columns) {
            1 -> 98f * dp
            2 -> 110f * dp
            else -> 94f * dp
        }
        val top = viewportTop + 4f * dp - scroll

        while (itemRects.size < ballSkins.size) {
            itemRects += RectF()
        }
        itemRects.forEach { it.setEmpty() }
        val visibleIndexes = filteredIndexes(ballSkins, filter)
        visibleIndexes.forEachIndexed { visibleIndex, skinIndex ->
            val col = visibleIndex % columns
            val row = visibleIndex / columns
            val left = side + col * (itemWidth + gap)
            val itemTop = top + row * (itemHeight + gap)
            itemRects[skinIndex].set(left, itemTop, left + itemWidth, itemTop + itemHeight)
        }
        while (itemRects.size > ballSkins.size) {
            itemRects.removeAt(itemRects.lastIndex)
        }

        val rows = ((visibleIndexes.size + columns - 1) / columns).coerceAtLeast(1)
        val contentHeight = rows * itemHeight + (rows - 1) * gap + 8f * dp
        val viewportHeight = viewportBottom - viewportTop
        val maxScroll = (contentHeight - viewportHeight).coerceAtLeast(0f)
        val clampedScroll = scroll.coerceIn(0f, maxScroll)
        return clampedScroll to maxScroll
    }

    fun calculateAura(skin: BallSkin, ballSkins: List<BallSkin>): Int {
        val index = ballSkins.indexOfFirst { it.id == skin.id }.coerceAtLeast(0)
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
            prefs.edit().putString(GameProgressRepository.SELECTED_SKIN_KEY, skin.id).apply()
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

        if (skin.unlock.type == UnlockType.HYPE_COST) {
            val price = skin.unlock.value.coerceAtLeast(0)
            val balance = hypeBalance()
            if (balance >= price) {
                spendHype(price)
                onSkinSelected(skin.id)
                onFocusSkin(skin.id)
                prefs.edit()
                    .putBoolean(GameProgressRepository.earnedSkinKey(skin.id), true)
                    .putString(GameProgressRepository.SELECTED_SKIN_KEY, skin.id)
                    .apply()
                setMessage("${t("UNLOCKED").uppercase()} ${skin.name} / -${formatHypeAmount(price)} ${t("HYPE").uppercase()}", 3.2f)
                playSoundEvent(com.moonsolstudios.kavvoro.audio.SoundEvent.UNLOCK, skinIndex)
                hapticSequence(
                    arrayOf(
                        HapticFeedbackCompat.confirm to 0L,
                        android.view.HapticFeedbackConstants.LONG_PRESS to 90L
                    )
                )
            } else {
                val missing = (price - balance).coerceAtLeast(0)
                setMessage("${skin.name} ${t("WANTS MORE HYPE")} / ${formatHypeAmount(balance)} / ${formatHypeAmount(price)}  +${formatHypeAmount(missing)}", 3.4f)
            }
            return
        }

        setMessage("${skin.name} ${t("REFUSES YOU")} / ${unlockLongLabel(skin)}", 3.4f)
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
