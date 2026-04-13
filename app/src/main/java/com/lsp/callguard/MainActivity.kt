package com.lsp.callguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.lsp.callguard.core.theme.CallGuardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CallGuardTheme {
                CallGuardApp()
            }
        }
    }
}