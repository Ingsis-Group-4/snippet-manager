package app.cases

import app.cases.model.dto.TestCaseEnvDto
import app.cases.model.dto.TestCaseOutput
import app.cases.persistance.repository.TestCaseRepository
import app.common.TestSecurityConfig
import app.manager.model.dto.CreateSnippetInput
import app.manager.service.ManagerService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(classes = [TestSecurityConfig::class])
@ExtendWith(SpringExtension::class)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class RunTestCasesTest {
    @Autowired
    private lateinit var testCaseRepository: TestCaseRepository

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var managerService: ManagerService

    private var base = "/case"

    @Test
    @WithMockUser("test")
    fun `001 _ running non-existing test case should return 404`() {
        // Setup
        val testCaseId = "001"

        // Action
        val result =
            mockMvc.perform(post("$base/run/$testCaseId").header(HttpHeaders.AUTHORIZATION, "Bearer token")).andReturn()

        // Assertion
        Assertions.assertEquals(404, result.response.status)
        Assertions.assertEquals("Test case not found", result.response.errorMessage)
    }

    @Test
    @WithMockUser("test")
    fun `002 _ running existing test case should return passed`() {
        // Setup
        val snippetName = "002"
        val snippet2 = managerService.createSnippet(CreateSnippetInput(snippetName, snippetName, "ps"), "test", "token")
        val testCaseRequest =
            TestCaseRequest(
                testCaseName = "Test Case 1",
                snippetId = snippet2.id,
                inputs = listOf("input 1"),
                expectedOutputs = listOf("output 1", "output 2"),
                envs = listOf(TestCaseEnvDto("key1", "value1")),
            )
        val requestBody = objectMapper.writeValueAsString(testCaseRequest)

        // Action
        val testCase =
            mockMvc.perform(
                post(base)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody),
            ).andReturn()

        val responseBody = testCase.response.contentAsString
        val testCaseResult: TestCaseOutput = objectMapper.readValue(responseBody, TestCaseOutput::class.java)

        val testCaseId = testCaseResult.id

        mockMvc.perform(post("$base/run/$testCaseId").header(HttpHeaders.AUTHORIZATION, "Bearer token"))
            .andExpect(status().isOk)
    }

    @Test
    @WithMockUser("test")
    fun `003 _ running existing test case should return not passed`() {
        // Setup
        val snippetName = "002"
        val snippet2 = managerService.createSnippet(CreateSnippetInput(snippetName, snippetName, "ps"), "test", "token")
        val testCaseRequest =
            TestCaseRequest(
                testCaseName = "Test Case 1",
                snippetId = snippet2.id,
                inputs = listOf("input 1"),
                expectedOutputs = listOf("output 1"),
                envs = listOf(TestCaseEnvDto("key1", "value1")),
            )
        val requestBody = objectMapper.writeValueAsString(testCaseRequest)

        // Action
        val testCase =
            mockMvc.perform(
                post(base)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody),
            ).andReturn()

        val responseBody = testCase.response.contentAsString
        val testCaseResult: TestCaseOutput = objectMapper.readValue(responseBody, TestCaseOutput::class.java)

        val testCaseId = testCaseResult.id

        mockMvc.perform(post("$base/run/$testCaseId").header(HttpHeaders.AUTHORIZATION, "Bearer token"))
            .andExpect(status().isOk)
    }
}
