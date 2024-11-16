package school.base.cli

import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import school.base.utils.Constants.CLI
import school.base.utils.Constants.CLI_PROPS
import school.base.utils.Constants.NORMAL_TERMINATION
import workspace.Log.i
import kotlin.system.exitProcess

@Component
@Profile(CLI)
@SpringBootApplication
class CommandLine : CommandLineRunner {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<CommandLine>(*args) {
                setAdditionalProfiles(CLI)
                setDefaultProperties(CLI_PROPS)
                //before loading config
            }.run {
                //after loading config
            }
            exitProcess(NORMAL_TERMINATION)
        }
    }

    override fun run(vararg args: String?) = runBlocking {
        i("command line interface: $args")
        i("Bienvenu dans le school:cli:  ")
    }
}

//package school.base.cli
//import kotlinx.coroutines.runBlocking
//import org.springframework.boot.CommandLineRunner
//import org.springframework.boot.autoconfigure.SpringBootApplication
//import org.springframework.boot.runApplication
//import org.springframework.context.annotation.Profile
//import org.springframework.stereotype.Component
//import school.base.utils.CLI
//import school.base.utils.CLI_PROPS
//import school.base.utils.NORMAL_TERMINATION
//import school.base.utils.Log.i
//import java.awt.GraphicsEnvironment
//import java.awt.HeadlessException
//import kotlin.system.exitProcess
//import javax.swing.SwingUtilities
//
//
//@Component
//@Profile(CLI)
//class CommandLine : CommandLineRunner {
//    companion object {
//        private val APP_PROPS = mapOf(
//            "spring.main.web-application-type" to "NONE",
//            "spring.main.headless" to false,
//            "java.awt.headless" to false
//        )
//
//        @JvmStatic
//        fun main(vararg args: String) {
//            i("Main method args: ${args.contentToString()}")
//
//            // Créer une Map avec les propriétés système
//            val systemProperties = APP_PROPS.map { (key, value) ->
//                "-D$key=$value"
//            }.toTypedArray()
//
//            // Lancer l'application avec les propriétés système
//            runApplication<CommandLine>(*args, *systemProperties) {
//                setAdditionalProfiles(CLI)
//            }
//            exitProcess(NORMAL_TERMINATION)
//        }
//    }
//
//    override fun run(vararg args: String?) = runBlocking {
//        val arguments = args.filterNotNull()
//        i("CommandLineRunner received args: $arguments")
//
//        if (arguments.contains("--gui")) {
//            i("Tentative de lancement de l'interface graphique...")
//            if (GraphicsEnvironment.isHeadless()) {
//                i("Environnement graphique non disponible, basculement en mode CLI...")
//                handleCliMode(arguments)
//            } else {
//                try {
//                    // Utiliser invokeAndWait pour s'assurer que l'UI est initialisée
//                    SwingUtilities.invokeAndWait {
//                        val setupFrame = SetupSwingFrame()
//                        setupFrame.isVisible = true
//
//                        // Maintenir l'application active tant que la fenêtre est ouverte
//                        while (setupFrame.isVisible) {
//                            Thread.sleep(100)
//                        }
//                    }
//                } catch (e: Exception) {
//                    if (e is HeadlessException) {
//                        i("Environnement graphique non disponible, basculement en mode CLI...")
//                        handleCliMode(arguments)
//                    } else {
//                        System.err.println("Erreur lors du lancement de l'interface graphique: ${e.message}")
//                        e.printStackTrace()
//                        exitProcess(1)
//                    }
//                }
//            }
//        } else {
//            i("Mode CLI sans interface graphique.")
//            handleCliMode(arguments)
//        }
//    }
//
//    private fun handleCliMode(arguments: List<String>) {
//        // Votre logique CLI ici
//        println("Arguments de la CLI: $arguments")
//    }
//}
//
////@Component
////@Profile(CLI)
////@SpringBootApplication
////class CommandLine : CommandLineRunner {
////    companion object {
////        private val APP_PROPS = mapOf(
////            "spring.main.web-application-type" to "NONE",
////            "spring.main.headless" to false,
////            "java.awt.headless" to false
////        )
////
////        @JvmStatic
////        fun main(vararg args: String) {
////            i("Main method args: ${args.contentToString()}")
////            val allProperties = (CLI_PROPS + APP_PROPS)
////            val argz: MutableList<String> = mutableListOf()
////            APP_PROPS
////                .map { "${it.key}=${it.value}" }
////                .forEach(argz::add)
////            runApplication<CommandLine>(*argz.toTypedArray()) {
////                setAdditionalProfiles(CLI)
////                setDefaultProperties(allProperties)
////            }
////            exitProcess(NORMAL_TERMINATION)
////        }
////    }
////
////    override fun run(vararg args: String?) = runBlocking {
////        val arguments = args.filterNotNull()
////        i("CommandLineRunner received args: $arguments")
////
////        if (arguments.contains("--gui")) {
////            i("Tentative de lancement de l'interface graphique...")
////            if (GraphicsEnvironment.isHeadless()) {
////                i("Environnement graphique non disponible, basculement en mode CLI...")
////                handleCliMode(arguments)
////            } else {
////                try {
////                    // Utiliser invokeAndWait pour s'assurer que l'UI est initialisée
////                    SwingUtilities.invokeAndWait {
////                        val setupFrame = SetupSwingFrame()
////                        setupFrame.isVisible = true
////
////                        // Maintenir l'application active tant que la fenêtre est ouverte
////                        while (setupFrame.isVisible) {
////                            Thread.sleep(100)
////                        }
////                    }
////                } catch (e: Exception) {
////                    if (e is HeadlessException) {
////                        i("Environnement graphique non disponible, basculement en mode CLI...")
////                        handleCliMode(arguments)
////                    } else {
////                        System.err.println("Erreur lors du lancement de l'interface graphique: ${e.message}")
////                        e.printStackTrace()
////                        exitProcess(1)
////                    }
////                }
////            }
////        } else {
////            i("Mode CLI sans interface graphique.")
////            handleCliMode(arguments)
////        }
////    }
////
////    private fun handleCliMode(arguments: List<String>) {
////        // Votre logique CLI ici
////        println("Arguments de la CLI: $arguments")
////    }
////}
//
//
////@Component
////@Profile(CLI)
////class CommandLine : CommandLineRunner {
////    companion object {
////        private val APP_PROPS = mapOf(
////            "spring.main.web-application-type" to "NONE",
////            "spring.main.headless" to false,
////            "java.awt.headless" to false
////        )
////
////        @JvmStatic
////        fun main(vararg args: String) {
////            i("Main method args: ${args.contentToString()}")
////            val allProperties = (CLI_PROPS + APP_PROPS)
////            runApplication<CommandLine>(*args) {
////                setAdditionalProfiles(CLI)
////                setDefaultProperties(allProperties)
////            }
////            exitProcess(NORMAL_TERMINATION)
////        }
////    }
////
////    override fun run(vararg args: String?) = runBlocking {
////        val arguments = args.filterNotNull()
////        i("CommandLineRunner received args: $arguments")
////
////        if (arguments.contains("--gui")) {
////            i("Tentative de lancement de l'interface graphique...")
////            // Vérifier si l'environnement graphique est disponible
////            if (GraphicsEnvironment.isHeadless()) {
////                i("Environnement graphique non disponible, basculement en mode CLI...")
////                // Mode dégradé CLI
////                handleCliMode(arguments)
////            } else {
////                // Mode GUI
////                handleGuiMode()
////            }
////        } else {
////            i("Mode CLI sans interface graphique.")
////            handleCliMode(arguments)
////        }
////    }
////
////    private fun handleGuiMode() {
////        try {
////            SwingUtilities.invokeAndWait {
////                val setupFrame = SetupSwingFrame()
////                setupFrame.isVisible = true
////
////                // Maintenir l'application active tant que la fenêtre est ouverte
////                while (setupFrame.isVisible) {
////                    Thread.sleep(100)
////                }
////            }
////        } catch (e: Exception) {
////            System.err.println("Erreur lors du lancement de l'interface graphique: ${e.message}")
////            e.printStackTrace()
////            exitProcess(1)
////        }
////    }
////
////    private fun handleCliMode(arguments: List<String>) {
////        // Votre logique CLI ici
////        println("Arguments de la CLI: $arguments")
////    }
////}
//
//
////@Component
////@Profile(CLI)
////@SpringBootApplication
////class CommandLine : CommandLineRunner {
////    companion object {
////        // Propriétés de configuration spécifiques à l'application
////        private val APP_PROPS = mapOf(
////            "spring.main.web-application-type" to "NONE",
////            "spring.main.headless" to false,
////            "java.awt.headless" to false
////        )
////
////        @JvmStatic
////        fun main(vararg args: String) {
////            i("Main method args: ${args.contentToString()}")
////
////            // Fusion des propriétés CLI et application
////            val allProperties = (CLI_PROPS + APP_PROPS)
////
////            runApplication<CommandLine>(*args) {
////                setAdditionalProfiles(CLI)
////                setDefaultProperties(allProperties)
////            }
////            exitProcess(NORMAL_TERMINATION)
////        }
////    }
////
////    override fun run(vararg args: String?) = runBlocking {
////        val arguments = args.filterNotNull()
////        i("CommandLineRunner received args: ${arguments.map { it }}")
////
////        if (arguments.contains("--gui")) {
////            i("Lancement de l'interface graphique...")
////            if (GraphicsEnvironment.isHeadless()) {
////                System.err.println("ERREUR: Environnement graphique non disponible")
////                exitProcess(1)
////            }
////
////            try {
////                // Utiliser invokeAndWait pour s'assurer que l'UI est initialisée
////                SwingUtilities.invokeAndWait {
////                    val setupFrame = SetupSwingFrame()
////                    setupFrame.isVisible = true
////
////
////                    // Maintenir l'application active tant que la fenêtre est ouverte
////                    while (setupFrame.isVisible) {
////                        Thread.sleep(100)
////                    }
////                }
////            } catch (e: Exception) {
////                System.err.println("Erreur lors du lancement de l'interface graphique: ${e.message}")
////                e.printStackTrace()
////                exitProcess(1)
////            }
////        } else {
////            i("Mode CLI sans interface graphique.")
////            // Votre logique CLI ici
////        }
////    }
////}
//
//
////class CommandLine : CommandLineRunner {
////    companion object {
////        @JvmStatic
////        fun main(args: Array<String>) {
////            runApplication<CommandLine>(*args) {
////                setAdditionalProfiles(CLI)
////                setDefaultProperties(
////                    CLI_PROPS + mapOf(
////                        "spring.main.web-application-type" to "NONE",
////                        "spring.main.headless" to false,
////                        "java.awt.headless" to false
////                    )
////                )
////            }.run {
////                // after loading config
////            }
////            exitProcess(NORMAL_TERMINATION)
////        }
////    }
////
////    override fun run(vararg args: String?) = runBlocking {
////        i("command line interface: ${args.map { it.toString() }}")
////
////        if (args.contains("--gui")) {
////            i("Lancement de l'interface graphique...")
////            SwingUtilities.invokeLater {
////                val setupFrame = SetupSwingFrame()
////                setupFrame.isVisible = true
////            }
////        } else {
////            i("Mode CLI sans interface graphique.")
////            // Ajoutez ici la logique de votre CLI sans interface graphique
////        }
////    }
////}