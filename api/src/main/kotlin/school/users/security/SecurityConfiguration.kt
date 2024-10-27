package school.users.security

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
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.header.ContentSecurityPolicyServerHttpHeadersWriter
import org.springframework.security.web.server.header.FeaturePolicyServerHttpHeadersWriter.FEATURE_POLICY
import org.springframework.security.web.server.header.ReferrerPolicyServerHttpHeadersWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.server.WebFilter
import school.base.http.Web.SpaWebFilter
import school.base.utils.Log.d
import school.base.utils.Properties
import school.base.utils.ROLE_ADMIN


@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfiguration(val context: ApplicationContext) {

    @Bean("passwordEncoder")
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun reactiveAuthenticationManager(): ReactiveAuthenticationManager =
        context.getBean<ReactiveUserDetailsService>()
            .run(::UserDetailsRepositoryReactiveAuthenticationManager)
            .apply { setPasswordEncoder(passwordEncoder()) }

    @Suppress("removal")
    @Bean
    fun springSecurityFilterChain(
        http: ServerHttpSecurity
    ): SecurityWebFilterChain =
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
//                "/",
//                "/**",//DEVMODE
//                "/*.*",
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
