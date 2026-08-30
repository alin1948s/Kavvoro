package com.moonsolstudios.kavvoro.billing

import android.app.Activity
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.moonsolstudios.kavvoro.i18n.KavvoroI18n
import com.moonsolstudios.kavvoro.ui.ChaosGameView

class PlayBillingController(private val activity: Activity) :
    ChaosGameView.PurchaseBridge,
    PurchasesUpdatedListener {

    interface Listener {
        fun onPremiumPricesUpdated(pricesByProductId: Map<String, String>)
        fun onPremiumEntitlementsSynced(ownedProductIds: Set<String>)
        fun onBillingMessage(message: String)
    }

    var listener: Listener? = null
    private val productDetails = mutableMapOf<String, ProductDetails>()
    private var connecting = false
    private var pendingPurchaseId: String? = null
    private var restoreRequested = false
    private var closed = false
    private var started = false

    private val billingClient = BillingClient.newBuilder(activity)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .enableAutoServiceReconnection()
        .build()

    fun start() {
        if (closed || started) return
        started = true
        connect()
    }

    fun refreshPurchases() {
        if (closed || !started) return
        if (billingClient.isReady) {
            queryOwnedProducts(showResult = false)
        } else {
            connect()
        }
    }

    override fun purchase(productId: String) {
        if (productId !in PremiumCatalog.productIds) return
        pendingPurchaseId = productId
        if (!billingClient.isReady) {
            postMessage("CONNECTING TO GOOGLE PLAY")
            connect()
            return
        }
        val details = productDetails[productId]
        if (details == null) {
            postMessage("LOADING LOCAL PRICE")
            queryProducts()
            return
        }
        pendingPurchaseId = null
        launchPurchase(details)
    }

    override fun restore() {
        if (closed) return
        restoreRequested = true
        if (billingClient.isReady) {
            queryOwnedProducts(showResult = true)
        } else {
            postMessage("CONNECTING TO GOOGLE PLAY")
            connect()
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                val completed = purchases.orEmpty().filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                completed.forEach(::acknowledgeIfNeeded)
                if (completed.isNotEmpty()) postMessage("PURCHASE RESTORED TO THE VAULT")
                queryOwnedProducts(showResult = false)
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> postMessage("PURCHASE CANCELLED")
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                postMessage("ALREADY OWNED / RESTORING")
                queryOwnedProducts(showResult = true)
            }

            else -> postMessage("${KavvoroI18n.t(activity, "GOOGLE PLAY ERROR")} ${result.responseCode}")
        }
    }

    fun close() {
        if (closed) return
        closed = true
        listener = null
        billingClient.endConnection()
    }

    private fun connect() {
        if (closed || connecting || billingClient.isReady) return
        connecting = true
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                connecting = false
                if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                    postMessage("GOOGLE PLAY BILLING UNAVAILABLE")
                    return
                }
                queryProducts()
                queryOwnedProducts(showResult = restoreRequested)
            }

            override fun onBillingServiceDisconnected() {
                connecting = false
            }
        })
    }

    private fun queryProducts() {
        if (!billingClient.isReady) return
        val products = PremiumCatalog.productIds.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(products)
            .build()
        billingClient.queryProductDetailsAsync(params) { result, queryResult ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) return@queryProductDetailsAsync
            queryResult.productDetailsList.forEach { details -> productDetails[details.productId] = details }
            val prices = productDetails.mapValues { (_, details) ->
                details.oneTimePurchaseOfferDetailsList?.firstOrNull()?.formattedPrice
                    ?: details.oneTimePurchaseOfferDetails?.formattedPrice
                    ?: "0.99"
            }
            activity.runOnUiThread { listener?.onPremiumPricesUpdated(prices) }
            val pending = pendingPurchaseId ?: return@queryProductDetailsAsync
            pendingPurchaseId = null
            val pendingDetails = productDetails[pending]
            if (pendingDetails != null) {
                launchPurchase(pendingDetails)
            } else {
                postMessage("PRODUCT NOT ACTIVE IN PLAY CONSOLE")
            }
        }
    }

    private fun queryOwnedProducts(showResult: Boolean) {
        if (!billingClient.isReady) return
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        billingClient.queryPurchasesAsync(params) { result, purchases ->
            restoreRequested = false
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                if (showResult) postMessage("RESTORE FAILED / CHECK CONNECTION")
                return@queryPurchasesAsync
            }
            val completed = purchases.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
            completed.forEach(::acknowledgeIfNeeded)
            val owned = completed.flatMap { it.products }.filter { it in PremiumCatalog.productIds }.toSet()
            activity.runOnUiThread {
                listener?.onPremiumEntitlementsSynced(owned)
                if (showResult) {
                    listener?.onBillingMessage(
                        if (owned.isEmpty()) {
                            KavvoroI18n.t(activity, "NO PREMIUM BRAINBALLS FOUND")
                        } else {
                            KavvoroI18n.t(activity, "RESTORED PREMIUM BRAINBALLS")
                                .replace("%d", owned.size.toString())
                        }
                    )
                }
            }
        }
    }

    private fun launchPurchase(details: ProductDetails) {
        val productBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
        details.oneTimePurchaseOfferDetailsList?.firstOrNull()?.offerToken?.takeIf { it.isNotBlank() }?.let {
            productBuilder.setOfferToken(it)
        }
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productBuilder.build()))
            .build()
        val result = billingClient.launchBillingFlow(activity, params)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            postMessage("PURCHASE COULD NOT START")
        }
    }

    private fun acknowledgeIfNeeded(purchase: Purchase) {
        if (purchase.isAcknowledged || purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient.acknowledgePurchase(params) { result ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                postMessage("PURCHASE SAVED / CONFIRMATION RETRYING")
            }
        }
    }

    private fun postMessage(message: String) {
        activity.runOnUiThread { listener?.onBillingMessage(KavvoroI18n.t(activity, message)) }
    }
}
