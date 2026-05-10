package com.example.domain.usecase.notification

import com.example.domain.repository.NotificationRepository
import javax.inject.Inject

class DeleteAllNotificationsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke() = repository.deleteAll()
}
