@file:Suppress(
    "JUnitMalformedDeclaration",
    "SqlNoDataSourceInspection",
    "SqlDialectInspection",
    "SqlSourceToSinkFlow"
)

package users

import app.Application
import app.database.EntityModel.Companion.MODEL_FIELD_FIELD
import app.database.EntityModel.Companion.MODEL_FIELD_MESSAGE
import app.database.EntityModel.Companion.MODEL_FIELD_OBJECTNAME
import app.database.EntityModel.Members.withId
import app.utils.Constants.EMPTY_STRING
import app.utils.Constants.ROLE_USER
import arrow.core.Either
import arrow.core.getOrElse
import jakarta.validation.Validator
import kotlinx.coroutines.reactive.collect
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.getBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitSingle
import org.springframework.r2dbc.core.awaitSingleOrNull
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import users.TestUtils.Data.signup
import users.TestUtils.Data.user
import users.TestUtils.Data.users
import users.TestUtils.defaultRoles
import users.TestUtils.findAuthsByEmail
import users.TestUtils.findAuthsByLogin
import users.TestUtils.findUserActivationByKey
import users.TestUtils.findUserById
import users.TestUtils.tripleCounts
import users.UserDao.Attributes.EMAIL_ATTR
import users.UserDao.Attributes.LOGIN_ATTR
import users.UserDao.Attributes.PASSWORD_ATTR
import users.UserDao.Dao.countUsers
import users.UserDao.Dao.delete
import users.UserDao.Dao.deleteAllUsersOnly
import users.UserDao.Dao.findOne
import users.UserDao.Dao.findOneByEmail
import users.UserDao.Dao.findOneWithAuths
import users.UserDao.Dao.save
import users.UserDao.Dao.signup
import users.UserDao.Dao.signupAvailability
import users.UserDao.Relations.FIND_USER_BY_LOGIN
import users.security.Role
import users.security.RoleDao
import users.security.RoleDao.Dao.countRoles
import users.security.UserRoleDao
import users.security.UserRoleDao.Dao.countUserAuthority
import users.signup.Signup
import users.signup.Signup.Companion.objectName
import users.signup.SignupService.Companion.SIGNUP_AVAILABLE
import users.signup.SignupService.Companion.SIGNUP_EMAIL_NOT_AVAILABLE
import users.signup.SignupService.Companion.SIGNUP_LOGIN_AND_EMAIL_NOT_AVAILABLE
import users.signup.SignupService.Companion.SIGNUP_LOGIN_NOT_AVAILABLE
import users.signup.UserActivation
import users.signup.UserActivationDao
import users.signup.UserActivationDao.Attributes.ACTIVATION_KEY_ATTR
import users.signup.UserActivationDao.Dao.activateUser
import users.signup.UserActivationDao.Dao.countUserActivation
import users.signup.UserActivationDao.Fields.ACTIVATION_DATE_FIELD
import users.signup.UserActivationDao.Fields.ACTIVATION_KEY_FIELD
import users.signup.UserActivationDao.Fields.CREATED_DATE_FIELD
import users.signup.UserActivationDao.Relations.FIND_ALL_USERACTIVATION
import users.signup.UserActivationDao.Relations.FIND_BY_ACTIVATION_KEY
import workspace.Log.i
import java.time.LocalDateTime
import java.time.ZoneOffset.UTC
import java.util.*
import java.util.UUID.fromString
import javax.inject.Inject
import kotlin.test.*


@ActiveProfiles("test")
@SpringBootTest(
    classes = [Application::class],
    properties = ["spring.main.web-application-type=reactive"]
)
class DaoTests {
    @Inject
    lateinit var context: ApplicationContext

    @AfterTest
    fun cleanUp() = runBlocking { context.deleteAllUsersOnly() }


