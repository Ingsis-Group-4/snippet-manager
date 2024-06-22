package app.auth

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration

@Configuration
class CorsConfig {
    @Bean
    fun corsFilter(): CorsConfiguration {
        val config = CorsConfiguration()
        config.allowCredentials = false
        config.addAllowedOrigin("*")
        config.addAllowedHeader("*")
        config.addAllowedMethod("*")
        return config
    }
}
