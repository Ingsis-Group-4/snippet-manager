package app.redis.consumer

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.stream.StreamReceiver
import org.springframework.stereotype.Component
import spring.mvc.redis.events.LintResultEvent
import spring.mvc.redis.streams.RedisStreamConsumer

@Component
@Profile("test")
class LintResultConsumer
    @Autowired
    constructor(
        redis: RedisTemplate<String, String>,
        @Value("\${redis.stream.result_lint_key}") streamKey: String,
        @Value("\${redis.groups.lint}") groupId: String,
    ) : RedisStreamConsumer<LintResultEvent>(streamKey, groupId, redis) {
        override fun onMessage(record: ObjectRecord<String, LintResultEvent>) {}

        override fun options(): StreamReceiver.StreamReceiverOptions<String, ObjectRecord<String, LintResultEvent>> {
            TODO("Not yet implemented")
        }
    }
