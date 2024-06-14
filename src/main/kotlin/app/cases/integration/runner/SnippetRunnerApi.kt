package app.cases.integration.runner

interface SnippetRunnerApi {
    fun runSnippet(
        content: String,
        inputs: List<String>,
    ): RunOutput
}
