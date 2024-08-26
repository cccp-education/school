package webapp.core.i18n

import org.springframework.context.annotation.Configuration
import org.springframework.context.i18n.LocaleContext
import org.springframework.context.i18n.SimpleLocaleContext
import org.springframework.format.FormatterRegistry
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar
import org.springframework.web.reactive.config.DelegatingWebFluxConfiguration
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.i18n.LocaleContextResolver
import webapp.core.property.REQUEST_PARAM_LANG
import java.util.*

@Suppress("unused")
@Configuration
class I18n : DelegatingWebFluxConfiguration() {

    override fun addFormatters(registry: FormatterRegistry) {
        DateTimeFormatterRegistrar().apply {
            setUseIsoFormat(true)
            registerFormatters(registry)
        }
    }

    override fun createLocaleContextResolver(): LocaleContextResolver = RequestParamLocaleContextResolver()

    class RequestParamLocaleContextResolver : LocaleContextResolver {
        override fun resolveLocaleContext(exchange: ServerWebExchange): LocaleContext {
            var targetLocale = Locale.getDefault()
            val referLang = exchange.request.queryParams[REQUEST_PARAM_LANG]
            if (!referLang.isNullOrEmpty()) targetLocale = Locale.forLanguageTag(referLang[0])
            return SimpleLocaleContext(targetLocale)
        }

        @Throws(UnsupportedOperationException::class)
        override fun setLocaleContext(
            exchange: ServerWebExchange, localeContext: LocaleContext?
        ): Unit = throw UnsupportedOperationException("Not Supported")
    }
}