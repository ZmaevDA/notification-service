package ru.zmaev.notification.repository.specification

import jakarta.persistence.criteria.*
import org.springframework.data.jpa.domain.Specification
import ru.zmaev.notification.model.dto.request.NotificationRequestDto
import ru.zmaev.notification.model.entity.Notification
import ru.zmaev.notification.model.entity.Subscription

class NotificationSpecification(private val requestDto: NotificationRequestDto) : Specification<Notification> {


    override fun toPredicate(
        root: Root<Notification>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate {
        var predicate: Predicate = criteriaBuilder.conjunction()

        val subscriptionJoin: Join<Notification, Subscription> = root.join("subscription")

        if (requestDto.subscriptionId != null) {
            predicate = criteriaBuilder.and(
                predicate,
                criteriaBuilder.equal(subscriptionJoin.get<Long>("id"), requestDto.subscriptionId)
            )
        }

        if (requestDto.buildId != null) {
            predicate = criteriaBuilder.and(
                predicate,
                criteriaBuilder.equal(root.get<String>("buildId"), requestDto.buildId)
            )
        }

        return predicate
    }
}