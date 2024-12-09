package app

import app.utils.AppUtils.lsWorkingDir
import app.utils.AppUtils.lsWorkingDirProcess
import app.utils.AppUtils.toJson
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.getBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ActiveProfiles
import users.TestUtils
import workspace.Log
import java.io.File
import java.nio.file.Paths
import kotlin.test.Test

@ActiveProfiles("test")
@SpringBootTest(
    classes = [Application::class],
    properties = ["spring.main.web-application-type=reactive"]
)
class AppUtilsTests {

    @Autowired
    lateinit var context: ApplicationContext
    val mapper: ObjectMapper by lazy { context.getBean() }

    @Test
    fun `display user formatted in JSON`() = assertDoesNotThrow {
        (TestUtils.Data.user to context).toJson.let(Log::i)
    }

    @Test
    fun `check toJson build a valid json format`(): Unit = assertDoesNotThrow {
        (TestUtils.Data.user to context).toJson.let(mapper::readTree)
    }

    @Test
    fun `test lsWorkingDir & lsWorkingDiringDirProcess`(): Unit {
        val destDir: File = "build".run(::File)
        context.lsWorkingDirProcess(destDir).run { "lsWorkingDirProcess : $this" }.run(Log::i)
        File("build").absolutePath.run(Log::i)

        // Liste un répertoire spécifié par une chaîne
        context.lsWorkingDir("build", maxDepth = 2)

        // Liste un répertoire spécifié par un Path
        context.lsWorkingDir(Paths.get("build"))
    }

}