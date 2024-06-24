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
import app.user.UserService
import jakarta.transaction.Transactional
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
    ) {
        @Transactional
        fun createSnippet(
            input: CreateSnippetInput,
            userId: String,
            token: String,
        ): GetSnippetOutput {
            val snippetKey = UUID.randomUUID().toString()

            val snippet = snippetPersistence(input, snippetKey)

            val bucketResponseEntity = assetStoreApi.createSnippetInBucket(snippetKey, input.content)
            if (bucketResponseEntity.statusCode.is2xxSuccessful) {
                try {
                    createPermissionsForSnippet(snippet.id!!, userId, token)
                } catch (e: Exception) {
                    throw Exception(e.message)
                }
                val username = getUsernameFromUserId(userId)
                return GetSnippetOutput(
                    name = snippet.name,
                    id = snippet.id,
                    language = snippet.language,
                    author = username,
                    content = input.content,
                )
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
                return "Snippet created successfully. Snippet id: $snippetId"
            } else {
                throw Exception("Failed to create permissions for snippet $snippetId. Status code: ${permissionResponseEntity.statusCode}")
            }
        }

        fun getSnippet(
            snippetId: String,
            token: String,
        ): GetSnippetOutput {
            val snippet = snippetRepository.findSnippetById(snippetId) ?: throw SnippetNotFoundException()
            val snippetKey = snippet.snippetKey

            val bucketResponseEntity = assetStoreApi.getSnippet(snippetKey)

            val authorResponse = snippetPermissionApi.getAuthorBySnippetId(snippetId, token)
            if (authorResponse.statusCode.isError) {
                throw NotFoundException("Failed to get author for snippet $snippetId. Status code: ${authorResponse.statusCode}")
            }
            val author = authorResponse.body!!
            val username = getUsernameFromUserId(author)

            if (bucketResponseEntity.statusCode.is2xxSuccessful) {
                val content = bucketResponseEntity.body!!
                return GetSnippetOutput(
                    id = snippetId,
                    name = snippet.name,
                    content = content,
                    language = snippet.language,
                    author = username,
                )
            } else {
                throw NotFoundException("Failed to get snippet. Status code: ${bucketResponseEntity.statusCode}")
            }
        }

        fun getSnippetsFromUserId(
            userId: String,
            token: String,
        ): List<GetSnippetOutput> {
            val permissionResponseEntity = snippetPermissionApi.getAllSnippetsPermission(userId, token)

            if (!permissionResponseEntity.statusCode.is2xxSuccessful) {
                throw Exception("Failed to get snippets for user $userId.")
            }

            return permissionResponseEntity.body!!.map { permissionSnippet ->
                val snippet =
                    snippetRepository.findSnippetById(permissionSnippet.snippetId)
                        ?: throw SnippetNotFoundException()

                val contentResponse = assetStoreApi.getSnippet(snippet.snippetKey)
                if (!contentResponse.statusCode.is2xxSuccessful) {
                    throw Exception("Failed to get snippet content for snippet ${snippet.id}. Status code: ${contentResponse.statusCode}")
                }

                GetSnippetOutput(
                    id = snippet.id!!,
                    name = snippet.name,
                    language = snippet.language,
                    author = getUsernameFromUserId(permissionSnippet.authorId),
                    content = contentResponse.body!!,
                )
            }
        }

        fun shareSnippet(
            input: ShareSnippetInput,
            token: String,
        ): String {
            snippetRepository.findSnippetById(input.snippetId) ?: throw SnippetNotFoundException()
            val permissionBodyInput =
                PermissionCreateSnippetInput(
                    snippetId = input.snippetId,
                    userId = input.userId,
                    permissionType = "SHARED",
                )
            val permissionResponseEntity =
                snippetPermissionApi.createSnippetPermission(permissionBodyInput, token)

            if (permissionResponseEntity.statusCode.is2xxSuccessful) {
                return "Snippet shared successfully"
            } else {
                throw Exception("Failed to share snippet ${input.snippetId}. Status code: ${permissionResponseEntity.statusCode}")
            }
        }

        @Transactional
        fun deleteSnippet(
            snippetId: String,
            token: String,
        ): String {
            val snippetKey = snippetRepository.findSnippetById(snippetId)?.snippetKey ?: throw SnippetNotFoundException()

            assetStoreApi.deleteSnippet(snippetKey).takeIf {
                it.statusCode.isError
            }?.let { throw Exception("Failed to delete snippet. Status code: ${it.statusCode}") }
            snippetPermissionApi.deleteSnippetPermissions(snippetId, token).takeIf {
                it.statusCode.isError
            }?.let { throw Exception("Failed to delete snippet permissions. Status code: ${it.statusCode}") }

            snippetRepository.deleteSnippetById(snippetId)

            return "Snippet deleted successfully"
        }

        private fun getUsernameFromUserId(userId: String): String {
            return userService.getUsernameById(userId)
        }

        fun updateSnippet(
            snippetId: String,
            snippetUpdateInput: SnippetContent,
            token: String,
        ): GetSnippetOutput {
            val snippet = this.snippetRepository.findSnippetById(snippetId) ?: throw SnippetNotFoundException()

            assetStoreApi.deleteSnippet(snippet.snippetKey)
            assetStoreApi.updateSnippet(snippet.snippetKey, snippetUpdateInput.content)

            val authorResponse = snippetPermissionApi.getAuthorBySnippetId(snippetId, token)

            if (authorResponse.statusCode.isError) {
                throw Exception(
                    "Request to permission service was unsuccessful. " +
                        "Reason: {status: ${authorResponse.statusCode}: ${authorResponse.body}}",
                )
            }
            val username = getUsernameFromUserId(authorResponse.body!!)

            return GetSnippetOutput(
                id = snippetId,
                name = snippet.name,
                content = snippetUpdateInput.content,
                language = snippet.language,
                author = username,
            )
        }
    }
