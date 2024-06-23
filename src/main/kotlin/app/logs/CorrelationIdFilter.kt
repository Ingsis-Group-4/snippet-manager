package app.logs

import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.util.context.Context
import java.util.UUID

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class CorrelationIdFilter : WebFilter {
    companion object {
        private const val CORRELATION_ID_KEY = "correlation-id"
        private const val CORRELATION_ID_HEADER = "X-Correlation-Id"
    }

    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain,
    ): Mono<Void> {
        val correlationId = exchange.request.headers[CORRELATION_ID_HEADER]?.firstOrNull() ?: generateCorrelationId()
        MDC.put(CORRELATION_ID_KEY, correlationId)
        return chain.filter(exchange).contextWrite { context: Context ->
            context.put(CORRELATION_ID_KEY, correlationId)
        }.doFinally {
            MDC.remove(CORRELATION_ID_KEY)
        }
    }

    private fun generateCorrelationId(): String {
        return UUID.randomUUID().toString()
    }
}
