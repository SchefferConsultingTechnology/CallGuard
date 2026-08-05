package com.lsp.callguard.telecom.screening

import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import com.lsp.callguard.data.local.database.CallGuardDatabase
import com.lsp.callguard.data.local.preferences.SettingsPreferences
import com.lsp.callguard.data.local.preferences.appPreferencesDataStore
import com.lsp.callguard.data.repository.CallDecisionLogRepository
import com.lsp.callguard.domain.engine.CallDecisionEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.lsp.callguard.data.local.preferences.LicensePreferences

class AppCallScreeningService : CallScreeningService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onScreenCall(callDetails: Call.Details) {
        val phoneNumber = callDetails.handle?.schemeSpecificPart

        serviceScope.launch {
            val response = try {
                evaluateCall(phoneNumber)
            } catch (e: Exception) {
                // Fail-open: a bug here should never silently block every call
                // (including emergencies/family). Allow and just log the error.
                Log.e(
                    "CallGuardScreening",
                    "Failed to evaluate call from phone=$phoneNumber, allowing by default",
                    e
                )
                CallResponse.Builder()
                    .setDisallowCall(false)
                    .setRejectCall(false)
                    .build()
            }

            respondToCall(callDetails, response)
        }
    }

    private suspend fun evaluateCall(phoneNumber: String?): CallResponse {
        val database = CallGuardDatabase.getInstance(applicationContext)
        val settingsPreferences = SettingsPreferences(applicationContext.appPreferencesDataStore)

        val settings = settingsPreferences.settingsFlow.first()
        val licensePreferences = LicensePreferences(
            applicationContext.appPreferencesDataStore
        )

        val license = licensePreferences.licenseFlow.first()

        val engine = CallDecisionEngine(
            allowedNumberDao = database.allowedNumberDao(),
            deviceContactDao = database.deviceContactDao()
        )

        val decision = engine.evaluate(
            phone = phoneNumber,
            settings = settings,
            isLicensed = license.isLicensed
        )

        val logRepository = CallDecisionLogRepository(
            dao = database.callDecisionLogDao()
        )

        logRepository.logDecision(
            originalPhone = phoneNumber,
            decision = decision
        )

        Log.d(
            "CallGuardScreening",
            "phone=$phoneNumber allow=${decision.allow} reason=${decision.reason}"
        )

        return if (decision.allow) {
            CallResponse.Builder()
                .setDisallowCall(false)
                .setRejectCall(false)
                .build()
        } else {
            CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSkipCallLog(false)
                .setSkipNotification(false)
                .build()
        }
    }
}