package school.base.utils

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import kotlin.reflect.KClass

@Suppress("unused")
object AppUtils {
  val Pair<Any, ApplicationContext>.toJson: String
    get() = second.getBean<ObjectMapper>().writeValueAsString(first)

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

  @JvmStatic
  val String.upperFirstLetter
    get() = run {
      replaceFirst(
        first(),
        first().uppercaseChar()
      )
    }

  @JvmStatic
  val String.lowerFirstLetter
    get() = run {
      replaceFirst(
        first(),
        first().lowercaseChar()
      )
    }


}