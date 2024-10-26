@file:Suppress("unused")

package school.users.security

import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.http.HttpMethod.OPTIONS
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers
import school.base.utils.ROLE_ANONYMOUS
import school.users.security.Security.Companion.negated
import java.security.SecureRandom


object SecurityUtils {

    private const val DEF_COUNT = 20
    private val SECURE_RANDOM: SecureRandom by lazy {
        SecureRandom().apply { nextBytes(ByteArray(size = 64)) }
    }
    private val generateRandomAlphanumericString: String
        get() = RandomStringUtils.random(
            DEF_COUNT,
            0,
            0,
            true,
            true,
            null,
            SECURE_RANDOM
        )

    val generatePassword: String
        get() = generateRandomAlphanumericString

    val generateActivationKey: String
        get() = generateRandomAlphanumericString

    val generateResetKey: String
        get() = generateRandomAlphanumericString

    val String.objectName get() = replaceFirst(first(), first().lowercaseChar())

    private fun extractPrincipal(authentication: Authentication?) =
        when (authentication) {
            null -> ""
            else -> when (val principal = authentication.principal) {
                is UserDetails -> principal.username
                is String -> principal
                else -> ""
            }
        }

    suspend fun getCurrentUserLogin() = extractPrincipal(
        getContext().awaitSingle().authentication
    )!!

    suspend fun getCurrentUserJwt() = getContext()
        .map(SecurityContext::getAuthentication)
        .filter { it.credentials is String }
        .map { it.credentials as String }
        .awaitSingle()!!

    suspend fun isAuthenticated(): Boolean = getContext()
        .map(SecurityContext::getAuthentication)
        .map(Authentication::getAuthorities)
        .map { roles: Collection<GrantedAuthority> ->
            roles.map(GrantedAuthority::getAuthority)
                .none { it == ROLE_ANONYMOUS }
        }.awaitSingleOrNull()!!


    suspend fun isCurrentUserInRole(authority: String) = getContext()
        .map(SecurityContext::getAuthentication)
        .map(Authentication::getAuthorities)
        .map { roles: Collection<GrantedAuthority> ->
            roles.map(GrantedAuthority::getAuthority)
                .any { it == authority }
        }.awaitSingle()!!


    private val exchangeMatcher: NegatedServerWebExchangeMatcher
        get() = NegatedServerWebExchangeMatcher(
            OrServerWebExchangeMatcher(
                pathMatchers(*negated),
                pathMatchers(OPTIONS, "/**")
            )
        )

//    fun ServerHttpSecurity.securityWebFilterChain(
//        security: Security,
//        spaFilter: SpaFilter,
//        authenticationManager: ReactiveAuthenticationManager
//    ): SecurityWebFilterChain? = securityMatcher(exchangeMatcher)
//        .csrf()
//        .disable()
//        .addFilterAt(spaFilter, AUTHENTICATION)
//        .addFilterAt(security, HTTP_BASIC)
//        .authenticationManager(authenticationManager)
//        .exceptionHandling()
//        .and()
//        .headers()
//        .contentSecurityPolicy(CONTENT_SECURITY_POLICY)
//        .and()
//        .referrerPolicy(STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
//        .and()
//        .permissionsPolicy()
//        .policy(FEATURE_POLICY)
//        .and()
//        .frameOptions()
//        .disable()
//        .and()
//        .authorizeExchange()
//        .pathMatchers(*permitAll)
//        .permitAll()
//        .pathMatchers(*authenticated)
//        .authenticated()
//        .pathMatchers(*adminAuthority)
//        .hasAuthority(ROLE_ADMIN)
//        .and()
//        .build()
}