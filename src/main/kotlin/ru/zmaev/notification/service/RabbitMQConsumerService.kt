package ru.zmaev.notification.service

import ru.zmaev.commonlib.model.dto.BuildMessage

interface RabbitMQConsumerService {
    fun processBuildQueue(buildMessage: BuildMessage)
}