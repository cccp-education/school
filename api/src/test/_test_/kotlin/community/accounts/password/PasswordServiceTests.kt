@file:Suppress("NonAsciiCharacters")

package community.accounts.password

import community.core.logging.i
import community.defaultAccount
import community.deleteAllAccounts
import community.initActivatedDefaultAccount
import community.launcher
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.springframework.beans.factory.getBean
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.OK
import kotlin.test.Test
import kotlin.test.assertEquals
@kotlin.test.Ignore
class PasswordServiceTests {
    private lateinit var context: ConfigurableApplicationContext

    @BeforeAll
    fun `lance le server en profile test`() {
        context = launcher()
    }

    @AfterAll
    fun `arrête le serveur`() = context.close()

    @AfterEach
    fun tearDown() = context.deleteAllAccounts()

    @Test
    fun `requestPasswordReset(email), l'email n'existe pas`(): Unit = runBlocking {
        context.initActivatedDefaultAccount()
        assertEquals(context
            .getBean<PasswordService>()
            .reset("foo@localhost")
            .first
            .statusCode, BAD_REQUEST
        )
    }
    @Test
    fun `requestPasswordReset(email), l'email a un format invalide`(): Unit = runBlocking {
        context.initActivatedDefaultAccount()
        assertEquals(context
            .getBean<PasswordService>()
            .reset("foo#localhost")
            .first
            .statusCode, BAD_REQUEST
        )
    }
    @Test
    fun `requestPasswordReset(email), l'email a une taille trop petite`(): Unit = runBlocking {
        context.initActivatedDefaultAccount()
        assertEquals(context
            .getBean<PasswordService>()
            .reset("foo@localhost")
            .first
            .statusCode, BAD_REQUEST
        )
    }
    @Test
    fun `requestPasswordReset(email), l'email a une taille trop grande`(): Unit = runBlocking {
        context.initActivatedDefaultAccount()
        assertEquals(context
            .getBean<PasswordService>()
            .reset("foo@localhost")
            .first
            .statusCode, BAD_REQUEST
        )
    }

    @Test
    fun `requestPasswordReset(email), l'email existe`(): Unit = runBlocking {
        context.initActivatedDefaultAccount()
        with(context.getBean<PasswordService>().reset(defaultAccount.email!!)) {
            //TODO: assertion sur la key, null avant et non null apres la request
            assertEquals(first.statusCode, OK)
            //            assertNotNull(this)
//            assertEquals(email!!, defaultAccount.email)
//            assertEquals(login!!, defaultAccount.login)
        }
    }

    @Test
    fun `changePassword(), le currentPassword est invalid`() {
        i("changePassword(): le currentPassword est invalid")
    }

    @Test
    fun `changePassword(), le newPassword est invalid`() {
        i("le newPassword(): est invalid")
    }

    @Test
    fun `changePassword(), le currentPassword ne match pas`() {
        i("changePassword(): le currentPassword ne match pas")

    }

    @Test
    fun `changePassword(), le currentPassword et le newPassword sont valide`() {
        i("changePassword(): le currentPassword et le newPassword sont valide")
    }

    @Test
    fun `completePasswordReset(), le newPassword n'est pas valide`() {
        i("completePasswordReset(): le currentPassword et le newPassword sont valide")
    }

    @Test
    fun `completePasswordReset(), la key n'existe pas`() {
        i("completePasswordReset(): la key n'existe pas")
    }

    @Test
    fun `completePasswordReset(), le newPassword est valide & la key existe`() {
        i("completePasswordReset(): le newPassword est valide & la key existe")
    }
}