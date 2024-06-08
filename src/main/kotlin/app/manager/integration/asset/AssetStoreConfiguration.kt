package app.manager.integration.asset

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class AssetStoreConfiguration
    @Autowired
    constructor(
        private val rest: RestTemplate,
        @Value("\${azuriteBucket}")
        private var azuriteBucketUrlV1: String,
    ) {
        @Bean
        fun createRemoteBucketApi(): AssetStoreApi {
            return RemoteAssetStore(rest, azuriteBucketUrlV1)
        }
    }
