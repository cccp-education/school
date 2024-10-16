package school.base.cli

import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import school.base.utils.CLI
import school.base.utils.CLI_PROPS
import school.base.utils.NORMAL_TERMINATION
import school.base.utils.i
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
        i("Bienvenu dans le school:cli:  ")
    }
}