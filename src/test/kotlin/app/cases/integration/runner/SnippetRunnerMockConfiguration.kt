package app.cases.integration.runner

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("test")
class SnippetRunnerMockConfiguration {
    @Bean
    @Primary
    fun createRunnerApi(): SnippetRunnerApi {
        return SnippetRunnerApiMock()
    }
}
