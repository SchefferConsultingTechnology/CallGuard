package com.lsp.callguard.ui.screen.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lsp.callguard.data.local.database.CallGuardDatabase
import com.lsp.callguard.data.repository.CallDecisionLogRepository

@Composable
fun rememberCallHistoryViewModel(): CallHistoryViewModel {
    val context = LocalContext.current.applicationContext

    val factory = remember(context) {
        val database = CallGuardDatabase.getInstance(context)
        val repository = CallDecisionLogRepository(database.callDecisionLogDao())

        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CallHistoryViewModel(repository) as T
            }
        }
    }

    return viewModel(factory = factory)
}

