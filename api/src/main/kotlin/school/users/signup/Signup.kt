package school.users.signup


import arrow.core.Either
import arrow.core.left
import arrow.core.right
import jakarta.validation.constraints.Size
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.r2dbc.core.awaitSingle
import school.users.User.UserDao
import school.users.User.UserDao.Fields.ID_FIELD
import school.users.signup.Signup.UserActivation.UserActivationDao.Fields.ACTIVATION_DATE_FIELD
import school.users.signup.Signup.UserActivation.UserActivationDao.Fields.ACTIVATION_KEY_FIELD
import school.users.signup.Signup.UserActivation.UserActivationDao.Fields.CREATED_DATE_FIELD
import java.lang.Boolean.parseBoolean
import java.time.Instant
import java.util.*


@JvmRecord
data class Signup(
    val login: String,
    val password: String,
    val repassword: String,
    val email: String,
) {
    @JvmRecord
    data class UserActivation(
        val id: UUID,
        @field:Size(max = 20)
        val activationKey: String,
        val activationDate: Instant,
        val createdDate: Instant,
    ) {
        object UserActivationDao {
            object Fields {
                const val ID_FIELD = "`id`"
                const val ACTIVATION_KEY_FIELD = "`activation_key`"
                const val ACTIVATION_DATE_FIELD = "`activation_date`"
                const val CREATED_DATE_FIELD = "`created_date`"
            }

            object Relations {
                const val TABLE_NAME = "`user_activation`"
                const val SQL_SCRIPT = """
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                $ID_FIELD                     UUID PRIMARY KEY,
                $ACTIVATION_KEY_FIELD         VARCHAR,
                $ACTIVATION_DATE_FIELD        datetime,
                $CREATED_DATE_FIELD           datetime,
            FOREIGN KEY ($ID_FIELD) REFERENCES ${UserDao.Relations.TABLE_NAME} (${UserDao.Fields.ID_FIELD})
                ON DELETE CASCADE
                ON UPDATE CASCADE);
"""
                const val INSERT = ""
//                """
//            insert into $TABLE_NAME (
//            ${Fields.LOGIN_FIELD}, ${Fields.EMAIL_FIELD}, ${Fields.PASSWORD_FIELD},
//            ${Fields.FIRST_NAME_FIELD}, ${Fields.LAST_NAME_FIELD}, ${Fields.LANG_KEY_FIELD}, ${Fields.IMAGE_URL_FIELD},
//            ${Fields.ENABLED_FIELD}, ${Fields.ACTIVATION_KEY_FIELD}, ${Fields.RESET_KEY_FIELD}, ${Fields.RESET_DATE_FIELD},
//            ${Fields.CREATED_BY_FIELD}, ${Fields.CREATED_DATE_FIELD}, ${Fields.LAST_MODIFIED_BY_FIELD}, ${Fields.LAST_MODIFIED_DATE_FIELD}, ${Fields.VERSION_FIELD})
//            values (:login, :email, :password, :firstName, :lastName,
//            :langKey, :imageUrl, :enabled, :activationKey, :resetKey, :resetDate,
//            :createdBy, :createdDate, :lastModifiedBy, :lastModifiedDate, :version)
//            """
            }

            object Dao {

                suspend fun Pair<Signup, ApplicationContext>.signupAvailability()
                        : Either<Throwable, Triple<Boolean/*OK*/,
                        Boolean/*email*/,
                        Boolean/*login*/>> = try {
                    second
                        .getBean<R2dbcEntityTemplate>()
                        .databaseClient
                        .sql(
                            """
                        SELECT
                          CASE
                            WHEN EXISTS(SELECT 1 FROM `user` WHERE LOWER(`login`) = LOWER(:login))
                            OR EXISTS(SELECT 1 FROM `user` WHERE LOWER(`email`) = LOWER(:email))
                            THEN FALSE
                            ELSE TRUE
                          END AS login_and_email_available,
                          NOT EXISTS(SELECT 1 FROM `user` WHERE LOWER(`email`) = LOWER(:email)) AS email_available,
                          NOT EXISTS(SELECT 1 FROM `user` WHERE LOWER(`login`) = LOWER(:login)) AS login_available;                    
                        """.trimIndent()
                        )
                        .bind("login", first.login)
                        .bind("email", first.email)
                        .fetch()
                        .awaitSingle()
                        .run {
                            Triple(
                                parseBoolean(this["login_and_email_available".uppercase()].toString()),
                                parseBoolean(this["email_available".uppercase()].toString()),
                                parseBoolean(this["login_available".uppercase()].toString())
                            ).right()
                        }
                } catch (e: Throwable) {
                    e.left()
                }

//            suspend fun Pair<User, ApplicationContext>.save(): Either<Throwable, Long> = try {
//                second
//                    .getBean<R2dbcEntityTemplate>()
//                    .databaseClient
//                    .sql(Relations.INSERT)
//                    .bind("login", first.login)
//                    .bind("email", first.email)
//                    .bind("password", first.password)
////                .bind("firstName", first.firstName)
////                .bind("lastName", first.lastName)
//                    .bind("langKey", first.langKey)
////                .bind("imageUrl", first.imageUrl)
//                    .bind("enabled", first.enabled)
////                .bind("activationKey", first.activationKey)
////                .bind("resetKey", first.resetKey)
////                .bind("resetDate", first.resetDate)
////                .bind("createdBy", first.createdBy)
////                .bind("createdDate", first.createdDate)
////                .bind("lastModifiedBy", first.lastModifiedBy)
////                .bind("lastModifiedDate", first.lastModifiedDate)
//                    .bind("version", first.version)
//                    .fetch()
//                    .awaitRowsUpdated()
//                    .right()
//            } catch (e: Throwable) {
//                e.left()
//            }
            }
        }
    }
}