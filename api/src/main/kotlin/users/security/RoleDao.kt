package users.security

import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitSingle
import users.security.RoleDao.Fields.ID_FIELD

object RoleDao {

    @JvmStatic
    fun main(args: Array<String>): Unit = println(RoleDao.Relations.SQL_SCRIPT)

    object Fields {
        const val ID_FIELD = "role"
    }

    object Constraints

    object Relations {
        const val TABLE_NAME = "authority"
        const val SQL_SCRIPT = """
        CREATE TABLE IF NOT EXISTS authority (
            "$ID_FIELD" VARCHAR(50) PRIMARY KEY
        );
        
        INSERT INTO authority ("$ID_FIELD")
        VALUES ('ADMIN'), 
               ('USER'), 
               ('ANONYMOUS')
        ON CONFLICT ("$ID_FIELD") DO NOTHING;
        """
    }

    object Dao {
        suspend fun ApplicationContext.countRoles(): Int =
            "select count(*) from authority;"
                .let(getBean<DatabaseClient>()::sql)
                .fetch()
                .awaitSingle()
                .values
                .first()
                .toString()
                .toInt()
    }
}