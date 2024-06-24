package app.manager.model.dto

class SnippetListOutput(
    val snippets: List<GetSnippetWithStatusOutput>,
    val count: Int,
)
