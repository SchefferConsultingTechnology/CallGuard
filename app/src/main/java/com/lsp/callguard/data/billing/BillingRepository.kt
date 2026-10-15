package com.lsp.callguard.data.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.InAppMessageParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.lsp.callguard.data.local.preferences.LicensePreferences
import com.lsp.callguard.domain.billing.Entitlement
import com.lsp.callguard.domain.billing.PurchaseSnapshot
import com.lsp.callguard.domain.billing.PurchaseState
import com.lsp.callguard.domain.billing.resolveEntitlement
import com.lsp.callguard.domain.model.PlanCatalog
import com.lsp.callguard.domain.model.Tier
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** One purchasable choice: a subscription base plan, or the one-time product (no base plan). */
data class PurchaseOption(
    val productId: String,
    val basePlanId: String? = null
)

/** A purchasable choice as the store priced it for this user. */
data class StoreOffer(
    val option: PurchaseOption,
    val formattedPrice: String,
    /** The user is eligible for the 7-day trial on this option (the store only returns eligible offers). */
    val hasFreeTrial: Boolean
)

sealed interface CatalogState {
    data object Loading : CatalogState
    data object Unavailable : CatalogState
    data class Ready(val offers: List<StoreOffer>) : CatalogState
}

sealed interface RestoreResult {
    data class Restored(val tier: Tier) : RestoreResult
    data object NothingFound : RestoreResult
    data object StoreUnavailable : RestoreResult
}

/**
 * Wraps Play Billing and mirrors the resolved plan into [LicensePreferences], the cache the rest
 * of the app reads (constitution VI). The cache is only written after a successful store answer,
 * so a paying user is never downgraded for being offline.
 */
