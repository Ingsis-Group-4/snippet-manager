package app.manager.service

import app.manager.model.dto.CreateSnippetInput
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.UUID

@Service
class ManagerService {
    private lateinit var azuriteBucketUrlV1: String

    fun createSnippet(input: CreateSnippetInput): String {
        val bucketBodyInfo = input.content
        val snippetKey = UUID.randomUUID().toString()
        val bucketURL = "http://localhost:8080/v1/asset/snippet/$snippetKey"
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val requestEntity = HttpEntity(bucketBodyInfo, headers)

        val restTemplate = RestTemplate()
        val responseEntity: ResponseEntity<String> = restTemplate.postForEntity(bucketURL, requestEntity, String::class.java)

        if (responseEntity.statusCode.is2xxSuccessful) {
            return "Snippet created successfully"
        } else {
            return "Failed to create snippet. Status code: ${responseEntity.statusCode}"
        }
    }
}
