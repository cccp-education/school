package school.users.security

import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.core.context.ReactiveSecurityContextHolder.withAuthentication
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import school.base.utils.AUTHORIZATION_HEADER
import school.base.utils.BEARER_START_WITH

@Component("jwtFilter")
class JwtFilter(private val context: ApplicationContext) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        resolveToken(exchange.request).apply token@{
            chain.apply {
                return when {
                    !isNullOrBlank() &&
                            this@token.run(context.getBean<SecurityManager>()::validateToken) -> exchange.run(::filter)
                        .contextWrite(
                            withAuthentication(
                                context.getBean<SecurityManager>().getAuthentication(this@token)
                            )
                        )

                    else -> filter(exchange)
                }
            }
        }
    }

    private fun resolveToken(request: ServerHttpRequest): String? = request
        .headers
        .getFirst(AUTHORIZATION_HEADER)
        .apply {
            return if (
                !isNullOrBlank() &&
                startsWith(BEARER_START_WITH)
            ) substring(startIndex = 7)
            else null
        }
}