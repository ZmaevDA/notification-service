package ru.zmaev.notification.controller

import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import ru.zmaev.notification.controller.api.SubscriptionApi
import ru.zmaev.notification.model.dto.request.SubscriptionRequestDto
import ru.zmaev.notification.model.dto.response.SubscriptionResponseDto
import ru.zmaev.notification.service.SubscriptionService

@Validated
@RestController
@RequestMapping("v1/subscriptions")
class SubscriptionController(
    private val subscriptionService: SubscriptionService
) : SubscriptionApi {

    @PostMapping
    @PreAuthorize("permitAll()")
    override fun create(@Valid @RequestBody requestDto: SubscriptionRequestDto): ResponseEntity<SubscriptionResponseDto> {
        return ResponseEntity.ok(subscriptionService.create(requestDto))
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    override fun findAll(
        @RequestParam(defaultValue = "0") @Min(0) pagePosition: Int,
        @RequestParam(defaultValue = "10") @Min(1) pageSize: Int,
        @RequestParam(defaultValue = "", required = false) subscriberId: String,
        @RequestParam(defaultValue = "", required = false) subscribedAtId: String
    ): ResponseEntity<Page<SubscriptionResponseDto>> {
        val response: Page<SubscriptionResponseDto> = subscriptionService
            .findAll(
                PageRequest.of(pagePosition, pageSize),
                SubscriptionRequestDto(subscriberId, subscribedAtId)
            )
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}")
    override fun findById(@PathVariable id: Long): ResponseEntity<SubscriptionResponseDto> {
        return ResponseEntity.ok(subscriptionService.findById(id))
    }

    @DeleteMapping("/{id}")
    override fun delete(@PathVariable id: Long): ResponseEntity<Unit> {
        subscriptionService.delete(id)
        return ResponseEntity.noContent().build()
    }
}
