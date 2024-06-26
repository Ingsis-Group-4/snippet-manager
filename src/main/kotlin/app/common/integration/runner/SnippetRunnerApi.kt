package app.common.integration.runner

import app.cases.model.dto.TestCaseEnvDto

interface SnippetRunnerApi {
    fun runSnippet(
        content: String,
        inputs: List<String>,
        envs: List<TestCaseEnvDto>,
        token: String,
    ): RunOutput

    fun formatSnippet(
        content: String,
        ruleConfig: String,
        token: String,
    ): String
}
