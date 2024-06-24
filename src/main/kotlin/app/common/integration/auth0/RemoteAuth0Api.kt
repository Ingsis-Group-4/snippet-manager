package app.common.integration.auth0

import app.user.User
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange

class RemoteAuth0Api(
    private val auth0Url: String,
    private val restTemplate: RestTemplate,
    private val managingToken: String,
) : Auth0Api {
    override fun getAllUsers(): List<User> {
        val url = "$auth0Url/api/v2/users"
        val headers = getJsonHeader()
        val entity: HttpEntity<Void> = HttpEntity(headers)

        val response = this.restTemplate.exchange<List<User>>(url, HttpMethod.GET, entity)

        if (!response.statusCode.is2xxSuccessful) {
            throw Exception("Request to url: '$url' was unsuccessful. Reason: {status: ${response.statusCode}}")
        }

        return response.body!!
    }

    override fun getUserById(userId: String): User {
        val url = "$auth0Url/api/v2/users/$userId"
        val headers = getJsonHeader()
        val entity: HttpEntity<Void> = HttpEntity(headers)

        val response = this.restTemplate.exchange<User>(url, HttpMethod.GET, entity)

        if (!response.statusCode.is2xxSuccessful) {
            throw Exception("Request to url: '$url' was unsuccessful. Reason: {status: ${response.statusCode}}")
        }
        return response.body!!
    }

    private fun getJsonHeader(): HttpHeaders {
        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                set("Authorization", "Bearer $managingToken")
            }
        return headers
    }
}
