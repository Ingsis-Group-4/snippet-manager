package app.common.integration.runner

interface SnippetRunnerApi {
    fun runSnippet(
        content: String,
        inputs: List<String>,
    ): RunOutput

    fun formatSnippet(
        content: String,
        ruleConfig: String,
    ): String
}
