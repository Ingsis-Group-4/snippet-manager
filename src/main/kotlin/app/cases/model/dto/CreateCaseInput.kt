package app.cases.model.dto

import jakarta.validation.constraints.NotBlank

class CreateCaseInput(
    val id: String?,
    @field:NotBlank(message = "snippetId must not be blank")
    val snippetId: String,
    @field:NotBlank(message = "testCaseName must not be blank")
    val testCaseName: String,
    val inputs: List<String>,
    val expectedOutputs: List<String>,
)
