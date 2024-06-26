package app.common.integration.runner

import app.cases.model.dto.TestCaseEnvDto
import app.logs.CorrelationIdFilter.Companion.CORRELATION_ID_KEY
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import org.springframework.web.client.postForEntity

class RemoteSnippetRunnerApi(
    private val snippetRunnerUrl: String,
    private val restTemplate: RestTemplate,
) : SnippetRunnerApi {
    private val logger = LoggerFactory.getLogger(RemoteSnippetRunnerApi::class.java)

    override fun runSnippet(
        content: String,
        inputs: List<String>,
        envs: List<TestCaseEnvDto>,
        token: String,
    ): RunOutput {
        logger.info("Running snippet with content: $content")
        val url = "$snippetRunnerUrl/execute/interpret"
        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                set("Authorization", "Bearer $token")
                set("X-Correlation-Id", MDC.get(CORRELATION_ID_KEY))
            }
        val body = RunInput(content, inputs, envs)

        val response = this.restTemplate.postForEntity<RunOutput>(url, HttpEntity(body, headers))

        if (!response.statusCode.is2xxSuccessful) {
            logger.error("Request to url: '$url' was unsuccessful. Reason: {status: ${response.statusCode}}")
            throw Exception("Request to url: '$url' was unsuccessful. Reason: {status: ${response.statusCode}}")
        }

        return response.body!!
    }

    override fun formatSnippet(
        content: String,
        ruleConfig: String,
        token: String,
    ): String {
        logger.info("Formatting snippet with content: $content")
        val url = "$snippetRunnerUrl/execute/format"
        val requestBody = FormatSnippetInput(content, ruleConfig)
        val headers = getJsonHeader(token)
        val response = this.restTemplate.exchange<String>(url, HttpMethod.POST, HttpEntity(requestBody, headers))

        if (!response.statusCode.is2xxSuccessful) {
            logger.error("Request to url: '$url' was unsuccessful. Reason: {status: ${response.statusCode}: ${response.body}}")
            throw Exception("Request to url: '$url' was unsuccessful. Reason: {status: ${response.statusCode}: ${response.body}}")
        }

        return response.body!!
    }

    private fun getJsonHeader(token: String): HttpHeaders {
        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                set("Authorization", "Bearer $token")
                set("X-Correlation-Id", MDC.get(CORRELATION_ID_KEY))
            }
        return headers
    }
}

class RunInput(val content: String, val inputs: List<String>, val envs: List<TestCaseEnvDto>)
