package app.database

import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import workspace.Log.i

@Component
class PostgresDataLoader : CommandLineRunner {
    override fun run(vararg args: String?) = runBlocking {
        "Not yet implemented".run(::i)
    }
}