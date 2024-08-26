package webapp.core.utils

import org.apache.commons.lang3.StringUtils
import kotlin.reflect.KClass

@Suppress("unused")
object AppUtils {
  val KClass<Any>.objectName
    get() = java.simpleName.run {
      replaceFirst(
        first(),
        first().lowercaseChar()
      )
    }

  fun List<String>.nameToLogin(): List<String> = map { StringUtils.stripAccents(it.lowercase().replace(' ', '.')) }

  fun String.cleanField(): String = StringBuilder(this)
    .deleteCharAt(0)
    .deleteCharAt(length - 2)
    .toString()
}