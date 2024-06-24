package app.redis.producer

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import spring.mvc.redis.events.LintRequestEvent
import spring.mvc.redis.streams.RedisStreamProducer

@Component
@Profile("test")
class LintRequestProducer
    @Autowired
    constructor(
        @Value("\${redis.stream.request_lint_key}") streamKey: String,
        redis: RedisTemplate<String, String>,
    ) : RedisStreamProducer(streamKey, redis) {
        suspend fun publishEvent(event: LintRequestEvent) {
        }
    }
