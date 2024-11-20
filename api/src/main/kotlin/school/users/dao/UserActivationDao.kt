@file:Suppress(
    "MemberVisibilityCanBePrivate",
    "SqlDialectInspection"
)

package school.users.dao

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.r2dbc.core.*
import school.base.utils.AppUtils.cleanField
import school.users.UserActivation
import school.users.dao.UserActivationDao.Attributes.ACTIVATION_DATE_ATTR
import school.users.dao.UserActivationDao.Attributes.ACTIVATION_KEY_ATTR
import school.users.dao.UserActivationDao.Attributes.CREATED_DATE_ATTR
import school.users.dao.UserActivationDao.Fields.ACTIVATION_DATE_FIELD
import school.users.dao.UserActivationDao.Fields.ACTIVATION_KEY_FIELD
import school.users.dao.UserActivationDao.Fields.CREATED_DATE_FIELD
import school.users.dao.UserActivationDao.Relations.TABLE_NAME
import java.time.LocalDateTime.*
import java.time.ZoneOffset.UTC
import java.util.*

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
            ${Fields.ID_FIELD}            UUID PRIMARY KEY,
            $ACTIVATION_KEY_FIELD         VARCHAR,
            $CREATED_DATE_FIELD           TIMESTAMP,
            $ACTIVATION_DATE_FIELD        TIMESTAMP,
        FOREIGN KEY (${Fields.ID_FIELD}) REFERENCES ${UserDao.Relations.TABLE_NAME} (${UserDao.Fields.ID_FIELD})
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
        ${Fields.ID_FIELD} , $ACTIVATION_KEY_FIELD, $CREATED_DATE_FIELD, $ACTIVATION_DATE_FIELD)
        values (:${Attributes.ID_ATTR}, :$ACTIVATION_KEY_ATTR, :$CREATED_DATE_ATTR, :$ACTIVATION_DATE_ATTR)
    """
    }

    object Dao {
        suspend fun ApplicationContext.countUserActivation(): Int =
            "SELECT COUNT(*) FROM $TABLE_NAME"
                .let(getBean<DatabaseClient>()::sql)
                .fetch()
                .awaitSingle()
                .values
                .first()
                .toString()
                .toInt()

        @Throws(EmptyResultDataAccessException::class)
        suspend fun Pair<UserActivation, ApplicationContext>.save(): Either<Throwable, Long> = try {
            Relations.INSERT
                .trimIndent()
                .run(second.getBean<R2dbcEntityTemplate>().databaseClient::sql)
                .bind(Attributes.ID_ATTR, first.id)
                .bind(ACTIVATION_KEY_ATTR, first.activationKey)
                .bind(CREATED_DATE_ATTR, first.createdDate)
                .bind(ACTIVATION_DATE_ATTR, first.activationDate)
                .fetch()
                .awaitRowsUpdated()
                .right()
        } catch (e: Throwable) {
            e.left()
        }

        //Find userActivation by key
        //Update userActivation
        //TODO: activate user from key (Find userActivation then Update userActivation)  one query select+update
        @Throws(EmptyResultDataAccessException::class)
        suspend fun ApplicationContext.findUserActivationByKey(key: String)
                : Either<Throwable, UserActivation> = try {
            "SELECT * FROM $TABLE_NAME WHERE $ACTIVATION_KEY_FIELD = :$ACTIVATION_KEY_ATTR"
                .trimIndent()
                .run(getBean<R2dbcEntityTemplate>().databaseClient::sql)
                .bind(ACTIVATION_KEY_ATTR, key)
                .fetch()
                .awaitSingleOrNull()
                .let {
                    return when (it) {
                        null -> EmptyResultDataAccessException(1).left()
                        else -> UserActivation(
                            id = it[Fields.ID_FIELD.cleanField().uppercase()].toString().run(UUID::fromString),
                            activationKey = it[ACTIVATION_KEY_FIELD.cleanField().uppercase()].toString(),
                            createdDate = parse(it[CREATED_DATE_FIELD.cleanField().uppercase()].toString()).toInstant(
                                UTC
                            ),
                            activationDate = it[ACTIVATION_DATE_FIELD.cleanField().uppercase()].run {
                                when {
                                    this == null || toString().lowercase() == "null" -> null
                                    else -> toString().run(::parse).toInstant(UTC)
                                }
                            },
                        ).right()
                    }
                }
        } catch (e: Throwable) {
            e.left()
        }
    }
}