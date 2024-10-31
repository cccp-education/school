package workspace


data class Workspace(val workspace: WorkspaceEntry) {
    data class WorkspaceEntry(
        val name: String,
        val office: OfficeEntry.Office,
        val cores: Map<String, CoreEntry>,
        val job: JobEntry,
        val configuration: ConfigurationEntry,
        val communication: CommunicationEntry,
        val organisation: OrganisationEntry,
        val collaboration: CollaborationEntry,
        val dashboard: DashboardEntry,
        val portfolio: PortfolioEntry,
    ) {
        sealed interface PortfolioEntry {
            data class Portfolio(val project: MutableMap<String, PortfolioProject>) : PortfolioEntry {
                data class PortfolioProject(
                    val name: String,
                    val cred: String,
                    val builds: MutableMap<String, ProjectBuild>
                ) {
                    data class ProjectBuild(val name: String)
                }
            }
        }

        sealed interface CoreEntry {
            data class Education(
                val school: EducationEntry,
                val student: EducationEntry,
                val teacher: EducationEntry,
                val educationTools: EducationEntry,
            ) : CoreEntry {
                sealed class EducationEntry {
                    data class Student(val name: String) : EducationEntry()
                    data class Teacher(val name: String) : EducationEntry()
                    data class School(val name: String) : EducationEntry()
                    data class EducationTools(val name: String) : EducationEntry()
                }
            }
        }

        sealed interface OfficeEntry {
            data class Office(
                val books: LibraryEntry,
                val datas: LibraryEntry,
                val formations: LibraryEntry,
                val bizness: LibraryEntry,
                val notebooks: LibraryEntry,
                val pilotage: LibraryEntry,
                val schemas: LibraryEntry,
                val slides: LibraryEntry.Slides,
                val sites: LibraryEntry,
            ) : OfficeEntry {
                sealed class LibraryEntry {
                    data class Books(val name: String) : LibraryEntry()
                    data class Datas(val name: String) : LibraryEntry()
                    data class TrainingCatalogue(val catalogue: String) : LibraryEntry()
                    data class Notebooks(val notebooks: String) : LibraryEntry()
                    data class Pilotage(val name: String) : LibraryEntry()
                    data class Schemas(val name: String) : LibraryEntry()
                    data class Slides(val path: String) : LibraryEntry()
                    data class Sites(val name: String) : LibraryEntry()
                    data class Profession(val name: String) : LibraryEntry()
                }
            }
        }

        sealed interface JobEntry {
            data class Job(
                val position: HumanResourcesEntry,
                val resume: HumanResourcesEntry
            ) : JobEntry {
                sealed class HumanResourcesEntry {
                    data class Resume(val name: String) : HumanResourcesEntry()
                    data class Position(val name: String) : HumanResourcesEntry()
                }
            }
        }

        sealed interface ConfigurationEntry {
            data class Configuration(val configuration: String) : ConfigurationEntry
        }

        sealed interface CommunicationEntry {
            data class Communication(val site: String) : CommunicationEntry
        }

        sealed interface OrganisationEntry {
            data class Organisation(val organisation: String) : OrganisationEntry
        }

        sealed interface CollaborationEntry {
            data class Collaboration(val collaboration: String) : CollaborationEntry
        }

        sealed interface DashboardEntry {
            data class Dashboard(val dashboard: String) : DashboardEntry
        }
    }
    fun someLibraryMethod(): Boolean {
        return true
    }
}