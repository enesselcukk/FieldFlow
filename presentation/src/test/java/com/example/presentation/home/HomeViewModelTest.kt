package com.example.presentation.home

import android.Manifest
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.presentation.fakes.MutableTrackingRepository
import com.example.presentation.fakes.StubStatusRepository
import com.example.presentation.test.MainDispatcherRule
import com.example.presentation.test.tapState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowApplication

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun vmWithGrantedLocation(): HomeViewModel {
        ShadowApplication.getInstance().grantPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS,
        )
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        return HomeViewModel(StubStatusRepository(), MutableTrackingRepository(), ctx)
    }

    @Test
    fun permissionsAndBatterySurfaceInUiState() =
        runTest {
            val status = StubStatusRepository(batteryLevel = MutableStateFlow(55))
            ShadowApplication.getInstance().grantPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS,
            )
            val ctx = ApplicationProvider.getApplicationContext<Context>()
            val vm = HomeViewModel(status, MutableTrackingRepository(), ctx)
            tapState(vm.uiState)
            advanceUntilIdle()
            assertTrue(vm.uiState.value.hasFineLocationPermission)
            assertEquals(55, vm.uiState.value.batteryLevel)
        }

    @Test
    fun connectivityFlipsReflectInState() = runTest {
        val status = StubStatusRepository()
        ShadowApplication.getInstance().grantPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS,
        )
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val vm = HomeViewModel(status, MutableTrackingRepository(), ctx)
        tapState(vm.uiState)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isOnline)
        status.connectivity.value = false
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isOnline)
    }

    @Test
    fun toggleTrackingUpdatesCombinedState() = runTest {
        val vm = vmWithGrantedLocation()
        tapState(vm.uiState)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isTracking)
        vm.toggleTracking()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isTracking)
    }
}
