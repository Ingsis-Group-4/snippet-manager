package app.manager.integration.permission

import app.manager.model.dto.PermissionCreateSnippetInput
import app.manager.model.dto.PermissionsSnippetOutput
import org.springframework.http.ResponseEntity

interface SnippetPermissonApi {
    fun createSnippetPermission(
        body: PermissionCreateSnippetInput,
        token: String,
    ): ResponseEntity<Any>

    fun getAllSnippetsPermission(
        userId: String,
        token: String,
    ): ResponseEntity<Array<PermissionsSnippetOutput>>

    fun deleteSnippetPermissions(
        snippetId: String,
        token: String,
    ): ResponseEntity<String>

    fun getAuthorBySnippetId(
        snippetId: String,
        token: String,
    ): ResponseEntity<String>
}
