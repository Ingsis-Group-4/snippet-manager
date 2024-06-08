package app.manager

import app.manager.integration.asset.AssetStoreApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("test")
class AssetStoreMockConfiguration {
    @Bean
    @Primary
    fun createApi(): AssetStoreApi {
        return MockAssetStore()
    }
}
