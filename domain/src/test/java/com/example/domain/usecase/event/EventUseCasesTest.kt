package com.example.domain.usecase.event

import com.example.domain.fakes.FakeEventRepository
import com.example.domain.model.EventRecord
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class EventUseCasesTest {

    private val event = EventRecord(
        timestamp = 10L,
        type = EventRecord.EventType.GEOFENCE_ENTER
    )

    @Test
    fun saveRoutesEvent() = runTest {
        val repo = FakeEventRepository()
        SaveEventUseCase(repo)(event)
        assertEquals(event, repo.lastSavedEvent)
    }

    @Test
    fun updateNoteRoutesArgs() = runTest {
        val repo = FakeEventRepository()
        UpdateEventNoteUseCase(repo)(5L, "note")
        assertEquals(5L, repo.lastUpdateNoteId)
        assertEquals("note", repo.lastUpdateNoteContent)
    }

    @Test
    fun observeAllExposesRepoFlow() = runTest {
        val repo = FakeEventRepository(initialAll = listOf(event))
        val items = ObserveAllEventsUseCase(repo)().first()
        assertEquals(listOf(event), items)
    }
}
