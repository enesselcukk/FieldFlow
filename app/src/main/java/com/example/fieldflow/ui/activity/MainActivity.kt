package com.example.fieldflow.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.fieldflow.activation.AppActivationStore
import com.example.fieldflow.constants.EXTRA_NAVIGATE_TO
import com.example.fieldflow.ui.FieldFlowApp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val activationStore by lazy { AppActivationStore(applicationContext) }

    private val pendingNavDestination = MutableStateFlow<String?>(null)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getStringExtra(EXTRA_NAVIGATE_TO)?.let { pendingNavDestination.value = it }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.getStringExtra(EXTRA_NAVIGATE_TO)?.let { pendingNavDestination.value = it }
        enableEdgeToEdge()
        setContent {
            FieldFlowApp(
                activity = this,
                pendingNavDestination = pendingNavDestination,
                activationStore = activationStore
            )
        }
    }
}
