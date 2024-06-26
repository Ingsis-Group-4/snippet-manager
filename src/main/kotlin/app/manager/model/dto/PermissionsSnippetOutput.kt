package app.manager.model.dto

data class PermissionsSnippetOutput(
    val id: String,
    val snippetId: String,
    val authorId: String,
    val permissionType: String,
)
