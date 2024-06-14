package app.cases

import app.cases.model.dto.TestCaseOutput
import app.cases.persistance.entity.TestCase
import app.cases.persistance.repository.TestCaseRepository
import app.manager.persistance.entity.Snippet
import app.manager.persistance.repository.SnippetRepository
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@ExtendWith(SpringExtension::class)
@AutoConfigureMockMvc
class CrudTestCasesTests {
    @Autowired
    private lateinit var testCaseRepository: TestCaseRepository

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var snippetRepository: SnippetRepository

    private val base = "/case"

    @Test
    @WithMockUser(username = "test")
    fun `001 _ create test case for snippet`() {
        val snippetName = "001"
        val snippet = snippetRepository.save(Snippet(snippetName, snippetName, "ps"))

        val testCaseRequest = createMockTestCaseCreateRequest(snippet.id!!)

        val requestBody = objectMapper.writeValueAsString(testCaseRequest)

        mockMvc.perform(
            post(base)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody),
        ).andExpect(status().isOk)
    }

    @Test
    @WithMockUser(username = "test")
    fun `002 _ get all test cases for snippet`() {
        val snippetName = "002"
        val snippet = snippetRepository.save(Snippet(snippetName, snippetName, "ps"))

        val testCaseRequest = createMockTestCaseCreateRequest(snippet.id!!)

        val requestBody = objectMapper.writeValueAsString(testCaseRequest)

        // Post test case
        mockMvc.perform(
            post(base)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody),
        ).andReturn()

        // Get test cases for snippet
        val getResult =
            mockMvc.perform(
                get("$base/${testCaseRequest.snippetId}")
                    .contentType(MediaType.APPLICATION_JSON),
            ).andReturn()

        val responseBody = getResult.response.contentAsString

        val testCaseList: List<TestCaseOutput> =
            objectMapper.readValue(responseBody, object : TypeReference<List<TestCaseOutput>>() {})

        Assertions.assertEquals(1, testCaseList.size)
        val testCaseResult = testCaseList[0]

        Assertions.assertEquals(testCaseRequest.testCaseName, testCaseResult.testCaseName)
    }

    @Test
    @WithMockUser(username = "test")
    fun `003 _ delete test case`() {
        // Setup
        val snippetName = "003"
        val snippet = snippetRepository.save(Snippet(snippetName, snippetName, "ps"))

        val testCase = testCaseRepository.save(TestCase(snippetName, snippet))

        // Action + Assertion
        mockMvc.perform(
            delete("$base/$testCase.id"),
        ).andExpect(status().isOk)
    }
}
