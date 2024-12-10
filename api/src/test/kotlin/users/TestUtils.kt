@file:Suppress("MemberVisibilityCanBePrivate")

package users

import app.utils.Constants.ADMIN
import app.utils.Constants.DOMAIN_DEV_URL
import app.utils.Constants.ROLE_ADMIN
import app.utils.Constants.ROLE_ANONYMOUS
import app.utils.Constants.ROLE_USER
import app.utils.Constants.USER
import org.springframework.context.ApplicationContext
import users.TestUtils.Data.displayInsertUserScript
import users.signup.Signup
import java.util.regex.Pattern
import kotlin.test.assertEquals

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
            "InsertUserScript :\n$INSERT_USERS_SCRIPT".run(::println)
        }

        //TODO : add methode to complete user generation
        const val INSERT_USERS_SCRIPT = """
            -- Fonction pour générer des mots de passe aléatoires
            CREATE OR REPLACE FUNCTION random_password(length INT) RETURNS TEXT AS ${'$'}${'$'}
            DECLARE
                chars TEXT := 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
                pwd TEXT := '';
            BEGIN
                FOR i IN 1..length LOOP
                    pwd := pwd || substring(chars from (floor(random() * length(chars)) + 1) for 1);
                END LOOP;
                RETURN pwd;
            END;
            ${'$'}${'$'} LANGUAGE plpgsql;
            
            -- Insertion de 100 nouveaux utilisateurs avec des mots de passe aléatoires
            INSERT INTO "user" ("login", "password", "email", "lang_key")
            VALUES
                ('user1', random_password(10), 'user1@example.com', 'en'),
                ('user2', random_password(10), 'user2@example.com', 'fr'),
                -- ... (répéter 98 fois en remplaçant les noms d'utilisateur et les emails)
                ('user100', random_password(10), 'user100@example.com', 'es');
            
            -- Attribution du rôle "USER" à tous les nouveaux utilisateurs
            INSERT INTO "user_authority" ("user_id", "role")
            SELECT "id", 'USER'
            FROM "user"
            WHERE "id" IN (
                SELECT "id"
                FROM "user"
                WHERE "login" LIKE 'user%'
            );"""
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