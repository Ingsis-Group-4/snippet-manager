package app.manager.service

import app.manager.model.dto.CreateSnippetInput
import app.manager.model.dto.PermissionCreateSnippetOutput
import app.manager.model.dto.ShareSnippetInput
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.UUID

@Service
class ManagerService {
    @Value("\${azuriteBucket}")
    private lateinit var azuriteBucketUrlV1: String

    @Value("\${permissionsService}")
    private lateinit var permissionsServiceUrl: String

    private var restTemplate = RestTemplate()

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    fun createSnippet(input: CreateSnippetInput): String {
        val snippetKey = UUID.randomUUID().toString()
        val bucketURL = "$azuriteBucketUrlV1/$snippetKey"
        val bucketResponseEntity = createSnippetInBucket(bucketURL, input.content)

        if (bucketResponseEntity.statusCode.is2xxSuccessful) {
            println("Snippet created successfully. Creating permissions...")
            return createPermissionsForSnippet(input, snippetKey)
        } else {
            throw Exception("Failed to create snippet. Status code: ${bucketResponseEntity.statusCode}")
        }
    }

    private fun createSnippetInBucket(
        bucketURL: String,
        bucketBodyInfo: String,
    ): ResponseEntity<String> {
        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }
        val bucketRequestEntity = HttpEntity(bucketBodyInfo, headers)

        return this.restTemplate.postForEntity(bucketURL, bucketRequestEntity, String::class.java)
    }

    private fun createPermissionsForSnippet(
        input: CreateSnippetInput,
        snippetKey: String,
    ): String {
        val permissionsURL = "$permissionsServiceUrl/permission/snippet/create"
        val permissionsReq =
            PermissionCreateSnippetOutput(
                name = input.name,
                snippetKey = snippetKey,
                userId = input.userId,
            )
        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }
        val permissionBody = objectMapper.writeValueAsString(permissionsReq)
        val permissionResponseEntity =
            this.restTemplate.postForEntity(permissionsURL, HttpEntity(permissionBody, headers), Any::class.java)

        if (permissionResponseEntity.statusCode.is2xxSuccessful) {
            return "Snippet created successfully. Snippet key: $snippetKey"
        } else {
            throw Exception("Failed to create permissions for snippet $snippetKey. Status code: ${permissionResponseEntity.statusCode}")
        }
    }

    fun getSnippet(snippetKey: String): String {
        val bucketURL = "$azuriteBucketUrlV1/$snippetKey"
        val bucketResponseEntity = this.restTemplate.getForEntity(bucketURL, String::class.java)

        if (bucketResponseEntity.statusCode.is2xxSuccessful) {
            return bucketResponseEntity.body!!
        } else {
            throw Exception("Failed to get snippet. Status code: ${bucketResponseEntity.statusCode}")
        }
    }

    fun getSnippetsFromUserId(userId: String): String {
        val permissionsURL = "$permissionsServiceUrl/permission/snippet/all/$userId"
        val permissionResponseEntity = this.restTemplate.getForEntity(permissionsURL, String::class.java)

        if (permissionResponseEntity.statusCode.is2xxSuccessful) {
            return permissionResponseEntity.body!!
        } else {
            throw Exception("Failed to get snippets for user $userId. Status code: ${permissionResponseEntity.statusCode}")
        }
    }

    fun shareSnippet(input: ShareSnippetInput): String {
        val permissionsURL = "$permissionsServiceUrl/permission/snippet/share"
        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }
        val permissionBodyInput =
            ShareSnippetInput(
                snippetKey = input.snippetKey,
                userIds = input.userIds,
            )
        val permissionBody = objectMapper.writeValueAsString(permissionBodyInput)
        val permissionResponseEntity =
            this.restTemplate.postForEntity(permissionsURL, HttpEntity(permissionBody, headers), Any::class.java)

        if (permissionResponseEntity.statusCode.is2xxSuccessful) {
            return "Snippet shared successfully"
        } else {
            throw Exception("Failed to share snippet ${input.snippetKey}. Status code: ${permissionResponseEntity.statusCode}")
        }
    }
}
