package ru.zmaev.notification.service.impl

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.zmaev.commonlib.exception.EntityNotFountException
import ru.zmaev.commonlib.exception.NoAccessException
import ru.zmaev.commonlib.model.dto.BuildMessage
import ru.zmaev.commonlib.model.dto.UserInfo
import ru.zmaev.notification.extension.transformer.toResponse.toNotificationResponse
import ru.zmaev.notification.logger.Log
import ru.zmaev.notification.model.dto.request.NotificationRequestDto
import ru.zmaev.notification.model.dto.response.NotificationResponseDto
import ru.zmaev.notification.model.entity.Notification
import ru.zmaev.notification.repository.NotificationRepository
import ru.zmaev.notification.repository.specification.NotificationSpecification
import ru.zmaev.notification.service.EmailService
import ru.zmaev.notification.service.NotificationService
import ru.zmaev.notification.service.SubscriptionService

@Service
class NotificationServiceImpl(
    private val notificationRepository: NotificationRepository,
    private val subscriptionService: SubscriptionService,
    private val emailService: EmailService,
    private val userInfo: UserInfo,
    @Value("\${spring.mail.unsubscribe-link}")
    private val unsubscribeLink: String,
    @Value("\${spring.mail.release-link}")
    private val releaseLink: String
) : NotificationService {

    companion object : Log()

    @Transactional
    override fun createAll(buildMessage: BuildMessage) {
        val subscriptions = subscriptionService.findAllBySubscriberAtId(buildMessage.uuid)
        if (subscriptions.isEmpty()) {
            log.info("Subscriptions are empty")
            return
        }
        subscriptions.forEach {
            val placeholders = buildList<String> {
                add(it.subscribedAtUsername!!)
                add(buildMessage.buildName)
                add(buildMessage.buildDescription)
                add(releaseLink)
                add(unsubscribeLink)
            }
            val savedNotification =
                notificationRepository.save(Notification(buildId = buildMessage.buildId, subscription = it))
            log.info("Saved notification with id: ${savedNotification.id}")
            emailService.send(it.subscriberEmail!!, placeholders)
        }
    }

    override fun findAll(
        pageable: Pageable,
        notificationRequestDto: NotificationRequestDto
    ): Page<NotificationResponseDto> {
        log.info("Fetching notifications")
        val specification = NotificationSpecification(notificationRequestDto)
        return notificationRepository.findAll(specification, pageable).map { it.toNotificationResponse() }
    }

    override fun findById(id: Long): NotificationResponseDto {
        log.info("Fetching notification with id: $id")
        val notification = getNotificationOrTrow(id)
        if (userInfo.userId != notification.subscription!!.subscriberId && !userInfo.role.contains("ROLE_ADMIN")) {
            log.error("User with id: ${userInfo.userId} doesn`t have permissions to do that")
            throw NoAccessException("User with id: ${userInfo.userId} doesn`t have permissions to do that!")
        }
        log.info("Fetched notification with id: $id")
        return notification.toNotificationResponse()
    }

    override fun findAllByUserId(pageable: Pageable): Page<NotificationResponseDto> {
        val notifications = notificationRepository.findAllBySubscriptionSubscriberId(userInfo.userId, pageable)
        return notifications.map { it.toNotificationResponse() }
    }

    @Transactional
    override fun delete(id: Long) {
        val notification = getNotificationOrTrow(id)
        notificationRepository.delete(notification)
    }

    private fun getNotificationOrTrow(id: Long): Notification {
        return notificationRepository.findById(id).orElseThrow {
            throw EntityNotFountException("Notification", id)
        }
    }
}
