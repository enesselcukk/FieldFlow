package com.example.domain.usecase

import com.example.domain.model.EventRecord
import com.example.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAllEventsUseCase @Inject constructor(
    private val repository: EventRepository
) {
    operator fun invoke(): Flow<List<EventRecord>> = repository.observeAll()
}
