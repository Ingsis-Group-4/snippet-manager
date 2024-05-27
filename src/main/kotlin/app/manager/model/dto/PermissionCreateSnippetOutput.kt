package app.manager.model.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class PermissionCreateSnippetOutput(
    @JsonProperty("name")
    val name: String,
    @JsonProperty("snippetKey")
    val snippetKey: String,
    @JsonProperty("userId")
    val userId: String,
)
