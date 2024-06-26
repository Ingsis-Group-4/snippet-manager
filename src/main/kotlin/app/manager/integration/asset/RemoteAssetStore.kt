package app.manager.integration.asset

import app.logs.CorrelationIdFilter.Companion.CORRELATION_ID_KEY
import org.slf4j.MDC
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity

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
        val correlationId = MDC.get(CORRELATION_ID_KEY)
        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                set("X-Correlation-Id", correlationId)
            }
        return headers
    }

    override fun updateSnippet(
        snippetKey: String,
        newContent: String,
    ): ResponseEntity<String> {
        val bucketUrl = "$bucketUrl/$snippetKey"

        return this.rest.postForEntity(bucketUrl, newContent)
    }
}
