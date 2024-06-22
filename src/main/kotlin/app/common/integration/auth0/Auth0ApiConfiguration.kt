package app.common.integration.auth0

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class Auth0ApiConfiguration
    @Autowired
    constructor(
        private val restTemplate: RestTemplate,
        @Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
        private val auth0url: String,
        @Value("\${auth0.managing.token}")
        private val managingToken: String,
    ) {
        @Bean
        fun createAuth0Api(): Auth0Api {
            return RemoteAuth0Api(auth0url, restTemplate, managingToken)
        }
    }
