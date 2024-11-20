package app

import ai.translator.AiTranslatorController.AssistantManager
import app.utils.LoggerUtils.startupLog
import app.utils.Properties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import users.User

@SpringBootApplication(
    scanBasePackageClasses = [
        Application::class,
        User::class,
        AssistantManager::class,
    ],
)
@EnableConfigurationProperties(Properties::class)
class Application {
    companion object {
        @JvmStatic
        fun main(args: Array<String>): Unit = runApplication<Application>(*args).startupLog()
    }
}