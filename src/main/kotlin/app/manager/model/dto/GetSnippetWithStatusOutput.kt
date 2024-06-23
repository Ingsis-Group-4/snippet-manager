package app.manager.model.dto

import app.manager.model.enums.SnippetStatus

class GetSnippetWithStatusOutput(
    val id: String,
    val name: String,
    val content: String,
    val language: String,
    val author: String,
    val status: SnippetStatus,
)
