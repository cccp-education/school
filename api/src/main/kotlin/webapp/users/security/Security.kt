package webapp.users.security
//
//import io.jsonwebtoken.JwtException
//import io.jsonwebtoken.Jwts.builder
//import io.jsonwebtoken.Jwts.parserBuilder
//import io.jsonwebtoken.SignatureAlgorithm.HS512
//import io.jsonwebtoken.io.Decoders.BASE64
//import io.jsonwebtoken.jackson.io.JacksonSerializer
//import io.jsonwebtoken.lang.Strings.hasLength
//import io.jsonwebtoken.security.Keys.hmacShaKeyFor
//import org.springframework.beans.factory.InitializingBean
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
//import org.springframework.security.core.Authentication
//import org.springframework.security.core.authority.SimpleGrantedAuthority
//import org.springframework.security.core.userdetails.User
//import org.springframework.stereotype.Component
//import webapp.core.property.AUTHORITIES_KEY
//import webapp.core.property.INVALID_TOKEN
//import webapp.core.property.VALID_TOKEN
//import webapp.core.logging.d
//import webapp.core.logging.i
//import webapp.core.logging.t
//import webapp.core.logging.w
//import webapp.core.property.Properties
//import java.security.Key
//import java.util.*
//import kotlin.text.Charsets.UTF_8
//
//@Component
//class Security(
//    private val properties: Properties,
//    private var key: Key? = null,
//    private var tokenValidityInMilliseconds: Long = 0,
//    private var tokenValidityInMillisecondsForRememberMe: Long = 0
//) : InitializingBean {
//    companion object {
//        val permitAll = arrayOf(
//            "/",
//            "/**",
//            "/*.*",
//            "/api/accounts/signup",
//            "/api/accounts/activate",
//            "/api/accounts/authenticate",
//            "/api/accounts/reset-password/init",
//            "/api/accounts/reset-password/finish",
//            "/api/auth-info",
//            "/api/users/**",
//            "/management/health",
//            "/management/health/**",
//            "/management/info",
//            "/management/prometheus",
//        )
//        val authenticated = arrayOf(
//            "/api/**",
//            "/services/**",
//            "/swagger-resources/**",
//            "/v2/api-docs",
//        )
//        val adminAuthority = arrayOf(
//            "/management/**",
//            "/api/admin/**",
//        )
//        val negated = arrayOf(
//            "/app/**",
//            "/i18n/**",
//            "/content/**",
//            "/swagger-ui/**",
//            "/test/**",
//            "/webjars/**"
//        )
//        val spaNegated = arrayOf(
//            "/api",
//            "/management",
//            "/services",
//            "/swagger",
//            "/v2/api-docs",
//        )
//    }
//
//    @Throws(Exception::class)
//    override fun afterPropertiesSet() {
//        properties
//            .security
//            .authentication
//            .jwt
//            .secret
//            .run {
//                key = hmacShaKeyFor(
//                    when {
//                        !hasLength(this) -> w(
//                            "Warning: the Jwt key used is not Base64-encoded. " +
//                                    "We recommend using the `webapp.security.authentication.jwt.base64-secret`" +
//                                    " key for optimum security."
//                        ).run { toByteArray(UTF_8) }
//
//                        else -> d("Using a Base64-encoded Jwt secret key").run {
//                            BASE64.decode(
//                                properties
//                                    .security
//                                    .authentication
//                                    .jwt
//                                    .base64Secret
//                            )
//                        }
//                    }
//                )
//            }
//        tokenValidityInMilliseconds = properties
//            .security
//            .authentication
//            .jwt
//            .tokenValidityInSeconds * 1000
//        tokenValidityInMillisecondsForRememberMe = properties
//            .security
//            .authentication
//            .jwt
//            .tokenValidityInSecondsForRememberMe * 1000
//    }
//
//    suspend fun createToken(
//        authentication: Authentication,
//        rememberMe: Boolean
//    ): String {
//        Date().time.run {
//            return builder()
//                .setSubject(authentication.name)
//                .claim(
//                    AUTHORITIES_KEY,
//                    authentication.authorities
//                        .asSequence()
//                        .map { it.authority }
//                        .joinToString(separator = ","))
//                .signWith(key, HS512)
//                .setExpiration(
//                    when {
//                        rememberMe -> Date(this + tokenValidityInMillisecondsForRememberMe)
//                        else -> Date(this + tokenValidityInMilliseconds)
//                    }
//                )
//                .serializeToJsonWith(JacksonSerializer())
//                .compact()
//        }
//    }
//
//    fun getAuthentication(token: String): Authentication {
//        parserBuilder()
//            .setSigningKey(key)
//            .build()
//            .parseClaimsJws(token)
//            .body
//            .apply {
//                this[AUTHORITIES_KEY]
//                    .toString()
//                    .splitToSequence(",")
//                    .mapTo(mutableListOf()) { SimpleGrantedAuthority(it) }
//                    .apply authorities@{
//                        return@getAuthentication UsernamePasswordAuthenticationToken(
//                            User(subject, "", this@authorities),
//                            token,
//                            this@authorities
//                        )
//                    }
//            }
//    }
//
//    fun validateToken(token: String): Boolean = try {
//        parserBuilder()
//            .setSigningKey(key)
//            .build()
//            .parseClaimsJws(token)
//        VALID_TOKEN
//    } catch (e: JwtException) {
//        i("Invalid Jwt token.")
//        t("Invalid Jwt token trace. $e")
//        INVALID_TOKEN
//    } catch (e: IllegalArgumentException) {
//        i("Invalid Jwt token.")
//        t("Invalid Jwt token trace. $e")
//        INVALID_TOKEN
//    }
//}
///*
//package community.security
//
//import community.Properties
//import community.d
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import org.springframework.security.authentication.ReactiveAuthenticationManager
//import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager
//import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
//import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
//import org.springframework.security.config.web.server.ServerHttpSecurity
//import org.springframework.security.core.userdetails.ReactiveUserDetailsService
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
//import org.springframework.security.crypto.password.PasswordEncoder
//import org.springframework.security.web.server.SecurityWebFilterChain
//import org.springframework.web.cors.reactive.CorsWebFilter
//import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
//
//@Configuration
//@EnableWebFluxSecurity
//@EnableReactiveMethodSecurity
//class Security(
//    private val properties: Properties,
//    private val spaFilter: community.Configuration.SpaFilter,
//    private val security: SecurityJwt,
//    private val userDetailsService: ReactiveUserDetailsService,
//) {
//    companion object {
//        val permitAll = arrayOf(
//            "/",
//            "/**",
//            "/*.*",
//            "/api/accounts/signup",
//            "/api/accounts/activate",
//            "/api/accounts/authenticate",
//            "/api/accounts/reset-password/init",
//            "/api/accounts/reset-password/finish",
//            "/api/auth-info",
//            "/api/users/**",
//            "/management/health",
//            "/management/health/**",
//            "/management/info",
//            "/management/prometheus",
//        )
//        val authenticated = arrayOf(
//            "/api/**",
//            "/services/**",
//            "/swagger-resources/**",
//            "/v2/api-docs",
//        )
//        val adminAuthority = arrayOf(
//            "/management/**",
//            "/api/admin/**",
//        )
//        val negated = arrayOf(
//            "/app/**",
//            "/i18n/**",
//            "/content/**",
//            "/swagger-ui/**",
//            "/test/**",
//            "/webjars/**"
//        )
//        val spaNegated = arrayOf(
//            "/api",
//            "/management",
//            "/services",
//            "/swagger",
//            "/v2/api-docs",
//        )
//    }
//
//    @Suppress("unused")
//    @Bean
//    fun springSecurityFilterChain(
//        http: ServerHttpSecurity
//    ): SecurityWebFilterChain = http.securityWebFilterChain(
//        security,
//        spaFilter,
//        reactiveAuthenticationManager()
//    )!!
//
//
//    @Suppress("unused")
//    @Bean
//    fun corsFilter(): CorsWebFilter = CorsWebFilter(UrlBasedCorsConfigurationSource().apply source@{
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
//        UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService).apply {
//            setPasswordEncoder(passwordEncoder())
//        }
//}
// */