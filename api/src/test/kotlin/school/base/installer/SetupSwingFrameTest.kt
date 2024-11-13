package school.base.installer

import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.edt.GuiActionRunner.execute
import org.assertj.swing.fixture.FrameFixture
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ActiveProfiles
import javax.inject.Inject
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test


//@org.junit.jupiter.api.extension.ExtendWith
@ActiveProfiles("test")
@SpringBootTest(properties = ["spring.main.web-application-type=reactive"])
class SetupSwingFrameTest {

    @Inject
    lateinit var context: ApplicationContext
    private lateinit var window: FrameFixture

    @BeforeTest
    fun setUp() = execute {
        window = context
            .run(::SetupSwingFrame)
            .run(::FrameFixture)
            .apply(FrameFixture::show)
    }


    @AfterTest
    fun tearDown() = window.cleanUp()

}