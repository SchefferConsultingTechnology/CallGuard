package com.lsp.callguard.data.telephony

import android.content.Context
import android.telephony.TelephonyManager
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.lsp.callguard.data.local.preferences.PhoneDataPreferences
import com.lsp.callguard.domain.phone.RegionProvider
import java.util.Locale
import kotlin.coroutines.cancellation.CancellationException

/**
 * Region order: SIM → network → system locale → last region seen live (research R5).
 * SIM comes first so national numbers keep working while roaming. None of these needs a
 * runtime permission.
 */
class DeviceRegionProvider(
    context: Context,
    private val preferences: PhoneDataPreferences,
    private val supportedRegions: Set<String> = PhoneNumberUtil.getInstance().supportedRegions
) : RegionProvider {

    private val appContext = context.applicationContext

    override suspend fun defaultRegion(): String? {
        return try {
            val telephonyManager = appContext.getSystemService(TelephonyManager::class.java)
            val liveRegion = resolveRegion(
                sim = telephonyManager?.simCountryIso,
                network = telephonyManager?.networkCountryIso,
                locale = Locale.getDefault().country,
                lastKnown = null,
                supported = supportedRegions
            )

            val lastKnown = preferences.lastKnownRegion()

            if (liveRegion != null) {
                if (liveRegion != lastKnown) {
                    preferences.setLastKnownRegion(liveRegion)
                }
                liveRegion
            } else {
                resolveRegion(null, null, null, lastKnown, supportedRegions)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }
}

internal fun resolveRegion(
    sim: String?,
    network: String?,
    locale: String?,
    lastKnown: String?,
    supported: Set<String>
): String? {
    return sequenceOf(sim, network, locale, lastKnown)
        .mapNotNull { value -> value?.trim()?.uppercase(Locale.ROOT)?.takeIf { it.isNotEmpty() } }
        .firstOrNull { it in supported }
}
