package app.cases.service

import app.cases.exception.SnippetNotFoundException
import app.cases.exception.TestCaseNotFoundException
import app.cases.integration.runner.SnippetRunnerApi
import app.cases.model.dto.CreateCaseInput
import app.cases.model.dto.TestCaseOutput
import app.cases.model.dto.TestCaseRunOutput
import app.cases.persistance.entity.TestCase
import app.cases.persistance.entity.TestCaseExpectedOutput
import app.cases.persistance.entity.TestCaseInput
import app.cases.persistance.repository.TestCaseExpectedOutputRepository
import app.cases.persistance.repository.TestCaseInputRepository
import app.cases.persistance.repository.TestCaseRepository
import app.manager.persistance.repository.SnippetRepository
import app.manager.service.ManagerService
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrElse

@Service
class TestCaseService
    @Autowired
    constructor(
        private val testCaseRepository: TestCaseRepository,
        private val testCaseInputRepository: TestCaseInputRepository,
        private val testCaseExpectedOutputRepository: TestCaseExpectedOutputRepository,
        private val snippetRepository: SnippetRepository,
        private val snippetManagerService: ManagerService,
        private val runnerApi: SnippetRunnerApi,
    ) {
        @Transactional
        fun postTestCase(newTestCase: CreateCaseInput): TestCaseOutput {
            return if (newTestCase.id == null) {
                createTestCase(newTestCase)
            } else {
                updateTestCase(newTestCase.id, newTestCase)
            }
        }

        fun getTestCasesForSnippet(snippetId: String): List<TestCaseOutput> {
            val testCaseEntities = testCaseRepository.findAllBySnippet_Id(snippetId)
            return testCaseEntities.stream().map {
                toTestCaseOutput(it)
            }.toList()
        }

        private fun createTestCase(newTestCase: CreateCaseInput): TestCaseOutput {
            val snippet = snippetRepository.findSnippetById(newTestCase.snippetId) ?: throw SnippetNotFoundException()

            val testCaseEntity =
                TestCase(
                    name = newTestCase.testCaseName,
                    snippet = snippet,
                )

            val savedTestCase = testCaseRepository.save(testCaseEntity)

            val savedInputs =
                newTestCase.inputs.map { input ->
                    testCaseInputRepository.save(TestCaseInput(input = input, testCase = savedTestCase))
                }

            val savedOutputs =
                newTestCase.expectedOutputs.map { output ->
                    testCaseExpectedOutputRepository.save(TestCaseExpectedOutput(output = output, testCase = savedTestCase))
                }

            return buildTestCaseOutput(savedTestCase, savedInputs, savedOutputs)
        }

        private fun updateTestCase(
            testCaseId: String,
            newTestCase: CreateCaseInput,
        ): TestCaseOutput {
            val testCase = getTestCaseOrThrowNotFound(testCaseId)

            testCase.name = newTestCase.testCaseName

            val updatedTestCase = this.testCaseRepository.save(testCase)

            deleteOldInputsAndOutputs(testCase)
            val savedInputs = saveNewInputs(testCase, newTestCase.inputs)
            val savedOutputs = saveNewOutputs(testCase, newTestCase.expectedOutputs)

            return buildTestCaseOutput(updatedTestCase, savedInputs, savedOutputs)
        }

        private fun getTestCaseOrThrowNotFound(testCaseId: String): TestCase {
            val testCaseOptional = this.testCaseRepository.findById(testCaseId)
            if (testCaseOptional.isEmpty) throw TestCaseNotFoundException()

            return testCaseOptional.get()
        }

        private fun deleteOldInputsAndOutputs(testCase: TestCase) {
            val oldInputsIds = testCase.inputs.map { it.id!! }
            val oldExpectedOutputsIds = testCase.expectedOutputs.map { it.id!! }

            this.testCaseInputRepository.deleteAllById(oldInputsIds)
            this.testCaseExpectedOutputRepository.deleteAllById(oldExpectedOutputsIds)
        }

        private fun saveNewInputs(
            testCase: TestCase,
            inputs: List<String>,
        ): List<TestCaseInput> {
            return inputs.map {
                this.testCaseInputRepository.save(TestCaseInput(input = it, testCase = testCase))
            }
        }

        private fun saveNewOutputs(
            testCase: TestCase,
            expectedOutputs: List<String>,
        ): List<TestCaseExpectedOutput> {
            return expectedOutputs.map {
                this.testCaseExpectedOutputRepository.save(TestCaseExpectedOutput(output = it, testCase = testCase))
            }
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

        private fun buildTestCaseOutput(
            testCase: TestCase,
            inputs: List<TestCaseInput>,
            expectedOutputs: List<TestCaseExpectedOutput>,
        ): TestCaseOutput {
            return TestCaseOutput(
                id = testCase.id!!,
                snippetId = testCase.snippet.id!!,
                testCaseName = testCase.name,
                inputs = inputs.map { it.input },
                expectedOutputs = expectedOutputs.map { it.output },
            )
        }

        fun runTestCase(
            testCaseId: String,
            token: String,
        ): TestCaseRunOutput {
            val testCase = testCaseRepository.findById(testCaseId).getOrElse { throw TestCaseNotFoundException() }

            val snippetContent = snippetManagerService.getSnippet(testCase.snippet.id!!, token)

            return runTest(snippetContent.content, testCase.inputs, testCase.expectedOutputs, token)
        }

        private fun runTest(
            snippetContent: String,
            inputs: List<TestCaseInput>,
            expectedOutputs: List<TestCaseExpectedOutput>,
            token: String,
        ): TestCaseRunOutput {
            val runResult = runnerApi.runSnippet(snippetContent, inputs.map { it.input }, token)

            if (runResult.errors.isNotEmpty()) {
                return TestCaseRunOutput(
                    hasPassed = false,
                    message = runResult.errors.joinToString(";"),
                )
            }

            val expectedStringOutputs = expectedOutputs.map { it.output }

            if (runResult.outputs != expectedStringOutputs) {
                return TestCaseRunOutput(
                    hasPassed = false,
                    message = "Expected: $expectedStringOutputs  Actual: ${runResult.outputs}",
                )
            }

            return TestCaseRunOutput(
                hasPassed = true,
                message = "",
            )
        }

        @Transactional
        fun deleteTestCaseById(testCaseId: String) {
            return this.testCaseRepository.deleteById(testCaseId)
        }
    }
