package workspace

import org.assertj.swing.edt.GuiActionRunner.execute
import org.assertj.swing.fixture.FrameFixture
import kotlin.test.AfterTest
import kotlin.test.BeforeTest


//@org.junit.jupiter.api.extension.ExtendWith
class SetupSwingFrameTest {
    private lateinit var window: FrameFixture

    @BeforeTest
    fun setUp() = execute {
        window =
//            context            .
            run(::SetupSwingFrame)
            .run(::FrameFixture)
            .apply(FrameFixture::show)
    }

    @AfterTest
    fun tearDown() = window.cleanUp()
}