@file:Suppress("unused")

package workspace

import workspace.Workspace.InstallationType.ALL_IN_ONE
import workspace.Workspace.InstallationType.SEPARATED_FOLDERS
import workspace.Workspace.WorkspaceConfig
import workspace.Workspace.WorkspaceEntry
import workspace.Workspace.WorkspaceEntry.CollaborationEntry.Collaboration
import workspace.Workspace.WorkspaceEntry.CommunicationEntry.Communication
import workspace.Workspace.WorkspaceEntry.ConfigurationEntry.Configuration
import workspace.Workspace.WorkspaceEntry.CoreEntry.Education
import workspace.Workspace.WorkspaceEntry.CoreEntry.Education.EducationEntry.*
import workspace.Workspace.WorkspaceEntry.DashboardEntry.Dashboard
import workspace.Workspace.WorkspaceEntry.JobEntry.Job
import workspace.Workspace.WorkspaceEntry.JobEntry.Job.HumanResourcesEntry.Position
import workspace.Workspace.WorkspaceEntry.JobEntry.Job.HumanResourcesEntry.Resume
import workspace.Workspace.WorkspaceEntry.OfficeEntry.Office
import workspace.Workspace.WorkspaceEntry.OfficeEntry.Office.LibraryEntry.*
import workspace.Workspace.WorkspaceEntry.OrganisationEntry.Organisation
import workspace.Workspace.WorkspaceEntry.PortfolioEntry.Portfolio
import workspace.Workspace.WorkspaceEntry.PortfolioEntry.Portfolio.PortfolioProject
import workspace.Workspace.WorkspaceEntry.PortfolioEntry.Portfolio.PortfolioProject.ProjectBuild
import java.io.File
import java.nio.file.Path
import kotlin.io.path.pathString

object WorkspaceManager {
    /**
    user scenarios :
     * ALL_IN_ONE
     * SEPARATED_FOLDERS
     */
    fun createWorkspace(config: WorkspaceConfig)
            : WorkspaceConfig = when (config.type) {
        ALL_IN_ONE -> config.createAllInOneFolder(config.basePath)
        SEPARATED_FOLDERS -> config.createSeparatedFolders(config.basePath)
    }.also { config.createConfigFiles() }

    private fun WorkspaceConfig.createAllInOneFolder(basePath: Path): WorkspaceConfig = listOf(
        "office",
        "education",
        "communication",
        "configuration",
        "job"
    ).forEach { dir -> basePath.resolve(dir).run(WorkspaceManager::createDirectory) }
        .let { this@createAllInOneFolder }

    private fun WorkspaceConfig.createSeparatedFolders(
        basePath: Path,
    ): WorkspaceConfig = /*TODO: va chercher les valeurs des fileChoosers*/
//        school.base.utils.Log.i("basePath.pathString : ${basePath.pathString}")
        println("basePath.pathString : ${basePath.pathString}")
            .let { this@createSeparatedFolders }


    private fun createDirectory(path: Path) = path.toFile().apply {
        when {
            !exists() -> mkdirs()
        }
    }


    private fun WorkspaceConfig.createConfigFiles() = File(
        basePath.toFile(),
        "config.yaml"
    ).apply {
        fromWorkspaceConfig.toYaml.trimIndent().run(::writeText)
        if (exists()) delete()
        createNewFile()
    }

    private val WorkspaceConfig.fromWorkspaceConfig: Workspace
        get() = Workspace(
            workspace = WorkspaceEntry(
                name = "workspace",
                path = basePath.pathString,
                office = Office(
                    books = Books(name = "books-collection"),
                    datas = Datas(name = "datas"),
                    formations = TrainingCatalogue(catalogue = "formations"),
                    bizness = Profession("bizness"),
                    notebooks = Notebooks(notebooks = "notebooks"),
                    pilotage = Pilotage(name = "pilotage"),
                    schemas = Schemas(name = "schemas"),
                    slides = Slides(
                        path = "${
                            System.getProperty("user.home")
                        }/workspace/bibliotheque/slides"
                    ),
                    sites = Sites(name = "sites")
                ),
                cores = mapOf(
                    "education" to Education(
                        school = School(name = "talaria"),
                        student = Student(name = "olivier"),
                        teacher = Teacher(name = "cheroliv"),
                        educationTools = EducationTools(name = "edTools")
                    ),
                ),
                job = Job(
                    position = Position("Teacher"),
                    resume = Resume(name = "CV")
                ),
                configuration = Configuration(configuration = "school-configuration"),
                communication = Communication(site = "static-website"),
                organisation = Organisation(organisation = "organisation"),
                collaboration = Collaboration(collaboration = "collaboration"),
                dashboard = Dashboard(dashboard = "dashboard"),
                portfolio = Portfolio(
                    mutableMapOf(
                        "school" to PortfolioProject(
                            name = "name",
                            cred = "credential",
                            builds = mutableMapOf("training" to ProjectBuild(name = "training"))
                        )
                    )
                ),
            )
        )
}


