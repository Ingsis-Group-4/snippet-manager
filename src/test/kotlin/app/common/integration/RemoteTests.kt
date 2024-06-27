package app.common.integration

import app.common.TestSecurityConfig
import app.common.integration.auth0.RemoteAuth0Api
import app.common.integration.runner.RemoteSnippetRunnerApi
import app.manager.integration.asset.RemoteAssetStore
import app.manager.integration.permission.RemoteSnippetPermission
import app.manager.model.dto.PermissionCreateSnippetInput
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.client.RestTemplate

@SpringBootTest(classes = [TestSecurityConfig::class])
@ExtendWith(SpringExtension::class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RemoteTests {
    @Autowired
    private lateinit var restTemplate: RestTemplate

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `001 _ remote test for auth api `() {
        val remoteAuth = RemoteAuth0Api("http://localhost:8080", restTemplate, "token")
        assertThrows<Exception> { remoteAuth.getAllUsers() }
        assertThrows<Exception> { remoteAuth.getUserById("1") }
    }

    @Test
    fun `002 _ remote test for snippet runner`() {
        val remoteRunner = RemoteSnippetRunnerApi("http://localhost:8080", restTemplate)
        val content = "content"
        val token = "token"
        assertThrows<Exception> { remoteRunner.runSnippet(content, emptyList(), emptyList(), token) }
        assertThrows<Exception> { remoteRunner.formatSnippet(content, "config", token) }
    }

    @Test
    fun `003 _ remote test for asset service`() {
        val remoteAssetService = RemoteAssetStore(restTemplate, "http://localhost:8080")
        assertThrows<Exception> { remoteAssetService.getSnippet("key") }
        assertThrows<Exception> { remoteAssetService.createSnippetInBucket("key", "content") }
        assertThrows<Exception> { remoteAssetService.updateSnippet("key", "new content") }
    }

    @Test
    fun `004 _ remote test for permission service`() {
        val remotePermissionService = RemoteSnippetPermission(restTemplate, "http://localhost:8080", objectMapper)
        val input = PermissionCreateSnippetInput("1", "2", "3")
        assertThrows<Exception> { remotePermissionService.createSnippetPermission(input, "token") }
        assertThrows<Exception> { remotePermissionService.getAllSnippetsPermission("1", "1", 2, 3) }
        assertThrows<Exception> { remotePermissionService.getAuthorBySnippetId("1", "token") }
    }
}
