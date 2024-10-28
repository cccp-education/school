package school.users.security

import jakarta.validation.Validator
import kotlinx.coroutines.reactor.mono
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod.OPTIONS
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder.AUTHENTICATION
import org.springframework.security.config.web.server.SecurityWebFiltersOrder.HTTP_BASIC
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.header.ContentSecurityPolicyServerHttpHeadersWriter
import org.springframework.security.web.server.header.FeaturePolicyServerHttpHeadersWriter.FEATURE_POLICY
import org.springframework.security.web.server.header.ReferrerPolicyServerHttpHeadersWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.server.WebFilter
import reactor.core.publisher.Mono
import school.base.http.Web.SpaWebFilter
import school.base.utils.Log.d
import school.base.utils.Properties
import school.base.utils.ROLE_ADMIN
import school.users.User
import school.users.User.UserDao.Dao.findOneWithAuths
import school.users.User.UserDao.Fields.EMAIL_FIELD
import school.users.User.UserDao.Fields.LOGIN_FIELD
import org.springframework.security.core.userdetails.User as UserSecurity


@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfiguration(private val context: ApplicationContext) {

    @Component("userDetailsService")
    class DomainUserDetailsService(private val context: ApplicationContext) : ReactiveUserDetailsService {

        @Transactional(readOnly = true)
        @Throws(UsernameNotFoundException::class)
        override fun findByUsername(emailOrLogin: String): Mono<UserDetails> = context
            .getBean<Validator>()
            .run {
                when {
                    validateProperty(
                        User(email = emailOrLogin),
                        EMAIL_FIELD
                    ).isNotEmpty() && validateProperty(
                        User(login = emailOrLogin),
                        LOGIN_FIELD
                    ).isNotEmpty() -> throw UsernameNotFoundException("User $emailOrLogin was not found")

                    else -> mono {
                        context.findOneWithAuths<User>(emailOrLogin).map {
                            return@mono createSpringSecurityUser(emailOrLogin, it)
                        }.getOrNull() ?: throw UsernameNotFoundException("User $emailOrLogin was not found")
                    }
                }
            }

        //        @Throws(UserNotActivatedException::class)
        fun createSpringSecurityUser(lowercaseLogin: String, user: User): UserSecurity = /*when {
        !user.activated -> throw UserNotActivatedException("User $lowercaseLogin was not activated")
        else -> */UserSecurity(
            user.login,
            user.password,
            user.roles.map { SimpleGrantedAuthority(it.id) }
        )
    }

    @Bean("passwordEncoder")
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun reactiveAuthenticationManager(): ReactiveAuthenticationManager =
        context.getBean<ReactiveUserDetailsService>()
            .run(::UserDetailsRepositoryReactiveAuthenticationManager)
            .apply { setPasswordEncoder(passwordEncoder()) }


//    @Bean
//    fun signupSecurityFilterChain(http: org.springframework.security.config.annotation.web.builders.HttpSecurity): org.springframework.security.web.SecurityFilterChain = http.authorizeHttpRequests {
//    }.build()
    /*.apply {
    authorizeExchange {
        securityMatcher(pathMatchers("/api/users/signup"))
    }
}.build()*/

    @Suppress("removal")
    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity)
            : SecurityWebFilterChain =
        http.securityMatcher(
            NegatedServerWebExchangeMatcher(
                OrServerWebExchangeMatcher(
                    pathMatchers(
                        "/app/**",
                        "/i18n/**",
                        "/content/**",
                        "/swagger-ui/**",
                        "/test/**",
                        "/webjars/**"
                    ), pathMatchers(OPTIONS, "/**")
                )
            )
        ).csrf()
            .disable()
            .addFilterAt(SpaWebFilter(), AUTHENTICATION)
            .addFilterAt(JwtFilter(context), HTTP_BASIC)
            .authenticationManager(reactiveAuthenticationManager())
            .exceptionHandling()
            .and()
            .headers()
            .contentSecurityPolicy(ContentSecurityPolicyServerHttpHeadersWriter.CONTENT_SECURITY_POLICY)
            .and()
            .referrerPolicy(STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
            .and()
            .permissionsPolicy().policy(FEATURE_POLICY)
            .and()
            .frameOptions().disable()
            .and()
            .authorizeExchange()
            .pathMatchers(
                "/",
//                "/**",//DEVMODE
                "/*.*",
                "/api/users/signup",
                "/api/users/activate",
                "/api/users/authenticate",
                "/api/users/reset-password/init",
                "/api/users/reset-password/finish",
            ).permitAll()
            .pathMatchers(
                "/api/**",
                "/services/**",
                "/swagger-resources/**",
                "/v2/api-docs",
                "/api/auth-info",
                "/api/users/**",
            ).authenticated()
            .pathMatchers(
                "/management/info",
                "/management/prometheus",
                "/management/health",
                "/management/health/**",
                "/management/**",
                "/api/admin/**",
            ).hasAuthority(ROLE_ADMIN)
            .and()
            .build()

    @Bean
    fun corsFilter(): WebFilter = CorsWebFilter(UrlBasedCorsConfigurationSource().apply source@{
        context.getBean<Properties>().cors.apply config@{
            when {
                allowedOrigins != null && allowedOrigins!!.isNotEmpty() -> this@source.apply {
                    d("Registering CORS filter")
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
