package ru.zmaev.notification.unit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import retrofit2.Call
import retrofit2.Response
import ru.zmaev.commonlib.api.SupportServiceApi
import ru.zmaev.commonlib.exception.EntityConflictException
import ru.zmaev.commonlib.exception.EntityNotFountException
import ru.zmaev.commonlib.exception.NoAccessException
import ru.zmaev.commonlib.model.dto.UserInfo
import ru.zmaev.commonlib.model.dto.common.UserCommonResponseDto
import ru.zmaev.commonlib.model.dto.response.UserInnerResponseDto
import ru.zmaev.notification.model.dto.request.SubscriptionRequestDto
import ru.zmaev.notification.model.dto.response.SubscriptionResponseDto
import ru.zmaev.notification.model.entity.Subscription
import ru.zmaev.notification.repository.SubscriptionRepository
import ru.zmaev.notification.repository.specification.SubscriptionSpecification
import ru.zmaev.notification.service.impl.SubscriptionServiceImpl
import java.util.*

@SpringBootTest
class SubscriptionServiceTests {

    private val subscriptionRepository: SubscriptionRepository = mock(SubscriptionRepository::class.java)
    private val supportServiceApi: SupportServiceApi = mock(SupportServiceApi::class.java)
    private val userInfo: UserInfo = UserInfo("uuid", "jwt", "username", false, listOf("ROLE_USER"))
    private val subscriptionService = SubscriptionServiceImpl(subscriptionRepository, supportServiceApi, userInfo)

    @Test
    fun create() {
        val request = SubscriptionRequestDto(subscriberId = "uuid", subscribedAtId = "SubscribedAtId")
        val subscription = Subscription(id = 1L, subscriberId = "uuid", subscribedAtId = "SubscribedAtId")
        val responseDto = SubscriptionResponseDto(id = 1L, subscriberId = "uuid", subscribedAtId = "SubscribedAtId")

        val subscriberCall: Call<UserCommonResponseDto> = mock()
        val subscribedAtCall: Call<UserCommonResponseDto> = mock()
        val subscriberResponse: Response<UserCommonResponseDto> = mock()
        val subscribedAtResponse: Response<UserCommonResponseDto> = mock()

        `when`(subscriberResponse.isSuccessful).thenReturn(true)
        `when`(subscribedAtResponse.isSuccessful).thenReturn(true)
        `when`(subscriberResponse.body()).thenReturn(UserInnerResponseDto("1", "user1", "email1"))
        `when`(subscribedAtResponse.body()).thenReturn(UserInnerResponseDto("2", "user2", "email2"))
        `when`(subscriptionRepository.existsBySubscriberIdAndSubscribedAtId("uuid", "SubscribedAtId")).thenReturn(false)
        `when`(subscriptionRepository.save(any(Subscription::class.java))).thenReturn(subscription)
        `when`(supportServiceApi.findUserById(anyString(), eq("inner"))).thenReturn(subscriberCall)
        `when`(supportServiceApi.findUserById(anyString(), eq("inner"))).thenReturn(subscribedAtCall)
        `when`(subscriberCall.execute()).thenReturn(subscriberResponse)
        `when`(subscribedAtCall.execute()).thenReturn(subscribedAtResponse)

        val response = subscriptionService.create(request)

        assertEquals(responseDto, response)
    }

    @Test
    fun createThrowsNoAccessException() {
        val subscriptionRequestDto = SubscriptionRequestDto("differentSubscriberId", "subscribedAtId")

        val exception = assertThrows<NoAccessException> {
            subscriptionService.create(subscriptionRequestDto)
        }
        assertEquals(
            "User with uuid: uuid can`t subscribe user with uuid: differentSubscriberId",
            exception.errorMessage
        )
    }

    @Test
    fun createThrowsEntityConflictExceptionWhenAlreadySubscribed() {
        val subscriptionRequestDto = SubscriptionRequestDto("uuid", "subscribedAtId")

        val mockCall: Call<UserCommonResponseDto> = mock()
        val mockResponse: Response<UserCommonResponseDto> = mock()

        `when`(mockResponse.isSuccessful).thenReturn(true)
        `when`(mockCall.execute()).thenReturn(mockResponse)
        `when`(mockResponse.body()).thenReturn(UserInnerResponseDto("uuid", "user2", "email2"))

        `when`(supportServiceApi.findUserById(anyString(), eq("inner"))).thenReturn(mockCall)
        `when`(subscriptionRepository.existsBySubscriberIdAndSubscribedAtId(anyString(), anyString())).thenReturn(true)

        val exception = assertThrows<EntityConflictException> {
            subscriptionService.create(subscriptionRequestDto)
        }

        assertEquals(
            "User with uuid: uuid already subscribed to user with uuid: subscribedAtId",
            exception.errorMessage
        )
    }