class BillingRepository private constructor(
    context: Context,
    private val licensePreferences: LicensePreferences
) : PurchasesUpdatedListener {

    private val appContext = context.applicationContext
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _catalog = MutableStateFlow<CatalogState>(CatalogState.Loading)
    val catalog: StateFlow<CatalogState> = _catalog.asStateFlow()

    @Volatile
    private var productDetails: Map<String, ProductDetails> = emptyMap()

    @Volatile
    private var lastEntitlement: Entitlement? = null

    private val billingClient: BillingClient = BillingClient.newBuilder(appContext)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .enablePrepaidPlans()
                .build()
        )
        .enableAutoServiceReconnection()
        .build()

    fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    repositoryScope.launch {
                        queryProducts()
                        refreshPurchases()
                    }
                } else {
                    Log.w(TAG, "Billing setup failed: ${billingResult.responseCode}")
                    _catalog.value = CatalogState.Unavailable
                }
            }

            override fun onBillingServiceDisconnected() {
                // enableAutoServiceReconnection() reconnects on the next call.
            }
        })
    }

    /** Re-reads the catalog, e.g. from the offer's "try again". */
    fun retryCatalog() {
        _catalog.value = CatalogState.Loading
        repositoryScope.launch { queryProducts() }
    }

    private suspend fun queryProducts() {
        val subscriptionIds = buildList {
            add(PlanCatalog.PLUS)
            if (PlanCatalog.PRO_ON_SALE) add(PlanCatalog.PRO)
        }

        val subscriptions = queryDetails(subscriptionIds, BillingClient.ProductType.SUBS)
        val oneTime = queryDetails(listOf(PlanCatalog.PLUS_LIFETIME), BillingClient.ProductType.INAPP)

        if (subscriptions == null || oneTime == null) {
            _catalog.value = CatalogState.Unavailable
            return
        }

        val details = (subscriptions + oneTime).associateBy { it.productId }
        productDetails = details

        val offers = details.values.flatMap(::offersOf)
        _catalog.value = if (offers.isEmpty()) CatalogState.Unavailable else CatalogState.Ready(offers)
    }

    private suspend fun queryDetails(ids: List<String>, type: String): List<ProductDetails>? {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                ids.map { id ->
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(id)
                        .setProductType(type)
                        .build()
                }
            )
            .build()

        return try {
            val result = billingClient.queryProductDetails(params)
            if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                result.productDetailsList.orEmpty()
            } else {
                Log.w(TAG, "queryProductDetails($type) failed: ${result.billingResult.responseCode}")
                null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "queryProductDetails($type) threw", e)
            null
        }
    }

    private fun offersOf(details: ProductDetails): List<StoreOffer> {
        if (details.productType == BillingClient.ProductType.INAPP) {
            val offer = details.oneTimePurchaseOfferDetailsList?.firstOrNull() ?: return emptyList()
            return listOf(
                StoreOffer(PurchaseOption(details.productId), offer.formattedPrice, hasFreeTrial = false)
            )
        }

        val subscriptionOffers = details.subscriptionOfferDetails.orEmpty()

        return subscriptionOffers
            .filter { it.offerId == null }
            .mapNotNull { base ->
                val price = base.pricingPhases.pricingPhaseList.lastOrNull()?.formattedPrice
                    ?: return@mapNotNull null
                val hasTrial = subscriptionOffers.any {
                    it.basePlanId == base.basePlanId && it.offerId == PlanCatalog.OFFER_ANNUAL_TRIAL
                }

                StoreOffer(PurchaseOption(details.productId, base.basePlanId), price, hasTrial)
            }
    }

    /** Re-reads purchases; returns false when the store could not answer (cache left untouched). */
    suspend fun refreshPurchases(): Boolean {
        val subscriptions = queryPurchases(BillingClient.ProductType.SUBS) ?: return false
        val oneTime = queryPurchases(BillingClient.ProductType.INAPP) ?: return false
        val purchases = subscriptions + oneTime

        purchases
            .filter { it.purchaseState == Purchase.PurchaseState.PURCHASED && !it.isAcknowledged }
            .forEach { acknowledge(it) }

        val entitlement = resolveEntitlement(purchases.map(::snapshotOf))
        lastEntitlement = entitlement
        licensePreferences.save(entitlement)
        return true
    }

    private suspend fun queryPurchases(type: String): List<Purchase>? {
        return try {
            val result = billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(type).build()
            )
            if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                result.purchasesList
            } else {
                Log.w(TAG, "queryPurchases($type) failed: ${result.billingResult.responseCode}")
                null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "queryPurchases($type) threw", e)
            null
        }
    }

    private suspend fun acknowledge(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        // Unacknowledged purchases are refunded by Play (after 3 days; 3 minutes for testers).
        val result = billingClient.acknowledgePurchase(params)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            Log.w(TAG, "acknowledgePurchase failed: ${result.responseCode}")
        }
    }

    suspend fun restorePurchases(): RestoreResult {
        if (!refreshPurchases()) return RestoreResult.StoreUnavailable

        val tier = lastEntitlement?.tier ?: Tier.FREE
        return if (tier == Tier.FREE) RestoreResult.NothingFound else RestoreResult.Restored(tier)
    }

    /** Starts the store purchase sheet; false when the option is not available right now. */
    fun purchase(activity: Activity, option: PurchaseOption): Boolean {
        val details = productDetails[option.productId] ?: return false

        val offerToken = if (details.productType == BillingClient.ProductType.INAPP) {
            details.oneTimePurchaseOfferDetailsList?.firstOrNull()?.offerToken
        } else {
            val forPlan = details.subscriptionOfferDetails.orEmpty()
                .filter { it.basePlanId == option.basePlanId }
            // The store only returns the trial offer to eligible users (FR-003).
            (forPlan.find { it.offerId == PlanCatalog.OFFER_ANNUAL_TRIAL } ?: forPlan.find { it.offerId == null })
                ?.offerToken
        } ?: return false

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .setOfferToken(offerToken)
                        .build()
                )
            )

        replacementFor(option)?.let { flowParams.setSubscriptionUpdateParams(it) }

        val result = billingClient.launchBillingFlow(activity, flowParams.build())
        return result.responseCode == BillingClient.BillingResponseCode.OK
    }

    /** Switching between subscriptions replaces the current one instead of charging twice (US4). */
    private fun replacementFor(option: PurchaseOption): BillingFlowParams.SubscriptionUpdateParams? {
        if (option.productId == PlanCatalog.PLUS_LIFETIME) return null

        val current = lastEntitlement ?: return null
        val currentToken = current.activeSubscriptionToken ?: return null
        val currentProduct = current.activeSubscriptionProduct ?: return null
        val currentTier = PlanCatalog.tierOf(currentProduct) ?: return null
        val targetTier = PlanCatalog.tierOf(option.productId) ?: return null

        val isUpgrade = targetTier > currentTier ||
            (targetTier == currentTier && option.basePlanId == PlanCatalog.BASE_ANNUAL)

        return BillingFlowParams.SubscriptionUpdateParams.newBuilder()
            .setOldPurchaseToken(currentToken)
            .setSubscriptionReplacementMode(
                if (isUpgrade) {
                    BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.WITH_TIME_PRORATION
                } else {
                    BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.DEFERRED
                }
            )
            .build()
    }

    /** Play's own messages for declined payments / grace period; no server needed (research R3). */
    fun showPaymentMessages(activity: Activity) {
        val params = InAppMessageParams.newBuilder()
            .addInAppMessageCategoryToShow(InAppMessageParams.InAppMessageCategoryId.TRANSACTIONAL)
            .build()

        runCatching {
            billingClient.showInAppMessages(activity, params) {
                repositoryScope.launch { refreshPurchases() }
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            repositoryScope.launch { refreshPurchases() }
        } else if (billingResult.responseCode != BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.w(TAG, "onPurchasesUpdated: ${billingResult.responseCode}")
        }
    }

    private fun snapshotOf(purchase: Purchase) = PurchaseSnapshot(
        productIds = purchase.products,
        state = when (purchase.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> PurchaseState.PURCHASED
            Purchase.PurchaseState.PENDING -> PurchaseState.PENDING
            else -> PurchaseState.UNSPECIFIED
        },
        isAutoRenewing = purchase.isAutoRenewing,
        purchaseToken = purchase.purchaseToken
    )

    companion object {
        private const val TAG = "BillingRepository"

        /** Play's subscription management page for this app. */
        fun manageSubscriptionUrl(productId: String?): String {
            val base = "https://play.google.com/store/account/subscriptions?package=com.lsp.callguard"
            return productId?.let { "$base&sku=$it" } ?: base
        }

        @Volatile
        private var INSTANCE: BillingRepository? = null

        fun getInstance(context: Context, licensePreferences: LicensePreferences): BillingRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BillingRepository(context, licensePreferences).also { INSTANCE = it }
            }
        }
    }
}
