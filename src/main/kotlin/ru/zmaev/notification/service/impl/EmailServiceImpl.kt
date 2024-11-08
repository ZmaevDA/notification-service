package ru.zmaev.notification.service.impl

import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import ru.zmaev.notification.logger.Log
import ru.zmaev.notification.service.EmailService
import java.nio.file.Files
import java.nio.file.Paths

@Service
class EmailServiceImpl(
    private val mailSender: JavaMailSender,
    @Value("\${spring.mail.subject}")
    private val subject: String,
    @Value("\${spring.mail.domain}")
    private val domain: String
) : EmailService {

    companion object : Log()

    override fun send(userEmail: String, placeholders: List<String>) {
        log.info("Sending email notification to $userEmail")
        val templateFilePath = Paths.get("src/main/resources/template/email_template.html")

        val templateContent = try {
            Files.newBufferedReader(templateFilePath).use { reader ->
                reader.readText()
            }
        } catch (e: Exception) {
            log.error("Error reading email template file: ${e.message}", e)
            throw RuntimeException("Failed to read email template", e)
        }

        val emailContent = fillTemplate(templateContent, placeholders)

        val mimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(mimeMessage, "utf-8")
        helper.setText(emailContent, true)
        helper.setTo(userEmail)
        helper.setSubject(subject)
        helper.setFrom(domain)

        try {
            mailSender.send(mimeMessage)
            log.info("Email notification sent to $userEmail")
        } catch (e: Exception) {
            log.error("Error sending email to $userEmail: ${e.message}", e)
            throw RuntimeException("Failed to send email", e)
        }
    }

    private fun fillTemplate(templateContent: String, placeholders: List<String>): String {
        var result = templateContent
        placeholders.forEachIndexed { index, value ->
            result = result.replace("{{${index}}}", value)
        }
        return result
    }
}
