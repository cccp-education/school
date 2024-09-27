@file:Suppress("NonAsciiCharacters")

package webapp.signup

//
//import com.fasterxml.jackson.databind.ObjectMapper
//import jakarta.validation.Validation.byProvider
//import jakarta.validation.Validator
//import jakarta.validation.constraints.Pattern
//import jakarta.validation.constraints.Size
//import org.hibernate.validator.HibernateValidator
//import org.junit.jupiter.api.AfterAll
//import org.junit.jupiter.api.AfterEach
//import org.junit.jupiter.api.BeforeAll
//import org.springframework.beans.factory.getBean
//import org.springframework.context.ConfigurableApplicationContext
//import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
//import org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE
//import webapp.users.profile.UserProfile.UserProfileDao.Fields.FIRST_NAME_FIELD
//import webapp.users.profile.UserProfile.UserProfileDao.Fields.LAST_NAME_FIELD

//import org.springframework.http.ProblemDetail
//import org.springframework.http.ResponseEntity
//import org.springframework.test.web.reactive.server.WebTestClient
//import org.springframework.test.web.reactive.server.WebTestClient.bindToServer
//import org.springframework.test.web.reactive.server.returnResult
//import webapp.DataTests.DEFAULT_ACCOUNT_JSON
//import webapp.DataTests.accounts
//import webapp.DataTests.defaultAccount
//import webapp.accounts.models.AccountCredentials
//import webapp.accounts.models.AccountUtils.generateActivationKey
//import webapp.core.logging.i
//import webapp.core.property.*
//import java.net.URI
//import java.util.Locale.*
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.validation.Validator
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.getBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.bindToApplicationContext
import org.springframework.test.web.reactive.server.returnResult
import webapp.core.utils.i
import webapp.tests.TestTools.logBody
import webapp.tests.TestTools.requestToString
import webapp.tests.TestUtils.Data.DEFAULT_USER_JSON
import webapp.tests.TestUtils.Data.user
import webapp.tests.TestUtils.Data.users
import webapp.tests.TestUtils.countUserAuthority
import webapp.tests.TestUtils.countUsers
import webapp.tests.TestUtils.deleteAllUsersOnly
import webapp.users.User.UserDao.Fields.EMAIL_FIELD
import webapp.users.User.UserDao.Fields.LOGIN_FIELD
import webapp.users.User.UserDao.Fields.PASSWORD_FIELD
import kotlin.test.*

@SpringBootTest(properties = ["spring.main.web-application-type=reactive"])
@ActiveProfiles("test")
class SignupIntegrationTests {

    @Autowired
    lateinit var context: ApplicationContext
    lateinit var client: WebTestClient
    val mapper: ObjectMapper by lazy { context.getBean() }
    val validator: Validator by lazy { context.getBean() }
    val dao: R2dbcEntityTemplate by lazy { context.getBean() }

    @BeforeTest
    fun setUp() {
        client = context.let(::bindToApplicationContext).build()
    }

    @AfterTest
    fun cleanUp() = runBlocking { context.deleteAllUsersOnly() }

    @Test
    fun `DataTestsChecks - affiche moi du json`() = run {
        assertDoesNotThrow {
            mapper.writeValueAsString(users).run(::i)
            mapper.writeValueAsString(user).run(::i)
            DEFAULT_USER_JSON.run(::i)
        }
    }

    @Test
    fun `SignupController - vérifie que la requête contient bien des données cohérentes`() {
        client
            .post()
            .uri("")
            .contentType(APPLICATION_JSON)
            .bodyValue(user)
            .exchange()
            .returnResult<Unit>()
            .requestBodyContent!!
            .logBody()
            .requestToString()
            .run {
                user.run {
                    mapOf(
                        LOGIN_FIELD to login,
                        PASSWORD_FIELD to password,
                        EMAIL_FIELD to email,
//                        FIRST_NAME_FIELD to firstName,
//                        LAST_NAME_FIELD to lastName,
                    ).map { (key, value) ->
                        assertTrue {
                            contains(key)
                            contains(value)
                        }
                    }
                }
            }
    }


