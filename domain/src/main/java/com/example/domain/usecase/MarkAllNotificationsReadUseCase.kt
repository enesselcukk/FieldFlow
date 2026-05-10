package com.example.domain.usecase

import com.example.domain.repository.NotificationRepository
import javax.inject.Inject

class MarkAllNotificationsReadUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke() = repository.markAllRead()
}
