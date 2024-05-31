package app.cases.model.dto

class UpdateCaseInput(
    val testCaseName: String?,
    val inputs: List<String>?,
    val expectedOutputs: List<String>?,
)
