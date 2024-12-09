package users.profile

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import app.utils.Constants
import users.UserDao
import users.profile.UserProfile.UserProfileDao.Fields.FIRST_NAME_FIELD
import users.profile.UserProfile.UserProfileDao.Fields.IMAGE_URL_FIELD
import users.profile.UserProfile.UserProfileDao.Fields.LAST_NAME_FIELD
import java.util.*

@JvmRecord
data class UserProfile(
    @field:NotNull
    val id: UUID,
    @field:Size(max = 50)
    val firstName: String = Constants.EMPTY_STRING,
    @field:Size(max = 50)
    val lastName: String = Constants.EMPTY_STRING,
    @field:Size(max = 256)
    val imageUrl: String? = null,
) {
    object UserProfileDao {
        object Fields {
            const val ID = "id"
            const val FIRST_NAME_FIELD = "first_name"
            const val LAST_NAME_FIELD = "last_name"
            const val IMAGE_URL_FIELD = "image_url"
        }

        object Relations {
            const val TABLE_NAME = "`user_profile`"
            const val SQL_SCRIPT = """
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                ${UserDao.Fields.ID_FIELD}                     UUID PRIMARY KEY,
                $FIRST_NAME_FIELD             VARCHAR,
                $LAST_NAME_FIELD              VARCHAR,
                $IMAGE_URL_FIELD              VARCHAR,
            FOREIGN KEY (${UserDao.Fields.ID_FIELD}) REFERENCES ${UserDao.Relations.TABLE_NAME} (${UserDao.Fields.ID_FIELD})
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
//            val Pair<User, ApplicationContext>.toJson: String
//                get() = second.getBean<ObjectMapper>().writeValueAsString(first)
//
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