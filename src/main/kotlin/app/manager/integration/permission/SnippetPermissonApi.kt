package app.manager.integration.permission

import app.manager.model.dto.PermissionCreateSnippetInput
import app.manager.model.dto.PermissionListOutput
import org.springframework.http.ResponseEntity

interface SnippetPermissonApi {
    fun createSnippetPermission(
        body: PermissionCreateSnippetInput,
        token: String,
    ): ResponseEntity<Any>

    fun getAllSnippetsPermission(
        userId: String,
        token: String,
        pageNum: Int,
        pageSize: Int,
    ): ResponseEntity<PermissionListOutput>

    fun deleteSnippetPermissions(
        snippetId: String,
        token: String,
    ): ResponseEntity<String>

    fun getAuthorBySnippetId(
        snippetId: String,
        token: String,
    ): ResponseEntity<String>
}
