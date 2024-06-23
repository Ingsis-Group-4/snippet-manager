package app.cases.integration.runner

import app.common.integration.runner.RunOutput
import app.common.integration.runner.SnippetRunnerApi

class SnippetRunnerApiMock : SnippetRunnerApi {
    override fun runSnippet(
        content: String,
        inputs: List<String>,
        token: String,
    ): RunOutput {
        return RunOutput(
            outputs = listOf("output 1", "output 2"),
            errors = listOf(),
        )
    }

    override fun formatSnippet(
        content: String,
        ruleConfig: String,
        token: String,
    ): String {
        TODO("Not yet implemented")
    }
}
