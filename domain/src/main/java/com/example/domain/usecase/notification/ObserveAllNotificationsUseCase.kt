package com.example.domain.usecase.notification

import com.example.domain.model.NotificationRecord
import com.example.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAllNotificationsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    operator fun invoke(): Flow<List<NotificationRecord>> = repository.observeAll()
}
