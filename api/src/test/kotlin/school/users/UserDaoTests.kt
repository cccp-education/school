@file:Suppress(
    "JUnitMalformedDeclaration",
    "SqlNoDataSourceInspection"
)

package school.users

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import kotlinx.coroutines.reactive.collect
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.getBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitSingleOrNull
import org.springframework.test.context.ActiveProfiles
import school.base.model.EntityModel.Members.withId
import school.base.utils.EMPTY_STRING
import school.base.utils.ROLE_USER
import school.base.utils.Log.i
import school.tdd.TestUtils.Data.user
import school.tdd.TestUtils.defaultRoles
import school.users.User.UserDao
import school.users.User.UserDao.Dao.countUsers
import school.users.User.UserDao.Dao.deleteAllUsersOnly
import school.users.User.UserDao.Dao.findOne
import school.users.User.UserDao.Dao.findOneByEmail
import school.users.User.UserDao.Dao.findOneWithAuths
import school.users.User.UserDao.Dao.save
import school.users.User.UserDao.Dao.signup
import school.users.User.UserDao.Relations.FIND_USER_BY_LOGIN
import school.users.UserDaoTests.Queries.h2SQLquery
import school.users.security.Role
import school.users.security.Role.RoleDao.Dao.countRoles
import school.users.security.UserRole.UserRoleDao
import school.users.security.UserRole.UserRoleDao.Dao.countUserAuthority
import java.util.*
import java.util.UUID.fromString
import kotlin.test.*


@ActiveProfiles("test")
@SpringBootTest(properties = ["spring.main.web-application-type=reactive"])
class UserDaoTests {
    @Autowired
    lateinit var context: ApplicationContext

    @AfterTest
    fun cleanUp() = runBlocking { context.deleteAllUsersOnly() }

    object Queries {
        const val pgSQLquery = """
        SELECT 
            u.id AS user_id,
            u.email AS user_email,
            u.login AS user_login,
            array_agg(DISTINCT a.role) AS user_roles
        FROM 
            "user" u
        LEFT JOIN 
            "user_authority" ua ON u.id = ua.user_id
        LEFT JOIN 
            "authority" a ON ua.role = a.role
        WHERE 
            LOWER(u.email) = LOWER(:email) OR LOWER(u.login) = LOWER(:login)
        GROUP BY 
            u.id, u.email, u.login;
        """

        const val h2SQLquery = """
        SELECT 
            u.id,
            u.email,
            u.login,
            u.password,
            u.lang_key,
            u.version,
            `GROUP_CONCAT`(DISTINCT `a`.`role`) AS `user_roles`
        FROM `user` as `u`
        LEFT JOIN 
            `user_authority` ua ON u.id = ua.user_id
        LEFT JOIN 
            `authority` as a ON ua.role = a.role
        WHERE 
            lower(u.email) = lower(:emailOrLogin) OR lower(u.login) = lower(:emailOrLogin)
        GROUP BY 
            u.id, u.email, u.login;
            """
    }

    suspend fun ApplicationContext.findAuthsByEmail(email: String): Either<Throwable, Set<Role>> = try {
        mutableSetOf<Role>().apply {
            getBean<DatabaseClient>()
                .sql("SELECT `ua`.`role` FROM `user` `u` JOIN `user_authority` `ua` ON `u`.`id` = `ua`.`user_id` WHERE `u`.`email` = :email")
                .bind("email", email)
                .fetch()
                .all()
                .collect { add(Role(it["ROLE"].toString())) }
        }.toSet().right()
    } catch (e: Throwable) {
        e.left()
    }

    suspend fun ApplicationContext.findAuthsByLogin(login: String): Either<Throwable, Set<Role>> = try {
        mutableSetOf<Role>().apply {
            getBean<DatabaseClient>()
                .sql("SELECT `ua`.`role` FROM `user` `u` JOIN `user_authority` `ua` ON `u`.`id` = `ua`.`user_id` WHERE `u`.`login` = :login")
                .bind("login", login)
                .fetch()
                .all()
                .collect { add(Role(it["ROLE"].toString())) }
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
            getBean<DatabaseClient>()
                .sql("SELECT `ua`.`role` FROM `user` as `u` JOIN `user_authority` as `ua` ON `u`.`id` = `ua`.`user_id` WHERE `u`.`id` = :userId")
                .bind("userId", userId)
                .fetch()
                .all()
                .collect { add(Role(it["ROLE"].toString())) }
        }.toSet().right()
    } catch (e: Throwable) {
        e.left()
    }


