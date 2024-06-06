package app.cases.service

import app.cases.exception.SnippetNotFoundException
import app.cases.model.dto.CreateCaseInput
import app.cases.model.dto.TestCaseOutput
import app.cases.persistance.entity.TestCase
import app.cases.persistance.entity.TestCaseExpectedOutput
import app.cases.persistance.entity.TestCaseInput
import app.cases.persistance.repository.TestCaseExpectedOutputRepository
import app.cases.persistance.repository.TestCaseInputRepository
import app.cases.persistance.repository.TestCaseRepository
import app.manager.persistance.repository.SnippetRepository
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class TestCaseService
    @Autowired
    constructor(
        private val testCaseRepository: TestCaseRepository,
        private val testCaseInputRepository: TestCaseInputRepository,
        private val testCaseExpectedOutputRepository: TestCaseExpectedOutputRepository,
        private val snippetRepository: SnippetRepository,
    ) {
        @Transactional
        fun createTestCase(newTestCase: CreateCaseInput) {
            val snippet = snippetRepository.findSnippetById(newTestCase.snippetId) ?: throw SnippetNotFoundException()

            val testCaseEntity =
                TestCase(
                    name = newTestCase.testCaseName,
                    snippet = snippet,
                )

            val savedTestCase = testCaseRepository.save(testCaseEntity)

            newTestCase.inputs.forEach { input ->
                testCaseInputRepository.save(TestCaseInput(input = input, testCase = savedTestCase))
            }

            newTestCase.expectedOutputs.forEach { output ->
                testCaseExpectedOutputRepository.save(TestCaseExpectedOutput(output = output, testCase = savedTestCase))
            }
        }

        fun getTestCasesForSnippet(snippetId: String): List<TestCaseOutput> {
            val testCaseEntities = testCaseRepository.findAllBySnippet_Id(snippetId)
            return testCaseEntities.stream().map {
                toTestCaseOutput(it)
            }.toList()
        }

        private fun toTestCaseOutput(testCase: TestCase): TestCaseOutput {
            val inputs = testCase.inputs.map { it.input }
            val expectedOutputs = testCase.expectedOutputs.map { it.output }

            return TestCaseOutput(
                id = testCase.id!!,
                snippetId = testCase.snippet.id!!,
                testCaseName = testCase.name,
                inputs = inputs,
                expectedOutputs = expectedOutputs,
            )
        }
    }
