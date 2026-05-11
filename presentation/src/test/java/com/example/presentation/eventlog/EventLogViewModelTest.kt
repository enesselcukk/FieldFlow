package com.example.presentation.eventlog

import com.example.domain.model.EventRecord
import com.example.domain.usecase.event.ObserveAllEventsUseCase
import com.example.domain.usecase.event.UpdateEventNoteUseCase
import com.example.presentation.fakes.RecordingEventRepository
import com.example.presentation.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EventLogViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun loadsEventsFromFlow() = runTest {
        val eventsFlow = MutableStateFlow(
            listOf(
                EventRecord(id = 1L, timestamp = 1L, type = EventRecord.EventType.INTERNET_LOST, note = "a")
            )
        )
        val repo = RecordingEventRepository(eventsFlow)
        val vm = EventLogViewModel(ObserveAllEventsUseCase(repo), UpdateEventNoteUseCase(repo))
        advanceUntilIdle()
        assertEquals(1, vm.uiState.value.events.size)
        assertEquals(1L, vm.uiState.value.events.first().id)
    }

    @Test
    fun onNoteSaveCallsUpdateAndClearsSelection() = runTest {
        val eventsFlow =
            MutableStateFlow(
                listOf(
                    EventRecord(id = 5L, timestamp = 10L, type = EventRecord.EventType.INTERNET_LOST),
                ),
            )
        val repo = RecordingEventRepository(eventsFlow)
        val vm = EventLogViewModel(ObserveAllEventsUseCase(repo), UpdateEventNoteUseCase(repo))
        advanceUntilIdle()
        vm.onEventSelected(vm.uiState.value.events.first())
        vm.onNoteChanged("note text")
        vm.onNoteSave()
        advanceUntilIdle()
        assertEquals(5L to "note text", repo.lastNoteUpdate)
        assertNull(vm.uiState.value.selectedEvent)
        assertEquals("", vm.uiState.value.noteInput)
    }

    @Test
    fun onNoteDismissClearsSheet() {
        val flow = MutableStateFlow(emptyList<EventRecord>())
        val repo = RecordingEventRepository(flow)
        val vm = EventLogViewModel(ObserveAllEventsUseCase(repo), UpdateEventNoteUseCase(repo))
        val event = EventRecord(id = 1L, timestamp = 1L, type = EventRecord.EventType.INTERNET_LOST)
        vm.onEventSelected(event)
        vm.onNoteDismiss()
        assertNull(vm.uiState.value.selectedEvent)
        assertEquals("", vm.uiState.value.noteInput)
    }
}
