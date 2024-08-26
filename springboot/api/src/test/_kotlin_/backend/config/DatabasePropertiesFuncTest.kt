package backend.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import backend.config.Constants.PROP_DATABASE_POPULATOR_PATH
import backend.tdd.functional.AbstractBaseFunctionalTest
import kotlin.test.Test
import javax.inject.Inject
class DatabasePropertiesFuncTest : AbstractBaseFunctionalTest() {

    @Inject
    lateinit var properties: ApplicationProperties

    @Value("\${$PROP_DATABASE_POPULATOR_PATH}")
    lateinit var databasePopulatorPath: String

    @Test
    fun `Check property reaktive_database_populator-path`() {
        checkProperty(
            PROP_DATABASE_POPULATOR_PATH,
            properties.database.populatorPath,
            databasePopulatorPath
        )
    }
}