package ru.zmaev.notification.extension.transformer.fromRequest

import ru.zmaev.notification.model.dto.request.SubscriptionRequestDto
import ru.zmaev.notification.model.entity.Subscription

fun SubscriptionRequestDto.toSubscriptionEntity() : Subscription {
    return Subscription(
        subscriberId = subscriberId,
        subscribedAtId = subscribedAtId
    )
}