package school.base.installer

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ActiveProfiles
import school.base.installer.WorkspaceService.InstallationType.ALL_IN_ONE
import school.base.installer.WorkspaceService.WorkspaceConfig
import school.base.utils.AppUtils.lsWorkingDir
import school.base.utils.AppUtils.lsWorkingDirProcess
import school.base.utils.Log.i
import java.io.File
import java.nio.file.Paths
import javax.inject.Inject
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue


//@org.junit.jupiter.api.extension.ExtendWith
@ActiveProfiles("test")
@SpringBootTest(properties = ["spring.main.web-application-type=reactive"])
class WorkspaceServiceTest {

    @Inject
    lateinit var context: ApplicationContext

    @BeforeTest
    fun setUp() = Unit

    @AfterTest
    fun tearDown() = Unit

    @Test
    fun `test workspace creation`(): Unit {
        val destDir = "build".run(::File)
        destDir.exists().run(::assertTrue)
        destDir.isDirectory.run(::assertTrue)

        WorkspaceConfig(
            basePath = destDir.toPath(),
            type = ALL_IN_ONE,
        ).run(WorkspaceService(context)::createWorkspace)

//        val config = WorkspaceService.WorkspaceConfig(
//            type = WorkspaceService.InstallationType.SEPARATED_FOLDERS,
//            basePath = context.classLoader?.getResource("workspace")!!.toURI().toPath(),
//            subPaths = mutableMapOf(
//                "office" to context.classLoader?.getResource("office")!!.toURI().toPath(),
//                "education" to context.classLoader?.getResource("education")!!.toURI().toPath(),
//                "communication" to context.classLoader?.getResource("communication")!!.toURI().toPath(),
//                "configuration" to context.classLoader?.getResource("configuration")!!.toURI().toPath(),
//                "job" to context.classLoader?.getResource("job")!!.toURI().toPath(),
//            ),
//        )


        context.lsWorkingDirProcess(destDir).run { "lsWorkingDirProcess : $this" }.run(::i)
        File("build").absolutePath.run(::i)

        // Liste un répertoire spécifié par une chaîne
        context.lsWorkingDir("build", maxDepth = 2)

        // Liste un répertoire spécifié par un Path
        context.lsWorkingDir(Paths.get("build"))
    }


    @Test
    fun `test lsWorkingDir & lsWorkingDirProcess`(): Unit {
        val destDir: File = "build".run(::File)
        context.lsWorkingDirProcess(destDir).run { "lsWorkingDirProcess : $this" }.run(::i)
        File("build").absolutePath.run(::i)

        // Liste un répertoire spécifié par une chaîne
        context.lsWorkingDir("build", maxDepth = 2)

        // Liste un répertoire spécifié par un Path
        context.lsWorkingDir(Paths.get("build"))
    }
}