    @Test
    fun `test findOneWithAuths using one query`(): Unit = runBlocking {
        assertEquals(0, context.countUsers())
        assertEquals(0, context.countUserAuthority())
        (user to context).signup()
        assertEquals(1, context.countUsers())
        assertEquals(1, context.countUserAuthority())
        """
            SELECT 
               u.id,
               u."email",
               u."login",
               u."password",
               u.lang_key,
               u.version,
               STRING_AGG(DISTINCT a."role", ',') AS roles
            FROM "user" AS u
            LEFT JOIN 
               user_authority ua ON u.id = ua.user_id
            LEFT JOIN 
               authority AS a ON ua."role" = a."role"
            WHERE 
               LOWER(u."email") = LOWER(:emailOrLogin) 
               OR 
               LOWER(u."login") = LOWER(:emailOrLogin)
            GROUP BY 
               u.id, u."email", u."login";"""
            .trimIndent()
            .run(context.getBean<DatabaseClient>()::sql)
            .bind("emailOrLogin", user.email)
            .fetch()
            .awaitSingleOrNull()
            ?.run {
                toString().run(::i)
                val expectedUserResult = User(
                    id = fromString(get("id").toString()),
                    email = get("email").toString(),
                    login = get("login").toString(),
                    roles = get("roles")
                        .toString()
                        .split(",")
                        .map { Role(it) }
                        .toSet(),
                    password = get("password").toString(),
                    langKey = get("lang_key").toString(),
                    version = get("version").toString().toLong(),
                )
                val userResult = context
                    .findOneWithAuths<User>(user.login)
                    .getOrNull()
                assertNotNull(expectedUserResult)
                assertNotNull(expectedUserResult.id)
                assertTrue(expectedUserResult.roles.isNotEmpty())
                assertEquals(
                    expectedUserResult.roles.first().id,
                    ROLE_USER
                )
                assertEquals(
                    1,
                    expectedUserResult.roles.size
                )
                assertEquals(expectedUserResult, userResult)
                assertEquals(
                    user.withId(expectedUserResult.id!!)
                        .copy(roles = setOf(Role(ROLE_USER))),
                    userResult
                )
            }
    }

    @Test
    fun `test findOneWithAuths`(): Unit = runBlocking {
        assertEquals(0, context.countUsers())
        assertEquals(0, context.countUserAuthority())
        val userId: UUID = (user to context).signup().getOrNull()!!.first
        userId.apply { run(::assertNotNull) }
            .run { "(user to context).signup() : $this" }
            .run(::println)
        assertEquals(1, context.countUsers())
        assertEquals(1, context.countUserAuthority())
        context.findOneWithAuths<User>(user.email)
            .getOrNull()
            .apply {
                run(::assertNotNull)
                assertEquals(1, this?.roles?.size)
                assertEquals(ROLE_USER, this?.roles?.first()?.id)
                assertEquals(userId, this?.id)
            }.run { "context.findOneWithAuths<User>(${user.email}).getOrNull() : $this" }
            .run(::println)
        context.findOne<User>(user.email).getOrNull()
            .run { "context.findOne<User>(user.email).getOrNull() : $this" }
            .run(::println)
        context.findAuthsByEmail(user.email).getOrNull()
            .run { "context.findAuthsByEmail(${user.email}).getOrNull() : $this" }
            .run(::println)
    }

    @Test
    fun `test findOne`(): Unit = runBlocking {
        assertEquals(0, context.countUsers())
        (user to context).save()
        assertEquals(1, context.countUsers())
        val findOneEmailResult: Either<Throwable, User> = context.findOne<User>(user.email)
        findOneEmailResult.map { assertDoesNotThrow { fromString(it.toString()) } }
        println("findOneEmailResult : ${findOneEmailResult.getOrNull()}")
        context.findOne<User>(user.login).map { assertDoesNotThrow { fromString(it.toString()) } }
    }

