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
    @Test
    fun someLibraryMethodReturnsTrue() {
        val classUnderTest = Workspace(
            workspace = WorkspaceEntry(
                name = "fonderie",
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
                office = Office(
                    books = Books(name = "books-collection"),
                    datas = Datas(name = "datas"),
                    formations = TrainingCatalogue(catalogue = "formations"),
                    bizness = Profession("bizness"),
                    notebooks = Notebooks(notebooks = "notebooks"),
                    pilotage = Pilotage(name = "pilotage"),
                    schemas = Schemas(name = "schemas"),
                    slides = Slides(path = "${
                            System.getProperty("user.home")
                        }/workspace/bibliotheque/slides"),
                    sites = Sites(name = "sites")
                ),
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
                )
            )
        )
        assertTrue(
            actual = classUnderTest.someLibraryMethod(),
            message = "someLibraryMethod should return 'true'"
        )
        classUnderTest.toString().run(::println)
    }
}