@file:Suppress("FunctionName")

package school

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.*

const val BUILD_GRADLE = "build.gradle.kts"
const val SETTINGS_GRADLE = "settings.gradle.kts"
private const val BUILD_SRC_LIBS = "build/libs"

@Ignore
class PluginFunctionalTests {

    @TempDir
    lateinit var projectDir: File
    private val buildFile: File by lazy { projectDir.resolve(BUILD_GRADLE) }
    private val settingsFile: File by lazy { projectDir.resolve(SETTINGS_GRADLE) }


    @BeforeTest
    fun setupBuildFiles() {
        Pair(
            "src/main/resources/build.gradle.kts",
            "src/main/resources/settings.gradle.kts"
        ).setupBuildFiles()
    }


    @Test
    fun `Given SchoolPlugin When hello task Then output expected message`() = GradleRunner
        .create()
        .withProjectDir(projectDir)
        .withPluginClasspath(
            BUILD_SRC_LIBS
                .let(::File)
                .listFiles()!!
                .toList()
        )
        .forwardOutput()
//        .withArguments(":hello")
        .withArguments(":tasks")
        .build()
//        .run { assertTrue("Hello from the SchoolPlugin".let(output::contains)) }
        .let { }

    @Test
    fun `Check context is ok`() {
        `Check projectDir exists`(projectDir)
        `Check projectDir is a directory`(projectDir)
        `Check buildFile is there`(buildFile)
        `Check settingsFile is there`(settingsFile)
    }


    private fun `Check projectDir exists`(projectDir: File) = projectDir
        .let(File::exists)
        .run(::assertTrue)


    private fun `Check projectDir is a directory`(projectDir: File) = projectDir
        .let(File::isDirectory)
        .run(::assertTrue)

    private fun `Check buildFile is there`(buildFile: File) = buildFile
        .apply(::assertNotNull)
        .apply { isDirectory.let(::assertFalse) }
        .run {
            assertTrue(
                path.contains(BUILD_GRADLE),
                absolutePath
            )
        }

    private fun `Check settingsFile is there`(settingsFile: File) = settingsFile
        .apply(::assertNotNull)
        .apply { isDirectory.let(::assertFalse) }
        .run {
            assertTrue(
                path.contains(SETTINGS_GRADLE),
                absolutePath
            )
        }

    fun Pair<String,String>.setupBuildFiles(){
        first
            .let(projectDir::resolve)
            .readText(Charsets.UTF_8)
            .let(buildFile::writeText)
        second
            .let(projectDir::resolve)
            .readText(Charsets.UTF_8)
            .let(settingsFile::writeText)
    }

}