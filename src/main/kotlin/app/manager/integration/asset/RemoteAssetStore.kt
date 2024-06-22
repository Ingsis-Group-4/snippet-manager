package app.manager.integration.asset

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

class RemoteAssetStore(val rest: RestTemplate, val bucketUrl: String) : AssetStoreApi {
    override fun createSnippetInBucket(
        snippetKey: String,
        content: String,
    ): ResponseEntity<String> {
        val bucketURL = "$bucketUrl/$snippetKey"
        val headers = getJsonHeader()
        val bucketRequestEntity = HttpEntity(content, headers)

        return this.rest.postForEntity(bucketURL, bucketRequestEntity, String::class.java)
    }

    override fun getSnippet(snippetKey: String): ResponseEntity<String> {
        val bucketURL = "$bucketUrl/$snippetKey"
        return this.rest.getForEntity(bucketURL, String::class.java)
    }

    override fun deleteSnippet(snippetKey: String): ResponseEntity<String> {
        val bucketURL = "$bucketUrl/$snippetKey"
        try {
            rest.delete(bucketURL)
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
