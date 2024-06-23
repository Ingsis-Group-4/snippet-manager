package app.cases.service

import app.cases.exception.SnippetNotFoundException
import app.cases.exception.TestCaseNotFoundException
import app.cases.model.dto.CreateCaseInput
import app.cases.model.dto.TestCaseOutput
import app.cases.model.dto.TestCaseRunOutput
import app.cases.persistance.entity.TestCase
import app.cases.persistance.entity.TestCaseExpectedOutput
import app.cases.persistance.entity.TestCaseInput
import app.cases.persistance.repository.TestCaseExpectedOutputRepository
import app.cases.persistance.repository.TestCaseInputRepository
import app.cases.persistance.repository.TestCaseRepository
import app.common.integration.runner.SnippetRunnerApi
import app.manager.persistance.repository.SnippetRepository
import app.manager.service.ManagerService
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
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
        private val logger = LoggerFactory.getLogger(TestCaseService::class.java)

        @Transactional
        fun postTestCase(newTestCase: CreateCaseInput): TestCaseOutput {
            logger.info("Received request for new test case, id: ${newTestCase.id}")
            return if (newTestCase.id == null) {
                logger.info("Creating new test case")
                createTestCase(newTestCase)
            } else {
                logger.info("Updating test case with id: ${newTestCase.id}")
                updateTestCase(newTestCase.id, newTestCase)
            }
        }

        fun getTestCasesForSnippet(snippetId: String): List<TestCaseOutput> {
            logger.info("Getting test cases for snippet with id: $snippetId")
            val testCaseEntities = testCaseRepository.findAllBySnippet_Id(snippetId)
            return testCaseEntities.stream().map {
                toTestCaseOutput(it)
            }.toList()
        }

        private fun createTestCase(newTestCase: CreateCaseInput): TestCaseOutput {
            val snippet = snippetRepository.findSnippetById(newTestCase.snippetId)
            if (snippet == null) {
                logger.error("Snippet with id ${newTestCase.snippetId} not found")
                throw SnippetNotFoundException()
            }

            val testCaseEntity =
                TestCase(
                    name = newTestCase.testCaseName,
                    snippet = snippet,
                )

            val savedTestCase = testCaseRepository.save(testCaseEntity)
            logger.info("Saved test case with id: ${savedTestCase.id}")

            val savedInputs =
                newTestCase.inputs.map { input ->
                    testCaseInputRepository.save(TestCaseInput(input = input, testCase = savedTestCase))
                }
            logger.info("Saved inputs for test case with id: ${savedTestCase.id}")

            val savedOutputs =
                newTestCase.expectedOutputs.map { output ->
                    testCaseExpectedOutputRepository.save(TestCaseExpectedOutput(output = output, testCase = savedTestCase))
                }
            logger.info("Saved outputs for test case with id: ${savedTestCase.id}")

            return buildTestCaseOutput(savedTestCase, savedInputs, savedOutputs)
        }

        private fun updateTestCase(
            testCaseId: String,
            newTestCase: CreateCaseInput,
        ): TestCaseOutput {
            val testCase = getTestCaseOrThrowNotFound(testCaseId)

            testCase.name = newTestCase.testCaseName

            val updatedTestCase = this.testCaseRepository.save(testCase)
            logger.info("Updated test case with id: ${updatedTestCase.id}")

            deleteOldInputsAndOutputs(testCase)
            logger.info("Deleted old inputs and outputs for test case with id: ${updatedTestCase.id}")
            val savedInputs = saveNewInputs(testCase, newTestCase.inputs)
            val savedOutputs = saveNewOutputs(testCase, newTestCase.expectedOutputs)
            logger.info("Saved new inputs and outputs for test case with id: ${updatedTestCase.id}")

            return buildTestCaseOutput(updatedTestCase, savedInputs, savedOutputs)
        }

        private fun getTestCaseOrThrowNotFound(testCaseId: String): TestCase {
            val testCaseOptional = this.testCaseRepository.findById(testCaseId)
            if (testCaseOptional.isEmpty) {
                logger.error("Test case with id $testCaseId not found")
                throw TestCaseNotFoundException()
            }

            return testCaseOptional.get()
        }

        private fun deleteOldInputsAndOutputs(testCase: TestCase) {
            logger.info("Attempting to delete old inputs and outputs for test case with id: ${testCase.id}")
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
            logger.info("Building test case output")
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
            logger.info("Received request to run test case with id: $testCaseId")
            val testCase =
                testCaseRepository.findById(testCaseId).getOrElse {
                    logger.error("Test case with id $testCaseId not found")
                    throw TestCaseNotFoundException()
                }

            logger.info("Getting snippet content for test case with id: ${testCase.id}")
            val snippetContent = snippetManagerService.getSnippet(testCase.snippet.id!!, token)

            return runTest(snippetContent.content, testCase.inputs, testCase.expectedOutputs, token)
        }

        private fun runTest(
            snippetContent: String,
            inputs: List<TestCaseInput>,
            expectedOutputs: List<TestCaseExpectedOutput>,
            token: String,
        ): TestCaseRunOutput {
            logger.info("Running test case with inputs: $inputs")
            val runResult = runnerApi.runSnippet(snippetContent, inputs.map { it.input }, token)

            if (runResult.errors.isNotEmpty()) {
                logger.info("Test case failed with errors: ${runResult.errors}")
                return TestCaseRunOutput(
                    hasPassed = false,
                    message = runResult.errors.joinToString(";"),
                )
            }

            val expectedStringOutputs = expectedOutputs.map { it.output }

            if (runResult.outputs != expectedStringOutputs) {
                logger.info("Test case failed. Expected outputs: $expectedStringOutputs . Actual outputs: ${runResult.outputs}")
                return TestCaseRunOutput(
                    hasPassed = false,
                    message = "Expected: $expectedStringOutputs  Actual: ${runResult.outputs}",
                )
            }

            logger.info("Test case passed")
            return TestCaseRunOutput(
                hasPassed = true,
                message = "",
            )
        }

        @Transactional
        fun deleteTestCaseById(testCaseId: String) {
            logger.info("Attempting to delete test case with id: $testCaseId")
            return this.testCaseRepository.deleteById(testCaseId)
        }
    }
