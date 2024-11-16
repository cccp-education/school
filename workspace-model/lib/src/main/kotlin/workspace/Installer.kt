package workspace

import workspace.Log.w
import java.awt.EventQueue.invokeLater
import javax.swing.UIManager.getInstalledLookAndFeels
import javax.swing.UIManager.setLookAndFeel
import javax.swing.UnsupportedLookAndFeelException

object Installer {
    private const val EMPTY_STRING = ""
    @JvmStatic
    fun main(args: Array<String>) = try {
        getInstalledLookAndFeels()
            .find { it.name == "Nimbus" }
            ?.let { setLookAndFeel(it.className) }
    } catch (ex: Exception) {
        when (ex) {
            is ClassNotFoundException,
            is InstantiationException,
            is IllegalAccessException,
            is UnsupportedLookAndFeelException -> w(EMPTY_STRING, ex)
            // Rethrow unknown exceptions
            else -> throw ex
        }
    }.run {
        invokeLater {
                run(::SetupSwingFrame)
                .run { isVisible = true }
        }
    }
}