    @Test
    fun `test findUserById`(): Unit = runBlocking {
        val countUserBefore = context.countUsers()
        assertEquals(0, countUserBefore)
        val countUserAuthBefore = context.countUserAuthority()
        assertEquals(0, countUserAuthBefore)
        lateinit var userWithAuths: User
        (user to context).signup().apply {
            isRight().run(::assertTrue)
            isLeft().run(::assertFalse)
        }.map {
            userWithAuths = user.withId(it.first).copy(password = EMPTY_STRING)
            userWithAuths.roles.isEmpty().run(::assertTrue)
        }
        userWithAuths.id.run(::assertNotNull)
        assertEquals(1, context.countUsers())
        assertEquals(1, context.countUserAuthority())
        val userResult = context.findUserById(userWithAuths.id!!)
            .getOrNull()
            .apply { run(::assertNotNull) }
            .apply { userWithAuths = userWithAuths.copy(roles = this?.roles ?: emptySet()) }
        (userResult to userWithAuths).run {
            assertEquals(first?.id, second.id)
            assertEquals(first?.roles?.size, second.roles.size)
            assertEquals(first?.roles?.first(), second.roles.first())
        }
        userWithAuths.roles.isNotEmpty().run(::assertTrue)
        assertEquals(ROLE_USER, userWithAuths.roles.first().id)
        "userWithAuths : $userWithAuths".run(::println)
        "userResult : $userResult".run(::println)
    }

    @Test
    fun `test findAuthsByLogin`(): Unit = runBlocking {
        val countUserBefore = context.countUsers()
        assertEquals(0, countUserBefore)
        val countUserAuthBefore = context.countUserAuthority()
        assertEquals(0, countUserAuthBefore)
        lateinit var userWithAuths: User
        (user to context).signup().apply {
            isRight().run(::assertTrue)
            isLeft().run(::assertFalse)
        }.map {
            userWithAuths = user.withId(it.first).copy(password = EMPTY_STRING)
            userWithAuths.roles.isEmpty().run(::assertTrue)
        }
        assertEquals(1, context.countUsers())
        assertEquals(1, context.countUserAuthority())
        context.findAuthsByLogin(user.login)
            .getOrNull()
            .apply { run(::assertNotNull) }
            .run { userWithAuths = userWithAuths.copy(roles = this!!) }
        userWithAuths.roles.isNotEmpty().run(::assertTrue)
        assertEquals(ROLE_USER, userWithAuths.roles.first().id)
        "userWithAuths : $userWithAuths".run(::println)
    }

    @Test
    fun `test findAuthsByEmail`(): Unit = runBlocking {
        val countUserBefore = context.countUsers()
        assertEquals(0, countUserBefore)
        val countUserAuthBefore = context.countUserAuthority()
        assertEquals(0, countUserAuthBefore)
        lateinit var userWithAuths: User
        (user to context).signup().apply {
            isRight().run(::assertTrue)
            isLeft().run(::assertFalse)
        }.map {
            userWithAuths = user.withId(it.first).copy(password = EMPTY_STRING)
            userWithAuths.roles.isEmpty().run(::assertTrue)
        }
        assertEquals(1, context.countUsers())
        assertEquals(1, context.countUserAuthority())
        context.findAuthsByEmail(user.email)
            .getOrNull()
            .apply { run(::assertNotNull) }
            .run { userWithAuths = userWithAuths.copy(roles = this!!) }
        userWithAuths.roles.isNotEmpty().run(::assertTrue)
        assertEquals(ROLE_USER, userWithAuths.roles.first().id)
        "userWithAuths : $userWithAuths".run(::println)
    }

    @Test
    fun `test findOneWithAuths with existing email login and roles`(): Unit = runBlocking {
        val countUserBefore = context.countUsers()
        assertEquals(0, countUserBefore)
        val countUserAuthBefore = context.countUserAuthority()
        assertEquals(0, countUserAuthBefore)
        (user to context).signup()
        val resultRoles = mutableSetOf<Role>()
        context.findAuthsByEmail(user.email).run {
            resultRoles.addAll(map { it }.getOrElse { emptySet() })
        }
        assertEquals(ROLE_USER, resultRoles.first().id)
        assertEquals(ROLE_USER, resultRoles.first().id)
        assertEquals(1, context.countUsers())
        assertEquals(1, context.countUserAuthority())
    }

