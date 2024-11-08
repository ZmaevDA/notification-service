package ru.zmaev.notification.service

interface EmailService {
    fun send(userEmail: String, placeholders: List<String>)
}