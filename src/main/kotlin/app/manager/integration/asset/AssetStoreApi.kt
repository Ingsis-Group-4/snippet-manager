package app.manager.integration.asset

import org.springframework.http.ResponseEntity

interface AssetStoreApi {
    fun createSnippetInBucket(
        snippetKey: String,
        content: String,
    ): ResponseEntity<String>

    fun getSnippet(snippetKey: String): ResponseEntity<String>

    fun deleteSnippet(snippetKey: String): ResponseEntity<String>

    fun updateSnippet(
        snippetKey: String,
        newContent: String,
    ): ResponseEntity<String>
}
