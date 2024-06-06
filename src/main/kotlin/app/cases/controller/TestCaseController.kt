package app.cases.controller

import app.cases.exception.TestCaseNotFoundException
import app.cases.model.dto.CreateCaseInput
import app.cases.model.dto.TestCaseOutput
import app.cases.service.TestCaseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@Validated
class TestCaseController
    @Autowired
    constructor(
        private val testCaseService: TestCaseService,
    ) : TestCaseControllerSpec {
        override fun createTestCase(
            @RequestBody input: CreateCaseInput,
        ): ResponseEntity<Unit> {
            testCaseService.createTestCase(input)
            return ResponseEntity.ok().build()
        }

        override fun getTestCasesForSnippet(
            @PathVariable("snippetId") snippetId: String,
        ): List<TestCaseOutput> {
            return testCaseService.getTestCasesForSnippet(snippetId)
        }

        override fun runTestCase(testCaseId: String): ResponseEntity<Unit> {
            throw TestCaseNotFoundException()
        }
    }
