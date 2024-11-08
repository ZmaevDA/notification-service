package ru.zmaev.notification.model.dto.request

data class NotificationRequestDto(
    val subscriptionId: Long? = null,
    val buildId: Long? = null
)