    @Test
    fun createThrowsEntityConflictExceptionWhenSubscribingSelf() {
        val subscriptionRequestDto = SubscriptionRequestDto("uuid", "uuid")

        val mockCall: Call<UserCommonResponseDto> = mock()
        val mockResponse: Response<UserCommonResponseDto> = mock()

        `when`(mockResponse.isSuccessful).thenReturn(true)
        `when`(mockCall.execute()).thenReturn(mockResponse)
        `when`(supportServiceApi.findUserById(anyString(), eq("inner"))).thenReturn(mockCall)

        `when`(subscriptionRepository.existsBySubscriberIdAndSubscribedAtId(anyString(), anyString())).thenReturn(false)

        val exception = assertThrows<EntityConflictException> {
            subscriptionService.create(subscriptionRequestDto)
        }

        assertEquals("User can`t be subscribed on himself!", exception.errorMessage)
    }

    @Test
    fun createThrowsEntityNotFountExceptionWhenUserDoesNotExist() {
        val subscriptionRequestDto = SubscriptionRequestDto("uuid", "subscribedAtId")

        val mockCall: Call<UserCommonResponseDto> = mock()
        val mockResponse: Response<UserCommonResponseDto> = mock()

        `when`(mockResponse.body()).thenReturn(UserInnerResponseDto("1", "user1", "email1"))
        `when`(mockResponse.isSuccessful).thenReturn(true)
        `when`(mockResponse.code()).thenReturn(404)
        `when`(mockCall.execute()).thenReturn(mockResponse)
        `when`(supportServiceApi.findUserById(anyString(), eq("inner"))).thenReturn(mockCall)

        val exception = assertThrows<EntityNotFountException> {
            subscriptionService.create(subscriptionRequestDto)
        }

        assertEquals("Entity: 'User' with id 'subscribedAtId' does not exist!", exception.errorMessage)
    }

    @Test
    fun findAll() {
        val pageable: Pageable = PageRequest.of(0, 10)
        val subscriptionRequestDto = SubscriptionRequestDto("subscriberId", "subscribedAtId")

        val subscription = Subscription(1, "subscriberId", "subscribedAtId")
        val subscriptions = listOf(subscription)
        val pagedSubscriptions: Page<Subscription> = PageImpl(subscriptions, pageable, subscriptions.size.toLong())

        `when`(subscriptionRepository.findAll(any(SubscriptionSpecification::class.java), eq(pageable)))
            .thenReturn(pagedSubscriptions)

        val result: Page<SubscriptionResponseDto> = subscriptionService.findAll(pageable, subscriptionRequestDto)

        assertEquals(1, result.totalElements)
        assertEquals(1, result.content[0].id)
        assertEquals("subscriberId", result.content[0].subscriberId)
        assertEquals("subscribedAtId", result.content[0].subscribedAtId)
    }

    @Test
    fun findById() {
        val subscription = Subscription(id = 1L, subscriberId = "uuid", subscribedAtId = "subscribedAtId")
        `when`(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription))

        val response = subscriptionService.findById(1L)

        assertEquals(subscription.subscriberId, response.subscriberId)
        assertEquals(subscription.subscribedAtId, response.subscribedAtId)
    }

    @Test
    fun findByIdThrowEntityNotFountException() {
        `when`(subscriptionRepository.findById(1L)).thenReturn(Optional.empty())

        val exception = assertThrows<EntityNotFountException> {
            subscriptionService.findById(1L)
        }

        assertEquals("Entity: 'Subscription' with id '1' does not exist!", exception.errorMessage)
    }

    @Test
    fun findByIdThrowNoAccessException() {
        val subscription = Subscription(id = 1L, subscriberId = "other-user-id", subscribedAtId = "subscribedAtId")
        `when`(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription))

        val exception = assertThrows<NoAccessException> {
            subscriptionService.findById(1L)
        }

        assertEquals("Can not access this subscribe!", exception.errorMessage)
    }

    @Test
    fun findByIdWhenUserIsAdmin() {
        val adminUserInfo = UserInfo("admin-id", "jwt-token", "admin", true, listOf("ROLE_ADMIN"))
        val subscriptionService = SubscriptionServiceImpl(subscriptionRepository, supportServiceApi, adminUserInfo)
        val subscription = Subscription(id = 1L, subscriberId = "user-id", subscribedAtId = "subscribedAtId")
        `when`(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription))

        val response = subscriptionService.findById(1L)

        assertEquals(subscription.subscriberId, response.subscriberId)
        assertEquals(subscription.subscribedAtId, response.subscribedAtId)
    }

    @Test
    fun delete() {
        val subscription = Subscription(1, "uuid", "subscriberId")
        `when`(subscriptionRepository.findById(1)).thenReturn(Optional.of(subscription))
        subscriptionService.delete(1)
        verify(subscriptionRepository).delete(subscription)
    }

    @Test
    fun deleteThrowNoAccessException() {
        val subscription = Subscription(1, "other-user-id", "subscriberId")
        `when`(subscriptionRepository.findById(1)).thenReturn(Optional.of(subscription))
        val exception = assertThrows<NoAccessException> {
            subscriptionService.delete(1L)
        }
        assertEquals("Can not access this subscribe!", exception.errorMessage)
    }
}
