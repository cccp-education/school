package users.security

import jakarta.validation.Validator
import kotlinx.coroutines.reactor.mono
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.header.ContentSecurityPolicyServerHttpHeadersWriter
import org.springframework.security.web.server.header.FeaturePolicyServerHttpHeadersWriter
import org.springframework.security.web.server.header.ReferrerPolicyServerHttpHeadersWriter
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.server.WebFilter
import reactor.core.publisher.Mono
import app.http.Web
import app.utils.Constants
import app.utils.Properties
import users.User
import users.UserDao
import users.UserDao.Dao.findOneWithAuths
import workspace.Log

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfiguration(private val context: ApplicationContext) {
    companion object {
        val permitAll = arrayOf(
            "/",
            "/*.*",
            "/api/users/signup",
            "/api/users/activate",
            "/api/users/authenticate",
            "/api/users/reset-password/init",
            "/api/users/reset-password/finish",
        )
        val authenticated = arrayOf(
            "/api/**",
            "/services/**",
            "/swagger-resources/**",
            "/v2/api-docs",
            "/api/auth-info",
            "/api/users/**",
        )
        val adminAuthority = arrayOf(
            "/management/info",
            "/management/prometheus",
            "/management/health",
            "/management/health/**",
            "/management/**",
            "/api/admin/**",
        )
        val negated = arrayOf(
            "/app/**",
            "/i18n/**",
            "/content/**",
            "/swagger-ui/**",
            "/test/**",
            "/webjars/**"
        )
        val spaNegated = arrayOf(
            "/api",
            "/management",
            "/services",
            "/swagger",
            "/v2/api-docs",
        )

        fun ApplicationContext.userDetailsMono(
            emailOrLogin: String
        ): Mono<UserDetails> = getBean<Validator>().run {
            when {
                validateProperty(
                    User(email = emailOrLogin),
                    UserDao.Fields.EMAIL_FIELD
                ).isNotEmpty() && validateProperty(
                    User(login = emailOrLogin),
                    UserDao.Fields.LOGIN_FIELD
                ).isNotEmpty() -> throw UsernameNotFoundException("User $emailOrLogin was not found")

                else -> mono {
                    findOneWithAuths<User>(emailOrLogin).map { user ->
                        return@mono org.springframework.security.core.userdetails.User(
                            user.login,
                            user.password,
                            user.roles.map { SimpleGrantedAuthority(it.id) })
                    }.getOrNull() ?: throw UsernameNotFoundException("User $emailOrLogin was not found")
                }
            }
        }
    }

    @Component("userDetailsService")
    class DomainUserDetailsService(private val context: ApplicationContext) : ReactiveUserDetailsService {

        @Transactional(readOnly = true)
        @Throws(UsernameNotFoundException::class)
        override fun findByUsername(emailOrLogin: String): Mono<UserDetails> = context.userDetailsMono(emailOrLogin)
    }

    @Bean("passwordEncoder")
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun reactiveAuthenticationManager(): ReactiveAuthenticationManager =
        context.getBean<ReactiveUserDetailsService>()
            .run(::UserDetailsRepositoryReactiveAuthenticationManager)
            .apply { setPasswordEncoder(passwordEncoder()) }

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity)
            : SecurityWebFilterChain = http.securityMatcher(
        NegatedServerWebExchangeMatcher(
            OrServerWebExchangeMatcher(
                ServerWebExchangeMatchers.pathMatchers(
                    "/app/**",
                    "/i18n/**",
                    "/content/**",
                    "/swagger-ui/**",
                    "/test/**",
                    "/webjars/**"
                ), ServerWebExchangeMatchers.pathMatchers(HttpMethod.OPTIONS, "/**")
            )
        )
    ).csrf { csrf ->
        csrf.disable()
            .addFilterAt(Web.SpaWebFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
            .addFilterAt(JwtFilter(context), SecurityWebFiltersOrder.HTTP_BASIC)
            .authenticationManager(reactiveAuthenticationManager())
            .exceptionHandling { }
            .headers { h ->
                h.contentSecurityPolicy { p ->
                    p.policyDirectives(ContentSecurityPolicyServerHttpHeadersWriter.CONTENT_SECURITY_POLICY)
                }
                h.referrerPolicy { p -> p.policy(ReferrerPolicyServerHttpHeadersWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN) }
                h.permissionsPolicy { p -> p.policy(FeaturePolicyServerHttpHeadersWriter.FEATURE_POLICY) }
                h.frameOptions { f -> f.disable() }
            }.authorizeExchange {
                it.pathMatchers(*permitAll).permitAll()
                it.pathMatchers(*authenticated).authenticated()
                it.pathMatchers(*adminAuthority).hasAuthority(Constants.ROLE_ADMIN)
            }
    }.build()

    @Bean
    fun corsFilter(): WebFilter = CorsWebFilter(UrlBasedCorsConfigurationSource().apply source@{
        context.getBean<Properties>().cors.apply config@{
            when {
                allowedOrigins != null && allowedOrigins!!.isNotEmpty() -> this@source.apply {
                    Log.d("Registering CORS filter")
                    setOf(
                        "/api/**",
                        "/management/**",
                        "/v2/api-docs"
                    ).forEach { registerCorsConfiguration(it, this@config) }
                }
            }
        }
    })
}