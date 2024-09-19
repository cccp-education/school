@file:Suppress("MemberVisibilityCanBePrivate")

package webapp.users.security

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import webapp.core.property.ROLE_ADMIN
import webapp.core.property.ROLE_ANONYMOUS
import webapp.core.property.ROLE_USER
import webapp.users.security.Role.RoleDao.Fields.ID_FIELD

data class Role(
    @field:NotNull
    @field:Size(max = 50)
    val role: String
) {
    object RoleDao {
        object Fields {
            const val ID_FIELD = "`role`"
        }

        object Constraints {
            const val PHONE_REGEX = "^(\\+|00)?[1-9]\\d{0,49}\$"
        }

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
    }
}