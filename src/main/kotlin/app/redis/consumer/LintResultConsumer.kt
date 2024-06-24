package app.redis.consumer

import app.manager.model.enums.SnippetStatus
import app.manager.service.ManagerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.stream.StreamReceiver
import org.springframework.stereotype.Component
import spring.mvc.redis.events.LintResultEvent
import spring.mvc.redis.events.LintStatus
import spring.mvc.redis.streams.RedisStreamConsumer

@Component
@Profile("!test")
class LintResultConsumer
    @Autowired
    constructor(
        redis: RedisTemplate<String, String>,
        @Value("\${redis.stream.result_lint_key}") streamKey: String,
        @Value("\${redis.groups.lint}") groupId: String,
        private val managerService: ManagerService,
    ) : RedisStreamConsumer<LintResultEvent>(streamKey, groupId, redis) {
        init {
            subscription()
        }

        override fun onMessage(record: ObjectRecord<String, LintResultEvent>) {
            println(
                "Received record: " +
                    "LintResultEvent(" +
                    "userId: ${record.value.userId}, " +
                    "snippetKey: ${record.value.snippetKey}, " +
                    "status: ${record.value.status}" +
                    ")",
            )

            val eventPayload = record.value

            val newStatus = toSnippetStatus(eventPayload.status)

            managerService.updateUserSnippetStatusBySnippetKey(eventPayload.userId, eventPayload.snippetKey, newStatus)

            println(
                "Finished processing record: " +
                    "LintResultEvent(" +
                    "userId: ${record.value.userId}, " +
                    "snippetKey: ${record.value.snippetKey}, " +
                    "status: ${record.value.status}" +
                    ")",
            )
        }

        override fun options(): StreamReceiver.StreamReceiverOptions<String, ObjectRecord<String, LintResultEvent>> {
            return StreamReceiver.StreamReceiverOptions.builder()
                .pollTimeout(java.time.Duration.ofMillis(10000)) // Set poll rate
                .targetType(LintResultEvent::class.java) // Set type to de-serialize record
                .build()
        }

        private fun toSnippetStatus(status: LintStatus): SnippetStatus {
            return when (status) {
                LintStatus.PASSED -> SnippetStatus.COMPLIANT
                LintStatus.FAILED -> SnippetStatus.NOT_COMPLIANT
                LintStatus.PENDING -> SnippetStatus.PENDING
            }
        }
    }
