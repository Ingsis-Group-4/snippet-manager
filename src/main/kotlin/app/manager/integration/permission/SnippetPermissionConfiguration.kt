package app.manager.integration.permission

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class SnippetPermissionConfiguration
    @Autowired
    constructor(
        private val rest: RestTemplate,
        @Value("\${permissionsService}")
        private var permissionsServiceUrl: String,
        private var objectMapper: ObjectMapper,
    ) {
        @Bean
        fun createRemotePermissionApi(): SnippetPermissonApi {
            return RemoteSnippetPermission(rest, permissionsServiceUrl, objectMapper)
        }
    }
