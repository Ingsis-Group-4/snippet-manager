package app.cases

import app.cases.model.dto.TestCaseEnvDto
import com.fasterxml.jackson.annotation.JsonProperty

data class TestCaseRequest(
    @JsonProperty("testCaseName")
    val testCaseName: String,
    @JsonProperty("snippetId")
    val snippetId: String,
    @JsonProperty("inputs")
    val inputs: List<String>,
    @JsonProperty("expectedOutputs")
    val expectedOutputs: List<String>,
    @JsonProperty("envs")
    val envs: List<TestCaseEnvDto>,
)

fun createMockTestCaseCreateRequest(snippetId: String): TestCaseRequest {
    return TestCaseRequest(
        testCaseName = "Test Case 1",
        snippetId = snippetId,
        inputs = listOf("input 1"),
        expectedOutputs = listOf("output 1"),
        envs = listOf(TestCaseEnvDto("key1", "value1")),
    )
}
