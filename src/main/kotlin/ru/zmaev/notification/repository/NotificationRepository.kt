package ru.zmaev.notification.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.zmaev.notification.model.entity.Notification

@Repository
interface NotificationRepository : JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification> {

    fun findAllBySubscriptionSubscriberId(@Param("uuid") uuid: String, pageable: Pageable): Page<Notification>
}