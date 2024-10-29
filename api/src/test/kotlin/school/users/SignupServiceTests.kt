@file:Suppress("NonAsciiCharacters", "SqlResolve")

package school.users

import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.getBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ActiveProfiles
import school.users.signup.Signup
import school.users.User.UserDao.Dao.countUsers
import school.users.User.UserDao.Dao.deleteAllUsersOnly
import school.users.security.UserRole.UserRoleDao.Dao.countUserAuthority
import school.users.signup.SignupService
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@ActiveProfiles("test")
@SpringBootTest(properties = ["spring.main.web-application-type=reactive"])
class SignupServiceTests {

    @Autowired
    lateinit var context: ApplicationContext

    @AfterTest
    fun cleanUp(context: ApplicationContext) = runBlocking { context.deleteAllUsersOnly() }

    @Test
    fun `signupService save user and role_user`(): Unit = runBlocking {
        val countUserBefore = context.countUsers()
        assertEquals(0, countUserBefore)
        val countUserAuthBefore = context.countUserAuthority()
        assertEquals(0, countUserAuthBefore)
        context.getBean<SignupService>().signup(
            Signup(
                login = "jdoe",
                email = "jdoe@acme.com",
                password = "secr3t",
                repassword = "secr3t"
            )
        )
        assertEquals(countUserBefore + 1, context.countUsers())
        assertEquals(countUserAuthBefore + 1, context.countUserAuthority())
    }
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