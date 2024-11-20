package app.utils

import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.context.MessageSource
import app.utils.Constants.CLOUD
import app.utils.Constants.DEVELOPMENT
import app.utils.Constants.DEV_HOST
import app.utils.Constants.EMPTY_CONTEXT_PATH
import app.utils.Constants.EMPTY_STRING
import app.utils.Constants.HTTP
import app.utils.Constants.HTTPS
import app.utils.Constants.LINE
import app.utils.Constants.PRODUCTION
import app.utils.Constants.SERVER_PORT
import app.utils.Constants.SERVER_SERVLET_CONTEXT_PATH
import app.utils.Constants.SERVER_SSL_KEY_STORE
import app.utils.Constants.SPRING_APPLICATION_NAME
import app.utils.Constants.STARTUP_HOST_WARN_LOG_MSG
import app.utils.Constants.STARTUP_LOG_MSG_KEY
import workspace.Log.e
import workspace.Log.i
import workspace.Log.w
import java.net.InetAddress.getLocalHost
import java.net.UnknownHostException
import java.util.Locale.getDefault

object LoggerUtils {

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
        ).run(LoggerUtils::startupLogMessage)
            .run(::i)
    }

    @JvmRecord
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