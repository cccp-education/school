package workspace

import kotlin.test.Test
import kotlin.test.assertTrue

class WorkspaceTest {
    @Test
    fun someLibraryMethodReturnsTrue() {
        val classUnderTest = Workspace(
            workspace = Workspace.WorkspaceEntry(
                name = "fonderie",
                cores = mapOf(
                    "education" to Workspace.WorkspaceEntry.CoreEntry.Education(
                        school = Workspace.WorkspaceEntry.CoreEntry.Education.EducationEntry.School(name = "talaria"),
                        student = Workspace.WorkspaceEntry.CoreEntry.Education.EducationEntry.Student(name = "olivier"),
                        teacher = Workspace.WorkspaceEntry.CoreEntry.Education.EducationEntry.Teacher(name = "cheroliv"),
                        educationTools = Workspace.WorkspaceEntry.CoreEntry.Education.EducationEntry.EducationTools(name = "edTools")
                    ),
                ),
                job = Workspace.WorkspaceEntry.JobEntry.Job(
                    position = Workspace.WorkspaceEntry.JobEntry.Job.HumanResourcesEntry.Position("Teacher"),
                    resume = Workspace.WorkspaceEntry.JobEntry.Job.HumanResourcesEntry.Resume(name = "CV")
                ),
                configuration = Workspace.WorkspaceEntry.ConfigurationEntry.Configuration(configuration = "school-configuration"),
                communication = Workspace.WorkspaceEntry.CommunicationEntry.Communication(site = "static-website"),
                office = Workspace.WorkspaceEntry.OfficeEntry.Office(
                    books = Workspace.WorkspaceEntry.OfficeEntry.Office.LibraryEntry.Books(name = "books-collection"),
                    datas = Workspace.WorkspaceEntry.OfficeEntry.Office.LibraryEntry.Datas(name = "datas"),
                    formations = Workspace.WorkspaceEntry.OfficeEntry.Office.LibraryEntry.TrainingCatalogue(catalogue = "formations"),
                    bizness = Workspace.WorkspaceEntry.OfficeEntry.Office.LibraryEntry.Profession("bizness"),
                    notebooks = Workspace.WorkspaceEntry.OfficeEntry.Office.LibraryEntry.Notebooks(notebooks = "notebooks"),
                    pilotage = Workspace.WorkspaceEntry.OfficeEntry.Office.LibraryEntry.Pilotage(name = "pilotage"),
                    schemas = Workspace.WorkspaceEntry.OfficeEntry.Office.LibraryEntry.Schemas(name = "schemas"),
                    slides = Workspace.WorkspaceEntry.OfficeEntry.Office.LibraryEntry.Slides(
                        path = "${
                            System.getProperty(
                                "user.home"
                            )
                        }/workspace/bibliotheque/slides"
                    ),
                    sites = Workspace.WorkspaceEntry.OfficeEntry.Office.LibraryEntry.Sites(name = "sites")
                ),
                organisation = Workspace.WorkspaceEntry.OrganisationEntry.Organisation(organisation = "organisation"),
                collaboration = Workspace.WorkspaceEntry.CollaborationEntry.Collaboration(collaboration = "collaboration"),
                dashboard = Workspace.WorkspaceEntry.DashboardEntry.Dashboard(dashboard = "dashboard"),
                portfolio = Workspace.WorkspaceEntry.PortfolioEntry.Portfolio(
                    mutableMapOf(
                        "school" to Workspace.WorkspaceEntry.PortfolioEntry.Portfolio.PortfolioProject(
                            name = "name",
                            cred = "credential",
                            builds = mutableMapOf(
                                "training" to Workspace.WorkspaceEntry.PortfolioEntry.Portfolio.PortfolioProject.ProjectBuild(
                                    name = "training"
                                )
                            )
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