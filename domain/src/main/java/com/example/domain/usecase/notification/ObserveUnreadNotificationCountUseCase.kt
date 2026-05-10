package com.example.domain.usecase.notification

import com.example.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveUnreadNotificationCountUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    operator fun invoke(): Flow<Int> = repository.observeUnreadCount()
}
