package ru.zmaev.notification.integration

import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.lessThanOrEqualTo
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import ru.zmaev.commonlib.model.dto.UserInfo
import ru.zmaev.notification.container.PostgresTestContainer

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class NotificationControllerIT : PostgresTestContainer() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean(name = "userInfo")
    private lateinit var userInfo: UserInfo

    @Test
    @WithMockUser(roles = ["ADMIN"])
    @Sql(
        scripts = ["/sql/delete_all.sql", "/sql/insert_subscription.sql", "/sql/insert_notification.sql"],
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    fun findAll() {
        mockMvc.perform(
            get("/v1/notifications")
                .param("pagePosition", "0")
                .param("pageSize", "10")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content.length()").value(lessThanOrEqualTo(10)))
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    @Sql(
        scripts = ["/sql/delete_all.sql", "/sql/insert_subscription.sql", "/sql/insert_notification.sql"],
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    fun findById() {
        val id = 1
        `when`(userInfo.role).thenReturn(listOf("ROLE_ADMIN"))
        mockMvc.perform(
            get("/v1/notifications/$id")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(id))
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    @Sql(
        scripts = ["/sql/delete_all.sql", "/sql/insert_subscription.sql", "/sql/insert_notification.sql"],
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    fun findAllByUserId() {
        `when`(userInfo.userId).thenReturn("1")
        mockMvc.perform(
            get("/v1/notifications/my")
                .param("pagePosition", "0")
                .param("pageSize", "10")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content.length()").value(lessThanOrEqualTo(10)))
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    @Sql(
        scripts = ["/sql/delete_all.sql", "/sql/insert_subscription.sql", "/sql/insert_notification.sql"],
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    fun delete() {
        `when`(userInfo.role).thenReturn(listOf("ROLE_ADMIN"))
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/v1/notifications/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNoContent)
    }
}
