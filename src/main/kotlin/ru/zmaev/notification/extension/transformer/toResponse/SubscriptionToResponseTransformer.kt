package ru.zmaev.notification.extension.transformer.toResponse

import ru.zmaev.notification.model.dto.response.SubscriptionResponseDto
import ru.zmaev.notification.model.entity.Subscription

fun Subscription.toSubscriptionResponse() : SubscriptionResponseDto {
    return SubscriptionResponseDto(
        id = id!!,
        subscriberId = subscriberId!!,
        subscribedAtId = subscribedAtId!!
    )
}