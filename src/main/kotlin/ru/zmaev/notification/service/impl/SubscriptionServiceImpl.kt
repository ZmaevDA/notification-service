package ru.zmaev.notification.service.impl

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.zmaev.commonlib.api.SupportServiceApi
import ru.zmaev.commonlib.exception.EntityConflictException
import ru.zmaev.commonlib.exception.EntityNotFountException
import ru.zmaev.commonlib.exception.InternalServerException
import ru.zmaev.commonlib.exception.NoAccessException
import ru.zmaev.commonlib.model.dto.UserInfo
import ru.zmaev.commonlib.model.dto.response.EntityIsExistsResponseDto
import ru.zmaev.commonlib.model.dto.response.UserInnerResponseDto
import ru.zmaev.commonlib.model.enums.Role
import ru.zmaev.notification.extension.transformer.fromRequest.toSubscriptionEntity
import ru.zmaev.notification.extension.transformer.toResponse.toSubscriptionResponse
import ru.zmaev.notification.logger.Log
import ru.zmaev.notification.model.dto.request.SubscriptionRequestDto
import ru.zmaev.notification.model.dto.response.SubscriptionResponseDto
import ru.zmaev.notification.model.entity.Subscription
import ru.zmaev.notification.repository.SubscriptionRepository
import ru.zmaev.notification.repository.specification.SubscriptionSpecification
import ru.zmaev.notification.service.SubscriptionService

@Service
class SubscriptionServiceImpl(
    private val subscriptionRepository: SubscriptionRepository,
    private val supportServiceApi: SupportServiceApi,
    private val userInfo: UserInfo
) : SubscriptionService {

    companion object : Log()

    @Transactional
    override fun create(subscriptionRequestDto: SubscriptionRequestDto): SubscriptionResponseDto {
        log.info("Creating user with request dto: $subscriptionRequestDto")
        val subscriberId = subscriptionRequestDto.subscriberId
        val subscribedAtId = subscriptionRequestDto.subscribedAtId
        val subscription = subscriptionRequestDto.toSubscriptionEntity()

        if (userInfo.userId != subscriberId) {
            throw NoAccessException("User with uuid: ${userInfo.userId} can`t subscribe user with uuid: $subscriberId")
        }

        if (subscriptionRequestDto.subscriberId == subscriptionRequestDto.subscribedAtId) {
            log.error("User can`t be subscribed on himself!")
            throw EntityConflictException("User can`t be subscribed on himself!")
        }

        val subscribedAt = findUserById(subscribedAtId)
        val subscriber = findUserById(subscriberId)
        subscription.subscribedAtUsername = subscribedAt.username
        subscription.subscriberEmail = subscriber.email

        if (existsBySubscriberIdAndSubscribedAtId(subscriberId, subscribedAtId)) {
            log.error("User with uuid: $subscriberId already subscribed to user with uuid: $subscribedAtId")
            throw EntityConflictException("User with uuid: $subscriberId already subscribed to user with uuid: $subscribedAtId")
        }

        val savedSubscription = subscriptionRepository.save(subscription)
        log.info("Subscription created with id: ${savedSubscription.id}")
        return savedSubscription.toSubscriptionResponse()
    }

    override fun findAll(
        pageable: Pageable,
        subscriptionRequestDto: SubscriptionRequestDto
    ): Page<SubscriptionResponseDto> {
        val specification = SubscriptionSpecification(subscriptionRequestDto)
        return subscriptionRepository.findAll(specification, pageable).map { it.toSubscriptionResponse() }
    }

    override fun findById(id: Long): SubscriptionResponseDto {
        val subscription = getSubscriptionOrThrow(id)
        if (subscription.subscriberId != userInfo.userId &&
            !userInfo.role.contains(Role.ROLE_ADMIN.name)
        ) {
            throw NoAccessException("Can not access this subscribe!")
        }
        return subscription.toSubscriptionResponse()
    }

    @Transactional
    override fun delete(id: Long) {
        val subscription = getSubscriptionOrThrow(id)
        if (subscription.subscriberId != userInfo.userId &&
            !userInfo.role.contains(Role.ROLE_ADMIN.name)
        ) {
            throw NoAccessException("Can not access this subscribe!")
        }
        subscriptionRepository.delete(subscription)
    }

    override fun findAllBySubscriberAtId(subscribedAtId: String): List<Subscription> {
        return subscriptionRepository.findAllBySubscribedAtId(subscribedAtId)
    }

    fun findUserById(id: String): UserInnerResponseDto {
        log.info("Fetching user with id: $id")
        val call = supportServiceApi.findUserById(id, "inner")
        val response = call.execute()

        if (response.code() == 404) {
            log.error("Can't fetch user with id: $id")
            throw EntityNotFountException("User", id)
        }

        return if (response.isSuccessful) {
            val responseBody = response.body()
            if (responseBody is UserInnerResponseDto) {
                log.info("Fetched user with id: $id")
                responseBody
            } else {
                log.error("Unexpected response type for user with id: $id")
                throw InternalServerException("Unexpected response type")
            }
        } else {
            log.error("Response body is null or the response was not successful for user with id: $id")
            throw InternalServerException("Response body is null or the response was not successful")
        }
    }

    fun isUserExistsById(id: String): EntityIsExistsResponseDto {
        log.info("Fetching build with id: $id")
        val call = supportServiceApi.userExistsById(id)
        val response = call.execute()
        if (response.isSuccessful) {
            if (!response.body()!!.isExists) {
                log.error("Can`t fetch user with id: $id")
                throw EntityNotFountException("User", id)
            }
            log.info("Fetched user with id: $id")
            return response.body()!!
        } else {
            log.error("Unexpected error while fetching user with id: $id")
            throw InternalServerException("Unexpected error: ${response.code()}")
        }
    }

    private fun getSubscriptionOrThrow(id: Long): Subscription {
        val subscription = subscriptionRepository.findById(id)
            .orElseThrow { EntityNotFountException("Subscription", id) }
        return subscription
    }

    private fun existsBySubscriberIdAndSubscribedAtId(
        subscriberId: String,
        subscribedAtId: String
    ): Boolean {
        return subscriptionRepository
            .existsBySubscriberIdAndSubscribedAtId(subscriberId, subscribedAtId)
    }
}