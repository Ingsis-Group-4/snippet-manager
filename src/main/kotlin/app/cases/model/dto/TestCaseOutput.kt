package app.cases.model.dto

class TestCaseOutput(
    val id: String,
    val snippetId: String,
    val testCaseName: String,
    val inputs: List<String>,
    val expectedOutputs: List<String>,
    val envs: List<TestCaseEnvDto>,
)
