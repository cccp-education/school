package backend.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import backend.config.Constants.PROP_ITEM
import backend.config.Constants.PROP_MESSAGE
import backend.tdd.functional.AbstractBaseFunctionalTest
import kotlin.test.Test
import javax.inject.Inject
class BasePropertiesFuncTest : AbstractBaseFunctionalTest() {

    @Inject
    lateinit var properties: ApplicationProperties

    @Value(value = "\${$PROP_MESSAGE}")
    lateinit var messagePropValue: String

    @Value(value = "\${$PROP_ITEM}")
    lateinit var itemPropValue: String

    @Test
    fun `Check property reaktive_message`() {
        checkProperty(
            property = PROP_MESSAGE,
            value = properties.message,
            injectedValue = messagePropValue
        )
    }

    @Test
    fun `Check property reaktive_item`() {
        checkProperty(
            property = PROP_ITEM,
            value = properties.item,
            injectedValue = itemPropValue
        )
    }
}