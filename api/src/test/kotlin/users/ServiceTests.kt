@file:Suppress("NonAsciiCharacters", "SqlResolve", "RedundantUnitReturnType")

package users

import app.Application
import app.utils.AppUtils.lsWorkingDir
import app.utils.AppUtils.lsWorkingDirProcess
import app.utils.AppUtils.toJson
import app.utils.Constants.DEVELOPMENT
import app.utils.Constants.PRODUCTION
import app.utils.Constants.STARTUP_LOG_MSG_KEY
import app.utils.Properties
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.getBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.context.MessageSource
import org.springframework.test.context.ActiveProfiles
import users.TestUtils.Data.OFFICIAL_SITE
import users.TestUtils.Data.user
import users.UserDao.Dao.countUsers
import users.UserDao.Dao.deleteAllUsersOnly
import users.security.UserRoleDao.Dao.countUserAuthority
import users.signup.Signup
import users.signup.SignupService
import users.signup.UserActivationDao.Dao.countUserActivation
import workspace.Log.i
import java.io.File
import java.nio.file.Paths
import java.util.Locale.FRENCH
import java.util.Locale.getDefault
import javax.inject.Inject
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@ActiveProfiles("test")
@SpringBootTest(
    classes = [Application::class],
    properties = ["spring.main.web-application-type=reactive"]
)
class ServiceTests {

    @Inject
    lateinit var context: ApplicationContext

    @AfterTest
    fun cleanUp(context: ApplicationContext) = runBlocking { context.deleteAllUsersOnly() }


    @Test
    fun `ConfigurationsTests - MessageSource test email_activation_greeting message fr`() = "artisan-logiciel".run {
        assertEquals(
            expected = "Cher $this",
            actual = context
                .getBean<MessageSource>()
                .getMessage(
                    "email.activation.greeting",
                    arrayOf(this),
                    FRENCH
                )
        )
    }


    @Test
    fun `ConfigurationsTests - MessageSource test message startupLog`() = context
        .getBean<MessageSource>()
        .getMessage(
            STARTUP_LOG_MSG_KEY,
            arrayOf(DEVELOPMENT, PRODUCTION),
            getDefault()
        ).run {
            i(this)
            assertEquals(buildString {
                append("You have misconfigured your application!\n")
                append("It should not run with both the $DEVELOPMENT\n")
                append("and $PRODUCTION profiles at the same time.")
            }, this)
        }


    @Test
    fun `ConfigurationsTests - test go visit message`() = assertEquals(
        OFFICIAL_SITE,
        context.getBean<Properties>().goVisitMessage
    )

    @Test
    fun `test lsWorkingDir & lsWorkingDirProcess`(): Unit = "build".let {
        it.run(::File).run {
            context
                .lsWorkingDirProcess(this)
                .run { "lsWorkingDirProcess : $this" }
                .run(::i)
            absolutePath.run(::i)
            // Liste un répertoire spécifié par une chaîne
            context.lsWorkingDir(it, maxDepth = 2)
            // Liste un répertoire spécifié par un Path
            context.lsWorkingDir(Paths.get(it))
        }
    }


    @Test
    fun `display user formatted in JSON`() = assertDoesNotThrow {
        (user to context).toJson.let(::i)
    }

    @Test
    fun `check toJson build a valid json format`(): Unit = assertDoesNotThrow {
        (user to context)
            .toJson
            .let(context.getBean<ObjectMapper>()::readTree)
    }

