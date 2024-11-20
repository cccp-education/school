package app.cli

import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import app.utils.Constants
import workspace.Log
import kotlin.system.exitProcess

@Component
@Profile(Constants.CLI)
@SpringBootApplication
class CommandLine : CommandLineRunner {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<CommandLine>(*args) {
                setAdditionalProfiles(Constants.CLI)
                setDefaultProperties(Constants.CLI_PROPS)
                //before loading config
            }.run {
                //after loading config
            }
            exitProcess(Constants.NORMAL_TERMINATION)
        }
    }

    override fun run(vararg args: String?) = runBlocking {
        Log.i("command line interface: $args")
        Log.i("Bienvenu dans le school:cli:  ")
    }
}