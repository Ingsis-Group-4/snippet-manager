package app.manager

import app.manager.requests.createMockCreateSnippetRequest
import app.manager.service.ManagerService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
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
    @WithMockUser("manager-user-test")
    fun createSnippetTest() {
        val requestBody = createMockCreateSnippetRequest("1")

        val result: String = managerService.createSnippet(requestBody, "manager-user-test")
        assert(result.isNotEmpty())
        // I need to change the return value of the functions, this is just to test if this works
        assert(result.contains("Snippet created successfully. Snippet id:"))
    }

//    @Test
//    @WithMockUser("manager-user-test")
//    fun shareSnippetTest() {
//        val requestBody = createMockCreateSnippetRequest("1")
//        managerService.createSnippet(requestBody, "manager-user-test")
//        val shareRequest = shareSnippetMockRequest("1", "manager-user-test-2")
//        val result: String = managerService.shareSnippet(shareRequest)
//        assert(result.isNotEmpty())
//        assert(result.contains("Snippet shared successfully"))
//    }
}
