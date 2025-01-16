package school.frontend

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import school.workspace.WorkspaceManager.TASK_PUBLISH_SITE
import school.workspace.WorkspaceManager.initWorkspace
import school.workspace.WorkspaceManager.initialConf
import school.workspace.WorkspaceManager.printConf
import school.workspace.WorkspaceUtils
import school.workspace.WorkspaceUtils.lsWorkingDir
import school.workspace.WorkspaceUtils.yamlMapper
import school.training.content.SPG
import school.training.content.SchoolContentManager
import school.frontend.SchoolOpsManager.fixEncodage
import school.frontend.SchoolOpsManager.loadSchoolFrontend
import school.frontend.SchoolOpsManager.processSchoolFrontendCnameFile
import school.frontend.SchoolOpsManager.pushSchoolFrontendPages
import java.lang.System.getenv
import java.nio.charset.StandardCharsets.UTF_8

/**
.\gradlew.bat `
:hello `
:schoolFrontendTest `
:cTY `
:pDP `
:projects:school:school-gradle-plugin:pEV `
:projects:school:school-gradle-plugin:lWD `
:projects:school:school-gradle-plugin:SchoolMoodleInitDev `
:projects:school:school-gradle-plugin:SchoolMoodleLaunchDev `
:projects:school:school-gradle-plugin:SchoolMoodleStopDev `
:spg `
-q `
-s

tasks.register("mathlin") {
description = "kotlin + mathématique"
group = "mathlin"
doLast { mathlin() }
}

fun mathlin() {
"Hello, World!".let(::println)
"os.name"
.let(System::getProperty)
.let(::println)

val a = Pair(0, 2)
val b = Pair(3, 0)
val c = Pair(0, 0)
}

 */
