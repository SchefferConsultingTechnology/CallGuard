package com.lsp.callguard.ui.screen.whitelist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lsp.callguard.data.local.database.CallGuardDatabase
import com.lsp.callguard.data.repository.WhitelistRepository

@Composable
fun rememberWhitelistViewModel(): WhitelistViewModel {
    val context = LocalContext.current.applicationContext

    val factory = remember(context) {
        val database = CallGuardDatabase.getInstance(context)
        val repository = WhitelistRepository(database.allowedNumberDao())

        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return WhitelistViewModel(repository) as T
            }
        }
    }

    return viewModel(factory = factory)
}

