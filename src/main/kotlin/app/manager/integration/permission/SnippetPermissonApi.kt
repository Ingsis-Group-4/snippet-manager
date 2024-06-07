package app.manager.integration.permission

import app.manager.model.dto.PermissionCreateSnippetInput
import app.manager.model.dto.PermissionsSnippetOutput
import org.springframework.http.ResponseEntity

interface SnippetPermissonApi {
    fun createSnippetPermission(body: PermissionCreateSnippetInput): ResponseEntity<Any>

    fun getAllSnippetsPermission(userId: String): ResponseEntity<Array<PermissionsSnippetOutput>>

    fun deleteSnippetPermissions(snippetId: String): ResponseEntity<String>
}
