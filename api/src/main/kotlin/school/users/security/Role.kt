@file:Suppress("MemberVisibilityCanBePrivate")

package school.users.security

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitSingle
import school.base.utils.ROLE_ADMIN
import school.base.utils.ROLE_ANONYMOUS
import school.base.utils.ROLE_USER
import school.base.model.EntityModel
import school.users.security.Role.RoleDao.Fields.ID_FIELD

data class Role(
    @field:NotNull
    @field:Size(max = 50)
    override val id: String
): EntityModel<String>() {
    object RoleDao {
        object Fields {
            const val ID_FIELD = "`role`"
        }

        object Constraints

        object Relations {
            const val TABLE_NAME = "`authority`"
            const val SQL_SCRIPT = """
        CREATE TABLE IF NOT EXISTS $TABLE_NAME(
            $ID_FIELD VARCHAR(50) PRIMARY KEY);
        MERGE INTO $TABLE_NAME
            VALUES ('$ROLE_ADMIN'),
                   ('$ROLE_USER'),
                   ('$ROLE_ANONYMOUS');
"""
        }

        object Dao {
            suspend fun ApplicationContext.countRoles(): Int =
                "select count(*) from `authority`"
                    .let(getBean<DatabaseClient>()::sql)
                    .fetch()
                    .awaitSingle()
                    .values
                    .first()
                    .toString()
                    .toInt()
        }
    }
}