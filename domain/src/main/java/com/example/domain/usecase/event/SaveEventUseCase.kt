package com.example.domain.usecase.event

import com.example.domain.model.EventRecord
import com.example.domain.repository.EventRepository
import javax.inject.Inject

class SaveEventUseCase @Inject constructor(
    private val repository: EventRepository
) {
    suspend operator fun invoke(event: EventRecord) = repository.saveEvent(event)
}
