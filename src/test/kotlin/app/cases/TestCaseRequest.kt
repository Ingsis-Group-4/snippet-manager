package app.cases

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
)

fun createMockTestCaseCreateRequest(snippetId: String): TestCaseRequest {
    return TestCaseRequest(
        testCaseName = "Test Case 1",
        snippetId = snippetId,
        inputs = listOf("input 1"),
        expectedOutputs = listOf("output 1"),
    )
}
