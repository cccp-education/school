@file:Suppress("JUnitMalformedDeclaration", "SqlNoDataSourceInspection")

package webapp.users

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.validation.Validator
import kotlinx.coroutines.reactive.collect
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.getBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.r2dbc.core.awaitRowsUpdated
import org.springframework.test.context.ActiveProfiles
import tdd.TestUtils.Data.user
import tdd.TestUtils.defaultRoles
import webapp.core.model.EntityModel.Members.withId
import webapp.core.utils.i
import webapp.users.UserDao.Attributes.EMAIL_ATTR
import webapp.users.UserDao.Attributes.LANG_KEY_ATTR
import webapp.users.UserDao.Attributes.LOGIN_ATTR
import webapp.users.UserDao.Attributes.PASSWORD_ATTR
import webapp.users.UserDao.Attributes.VERSION_ATTR
import webapp.users.UserDao.Dao.countUsers
import webapp.users.UserDao.Dao.deleteAllUsersOnly
import webapp.users.UserDao.Dao.findOneByEmail
import webapp.users.UserDao.Dao.save
import webapp.users.UserDao.Relations.INSERT
import webapp.users.security.Role.RoleDao.Dao.countRoles
import kotlin.test.*


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
    fun `trying to retrieve the user id from databaseClient object`(): Unit = runBlocking {
        user.run {
            context.getBean<R2dbcEntityTemplate>()
                .databaseClient
                .sql(INSERT)
                .bind(LOGIN_ATTR, login)
                .bind(EMAIL_ATTR, email)
                .bind(PASSWORD_ATTR, password)
                .bind(LANG_KEY_ATTR, langKey)
                .bind(VERSION_ATTR, version)
                .fetch()
                .run {
                    i("Number of rows updated (user saved): ${awaitRowsUpdated()}")
                    // passer par un est si rowsUpdated == 1,
                    // alors repasser par un sql pour retrieve l'id generated par la dataBase
                    //                    one()
                    //                        .toString()
                    //                        .let(::i)
                    //                        .let {
                    //                        if (it == null) i("It's empty")
                    //                        else if (it.isEmpty()) i("It's empty")
                    //                        else i(it.toString())
                    //                    }
                }

            context.getBean<R2dbcEntityTemplate>()
                .databaseClient
                .sql("SELECT * FROM `user`")
                .fetch()
                .all()
                .collect {
                    it
                        .apply {
                            "User retrieved: " + this.entries.toString()
                        }
                        .toString()
                        .let(::i)
                }
        }
    }
}