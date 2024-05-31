package app.cases.controller

import app.cases.model.dto.CreateCaseInput
import app.cases.model.dto.TestCaseOutput
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.parameters.RequestBody
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping("case")
interface TestCaseControllerSpec {
    @PostMapping
    @Operation(
        summary = "Create a new test case for a snippet",
    )
    fun createTestCase(
        @RequestBody input: CreateCaseInput,
    ): ResponseEntity<Unit>

    @GetMapping("{snippetKey}")
    @Operation(
        summary = "Get all test cases for snippet",
    )
    fun getTestCasesForSnippet(
        @PathVariable("snippetKey") snippetKey: String,
    ): List<TestCaseOutput>
}
