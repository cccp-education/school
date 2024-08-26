package workspace

import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.transport.PushResult

sealed class GitOperationResult {
    data class Success(
        val commit: RevCommit, val pushResults: MutableIterable<PushResult>?
    ) : GitOperationResult()

    data class Failure(val error: String) : GitOperationResult()
}

sealed class FileOperationResult {
    object Success : FileOperationResult()
    data class Failure(val error: String) : FileOperationResult()
}

sealed class WorkspaceError {
    object FileNotFound : WorkspaceError()
    data class ParsingError(val message: String) : WorkspaceError()
}