    @Test
    fun `try to do implementation of findOneWithAuths with existing email login and roles using composed query`(): Unit =
        runBlocking {
            val countUserBefore = context.countUsers()
            assertEquals(0, countUserBefore)
            val countUserAuthBefore = context.countUserAuthority()
            assertEquals(0, countUserAuthBefore)
            val resultRoles = mutableSetOf<String>()
            (user to context).signup()
            """
            SELECT ua."role" 
            FROM "user" u 
            JOIN user_authority ua 
            ON u.id = ua.user_id 
            WHERE u."email" = :email;"""
                .trimIndent()
                .run(context.getBean<DatabaseClient>()::sql)
                .bind("email", user.email)
                .fetch()
                .all()
                .collect { rows ->
                    assertEquals(rows[RoleDao.Fields.ID_FIELD], ROLE_USER)
                    resultRoles.add(rows[RoleDao.Fields.ID_FIELD].toString())
                }
            assertEquals(ROLE_USER, resultRoles.first())
            assertEquals(ROLE_USER, resultRoles.first())
            assertEquals(1, context.countUsers())
            assertEquals(1, context.countUserAuthority())
        }

    @Test
    fun `try to do implementation of findOneWithAuths with existing email login and roles`(): Unit = runBlocking {
        val countUserBefore = context.countUsers()
        assertEquals(0, countUserBefore)
        val countUserAuthBefore = context.countUserAuthority()
        assertEquals(0, countUserAuthBefore)
        val resultRoles = mutableSetOf<Role>()
        val findAuthsAnswer: Any?//= Either<Throwable,Set<String>>()
        lateinit var resultUserId: UUID
        (user to context).signup().apply {
            assertTrue(isRight())
            assertFalse(isLeft())
        }.onRight { signupResult ->
            """
            SELECT ur."role" 
            FROM user_authority AS ur 
            WHERE ur.user_id = :userId"""
                .trimIndent()
                .run(context.getBean<DatabaseClient>()::sql)
                .bind(UserRoleDao.Attributes.USER_ID_ATTR, signupResult.first)
                .fetch()
                .all()
                .collect { rows ->
                    assertEquals(rows[RoleDao.Fields.ID_FIELD], ROLE_USER)
                    resultRoles.add(Role(id = rows[RoleDao.Fields.ID_FIELD].toString()))
                }
            assertEquals(
                ROLE_USER,
                user.withId(signupResult.first).copy(
                    roles =
                        resultRoles
                            .map { it.id.run(::Role) }
                            .toMutableSet())
                    .roles.first().id
            )
            resultUserId = signupResult.first
        }
        assertEquals(
            resultUserId.toString().length,
            "85b34d71-ef1d-41e0-acc1-00ab4ee1f932".length
        )//TODO : assertnotthrow with fromStringToUuid
        assertEquals(ROLE_USER, resultRoles.first().id)
        assertEquals(1, context.countUsers())
        assertEquals(1, context.countUserAuthority())
    }

    @Test
    fun `test signup and trying to retrieve the user id from databaseClient object`(): Unit = runBlocking {
        assertEquals(0, context.countUsers())
        (user to context).signup().onRight {
            //Because 36 == UUID.toString().length
            it.toString().apply { assertEquals(36, it.first.toString().length) }.apply(::i)
        }
        assertEquals(1, context.countUsers())
        assertDoesNotThrow {
            """SELECT * FROM "user";"""
                .trimIndent()
                .run(context.getBean<R2dbcEntityTemplate>().databaseClient::sql)
                .fetch()
                .all()
                .collect { it[UserDao.Fields.ID_FIELD].toString().run(UUID::fromString) }
        }
    }

