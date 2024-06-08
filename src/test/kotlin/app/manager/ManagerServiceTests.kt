package app.manager

import app.manager.exceptions.NotFoundException
import app.manager.model.dto.GetAllSnippetsOutput
import app.manager.model.dto.GetSnippetOutput
import app.manager.requests.createMockCreateSnippetRequest
import app.manager.requests.shareSnippetMockRequest
import app.manager.service.ManagerService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ExtendWith(SpringExtension::class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ManagerServiceTests {
    @Autowired
    private lateinit var managerService: ManagerService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Value("\${azuriteBucket}")
    private lateinit var azuriteBucketUrl: String

    @Value("\${permissionsService}")
    private lateinit var permissionsServiceUrl: String

    @Test
    @WithMockUser("create-snippet-test-user")
    fun createSnippetTest() {
        val requestBody = createMockCreateSnippetRequest("1")

        val result: GetAllSnippetsOutput = managerService.createSnippet(requestBody, "create-snippet-test-user")
        assert(result.snippetId.isNotEmpty())
        assert(result.name.isNotEmpty())
        assert(result.name == "Snippet 1")
    }

    @Test
    @WithMockUser("share-snippet-test-user")
    fun shareSnippetTest() {
        val requestBody = createMockCreateSnippetRequest("1")
        val snippet: GetAllSnippetsOutput = managerService.createSnippet(requestBody, "share-snippet-test-user")
        val shareRequest = shareSnippetMockRequest(snippet.snippetId, "share-snippet-test-user-2")
        val result: String = managerService.shareSnippet(shareRequest)
        assert(result.isNotEmpty())
        assert(result.contains("Snippet shared successfully"))
    }

    @Test
    @WithMockUser("get-all-snippets-test-user")
    fun getAllUserSnippetsTest() {
        val requestBody1 = createMockCreateSnippetRequest("1")
        val requestBody2 = createMockCreateSnippetRequest("2")
        val requestBody3 = createMockCreateSnippetRequest("3")
        val requestBody4 = createMockCreateSnippetRequest("4")

        managerService.createSnippet(requestBody1, "get-all-snippets-test-user")
        managerService.createSnippet(requestBody2, "get-all-snippets-test-user")
        managerService.createSnippet(requestBody3, "get-all-snippets-test-user")
        managerService.createSnippet(requestBody4, "another-get-all-snippets-test-user")

        val result: List<GetAllSnippetsOutput> = managerService.getSnippetsFromUserId("get-all-snippets-test-user")
        assert(result.isNotEmpty())
        assert(result.size == 3)
        assert(result[0].name == "Snippet 1")
        for (i in result) {
            assert(i.author == "get-all-snippets-test-user")
        }
    }

    @Test
    @WithMockUser("manager-user-test")
    fun getSnippetTest() {
        val requestBody = createMockCreateSnippetRequest("1")
        val snippet: GetAllSnippetsOutput = managerService.createSnippet(requestBody, "get-snippet-test-user")
        val snippetId = snippet.snippetId

        val getResult: GetSnippetOutput = managerService.getSnippet(snippetId)
        assert(getResult.name.isNotEmpty())
        assert(getResult.content.isNotEmpty())
        assert(getResult.name == "Snippet 1")
        assert(getResult.content == "Content 1")

        assertThrows<NotFoundException> { managerService.getSnippet("${snippetId}randomID") }
    }

    @Test
    @WithMockUser("manager-user-test")
    fun deleteSnippet() {
        val requestBody = createMockCreateSnippetRequest("1")
        val snippet: GetAllSnippetsOutput = managerService.createSnippet(requestBody, "delete-snippet-test-user")

        val snippetId = snippet.snippetId
        managerService.deleteSnippet(snippetId)

        assertThrows<NotFoundException> { managerService.getSnippet(snippetId) }
    }
}
