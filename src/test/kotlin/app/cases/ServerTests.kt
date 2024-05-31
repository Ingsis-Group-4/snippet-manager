package app.cases

import app.cases.model.dto.TestCaseOutput
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@ExtendWith(SpringExtension::class)
@AutoConfigureMockMvc
class ServerTests {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val base = "/case"

    @Test
    @WithMockUser(username = "test")
    fun createTestCaseTest() {
        val testCaseRequest = createMockTestCaseCreateRequest("1")

        val requestBody = objectMapper.writeValueAsString(testCaseRequest)

        mockMvc.perform(
            post(base)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody),
        ).andExpect(status().isOk)
    }

    @Test
    @WithMockUser(username = "test")
    fun getTestCasesTest() {
        val testCaseRequest = createMockTestCaseCreateRequest("2")

        val requestBody = objectMapper.writeValueAsString(testCaseRequest)

        // Post snippet
        mockMvc.perform(
            post(base)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody),
        ).andReturn()

        // Get snippets
        val getResult =
            mockMvc.perform(
                get("$base/${testCaseRequest.snippetKey}")
                    .contentType(MediaType.APPLICATION_JSON),
            ).andReturn()

        val responseBody = getResult.response.contentAsString

        val testCaseList: List<TestCaseOutput> =
            objectMapper.readValue(responseBody, object : TypeReference<List<TestCaseOutput>>() {})

        Assertions.assertEquals(1, testCaseList.size)
        val testCaseResult = testCaseList[0]

        Assertions.assertEquals(testCaseRequest.testCaseName, testCaseResult.testCaseName)
    }
}
