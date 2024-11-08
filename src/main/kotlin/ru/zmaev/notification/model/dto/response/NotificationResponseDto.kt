package ru.zmaev.notification.model.dto.response

data class NotificationResponseDto(
    val id: Long,
    val buildId: Long,
    val subscription: SubscriptionResponseDto
)