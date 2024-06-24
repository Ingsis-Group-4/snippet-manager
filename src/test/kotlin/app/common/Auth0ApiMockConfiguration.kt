package app.common

import app.common.integration.auth0.Auth0Api
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Component
class Auth0ApiMockConfiguration {
    @Bean
    @Primary
    fun createMockAuth0Api(): Auth0Api {
        return MockAuth0Api()
    }
}
