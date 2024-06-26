package app.manager.service

import app.cases.exception.SnippetNotFoundException
import app.manager.exceptions.NotFoundException
import app.manager.integration.asset.AssetStoreApi
import app.manager.integration.permission.SnippetPermissonApi
import app.manager.model.dto.CreateSnippetInput
import app.manager.model.dto.GetSnippetOutput
import app.manager.model.dto.GetSnippetWithStatusOutput
import app.manager.model.dto.PermissionCreateSnippetInput
import app.manager.model.dto.ShareSnippetInput
import app.manager.model.dto.SnippetListOutput
import app.manager.model.enums.SnippetStatus
import app.manager.persistance.entity.Snippet
import app.manager.persistance.entity.SnippetUserStatus
import app.manager.persistance.repository.SnippetRepository
import app.manager.persistance.repository.SnippetUserStatusRepository
import app.run.model.dto.SnippetContent
import app.user.UserService
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ManagerService
    @Autowired
    constructor(
        private val snippetRepository: SnippetRepository,
        private val assetStoreApi: AssetStoreApi,
        private val snippetPermissionApi: SnippetPermissonApi,
        private val userService: UserService,
        private val snippetUserStatusRepository: SnippetUserStatusRepository,
    ) {
        private val logger = LoggerFactory.getLogger(ManagerService::class.java)

        @Transactional
        fun createSnippet(
            input: CreateSnippetInput,
            userId: String,
            token: String,
        ): GetSnippetOutput {
            logger.info("Received request to create snippet for user $userId")
            val snippetKey = UUID.randomUUID().toString()
            logger.info("Persisting snippet with key $snippetKey")
            val snippet = snippetPersistence(input, snippetKey)

            logger.info("Attempting to create snippet in asset store")
            logger.info("Content to be saved: ${input.content}")
            val bucketResponseEntity = assetStoreApi.createSnippetInBucket(snippetKey, input.content)
            if (bucketResponseEntity.statusCode.is2xxSuccessful) {
                logger.info("Snippet created in asset store")
                try {
                    logger.info("Attempting to create permissions for snippet")
                    createPermissionsForSnippet(snippet.id!!, userId, token)
                } catch (e: Exception) {
                    logger.error("Failed to create permissions for snippet. Reason: ${e.message}")
                    throw Exception(e.message)
                }

                logger.info("Creating user status for snippet")
                createUserStatusForSnippet(userId, snippet)

                val username = getUsernameFromUserId(userId)

                logger.info("Returning snippet output")
                return GetSnippetOutput(
                    name = snippet.name,
                    id = snippet.id,
                    language = snippet.language,
                    author = username,
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
            logger.info("Snippet persisted successfully")
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
                logger.info("Permissions created for snippet $snippetId")
                return "Snippet created successfully. Snippet id: $snippetId"
            } else {
                logger.error("Failed to create permissions for snippet $snippetId. Status code: ${permissionResponseEntity.statusCode}")
                throw Exception("Failed to create permissions for snippet $snippetId. Status code: ${permissionResponseEntity.statusCode}")
            }
        }

        private fun createUserStatusForSnippet(
            userId: String,
            snippet: Snippet,
        ) {
            this.snippetUserStatusRepository.save(SnippetUserStatus(userId, SnippetStatus.PENDING, snippet))
            logger.info("User status created for snippet ${snippet.id}")
        }

        fun getSnippet(
            snippetId: String,
            token: String,
        ): GetSnippetOutput {
            logger.info("Received request to get snippet with id $snippetId")
            val snippet = snippetRepository.findSnippetById(snippetId)
            if (snippet == null) {
                logger.error("Snippet with id $snippetId not found")
                throw SnippetNotFoundException()
            }
            val snippetKey = snippet.snippetKey

            logger.info("Attempting to get snippet from asset store")
            val bucketResponseEntity = assetStoreApi.getSnippet(snippetKey)

            logger.info("Attempting to get author for snippet")
            val authorResponse = snippetPermissionApi.getAuthorBySnippetId(snippetId, token)
            if (authorResponse.statusCode.isError) {
                logger.error("Failed to get author for snippet $snippetId. Status code: ${authorResponse.statusCode}")
                throw NotFoundException("Failed to get author for snippet $snippetId. Status code: ${authorResponse.statusCode}")
            }
            val author = authorResponse.body!!
            val username = getUsernameFromUserId(author)

            if (bucketResponseEntity.statusCode.is2xxSuccessful) {
                logger.info("Snippet retrieved successfully from bucket")
                val content = bucketResponseEntity.body!!
                logger.info("Returning snippet output")
                return GetSnippetOutput(
                    id = snippetId,
                    name = snippet.name,
                    content = content,
                    language = snippet.language,
                    author = username,
                )
            } else {
                logger.error("Failed to get snippet from bucket. Status code: ${bucketResponseEntity.statusCode}")
                throw NotFoundException("Failed to get snippet. Status code: ${bucketResponseEntity.statusCode}")
            }
        }

        fun getSnippetsFromUserId(
            userId: String,
            token: String,
            pageNum: Int,
            pageSize: Int,
        ): SnippetListOutput {
            logger.info("Received request to get snippets for user $userId")
            logger.info("Attempting to get snippets from permission service")
            val permissionResponseEntity =
                snippetPermissionApi.getAllSnippetsPermission(userId, token, pageNum, pageSize)

            if (permissionResponseEntity.statusCode.is2xxSuccessful) {
                logger.info("Snippets retrieved successfully from permissions service")
                val snippets: MutableList<GetSnippetWithStatusOutput> = mutableListOf()
                for (permissionSnippet in permissionResponseEntity.body!!.permissions) {
                    val snippetId = permissionSnippet.snippetId
                    val snippetAuthor = permissionSnippet.authorId
                    val snippet = snippetRepository.findSnippetById(snippetId)
                    if (snippet == null) {
                        logger.error("Snippet with id $snippetId not found")
                        throw SnippetNotFoundException()
                    }
                    logger.info("Attempting to get snippet content for snippet $snippetId from bucket")
                    val contentResponse =
                        assetStoreApi.getSnippet(snippet.snippetKey)
                    if (!contentResponse.statusCode.is2xxSuccessful) {
                        logger.error("Failed to get snippet content for snippet $snippetId. Status code: ${contentResponse.statusCode}")
                        throw Exception("Failed to get snippet content for snippet $snippetId. Status code: ${contentResponse.statusCode}")
                    }

                    val snippetStatus =
                        snippetUserStatusRepository.findByUserIdAndSnippet_Id(
                            userId,
                            snippet.id!!,
                        )
                    if (snippetStatus == null) {
                        logger.error("Snippet status for user not found")
                        throw Exception("Snippet status for user not found")
                    }
                    val content = contentResponse.body!!
                    val snippetOutput =
                        GetSnippetWithStatusOutput(
                            id = snippet.id,
                            name = snippet.name,
                            language = snippet.language,
                            author = snippetAuthor,
                            content = content,
                            status = snippetStatus.status,
                        )
                    logger.info("Adding snippet to list")
                    snippets.add(snippetOutput)
                }
                logger.info("Returning snippet list output")
                return SnippetListOutput(snippets, permissionResponseEntity.body!!.count)
            } else {
                logger.error(
                    "Failed to get snippets for user $userId from permissions service. Status code: ${permissionResponseEntity.statusCode}",
                )
                throw Exception("Failed to get snippets for user $userId.")
            }
        }

        fun shareSnippet(
            input: ShareSnippetInput,
            token: String,
        ): String {
            logger.info("Received request to share snippet with id ${input.snippetId} with user ${input.userId}")
            snippetRepository.findSnippetById(input.snippetId) ?: {
                logger.error("Snippet with id ${input.snippetId} not found")
                throw SnippetNotFoundException()
            }
            val permissionBodyInput =
                PermissionCreateSnippetInput(
                    snippetId = input.snippetId,
                    userId = input.userId,
                    permissionType = "SHARED",
                )
            logger.info("Attempting to create sharing permission for snippet")
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
            logger.info("Received request to delete snippet with id $snippetId")
            val snippetKey =
                snippetRepository.findSnippetById(snippetId)?.snippetKey ?: throw SnippetNotFoundException()

            assetStoreApi.deleteSnippet(snippetKey).takeIf {
                it.statusCode.isError
            }?.let {
                logger.info("Failed to delete snippet from bucket. Status code: ${it.statusCode}")
                throw Exception("Failed to delete snippet. Status code: ${it.statusCode}")
            }
            snippetPermissionApi.deleteSnippetPermissions(snippetId, token).takeIf {
                it.statusCode.isError
            }?.let {
                logger.info("Failed to delete snippet permissions. Status code: ${it.statusCode}")
                throw Exception("Failed to delete snippet permissions. Status code: ${it.statusCode}")
            }

            snippetRepository.deleteSnippetById(snippetId)
            logger.info("Snippet deleted successfully")
            return "Snippet deleted successfully"
        }

        private fun getUsernameFromUserId(userId: String): String {
            logger.info("Attempting to get username for user with id $userId")
            return userService.getUsernameById(userId)
        }

        fun updateSnippet(
            snippetId: String,
            snippetUpdateInput: SnippetContent,
            token: String,
        ): GetSnippetOutput {
            logger.info("Received request to update snippet with id $snippetId")
            val snippet = this.snippetRepository.findSnippetById(snippetId) ?: throw SnippetNotFoundException()

            logger.info("Attempting to delete snippet with id $snippetId from asset store")
            assetStoreApi.deleteSnippet(snippet.snippetKey)
            logger.info("Attempting to update snippet with id $snippetId in asset store")
            assetStoreApi.updateSnippet(snippet.snippetKey, snippetUpdateInput.content)

            logger.info("Attempting to get author for snippet with id $snippetId")
            val authorResponse = snippetPermissionApi.getAuthorBySnippetId(snippetId, token)

            if (authorResponse.statusCode.isError) {
                logger.error("Failed to get author for snippet $snippetId. Status code: ${authorResponse.statusCode}")
                throw Exception(
                    "Request to permission service was unsuccessful. " +
                        "Reason: {status: ${authorResponse.statusCode}: ${authorResponse.body}}",
                )
            }
            val username = getUsernameFromUserId(authorResponse.body!!)

            logger.info("Returning updated snippet output for snippet with id $snippetId")
            return GetSnippetOutput(
                id = snippetId,
                name = snippet.name,
                content = snippetUpdateInput.content,
                language = snippet.language,
                author = username,
            )
        }

        fun updateAllUserSnippetsStatus(
            userId: String,
            newStatus: SnippetStatus,
        ): List<SnippetUserStatus> {
            logger.info("Received request to update all snippets status for user with id $userId")
            val userSnippetStatuses = this.snippetUserStatusRepository.findAllByUserId(userId)

            for (snippetStatus in userSnippetStatuses) {
                snippetStatus.status = newStatus
            }
            logger.info("Updating all snippets status for user with id $userId")
            return snippetUserStatusRepository.saveAll(userSnippetStatuses)
        }

        @Transactional
        fun updateUserSnippetStatusBySnippetKey(
            userId: String,
            snippetKey: String,
            status: SnippetStatus,
        ) {
            logger.info("Received request to update snippet status for user with id $userId and snippet key $snippetKey")
            snippetUserStatusRepository.updateByUserIdAndSnippetKey(userId, snippetKey, status)
        }
    }
