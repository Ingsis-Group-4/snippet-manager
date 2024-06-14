package app.cases.integration.runner

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class SnippetRunnerConfiguration
    @Autowired
    constructor(
        private val restTemplate: RestTemplate,
        @Value("\${snippet.runner.url}")
        private val snippetRunnerUrl: String,
    ) {
        @Bean
        fun createApi(): SnippetRunnerApi {
            return RemoteSnippetRunnerApi(snippetRunnerUrl, restTemplate)
        }
    }
