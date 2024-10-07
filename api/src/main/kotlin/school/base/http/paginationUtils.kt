package school.base.configurations.http

import org.springframework.data.domain.Page
import org.springframework.http.HttpHeaders
import org.springframework.web.util.UriComponentsBuilder
import java.text.MessageFormat


/*=================================================================================*/
/**
 * Utility class for handling pagination.
 *
 *
 *
 * Pagination uses the same principles as the [GitHub API](https://developer.github.com/v3/#pagination),
 * and follow [RFC 5988 (Link header)](http://tools.ietf.org/html/rfc5988).
 */
private const val HEADER_X_TOTAL_COUNT = "X-Total-Count"
private const val HEADER_LINK_FORMAT = "<{0}>; rel=\"{1}\""
/*=================================================================================*/

/**
 * Generate pagination headers for a Spring Data [org.springframework.data.domain.Page] object.
 *
 * @param uriBuilder The URI builder.
 * @param page The page.
 * @param <T> The type of object.
 * @return http header.
</T> */
fun <T> generatePaginationHttpHeaders(uriBuilder: UriComponentsBuilder, page: Page<T>): HttpHeaders {
    val headers = HttpHeaders()
    headers.add(HEADER_X_TOTAL_COUNT, page.totalElements.toString())
    val pageNumber = page.number
    val pageSize = page.size
    val link = StringBuilder()
    if (pageNumber < page.totalPages - 1) {
        link.append(
            prepareLink(
                uriBuilder = uriBuilder,
                pageNumber = pageNumber + 1,
                pageSize = pageSize,
                relType = "next"
            )
        ).append(",")
    }
    if (pageNumber > 0) {
        link.append(prepareLink(uriBuilder, pageNumber - 1, pageSize, "prev"))
            .append(",")
    }
    link.append(prepareLink(uriBuilder, page.totalPages - 1, pageSize, "last"))
        .append(",")
        .append(prepareLink(uriBuilder, 0, pageSize, "first"))
    headers.add(HttpHeaders.LINK, link.toString())
    return headers
}
/*=================================================================================*/

private fun prepareLink(
    uriBuilder: UriComponentsBuilder,
    pageNumber: Int,
    pageSize: Int,
    relType: String
): String = MessageFormat.format(
    HEADER_LINK_FORMAT,
    preparePageUri(
        uriBuilder = uriBuilder,
        pageNumber = pageNumber,
        pageSize = pageSize
    ),
    relType
)
/*=================================================================================*/

private fun preparePageUri(
    uriBuilder: UriComponentsBuilder,
    pageNumber: Int,
    pageSize: Int
): String = uriBuilder.replaceQueryParam(
    "page",
    pageNumber.toString()
).replaceQueryParam(
    "size",
    pageSize.toString()
).toUriString()
    .replace(oldValue = ",", newValue = "%2C")
    .replace(oldValue = ";", newValue = "%3B")

/*=================================================================================*/
