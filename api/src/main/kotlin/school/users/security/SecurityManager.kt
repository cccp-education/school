package school.users.security

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.Jwts.builder
import io.jsonwebtoken.SignatureAlgorithm.HS512
import io.jsonwebtoken.io.Decoders.BASE64
import io.jsonwebtoken.jackson.io.JacksonSerializer
import io.jsonwebtoken.lang.Strings.hasLength
import io.jsonwebtoken.security.Keys.hmacShaKeyFor
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Component
import school.base.utils.AUTHORITIES_KEY
import school.base.utils.INVALID_TOKEN
import school.base.utils.Log.d
import school.base.utils.Log.i
import school.base.utils.Log.t
import school.base.utils.Properties
import school.base.utils.VALID_TOKEN
import java.security.Key
import java.time.ZonedDateTime.now
import java.util.*

@Component
class SecurityManager(
    private val context: ApplicationContext,
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    private var key: Key? = null,
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    private var tokenValidityInMilliseconds: Long = 0,
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    private var tokenValidityInMillisecondsForRememberMe: Long = 0
) : InitializingBean {
    companion object {
        val permitAll = arrayOf(
            "/",
//            "/**",//DEVMODE
            "/*.*",
            "/api/accounts/signup",
            "/api/accounts/activate",
            "/api/accounts/authenticate",
            "/api/accounts/reset-password/init",
            "/api/accounts/reset-password/finish",
            "/api/auth-info",
            "/api/users/**",
            "/management/health",
            "/management/health/**",
            "/management/info",
            "/management/prometheus",
        )
        val authenticated = arrayOf(
            "/api/**",
            "/services/**",
            "/swagger-resources/**",
            "/v2/api-docs",
        )
        val adminAuthority = arrayOf(
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
    }

    @Throws(Exception::class)
    override fun afterPropertiesSet() {
        context.getBean<Properties>()
            .security
            .authentication
            .jwt
            .secret
            .run {
                key = hmacShaKeyFor(
                    when {
                        !hasLength(this) -> (
                                buildString {
                                    append("Warning: the Jwt key used is not Base64-encoded. ")
                                    append("We recommend using the `school.security.authentication.jwt.base64-secret`")
                                    append(" key for optimum security.")
                                }
                                ).run(_root_ide_package_.school.base.utils.Log::w)
                            .run { toByteArray() }

                        else -> d("Using a Base64-encoded Jwt secret key").run {
                            context.getBean<Properties>()
                                .security
                                .authentication
                                .jwt
                                .base64Secret
                                .run(BASE64::decode)
                        }
                    }
                )
            }
        tokenValidityInMilliseconds = context.getBean<Properties>()
            .security
            .authentication
            .jwt
            .tokenValidityInSeconds * 1000
        tokenValidityInMillisecondsForRememberMe = context.getBean<Properties>()
            .security
            .authentication
            .jwt
            .tokenValidityInSecondsForRememberMe * 1000
    }

    private fun calculateExpirationDate(rememberMe: Boolean): Date = when {
        rememberMe -> tokenValidityInMillisecondsForRememberMe

        else -> tokenValidityInMilliseconds
    }.run {
        now()
            .plusSeconds(this / 1000)
            .toInstant()
            .run(Date::from)
    }


    suspend fun createToken(
        authentication: Authentication,
        rememberMe: Boolean
    ): String = now().run {
        builder()
            .setSubject(authentication.name)
            .claim(
                AUTHORITIES_KEY,
                authentication.authorities
                    .asSequence()
                    .map { it.authority }
                    .joinToString(separator = ","))
            .signWith(key, HS512)
            .setExpiration(calculateExpirationDate(rememberMe))
            .serializeToJsonWith(JacksonSerializer())
            .compact()
    }

    fun getAuthentication(token: String): Authentication {
        Jwts.parserBuilder()
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
                        return@getAuthentication UsernamePasswordAuthenticationToken(
                            User(subject, "", this@authorities),
                            token,
                            this@authorities
                        )
                    }
            }
    }

    fun validateToken(token: String): Boolean = try {
        Jwts.parserBuilder()
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
}

//import community.Properties
//import community.d
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import org.springframework.security.authentication.ReactiveAuthenticationManager
//import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager
//import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
//import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
//import org.springframework.security.config.web.server.ServerHttpSecurity
//import org.springframework.security.base.userdetails.ReactiveUserDetailsService
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