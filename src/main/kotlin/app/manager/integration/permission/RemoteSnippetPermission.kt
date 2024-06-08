package app.manager.integration.permission

import app.manager.model.dto.PermissionCreateSnippetInput
import app.manager.model.dto.PermissionsSnippetOutput
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

class RemoteSnippetPermission(val rest: RestTemplate, val permissionUrl: String, val objectMapper: ObjectMapper) : SnippetPermissonApi {
    override fun createSnippetPermission(body: PermissionCreateSnippetInput): ResponseEntity<Any> {
        val permissionBody = objectMapper.writeValueAsString(body)
        val createPermissionUrl = "$permissionUrl/create"
        val headers = getJsonHeader()
        return rest.postForEntity(createPermissionUrl, HttpEntity(permissionBody, headers), Any::class.java)
    }

    override fun getAllSnippetsPermission(userId: String): ResponseEntity<Array<PermissionsSnippetOutput>> {
        val getSnippetsUrl: String = "$permissionUrl/all/$userId"
        return rest.getForEntity(getSnippetsUrl, Array<PermissionsSnippetOutput>::class.java)
    }

    override fun deleteSnippetPermissions(snippetId: String): ResponseEntity<String> {
        val deleteSnippetUrl = "$permissionUrl/all/$snippetId"
        try {
            rest.delete(deleteSnippetUrl)
        } catch (e: Exception) {
            return ResponseEntity.badRequest().build()
        }
        return ResponseEntity.ok().build()
    }

    private fun getJsonHeader(): HttpHeaders {
        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }
        return headers
    }
}
