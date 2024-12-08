package app.cli

import app.utils.Constants.CLI
import app.utils.Constants.CLI_PROPS
import app.utils.Constants.NORMAL_TERMINATION
import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import workspace.Log.i
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
        "command line interface: $args".run(::i)
        "Bienvenu dans le school:cli:  ".run(::i)
    }
}