    @Test
    fun `test UserRoleDao signup with existing user without user_role`(): Unit = runBlocking {
        val countUserBefore = context.countUsers()
        assertEquals(0, countUserBefore)
        val countUserAuthBefore = context.countUserAuthority()
        assertEquals(0, countUserAuthBefore)
        val userSaveResult = (user to context).save()
        assertEquals(countUserBefore + 1, context.countUsers())
        userSaveResult//TODO: Problem with the either result do not return the user id but persist it on database
            .map { i("on passe ici!") }
            .mapLeft { i("on passe par la!") }

        val userId = context.getBean<DatabaseClient>().sql(FIND_USER_BY_LOGIN)
            .bind(LOGIN_ATTR, user.login.lowercase())
            .fetch()
            .one()
            .awaitSingle()[UserDao.Attributes.ID_ATTR]
            .toString()
            .run(UUID::fromString)

        context.getBean<DatabaseClient>()
            .sql(UserRoleDao.Relations.INSERT)
            .bind(UserRoleDao.Attributes.USER_ID_ATTR, userId)
            .bind(UserRoleDao.Attributes.ROLE_ATTR, ROLE_USER)
            .fetch()
            .one()
            .awaitSingleOrNull()

        """
        SELECT ua.${UserRoleDao.Fields.ID_FIELD} 
        FROM ${UserRoleDao.Relations.TABLE_NAME} AS ua 
        where ua.user_id= :userId and ua."role" = :role"""
            .trimIndent()
            .run(context.getBean<DatabaseClient>()::sql)
            .bind("userId", userId)
            .bind("role", ROLE_USER)
            .fetch()
            .one()
            .awaitSingle()["id"]
            .toString()
            .let { "user_role_id : $it" }
            .run(::i)

        assertEquals(countUserAuthBefore + 1, context.countUserAuthority())
    }

    @Test
    fun `check findOneByEmail with non-existent email`(): Unit = runBlocking {
        assertEquals(
            0,
            context.countUsers(),
            "context should not have a user recorded in database"
        )
        context.findOneByEmail<User>("user@dummy.com").apply {
            assertFalse(isRight())
            assertTrue(isLeft())
        }.mapLeft { assertTrue(it is EmptyResultDataAccessException) }
    }

    @Test
    fun `check findOneByEmail with existant email`(): Unit = runBlocking {
        assertEquals(
            0,
            context.countUsers(),
            "context should not have a user recorded in database"
        )
        (user to context).save()
        assertEquals(
            1,
            context.countUsers(),
            "context should have only one user recorded in database"
        )

        context.findOneByEmail<User>(user.email).apply {
            assertTrue(isRight())
            assertFalse(isLeft())
        }.map { assertDoesNotThrow { fromString(it.toString()) } }
    }

    @Test
    fun `test findOne with not existing email or login`(): Unit = runBlocking {
        assertEquals(0, context.countUsers())
        context.findOne<User>(user.email).apply {
            assertFalse(isRight())
            assertTrue(isLeft())
        }
        context.findOne<User>(user.login).apply {
            assertFalse(isRight())
            assertTrue(isLeft())
        }
    }

    @Test
    fun `save default user should work in this context `(): Unit = runBlocking {
        val count = context.countUsers()
        (user to context).save()
        assertEquals(expected = count + 1, context.countUsers())
    }

    @Test
    fun `test retrieve id from user by existing login`(): Unit = runBlocking {
        val countUserBefore = context.countUsers()
        assertEquals(0, countUserBefore)
        val countUserAuthBefore = context.countUserAuthority()
        assertEquals(0, countUserAuthBefore)
        (user to context).save()
        assertEquals(countUserBefore + 1, context.countUsers())
        assertDoesNotThrow {
            FIND_USER_BY_LOGIN
                .run(context.getBean<DatabaseClient>()::sql)
                .bind(LOGIN_ATTR, user.login.lowercase())
                .fetch()
                .one()
                .awaitSingle()[UserDao.Attributes.ID_ATTR]
                .toString()
                .run(UUID::fromString)
                .run { i("UserId : $this") }
        }
    }

