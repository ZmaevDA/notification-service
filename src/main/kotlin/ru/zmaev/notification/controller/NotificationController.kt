package ru.zmaev.notification.controller

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import ru.zmaev.notification.controller.api.NotificationApi
import ru.zmaev.notification.model.dto.request.NotificationRequestDto
import ru.zmaev.notification.model.dto.response.NotificationResponseDto
import ru.zmaev.notification.service.NotificationService

@RestController
@RequestMapping("v1/notifications")
class NotificationController(
    private val notificationService: NotificationService
) : NotificationApi {

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    override fun findAll(
        @RequestParam(defaultValue = "0") @Min(0) pagePosition: Int,
        @RequestParam(defaultValue = "10") @Min(1) @Max(20) pageSize: Int,
        @RequestParam(required = false) subscriptionId: Long?,
        @RequestParam(required = false) buildId: Long?
    ): ResponseEntity<Page<NotificationResponseDto>> {
        val response = notificationService.findAll(
            PageRequest.of(pagePosition, pageSize),
            NotificationRequestDto(subscriptionId, buildId)
        )
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}")
    override fun findById(@PathVariable id: Long): ResponseEntity<NotificationResponseDto> {
        val response = notificationService.findById(id)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/my")
    override fun findAllByUserId(
        @RequestParam(defaultValue = "0") @Min(0) pagePosition: Int,
        @RequestParam(defaultValue = "10") @Min(1) @Max(20) pageSize: Int
    ): ResponseEntity<Page<NotificationResponseDto>> {
        val response = notificationService.findAllByUserId(PageRequest.of(pagePosition, pageSize))
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    override fun delete(@PathVariable id: Long): ResponseEntity<Unit> {
        notificationService.delete(id)
        return ResponseEntity.noContent().build()
    }
}