package app.cases.model.dto

class TestCaseOutput(
    val id: String,
    val snippetKey: String,
    val testCaseName: String,
    val inputs: List<String>,
    val expectedOutputs: List<String>,
)
