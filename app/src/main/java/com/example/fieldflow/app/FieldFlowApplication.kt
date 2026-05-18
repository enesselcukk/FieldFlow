package com.example.fieldflow.app

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.domain.repository.SettingsRepository
import com.example.fieldflow.sync.SyncWorker
import com.example.utils.settings.SettingsBootstrapPreferences
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration as OsmdroidConfiguration
import javax.inject.Inject

@HiltAndroidApp
internal class FieldFlowApplication : Application(), Configuration.Provider {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Inject lateinit var workerFactory: HiltWorkerFactory

    @Inject lateinit var settingsRepository: SettingsRepository

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private val connectivityManager: ConnectivityManager by lazy {
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(
            network: Network,
            capabilities: NetworkCapabilities
        ) {
            val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            if (hasInternet) {
                SyncWorker.schedule(applicationContext)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            runCatching {
                val prefs = settingsRepository.preferences.first()
                SettingsBootstrapPreferences.writeAllSync(
                    this@FieldFlowApplication,
                    prefs,
                )
            }
        }
        OsmdroidConfiguration.getInstance().apply {
            userAgentValue = packageName
            osmdroidTileCache = cacheDir
        }
        registerNetworkCallback()
        SyncWorker.schedule(this)
        SyncWorker.schedulePeriodicFallback(this)
    }

    private fun registerNetworkCallback() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
        try {
            connectivityManager.registerNetworkCallback(request, networkCallback)
        } catch (_: Exception) { }
    }

    override fun onTerminate() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (_: Exception) { }
        super.onTerminate()
    }
}
