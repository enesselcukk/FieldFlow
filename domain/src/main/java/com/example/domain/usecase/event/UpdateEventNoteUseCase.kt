package com.example.domain.usecase.event

import com.example.domain.repository.EventRepository
import javax.inject.Inject

class UpdateEventNoteUseCase @Inject constructor(
    private val repository: EventRepository
) {
    suspend operator fun invoke(id: Long, note: String) = repository.updateNote(id, note)
}
