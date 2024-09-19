@file:Suppress("unused")

package webapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import webapp.core.property.Config
import webapp.core.utils.startupLog


@SpringBootApplication
@EnableConfigurationProperties(Config::class)
class Application {
  companion object {
    @JvmStatic
    fun main(args: Array<String>) = runApplication<Application>(*args).startupLog
  }
}