    @Test
    fun `count users, expected 0`(): Unit = runBlocking {
        assertEquals(
            0,
            context.countUsers(),
            "because init sql script does not inserts default users."
        )
    }

    @Test
    fun `count roles, expected 3`(): Unit = runBlocking {
        context.run {
            assertEquals(
                defaultRoles.size,
                countRoles(),
                "Because init sql script does insert default roles."
            )
        }
    }

    @Test
    fun test_deleteAllUsersOnly(): Unit = runBlocking {
        val countUserBefore = context.countUsers()
        val countUserAuthBefore = context.countUserAuthority()
        users.forEach { (it to context).signup() }
        assertEquals(countUserBefore + 2, context.countUsers())
        assertEquals(countUserAuthBefore + 2, context.countUserAuthority())
        context.deleteAllUsersOnly()
        assertEquals(countUserBefore, context.countUsers())
        assertEquals(countUserAuthBefore, context.countUserAuthority())
    }

    @Test
    fun test_delete(): Unit = runBlocking {
        val countUserBefore = context.countUsers()
        val countUserAuthBefore = context.countUserAuthority()
        val ids = users.map { (it to context).signup().getOrNull()!! }
        assertEquals(countUserBefore + 2, context.countUsers())
        assertEquals(countUserAuthBefore + 2, context.countUserAuthority())
        ids.forEach { context.delete(it.first) }
        assertEquals(countUserBefore, context.countUsers())
        assertEquals(countUserAuthBefore, context.countUserAuthority())
    }

    @Test
    fun `signupAvailability should return SIGNUP_AVAILABLE for all when login and email are available`() = runBlocking {
        (Signup(
            "testuser",
            "password",
            "password",
            "testuser@example.com"
        ) to context).signupAvailability().run {
            isRight().run(::assertTrue)
            assertEquals(SIGNUP_AVAILABLE, getOrNull()!!)
        }
    }

    @Test
    fun `signupAvailability should return SIGNUP_NOT_AVAILABLE_AGAINST_LOGIN_AND_EMAIL for all when login and email are not available`(): Unit =
        runBlocking {
            assertEquals(0, context.countUsers())
            (user to context).save()
            assertEquals(1, context.countUsers())
            (signup to context).signupAvailability().run {
                assertEquals(
                    SIGNUP_LOGIN_AND_EMAIL_NOT_AVAILABLE,
                    getOrNull()!!
                )
            }
        }

    @Test
    fun `signupAvailability should return SIGNUP_EMAIL_NOT_AVAILABLE when only email is not available`() = runBlocking {
        assertEquals(0, context.countUsers())
        (user to context).save()
        assertEquals(1, context.countUsers())
        (Signup(
            "testuser",
            "password",
            "password",
            user.email
        ) to context).signupAvailability().run {
            assertEquals(SIGNUP_EMAIL_NOT_AVAILABLE, getOrNull()!!)
        }
    }

    @Test
    fun `signupAvailability should return SIGNUP_LOGIN_NOT_AVAILABLE when only login is not available`() = runBlocking {
        assertEquals(0, context.countUsers())
        (user to context).save()
        assertEquals(1, context.countUsers())
        (Signup(
            user.login,
            "password",
            "password",
            "testuser@example.com"
        ) to context).signupAvailability().run {
            assertEquals(SIGNUP_LOGIN_NOT_AVAILABLE, getOrNull()!!)
        }
    }

    @Test
    fun `check signup validate implementation`() {
        setOf(PASSWORD_ATTR, EMAIL_ATTR, LOGIN_ATTR)
            .map { it to context.getBean<Validator>().validateProperty(signup, it) }
            .flatMap { (first, second) ->
                second.map {
                    mapOf<String, String?>(
                        MODEL_FIELD_OBJECTNAME to objectName,
                        MODEL_FIELD_FIELD to first,
                        MODEL_FIELD_MESSAGE to it.message
                    )
                }
            }.toSet()
            .apply { run(::isEmpty).let(::assertTrue) }
    }

