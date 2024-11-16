@file:Suppress("unused")

package workspace

import workspace.Workspace.InstallationType.ALL_IN_ONE
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
import workspace.WorkspaceManager.WorkspaceConstants.entries
import java.io.File
import java.nio.file.Path
import kotlin.io.path.pathString

object WorkspaceManager {

    object WorkspaceConstants {
        val entries = listOf(
            "office",
            "education",
            "communication",
            "configuration",
            "job"
        )
    }


    fun createWorkspace(config: WorkspaceConfig): WorkspaceConfig = config.createConfigFiles("config.yaml").run {
        if (config.type == ALL_IN_ONE) {
            config.createAllInOneFolder(config.basePath)
        }
        return config
    }

    fun WorkspaceConfig.createAllInOneFolder(basePath: Path)
            : WorkspaceConfig = entries.forEach { dir -> basePath.resolve(dir).run(WorkspaceManager::createDirectory) }
        .let { this@createAllInOneFolder }

    fun createDirectory(path: Path) = path
        .toFile()
        .apply {
            when {
                !exists() -> mkdirs()
            }
        }


    fun WorkspaceConfig.createConfigFiles(configFileName: String) = File(
        basePath.toFile(),
        configFileName
    ).apply {
        when {
            exists() -> delete()
        }
        createNewFile()
        workspace.toYaml.trimIndent().run(::writeText)
    }

    val WorkspaceConfig.workspace: Workspace
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
                    sites = Sites(name = "sites"),
                    path = subPaths["office"]?.pathString ?: "office"
                ),
                cores = mapOf(
                    "education" to Education(
                        path = subPaths["education"]?.pathString ?: "education",
                        school = School(name = "talaria"),
                        student = Student(name = "olivier"),
                        teacher = Teacher(name = "cheroliv"),
                        educationTools = EducationTools(name = "edTools")
                    ),
                ),
                job = Job(
                    path = subPaths["job"]?.pathString ?: "job",
                    position = Position("Teacher"),
                    resume = Resume(name = "CV")
                ),
                configuration = Configuration(
                    path = subPaths["configuration"]?.pathString ?: "configuration",
                    configuration = "school-configuration"),
                communication = Communication(path = subPaths["communication"]?.pathString ?: "communication", site = "static-website"),
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