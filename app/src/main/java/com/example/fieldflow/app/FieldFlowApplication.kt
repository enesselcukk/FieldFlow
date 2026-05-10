package com.example.fieldflow.app

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.fieldflow.sync.SyncWorker
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration as OsmdroidConfiguration
import javax.inject.Inject

@HiltAndroidApp
class FieldFlowApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

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
