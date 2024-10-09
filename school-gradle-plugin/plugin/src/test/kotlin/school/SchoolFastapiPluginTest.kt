package school

import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * A simple unit test for the 'school.greeting' plugin.
 */
class SchoolFastapiPluginTest {
    @Test
    fun `plugin registers task`() {
        // Create a test project and apply the plugin
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("school-gradle-plugin")

        // Verify the result
        assertNotNull(project.tasks.findByName("greeting"))
    }
}