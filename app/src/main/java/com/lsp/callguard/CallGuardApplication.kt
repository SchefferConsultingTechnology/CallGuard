package com.lsp.callguard

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.lsp.callguard.core.phone.PhoneServices
import com.lsp.callguard.data.billing.BillingRepository
import com.lsp.callguard.data.local.database.CallGuardDatabase
import com.lsp.callguard.data.local.preferences.LicensePreferences
import com.lsp.callguard.data.local.preferences.PhoneDataPreferences
import com.lsp.callguard.data.local.preferences.SettingsPreferences
import com.lsp.callguard.data.local.preferences.appPreferencesDataStore
import com.lsp.callguard.data.migration.PhoneDataRenormalizer
import com.lsp.callguard.data.repository.ContactsRepository
import com.lsp.callguard.data.repository.DeviceContactCacheRepository
import com.lsp.callguard.domain.phone.RegionProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CallGuardApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        val licensePreferences = LicensePreferences(appPreferencesDataStore)
        BillingRepository.getInstance(this, licensePreferences).startConnection()

        applicationScope.launch {
            val regionProvider = PhoneServices.regionProvider(this@CallGuardApplication)

            // Loads phone metadata off the main thread so the first screened call doesn't pay for it.
            runCatching {
                PhoneServices.normalizer.normalize("0", regionProvider.defaultRegion())
            }

            phoneDataRenormalizer(regionProvider).runIfNeeded()
        }
    }

    private fun phoneDataRenormalizer(regionProvider: RegionProvider): PhoneDataRenormalizer {
        val database = CallGuardDatabase.getInstance(this)
        val settingsPreferences = SettingsPreferences(appPreferencesDataStore)
        val contactsRepository = ContactsRepository(this, regionProvider)

        return PhoneDataRenormalizer(
            allowedNumberDao = database.allowedNumberDao(),
            contactCache = DeviceContactCacheRepository(database.deviceContactDao()),
            preferences = PhoneDataPreferences(appPreferencesDataStore),
            hasContactsConsent = {
                settingsPreferences.settingsFlow.first().hasAcceptedContactsConsent
            },
            hasContactsPermission = {
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) ==
                    PackageManager.PERMISSION_GRANTED
            },
            loadDeviceContacts = contactsRepository::getDeviceContacts,
            normalizer = PhoneServices.normalizer,
            regionProvider = regionProvider
        )
    }
}
