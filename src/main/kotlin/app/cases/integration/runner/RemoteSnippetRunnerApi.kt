package app.cases.integration.runner

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity

class RemoteSnippetRunnerApi(
    private val snippetRunnerUrl: String,
    private val restTemplate: RestTemplate,
) : SnippetRunnerApi {
    override fun runSnippet(
        content: String,
        inputs: List<String>,
        token: String,
    ): RunOutput {
        val url = "$snippetRunnerUrl/execute/interpret"
        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                set("Authorization", "Bearer $token")
            }
        val response = this.restTemplate.postForEntity<RunOutput>(url, HttpEntity(content, headers))

        if (!response.statusCode.is2xxSuccessful) {
            throw Exception("Request to url: '$url' was unsuccessful. Reason: {status: ${response.statusCode}}")
        }

        return response.body!!
    }
}
