package ru.zmaev.notification.controller.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import ru.zmaev.commonlib.exception.message.ErrorMessage
import ru.zmaev.notification.model.dto.request.SubscriptionRequestDto
import ru.zmaev.notification.model.dto.response.SubscriptionResponseDto

@Tag(name = "Subscription API", description = "API для работы с сущностью Subscription")
interface SubscriptionApi {
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200",
            description = "Успешное создание подписки",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = SubscriptionResponseDto::class)
            )]
        ), ApiResponse(
            responseCode = "403",
            description = "Нет прав доступа",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(
                    implementation = ErrorMessage::class
                )
            )]
        )]
    )
    @Operation(summary = "Сохранение новой подписки", description = "Access: ROLE_USER, ROLE_EDITOR")
    fun create(requestDto: SubscriptionRequestDto): ResponseEntity<SubscriptionResponseDto>

    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200",
            description = "Успешный возврат списка подписок",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = SubscriptionResponseDto::class)
            )]
        )]
    )
    @Operation(
        summary = "Получение списка подписок",
        description = "Access: ROLE_ADMIN"
    )
    fun findAll(
        @Parameter(description = "Начальная страница") pagePosition: Int,
        @Parameter(description = "Размер страницы") pageSize: Int,
        @Parameter(description = "Id подписчика") subscriberId: String,
        @Parameter(description = "Id автора") subscribedAtId: String
    ): ResponseEntity<Page<SubscriptionResponseDto>>

    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Успешный возврат подписки",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = SubscriptionResponseDto::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Подписки не сущесвует",
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
        summary = "Получение подписки по id",
        description = "Access: ROLE_ADMIN, ROLE_USER, ROLE_EDITOR"
    )
    fun findById(id: Long): ResponseEntity<SubscriptionResponseDto>


    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "202",
                description = "Успешное удаление подписки",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = SubscriptionResponseDto::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Подписки не сущесвует",
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
        summary = "Удаление подписки по id",
        description = "Access: ROLE_ADMIN, ROLE_USER, ROLE_EDITOR"
    )
    fun delete(id: Long): ResponseEntity<Unit>
}
