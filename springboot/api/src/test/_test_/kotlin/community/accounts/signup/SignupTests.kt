@file:Suppress("NonAsciiCharacters")

package community.accounts.signup

import community.*
import community.accounts.Account.Companion.EMAIL_FIELD
import community.accounts.Account.Companion.FIRST_NAME_FIELD
import community.accounts.Account.Companion.LAST_NAME_FIELD
import community.accounts.Account.Companion.LOGIN_FIELD
import jakarta.validation.Validator
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.springframework.beans.factory.getBean
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.bindToServer
import org.springframework.test.web.reactive.server.returnResult
import kotlin.test.Test
import kotlin.test.assertTrue

@kotlin.test.Ignore
internal class SignupTests {
    private lateinit var context: ConfigurableApplicationContext
    private val validator: Validator by lazy { context.getBean() }
    private val client: WebTestClient by lazy { bindToServer().baseUrl(BASE_URL_DEV).build() }

    @BeforeAll
    fun `lance le server en profile test`() {
        context = launcher()
    }

    @AfterAll
    fun `arrête le serveur`() = context.close()

    @AfterEach
    fun tearDown() = context.deleteAllAccounts()

    @Test
    fun `vérifie que la requête contient bien des données cohérentes`() {
        client
            .post()
            .uri("")
            .contentType(APPLICATION_JSON)
            .bodyValue(defaultAccount)
            .exchange()
            .returnResult<Unit>()
            .requestBodyContent!!
            .logBody
            .requestToString()
            .run {
                defaultAccount.run {
                    setOf(
                        "\"$LOGIN_FIELD\":\"${login}\"",
//                        "\"$PASSWORD_FIELD\":\"${password}\"",
                        "\"$FIRST_NAME_FIELD\":\"${firstName}\"",
                        "\"$LAST_NAME_FIELD\":\"${lastName}\"",
                        "\"$EMAIL_FIELD\":\"${email}\"",
                    ).map { assertTrue(contains(it)) }
                }
            }
    }

//
//    @Test //TODO: mock sendmail
//    fun `test signup avec un account valide`() {
//        val countUserBefore = context.countAccount
//        val countUserAuthBefore = context.countUserRole
//        assertEquals(0, countUserBefore)
//        assertEquals(0, countUserAuthBefore)
//        client
//            .post()
//            .uri(API_SIGNUP_PATH)
//            .contentType(APPLICATION_JSON)
//            .bodyValue(defaultAccount)
//            .exchange()
//            .expectStatus()
//            .isCreated
//            .returnResult<ResponseEntity<ProblemDetail>>()
//            .responseBodyContent!!
//            .isEmpty()
//            .run { assertTrue(this) }
//        assertEquals(countUserBefore + 1, context.countAccount)
//        assertEquals(countUserAuthBefore + 1, context.countUserRole)
//        context.findOneByEmail(defaultAccount.email!!).run {
//            assertNotNull(this)
//            assertFalse(activated)
//            assertNotNull(activationKey)
//        }
//    }
//
//    @Test
//    fun `test signup account validator avec login invalid`() {
//        validator
//            .validateProperty(Account(login = "funky-log(n"), LOGIN_FIELD)
//            .run viol@{
//                assertTrue(isNotEmpty())
//                first().run {
//                    assertEquals(
//                        "{${Pattern::class.java.name}.message}",
//                        messageTemplate
//                    )
//                }
//            }
//    }
//
//    @Test
//    fun `test signup account avec login invalid`() {
//        assertEquals(0, context.countAccount)
//        client
//            .post()
//            .uri(API_SIGNUP_PATH)
//            .contentType(APPLICATION_JSON)
//            .header(ACCEPT_LANGUAGE, FRENCH.language)
//            .bodyValue(defaultAccount.copy(login = "funky-log(n"))
//            .exchange()
//            .expectStatus()
//            .isBadRequest
//            .returnResult<ResponseEntity<ProblemDetail>>()
//            .responseBodyContent!!
//            .logBody
//            .isNotEmpty()
//            .run { assertTrue(this) }
//        assertEquals(0, context.countAccount)
//    }
//
//
//    @Test
//    fun `test signup account avec un email invalid`() {
//        val countBefore = context.countAccount
//        assertEquals(0, countBefore)
//        client
//            .post()
//            .uri(API_SIGNUP_PATH)
//            .contentType(APPLICATION_JSON)
//            .bodyValue(defaultAccount.copy(email = "inv"))
//            .exchange()
//            .expectStatus()
//            .isBadRequest
//            .returnResult<ResponseEntity<ProblemDetail>>()
//            .responseBodyContent!!
//            .isNotEmpty()
//            .run { assertTrue(this) }
//        assertEquals(0, countBefore)
//    }
//
//    @Test
//    fun `test signup account validator avec un password invalid`() {
//        val wrongPassword = "123"
//        validator
//            .validateProperty(Signup(password = wrongPassword), PASSWORD_FIELD)
//            .run {
//                assertTrue(isNotEmpty())
//                first().run {
//                    assertEquals(
//                        "{${Size::class.java.name}.message}",
//                        messageTemplate
//                    )
//                }
//            }
//    }
//
//    @Test
//    fun `test signup account avec un password invalid`() {
//        assertEquals(0, context.countAccount)
//        client
//            .post()
//            .uri(API_SIGNUP_PATH)
//            .contentType(APPLICATION_JSON)
//            .bodyValue(defaultAccount.copy(password = "123"))
//            .exchange()
//            .expectStatus()
//            .isBadRequest
//            .returnResult<ResponseEntity<ProblemDetail>>()
//            .responseBodyContent!!
//            .logBody
//            .isNotEmpty()
//            .run { assertTrue(this) }
//        assertEquals(0, context.countAccount)
//    }
//
//    @Test
//    fun `test signup account avec un password null`() {
//        assertEquals(0, context.countAccount)
//        client
//            .post()
//            .uri(API_SIGNUP_PATH)
//            .contentType(APPLICATION_JSON)
//            .bodyValue(defaultAccount.copy(password = null))
//            .exchange()
//            .expectStatus()
//            .isBadRequest
//            .returnResult<ResponseEntity<ProblemDetail>>()
//            .responseBodyContent!!
//            .isNotEmpty()
//            .run { assertTrue(this) }
//        assertEquals(0, context.countAccount)
//    }
//
//    @Test
//    fun `test signup account activé avec un email existant`() {
//        assertEquals(0, context.countAccount)
//        assertEquals(0, context.countUserRole)
//        //activation de l'account
//        context.createActivatedDataAccounts(setOf(defaultAccount))
//        assertEquals(1, context.countAccount)
//        assertEquals(1, context.countUserRole)
//        context.findOneByEmail(defaultAccount.email!!).run {
//            assertNotNull(this)
//            assertTrue(activated)
//            assertNull(activationKey)
//        }
//
//        client
//            .post()
//            .uri(API_SIGNUP_PATH)
//            .contentType(APPLICATION_JSON)
//            .bodyValue(defaultAccount.copy(login = "foo"))
//            .exchange()
//            .expectStatus()
//            .isBadRequest
//            .returnResult<ResponseEntity<ProblemDetail>>()
//            .responseBodyContent!!
//            .isNotEmpty()
//            .run { assertTrue(this) }
//    }
//
//
//    @Test
//    fun `test signup account activé avec un login existant`() {
//        assertEquals(0, context.countAccount)
//        assertEquals(0, context.countUserRole)
//        //activation de l'account
//        context.createActivatedDataAccounts(setOf(defaultAccount))
//        context.findOneByEmail(defaultAccount.email!!).run {
//            assertNotNull(this)
//            assertTrue(activated)
//            assertNull(activationKey)
//        }
//        assertEquals(1, context.countAccount)
//        assertEquals(1, context.countUserRole)
//
//        client
//            .post()
//            .uri(API_SIGNUP_PATH)
//            .contentType(APPLICATION_JSON)
//            .bodyValue(defaultAccount.copy(email = "foo@localhost"))
//            .exchange()
//            .expectStatus()
//            .isBadRequest
//            .returnResult<ResponseEntity<ProblemDetail>>()
//            .responseBodyContent!!
//            .isNotEmpty()
//            .run { assertTrue(this) }
//    }
//
//    @Test//TODO: mock sendmail
//    fun `test signup account avec un email dupliqué`() {
//        assertEquals(0, context.countAccount)
//        assertEquals(0, context.countUserRole)
//        // premier user
//        // sign up premier user
//        client
//            .post()
//            .uri(API_SIGNUP_PATH)
//            .contentType(APPLICATION_JSON)
//            .bodyValue(defaultAccount)
//            .exchange()
//            .expectStatus()
//            .isCreated
//            .returnResult<Unit>()
//            .responseBodyContent!!
//            .isEmpty()
//            .run { assertTrue(this) }
//        assertEquals(1, context.countAccount)
//        assertEquals(1, context.countUserRole)
//        assertFalse(context.findOneByEmail(defaultAccount.email!!)!!.activated)
//
//        // email dupliqué, login different
//        // sign up un second user (non activé)
//        val secondLogin = "foo"
//        client
//            .post()
//            .uri(API_SIGNUP_PATH)
//            .contentType(APPLICATION_JSON)
//            .bodyValue(defaultAccount.copy(login = secondLogin))
//            .exchange()
//            .expectStatus()
//            .isCreated
//            .returnResult<Unit>()
//            .responseBodyContent!!
//            .isEmpty()
//            .run { assertTrue(this) }
//        assertEquals(1, context.countAccount)
//        assertEquals(1, context.countUserRole)
//        assertNull(context.findOneByLogin(defaultAccount.login!!))
//        context.findOneByLogin(secondLogin).run {
//            assertNotNull(this)
//            assertEquals(defaultAccount.email!!, email)
//            assertFalse(activated)
//        }
//
//        // email dupliqué - avec un email en majuscule, login différent
//        // sign up un troisieme user (non activé)
//        val thirdLogin = "bar"
//        client
//            .post()
//            .uri(API_SIGNUP_PATH)
//            .contentType(APPLICATION_JSON)
//            .bodyValue(defaultAccount.copy(login = thirdLogin, email = defaultAccount.email!!.uppercase()))
//            .exchange()
//            .expectStatus()
//            .isCreated
//            .returnResult<Unit>()
//            .responseBodyContent!!
//            .isEmpty()
//            .run { assertTrue(this) }
//        assertEquals(1, context.countAccount)
//        assertEquals(1, context.countUserRole)
//        context.findOneByLogin(thirdLogin).run {
//            assertNotNull(this)
//            assertEquals(defaultAccount.email!!, email!!.lowercase())
//            assertFalse(activated)
//            //activation du troisieme user
//            context.saveAccount(copy(activated = true, activationKey = null))
//        }
//        //validation que le troisieme est actif et activationKey est null
//        context.findOneByLogin(thirdLogin).run {
//            assertNotNull(this)
//            assertTrue(activated)
//            assertNull(activationKey)
//        }
//        val fourthLogin = "baz"
//        // sign up un quatrieme user avec login different et meme email
//        // le user existant au meme mail est deja activé
//        client
//            .post()
//            .uri(API_SIGNUP_PATH)
//            .contentType(APPLICATION_JSON)
//            .bodyValue(defaultAccount.copy(login = fourthLogin))
//            .exchange()
//            .expectStatus()
//            .isBadRequest
//            .returnResult<Unit>()
//            .responseBodyContent!!
//            .isNotEmpty()
//            .run { assertTrue(this) }
//        assertEquals(1, context.countAccount)
//        assertEquals(1, context.countUserRole)
//        assertNull(context.findOneByLogin(fourthLogin))
//        //meme id
//        assertEquals(context.findOneByLogin(thirdLogin).apply {
//            assertNotNull(this)
//            assertTrue(activated)
//            assertNull(activationKey)
//            assertTrue(defaultAccount.email!!.equals(email!!, true))
//        }!!.id, context.findOneByEmail(defaultAccount.email!!).apply {
//            assertNotNull(this)
//            assertTrue(activated)
//            assertNull(activationKey)
//            assertTrue(thirdLogin.equals(login, true))
//        }!!.id
//        )
//    }
//
//    @Test//TODO: mock sendmail
//    fun `test signup account en renseignant l'autorité admin qui sera ignoré et le champ activé qui sera mis à false`() {
//        val countUserBefore = context.countAccount
//        val countUserAuthBefore = context.countUserRole
//        assertEquals(0, countUserBefore)
//        assertEquals(0, countUserAuthBefore)
//        val login = "badguy"
//        client
//            .post()
//            .uri(API_SIGNUP_PATH)
//            .contentType(APPLICATION_JSON)
//            .bodyValue(
//                Account(
//                    login = login,
//                    password = "password",
//                    firstName = "Bad",
//                    lastName = "Guy",
//                    email = "badguy@example.com",
//                    activated = true,
//                    imageUrl = "http://placehold.it/50x50",
//                    langKey = DEFAULT_LANGUAGE,
//                    authorities = setOf(ROLE_ADMIN),
//                )
//            )
//            .exchange()
//            .expectStatus()
//            .isCreated
//            .returnResult<Unit>()
//            .responseBodyContent.run {
//                assertNotNull(this)
//                assertTrue(isEmpty())
//            }
//        assertEquals(countUserBefore + 1, context.countAccount)
//        assertEquals(countUserAuthBefore + 1, context.countUserRole)
//        context.findOneByLogin(login).run {
//            assertNotNull(this)
//            assertFalse(activated)
//            assertFalse(activationKey.isNullOrBlank())
//        }
//        assertTrue(context.findAllUserRoles.none {
//            it.role.equals(ROLE_ADMIN, true)
//        })
//    }
//
//    @Test
//    fun `vérifie l'internationalisation des validations par validator factory avec mauvais login en italien`() {
//        byProvider(HibernateValidator::class.java)
//            .configure()
//            .defaultLocale(ENGLISH)
//            .locales(FRANCE, ITALY, US)
//            .localeResolver {
//                // get the locales supported by the client from the Accept-Language header
//                val acceptLanguageHeader = "it-IT;q=0.9,en-US;q=0.7"
//                val acceptedLanguages = LanguageRange.parse(acceptLanguageHeader)
//                val resolvedLocales = filter(acceptedLanguages, it.supportedLocales)
//                if (resolvedLocales.size > 0) resolvedLocales[0]
//                else it.defaultLocale
//            }
//            .buildValidatorFactory()
//            .validator
//            .validateProperty(defaultAccount.copy(login = "funky-log(n"), LOGIN_FIELD)
//            .run viol@{
//                assertTrue(isNotEmpty())
//                first().run {
//                    assertEquals(
//                        "{${Pattern::class.java.name}.message}",
//                        messageTemplate
//                    )
//                    assertEquals(false, message.contains("doit correspondre à"))
//                    assertContains(
//                        "deve corrispondere a \"^(?>[a-zA-Z0-9!\$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*)|(?>[_.@A-Za-z0-9-]+)\$\"",
//                        message
//                    )
//                }
//            }
//    }
//
//    @Test
//    fun `vérifie l'internationalisation des validations par REST avec mot de passe non conforme en francais`() {
//        assertEquals(0, context.countAccount)
//        client
//            .post()
//            .uri(API_SIGNUP_PATH)
//            .contentType(APPLICATION_JSON)
//            .header(ACCEPT_LANGUAGE, FRENCH.language)
//            .bodyValue(defaultAccount.copy(password = "123"))
//            .exchange()
//            .expectStatus()
//            .isBadRequest
//            .returnResult<ResponseEntity<ProblemDetail>>()
//            .responseBodyContent!!
//            .run {
//                assertTrue(isNotEmpty())
//                assertContains(requestToString(), "la taille doit")
//            }
//        assertEquals(0, context.countAccount)
//
//    }
//
//
//    @Test
//    fun `test activate avec une mauvaise clé`() {
//        client
//            .get()
//            .uri("$API_ACTIVATE_PATH$API_ACTIVATE_PARAM", "wrongActivationKey")
//            .exchange()
//            .expectStatus()
//            .is5xxServerError
//            .returnResult<ProblemDetail>()
//    }
//
//    @Test
//    fun `test activate avec une clé valide`() {
//        assertEquals(0, context.countAccount)
//        assertEquals(0, context.countUserRole)
//        context.createDataAccounts(setOf(defaultAccount))
//        assertEquals(1, context.countAccount)
//        assertEquals(1, context.countUserRole)
//
//        client
//            .get()
//            .uri(
//                "$API_ACTIVATE_PATH$API_ACTIVATE_PARAM",
//                context.findOneByLogin(defaultAccount.login!!)!!.apply {
//                    assertTrue(activationKey!!.isNotBlank())
//                    assertFalse(activated)
//                }.activationKey
//            ).exchange()
//            .expectStatus()
//            .isOk
//            .returnResult<ProblemDetail>()
//
//        context.findOneByLogin(defaultAccount.login!!)!!.run {
//            assertNull(activationKey)
//            assertTrue(activated)
//        }
//    }
//
//    @Test
//    fun `vérifie que la requête avec mauvaise URI renvoi la bonne URL erreur`() {
//        generateActivationKey.run {
//            client
//                .get()
//                .uri("$API_ACTIVATE_PATH$API_ACTIVATE_PARAM", this)
//                .exchange()
//                .returnResult<ProblemDetail>()
//                .url
//                .let { assertEquals(URI("$BASE_URL_DEV$API_ACTIVATE_PATH$this"), it) }
//        }
//    }
}