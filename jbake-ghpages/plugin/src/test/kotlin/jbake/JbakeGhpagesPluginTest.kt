package jbake

import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * A simple unit test for the 'jbake.greeting' plugin.
 */
class JbakeGhpagesPluginTest {
    @Test
    fun `plugin registers task`() {
        // Create a test project and apply the plugin
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("jbake.greeting")

        // Verify the result
        assertNotNull(project.tasks.findByName("greeting"))
    }
}
