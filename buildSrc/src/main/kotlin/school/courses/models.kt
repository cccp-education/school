package school.courses

@JvmRecord
data class DirectoryStructure(
    val files: List<String> = emptyList(), val directories: Map<String, DirectoryStructure> = emptyMap()
)