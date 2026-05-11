package com.example.domain.usecase.tracking

import com.example.domain.fakes.FakeTrackingRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class TrackingUseCasesTest {

    @Test
    fun startDelegates() {
        val repo = FakeTrackingRepository()
        StartTrackingUseCase(repo)()
        assertEquals(1, repo.starts)
    }

    @Test
    fun stopDelegates() {
        val repo = FakeTrackingRepository(tracking = true)
        StopTrackingUseCase(repo)()
        assertEquals(1, repo.stops)
    }
}
