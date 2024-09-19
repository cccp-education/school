package webapp.core.cli

import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import webapp.core.property.CLI
import webapp.core.property.CLI_PROPS
import webapp.core.property.NORMAL_TERMINATION
import webapp.core.utils.i
import kotlin.system.exitProcess

@Component
@Profile(CLI)
@SpringBootApplication
class CommandLine : CommandLineRunner {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<CommandLine>(*args) {
                setAdditionalProfiles(CLI)
                setDefaultProperties(CLI_PROPS)
                //before loading config
            }.run {
                //after loading config
            }
            exitProcess(NORMAL_TERMINATION)
        }
    }

    override fun run(vararg args: String?) = runBlocking {
        i("command line interface: $args")
        i("Bienvenu dans le webapp:cli:  ")
    }
}