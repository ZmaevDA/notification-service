package ru.zmaev.notification.extension.transformer.toResponse

import ru.zmaev.notification.model.dto.response.NotificationResponseDto
import ru.zmaev.notification.model.entity.Notification

fun Notification.toNotificationResponse(): NotificationResponseDto {
    return NotificationResponseDto(
        id = id!!,
        subscription = subscription!!.toSubscriptionResponse(),
        buildId = buildId!!
    )
}