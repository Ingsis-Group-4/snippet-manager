package app.manager.model.dto

data class PermissionCreateSnippetOutput(
    val name: String,
    val snippetKey: String,
    val userId: String,
)
