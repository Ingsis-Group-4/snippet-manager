package app.cases

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@SpringBootTest
@ExtendWith(SpringExtension::class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RunTestCasesTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    private var base = "/case"

    @Test
    @WithMockUser("test")
    fun `001 _ running non-existing test case should return 404`() {
        // Setup
        val testCaseId = "001"

        // Action
        val result = mockMvc.post("$base/run/$testCaseId").andReturn()

        // Assertion
        Assertions.assertEquals(404, result.response.status)
        Assertions.assertEquals("Test case not found", result.response.errorMessage)
    }
}