//TODO: Switch from moodle to canvas
// https://curriculeon.github.io/Curriculeon/lectures/learning-management-systems/canvas/local-deployment-quickstart/content.html
class SchoolPlugin : Plugin<Project> {
    override fun apply(project: Project) {

        project.task("spg") {
            group = "School"
            description = "Show SPG."
            doFirst {
                SPG()
                    .let(project.yamlMapper::writeValueAsString)
                    .let { String(it.toByteArray(UTF_8)) }
                    .fixEncodage
                    .let(::println)
                "é".let(::println)
                val rawString = "Entwickeln Sie mit Vergnügen, Démonstration à éàè"
                val buffer = UTF_8.encode(rawString)

                val utf8EncodedString: String = UTF_8.decode(buffer).toString()
                println(utf8EncodedString)
                println("éàè$*ù§²")
            }
        }

//TODO: faire des transformation mapper un résultat qui contient taskName, group, description, dependsOn, finalizedBy
        schoolMoodleTasks
            .map { mapOf(it to GROUP_SCHOOL_MOODLE) }
            .map(WorkspaceUtils::constructTaskName)
            .map(project::task)
            .forEach {
                it.group = GROUP_SCHOOL_MOODLE
                when {
                    it.name.contains(TASK_SCHOOL_MOODLE_INIT_DEV) -> {
                        it.description = "Initialize docker image of Moodle in localhost."
                        it.doFirst {
                            println("Initialize local Moodle for development purpose.")
//                            project.extensions.getByType<DockerComposePlugin>() run (dockerCompose::isRequiredBy)
                        }
                    }

                    it.name.contains(TASK_SCHOOL_MOODLE_LAUNCH_DEV) -> {
                        it.description = "Launch docker image of Moodle in localhost."
//                        it.dependsOn("composeUp")
                        it.doFirst { println("Launch local Moodle.") }
                    }

                    it.name.contains(TASK_SCHOOL_MOODLE_STOP_DEV) -> {
                        it.description = "Stop docker image of Moodle in localhost."
//                        it.finalizedBy("composeDown")
                        it.doFirst { println("Local Moodle shutdown.") }
                    }
                }
            }

        schoolFrontendTasks.forEach { (taskName, npmTask, manual) ->
            project.task("$GROUP_SCHOOL_FRONTEND$taskName") {
                group = GROUP_SCHOOL_FRONTEND
                description = manual
                dependsOn(TASK_NPM_INSTALL)
                finalizedBy(npmTask)
                doFirst { println(":$GROUP_SCHOOL_FRONTEND$taskName") }
                doLast { project.loadSchoolFrontend() }
            }
        }

        project.task(TASK_HELLO) {
            group = GROUP_SCHOOL
            description = "Greetings from the School Manager !"
            doLast {
                SchoolPlugin::class
                    .java
                    .simpleName
                    .let { "Hello from the $it" }
                    .run(::println)
            }
        }

        project.task(TASK_CONF_TO_YAML) {
            group = GROUP_SCHOOL
            description = "Task tool to dev"
            doFirst { println(":$TASK_CONF_TO_YAML") }
            doLast { project.printConf() }
        }


        project.task(TASK_INITIAL_CONF_TO_YAML) {
            group = GROUP_WORKSPACE_UTILS
            description = "Task tool to dev"
            doFirst { println(":$TASK_INITIAL_CONF_TO_YAML") }
//            doLast {
////                val jsonInput = """{"nom":"John","age":30,"ville":"Paris"}"""  // JSON non formaté
////                val objectMapper = ObjectMapper().apply {
////                    enable(SerializationFeature.INDENT_OUTPUT)  // Active l'indentation
////                }
////                val jsonNode: JsonNode = objectMapper.readTree(jsonInput)
//                //                println(project.initialConf)
//
//                // not formated JSON
//                val jsonInput = project.initialConf.toString()
//                val objectMapper = ObjectMapper().apply {
//                    // Activate indentation
//                    enable(INDENT_OUTPUT)
//                }
//                // Read raw JSON as JsonNode
//                val jsonNode: JsonNode = objectMapper.readTree(jsonInput)
//                // Convert to formatted JSON
//                val formattedJson = objectMapper.writeValueAsString(jsonNode)
//                // Displaying formatted JSON
//                println(formattedJson)
//            }
            doLast { println(project.initialConf) }
        }

        project.task(TASK_PRINT_ENV_VARS) {
            group = GROUP_WORKSPACE_UTILS
            description = "Retrieve environment variables."
            doFirst {
                getenv()
                    .toSortedMap()
                    .map { "${it.key} = ${it.value}" }
                    .forEach(::println)
            }
        }

        project.task(TASK_INIT_WORKSPACE) {
            group = GROUP_SCHOOL
            description = "Task tool to dev"
            doFirst { println(":$TASK_INIT_WORKSPACE") }
            doLast { project.initWorkspace() }
        }

        project.task(TASK_PRINT_DOSSIER_PROFESSIONNELLE) {
            group = GROUP_SCHOOL
            description = "Log dossier professionnel structure"
            doLast { project.projectDir.run(SchoolContentManager::printDossierProfessionnelle) }
        }


        //TODO: Créer une tache qui vérifie l'existence de workspace.yaml
        //auquel cas, il y a création d'un nouveau workspace.yaml
        // sur la base de celui de initWorkspace
        // publishSite
        project.task(TASK_PUBLISH_SITE) {
            group = GROUP_SCHOOL_FRONTEND
            description = "Publish school frontend online."
            dependsOn(TASK_NPM_RUN_PREDEPLOY)
            doFirst { project.processSchoolFrontendCnameFile() }
            doLast { project.pushSchoolFrontendPages() }
        }

        project.task(TASK_SCHOOL_BACKOFFICE_TESTS) {
            group = GROUP_SCHOOL
            description = "Test backoffice."
            dependsOn(TASK_NPM_INSTALL)
            doFirst { println(":$TASK_SCHOOL_BACKOFFICE_TESTS") }
        }

        project.task(TASK_SCHOOL_BACKOFFICE_PUSH) {
            group = GROUP_SCHOOL
            description = "Push backoffice script to google workspace."
            dependsOn(TASK_SCHOOL_BACKOFFICE_TESTS)
            doFirst { println(":$TASK_SCHOOL_BACKOFFICE_PUSH") }
        }

        project.task(TASK_LS_WORKING_DIR) {
            group = GROUP_WORKSPACE_UTILS
            description = "Run ls command against workingDir."
            doFirst { project.lsWorkingDir() }
        }
        project.tasks.withType(JavaCompile::class.java) { options.encoding = "UTF-8" }
    }


