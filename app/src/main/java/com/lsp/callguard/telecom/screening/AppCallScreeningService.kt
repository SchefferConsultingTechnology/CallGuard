package com.lsp.callguard.telecom.screening

import android.telecom.Call
import android.telecom.CallScreeningService
import com.lsp.callguard.data.local.database.CallGuardDatabase
import com.lsp.callguard.data.local.preferences.SettingsPreferences
import com.lsp.callguard.data.local.preferences.appPreferencesDataStore
import com.lsp.callguard.domain.engine.CallDecisionEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AppCallScreeningService : CallScreeningService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onScreenCall(callDetails: Call.Details) {
        val phoneNumber = callDetails.handle?.schemeSpecificPart

        serviceScope.launch {
            val database = CallGuardDatabase.getInstance(applicationContext)
            val settingsPreferences = SettingsPreferences(applicationContext.appPreferencesDataStore)

            val settings = settingsPreferences.settingsFlow.first()

            val engine = CallDecisionEngine(
                allowedNumberDao = database.allowedNumberDao()
            )

            val decision = engine.evaluate(
                phone = phoneNumber,
                settings = settings
            )

            android.util.Log.d(
                "CallGuardScreening",
                "phone=$phoneNumber allow=${decision.allow} reason=${decision.reason}"
            )

            val response = if (decision.allow) {
                CallResponse.Builder()
                    .setDisallowCall(false)
                    .setRejectCall(false)
//                    .setSilenceCall(false)
                    .build()
            } else {
                CallResponse.Builder()
                    .setDisallowCall(true)
                    .setRejectCall(true)
                    .setSkipCallLog(false)
                    .setSkipNotification(false)
                    .build()
            }

            respondToCall(callDetails, response)
        }
    }
}