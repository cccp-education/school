package workspace

import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger

object Log {

    private val log: Logger by lazy { getLogger(Workspace::class.java) }

    fun i(message: String): Unit = log.info(message)
    fun d(message: String): Unit = log.debug(message)
    fun w(message: String): Unit = log.warn(message)
    fun t(message: String): Unit = log.trace(message)
    fun e(message: String): Unit = log.error(message)
    fun e(message: String, defaultMessage: String?): Unit = log.error(message, defaultMessage)
    fun e(message: String, e: Exception?): Unit = log.error(message, e)
    fun w(message: String, e: Exception?): Unit = log.warn(message, e)
}