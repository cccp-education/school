package school.users.signup

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import jakarta.validation.constraints.Size
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.r2dbc.core.awaitRowsUpdated
import school.users.User
import school.users.signup.UserActivation.UserActivationDao.Attributes.ACTIVATION_DATE_ATTR
import school.users.signup.UserActivation.UserActivationDao.Attributes.ACTIVATION_KEY_ATTR
import school.users.signup.UserActivation.UserActivationDao.Attributes.CREATED_DATE_ATTR
import school.users.signup.UserActivation.UserActivationDao.Attributes.ID_ATTR
import school.users.signup.UserActivation.UserActivationDao.Fields.ACTIVATION_DATE_FIELD
import school.users.signup.UserActivation.UserActivationDao.Fields.ACTIVATION_KEY_FIELD
import school.users.signup.UserActivation.UserActivationDao.Fields.CREATED_DATE_FIELD
import school.users.signup.UserActivation.UserActivationDao.Fields.ID_FIELD
import school.users.signup.UserActivation.UserActivationDao.Relations.INSERT
import java.time.Instant
import java.util.*

@JvmRecord
data class UserActivation(
    val id: UUID,
    @field:Size(max = 20)
    val activationKey: String,
    val activationDate: Instant,
    val createdDate: Instant,
) {
    companion object {
        @JvmStatic
        fun main(args: Array<String>): Unit = println(UserActivationDao.Relations.SQL_SCRIPT)
    }

    object UserActivationDao {
        object Fields {
            const val ID_FIELD = "`id`"
            const val ACTIVATION_KEY_FIELD = "`activation_key`"
            const val ACTIVATION_DATE_FIELD = "`activation_date`"
            const val CREATED_DATE_FIELD = "`created_date`"
        }

        object Attributes {
            const val ID_ATTR = "id"
            const val ACTIVATION_KEY_ATTR = "activationKey"
            const val CREATED_DATE_ATTR = "createdDate"
            const val ACTIVATION_DATE_ATTR = "activationDate"
        }

        object Relations {
            const val TABLE_NAME = "`user_activation`"
            const val SQL_SCRIPT = """
                CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                    $ID_FIELD                     UUID PRIMARY KEY,
                    $ACTIVATION_KEY_FIELD         VARCHAR,
                    $CREATED_DATE_FIELD           datetime,
                    $ACTIVATION_DATE_FIELD        datetime,
                FOREIGN KEY ($ID_FIELD) REFERENCES ${User.UserDao.Relations.TABLE_NAME} (${User.UserDao.Fields.ID_FIELD})
                    ON DELETE CASCADE
                    ON UPDATE CASCADE);
                CREATE UNIQUE INDEX IF NOT EXISTS `uniq_idx_user_activation_key`
                ON $TABLE_NAME ($ACTIVATION_KEY_FIELD);
                CREATE INDEX IF NOT EXISTS `idx_user_activation_date`
                ON $TABLE_NAME ($CREATED_DATE_FIELD);
                CREATE INDEX IF NOT EXISTS `idx_user_activation_creation_date`
                ON $TABLE_NAME ($ACTIVATION_DATE_FIELD);
            """

            const val INSERT = """
                insert into $TABLE_NAME (
                $ID_FIELD, $ACTIVATION_KEY_FIELD, $CREATED_DATE_FIELD, $ACTIVATION_DATE_FIELD)
                values (:$ID_ATTR, :$ACTIVATION_KEY_ATTR, :$ACTIVATION_DATE_ATTR, :$CREATED_DATE_ATTR)
            """
        }

        object Dao {
            @Throws(EmptyResultDataAccessException::class)
            suspend fun Pair<UserActivation, ApplicationContext>.save(): Either<Throwable, Long> = try {
                second.getBean<R2dbcEntityTemplate>()
                    .databaseClient
                    .sql(INSERT.trimIndent())
                    .bind(ID_ATTR, first.id)
                    .bind(ACTIVATION_KEY_ATTR, first.activationKey)
                    .bind(CREATED_DATE_ATTR, first.createdDate)
                    .bind(ACTIVATION_DATE_ATTR, first.activationDate)
                    .fetch()
                    .awaitRowsUpdated()
                    .right()
            } catch (e: Throwable) {
                e.left()
            }
        }
    }
}