package webapp.users.security
//
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import org.springframework.http.HttpMethod.*
//import org.springframework.security.authentication.ReactiveAuthenticationManager
//import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
//import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
//import org.springframework.security.config.web.server.SecurityWebFiltersOrder.AUTHENTICATION
//import org.springframework.security.config.web.server.SecurityWebFiltersOrder.HTTP_BASIC
//import org.springframework.security.config.web.server.ServerHttpSecurity
//import org.springframework.security.core.userdetails.ReactiveUserDetailsService
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
//import org.springframework.security.crypto.password.PasswordEncoder
//import org.springframework.security.web.server.SecurityWebFilterChain
//import org.springframework.security.web.server.header.ReferrerPolicyServerHttpHeadersWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN
//import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher
//import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher
//import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers
//import org.springframework.web.cors.reactive.CorsWebFilter
//import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
//import webapp.core.http.Web.SpaWebFilter
//import webapp.core.property.CONTENT_SECURITY_POLICY
//import webapp.core.property.FEATURE_POLICY
//import webapp.core.property.ROLE_ADMIN
//import webapp.core.logging.d
//import webapp.core.property.Properties
//import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager as RepositoryAuthentication
//
//@Configuration
//@EnableWebFluxSecurity
//@EnableReactiveMethodSecurity
//class SecurityConfiguration(
//    private val properties: Properties,
//    private val security: Security,
//    private val userDetailsService: ReactiveUserDetailsService,
//) {
//    @Suppress("removal")
//    @Bean
//    fun springSecurityFilterChain(
//        http: ServerHttpSecurity
//    ): SecurityWebFilterChain =
//        http.securityMatcher(
//            NegatedServerWebExchangeMatcher(
//                OrServerWebExchangeMatcher(
//                    pathMatchers(
//                        "/app/**",
//                        "/i18n/**",
//                        "/content/**",
//                        "/swagger-ui/**",
//                        "/test/**",
//                        "/webjars/**"
//                    ), pathMatchers(OPTIONS, "/**")
//                )
//            )
//        ).csrf()
//            .disable()
//            .addFilterAt(SpaWebFilter(), AUTHENTICATION)
//            .addFilterAt(JwtFilter(security), HTTP_BASIC)
//            .authenticationManager(reactiveAuthenticationManager())
//            .exceptionHandling()
//            .and()
//            .headers().contentSecurityPolicy(CONTENT_SECURITY_POLICY)
//            .and()
//            .referrerPolicy(STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
//            .and()
//            .permissionsPolicy().policy(FEATURE_POLICY)
//            .and()
//            .frameOptions().disable()
//            .and()
//            .authorizeExchange()
//            .pathMatchers(
//                "/",
//                "/**",
//                "/*.*",
//                "/api/accounts/signup",
//                "/api/accounts/activate",
//                "/api/accounts/authenticate",
//                "/api/accounts/reset-password/init",
//                "/api/accounts/reset-password/finish",
//                "/api/auth-info",
//                "/api/users/**",
//                "/management/health",
//                "/management/health/**",
//                "/management/info",
//                "/management/prometheus",
//                "/api/**"
//            ).permitAll()
//            .pathMatchers(
//                "/services/**",
//                "/swagger-resources/**",
//                "/v2/api-docs"
//            ).authenticated()
//            .pathMatchers(
//                "/management/**",
//                "/api/admin/**"
//            ).hasAuthority(ROLE_ADMIN)
//            .and()
//            .build()
//
//    @Bean
//    fun corsFilter() = CorsWebFilter(UrlBasedCorsConfigurationSource().apply source@{
//        properties.cors.apply config@{
//            when {
//                allowedOrigins != null && allowedOrigins!!.isNotEmpty() -> this@source.apply {
//                    d("Registering CORS filter")
//                    setOf(
//                        "/api/**",
//                        "/management/**",
//                        "/v2/api-docs"
//                    ).forEach { registerCorsConfiguration(it, this@config) }
//                }
//            }
//        }
//    })
//
//
//    @Bean("passwordEncoder")
//    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
//
//    @Bean
//    fun reactiveAuthenticationManager(): ReactiveAuthenticationManager =
//        RepositoryAuthentication(userDetailsService).apply {
//            setPasswordEncoder(passwordEncoder())
//        }
//
//
//}