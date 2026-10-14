package com.lsp.callguard.core.phone

import android.content.Context
import com.lsp.callguard.data.local.preferences.PhoneDataPreferences
import com.lsp.callguard.data.local.preferences.appPreferencesDataStore
import com.lsp.callguard.data.telephony.DeviceRegionProvider
import com.lsp.callguard.domain.phone.PhoneNormalizer
import com.lsp.callguard.domain.phone.PhoneNumberDisplayFormatter
import com.lsp.callguard.domain.phone.RegionProvider

/** Manual wiring for phone-number services, in the same style as the screen factories. */
object PhoneServices {

    val normalizer: PhoneNormalizer by lazy { PhoneNormalizer() }

    val formatter: PhoneNumberDisplayFormatter by lazy { PhoneNumberDisplayFormatter() }

    fun regionProvider(context: Context): RegionProvider {
        val appContext = context.applicationContext

        return DeviceRegionProvider(
            context = appContext,
            preferences = PhoneDataPreferences(appContext.appPreferencesDataStore)
        )
    }
}