    companion object {
        const val GROUP_SCHOOL = "School"
        const val GROUP_SCHOOL_FRONTEND = "SchoolFrontend"
        const val GROUP_SCHOOL_MOODLE = "SchoolMoodle"
        const val GROUP_WORKSPACE_UTILS = "WorkspaceUtils"

        const val TASK_HELLO = "hello"
        const val TASK_LS_WORKING_DIR = "lsWorkingDir"
        const val TASK_PRINT_ENV_VARS = "printEnvVars"
        const val TASK_CONF_TO_YAML = "confToYaml"
        const val TASK_INITIAL_CONF_TO_YAML = "initialConfToYaml"

        const val TASK_PRINT_DOSSIER_PROFESSIONNELLE = "printDossierProfessionnelle"

        const val TASK_INIT_WORKSPACE = "initWorkspace"

        const val TASK_SCHOOL_FRONTEND_SERVE = "Serve"
        const val TASK_SCHOOL_FRONTEND_DESIGN = "Design"
        const val TASK_SCHOOL_FRONTEND_TEST = "Test"
        const val TASK_SCHOOL_FRONTEND_BUILD = "Build"
        const val TASK_SCHOOL_FRONTEND_PUBLISH = "Publish"

        const val TASK_SCHOOL_MOODLE_STOP_DEV = "StopDev"
        const val TASK_SCHOOL_MOODLE_LAUNCH_DEV = "LaunchDev"
        const val TASK_SCHOOL_MOODLE_INIT_DEV = "InitDev"

        const val TASK_SCHOOL_BACKOFFICE_TESTS = "schoolBackofficeTests"
        const val TASK_SCHOOL_BACKOFFICE_PUSH = "schoolBackofficePush"

        const val TASK_NPM_INSTALL = "npmInstall"
        const val TASK_NPM_RUN_STORYBOOK = "npm_run_storybook"
        const val TASK_NPM_RUN_DEPLOY = "npm_run_deploy"
        const val TASK_NPM_RUN_DEV = "npm_run_dev"
        const val TASK_NPM_RUN_TEST = "npm_run_test"
        const val TASK_NPM_RUN_PREDEPLOY = "npm_run_predeploy"

        val schoolFrontendTasks = setOf(
            Triple(TASK_SCHOOL_FRONTEND_SERVE, TASK_NPM_RUN_DEV, "Preview School frontend."),
            Triple(TASK_SCHOOL_FRONTEND_TEST, TASK_NPM_RUN_TEST, "Test School frontend."),
            Triple(TASK_SCHOOL_FRONTEND_DESIGN, TASK_NPM_RUN_STORYBOOK, "Run School Frontend Storybook"),
            Triple(TASK_SCHOOL_FRONTEND_PUBLISH, TASK_NPM_RUN_DEPLOY, "Publish School frontend to github-pages."),
            Triple(TASK_SCHOOL_FRONTEND_BUILD, TASK_NPM_RUN_PREDEPLOY, "Build School frontend.")
        )

        val schoolMoodleTasks = listOf(
            Pair(TASK_SCHOOL_MOODLE_INIT_DEV, "Initialize docker image of Moodle in localhost."),
            Pair(TASK_SCHOOL_MOODLE_LAUNCH_DEV, "Launch docker image of Moodle in localhost."),
            Pair(TASK_SCHOOL_MOODLE_STOP_DEV, "Stop docker image of Moodle in localhost.")
        )

    }
}
////TODO:mettre ca en taches
////# Set up path to Moodle code
////        export MOODLE_DOCKER_WWWROOT=/path/to/moodle/code
////# Choose a db server (Currently supported: pgsql, mariadb, mysql, mssql, oracle)
////export MOODLE_DOCKER_DB=pgsql
////
////# Ensure customized config.php for the Docker containers is in place
////        cp config.docker-template.php $MOODLE_DOCKER_WWWROOT/config.php
////
////# Start up containers
////bin/moodle-docker-compose up -d
////
////# Wait for DB to come up (important for oracle/mssql)
////bin/moodle-docker-wait-for-db
////
////# Work with the containers (see below)
////# [..]
////
////# Shut down and destroy containers
////bin/moodle-docker-compose down
//
//
///*=================================================================================*/
//
//
//
///**
// * Remplace la couleur d'une image.
// *
// * @param imagePath Chemin de l'image
// * @param oldColor Ancienne couleur
// * @param newColor Nouvelle couleur
// * @param outputPath Chemin de l'image de sortie
// * @return Un `Result` représentant le succès ou l'échec de l'opération
// */
//fun replaceColor(
//    imagePath: String,
//    oldColor: Color,
//    newColor: Color,
//    outputPath: String
//): Result<Unit> = runCatching {
//    val inputFile = File(imagePath)
//    assert(inputFile.exists() && inputFile.isFile())
//
//    read(inputFile).runCatching {
//        BufferedImage(width, height, TYPE_INT_ARGB).let {
//            (0 until height).forEach { y ->
//                (0 until width).forEach { x ->
//                    Color(getRGB(x, y), true).run {
//                        it.setRGB(
//                            x, y, when (rgb) {
//                                oldColor.rgb -> newColor.rgb
//                                else -> rgb
//                            }
//                        )
//                    }
//                }
//            }
//            val outputFile = File(outputPath)
//            write(it, "png", outputFile)
//            println("Changement de couleur réussi. Image enregistrée à : ${outputFile.absolutePath}")
//        }
//    }
//}
//

///**
// * Tâche Gradle pour changer la couleur de l'image.
// */
//tasks.register("changeColor") {
//    group = "FPA"
//    description = "Change color of image."
//
//    doFirst {
//        val inputFilePath = "/ECF/A2SP/img/logo_free_4.png"
//        val outputFilePath = "/ECF/A2SP/img/logo_site.png"
//
//        listOf(
//            listOf(Color(255, 249, 250), Color(28, 28, 28)),
//            listOf(Color(89, 47, 55), Color(249, 249, 249))
//            // Ajoutez d'autres paires de couleurs au besoin
//        ).forEach { pair ->
//            when (pair.size) {
//                2 -> {
//                    replaceColor(
//                        file("$projectDir$inputFilePath").apply {
//                            assert(exists() && isFile())
//                        }.absolutePath,
//                        pair.first(),
//                        pair.last(),
//                        "$projectDir$outputFilePath"
//                    ).onFailure { e ->
//                        println("Erreur lors du changement de couleur : $e")
//                    }
//                }
//
//                else -> {
//                    println("Chaque paire de couleurs doit contenir exactement deux couleurs.")
//                }
//            }
//        }
//    }
//}
//*/