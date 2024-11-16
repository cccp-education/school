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
import java.io.File
import kotlin.test.*

class WorkspaceTest {
    @BeforeTest
    fun setUp() = Unit

    @AfterTest
    fun tearDown() = Unit

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

    @Test
    fun `test create workspace with ALL_IN_ONE config`(): Unit {
        val path = "build/workspace"
        val configFileName = "config.yaml"

        path.run(::File)
            .apply {
                when {
                    !exists() -> mkdirs().run(::assertTrue)
                }
            }
            .run {
                exists().run(::assertTrue)
                isDirectory.run(::assertTrue)
                WorkspaceConfig(
                    basePath = toPath(),
                    type = ALL_IN_ONE,
                ).run(WorkspaceManager::createWorkspace)
//                listOf(
//                    "office",
//                    "education",
//                    "communication",
//                    "configuration",
//                    "job",
//                ).forEach { "$this/$it".run(::File).exists().run(::assertTrue) }
                "$path/$configFileName".run(::File).exists().run(::assertTrue)
                deleteRecursively().run(::assertTrue)
            }
    }

    @Ignore
    @Test
    fun `test create workspace with SEPARATED_FOLDERS config`(): Unit {
        //check if mapPath from WorkspaceConfig exists
        //must provide existing folders
        "build/workspace"
            .run(::File)
            .apply { if (!exists()) mkdirs().run(::assertTrue) }
            .run {
                exists().run(::assertTrue)
                isDirectory.run(::assertTrue)
                WorkspaceConfig(
                    basePath = toPath(),
                    type = ALL_IN_ONE,
                ).run(WorkspaceManager::createWorkspace)
                listOf(
                    "office",
                    "education",
                    "communication",
                    "configuration",
                    "job",
                ).forEach { "$this/$it".run(::File).exists().run(::assertTrue) }

//                val config = WorkspaceConfig(
//                    type = SEPARATED_FOLDERS,
//                    basePath = context.classLoader?.getResource("workspace")!!.toURI().toPath(),
//                    subPaths = mutableMapOf(
//                        "office" to context.classLoader?.getResource("office")!!.toURI().toPath(),
//                        "education" to context.classLoader?.getResource("education")!!.toURI().toPath(),
//                        "communication" to context.classLoader?.getResource("communication")!!.toURI().toPath(),
//                        "configuration" to context.classLoader?.getResource("configuration")!!.toURI().toPath(),
//                        "job" to context.classLoader?.getResource("job")!!.toURI().toPath(),
//                    ),
//                )
                deleteRecursively().run(::assertTrue)
            }
    }
//TODO: move this test into AppllicationContextUtilsExtensionFunctionsTests into api project
//    @Ignore
//    @Test
//    fun `test lsWorkingDir & lsWorkingDiringDirProcess`(): Unit {
//        val destDir: File = "build".run(::File)
//        context.lsWorkingDirProcess(destDir).run { "lsWorkingDirProcess : $this" }.run(::i)
//        File("build").absolutePath.run(::i)
//
//        // Liste un répertoire spécifié par une chaîne
//        context.lsWorkingDir("build", maxDepth = 2)
//
//        // Liste un répertoire spécifié par un Path
//        context.lsWorkingDir(Paths.get("build"))
//    }

}