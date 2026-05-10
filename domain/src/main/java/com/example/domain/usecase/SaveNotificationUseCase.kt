package com.example.domain.usecase

import com.example.domain.model.NotificationRecord
import com.example.domain.repository.NotificationRepository
import javax.inject.Inject

class SaveNotificationUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(record: NotificationRecord) = repository.save(record)
}
