package app.manager.requests

import app.manager.model.dto.CreateSnippetInput

fun createMockCreateSnippetRequest(number: String): CreateSnippetInput {
    return CreateSnippetInput(
        name = "Snippet $number",
        content = "Content $number",
        language = "Language $number",
    )
}
