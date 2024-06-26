package app.manager.model.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class PermissionCreateSnippetInput(
    @JsonProperty("snippetId")
    val snippetId: String,
    @JsonProperty("userId")
    val userId: String,
    @JsonProperty("permissionType")
    val permissionType: String,
)
