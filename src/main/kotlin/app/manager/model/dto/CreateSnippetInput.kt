package app.manager.model.dto

import jakarta.validation.constraints.NotBlank

data class CreateSnippetInput(
    @NotBlank
    val name: String,
    @NotBlank
    val content: String,
    @NotBlank
    val userId: String,
    @NotBlank
    val language: String,
)
