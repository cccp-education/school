@file:Suppress("unused")

package school

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import school.base.utils.Properties
import school.base.utils.Log.startupLog


@SpringBootApplication
@EnableConfigurationProperties(Properties::class)
class Application {
  companion object {
    @JvmStatic
    fun main(args: Array<String>): Unit = runApplication<Application>(*args).startupLog
  }
}