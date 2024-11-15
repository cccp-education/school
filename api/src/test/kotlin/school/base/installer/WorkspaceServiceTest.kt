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
import kotlin.io.path.toPath
import kotlin.test.*


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
    fun `test create workspace with ALL_IN_ONE config`(): Unit {
        "build/workspace"
            .run(::File)
            .apply {
                when {
                    !exists() -> mkdirs().run(::assertTrue)
                }
            }
            .run {
                exists().run(::assertTrue)
                isDirectory.run(::assertTrue)
                WorkspaceConfig(
                    basePath = toPath(),
                    type = ALL_IN_ONE,
                ).run(WorkspaceService(context)::createWorkspace)
                listOf(
                    "office",
                    "education",
                    "communication",
                    "configuration",
                    "job",
                ).forEach { "$this/$it".run(::File).exists().run(::assertTrue) }
                deleteRecursively().run(::assertTrue)
            }
    }

    @Ignore
    @Test
    fun `test create workspace with SEPARATED_FOLDERS config`(): Unit {
    //check if mapPath from WorkspaceConfig exists
        //must provide existing folders
        "build/workspace"
            .run(::File)
            .apply { if (!exists()) mkdirs().run(::assertTrue) }
            .run {
                exists().run(::assertTrue)
                isDirectory.run(::assertTrue)
                WorkspaceConfig(
                    basePath = toPath(),
                    type = ALL_IN_ONE,
                ).run(WorkspaceService(context)::createWorkspace)
                listOf(
                    "office",
                    "education",
                    "communication",
                    "configuration",
                    "job",
                ).forEach { "$this/$it".run(::File).exists().run(::assertTrue) }

                val config = WorkspaceConfig(
                    type = WorkspaceService.InstallationType.SEPARATED_FOLDERS,
                    basePath = context.classLoader?.getResource("workspace")!!.toURI().toPath(),
                    subPaths = mutableMapOf(
                        "office" to context.classLoader?.getResource("office")!!.toURI().toPath(),
                        "education" to context.classLoader?.getResource("education")!!.toURI().toPath(),
                        "communication" to context.classLoader?.getResource("communication")!!.toURI().toPath(),
                        "configuration" to context.classLoader?.getResource("configuration")!!.toURI().toPath(),
                        "job" to context.classLoader?.getResource("job")!!.toURI().toPath(),
                    ),
                )
                deleteRecursively().run(::assertTrue)
            }
    }

    @Ignore
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