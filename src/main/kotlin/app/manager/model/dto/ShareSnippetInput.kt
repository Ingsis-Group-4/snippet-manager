package app.manager.model.dto

import jakarta.validation.constraints.NotBlank

data class ShareSnippetInput(
    @field:NotBlank(message = "Snippet Key must not be blank")
    val snippetKey: String?,
    val userIds: List<String> = listOf(),
)
