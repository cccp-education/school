package workspace.courses

data class DirectoryStructure(
    val files: List<String> = emptyList(), val directories: Map<String, DirectoryStructure> = emptyMap()
)