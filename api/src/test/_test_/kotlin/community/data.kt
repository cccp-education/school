@file:Suppress(
    "unused",
    "HttpUrlsUsage",
    "MemberVisibilityCanBePrivate",
    "NonAsciiCharacters"
)

package community

import com.fasterxml.jackson.databind.ObjectMapper
import community.accounts.Account
import community.core.logging.i
import org.apache.commons.lang3.StringUtils.stripAccents
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.springframework.beans.factory.getBean
import org.springframework.context.ConfigurableApplicationContext
import java.text.Normalizer.Form.NFD
import java.text.Normalizer.normalize
import kotlin.test.Test

val systemAccount by lazy { accountCredentialsFactory(SYSTEM_USER) }
val adminAccount by lazy { accountCredentialsFactory(ADMIN) }
val defaultAccount by lazy { accountCredentialsFactory(USER) }
val threeAccounts = Triple(systemAccount, adminAccount, defaultAccount)

const val defaultAccountJson = """{
    "login": "$USER",
    "firstName": "$USER",
    "lastName": "$USER",
    "email": "$USER@$DOMAIN_DEV_URL",
    "imageUrl": "http://placehold.it/50x50"
}"""

fun accountCredentialsFactory(login: String): Account = Account(
    login = login,
    firstName = login,
    lastName = login,
    email = "$login@$DOMAIN_DEV_URL",
    imageUrl = "http://placehold.it/50x50",
)

@kotlin.test.Ignore
internal class DataTestsChecks {

    private lateinit var context: ConfigurableApplicationContext

    @BeforeAll
    fun `lance le server en profile test`() {
        context = launcher()
    }

    @AfterAll
    fun `arrête le serveur`() = context.close()


    @Test
    fun `affiche moi du json`() {
        i(context.getBean<ObjectMapper>().writeValueAsString(threeAccounts))
        i(context.getBean<ObjectMapper>().writeValueAsString(defaultAccount))
        i(defaultAccountJson)
    }
}


val writers = listOf(
    "Karl Marx",
    "Jean-Jacques Rousseau",
    "Victor Hugo",
    "Platon",
    "René Descartes",
    "Socrate",
    "Homère",
    "Paul Verlaine",
    "Claude Roy",
    "Bernard Friot",
    "François Bégaudeau",
    "Frederic Lordon",
    "Antonio Gramsci",
    "Georg Lukacs",
    "Franz Kafka",
    "Arthur Rimbaud",
    "Gérard de Nerval",
    "Paul Verlaine",
    "Dominique Pagani",
    "Rocé",
    "Chrétien de Troyes",
    "François Rabelais",
    "Montesquieu",
    "Georg Hegel",
    "Friedrich Engels",
    "Voltaire",
    "Michel Clouscard",
    "Houria Bouteldja"
)

/**
 *
 */
@Suppress("unused")
fun Array<String>.nameToLogin() = map {
    stripAccents(it.lowercase().replace(' ', '.'))
}.toTypedArray()

/**
 *
 */
@Suppress("MemberVisibilityCanBePrivate", "SpellCheckingInspection")
fun CharSequence.unaccent() = "\\p{InCombiningDiacriticalMarks}+"
    .toRegex()
    .replace(normalize(this, NFD), "")

/**
 *
 */
fun Array<String>.nameToLoginNormalizer() = map {
    it.lowercase().replace(' ', '.').unaccent()
}.toTypedArray()

fun nameToLogin(userList: List<String>): List<String> = userList.map { s ->
    stripAccents(s.lowercase().replace(' ', '.'))
}
