package com.example.fieldflow

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.fieldflow.activation.AppActivationStore
import com.example.fieldflow.navigation.ActivationRoute
import com.example.fieldflow.navigation.HomeRoute
import com.example.fieldflow.navigation.ScanRoute
import com.example.fieldflow.navigation.SplashRoute
import com.example.fieldflow.ui.theme.FieldFlowTheme
import com.example.presentation.auth.ActivationCodeScreen
import com.example.presentation.auth.IdScanScreen
import com.example.presentation.home.HomeScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val activationStore by lazy { AppActivationStore(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FieldFlowTheme {
                val scope = rememberCoroutineScope()
                val backStack = rememberNavBackStack(SplashRoute)

                LaunchedEffect(Unit) {
                    activationStore.isActivated.collect { activated ->
                        if (activated && backStack.lastOrNull() != HomeRoute) {
                            backStack.clear()
                            backStack.add(HomeRoute)
                        } else if (!activated && backStack.lastOrNull() == SplashRoute) {
                            backStack.clear()
                            backStack.add(ScanRoute)
                        }
                    }
                }

                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    entryProvider = entryProvider {
                        entry<SplashRoute> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {}
                        }

                        entry<ScanRoute> {
                            IdScanScreen(
                                onIdentityDetected = { identity ->
                                    Toast.makeText(
                                        this@MainActivity,
                                        getString(R.string.detected_identity, identity.name, identity.surname),
                                        Toast.LENGTH_LONG
                                    ).show()
                                    backStack.add(ActivationRoute(identity.name, identity.surname))
                                }
                            )
                        }

                        entry<ActivationRoute> {
                            ActivationCodeScreen(
                                expectedCode = ACTIVATION_CODE,
                                onActivationSuccess = {
                                    scope.launch {
                                        activationStore.setActivated(true)
                                    }
                                    Toast.makeText(
                                        this@MainActivity,
                                        getString(R.string.activation_success),
                                        Toast.LENGTH_LONG
                                    ).show()
                                    backStack.clear()
                                    backStack.add(HomeRoute)
                                }
                            )
                        }

                        entry<HomeRoute> {
                            HomeScreen(message = getString(R.string.app_activated_message))
                        }
                    }
                )
            }
        }
    }

    private companion object {
        const val ACTIVATION_CODE = "123456"
    }
}