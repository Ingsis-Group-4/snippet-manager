package app.manager.integration.permission

import app.logs.CorrelationIdFilter.Companion.CORRELATION_ID_KEY
import app.manager.model.dto.PermissionCreateSnippetInput
import app.manager.model.dto.PermissionListOutput
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.MDC
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

class RemoteSnippetPermission(val rest: RestTemplate, val permissionUrl: String, val objectMapper: ObjectMapper) :
    SnippetPermissonApi {
    override fun createSnippetPermission(
        body: PermissionCreateSnippetInput,
        token: String,
    ): ResponseEntity<Any> {
        val permissionBody = objectMapper.writeValueAsString(body)
        val createPermissionUrl = "$permissionUrl/create"
        val headers = getJsonHeader(token)
        return rest.postForEntity(createPermissionUrl, HttpEntity(permissionBody, headers), Any::class.java)
    }

    override fun getAllSnippetsPermission(
        userId: String,
        token: String,
        pageNum: Int,
        pageSize: Int,
    ): ResponseEntity<PermissionListOutput> {
        val getSnippetsUrl: String = "$permissionUrl/all?page_num=$pageNum&page_size=$pageSize"
        val headers = getJsonHeader(token)
        val entity: HttpEntity<Void> = HttpEntity(headers)
        return rest.exchange(getSnippetsUrl, HttpMethod.GET, entity, PermissionListOutput::class.java)
    }

    override fun deleteSnippetPermissions(
        snippetId: String,
        token: String,
    ): ResponseEntity<String> {
        val deleteSnippetUrl = "$permissionUrl/all/$snippetId"
        val headers = getJsonHeader(token)
        val entity: HttpEntity<Void> = HttpEntity(headers)
        try {
            rest.exchange(deleteSnippetUrl, HttpMethod.DELETE, entity, String::class.java)
        } catch (e: Exception) {
            return ResponseEntity.badRequest().build()
        }
        return ResponseEntity.ok().build()
    }

    override fun getAuthorBySnippetId(
        snippetId: String,
        token: String,
    ): ResponseEntity<String> {
        val getAuthorUrl = "$permissionUrl/author/$snippetId"
        val headers = getJsonHeader(token)
        val entity: HttpEntity<Void> = HttpEntity(headers)
        return rest.exchange(getAuthorUrl, HttpMethod.GET, entity, String::class.java)
    }

    private fun getJsonHeader(token: String): HttpHeaders {
        val correlationId = MDC.get(CORRELATION_ID_KEY)
        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                set("Authorization", "Bearer $token")
                set("X-Correlation-Id", correlationId)
            }
        return headers
    }
}
