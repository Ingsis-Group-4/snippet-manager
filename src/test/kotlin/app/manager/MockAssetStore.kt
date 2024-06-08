package app.manager

import app.manager.integration.asset.AssetStoreApi
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

class MockAssetStore(private val contentMap: MutableMap<String, String> = mutableMapOf()) : AssetStoreApi {
    override fun createSnippetInBucket(
        snippetKey: String,
        content: String,
    ): ResponseEntity<String> {
        contentMap[snippetKey] = content
        return ResponseEntity.ok().build()
    }

    override fun getSnippet(snippetKey: String): ResponseEntity<String> {
        val content = contentMap[snippetKey]
        return if (content != null) {
            ResponseEntity.ok(content)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    override fun deleteSnippet(snippetKey: String): ResponseEntity<String> {
        if (getSnippet(snippetKey).statusCode == HttpStatus.NOT_FOUND) return ResponseEntity.notFound().build()
        contentMap.remove(snippetKey)
        return ResponseEntity.ok().build()
    }
}
