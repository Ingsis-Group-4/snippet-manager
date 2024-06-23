package app.manager.service

import app.cases.exception.SnippetNotFoundException
import app.manager.exceptions.NotFoundException
import app.manager.integration.asset.AssetStoreApi
import app.manager.integration.permission.SnippetPermissonApi
import app.manager.model.dto.CreateSnippetInput
import app.manager.model.dto.GetSnippetOutput
import app.manager.model.dto.PermissionCreateSnippetInput
import app.manager.model.dto.ShareSnippetInput
import app.manager.persistance.entity.Snippet
import app.manager.persistance.repository.SnippetRepository
import app.run.model.dto.SnippetContent
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ManagerService
    @Autowired
    constructor(
        private val snippetRepository: SnippetRepository,
        private val assetStoreApi: AssetStoreApi,
        private val snippetPermissionApi: SnippetPermissonApi,
    ) {
        private val logger = LoggerFactory.getLogger(ManagerService::class.java)

        @Transactional
        fun createSnippet(
            input: CreateSnippetInput,
            userId: String,
            token: String,
        ): GetSnippetOutput {
            logger.info("Received request to create snippet with name: ${input.name}")
            val snippetKey = UUID.randomUUID().toString()

            val snippet = snippetPersistence(input, snippetKey)
            logger.info("Attempting to create snippet in bucket with key: $snippetKey")
            val bucketResponseEntity = assetStoreApi.createSnippetInBucket(snippetKey, input.content)
            if (bucketResponseEntity.statusCode.is2xxSuccessful) {
                logger.info("Snippet created successfully in bucket. Snippet id: ${snippet.id}")
                try {
                    logger.info("Attempting to create permissions for snippet ${snippet.id}")
                    createPermissionsForSnippet(snippet.id!!, userId, token)
                } catch (e: Exception) {
                    throw Exception(e.message)
                }
                return GetSnippetOutput(
                    name = snippet.name,
                    id = snippet.id,
                    language = snippet.language,
                    author = userId,
                    content = input.content,
                )
            } else {
                logger.error("Failed to create snippet in bucket. Status code: ${bucketResponseEntity.statusCode}")
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
            logger.info("Snippet persisted successfully. Snippet id: ${snippet.id}")
            return snippet
        }

        private fun createPermissionsForSnippet(
            snippetId: String,
            userId: String,
            token: String,
        ): String {
            val permissionsReq =
                PermissionCreateSnippetInput(
                    snippetId = snippetId,
                    userId = userId,
                    permissionType = "OWNER",
                )
            val permissionResponseEntity = snippetPermissionApi.createSnippetPermission(permissionsReq, token)

            if (permissionResponseEntity.statusCode.is2xxSuccessful) {
                logger.info("Permissions created successfully for snippet $snippetId")
                return "Snippet created successfully. Snippet id: $snippetId"
            } else {
                logger.error("Failed to create permissions for snippet $snippetId. Status code: ${permissionResponseEntity.statusCode}")
                throw Exception("Failed to create permissions for snippet $snippetId. Status code: ${permissionResponseEntity.statusCode}")
            }
        }

        fun getSnippet(
            snippetId: String,
            token: String,
        ): GetSnippetOutput {
            logger.info("Received request to get snippet with id: $snippetId")
            val snippet = snippetRepository.findSnippetById(snippetId)
            if (snippet == null) {
                logger.error("Snippet with id $snippetId not found")
                throw NotFoundException("Snippet not found")
            }
            logger.info("Snippet found with id $snippetId. Attempting to get snippet content from bucket.")
            val snippetKey = snippet.snippetKey

            val bucketResponseEntity = assetStoreApi.getSnippet(snippetKey)

            logger.info("Attempting to get author for snippet $snippetId")
            val authorResponse = snippetPermissionApi.getAuthorBySnippetId(snippetId, token)
            if (authorResponse.statusCode.isError) {
                logger.error("Failed to get author for snippet $snippetId. Status code: ${authorResponse.statusCode}")
                throw NotFoundException("Failed to get author for snippet $snippetId. Status code: ${authorResponse.statusCode}")
            }
            val author = authorResponse.body!!

            if (bucketResponseEntity.statusCode.is2xxSuccessful) {
                logger.info("Snippet content retrieved successfully from bucket for snippet $snippetId")
                val content = bucketResponseEntity.body!!
                return GetSnippetOutput(
                    id = snippetId,
                    name = snippet.name,
                    content = content,
                    language = snippet.language,
                    author = author,
                )
            } else {
                logger.error("Failed to get snippet content from bucket. Status code: ${bucketResponseEntity.statusCode}")
                throw NotFoundException("Failed to get snippet. Status code: ${bucketResponseEntity.statusCode}")
            }
        }

        fun getSnippetsFromUserId(
            userId: String,
            token: String,
        ): List<GetSnippetOutput> {
            logger.info("Received request to get all snippets for user $userId")
            logger.info("Attempting to get all snippets for user $userId from permission service")
            val permissionResponseEntity =
                snippetPermissionApi.getAllSnippetsPermission(userId, token)

            if (permissionResponseEntity.statusCode.is2xxSuccessful) {
                logger.info("Snippets retrieved successfully for user $userId from permission service")
                val snippets: MutableList<GetSnippetOutput> = emptyList<GetSnippetOutput>().toMutableList()
                for (permissionSnippet in permissionResponseEntity.body!!) {
                    val snippetId = permissionSnippet.snippetId
                    val snippetAuthor = permissionSnippet.authorId
                    val snippet = snippetRepository.findSnippetById(snippetId)
                    if (snippet == null) {
                        logger.error("Snippet with id $snippetId not found in repository")
                        throw NotFoundException("Snippet not found")
                    }
                    logger.info("Attempting to get snippet content for snippet $snippetId from bucket")
                    val contentResponse =
                        assetStoreApi.getSnippet(snippet.snippetKey)
                    if (!contentResponse.statusCode.is2xxSuccessful) {
                        logger.error("Failed to get snippet content for snippet $snippetId. Status code: ${contentResponse.statusCode}")
                        throw Exception("Failed to get snippet content for snippet $snippetId. Status code: ${contentResponse.statusCode}")
                    }
                    logger.info("Snippet content retrieved successfully for snippet $snippetId")
                    val content = contentResponse.body!!
                    val snippetOutput =
                        GetSnippetOutput(
                            id = snippet.id!!,
                            name = snippet.name,
                            language = snippet.language,
                            author = snippetAuthor,
                            content = content,
                        )
                    snippets.add(snippetOutput)
                }
                logger.info("Returning snippets for user $userId")
                return snippets
            } else {
                logger.error(
                    "Failed to get snippets for user $userId from permission service. Status code: ${permissionResponseEntity.statusCode}",
                )
                throw Exception("Failed to get snippets for user $userId.")
            }
        }

        fun shareSnippet(
            input: ShareSnippetInput,
            token: String,
        ): String {
            logger.info("Received request to share snippet ${input.snippetId} with user ${input.userId}")
            if (snippetRepository.findSnippetById(input.snippetId) == null) {
                logger.error("Snippet with id ${input.snippetId} not found in repository")
                throw NotFoundException("Snippet not found")
            }

            val permissionBodyInput =
                PermissionCreateSnippetInput(
                    snippetId = input.snippetId,
                    userId = input.userId,
                    permissionType = "SHARED",
                )
            logger.info("Attempting to create sharing permission for snippet ${input.snippetId}")
            val permissionResponseEntity =
                snippetPermissionApi.createSnippetPermission(permissionBodyInput, token)

            if (permissionResponseEntity.statusCode.is2xxSuccessful) {
                logger.info("Snippet shared successfully")
                return "Snippet shared successfully"
            } else {
                logger.error("Failed to share snippet ${input.snippetId}. Status code: ${permissionResponseEntity.statusCode}")
                throw Exception("Failed to share snippet ${input.snippetId}. Status code: ${permissionResponseEntity.statusCode}")
            }
        }

        @Transactional
        fun deleteSnippet(
            snippetId: String,
            token: String,
        ): String {
            logger.info("Received request to delete snippet $snippetId")
            val snippet = snippetRepository.findSnippetById(snippetId)
            if (snippet == null) {
                logger.error("Snippet with id $snippetId not found in repository")
                throw NotFoundException("Snippet not found")
            }
            logger.info("Attempting to delete snippet $snippetId from bucket and from permission service")
            val snippetKey = snippet.snippetKey
            try {
                val bucketResponse = assetStoreApi.deleteSnippet(snippetKey)
                val permissionResponse = snippetPermissionApi.deleteSnippetPermissions(snippetId, token)
                throwExceptionIfResponseError(bucketResponse)
                throwExceptionIfResponseError(permissionResponse)
                this.snippetRepository.deleteSnippetById(snippetId)
            } catch (e: Exception) {
                throw Exception("Failed to delete snippet $snippetId.")
            }
            logger.info("Snippet $snippetId deleted successfully")
            return "Snippet deleted successfully"
        }

        private fun throwExceptionIfResponseError(bucketResponse: ResponseEntity<String>) {
            if (bucketResponse.statusCode.isError) {
                logger.error("Failed to delete snippet. Status code: ${bucketResponse.statusCode}")
                throw Exception("Failed to create snippet. Status code: ${bucketResponse.statusCode}")
            }
        }

        fun updateSnippet(
            snippetId: String,
            snippetUpdateInput: SnippetContent,
            token: String,
        ): GetSnippetOutput {
            logger.info("Received request to update snippet $snippetId")
            val snippet = this.snippetRepository.findSnippetById(snippetId) ?: throw SnippetNotFoundException()

            logger.info("Attempting to delete snippet $snippetId from bucket and update snippet content")
            assetStoreApi.deleteSnippet(snippet.snippetKey)
            assetStoreApi.updateSnippet(snippet.snippetKey, snippetUpdateInput.content)

            logger.info("Attempting to get author for snippet $snippetId")
            val authorResponse = snippetPermissionApi.getAuthorBySnippetId(snippetId, token)

            if (authorResponse.statusCode.isError) {
                logger.error("Failed to get author for snippet $snippetId. Status code: ${authorResponse.statusCode}")
                throw Exception(
                    "Request to permission service was unsuccessful. " +
                        "Reason: {status: ${authorResponse.statusCode}: ${authorResponse.body}}",
                )
            }

            logger.info("Snippet $snippetId updated successfully")
            return GetSnippetOutput(
                id = snippetId,
                name = snippet.name,
                content = snippetUpdateInput.content,
                language = snippet.language,
                author = authorResponse.body!!,
            )
        }
    }
