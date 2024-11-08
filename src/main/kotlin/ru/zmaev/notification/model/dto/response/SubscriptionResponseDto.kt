package ru.zmaev.notification.model.dto.response

data class SubscriptionResponseDto (
    val id: Long,
    val subscriberId: String,
    val subscribedAtId: String
)
