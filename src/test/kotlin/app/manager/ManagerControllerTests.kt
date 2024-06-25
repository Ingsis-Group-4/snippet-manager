package app.manager

import app.common.TestSecurityConfig
import app.manager.model.dto.GetSnippetOutput
import app.manager.model.dto.ShareSnippetInput
import app.manager.requests.createMockCreateSnippetRequest
import app.run.model.dto.SnippetContent
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(classes = [TestSecurityConfig::class])
@ExtendWith(SpringExtension::class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ManagerControllerTests {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val base = "/manager"

    @Test
    @WithMockUser("test1")
    fun `001 _ create snippet`() {
        // Setup
        val snippetRequest = createMockCreateSnippetRequest("1")
        val requestBody = objectMapper.writeValueAsString(snippetRequest)

        // Action
        mockMvc.perform(
            post("$base/create")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody),
        ).andExpect(status().isOk)
    }

    @Test
    @WithMockUser("user")
    fun `002 _ get all snippets`() {
        val snippetRequest = createMockCreateSnippetRequest("1")
        val snippetRequest2 = createMockCreateSnippetRequest("2")
        val requestBody = objectMapper.writeValueAsString(snippetRequest)
        val requestBody2 = objectMapper.writeValueAsString(snippetRequest2)

        mockMvc.perform(
            post("$base/create")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody),
        ).andExpect(status().isOk)

        mockMvc.perform(
            post("$base/create")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody2),
        ).andExpect(status().isOk)

        val getAllSnippets =
            mockMvc.perform(
                get("$base/snippets").header(HttpHeaders.AUTHORIZATION, "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON),
            ).andReturn()

        val responseBody = getAllSnippets.response.contentAsString
        val snippetList: List<GetSnippetOutput> =
            objectMapper.readValue(responseBody, Array<GetSnippetOutput>::class.java).toList()

        val firstSnippet = snippetList.first()

        Assertions.assertEquals("Snippet 1", firstSnippet.name)
        Assertions.assertEquals("Content 1", firstSnippet.content)
        Assertions.assertEquals("Language 1", firstSnippet.language)
    }

    @Test
    @WithMockUser("test")
    fun `003 _ get snippet by id`() {
        val snippetRequest = createMockCreateSnippetRequest("1")
        val requestBody = objectMapper.writeValueAsString(snippetRequest)

        val snippet =
            mockMvc.perform(
                post("$base/create")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody),
            ).andReturn()

        val responseBody = snippet.response.contentAsString
        val snippetOutput: GetSnippetOutput = objectMapper.readValue(responseBody, GetSnippetOutput::class.java)

        val snippetId = snippetOutput.id

        val getSnippet =
            mockMvc.perform(
                get("$base/snippets/$snippetId").header(HttpHeaders.AUTHORIZATION, "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON),
            ).andReturn()

        val responseBody2 = getSnippet.response.contentAsString
        val snippetOutput2: GetSnippetOutput = objectMapper.readValue(responseBody2, GetSnippetOutput::class.java)

        Assertions.assertEquals("Snippet 1", snippetOutput2.name)
        Assertions.assertEquals("Content 1", snippetOutput2.content)
        Assertions.assertEquals("Language 1", snippetOutput2.language)
    }

    @Test
    @WithMockUser("test")
    fun `test _ 004 share snippet`() {
        val snippetRequest = createMockCreateSnippetRequest("1")
        val requestBody = objectMapper.writeValueAsString(snippetRequest)

        val snippet =
            mockMvc.perform(
                post("$base/create")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody),
            ).andReturn()

        val responseBody = snippet.response.contentAsString
        val snippetOutput: GetSnippetOutput = objectMapper.readValue(responseBody, GetSnippetOutput::class.java)

        val snippetId = snippetOutput.id

        val shareSnippetBody = ShareSnippetInput(snippetId, "user")

        val shareSnippet =
            mockMvc.perform(
                post("$base/share").header(HttpHeaders.AUTHORIZATION, "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(shareSnippetBody)),
            ).andReturn()

        Assertions.assertEquals(200, shareSnippet.response.status)
    }

    @Test
    @WithMockUser("test")
    fun `test _ 005 delete snippet`() {
        val snippetRequest = createMockCreateSnippetRequest("1")
        val requestBody = objectMapper.writeValueAsString(snippetRequest)

        val snippet =
            mockMvc.perform(
                post("$base/create")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody),
            ).andReturn()

        val responseBody = snippet.response.contentAsString
        val snippetOutput: GetSnippetOutput = objectMapper.readValue(responseBody, GetSnippetOutput::class.java)

        val snippetId = snippetOutput.id

        val deleteSnippet =
            mockMvc.perform(
                delete("$base/$snippetId").header(HttpHeaders.AUTHORIZATION, "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON),
            ).andReturn()

        Assertions.assertEquals(200, deleteSnippet.response.status)
    }

    @Test
    @WithMockUser("test")
    fun `test _ 006 update snippet`() {
        val snippetRequest = createMockCreateSnippetRequest("1")
        val requestBody = objectMapper.writeValueAsString(snippetRequest)

        val snippet =
            mockMvc.perform(
                post("$base/create")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody),
            ).andReturn()

        val responseBody = snippet.response.contentAsString
        val snippetOutput: GetSnippetOutput = objectMapper.readValue(responseBody, GetSnippetOutput::class.java)

        val snippetId = snippetOutput.id

        val snippetUpdatedContent = SnippetContent("I am an updated content")
        val updateRequest = objectMapper.writeValueAsString(snippetUpdatedContent)

        val updateSnippet =
            mockMvc.perform(
                put("$base/snippets/$snippetId").header(HttpHeaders.AUTHORIZATION, "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(updateRequest),
            ).andReturn()

        val responseBody2 = updateSnippet.response.contentAsString
        val snippetOutput2: GetSnippetOutput = objectMapper.readValue(responseBody2, GetSnippetOutput::class.java)

        Assertions.assertEquals("Snippet 1", snippetOutput2.name)
        Assertions.assertEquals("I am an updated content", snippetOutput2.content)
        Assertions.assertEquals("Language 1", snippetOutput2.language)
    }
}
