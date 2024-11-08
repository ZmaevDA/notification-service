package ru.zmaev.notification.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import ru.zmaev.commonlib.model.dto.BuildMessage
import ru.zmaev.notification.model.dto.request.NotificationRequestDto
import ru.zmaev.notification.model.dto.response.NotificationResponseDto

interface NotificationService {
    fun createAll(buildMessage: BuildMessage)
    fun findAll(pageable: Pageable, notificationRequestDto: NotificationRequestDto): Page<NotificationResponseDto>
    fun findById(id: Long): NotificationResponseDto
    fun findAllByUserId(pageable: Pageable): Page<NotificationResponseDto>
    fun delete(id: Long)
}