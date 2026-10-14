package com.lsp.callguard.ui.screen.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lsp.callguard.core.phone.PhoneServices
import com.lsp.callguard.data.local.database.CallGuardDatabase
import com.lsp.callguard.data.repository.CallDecisionLogRepository

@Composable
fun rememberCallHistoryViewModel(): CallHistoryViewModel {
    val context = LocalContext.current.applicationContext

    val factory = remember(context) {
        val database = CallGuardDatabase.getInstance(context)
        val repository = CallDecisionLogRepository(
            dao = database.callDecisionLogDao(),
            normalizer = PhoneServices.normalizer
        )

        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CallHistoryViewModel(
                    repository = repository,
                    formatter = PhoneServices.formatter,
                    regionProvider = PhoneServices.regionProvider(context)
                ) as T
            }
        }
    }

    return viewModel(factory = factory)
}

