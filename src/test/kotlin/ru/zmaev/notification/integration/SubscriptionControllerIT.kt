package ru.zmaev.notification.integration

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.anyString
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import retrofit2.Call
import retrofit2.Response
import ru.zmaev.commonlib.api.SupportServiceApi
import ru.zmaev.commonlib.model.dto.UserInfo
import ru.zmaev.commonlib.model.dto.common.UserCommonResponseDto
import ru.zmaev.commonlib.model.dto.response.UserInnerResponseDto
import ru.zmaev.notification.container.PostgresTestContainer
import ru.zmaev.notification.model.dto.request.SubscriptionRequestDto

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class SubscriptionControllerIT : PostgresTestContainer() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean(name = "userInfo")
    private lateinit var userInfo: UserInfo

    @MockBean
    private lateinit var supportServiceApi: SupportServiceApi

    @Test
    @WithMockUser(roles = ["USER"])
    fun create() {
        val subscriptionRequest = SubscriptionRequestDto("uuid", "subscribedAtId")

        val subscriberCall: Call<UserCommonResponseDto> = Mockito.mock()
        val subscribedAtCall: Call<UserCommonResponseDto> = Mockito.mock()
        val subscriberResponse: Response<UserCommonResponseDto> = Mockito.mock()
        val subscribedAtResponse: Response<UserCommonResponseDto> = Mockito.mock()

        `when`(subscriberResponse.isSuccessful).thenReturn(true)
        `when`(subscribedAtResponse.isSuccessful).thenReturn(true)
        `when`(subscriberResponse.body()).thenReturn(UserInnerResponseDto("1", "user1", "email1"))
        `when`(subscribedAtResponse.body()).thenReturn(UserInnerResponseDto("2", "user2", "email2"))

        `when`(userInfo.userId).thenReturn("uuid")

        `when`(supportServiceApi.findUserById(anyString(), Mockito.eq("inner"))).thenReturn(subscriberCall)
        `when`(supportServiceApi.findUserById(anyString(), Mockito.eq("inner"))).thenReturn(subscribedAtCall)

        `when`(subscriberCall.execute()).thenReturn(subscriberResponse)
        `when`(subscribedAtCall.execute()).thenReturn(subscribedAtResponse)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/v1/subscriptions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(subscriptionRequest))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.subscriberId").value("uuid"))
            .andExpect(jsonPath("$.subscribedAtId").value("subscribedAtId"))
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    @Sql(
        scripts = ["/sql/delete_all.sql", "/sql/insert_subscription.sql"],
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    fun findAll() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/v1/subscriptions")
                .param("pagePosition", "0")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content[0].subscriberId").value("s1"))
            .andExpect(jsonPath("$.content[0].subscribedAtId").value("sat1"))
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    @Sql(
        scripts = ["/sql/delete_all.sql", "/sql/insert_subscription.sql"],
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    fun findById() {
        `when`(userInfo.role).thenReturn(listOf("ROLE_ADMIN"))
        mockMvc.perform(
            MockMvcRequestBuilders.get("/v1/subscriptions/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.subscriberId").value("s1"))
            .andExpect(jsonPath("$.subscribedAtId").value("sat1"))
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    @Sql(
        scripts = ["/sql/delete_all.sql", "/sql/insert_subscription.sql"],
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    fun deleteById() {
        `when`(userInfo.role).thenReturn(listOf("ROLE_ADMIN"))
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/v1/subscriptions/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNoContent)
    }
}
