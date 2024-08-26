package workspace.jamstack

import org.gradle.api.Plugin
import org.gradle.api.Project

class JBakeGhPagesPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.task("helloJBakeGhPages") {
            doFirst { println("Hello JbakeGhPages") }
//            tasks.register("jbakeGhPagesTests") {
//                group = "JbakeGhPages"
//                description = "Tests JbakeGhPages."
//                dependsOn("npmInstall")
//                doFirst { println(":jbakeGhPagesTests") }
//            }
//
//            tasks.register("jbakeGhPagesInit") {
//                group = "JbakeGhPages"
//                description = "Initialize a bake in a empty folder."
//                dependsOn("jbakeGhPagesTests")
//                doFirst { println(":jbakeGhPagesInit") }
//            }
//
//            tasks.register("jbakeGhPagesServe") {
//                group = "JbakeGhPages"
//                description = "Run server to preview bake on localhost."
//                dependsOn("npmInstall")
//                doFirst { println(":jbakeGhPagesServe") }
//            }
//
//            tasks.register("jbakeGhPagesPublish") {
//                group = "JbakeGhPages"
//                description = "Publish bake to github-pages."
//                dependsOn("jbakeGhPagesTests")
//                doFirst { println(":jbakeGhPagesPublish") }
//            }
//
//            tasks.register("jbakeGhPagesCommitAndPush") {
//                group = "JbakeGhPages"
//                description = "Commit and push sources to github repository."
//                doFirst {
//                    println(":jbakeGhPagesCommitAndPush")
////        grgit.add(pattern = "**/*")
////        grgit.commit(message = "Votre message de commit")
////        grgit.push()
//                }
//            }
        }

    }
}