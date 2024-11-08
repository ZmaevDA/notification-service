package ru.zmaev.notification.service.impl

import org.springframework.amqp.rabbit.annotation.EnableRabbit
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service
import ru.zmaev.commonlib.model.dto.BuildMessage
import ru.zmaev.notification.logger.Log
import ru.zmaev.notification.service.NotificationService
import ru.zmaev.notification.service.RabbitMQConsumerService

@Service
@EnableRabbit
class RabbitMQConsumerServiceImpl(
    private val notificationService: NotificationService
) : RabbitMQConsumerService {

    companion object : Log()

    @RabbitListener(queues = ["\${spring.rabbitmq.template.queue.name}"])
    override fun processBuildQueue(buildMessage: BuildMessage) {
        log.info("Consuming $buildMessage from queue")
        notificationService.createAll(buildMessage)
    }
}