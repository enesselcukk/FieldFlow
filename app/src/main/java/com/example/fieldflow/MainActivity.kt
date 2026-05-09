package com.example.fieldflow

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.fieldflow.activation.AppActivationStore
import com.example.fieldflow.navigation.ActivationRoute
import com.example.fieldflow.navigation.BiometricRoute
import com.example.fieldflow.navigation.HomeRoute
import com.example.fieldflow.navigation.ScanRoute
import com.example.fieldflow.navigation.SplashRoute
import com.example.fieldflow.ui.theme.FieldFlowTheme
import com.example.presentation.auth.activation.ActivationCodeScreen
import com.example.presentation.auth.biometric.BiometricAuthScreen
import com.example.presentation.auth.idscan.IdScanScreen
import com.example.presentation.home.HomeScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val activationStore by lazy { AppActivationStore(applicationContext) }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FieldFlowTheme {
                val scope = rememberCoroutineScope()
                val backStack = rememberNavBackStack(SplashRoute)
                var isBiometricVerified by rememberSaveable { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    activationStore.isActivated.collect { activated ->
                        if (activated && isBiometricVerified && backStack.lastOrNull() != HomeRoute) {
                            backStack.clear()
                            backStack.add(HomeRoute)
                        } else if (activated && !isBiometricVerified && backStack.lastOrNull() != BiometricRoute) {
                            backStack.clear()
                            backStack.add(BiometricRoute)
                        } else if (!activated && backStack.lastOrNull() == SplashRoute) {
                            backStack.clear()
                            backStack.add(ScanRoute)
                        }
                    }
                }

                val currentRoute = backStack.lastOrNull()

                val topBarTitle = when (currentRoute) {
                    is ScanRoute -> stringResource(R.string.topbar_scan)
                    is ActivationRoute -> stringResource(R.string.topbar_activation)
                    is BiometricRoute -> stringResource(R.string.topbar_biometric)
                    is HomeRoute -> stringResource(R.string.topbar_home)
                    else -> stringResource(R.string.app_name)
                }

                val showTopBar = currentRoute !is SplashRoute
                val canGoBack = backStack.size > 1 &&
                    currentRoute !is HomeRoute &&
                    currentRoute !is SplashRoute

                Scaffold(
                    topBar = {
                        if (showTopBar) {
                            TopAppBar(
                                title = {
                                    Text(
                                        text = topBarTitle,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                },
                                navigationIcon = {
                                    if (canGoBack) {
                                        IconButton(onClick = { backStack.removeLastOrNull() }) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "Geri"
                                            )
                                        }
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    titleContentColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        NavDisplay(
                            backStack = backStack,
                            onBack = { backStack.removeLastOrNull() },
                            entryProvider = entryProvider {
                                entry<SplashRoute> {
                                    Box(modifier = Modifier.fillMaxSize())
                                }

                                entry<ScanRoute> {
                                    IdScanScreen(
                                        onIdentityDetected = { identity ->
                                            Toast.makeText(
                                                this@MainActivity,
                                                getString(
                                                    R.string.detected_identity,
                                                    identity.name,
                                                    identity.surname
                                                ),
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
                                            isBiometricVerified = true
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

                                entry<BiometricRoute> {
                                    BiometricAuthScreen(
                                        onAuthenticated = {
                                            isBiometricVerified = true
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
        }
    }

    private companion object {
        const val ACTIVATION_CODE = "123456"
    }
}