
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory

@Configuration
@Profile("!test")
class ConnectionFactory(
    @Value("\${redis.host}") private val hostName: String,
    @Value("\${redis.port}") private val port: Int,
) {

    private val logger = LoggerFactory.getLogger(ConnectionFactory::class.java)

    @Bean
    fun redisConnectionFactory(): LettuceConnectionFactory {
        logger.info("Connection to redis on host: $hostName and port $port")
        return LettuceConnectionFactory(
            RedisStandaloneConfiguration(hostName, port),
        )
    }
}
