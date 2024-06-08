package app.manager

import app.manager.integration.permission.SnippetPermissonApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("test")
class SnippetPermissionMockConfiguration {
    @Bean
    @Primary
    fun createPermissionApi(): SnippetPermissonApi {
        return MockSnippetPermission()
    }
}
