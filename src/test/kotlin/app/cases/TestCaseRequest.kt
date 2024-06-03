package app.cases

import com.fasterxml.jackson.annotation.JsonProperty

data class TestCaseRequest(
    @JsonProperty("testCaseName")
    val testCaseName: String,
    @JsonProperty("snippetKey")
    val snippetKey: String,
    @JsonProperty("inputs")
    val inputs: List<String>,
    @JsonProperty("expectedOutputs")
    val expectedOutputs: List<String>,
)

fun createMockTestCaseCreateRequest(snippetKey: String): TestCaseRequest {
    return TestCaseRequest(
        testCaseName = "Test Case 1",
        snippetKey = snippetKey,
        inputs = listOf("input 1"),
        expectedOutputs = listOf("output 1"),
    )
}