    @Test
    fun `test findOneWithAuths with one query using h2 database`(): Unit = runBlocking {
        assertEquals(0, context.countUsers())
        assertEquals(0, context.countUserAuthority())
        (user to context).signup()
        assertEquals(1, context.countUsers())
        assertEquals(1, context.countUserAuthority())

        val userWithAuths: MutableMap<String, Any>? = context
            .getBean<DatabaseClient>()
            .sql(h2SQLquery)
            .bind("emailOrLogin", user.email)
            .fetch()
            .awaitSingleOrNull()

        userWithAuths?.run {
            val expectedUserResult = User(
                id = fromString(get("id".uppercase()).toString()),
                email = get("email".uppercase()).toString(),
                login = get("login".uppercase()).toString(),
                roles = get("user_roles".uppercase())
                    .toString()
                    .split(",")
                    .map { Role(it) }
                    .toSet(),
                password = get("password".uppercase()).toString(),
                langKey = get("lang_key".uppercase()).toString(),
                version = get("version".uppercase()).toString().toLong(),
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
            context.getBean<DatabaseClient>()
                .sql("SELECT `ua`.`role` FROM `user` `u` JOIN `user_authority` `ua` ON `u`.`id` = `ua`.`user_id` WHERE `u`.`email` = :email")
                .bind("email", user.email)
                .fetch()
                .all()
                .collect { rows ->
                    assertEquals(rows["ROLE"], ROLE_USER)
                    resultRoles.add(rows["ROLE"].toString())
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
            context.getBean<DatabaseClient>()
                .sql("SELECT ur.`role` FROM `user_authority` ur WHERE ur.`user_id` = :userId")
                .bind("userId", uuid)
                .fetch()
                .all()
                .collect { rows ->
                    assertEquals(rows["ROLE"], ROLE_USER)
                    resultRoles.add(Role(id = rows["ROLE"].toString()))
                }
            assertEquals(
                ROLE_USER,
                user.withId(uuid).copy(roles =
                resultRoles
                    .map { Role(it.id) }
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
            context.getBean<R2dbcEntityTemplate>()
                .databaseClient
                .sql("SELECT * FROM `user`")
                .fetch()
                .all()
                .collect { it["ID"].toString().run(UUID::fromString) }
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
            .awaitSingle()[UserDao.Attributes.ID_ATTR.uppercase()]
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
            .sql("SELECT ua.${UserRoleDao.Fields.ID_FIELD} FROM ${UserRoleDao.Relations.TABLE_NAME} AS ua where ua.`user_id`= :userId and ua.`role` = :role")
            .bind("userId", userId)
            .bind("role", ROLE_USER)
            .fetch()
            .one()
            .awaitSingle()["ID"]
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
            context.getBean<DatabaseClient>()
                .sql(FIND_USER_BY_LOGIN)
                .bind(UserDao.Attributes.LOGIN_ATTR, user.login.lowercase())
                .fetch()
                .one()
                .awaitSingle()[UserDao.Attributes.ID_ATTR.uppercase()]
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
}


//            suspend inline fun <reified T : EntityModel<UUID>> ApplicationContext.__findOneWithAuths__(emailOrLogin: String): Either<Throwable, User> =
//                when (T::class) {
//                    User::class -> {
//                        try {
//                            if (!((emailOrLogin to this).isThisEmail() || (emailOrLogin to this).isThisLogin()))
//                                "not a valid login or not a valid email"
//                                    .run(::Exception)
//                                    .left()
//                            //TODO: refactor à partir d'ici pour utiliser la requete avec jointures
//                            val user = findOne<User>(emailOrLogin).getOrNull()
//                            val roles: Set<Role>? = findAuthsByEmail(emailOrLogin).getOrNull()
//                            // No need for that test, let catch intercept throwable.
//                            when {
//                                user != null && roles != null -> user.copy(roles = roles).right()
//
//                                else -> Exception("not able to retrieve user id and roles").left()
//                            }
//                        } catch (e: Throwable) {
//                            e.left()
//                        }
//                    }
//
//                    else -> (T::class.simpleName)
//                        .run { "Unsupported type: $this" }
//                        .run(::IllegalArgumentException)
//                        .left()
//                }

//            //TODO: return the complete user from db with roles
//            suspend fun ApplicationContext.findAuthsByEmail(email: String): Either<Throwable, Set<Role>> = try {
//                mutableSetOf<Role>().apply {
//                    getBean<DatabaseClient>()
//                        .sql("SELECT `ua`.`role` FROM `user` `u` JOIN `user_authority` `ua` ON `u`.`id` = `ua`.`user_id` WHERE `u`.`email` = :email")
//                        .bind("email", email)
//                        .fetch()
//                        .all()
//                        .collect { add(Role(it["ROLE"].toString())) }
//                }.toSet().right()
//            } catch (e: Throwable) {
//                e.left()
//            }

////@file:Suppress("NonAsciiCharacters")
//package school.users
////package school.repository
////
////import kotlinx.coroutines.runBlocking
////import org.junit.jupiter.api.AfterAll
////import org.junit.jupiter.api.AfterEach
////import org.junit.jupiter.api.BeforeAll
////import org.springframework.beans.factory.getBean
////import org.springframework.context.ConfigurableApplicationContext
////import org.springframework.data.r2dbc.base.R2dbcEntityTemplate
////import school.*
////import school.DataTests.accounts
////import school.DataTests.defaultAccount
////import school.accounts.models.AccountUtils.generateActivationKey
////import school.accounts.models.toAccount
////import school.accounts.repository.AccountRepository
////import school.accounts.repository.AccountRepositoryR2dbc
////import school.base.property.DEFAULT_LANGUAGE
////import school.base.property.ROLE_USER
////import school.base.property.SYSTEM_USER
////import java.time.Instant
////import kotlin.test.Test
////import kotlin.test.assertEquals
////import kotlin.test.assertNotNull
////
////
////internal class AccountRepositoryR2dbcTest {
////    private lateinit var context: ConfigurableApplicationContext
////
////    private val dao: R2dbcEntityTemplate by lazy { context.getBean() }
////    private val accountRepository: AccountRepository by lazy { context.getBean<AccountRepositoryR2dbc>() }
////
////    //    @BeforeAll
//////    fun `lance le server en profile test`() = runApplication<Application> {
//////        testLoader(this)
//////    }.run { context = this }
////    @BeforeAll
////    fun `lance le server en profile test`() {
////        context = launcher()
////    }
////
////    @AfterAll
////    fun `arrête le serveur`() = context.close()
////
////
////    @AfterEach
////    fun tearDown() = deleteAllAccounts(dao)
////
////
////    @Test
////    fun test_save() = runBlocking {
////            val countBefore = countAccount(dao)
////            assertEquals(0, countBefore)
////            accountRepository.save(defaultAccount)
////            assertEquals(countBefore + 1, countAccount(dao))
////    }
////
////    @Test
////    fun test_delete() = runBlocking {
////        assertEquals(0, countAccount(dao))
////        createDataAccounts(accounts, dao)
////        assertEquals(accounts.size, countAccount(dao))
////        assertEquals(accounts.size + 1, countAccountAuthority(dao))
////        accountRepository.delete(defaultAccount.toAccount())
////        assertEquals(accounts.size - 1, countAccount(dao))
////        assertEquals(accounts.size, countAccountAuthority(dao))
////    }
////
////    @Test
////    fun test_findOne_with_Email() = runBlocking {
////        assertEquals(0, countAccount(dao))
////        createDataAccounts(accounts, dao)
////        assertEquals(accounts.size, countAccount(dao))
////        assertEquals(
////            defaultAccount.login,
////            accountRepository.findOne(defaultAccount.email!!)!!.login
////        )
////    }
////
////    @Test
////    fun test_findOne_with_Login() = runBlocking {
////        assertEquals(0, countAccount(dao))
////        createDataAccounts(accounts, dao)
////        assertEquals(accounts.size, countAccount(dao))
////        assertEquals(
////            defaultAccount.email,
////            accountRepository.findOne(defaultAccount.login!!)!!.email
////        )
////    }
////
////    @Test
////    fun test_signup() {
////        assertEquals(0, countAccount(dao))
////        assertEquals(0, countAccountAuthority(dao))
////        runBlocking {
////            accountRepository.signup(
////                defaultAccount.copy(
////                    activationKey = generateActivationKey,
////                    langKey = DEFAULT_LANGUAGE,
////                    createdBy = SYSTEM_USER,
////                    createdDate = Instant.now(),
////                    lastModifiedBy = SYSTEM_USER,
////                    lastModifiedDate = Instant.now(),
////                    authorities = mutableSetOf(ROLE_USER)
////                )
////            )
////        }
////        assertEquals(1, countAccount(dao))
////        assertEquals(1, countAccountAuthority(dao))
////    }
////
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