    @Test
    fun `test signupService signup saves user and role_user and user_activation`(): Unit = runBlocking {
        Signup(
            login = "jdoe",
            email = "jdoe@acme.com",
            password = "secr3t",
            repassword = "secr3t"
        ).run signup@{
            Triple(
                context.countUsers(),
                context.countUserAuthority(),
                context.countUserActivation()
            ).run {
                assertEquals(0, first)
                assertEquals(0, second)
                assertEquals(0, third)
                context.getBean<SignupService>().signup(this@signup)
                assertEquals(first + 1, context.countUsers())
                assertEquals(second + 1, context.countUserAuthority())
                assertEquals(third + 1, context.countUserActivation())
            }
        }
    }


////    @Test
////    fun test_findActivationKeyByLogin() {
////        assertEquals(0, countAccount(dao))
////        createDataAccounts(accounts, dao)
////        assertEquals(accounts.size, countAccount(dao))
////        assertEquals(accounts.size + 1, countAccountAuthority(dao))
////        runBlocking {
////            assertEquals(
////                findOneByEmail(defaultAccount.email!!, dao)!!.activationKey,
////                accountRepository.findActivationKeyByLogin(defaultAccount.login!!)
////            )
////        }
////    }
////
////    @Test
////    fun test_findOneByActivationKey() {
////        assertEquals(0, countAccount(dao))
////        createDataAccounts(accounts, dao)
////        assertEquals(accounts.size, countAccount(dao))
////        assertEquals(accounts.size + 1, countAccountAuthority(dao))
////        findOneByLogin(defaultAccount.login!!, dao).run findOneByLogin@{
////            assertNotNull(this@findOneByLogin)
////            assertNotNull(this@findOneByLogin.activationKey)
////            runBlocking {
////                accountRepository.findOneByActivationKey(this@findOneByLogin.activationKey!!)
////                    .run findOneByActivationKey@{
////                        assertNotNull(this@findOneByActivationKey)
////                        assertNotNull(this@findOneByActivationKey.id)
////                        assertEquals(
////                            this@findOneByLogin.id,
////                            this@findOneByActivationKey.id
////                        )
////                    }
////            }
////        }
////    }
////}
//TODO test phase book Spath P., Cosmina I., Harrop R., Schaefer C. - Pro Spring 6 with Kotlin - 2023.pdf p 456

//    @Test
//    fun `DataTestsChecks - affiche moi du json`() = run {
//        assertDoesNotThrow {
//            mapper.writeValueAsString(TestUtils.Data.users).run(::i)
//            mapper.writeValueAsString(user).run(::i)
//            DEFAULT_USER_JSON.run(::i)
//        }
//    }

//    @Test
//    fun `UserController - vérifie que la requête contient bien des données cohérentes`() {
//        client
//            .post()
//            .uri("")
//            .contentType(APPLICATION_JSON)
//            .bodyValue(user)
//            .exchange()
//            .returnResult<Unit>()
//            .requestBodyContent!!
//            .logBody()
//            .requestToString()
//            .run {
//                user.run {
//                    mapOf(
//                        UserDao.Fields.LOGIN_FIELD to login,
//                        UserDao.Fields.PASSWORD_FIELD to password,
//                        UserDao.Fields.EMAIL_FIELD to email,
//                        //FIRST_NAME_FIELD to firstName,
//                        //LAST_NAME_FIELD to lastName,
//                    ).map { (key, value) ->
//                        assertTrue {
//                            contains(key)
//                            contains(value)
//                        }
//                    }
//                }
//            }
//    }

//    @Test
//    fun `UserController - test signup avec une url invalide`(): Unit = runBlocking {
//        val countUserBefore = context.countUsers()
////        val countUserAuthBefore = context.countUserAuthority()
//        assertEquals(0, countUserBefore)
////        assertEquals(0, countUserAuthBefore)
//        client
//            .post()
//            .uri("/api/users/foobar")
//            .contentType(APPLICATION_JSON)
//            .bodyValue(user)
//            .exchange()
//            .expectStatus()
//            .isNotFound
//            .returnResult<Unit>()
//            .responseBodyContent!!
//            .logBody()
//            .isNotEmpty()
//            .let(::assertTrue)
//        assertEquals(countUserBefore, context.countUsers())
////        assertEquals(countUserBefore + 1, context.countUsers())
////        assertEquals(countUserAuthBefore + 1, context.countUserAuthority())
//        context.findOneByEmail<User>(user.email).run {
//            when (this) {
//                is Left -> assertEquals(EmptyResultDataAccessException::class.java, value::class.java)
//                is Right -> {
//                    assertEquals(user, value)
//                }
//            }
//        }
//    }

//    @Ignore
//    @Test //TODO: mock sendmail
//    fun `UserController - test signup avec un account valide`(): Unit = runBlocking {
//        val countUserBefore = context.countUsers()
//        val countUserAuthBefore = context.countUserAuthority()
//        assertEquals(0, countUserBefore)
//        assertEquals(0, countUserAuthBefore)
//        client
//            .post()
//            .uri(API_SIGNUP_PATH)
//            .contentType(APPLICATION_JSON)
//            .bodyValue(user)
//            .exchange()
//            .expectStatus()
//            .isCreated
//            .returnResult<Unit>()
//            .responseBodyContent!!
//            .logBody()
//            .isEmpty()
//            .let(::assertTrue)
//        assertEquals(countUserBefore, context.countUsers())
//        assertEquals(countUserBefore + 1, context.countUsers())
//        assertEquals(countUserAuthBefore + 1, context.countUserAuthority())
//        context.findOneByEmail<User>(user.email).run {
//            when (this) {
//                is Left -> assertEquals(value::class.java, NullPointerException::class.java)
//                is Right -> {
//                    assertEquals(user, value)
//                }
//            }
//        }
//    }

//    @Test
//    fun `UserController - test signup account validator avec login invalid`() {
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
//    fun `UserController - test signup account avec login invalid`() {
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
//    fun `UserController - test signup account avec un email invalid`() {
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
//    fun `UserController - test signup account validator avec un password invalid`() {
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
//    fun `UserController - test signup account avec un password invalid`() {
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
//    fun `UserController - test signup account avec un password null`() {
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
//    fun `UserController - test signup account activé avec un email existant`() {
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
//    fun `UserController - test signup account activé avec un login existant`() {
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
//    fun `UserController - test signup account avec un email dupliqué`() {
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
//    fun `UserController - test signup account en renseignant l'autorité admin qui sera ignoré et le champ activé qui sera mis à false`() {
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
//    fun `UserController - vérifie l'internationalisation des validations par validator factory avec mauvais login en italien`() {
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
//    fun `UserController - vérifie l'internationalisation des validations par REST avec mot de passe non conforme en francais`() {
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
//    fun `UserController - test activate avec une mauvaise clé`() {
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
//    fun `UserController - test activate avec une clé valide`() {
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
//    fun `UserController - vérifie que la requête avec mauvaise URI renvoi la bonne URL erreur`() {
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