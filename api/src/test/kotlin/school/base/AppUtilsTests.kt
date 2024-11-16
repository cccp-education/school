@file:Suppress("JUnitMalformedDeclaration")

package school.base

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.getBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ActiveProfiles
import school.tdd.TestUtils.Data.user
import school.Application
import school.base.utils.AppUtils.cleanField
import school.base.utils.AppUtils.lsWorkingDir
import school.base.utils.AppUtils.lsWorkingDirProcess
import school.base.utils.AppUtils.toJson
import workspace.Log.i
import java.io.File
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertEquals

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
        (user to context).toJson.let(::i)
    }

    @Test
    fun `check toJson build a valid json format`(): Unit = assertDoesNotThrow {
        (user to context).toJson.let(mapper::readTree)
    }

    @Test
    fun `test cleanField extension function`() = assertEquals(
        "login",
        "`login`".cleanField(),
        "Backtick should be removed"
    )

    @Test
    fun `test lsWorkingDir & lsWorkingDiringDirProcess`(): Unit {
        val destDir: File = "build".run(::File)
        context.lsWorkingDirProcess(destDir).run { "lsWorkingDirProcess : $this" }.run(::i)
        File("build").absolutePath.run(::i)

        // Liste un répertoire spécifié par une chaîne
        context.lsWorkingDir("build", maxDepth = 2)

        // Liste un répertoire spécifié par un Path
        context.lsWorkingDir(Paths.get("build"))
    }

}