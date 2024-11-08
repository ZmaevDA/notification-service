package ru.zmaev.notification.repository.specification

import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification
import ru.zmaev.notification.model.dto.request.SubscriptionRequestDto
import ru.zmaev.notification.model.entity.Subscription

class SubscriptionSpecification(private val requestDto: SubscriptionRequestDto) : Specification<Subscription> {


    override fun toPredicate(
        root: Root<Subscription>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate {
        var predicate: Predicate = criteriaBuilder.conjunction()

        if (requestDto.subscriberId.isNotBlank()) {
            predicate = criteriaBuilder
                .and(
                    predicate, criteriaBuilder.equal(root.get<String>("subscriberId"), requestDto.subscriberId)
                )
        }

        if (requestDto.subscribedAtId.isNotBlank()) {
            predicate = criteriaBuilder.and(
                predicate,
                criteriaBuilder.equal(root.get<String>("subscribedAtId"), requestDto.subscribedAtId)
            )
        }

        return predicate
    }
}