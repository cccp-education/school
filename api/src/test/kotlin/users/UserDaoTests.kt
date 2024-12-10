@file:Suppress(
    "JUnitMalformedDeclaration",
    "SqlNoDataSourceInspection",
    "SqlDialectInspection",
    "SqlSourceToSinkFlow"
)

package users

import app.Application
import app.database.EntityModel.Members.withId
import app.utils.Constants.EMPTY_STRING
import app.utils.Constants.ROLE_USER
import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
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
import users.TestUtils.Data.user
import users.TestUtils.Data.users
import users.TestUtils.defaultRoles
import users.UserDao.Dao.countUsers
import users.UserDao.Dao.delete
import users.UserDao.Dao.deleteAllUsersOnly
import users.UserDao.Dao.findOne
import users.UserDao.Dao.findOneByEmail
import users.UserDao.Dao.findOneWithAuths
import users.UserDao.Dao.save
import users.UserDao.Dao.signup
import users.UserDao.Relations.FIND_USER_BY_LOGIN
import users.security.Role
import users.security.RoleDao.Dao.countRoles
import users.security.UserRoleDao
import users.security.UserRoleDao.Dao.countUserAuthority
import users.signup.UserActivation
import users.signup.UserActivationDao.Attributes.ACTIVATION_KEY_ATTR
import users.signup.UserActivationDao.Dao.countUserActivation
import users.signup.UserActivationDao.Dao.findUserActivationByKey
import workspace.Log.i
import java.util.*
import java.util.UUID.fromString
import javax.inject.Inject
import kotlin.test.*


@ActiveProfiles("test")
@SpringBootTest(
    classes = [Application::class], properties = ["spring.main.web-application-type=reactive"]
)
class UserDaoTests {
    @Inject
    lateinit var context: ApplicationContext

    @AfterTest
    fun cleanUp() = runBlocking { context.deleteAllUsersOnly() }

    suspend fun ApplicationContext.findAuthsByEmail(email: String): Either<Throwable, Set<Role>> = try {
        mutableSetOf<Role>().apply {
            @Suppress("SqlResolve")
            getBean<DatabaseClient>()
                .sql(
                    """
                    SELECT ua."role" 
                    FROM "user" u 
                    JOIN user_authority ua 
                    ON u.id = ua.user_id 
                    WHERE u."email" = :email;"""
                )
                .bind("email", email)
                .fetch()
                .all()
                .collect { add(Role(it["role"].toString())) }
        }.toSet().right()
    } catch (e: Throwable) {
        e.left()
    }

    suspend fun ApplicationContext.findAuthsByLogin(login: String): Either<Throwable, Set<Role>> = try {
        mutableSetOf<Role>().apply {
            @Suppress("SqlResolve")
            getBean<DatabaseClient>()
                .sql(
                    """
                    SELECT ua."role" 
                    FROM "user" u 
                    JOIN user_authority ua 
                    ON u.id = ua.user_id 
                    WHERE u."login" = :login;"""
                )
                .bind("login", login)
                .fetch()
                .all()
                .collect { add(Role(it["role"].toString())) }
        }.toSet().right()
    } catch (e: Throwable) {
        e.left()
    }

    suspend fun ApplicationContext.findUserById(id: UUID): Either<Throwable, User> = try {
        User().withId(id).copy(password = EMPTY_STRING).run user@{
            findAuthsById(id).getOrNull().run roles@{
                return if (isNullOrEmpty()) Exception("Unable to retrieve roles from user by id").left()
                else copy(roles = this@roles).right()
            }
        }
    } catch (e: Throwable) {
        e.left()
    }

