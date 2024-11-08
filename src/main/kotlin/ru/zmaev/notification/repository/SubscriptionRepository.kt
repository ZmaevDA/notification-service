package ru.zmaev.notification.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import ru.zmaev.notification.model.entity.Subscription

@Repository
interface SubscriptionRepository : JpaRepository<Subscription, Long>, JpaSpecificationExecutor<Subscription> {
    fun existsBySubscriberIdAndSubscribedAtId(subscriberId: String, subscribedAtId: String): Boolean

    fun findAllBySubscribedAtId(subscribedAtId: String): List<Subscription>
}