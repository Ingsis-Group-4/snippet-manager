package app.cases.controller

import app.cases.model.dto.CreateCaseInput
import app.cases.model.dto.TestCaseOutput
import app.cases.model.dto.TestCaseRunOutput
import app.cases.service.TestCaseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
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
        override fun postTestCase(
            @RequestBody input: CreateCaseInput,
        ): ResponseEntity<TestCaseOutput> {
            val newTestCase = testCaseService.postTestCase(input)
            return ResponseEntity.ok(newTestCase)
        }

        override fun getTestCasesForSnippet(
            @PathVariable("snippetId") snippetId: String,
        ): List<TestCaseOutput> {
            return testCaseService.getTestCasesForSnippet(snippetId)
        }

        override fun deleteTestCase(testCaseId: String): ResponseEntity<Unit> {
            testCaseService.deleteTestCaseById(testCaseId)
            return ResponseEntity.ok().build()
        }

        override fun runTestCase(
            @PathVariable(value = "testCaseId") testCaseId: String,
            @AuthenticationPrincipal jwt: Jwt,
        ): TestCaseRunOutput {
            val token = jwt.tokenValue
            return testCaseService.runTestCase(testCaseId, token)
        }
    }
