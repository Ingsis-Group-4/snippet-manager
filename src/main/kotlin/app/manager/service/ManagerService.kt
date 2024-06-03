package app.manager.service

import app.manager.model.dto.CreateSnippetInput
import app.manager.model.dto.GetAllSnippetsOutput
import app.manager.model.dto.GetSnippetOutput
import app.manager.model.dto.PermissionCreateSnippetInput
import app.manager.model.dto.PermissionsSnippetOutput
import app.manager.model.dto.ShareSnippetInput
import app.manager.persistance.entity.Snippet
import app.manager.persistance.repository.SnippetRepository
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
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
class ManagerService(
    @Autowired
    private val snippetRepository: SnippetRepository,
) {
    @Value("\${azuriteBucket}")
    private lateinit var azuriteBucketUrlV1: String

    @Value("\${permissionsService}")
    private lateinit var permissionsServiceUrl: String

    private var restTemplate = RestTemplate()

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Transactional
    fun createSnippet(
        input: CreateSnippetInput,
        userId: String,
    ): String {
        val snippetKey = UUID.randomUUID().toString()
        val bucketURL = "$azuriteBucketUrlV1/$snippetKey"

        val snippet = snippetPersistence(input, snippetKey)

        val bucketResponseEntity = createSnippetInBucket(bucketURL, input.content)
        if (bucketResponseEntity.statusCode.is2xxSuccessful) {
            return createPermissionsForSnippet(snippet.id!!, userId)
        } else {
            throw Exception("Failed to create snippet. Status code: ${bucketResponseEntity.statusCode}")
        }
    }

    private fun snippetPersistence(
        input: CreateSnippetInput,
        snippetKey: String,
    ): Snippet {
        val snippet =
            Snippet(
                name = input.name,
                snippetKey = snippetKey,
                language = input.language,
            )
        snippetRepository.save(snippet)
        println("Persisted snippet with key: $snippetKey")
        return snippet
    }

    private fun createSnippetInBucket(
        bucketURL: String,
        bucketBodyInfo: String,
    ): ResponseEntity<String> {
        val headers = getJsonHeader()
        val bucketRequestEntity = HttpEntity(bucketBodyInfo, headers)

        return this.restTemplate.postForEntity(bucketURL, bucketRequestEntity, String::class.java)
    }

    private fun createPermissionsForSnippet(
        snippetId: String,
        userId: String,
    ): String {
        val permissionsURL = "$permissionsServiceUrl/permission/create"
        println(permissionsURL)
        val permissionsReq =
            PermissionCreateSnippetInput(
                snippetId = snippetId,
                userId = userId,
                permissionType = "OWNER",
            )
        val headers = getJsonHeader()
        val permissionBody = objectMapper.writeValueAsString(permissionsReq)
        val permissionResponseEntity =
            this.restTemplate.postForEntity(permissionsURL, HttpEntity(permissionBody, headers), Any::class.java)
        println(permissionResponseEntity.statusCode)

        if (permissionResponseEntity.statusCode.is2xxSuccessful) {
            println("Created permissions for snippet $snippetId")
            return "Snippet created successfully. Snippet id: $snippetId"
        } else {
            throw Exception("Failed to create permissions for snippet $snippetId. Status code: ${permissionResponseEntity.statusCode}")
        }
    }

    fun getSnippet(snippetId: String): GetSnippetOutput {
        println(snippetId)
        println()
        val snippet = snippetRepository.findSnippetById(snippetId) ?: throw Exception("Snippet not found")
        val snippetKey = snippet.snippetKey
        print(snippetKey)
        val bucketURL = "$azuriteBucketUrlV1/$snippetKey"
        val bucketResponseEntity = this.restTemplate.getForEntity(bucketURL, String::class.java)

        if (bucketResponseEntity.statusCode.is2xxSuccessful) {
            println("Success")
            val content = bucketResponseEntity.body!!
            return GetSnippetOutput(
                name = snippet.name,
                content = content,
            )
        } else {
            throw Exception("Failed to get snippet. Status code: ${bucketResponseEntity.statusCode}")
        }
    }

    fun getSnippetsFromUserId(userId: String): List<GetAllSnippetsOutput> {
        val permissionsURL = "$permissionsServiceUrl/permission/all/$userId"
        val permissionResponseEntity =
            this.restTemplate.getForEntity(permissionsURL, Array<PermissionsSnippetOutput>::class.java)

        if (permissionResponseEntity.statusCode.is2xxSuccessful) {
            val snippets: MutableList<GetAllSnippetsOutput> = emptyList<GetAllSnippetsOutput>().toMutableList()
            for (permissionSnippet in permissionResponseEntity.body!!) {
                val snippetId = permissionSnippet.snippetId
                val snippetAuthor = permissionSnippet.authorId
                val snippet = snippetRepository.findSnippetById(snippetId) ?: throw Exception("Snippet not found")
                val snippetOutput =
                    GetAllSnippetsOutput(
                        name = snippet.name,
                        snippetId = snippet.id!!,
                        language = snippet.language,
                        author = snippetAuthor,
                    )
                snippets.add(snippetOutput)
            }
            return snippets
        } else {
            throw Exception("Failed to get snippets for user $userId. Status code: ${permissionResponseEntity.statusCode}")
        }
    }

    fun shareSnippet(input: ShareSnippetInput): String {
        val permissionsURL = "$permissionsServiceUrl/permission/create"
        snippetRepository.findSnippetById(input.snippetId) ?: throw Exception("Snippet not found")
        val headers =
            getJsonHeader()
        println(input.snippetId)
        println(input.userId)
        val permissionBodyInput =
            PermissionCreateSnippetInput(
                snippetId = input.snippetId,
                userId = input.userId,
                permissionType = "SHARED",
            )
        val permissionBody = objectMapper.writeValueAsString(permissionBodyInput)
        val permissionResponseEntity =
            this.restTemplate.postForEntity(permissionsURL, HttpEntity(permissionBody, headers), Any::class.java)

        if (permissionResponseEntity.statusCode.is2xxSuccessful) {
            return "Snippet shared successfully"
        } else {
            throw Exception("Failed to share snippet ${input.snippetId}. Status code: ${permissionResponseEntity.statusCode}")
        }
    }

    @Transactional
    fun deleteSnippet(snippetId: String): String {
        val snippet = snippetRepository.findSnippetById(snippetId) ?: throw Exception("Snippet not found")
        val snippetKey = snippet.snippetKey
        val bucketURL = "$azuriteBucketUrlV1/$snippetKey"
        try {
            this.restTemplate.delete(bucketURL)
            this.snippetRepository.deleteSnippetById(snippetId)
        } catch (e: Exception) {
            throw Exception("Failed to delete snippet $snippetId.")
        }
        return "Snippet deleted successfully"
    }

    private fun getJsonHeader(): HttpHeaders {
        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }
        return headers
    }
}
