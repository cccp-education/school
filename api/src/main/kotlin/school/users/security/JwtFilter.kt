package school.users.security

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
class JwtFilter(private val security: Security) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        resolveToken(exchange.request).apply token@{
            chain.apply {
                return when {
                    !isNullOrBlank() &&
                            security.validateToken(this@token) -> filter(exchange)
                        .contextWrite(withAuthentication(security.getAuthentication(this@token)))

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
/*
package community.security


import community.*
import community.Properties
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts.builder
import io.jsonwebtoken.Jwts.parserBuilder
import io.jsonwebtoken.SignatureAlgorithm.HS512
import io.jsonwebtoken.io.Decoders.BASE64
import io.jsonwebtoken.jackson.io.JacksonSerializer
import io.jsonwebtoken.lang.Strings.hasLength
import io.jsonwebtoken.security.Keys.hmacShaKeyFor
import org.springframework.beans.factory.InitializingBean
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.base.Authentication
import org.springframework.security.base.authority.SimpleGrantedAuthority
import org.springframework.security.base.context.ReactiveSecurityContextHolder
import org.springframework.security.base.userdetails.User
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.base.publisher.Mono
import java.security.Key
import java.util.*
import kotlin.text.Charsets.UTF_8

@Component
class SecurityJwt(
    private val properties: Properties,
    private var key: Key? = null,
    private var tokenValidityInMilliseconds: Long = 0,
    private var tokenValidityInMillisecondsForRememberMe: Long = 0
) : InitializingBean, WebFilter {


    @Throws(Exception::class)
    override fun afterPropertiesSet() {
        properties
            .security
            .authentication
            .jwt
            .secret
            .run {
                key = hmacShaKeyFor(
                    when {
                        !hasLength(this) -> w(
                            "Warning: the Jwt key used is not Base64-encoded. " +
                                    "We recommend using the `community.accounts.security.authentication.jwt.base64-secret`" +
                                    " key for optimum security."
                        ).run { toByteArray(UTF_8) }

                        else -> d("Using a Base64-encoded Jwt secret key").run {
                            BASE64.decode(
                                properties
                                    .security
                                    .authentication
                                    .jwt
                                    .base64Secret
                            )
                        }
                    }
                )
            }
        tokenValidityInMilliseconds = properties
            .security
            .authentication
            .jwt
            .tokenValidityInSeconds * 1000
        tokenValidityInMillisecondsForRememberMe = properties
            .security
            .authentication
            .jwt
            .tokenValidityInSecondsForRememberMe * 1000
    }

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        resolveToken(exchange.request).apply token@{
            chain.apply {
                return when {
                    !isNullOrBlank() && validateToken(this@token) ->
                        filter(exchange)
                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication(this@token)))

                    else -> filter(exchange)
                }
            }
        }
    }

    suspend fun createToken(
        authentication: Authentication,
        rememberMe: Boolean
    ): String {
        Date().time.run {
            return builder()
                .setSubject(authentication.name)
                .claim(
                    AUTHORITIES_KEY,
                    authentication.authorities
                        .asSequence()
                        .map { it.authority }
                        .joinToString(separator = ","))
                .signWith(key, HS512)
                .setExpiration(
                    when {
                        rememberMe -> Date(this + tokenValidityInMillisecondsForRememberMe)
                        else -> Date(this + tokenValidityInMilliseconds)
                    }
                )
                .serializeToJsonWith(JacksonSerializer())
                .compact()
        }
    }

    private fun authentication(token: String): Authentication {
        parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body
            .apply {
                this[AUTHORITIES_KEY]
                    .toString()
                    .splitToSequence(",")
                    .mapTo(mutableListOf()) { SimpleGrantedAuthority(it) }
                    .apply authorities@{
                        return@authentication UsernamePasswordAuthenticationToken(
                            User(subject, "", this@authorities),
                            token,
                            this@authorities
                        )
                    }
            }
    }

    private fun validateToken(token: String): Boolean = try {
        parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
        VALID_TOKEN
    } catch (e: JwtException) {
        i("Invalid Jwt token.")
        t("Invalid Jwt token trace. $e")
        INVALID_TOKEN
    } catch (e: IllegalArgumentException) {
        i("Invalid Jwt token.")
        t("Invalid Jwt token trace. $e")
        INVALID_TOKEN
    }


    private fun resolveToken(request: ServerHttpRequest): String? = request
        .headers
        .getFirst(AUTHORIZATION_HEADER)
        .apply {
            return when {
                !isNullOrBlank() && startsWith(BEARER_START_WITH) ->
                    substring(startIndex = 7)

                else -> null
            }
        }
}
 */