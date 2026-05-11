package com.example.presentation.auth.idscan

import com.example.domain.model.IdentityInfo
import com.example.presentation.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IdScanViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun onOcrSuccessPopulatesNames() = runTest {
        val vm = IdScanViewModel(IdentityTextParser())
        vm.setLoading(false)
        val text =
            """
            NAME Enes
            SURNAME Selçuk
            """.trimIndent()
        vm.onOcrSuccess(text, "missing")
        assertEquals("Enes", vm.uiState.value.name.trim())
        assertEquals("Selçuk", vm.uiState.value.surname.trim())
        assertNull(vm.uiState.value.errorText)
    }

    @Test
    fun onOcrSuccessSetsNotFoundWhenEmptyParse() = runTest {
        val vm = IdScanViewModel(IdentityTextParser())
        vm.onOcrSuccess("no labels here", "not found")
        assertEquals("not found", vm.uiState.value.errorText)
    }

    @Test
    fun clearDetectedIdentityResetsFields() {
        val vm = IdScanViewModel(IdentityTextParser())
        vm.onNameChanged("x")
        vm.onSurnameChanged("y")
        vm.clearDetectedIdentity()
        assertEquals("", vm.uiState.value.name)
        assertEquals("", vm.uiState.value.surname)
        assertNull(vm.uiState.value.errorText)
    }

    @Test
    fun buildIdentityInfoTrimsFields() {
        val vm = IdScanViewModel(IdentityTextParser())
        vm.onNameChanged("  Ali  ")
        vm.onSurnameChanged(" Veli ")
        assertEquals(IdentityInfo(name = "Ali", surname = "Veli"), vm.buildIdentityInfo())
    }

    @Test
    fun setLoadingClearsErrorText() {
        val vm = IdScanViewModel(IdentityTextParser())
        vm.onOcrError("err")
        vm.setLoading(true)
        assertNull(vm.uiState.value.errorText)
    }
}
