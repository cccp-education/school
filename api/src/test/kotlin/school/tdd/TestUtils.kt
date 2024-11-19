@file:Suppress(
    "IdentifierGrammar",
    "MemberVisibilityCanBePrivate",
    "unused",
)

package school.tdd

import org.springframework.context.ApplicationContext
import school.base.utils.Constants.ADMIN
import school.base.utils.Constants.DOMAIN_DEV_URL
import school.base.utils.Constants.ROLE_ADMIN
import school.base.utils.Constants.ROLE_ANONYMOUS
import school.base.utils.Constants.ROLE_USER
import school.base.utils.Constants.USER
import school.tdd.TestUtils.Data.displayInsertUserScript
import school.users.User
import school.users.dao.UserDao.Dao.findOne
import school.users.Signup
import java.util.regex.Pattern
import kotlin.test.assertEquals
import kotlin.test.assertTrue

object TestUtils {
    @JvmStatic
    fun main(args: Array<String>): Unit = displayInsertUserScript()

    object Data {
        const val OFFICIAL_SITE = "https://cccp-education.github.io/"
        const val DEFAULT_IMAGE_URL = "https://placehold.it/50x50"
        val admin: User by lazy { userFactory(ADMIN) }
        val user: User by lazy { userFactory(USER) }
        val users: Set<User> = setOf(admin, user)
        const val DEFAULT_USER_JSON = """{
    "login": "$USER",
    "email": "$USER@$DOMAIN_DEV_URL",
    "password": "$USER"}"""
        val signup: Signup by lazy {
            Signup(
                login = user.login,
                password = user.password,
                email = user.email,
                repassword = user.password
            )
        }

        fun userFactory(login: String): User = User(
            password = login,
            login = login,
            email = "$login@$DOMAIN_DEV_URL",
        )

        fun displayInsertUserScript() {
            "InsertUserScript :\n$insertUsersScript".run(::println)
        }

        //TODO : add methode to complete user generation
        val insertUsersScript = """
            -- Fonction pour générer des mots de passe aléatoires (ajustez la longueur si nécessaire)
            DELIMITER ${'$'}${'$'}
            CREATE FUNCTION random_password(length INT)
            RETURNS CHAR(length)
            BEGIN
                SET @chars := 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
                SET @i := 0;
                SET @pwd := '';
                WHILE @i < length DO
                    SET @pwd := CONCAT(@pwd, SUBSTRING(@chars, FLOOR(RAND() * LENGTH(@chars)) + 1, 1));
                    SET @i := @i + 1;
                END WHILE;
                RETURN @pwd;
            END ${'$'}${'$'}
            DELIMITER ;
            
            -- Insertion de 100 nouveaux utilisateurs avec des mots de passe aléatoires
            INSERT INTO `user` (`login`, `password`, `email`, `lang_key`)
            VALUES
                ('user1', random_password(10), 'user1@example.com', 'en'),
                ('user2', random_password(10), 'user2@example.com', 'fr'),
                -- ... (répéter 98 fois en remplaçant les noms d'utilisateur et les emails)
                ('user100', random_password(10), 'user100@example.com', 'es');
            
            -- Attribution du rôle "USER" à tous les nouveaux utilisateurs
            INSERT INTO `user_authority` (`user_id`, `role`)
            SELECT `id`, 'USER'
            FROM `user`
            WHERE `id` IN (
                SELECT `id`
                FROM `user`
                WHERE `login` LIKE 'user%'
            );            
        """.trimIndent()

        suspend fun ApplicationContext.assertUserExists(pairLoginEmail: Pair<String, String>) = assertEquals(
            pairLoginEmail.first,
            findOne<User>(pairLoginEmail.second).getOrNull()?.login
        )

        suspend fun ApplicationContext.assertUserNotExists(email: String) = findOne<User>(email)
            .isLeft()
            .apply(::assertTrue)

    }

    val ApplicationContext.PATTERN_LOCALE_2: Pattern
        get() = Pattern.compile("([a-z]{2})-([a-z]{2})")

    val ApplicationContext.PATTERN_LOCALE_3: Pattern
        get() = Pattern.compile("([a-z]{2})-([a-zA-Z]{4})-([a-z]{2})")

    val ApplicationContext.languages
        get() = setOf("en", "fr", "de", "it", "es")

    val ApplicationContext.defaultRoles
        get() = setOf(ROLE_ADMIN, ROLE_USER, ROLE_ANONYMOUS)

    fun ApplicationContext.checkProperty(
        property: String,
        value: String,
        injectedValue: String
    ) = property.apply {
        assertEquals(value, let(environment::getProperty))
        assertEquals(injectedValue, let(environment::getProperty))
    }
}