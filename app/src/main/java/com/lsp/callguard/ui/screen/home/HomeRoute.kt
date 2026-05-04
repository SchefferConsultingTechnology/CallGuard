package com.lsp.callguard.ui.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lsp.callguard.data.local.database.CallGuardDatabase
import com.lsp.callguard.data.repository.WhitelistRepository

@Composable
fun HomeRoute(
    onOpenWhitelist: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenPaywall: () -> Unit,
    onOpenProtectionSetup: () -> Unit
) {
    val context = LocalContext.current.applicationContext

    val viewModel: HomeViewModel = viewModel(
        factory = remember(context) {
            val db = CallGuardDatabase.getInstance(context)
            val repository = WhitelistRepository(db.allowedNumberDao())

            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(repository) as T
                }
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()

    HomeScreen(
        uiState = uiState,
        onOpenWhitelist = onOpenWhitelist,
        onOpenSettings = onOpenSettings,
        onOpenPaywall = onOpenPaywall,
        onOpenProtectionSetup = onOpenProtectionSetup
    )
}

