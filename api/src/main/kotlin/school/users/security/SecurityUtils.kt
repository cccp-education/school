@file:Suppress("unused")

package school.users.security

import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.userdetails.UserDetails
import school.base.utils.ROLE_ANONYMOUS


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

///*
//@file:Suppress("unused", "DEPRECATION", "removal")
//
//package community.security
//
//import community.CONTENT_SECURITY_POLICY
//import community.Configuration.SpaFilter
//import community.FEATURE_POLICY
//import community.ROLE_ADMIN
//import community.ROLE_ANONYMOUS
//import community.security.Security.Companion.adminAuthority
//import community.security.Security.Companion.authenticated
//import community.security.Security.Companion.negated
//import community.security.Security.Companion.permitAll
//import kotlinx.coroutines.reactive.awaitSingle
//import org.apache.commons.lang3.RandomStringUtils.random
//import org.springframework.http.HttpMethod.OPTIONS
//import org.springframework.security.authentication.ReactiveAuthenticationManager
//import org.springframework.security.config.web.server.SecurityWebFiltersOrder.AUTHENTICATION
//import org.springframework.security.config.web.server.SecurityWebFiltersOrder.HTTP_BASIC
//import org.springframework.security.config.web.server.ServerHttpSecurity
//import org.springframework.security.base.Authentication
//import org.springframework.security.base.GrantedAuthority
//import org.springframework.security.base.context.ReactiveSecurityContextHolder.getContext
//import org.springframework.security.base.context.SecurityContext
//import org.springframework.security.base.userdetails.UserDetails
//import org.springframework.security.web.server.SecurityWebFilterChain
//import org.springframework.security.web.server.header.ReferrerPolicyServerHttpHeadersWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN
//import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher
//import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher
//import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers
//import java.security.SecureRandom
//
//
//private fun extractPrincipal(authentication: Authentication?): String =
//    when (authentication) {
//        null -> ""
//        else -> when (val principal = authentication.principal) {
//            is UserDetails -> principal.username
//            is String -> principal
//            else -> ""
//        }
//    }
//
//suspend fun currentUserLogin(): String = extractPrincipal(
//    getContext().awaitSingle().authentication
//)
//
//suspend fun currentUserJwt(): String = getContext()
//    .map(SecurityContext::getAuthentication)
//    .filter { it.credentials is String }
//    .map { it.credentials as String }
//    .awaitSingle()!!
//
//suspend fun isAuthenticated(): Boolean = getContext()
//    .map(SecurityContext::getAuthentication)
//    .map(Authentication::getAuthorities)
//    .map { roles: Collection<GrantedAuthority> ->
//        roles.map(GrantedAuthority::getAuthority)
//            .none { it == ROLE_ANONYMOUS }
//    }.awaitSingle()!!
//
//
//suspend fun isCurrentUserInRole(authority: String): Boolean = getContext()
//    .map(SecurityContext::getAuthentication)
//    .map(Authentication::getAuthorities)
//    .map { roles: Collection<GrantedAuthority> ->
//        roles.map(GrantedAuthority::getAuthority)
//            .any { it == authority }
//    }.awaitSingle()!!
//
//
//private const val DEF_COUNT: Int = 20
//private val SECURE_RANDOM: SecureRandom by lazy {
//    SecureRandom().apply { nextBytes(ByteArray(size = 64)) }
//}
//private val generateRandomAlphanumericString: String
//    get() = random(
//        /* count = */ DEF_COUNT,
//        /* start = */ 0,
//        /* end = */ 0,
//        /* letters = */ true,
//        /* numbers = */ true,
//        /* chars = */ null,
//        /* random = */ SECURE_RANDOM
//    )
//
//val generatePassword: String get() = generateRandomAlphanumericString
//
//val generateActivationKey: String get() = generateRandomAlphanumericString
//
//val generateResetKey: String get() = generateRandomAlphanumericString
//
//private val exchangeMatcher: NegatedServerWebExchangeMatcher
//    get() = NegatedServerWebExchangeMatcher(
//        OrServerWebExchangeMatcher(
//            pathMatchers(*negated),
//            pathMatchers(OPTIONS, "/**")
//        )
//    )
//
//fun ServerHttpSecurity.securityWebFilterChain(
//    security: SecurityJwt,
//    spaFilter: SpaFilter,
//    authenticationManager: ReactiveAuthenticationManager
//): SecurityWebFilterChain? = securityMatcher(exchangeMatcher)
//    .csrf()
//    .disable()
//    .addFilterAt(spaFilter, AUTHENTICATION)
//    .addFilterAt(security, HTTP_BASIC)
//    .authenticationManager(authenticationManager)
//    .exceptionHandling()
//    .and()
//    .headers()
//    .contentSecurityPolicy(CONTENT_SECURITY_POLICY)
//    .and()
//    .referrerPolicy(STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
//    .and()
//    .permissionsPolicy()
//    .policy(FEATURE_POLICY)
//    .and()
//    .frameOptions()
//    .disable()
//    .and()
//    .authorizeExchange()
//    .pathMatchers(*permitAll)
//    .permitAll()
//    .pathMatchers(*authenticated)
//    .authenticated()
//    .pathMatchers(*adminAuthority)
//    .hasAuthority(ROLE_ADMIN)
//    .and()
//    .build()
//
//
//
// */