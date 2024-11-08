package ru.zmaev.notification.unit

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import ru.zmaev.commonlib.exception.EntityNotFountException
import ru.zmaev.commonlib.exception.NoAccessException
import ru.zmaev.commonlib.model.dto.BuildMessage
import ru.zmaev.commonlib.model.dto.UserInfo
import ru.zmaev.notification.model.dto.request.NotificationRequestDto
import ru.zmaev.notification.model.dto.response.NotificationResponseDto
import ru.zmaev.notification.model.dto.response.SubscriptionResponseDto
import ru.zmaev.notification.model.entity.Notification
import ru.zmaev.notification.model.entity.Subscription
import ru.zmaev.notification.repository.NotificationRepository
import ru.zmaev.notification.repository.specification.NotificationSpecification
import ru.zmaev.notification.service.impl.EmailServiceImpl
import ru.zmaev.notification.service.impl.NotificationServiceImpl
import ru.zmaev.notification.service.impl.SubscriptionServiceImpl
import java.util.*
import kotlin.test.Test

@ExtendWith(MockitoExtension::class)
class NotificationServiceTests {

    @Mock
    private lateinit var emailService: EmailServiceImpl

    @Mock
    private lateinit var subscriptionService: SubscriptionServiceImpl

    @Mock
    private lateinit var notificationRepository: NotificationRepository

    private val unsubscribeLink: String = "http://unsubscribe-link"
    private val releaseLink: String = "http://release-link"

    private lateinit var notificationService: NotificationServiceImpl

    private lateinit var buildMessage: BuildMessage

    private val userInfo: UserInfo = UserInfo("uuid", "jwt", "username", false, listOf("ROLE_USER"))

    @BeforeEach
    fun setUp() {
        buildMessage = BuildMessage("test-uuid", 123, "Build", "Description")
        notificationService = NotificationServiceImpl(
            notificationRepository,
            subscriptionService,
            emailService,
            userInfo,
            unsubscribeLink,
            releaseLink
        )
    }

    @Test
    fun createAllSubscriptionsEmpty() {
        `when`(subscriptionService.findAllBySubscriberAtId(buildMessage.uuid)).thenReturn(emptyList())

        notificationService.createAll(buildMessage)

        verify(subscriptionService).findAllBySubscriberAtId(buildMessage.uuid)
        verify(notificationRepository, never()).saveAll(anyList())
    }

    @Test
    fun createAll() {
        val subscription = Subscription(
            id = 1,
            subscriberId = "1",
            subscribedAtId = "2",
            subscriberEmail = "email",
            subscribedAtUsername = "username"
        )
        val notification = Notification(
            id = null,
            buildId = 123,
            subscription = subscription
        )
        val savedNotification = notification.copy(id = 1)

        `when`(subscriptionService.findAllBySubscriberAtId(anyString())).thenReturn(listOf(subscription))
        `when`(notificationRepository.save(any())).thenReturn(savedNotification)

        notificationService.createAll(buildMessage)

        val notificationCaptor = ArgumentCaptor.forClass(Notification::class.java)
        verify(notificationRepository).save(notificationCaptor.capture())
        val capturedNotification = notificationCaptor.value

        assertEquals(null, capturedNotification.id)
        assertEquals(subscription, capturedNotification.subscription)
    }

    @Test
    fun findAll() {
        val pageable: Pageable = mock(Pageable::class.java)

        val subscriptionResponseDto = SubscriptionResponseDto(1, "1", "1")
        val subscription = Subscription(1, "1", "1")

        val notificationRequestDto = NotificationRequestDto(1, 1)
        val notification = Notification(1, 1, subscription)
        val notificationsPage: Page<Notification> = PageImpl(listOf(notification), pageable, 1)

        `when`(notificationRepository.findAll(any(NotificationSpecification::class.java), eq(pageable)))
            .thenReturn(notificationsPage)

        val result: Page<NotificationResponseDto> = notificationService.findAll(pageable, notificationRequestDto)

        Assertions.assertEquals(1, result.totalElements)
        Assertions.assertEquals(1, result.content[0].id)
        Assertions.assertEquals(subscriptionResponseDto, result.content[0].subscription)
        Assertions.assertEquals(1, result.content[0].buildId)
    }

    @Test
    fun findById() {
        val subscription = Subscription(1, "uuid", "1")
        val subscriptionResponseDto = SubscriptionResponseDto(1, "uuid", "1")
        val notification = Notification(id = 1, 1, subscription)
        val notificationResponseDto = NotificationResponseDto(1, 1, subscriptionResponseDto)

        `when`(notificationRepository.findById(anyLong())).thenReturn(Optional.of(notification))

        val result = notificationService.findById(1)

        assertNotNull(result)
        assertEquals(notificationResponseDto.id, result.id)
    }

    @Test
    fun findByIdThrowNoAccessException() {
        val subscription = Subscription(1, "1", "1")
        val notification = Notification(id = 1, 1, subscription)

        `when`(notificationRepository.findById(1)).thenReturn(Optional.of(notification))

        val exception = assertThrows<NoAccessException> {
            notificationService.findById(1L)
        }

        assertEquals("User with id: uuid doesn`t have permissions to do that!", exception.errorMessage)
    }

    @Test
    fun findByIdThrowEntityNotFoundException() {
        `when`(notificationRepository.findById(1)).thenReturn(Optional.empty())

        val exception = assertThrows<EntityNotFountException> {
            notificationService.findById(1L)
        }

        assertEquals("Entity: 'Notification' with id '1' does not exist!", exception.errorMessage)
    }

    @Test
    fun findAllByUserId() {
        val pageable: Pageable = mock(Pageable::class.java)

        val subscriptionResponseDto = SubscriptionResponseDto(1, "uuid", "1")
        val subscription = Subscription(1, "uuid", "1")

        val notification = Notification(1, 1, subscription)
        val notificationsPage: Page<Notification> = PageImpl(listOf(notification), pageable, 1)

        `when`(notificationRepository.findAllBySubscriptionSubscriberId("uuid", pageable))
            .thenReturn(notificationsPage)

        val result: Page<NotificationResponseDto> = notificationService.findAllByUserId(pageable)

        Assertions.assertEquals(1, result.totalElements)
        Assertions.assertEquals(1, result.content[0].id)
        Assertions.assertEquals(subscriptionResponseDto, result.content[0].subscription)
        Assertions.assertEquals(1, result.content[0].buildId)
    }

    @Test
    fun delete() {
        val subscription = Subscription(1, "uuid", "subscriberId")
        val notification = Notification(1, 1, subscription)
        `when`(notificationRepository.findById(1)).thenReturn(Optional.of(notification))

        notificationService.delete(1)

        verify(notificationRepository, times(1)).delete(notification)
    }

    @Test
    fun deleteThrowEntityNotFoundException() {
        `when`(notificationRepository.findById(1)).thenReturn(Optional.empty())

        val exception = assertThrows<EntityNotFountException> {
            notificationService.delete(1L)
        }

        assertEquals("Entity: 'Notification' with id '1' does not exist!", exception.errorMessage)
    }
}