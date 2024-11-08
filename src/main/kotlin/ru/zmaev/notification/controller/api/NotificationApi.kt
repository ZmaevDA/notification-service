package ru.zmaev.notification.controller.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestParam
import ru.zmaev.commonlib.exception.message.ErrorMessage
import ru.zmaev.notification.model.dto.response.NotificationResponseDto

@Tag(name = "Notification API", description = "API для работы с сущностью Notification")
interface NotificationApi {
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200",
            description = "Успешный возврат списка уведомлений",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = NotificationResponseDto::class)
            )]
        )]
    )
    @Operation(
        summary = "Получение списка уведомлений",
        description = "Access: ROLE_ADMIN"
    )
    fun findAll(
        @RequestParam(defaultValue = "0") @Min(0) pagePosition: Int,
        @RequestParam(defaultValue = "10") @Min(1) @Max(20) pageSize: Int,
        @RequestParam(required = false) subscriptionId: Long?,
        @RequestParam(required = false) buildId: Long?
    ): ResponseEntity<Page<NotificationResponseDto>>

    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Успешный возврат уведомления",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = NotificationResponseDto::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Уведомления не сущесвует",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorMessage::class)
                )]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Нет доступа",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorMessage::class)
                )]
            )
        ]
    )
    @Operation(
        summary = "Получение уведомления по id",
        description = "Access: ROLE_ADMIN, ROLE_USER, ROLE_EDITOR"
    )
    fun findById(id: Long): ResponseEntity<NotificationResponseDto>

    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200",
            description = "Успешный возврат списка уведомлений текущего пользователя",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = NotificationResponseDto::class)
            )]
        )]
    )
    @Operation(
        summary = "Получение списка  уведомлений текущего пользователя",
        description = "Access: ROLE_ADMIN, ROLE_USER, ROLE_EDITOR"
    )
    fun findAllByUserId(
        @RequestParam(defaultValue = "0") @Min(0) pagePosition: Int,
        @RequestParam(defaultValue = "10") @Min(1) @Max(20) pageSize: Int
    ): ResponseEntity<Page<NotificationResponseDto>>

    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "202",
                description = "Успешное удаление уведомления"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Уведомления не существует",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorMessage::class)
                )]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Нет доступа",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorMessage::class)
                )]
            )
        ]
    )
    @Operation(
        summary = "Удаление уведомления по id",
        description = "Access: ROLE_ADMIN"
    )
    fun delete(id: Long): ResponseEntity<Unit>
}