    @Test //TODO: mock sendmail
    fun `SignupController - test signup avec un account valide`() = runBlocking {
        val countUserBefore = context.countUsers()
        val countUserAuthBefore = context.countUserAuthority()
        assertEquals(0, countUserBefore)
        assertEquals(0, countUserAuthBefore)
//        client
//            .post()
//            .uri(SIGNUP_API_PATH)
//            .contentType(APPLICATION_JSON)
//            .bodyValue(user)
//            .exchange()
//            .expectStatus()
//            .isCreated
//            .returnResult<Unit>()
//            .responseBodyContent!!
//            .isEmpty()
//            .run { assertTrue(this) }
//        assertEquals(countUserBefore + 1, countAccount(dao))
//        assertEquals(countUserAuthBefore + 1, countAccountAuthority(dao))
//        findOneByEmail(defaultAccount.email!!, dao).run {
//            assertNotNull(this)
//            assertFalse(activated)
//            assertNotNull(activationKey)
//        }
    }

//    @Test
//    fun `SignupController - test signup account validator avec login invalid`() {
//        validator
//            .validateProperty(AccountCredentials(login = "funky-log(n"), LOGIN_FIELD)
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
//    fun `SignupController - test signup account avec login invalid`() {
//        assertEquals(0, countAccount(dao))
//        client
//            .post()
//            .uri(SIGNUP_API_PATH)
//            .contentType(APPLICATION_JSON)
//            .header(ACCEPT_LANGUAGE, FRENCH.language)
//            .bodyValue(defaultAccount.copy(login = "funky-log(n"))
//            .exchange()
//            .expectStatus()
//            .isBadRequest
//            .returnResult<Unit>()
//            .responseBodyContent!!
//            .logBody()
//            .isNotEmpty()
//            .run { assertTrue(this) }
//        assertEquals(0, countAccount(dao))
//    }
//
//
//    @Test
//    fun `SignupController - test signup account avec un email invalid`() {
//        val countBefore = countAccount(dao)
//        assertEquals(0, countBefore)
//        client
//            .post()
//            .uri(SIGNUP_API_PATH)
//            .contentType(APPLICATION_JSON)
//            .bodyValue(defaultAccount.copy(password = "inv"))
//            .exchange()
//            .expectStatus()
//            .isBadRequest
//            .returnResult<Unit>()
//            .responseBodyContent!!
//            .isNotEmpty()
//            .run { assertTrue(this) }
//        assertEquals(0, countBefore)
//    }
//
//    @Test
//    fun `SignupController - test signup account validator avec un password invalid`() {
//        val wrongPassword = "123"
//        validator
//            .validateProperty(AccountCredentials(password = wrongPassword), PASSWORD_FIELD)
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
//    fun `SignupController - test signup account avec un password invalid`() {
//        assertEquals(0, countAccount(dao))
//        client
//            .post()
//            .uri(SIGNUP_API_PATH)
//            .contentType(APPLICATION_JSON)
//            .bodyValue(defaultAccount.copy(password = "123"))
//            .exchange()
//            .expectStatus()
//            .isBadRequest
//            .returnResult<Unit>()
//            .responseBodyContent!!
//            .logBody()
//            .isNotEmpty()
//            .run { assertTrue(this) }
//        assertEquals(0, countAccount(dao))
//    }
//
//    @Test
//    fun `SignupController - test signup account avec un password null`() {
//        assertEquals(0, countAccount(dao))
//        client
//            .post()
//            .uri(SIGNUP_API_PATH)
//            .contentType(APPLICATION_JSON)
//            .bodyValue(defaultAccount.copy(password = null))
//            .exchange()
//            .expectStatus()
//            .isBadRequest
//            .returnResult<Unit>()
//            .responseBodyContent!!
//            .isNotEmpty()
//            .run { assertTrue(this) }
//        assertEquals(0, countAccount(dao))
//    }
//
//    @Test
//    fun `SignupController - test signup account activé avec un email existant`() {
//        assertEquals(0, countAccount(dao))
//        assertEquals(0, countAccountAuthority(dao))
//        //activation de l'account
//        createActivatedDataAccounts(setOf(defaultAccount), dao)
//        assertEquals(1, countAccount(dao))
//        assertEquals(1, countAccountAuthority(dao))
//        findOneByEmail(defaultAccount.email!!, dao).run {
//            assertNotNull(this)
//            assertTrue(activated)
//            assertNull(activationKey)
//        }
//
//        client
//            .post()
//            .uri(SIGNUP_API_PATH)
//            .contentType(APPLICATION_JSON)
//            .bodyValue(defaultAccount.copy(login = "foo"))
//            .exchange()
//            .expectStatus()
//            .isBadRequest
//            .returnResult<Unit>()
//            .responseBodyContent!!
//            .isNotEmpty()
//            .run { assertTrue(this) }
//    }
//
//
//    @Test
//    fun `SignupController - test signup account activé avec un login existant`() {
//        assertEquals(0, countAccount(dao))
//        assertEquals(0, countAccountAuthority(dao))
//        //activation de l'account
//        createActivatedDataAccounts(setOf(defaultAccount), dao)
//        findOneByEmail(defaultAccount.email!!, dao).run {
//            assertNotNull(this)
//            assertTrue(activated)
//            assertNull(activationKey)
//        }
//        assertEquals(1, countAccount(dao))
//        assertEquals(1, countAccountAuthority(dao))
//
//        client
//            .post()
//            .uri(SIGNUP_API_PATH)
//            .contentType(APPLICATION_JSON)
//            .bodyValue(defaultAccount.copy(email = "foo@localhost"))
//            .exchange()
//            .expectStatus()
//            .isBadRequest
//            .returnResult<Unit>()
//            .responseBodyContent!!
//            .isNotEmpty()
//            .run { assertTrue(this) }
//    }
//
//    @Test//TODO: mock sendmail
//    fun `SignupController - test signup account avec un email dupliqué`() {
//
//        assertEquals(0, countAccount(dao))
//        assertEquals(0, countAccountAuthority(dao))
//        // premier user
//        // sign up premier user
//        client
//            .post()
//            .uri(SIGNUP_API_PATH)
//            .contentType(APPLICATION_JSON)
//            .bodyValue(defaultAccount)
//            .exchange()
//            .expectStatus()
//            .isCreated
//            .returnResult<Unit>()
//            .responseBodyContent!!
//            .isEmpty()
//            .run { assertTrue(this) }
//        assertEquals(1, countAccount(dao))
//        assertEquals(1, countAccountAuthority(dao))
//        assertFalse(findOneByEmail(defaultAccount.email!!, dao)!!.activated)
//
//        // email dupliqué, login different
//        // sign up un second user (non activé)
//        val secondLogin = "foo"
//        client
//            .post()
//            .uri(SIGNUP_API_PATH)
//            .contentType(APPLICATION_JSON)
//            .bodyValue(defaultAccount.copy(login = secondLogin))
//            .exchange()
//            .expectStatus()
//            .isCreated
//            .returnResult<Unit>()
//            .responseBodyContent!!
//            .isEmpty()
//            .run { assertTrue(this) }
//        assertEquals(1, countAccount(dao))
//        assertEquals(1, countAccountAuthority(dao))
//        assertNull(findOneByLogin(defaultAccount.login!!, dao))
//        findOneByLogin(secondLogin, dao).run {
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
//            .uri(SIGNUP_API_PATH)
//            .contentType(APPLICATION_JSON)
//            .bodyValue(defaultAccount.copy(login = thirdLogin, email = defaultAccount.email!!.uppercase()))
//            .exchange()
//            .expectStatus()
//            .isCreated
//            .returnResult<Unit>()
//            .responseBodyContent!!
//            .isEmpty()
//            .run { assertTrue(this) }
//        assertEquals(1, countAccount(dao))
//        assertEquals(1, countAccountAuthority(dao))
//        findOneByLogin(thirdLogin, dao).run {
//            assertNotNull(this)
//            assertEquals(defaultAccount.email!!, email!!.lowercase())
//            assertFalse(activated)
//            //activation du troisieme user
//            saveAccount(copy(activated = true, activationKey = null), dao)
//        }
//        //validation que le troisieme est actif et activationKey est null
//        findOneByLogin(thirdLogin, dao).run {
//            assertNotNull(this)
//            assertTrue(activated)
//            assertNull(activationKey)
//        }
//        val fourthLogin = "baz"
//        // sign up un quatrieme user avec login different et meme email
//        // le user existant au meme mail est deja activé
//        client
//            .post()
//            .uri(SIGNUP_API_PATH)
//            .contentType(APPLICATION_JSON)
//            .bodyValue(defaultAccount.copy(login = fourthLogin))
//            .exchange()
//            .expectStatus()
//            .isBadRequest
//            .returnResult<Unit>()
//            .responseBodyContent!!
//            .isNotEmpty()
//            .run { assertTrue(this) }
//        assertEquals(1, countAccount(dao))
//        assertEquals(1, countAccountAuthority(dao))
//        assertNull(findOneByLogin(fourthLogin, dao))
//        //meme id
//        assertEquals(findOneByLogin(thirdLogin, dao).apply {
//            assertNotNull(this)
//            assertTrue(activated)
//            assertNull(activationKey)
//            assertTrue(defaultAccount.email!!.equals(email!!, true))
//        }!!.id, findOneByEmail(defaultAccount.email!!, dao).apply {
//            assertNotNull(this)
//            assertTrue(activated)
//            assertNull(activationKey)
//            assertTrue(thirdLogin.equals(login, true))
//        }!!.id
//        )
//    }
//
//    @Test//TODO: mock sendmail
//    fun `SignupController - test signup account en renseignant l'autorité admin qui sera ignoré et le champ activé qui sera mis à false`() {
//        val countUserBefore = countAccount(dao)
//        val countUserAuthBefore = countAccountAuthority(dao)
//        assertEquals(0, countUserBefore)
//        assertEquals(0, countUserAuthBefore)
//        val login = "badguy"
//        client
//            .post()
//            .uri(SIGNUP_API_PATH)
//            .contentType(APPLICATION_JSON)
//            .bodyValue(
//                AccountCredentials(
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
//        assertEquals(countUserBefore + 1, countAccount(dao))
//        assertEquals(countUserAuthBefore + 1, countAccountAuthority(dao))
//        findOneByLogin(login, dao).run {
//            assertNotNull(this)
//            assertFalse(activated)
//            assertFalse(activationKey.isNullOrBlank())
//        }
//        assertTrue(findAllAccountAuthority(dao).none {
//            it.role.equals(ROLE_ADMIN, true)
//        })
//    }
//
//    @Test
//    fun `SignupController - vérifie l'internationalisation des validations par validator factory avec mauvais login en italien`() {
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
//    fun `SignupController - vérifie l'internationalisation des validations par REST avec mot de passe non conforme en francais`() {
//        assertEquals(0, countAccount(dao))
//        client
//            .post()
//            .uri(SIGNUP_API_PATH)
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
//        assertEquals(0, countAccount(dao))
//
//    }
//
//
//    @Test
//    fun `SignupController - test activate avec une mauvaise clé`() {
//        client
//            .get()
//            .uri("$ACTIVATE_API_PATH$ACTIVATE_API_PARAM", "wrongActivationKey")
//            .exchange()
//            .expectStatus()
//            .is5xxServerError
//            .returnResult<Unit>()
//    }
//
//    @Test
//    fun `SignupController - test activate avec une clé valide`() {
//        assertEquals(0, countAccount(dao))
//        assertEquals(0, countAccountAuthority(dao))
//        createDataAccounts(setOf(defaultAccount), dao)
//        assertEquals(1, countAccount(dao))
//        assertEquals(1, countAccountAuthority(dao))
//
//        client
//            .get()
//            .uri(
//                "$ACTIVATE_API_PATH$ACTIVATE_API_PARAM",
//                findOneByLogin(defaultAccount.login!!, dao)!!.apply {
//                    assertTrue(activationKey!!.isNotBlank())
//                    assertFalse(activated)
//                }.activationKey
//            ).exchange()
//            .expectStatus()
//            .isOk
//            .returnResult<Unit>()
//
//        findOneByLogin(defaultAccount.login!!, dao)!!.run {
//            assertNull(activationKey)
//            assertTrue(activated)
//        }
//    }
//
//    @Test
//    fun `SignupController - vérifie que la requête avec mauvaise URI renvoi la bonne URL erreur`() {
//        generateActivationKey.run {
//            client
//                .get()
//                .uri("$ACTIVATE_API_PATH$ACTIVATE_API_PARAM", this)
//                .exchange()
//                .returnResult<Unit>()
//                .url
//                //when test is ran against localhost:8080
//                .let { assertEquals(URI("$BASE_URL_DEV$ACTIVATE_API_PATH$this"), it) }
////                .let { assertEquals(URI("$ACTIVATE_API_PATH$this"), it) }
//        }
//    }
}