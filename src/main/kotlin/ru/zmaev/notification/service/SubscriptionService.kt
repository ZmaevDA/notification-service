package ru.zmaev.notification.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import ru.zmaev.notification.model.dto.request.SubscriptionRequestDto
import ru.zmaev.notification.model.dto.response.SubscriptionResponseDto
import ru.zmaev.notification.model.entity.Subscription

interface SubscriptionService {
    fun create(subscriptionRequestDto: SubscriptionRequestDto): SubscriptionResponseDto

    fun findAll(pageable: Pageable, subscriptionRequestDto: SubscriptionRequestDto): Page<SubscriptionResponseDto>

    fun findById(id: Long): SubscriptionResponseDto

    fun delete(id: Long)

    fun findAllBySubscriberAtId(subscribedAtId: String): List<Subscription>
}