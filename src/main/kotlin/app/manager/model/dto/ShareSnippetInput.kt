package app.manager.model.dto

import jakarta.validation.constraints.NotBlank

data class ShareSnippetInput(
    @field:NotBlank(message = "Snippet Id must not be blank")
    val snippetId: String?,
    val userId: String,
    val permissionType: String = "SHARED",
)
