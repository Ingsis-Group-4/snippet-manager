package app.cases.integration.runner

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
}
