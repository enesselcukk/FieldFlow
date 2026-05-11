package com.example.presentation.settings

import com.example.domain.model.AppLanguage
import com.example.domain.model.AppTheme
import com.example.presentation.fakes.StubSettingsRepository
import com.example.presentation.test.MainDispatcherRule
import com.example.presentation.test.tapState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun preferencesStartFromRepositorySeed() = runTest {
        val repo = StubSettingsRepository()
        val vm = SettingsViewModel(repo)
        tapState(vm.preferences)
        advanceUntilIdle()
        assertEquals(AppLanguage.ENGLISH, vm.preferences.value.language)
        assertEquals(AppTheme.SYSTEM, vm.preferences.value.theme)
        assertEquals(60, vm.preferences.value.locationIntervalSeconds)
    }

    @Test
    fun setLanguageUpdatesPreferences() = runTest {
        val repo = StubSettingsRepository()
        val vm = SettingsViewModel(repo)
        tapState(vm.preferences)
        advanceUntilIdle()
        vm.setLanguage(AppLanguage.TURKISH)
        advanceUntilIdle()
        assertEquals(AppLanguage.TURKISH, vm.preferences.value.language)
    }

    @Test
    fun setThemeUpdatesPreferences() = runTest {
        val repo = StubSettingsRepository()
        val vm = SettingsViewModel(repo)
        tapState(vm.preferences)
        vm.setTheme(AppTheme.DARK)
        advanceUntilIdle()
        assertEquals(AppTheme.DARK, vm.preferences.value.theme)
    }

    @Test
    fun setLocationIntervalPersistsSeconds() = runTest {
        val repo = StubSettingsRepository()
        val vm = SettingsViewModel(repo)
        tapState(vm.preferences)
        vm.setLocationInterval(120)
        advanceUntilIdle()
        assertEquals(120, vm.preferences.value.locationIntervalSeconds)
    }
}
