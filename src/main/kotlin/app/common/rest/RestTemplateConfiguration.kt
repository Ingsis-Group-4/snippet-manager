package app.common.rest

import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class RestTemplateConfiguration {
    @Bean
    fun createRestTemplate(): RestTemplate {
        return RestTemplate()
    }
}
