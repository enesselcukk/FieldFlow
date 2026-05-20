package com.example.fieldflow.app

import android.app.Application
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration as OsmdroidConfiguration
import java.io.File
import javax.inject.Inject

@HiltAndroidApp
internal class FieldFlowApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    @Inject lateinit var settingsRepository: SettingsRepository

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private val connectivityManager: ConnectivityManager by lazy {
        getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
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
        runBlocking {
            withContext(Dispatchers.IO) {
                runCatching {
                    val prefs = settingsRepository.preferences.first()
                    SettingsBootstrapPreferences.writeAllSync(
                        this@FieldFlowApplication,
                        prefs,
                    )
                }
            }
        }
        OsmdroidConfiguration.getInstance().apply {
            userAgentValue = packageName
            val basePath = File(cacheDir, "osmdroid").apply { mkdirs() }
            osmdroidBasePath = basePath
            osmdroidTileCache = File(basePath, "tiles").apply { mkdirs() }
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