    suspend fun ApplicationContext.findAuthsById(userId: UUID): Either<Throwable, Set<Role>> = try {
        mutableSetOf<Role>().apply {
            @Suppress("SqlResolve")
            getBean<DatabaseClient>()
                .sql(
                    """
                    SELECT ua."role" 
                    FROM "user" as u 
                    JOIN user_authority as ua 
                    ON u.id = ua.user_id 
                    WHERE u.id = :userId;"""
                )
                .bind("userId", userId)
                .fetch()
                .all()
                .collect { add(Role(it["role"].toString())) }
        }.toSet().right()
    } catch (e: Throwable) {
        e.left()
    }


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
               u.id, u."email", u."login";
        """.run(context.getBean<DatabaseClient>()::sql)
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
        val userId: UUID = (user to context).signup().getOrNull()!!
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
            userWithAuths = user.withId(it).copy(password = EMPTY_STRING)
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
            userWithAuths = user.withId(it).copy(password = EMPTY_STRING)
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
            userWithAuths = user.withId(it).copy(password = EMPTY_STRING)
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
            @Suppress("SqlResolve")
            context.getBean<DatabaseClient>()
                .sql(
                    """
                    SELECT ua."role" 
                    FROM "user" u 
                    JOIN user_authority ua 
                    ON u.id = ua.user_id 
                    WHERE u."email" = :email;"""
                )
                .bind("email", user.email)
                .fetch()
                .all()
                .collect { rows ->
                    assertEquals(rows["role"], ROLE_USER)
                    resultRoles.add(rows["role"].toString())
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
        }.onRight { uuid ->
            @Suppress("SqlResolve")
            context.getBean<DatabaseClient>()
                .sql(
                    """
                    SELECT ur."role" 
                    FROM user_authority AS ur 
                    WHERE ur.user_id = :userId"""
                )
                .bind("userId", uuid)
                .fetch()
                .all()
                .collect { rows ->
                    assertEquals(rows["role"], ROLE_USER)
                    resultRoles.add(Role(id = rows["role"].toString()))
                }
            assertEquals(
                ROLE_USER,
                user.withId(uuid).copy(
                    roles =
                        resultRoles
                            .map { it.id.run(::Role) }
                            .toMutableSet())
                    .roles.first().id
            )
            resultUserId = uuid
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
            //36 is the to string length of a UUID
            it.toString().apply { assertEquals(36, it.toString().length) }.apply(::i)
        }
        assertEquals(1, context.countUsers())
        assertDoesNotThrow {
            @Suppress("SqlResolve")
            context.getBean<R2dbcEntityTemplate>()
                .databaseClient
                .sql("""SELECT * FROM "user";""")
                .fetch()
                .all()
                .collect { it["id"].toString().run(UUID::fromString) }
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
        @Suppress("SqlSourceToSinkFlow")
        val userId = context.getBean<DatabaseClient>().sql(FIND_USER_BY_LOGIN)
            .bind(UserDao.Attributes.LOGIN_ATTR, user.login.lowercase())
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
        context.getBean<DatabaseClient>()
            .sql(
                """
                SELECT ua.${UserRoleDao.Fields.ID_FIELD} 
                FROM ${UserRoleDao.Relations.TABLE_NAME} AS ua 
                where ua.user_id= :userId and ua."role" = :role"""
            )
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
                .bind(UserDao.Attributes.LOGIN_ATTR, user.login.lowercase())
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

    //TODO: move this test RoleDaoTests
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
        ids.forEach { context.delete(it) }
        assertEquals(countUserBefore, context.countUsers())
        assertEquals(countUserAuthBefore, context.countUserAuthority())
    }

    @Test
    fun `test create userActivation inside signup`(): Unit = runBlocking {
        val countUserBefore = context.countUsers()
        val countUserAuthBefore = context.countUserAuthority()
        val countUserActivationBefore = context.countUserActivation()
        (user to context).signup().apply {
            assertTrue(isRight())
            assertFalse(isLeft())
        }
        assertEquals(countUserBefore + 1, context.countUsers())
        assertEquals(countUserActivationBefore + 1, context.countUserActivation())
        assertEquals(countUserAuthBefore + 1, context.countUserAuthority())
    }

    @Test
    fun `test find userActivation by key`(): Unit = runBlocking {

        val countUserBefore = context.countUsers().apply { assertEquals(0, this) }

        val countUserAuthBefore = context.countUserAuthority()
        val countUserActivationBefore = context.countUserActivation()
        UserActivation(id = (user to context).signup().getOrNull()!!).run {
            assertEquals(countUserBefore + 1, context.countUsers())
            assertEquals(countUserAuthBefore + 1, context.countUserAuthority())
            assertEquals(countUserActivationBefore + 1, context.countUserActivation())
            activationKey
                .apply(::i)
                .isBlank()
                .run(::assertFalse)
//            assertEquals(
//                id,
//                context.findUserActivationByKey(activationKey).getOrNull()!!.id
//            )
            context.findUserActivationByKey(activationKey).getOrNull().toString().run(::i)
            // BabyStepping to find an implementation and debugging
//            assertDoesNotThrow {
//                id.toString().run(::i)
//                activationKey.run(::i)
//                context.getBean<TransactionalOperator>().executeAndAwait {
//                    """
//                        SELECT * FROM ${UserActivationDao.Relations.TABLE_NAME}
//                        WHERE $ACTIVATION_KEY_FIELD = :$ACTIVATION_KEY_ATTR
//                        """.trimIndent()
//                """
//                        SELECT * FROM user_activation
//                        WHERE activation_key = :$ACTIVATION_KEY_ATTR
//                        """.trimIndent()
//                    .run(context.getBean<R2dbcEntityTemplate>().databaseClient::sql)
//                    .bind(ACTIVATION_KEY_ATTR, activationKey)
//                    .fetch()
//                    .awaitSingle()
//                    .apply(::assertNotNull)
//                    .apply { toString().run(::i) }
//                    .let {
////                        UserActivation(
////                            id = UserActivationDao.Fields.ID_FIELD
////                                .cleanField()
////                                .uppercase()
////                                .run(it::get)
////                                .toString()
////                                .run(UUID::fromString),
////                            activationKey = ACTIVATION_KEY_FIELD
////                                .cleanField()
////                                .uppercase()
////                                .run(it::get)
////                                .toString(),
////                            createdDate = CREATED_DATE_FIELD
////                                .cleanField()
////                                .uppercase()
////                                .run(it::get)
////                                .toString()
////                                .run(java.time.LocalDateTime::parse)
////                                .toInstant(java.time.ZoneOffset.UTC),
////                            activationDate = ACTIVATION_DATE_FIELD
////                                .cleanField()
////                                .uppercase()
////                                .run(it::get)
////                                .run {
////                                    when {
////                                        this == null || toString().lowercase() == "null" -> null
////                                        else -> toString().run(java.time.LocalDateTime::parse).toInstant(java.time.ZoneOffset.UTC)
////                                    }
////                                },
////                        )
//                    }
//                    .toString().run(::i)
//            }
        }
    }
}
//}