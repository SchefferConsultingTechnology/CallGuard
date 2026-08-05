package com.lsp.callguard

import android.app.Application
import com.lsp.callguard.data.billing.BillingRepository
import com.lsp.callguard.data.local.preferences.LicensePreferences
import com.lsp.callguard.data.local.preferences.appPreferencesDataStore

class CallGuardApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val licensePreferences = LicensePreferences(appPreferencesDataStore)
        BillingRepository.getInstance(this, licensePreferences).startConnection()
    }
}
