package app.manager

import app.cases.exception.SnippetNotFoundException
import app.common.TestSecurityConfig
import app.manager.model.dto.GetSnippetOutput
import app.manager.model.dto.SnippetListOutput
import app.manager.model.enums.SnippetStatus
import app.manager.persistance.entity.SnippetUserStatus
import app.manager.requests.createMockCreateSnippetRequest
import app.manager.requests.shareSnippetMockRequest
import app.manager.service.ManagerService
import app.run.model.dto.SnippetContent
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest(classes = [TestSecurityConfig::class])
@ExtendWith(SpringExtension::class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ManagerServiceTests {
    @Autowired
    private lateinit var managerService: ManagerService

    @Test
    @WithMockUser("create-snippet-test-user")
    fun createSnippetTest() {
        val requestBody = createMockCreateSnippetRequest("1")

        val result: GetSnippetOutput = managerService.createSnippet(requestBody, "create-snippet-test-user", "token")
        assert(result.id.isNotEmpty())
        assert(result.name.isNotEmpty())
        assert(result.name == "Snippet 1")
    }

    @Test
    @WithMockUser("share-snippet-test-user")
    fun shareSnippetTest() {
        val requestBody = createMockCreateSnippetRequest("1")
        val snippet: GetSnippetOutput = managerService.createSnippet(requestBody, "share-snippet-test-user", "token")
        val shareRequest = shareSnippetMockRequest(snippet.id, "share-snippet-test-user-2")
        val result: String = managerService.shareSnippet(shareRequest, "token")
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

        managerService.createSnippet(requestBody1, "get-all-snippets-test-user", "token")
        managerService.createSnippet(requestBody2, "get-all-snippets-test-user", "token")
        managerService.createSnippet(requestBody3, "get-all-snippets-test-user", "token")
        managerService.createSnippet(requestBody4, "another-get-all-snippets-test-user", "another-token")

        val result: SnippetListOutput =
            managerService.getSnippetsFromUserId("get-all-snippets-test-user", "token", 0, 3)
        assert(result.snippets.isNotEmpty())
        assert(result.snippets.size == 3)
        assert(result.snippets[0].name == "Snippet 1")
        for (i in result.snippets) {
            assert(i.author == "user")
        }
    }

    @Test
    @WithMockUser("manager-user-test")
    fun getSnippetTest() {
        val requestBody = createMockCreateSnippetRequest("1")
        val snippet: GetSnippetOutput = managerService.createSnippet(requestBody, "get-snippet-test-user", "token")
        val snippetId = snippet.id

        val getResult: GetSnippetOutput = managerService.getSnippet(snippetId, "token")
        assert(getResult.name.isNotEmpty())
        assert(getResult.content.isNotEmpty())
        assert(getResult.name == "Snippet 1")
        assert(getResult.content == "Content 1")

        assertThrows<SnippetNotFoundException> { managerService.getSnippet("${snippetId}randomID", "token") }
    }

    @Test
    @WithMockUser("manager-user-test")
    fun deleteSnippet() {
        val requestBody = createMockCreateSnippetRequest("1")
        val snippet: GetSnippetOutput = managerService.createSnippet(requestBody, "delete-snippet-test-user", "token")

        val snippetId = snippet.id
        managerService.deleteSnippet(snippetId, "token")

        assertThrows<SnippetNotFoundException> { managerService.getSnippet(snippetId, "token") }
    }

    @Test
    @WithMockUser("manager-user-test")
    fun updateSnippet() {
        val requestBody = createMockCreateSnippetRequest("1")
        val snippet: GetSnippetOutput = managerService.createSnippet(requestBody, "update-snippet-test-user", "token")

        val snippetId = snippet.id
        val updatedContent = SnippetContent("I am updated content")
        managerService.updateSnippet(snippetId, updatedContent, "token")

        val updatedSnippet: GetSnippetOutput = managerService.getSnippet(snippetId, "token")
        assert(updatedSnippet.name == "Snippet 1")
        assert(updatedSnippet.content == "I am updated content")
    }

    @Test
    @WithMockUser("manager-user-test")
    fun updateAllSnippetStatus() {
        val requestBody = createMockCreateSnippetRequest("1")
        val requestBody2 = createMockCreateSnippetRequest("2")
        managerService.createSnippet(requestBody, "update-all-snippet-status-user", "token")
        managerService.createSnippet(requestBody2, "update-all-snippet-status-user", "token")

        val result: List<SnippetUserStatus> =
            managerService.updateAllUserSnippetsStatus("update-all-snippet-status-user", SnippetStatus.COMPLIANT)
        assert(result.isNotEmpty())
        assert(result[0].status == SnippetStatus.COMPLIANT)
        assert(result[1].status == SnippetStatus.COMPLIANT)
    }
}
