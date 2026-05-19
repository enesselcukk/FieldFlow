package com.example.presentation.auth.idscan

import com.example.domain.model.IdentityInfo
import com.example.presentation.auth.idscan.fixtures.IdScanOcrFixtures
import com.example.presentation.auth.idscan.parser.IdentityTextParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class IdScanViewModelTest {

    private lateinit var parser: IdentityTextParser
    private lateinit var viewModel: IdScanViewModel

    @Before
    fun setUp() {
        parser = IdentityTextParser()
        viewModel = IdScanViewModel(parser)
    }

    @Test
    fun onOcrSuccess_populatesParsedNameAndSurname() {
        viewModel.setLoading(false)

        viewModel.onOcrSuccess(IdScanOcrFixtures.latinNameSurname, IdScanOcrFixtures.NOT_FOUND_MESSAGE)

        assertEquals("Enes", viewModel.uiState.value.name.trim())
        assertEquals("Selçuk", viewModel.uiState.value.surname.trim())
        assertNull(viewModel.uiState.value.errorText)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun onOcrSuccess_setsNotFoundMessageWhenParserReturnsEmpty() {
        viewModel.onOcrSuccess(IdScanOcrFixtures.EMPTY_LABELS, IdScanOcrFixtures.NOT_FOUND_MESSAGE)

        assertEquals(IdScanOcrFixtures.NOT_FOUND_MESSAGE, viewModel.uiState.value.errorText)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun onOcrError_stopsLoadingAndSurfacesMessage() {
        viewModel.setLoading(true)

        viewModel.onOcrError("OCR failed")

        assertEquals("OCR failed", viewModel.uiState.value.errorText)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun setLoadingTrue_clearsErrorText() {
        viewModel.onOcrError("err")

        viewModel.setLoading(true)

        assertNull(viewModel.uiState.value.errorText)
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun setLoadingFalse_preservesErrorText() {
        viewModel.onOcrError("err")

        viewModel.setLoading(false)

        assertEquals("err", viewModel.uiState.value.errorText)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun onNameChanged_andOnSurnameChanged_updateUiState() {
        viewModel.onNameChanged("Ali")
        viewModel.onSurnameChanged("Veli")

        assertEquals("Ali", viewModel.uiState.value.name)
        assertEquals("Veli", viewModel.uiState.value.surname)
    }

    @Test
    fun clearDetectedIdentity_resetsFieldsAndError() {
        viewModel.onNameChanged("x")
        viewModel.onSurnameChanged("y")
        viewModel.onOcrError("err")

        viewModel.clearDetectedIdentity()

        assertEquals("", viewModel.uiState.value.name)
        assertEquals("", viewModel.uiState.value.surname)
        assertNull(viewModel.uiState.value.errorText)
    }

    @Test
    fun buildIdentityInfo_trimsManualInput() {
        viewModel.onNameChanged("  Ali  ")
        viewModel.onSurnameChanged(" Veli ")

        assertEquals(IdentityInfo(name = "Ali", surname = "Veli"), viewModel.buildIdentityInfo())
    }
}
