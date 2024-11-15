@file:Suppress("unused")

package workspace

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import workspace.Workspace.WorkspaceEntry.OfficeEntry.Office
import workspace.Workspace.WorkspaceEntry.OfficeEntry.Office.LibraryEntry.Slides
import java.nio.file.Path


data class Workspace(val workspace: WorkspaceEntry) {
    enum class InstallationType {
        ALL_IN_ONE,
        SEPARATED_FOLDERS
    }

    data class WorkspaceConfig(
        val basePath: Path,
        val type: InstallationType,
        val subPaths: Map<String, Path> = emptyMap()
    )

    data class WorkspaceEntry(
        val name: String,
        val path: String,
        val office: Office,
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
                val slides: Slides,
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

    fun someLibraryMethod(): Boolean = true

    companion object {
        @JvmStatic
        fun install(path: String) = println("Installing workspace on path : $path")
    }
}

fun Workspace.displayWorkspaceStructure(): Unit = toYaml.run(::println)

val Workspace.toYaml: String
    get() = YAMLMapper().writeValueAsString(this)
