package school.base.installer

import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.FrameFixture
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ActiveProfiles
import javax.inject.Inject
import kotlin.test.Test


//@org.junit.jupiter.api.extension.ExtendWith
@SpringBootTest(properties = ["spring.main.web-application-type=reactive"])
@ActiveProfiles("test")
class SetupTest {

    @Inject
    lateinit var context: ApplicationContext
    private lateinit var window: FrameFixture

//    @kotlin.test.BeforeTest
    fun setUp(): Unit {
        lateinit var setup: Setup
        GuiActionRunner.execute { setup = Setup(context = context) }
        window = FrameFixture(setup)
        window.show();
    }


//    @kotlin.test.AfterTest
    fun tearDown() {
    }

    @Test
    fun `initUI$api`() {
    }

    @Test
    fun getSelectedPaths() {
    }

    @Test
    fun `getCurrentInstallationType$api`() {
    }

    @Test
    fun `setCurrentInstallationType$api`() {
    }

    @Test
    fun `getCommunicationPathLabel$api`() {
    }

    @Test
    fun `getCommunicationPathTextField$api`() {
    }

    @Test
    fun `getConfigurationPathLabel$api`() {
    }

    @Test
    fun `getConfigurationPathTextField$api`() {
    }

    @Test
    fun `getEducationPathLabel$api`() {
    }

    @Test
    fun `getEducationPathTextField$api`() {
    }

    @Test
    fun `getJobPathLabel$api`() {
    }

    @Test
    fun `getJobPathTextField$api`() {
    }

    @Test
    fun `getOfficePathLabel$api`() {
    }

    @Test
    fun `getOfficePathTextField$api`() {
    }

    @Test
    fun `getTitleLabel$api`() {
    }

    @Test
    fun `getWorkspacePathLabel$api`() {
    }

    @Test
    fun `getWorkspacePathTextField$api`() {
    }

    @Test
    fun `getWorkspaceTypePanel$api`() {
    }

    @Test
    fun `getWorkspaceTypeSelectorPanel$api`() {
    }

    @Test
    fun `getWorkspaceTopPanel$api`() {
    }

    @Test
    fun `getWorkspacePathPanel$api`() {
    }

    @Test
    fun `getWorkspaceEntriesPanel$api`() {
    }

    @Test
    fun `getSplitWorkspaceRadioButton$api`() {
    }

    @Test
    fun `getAllInOneWorkspaceRadioButton$api`() {
    }

    @Test
    fun `getBrowseCommunicationPathButton$api`() {
    }

    @Test
    fun `getBrowseConfigurationPathButton$api`() {
    }

    @Test
    fun `getBrowseEducationPathButton$api`() {
    }

    @Test
    fun `getBrowseOfficePathButton$api`() {
    }

    @Test
    fun `getBrowseWorkspacePathButton$api`() {
    }

    @Test
    fun `getBrowsejobPathButton$api`() {
    }

    @Test
    fun `getCreateWorkspaceButton$api`() {
    }

    @Test
    fun `getInstallationTypeGroup$api`() {
    }
}