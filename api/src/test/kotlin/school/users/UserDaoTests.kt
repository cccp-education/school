@file:Suppress("JUnitMalformedDeclaration", "SqlNoDataSourceInspection")

package school.users

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.validation.Validator
import kotlinx.coroutines.reactive.collect
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.getBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.test.context.ActiveProfiles
import school.base.model.EntityModel.Members.withId
import school.base.tdd.TestUtils.Data.user
import school.base.tdd.TestUtils.defaultRoles
import school.base.utils.i
import school.users.User.UserDao.Dao.countUsers
import school.users.User.UserDao.Dao.deleteAllUsersOnly
import school.users.User.UserDao.Dao.findOne
import school.users.User.UserDao.Dao.findOneByEmail
import school.users.User.UserDao.Dao.save
import school.users.User.UserDao.Dao.signup
import school.users.security.Role.RoleDao.Dao.countRoles
import java.util.*
import kotlin.test.*

//import school.users.User.UserDao.Dao.signup
//import school.users.security.UserRole.UserRoleDao.Dao.signup

@SpringBootTest(properties = ["spring.main.web-application-type=reactive"])
@ActiveProfiles("test")
class UserDaoTests {

    @Autowired
    lateinit var context: ApplicationContext
    val mapper: ObjectMapper by lazy { context.getBean() }
    val validator: Validator by lazy { context.getBean() }

    @AfterTest
    fun cleanUp() = runBlocking { context.deleteAllUsersOnly() }

    @Test
    fun `count users, expected 0`() = runBlocking {
        assertEquals(
            0,
            context.countUsers(),
            "because init sql script does not inserts default users."
        )
    }

    //TODO: move this test RoleDaoTests
    @Test
    fun `count roles, expected 3`() = runBlocking {
        context.run {
            assertEquals(
                defaultRoles.size,
                countRoles(),
                "Because init sql script does insert default roles."
            )
        }
    }

    @Test
    fun `save default user should work in this context `() = runBlocking {
        val count = context.countUsers()
        (user to context).save()
        assertEquals(expected = count + 1, context.countUsers())
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
        }.map { assertEquals(it, user.withId(it.id!!)) }
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
    fun `test findOne`() = runBlocking {
        assertEquals(0, context.countUsers())
        (user to context).save()
        assertEquals(1, context.countUsers())

        context.findOne<User>(user.email).apply {
            assertTrue(isRight())
            assertFalse(isLeft())
        }.map { assertEquals(it, user.withId(it.id!!)) }

        context.findOne<User>(user.login).apply {
            assertTrue(isRight())
            assertFalse(isLeft())
        }.map { assertEquals(it, user.withId(it.id!!)) }
    }

    @Test
    fun `test findOne with not existing email or login`() = runBlocking {
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
}