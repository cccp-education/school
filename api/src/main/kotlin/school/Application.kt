@file:Suppress("unused")

package school

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import school.base.property.Properties
import school.base.utils.startupLog


@SpringBootApplication
@EnableConfigurationProperties(Properties::class)
class Application {
  companion object {
    @JvmStatic
    fun main(args: Array<String>) = runApplication<Application>(*args).startupLog
  }
}