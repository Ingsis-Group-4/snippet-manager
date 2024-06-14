package app.cases.model.dto

import jakarta.validation.constraints.NotBlank

class CreateCaseInput(
    @field:NotBlank(message = "snippetId must not be blank")
    val snippetId: String,
    @field:NotBlank(message = "testCaseName must not be blank")
    val testCaseName: String,
    @field:NotBlank(message = "inputs must not be blank")
    val inputs: List<String>,
    @field:NotBlank(message = "expectedOutputs must not be blank")
    val expectedOutputs: List<String>,
)
