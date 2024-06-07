package app.manager.service

import app.manager.integration.asset.AssetStoreApi
import app.manager.integration.permission.SnippetPermissonApi
import app.manager.model.dto.CreateSnippetInput
import app.manager.model.dto.GetAllSnippetsOutput
import app.manager.model.dto.GetSnippetOutput
import app.manager.model.dto.PermissionCreateSnippetInput
import app.manager.model.dto.ShareSnippetInput
import app.manager.persistance.entity.Snippet
import app.manager.persistance.repository.SnippetRepository
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.UUID

@Service
class ManagerService
    @Autowired
    constructor(
        private val snippetRepository: SnippetRepository,
        private val assetStoreApi: AssetStoreApi,
        private val snippetPermissionApi: SnippetPermissonApi,
    ) {
        @Value("\${azuriteBucket}")
        private lateinit var azuriteBucketUrlV1: String

        @Value("\${permissionsService}")
        private lateinit var permissionsServiceUrl: String

        @Autowired
        private lateinit var restTemplate: RestTemplate

        @Autowired
        private lateinit var objectMapper: ObjectMapper

        @Transactional
        fun createSnippet(
            input: CreateSnippetInput,
            userId: String,
        ): String {
            val snippetKey = UUID.randomUUID().toString()

            val snippet = snippetPersistence(input, snippetKey)

            val bucketResponseEntity = assetStoreApi.createSnippetInBucket(snippetKey, input.content)
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

        private fun createPermissionsForSnippet(
            snippetId: String,
            userId: String,
        ): String {
            val permissionsReq =
                PermissionCreateSnippetInput(
                    snippetId = snippetId,
                    userId = userId,
                    permissionType = "OWNER",
                )
            val permissionResponseEntity = snippetPermissionApi.createSnippetPermission(permissionsReq)

            if (permissionResponseEntity.statusCode.is2xxSuccessful) {
                return "Snippet created successfully. Snippet id: $snippetId"
            } else {
                throw Exception("Failed to create permissions for snippet $snippetId. Status code: ${permissionResponseEntity.statusCode}")
            }
        }

        fun getSnippet(snippetId: String): GetSnippetOutput {
            val snippet = snippetRepository.findSnippetById(snippetId) ?: throw Exception("Snippet not found")
            val snippetKey = snippet.snippetKey

            val bucketResponseEntity = assetStoreApi.getSnippet(snippetKey)

            if (bucketResponseEntity.statusCode.is2xxSuccessful) {
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
            val permissionResponseEntity =
                snippetPermissionApi.getAllSnippetsPermission(userId)

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
            snippetRepository.findSnippetById(input.snippetId) ?: throw Exception("Snippet not found")
            val permissionBodyInput =
                PermissionCreateSnippetInput(
                    snippetId = input.snippetId,
                    userId = input.userId,
                    permissionType = "SHARED",
                )
            val permissionResponseEntity =
                snippetPermissionApi.createSnippetPermission(permissionBodyInput)

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
            try {
                val bucketResponse = assetStoreApi.deleteSnippet(snippetKey)
                val permissionResponse = snippetPermissionApi.deleteSnippetPermissions(snippetId)
                throwExceptionIfResponseError(bucketResponse)
                throwExceptionIfResponseError(permissionResponse)
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

        private fun throwExceptionIfResponseError(bucketResponse: ResponseEntity<String>) {
            if (bucketResponse.statusCode.isError) throw Exception("Failed to create snippet. Status code: ${bucketResponse.statusCode}")
        }
    }