    @Test
    fun `test create userActivation inside signup`(): Unit = runBlocking {
        context.tripleCounts().run {
            (user to context).signup().apply {
                assertTrue(isRight())
                assertFalse(isLeft())
            }
            assertEquals(first + 1, context.countUsers())
            assertEquals(second + 1, context.countUserActivation())
            assertEquals(third + 1, context.countUserAuthority())
        }
    }

    @Test
    fun `test find userActivation by key`(): Unit = runBlocking {
        context.tripleCounts().run counts@{
            (user to context).signup()
                .getOrNull()!!
                .run {
                    assertEquals(this@counts.first + 1, context.countUsers())
                    assertEquals(this@counts.second + 1, context.countUserAuthority())
                    assertEquals(third + 1, context.countUserActivation())
                    second.apply(::i)
                        .isBlank()
                        .run(::assertFalse)
                    assertEquals(
                        first,
                        context.findUserActivationByKey(second).getOrNull()!!.id
                    )
                    context.findUserActivationByKey(second).getOrNull().toString().run(::i)
                    // BabyStepping to find an implementation and debugging
                    assertDoesNotThrow {
                        first.toString().run(::i)
                        second.run(::i)
                        context.getBean<TransactionalOperator>().executeAndAwait {
                            FIND_BY_ACTIVATION_KEY
                                .run(context.getBean<R2dbcEntityTemplate>().databaseClient::sql)
                                .bind(ACTIVATION_KEY_ATTR, second)
                                .fetch()
                                .awaitSingle()
                                .apply(::assertNotNull)
                                .apply { toString().run(::i) }
                                .let {
                                    UserActivation(
                                        id = UserActivationDao.Fields.ID_FIELD
                                            .run(it::get)
                                            .toString()
                                            .run(UUID::fromString),
                                        activationKey = ACTIVATION_KEY_FIELD
                                            .run(it::get)
                                            .toString(),
                                        createdDate = CREATED_DATE_FIELD
                                            .run(it::get)
                                            .toString()
                                            .run(LocalDateTime::parse)
                                            .toInstant(UTC),
                                        activationDate = ACTIVATION_DATE_FIELD
                                            .run(it::get)
                                            .run {
                                                when {
                                                    this == null || toString().lowercase() == "null" -> null
                                                    else -> toString().run(LocalDateTime::parse).toInstant(UTC)
                                                }
                                            },
                                    )
                                }.toString().run(::i)
                        }
                    }
                }
        }
    }

    @Test
    fun `test activate user by key`(): Unit = runBlocking {
        context.tripleCounts().run counts@{
            (user to context).signup().getOrNull()!!.run {
                assertEquals(
                    "null",
                    FIND_ALL_USERACTIVATION
                        .trimIndent()
                        .run(context.getBean<R2dbcEntityTemplate>().databaseClient::sql)
                        .fetch()
                        .awaitSingleOrNull()!![ACTIVATION_DATE_FIELD]
                        .toString()
                        .lowercase()
                )
                assertEquals(this@counts.first + 1, context.countUsers())
                assertEquals(this@counts.second + 1, context.countUserAuthority())
                assertEquals(third + 1, context.countUserActivation())
                "user.id : $first".run(::i)
                "activation key : $second".run(::i)
                assertEquals(1, context.activateUser(second).getOrNull()!!)
                assertEquals(this@counts.first + 1, context.countUsers())
                assertEquals(this@counts.second + 1, context.countUserAuthority())
                assertEquals(third + 1, context.countUserActivation())
                assertNotEquals(
                    "null",
                    FIND_ALL_USERACTIVATION
                        .trimIndent()
                        .run(context.getBean<R2dbcEntityTemplate>().databaseClient::sql)
                        .fetch()
                        .awaitSingleOrNull()!!
                        .apply { "user_activation : $this".run(::i) }[ACTIVATION_DATE_FIELD]
                        .toString()
                        .lowercase()
                )
            }
        }
    }
}