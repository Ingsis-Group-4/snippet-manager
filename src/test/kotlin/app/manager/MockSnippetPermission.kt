package app.manager

import app.manager.integration.permission.SnippetPermissonApi
import app.manager.model.dto.PermissionCreateSnippetInput
import app.manager.model.dto.PermissionsSnippetOutput
import org.springframework.http.ResponseEntity

class MockSnippetPermission(private val list: MutableList<PermissionCreateSnippetInput> = mutableListOf()) :
    SnippetPermissonApi {
    override fun createSnippetPermission(
        body: PermissionCreateSnippetInput,
        token: String,
    ): ResponseEntity<Any> {
        list.add(body)
        return ResponseEntity.ok().build()
    }

    override fun getAllSnippetsPermission(
        userId: String,
        token: String,
    ): ResponseEntity<Array<PermissionsSnippetOutput>> {
        val auxList: MutableList<PermissionsSnippetOutput> = mutableListOf()
        for (i in list) {
            if (i.userId == userId) {
                auxList.add(
                    PermissionsSnippetOutput(
                        i.snippetId,
                        i.snippetId,
                        i.userId,
                        i.permissionType,
                    ),
                )
            }
        }
        return ResponseEntity.ok(auxList.toTypedArray())
    }

    override fun deleteSnippetPermissions(
        snippetId: String,
        token: String,
    ): ResponseEntity<String> {
        for (i in list) {
            if (i.snippetId == snippetId) {
                list.remove(i)
                return ResponseEntity.ok().build()
            }
        }
        return ResponseEntity.badRequest().build()
    }

    override fun getAuthorBySnippetId(
        snippetId: String,
        token: String,
    ): ResponseEntity<String> {
        for (i in list) {
            if (i.snippetId == snippetId) {
                val author = i.userId
                return ResponseEntity.ok(author)
            }
        }
        return ResponseEntity.badRequest().build()
    }
}
