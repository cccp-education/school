package school.base.http

import org.springframework.http.HttpHeaders
import school.base.utils.Log.e
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import kotlin.text.Charsets.UTF_8

/**
 * Utility extension HttpHeaders functions for HTTP headers creation.
 */

    /**
     *
     * createAlert.
     *
     * @param applicationName a [java.lang.String] object.
     * @param message a [java.lang.String] object.
     * @param param a [java.lang.String] object.
     * @return a [org.springframework.http.HttpHeaders] object.
     */
    private fun HttpHeaders.createAlert(
        applicationName: String,
        message: String?,
        param: String?
    ): HttpHeaders = apply {
        add("X-$applicationName-alert", message)
        try {
            add("X-$applicationName-params", @Suppress("Since15") (URLEncoder.encode(param, UTF_8)))
        } catch (_: UnsupportedEncodingException) {
            // StandardCharsets are supported by every Java implementation
            // so this exceptions will never happen
        }
    }

    /**
     *
     * createEntityCreationAlert.
     *
     * @param applicationName a [java.lang.String] object.
     * @param enableTranslation a boolean.
     * @param entityName a [java.lang.String] object.
     * @param param a [java.lang.String] object.
     * @return a [org.springframework.http.HttpHeaders] object.
     */
    fun HttpHeaders.createEntityCreationAlert(
        applicationName: String,
        enableTranslation: Boolean,
        entityName: String,
        param: String
    ): HttpHeaders = createAlert(
        applicationName,
        when {
            enableTranslation -> "$applicationName.$entityName.created"
            else -> "A new $entityName is created with identifier $param"
        },
        param
    )

    /**
     *
     * createEntityUpdateAlert.
     *
     * @param applicationName a [java.lang.String] object.
     * @param enableTranslation a boolean.
     * @param entityName a [java.lang.String] object.
     * @param param a [java.lang.String] object.
     * @return a [org.springframework.http.HttpHeaders] object.
     */
    fun HttpHeaders.createEntityUpdateAlert(
        applicationName: String,
        enableTranslation: Boolean,
        entityName: String,
        param: String
    ): HttpHeaders = createAlert(
        applicationName,
        if (enableTranslation) "$applicationName.$entityName.updated"
        else "A $entityName is updated with identifier $param",
        param
    )

    /**
     *
     * createEntityDeletionAlert.
     *
     * @param applicationName a [java.lang.String] object.
     * @param enableTranslation a boolean.
     * @param entityName a [java.lang.String] object.
     * @param param a [java.lang.String] object.
     * @return a [org.springframework.http.HttpHeaders] object.
     */
    fun HttpHeaders.createEntityDeletionAlert(
        applicationName: String,
        enableTranslation: Boolean,
        entityName: String,
        param: String
    ): HttpHeaders = createAlert(
        applicationName,
        if (enableTranslation) "$applicationName.$entityName.deleted"
        else "A $entityName is deleted with identifier $param",
        param
    )

    /**
     *
     * createFailureAlert.
     *
     * @param applicationName a [java.lang.String] object.
     * @param enableTranslation a boolean.
     * @param entityName a [java.lang.String] object.
     * @param errorKey a [java.lang.String] object.
     * @param defaultMessage a [java.lang.String] object.
     * @return a [org.springframework.http.HttpHeaders] object.
     */
    fun HttpHeaders.createFailureAlert(
        applicationName: String,
        enableTranslation: Boolean,
        entityName: String?,
        errorKey: String,
        defaultMessage: String?
    ): HttpHeaders = e(
        "Entity processing failed, {}",
        defaultMessage
    ).run {
        return@run HttpHeaders().apply {
            add(
                "X-$applicationName-error",
                if (enableTranslation) "error.$errorKey"
                else defaultMessage!!
            )
            add("X-$applicationName-params", entityName)
        }
    }
