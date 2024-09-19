package workspace.forms

import org.gradle.api.Plugin
import org.gradle.api.Project
import workspace.forms.FormManager.formAccessToken

const val CRED="/Workspace/Bibliotheque/projects/forms_generator/private/training-institut-2598582b592a.json"

@Suppress("unused")
class FormPlugin : Plugin<Project> {


    override fun apply(project: Project) {

        project.task("isFormAuthOk") {
            group = "Form"
            description = "Greetings from the Form Manager !"
            doLast {
                FormPlugin::class
                    .java
                    .simpleName
                    .let { "Hello from the $it, authentication not yet implemented !" }
                    .let{"$it\nformAccessToken : ${project.formAccessToken}"}
                    .run(::println)
            }
        }
    }
}
//TODO:  implementation 'com.google.apis:google-api-services-forms:v1-rev20220908-2.0.0'
