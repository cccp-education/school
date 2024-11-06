package school.base.utils


import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.context.MessageSource
import school.Application
import school.base.utils.Constants.CLOUD
import school.base.utils.Constants.DEVELOPMENT
import school.base.utils.Constants.DEV_HOST
import school.base.utils.Constants.EMPTY_CONTEXT_PATH
import school.base.utils.Constants.EMPTY_STRING
import school.base.utils.Constants.HTTP
import school.base.utils.Constants.HTTPS
import school.base.utils.Constants.LINE
import school.base.utils.Constants.PRODUCTION
import school.base.utils.Constants.SERVER_PORT
import school.base.utils.Constants.SERVER_SERVLET_CONTEXT_PATH
import school.base.utils.Constants.SERVER_SSL_KEY_STORE
import school.base.utils.Constants.SPRING_APPLICATION_NAME
import school.base.utils.Constants.STARTUP_HOST_WARN_LOG_MSG
import school.base.utils.Constants.STARTUP_LOG_MSG_KEY
import java.net.InetAddress.getLocalHost
import java.net.UnknownHostException
import java.util.Locale.getDefault


object Log {
    private val log: Logger by lazy { getLogger(Application::class.java) }

    fun i(message: String): Unit = log.info(message)
    fun d(message: String): Unit = log.debug(message)
    fun w(message: String): Unit = log.warn(message)
    fun t(message: String): Unit = log.trace(message)
    fun e(message: String): Unit = log.error(message)
    fun e(message: String, defaultMessage: String?): Unit = log.error(message, defaultMessage)
    fun e(message: String, e: Exception?): Unit = log.error(message, e)
    fun w(message: String, e: Exception?): Unit = log.warn(message, e)

    @JvmStatic
    fun ApplicationContext.startupLog() = logProfiles.run {
        StartupLogMsg(
            appName = SPRING_APPLICATION_NAME.run(environment::getProperty),
            goVisitMessage = getBean<Properties>().goVisitMessage,
            protocol = when {
                SERVER_SSL_KEY_STORE.run(environment::getProperty) != null -> HTTPS
                else -> HTTP
            },
            serverPort = SERVER_PORT.run(environment::getProperty),
            contextPath = SERVER_SERVLET_CONTEXT_PATH.run(environment::getProperty) ?: EMPTY_CONTEXT_PATH,
            hostAddress = try {
                getLocalHost().hostAddress
            } catch (e: UnknownHostException) {
                STARTUP_HOST_WARN_LOG_MSG.run(::w)
                DEV_HOST
            },
            profiles = when {
                environment.defaultProfiles.isNotEmpty() -> environment.defaultProfiles.reduce { accumulator, profile -> "$accumulator, $profile" }

                else -> EMPTY_STRING
            },
            activeProfiles = when {
                environment.activeProfiles.isNotEmpty() -> environment.activeProfiles.reduce { accumulator, profile -> "$accumulator, $profile" }

                else -> EMPTY_STRING
            },
        ).run(::startupLogMessage)
            .run(::i)
    }


    private data class StartupLogMsg(
        val appName: String?,
        val goVisitMessage: String,
        val protocol: String,
        val serverPort: String?,
        val contextPath: String,
        val hostAddress: String,
        val profiles: String,
        val activeProfiles: String
    )

    private fun startupLogMessage(startupLogMsg: StartupLogMsg): String = """$LINE$LINE$LINE
----------------------------------------------------------
Go visit ${startupLogMsg.goVisitMessage}    
----------------------------------------------------------
Application '${startupLogMsg.appName}' is running!
Access URLs
    Local:      ${startupLogMsg.protocol}://localhost:${startupLogMsg.serverPort}${startupLogMsg.contextPath}
    External:   ${startupLogMsg.protocol}://${startupLogMsg.hostAddress}:${startupLogMsg.serverPort}${startupLogMsg.contextPath}${
        when {
            startupLogMsg.profiles.isNotBlank() -> LINE + buildString {
                append("Profile(s): ")
                append(startupLogMsg.profiles)
            }

            else -> EMPTY_STRING
        }
    }${
        when {
            startupLogMsg.activeProfiles.isNotBlank() -> LINE + buildString {
                append("Active(s) profile(s): ")
                append(startupLogMsg.activeProfiles)
            }

            else -> EMPTY_STRING
        }
    }
----------------------------------------------------------
$LINE$LINE""".trimIndent()

    private val ApplicationContext.logProfiles: ApplicationContext
        get() = apply {
            environment.activeProfiles.run {
                when {
                    contains(DEVELOPMENT) && contains(PRODUCTION) -> e(
                        getBean<MessageSource>().getMessage(
                            STARTUP_LOG_MSG_KEY,
                            arrayOf(DEVELOPMENT, PRODUCTION),
                            getDefault()
                        )
                    )

                    contains(DEVELOPMENT) && contains(CLOUD) -> e(
                        getBean<MessageSource>().getMessage(
                            STARTUP_LOG_MSG_KEY,
                            arrayOf(DEVELOPMENT, CLOUD),
                            getDefault()
                        )
                    )
                }
            }
        }
}