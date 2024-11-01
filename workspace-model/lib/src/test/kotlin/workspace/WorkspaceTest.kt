package workspace

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
import kotlin.test.Test
import kotlin.test.assertTrue

class WorkspaceTest {
    private val workspace = Workspace(
        workspace = WorkspaceEntry(
            name = "fonderie",
            path = "${System.getProperty("user.home")}/workspace/school",
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

    @Test
    fun someLibraryMethodReturnsTrue() {
        assertTrue(
            actual = workspace.someLibraryMethod(),
            message = "someLibraryMethod should return 'true'"
        )
        workspace.toString().run(::println)
        workspace.displayWorkspaceStructure()
    }

    @Test
    fun `install workspace`() {
        Workspace.install(path = "${System.getProperty("user.home")}/workspace/school")
        // default type : AllInOneWorkspace
        // ExplodedWorkspace
    }
    /*
    1/ Workspace
        a. create a workspace
        b. add an entry to the workspace
        c. remove an entry from the workspace
        d. update an entry in the workspace
        e. find an entry in the workspace
    2/ WorkspaceEntry
        a. create an Education
        b. create an Office
        c. create a Job
        d. create a Configuration
        e. create a Communication
        f. create an Organisation
        g. create a Collaboration
        h. create a Dashboard
        i. create a Portfolio
    3/ name
    4/ office
    5/ cores
    6/ job
    7/ configuration
    8/ communication
    9/ organisation
    10/ collaboration
    11/ dashboard
    12/ portfolio
     */
}