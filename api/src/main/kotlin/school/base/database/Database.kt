@file:Suppress("unused")

package school.base.database

import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.core.io.FileSystemResource
import org.springframework.data.convert.CustomConversions
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions
import org.springframework.data.r2dbc.dialect.DialectResolver
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.reactive.TransactionalOperator
import school.base.utils.Properties
import school.users.User.UserDao
import workspace.Log.i
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalDateTime.ofInstant
import java.time.ZoneOffset.UTC
import kotlin.text.Charsets.UTF_8

@Configuration
@EnableTransactionManagement
@EnableR2dbcRepositories("school")
class Database(private val properties: Properties) {

    //TODO: https://reflectoring.io/spring-bean-lifecycle/
    fun createSystemUser(): Unit = i("Creating system user")

    @Bean
    fun inMemoryConnectionFactory(
        @Qualifier("connectionFactory")
        connectionFactory: ConnectionFactory
    ): ConnectionFactoryInitializer =
        ConnectionFactoryInitializer().apply {
            setConnectionFactory(connectionFactory)
            setDatabasePopulator(
                ResourceDatabasePopulator(
                    File.createTempFile("prefix", "suffix")
                        .apply { writeText(UserDao.Relations.CREATE_TABLES, UTF_8) }
                        .let(::FileSystemResource)
                )
            )
        }

    @Bean
    fun reactiveTransactionManager(
        connectionFactory: ConnectionFactory
    ): ReactiveTransactionManager = R2dbcTransactionManager(connectionFactory)

    @Bean
    fun transactionalOperator(
        reactiveTransactionManager: ReactiveTransactionManager
    ): TransactionalOperator = TransactionalOperator.create(reactiveTransactionManager)

    @WritingConverter
    class InstantWriteConverter : Converter<Instant, LocalDateTime> {
        override fun convert(source: Instant): LocalDateTime? = ofInstant(source, UTC)
    }

    @ReadingConverter
    class InstantReadConverter : Converter<LocalDateTime, Instant> {
        override fun convert(localDateTime: LocalDateTime): Instant = localDateTime.toInstant(UTC)!!
    }

    @Bean
    fun r2dbcCustomConversions(
        @Qualifier("connectionFactory")
        connectionFactory: ConnectionFactory
    ): R2dbcCustomConversions {
        DialectResolver.getDialect(connectionFactory).apply {
            return@r2dbcCustomConversions R2dbcCustomConversions(
                CustomConversions.StoreConversions.of(
                    simpleTypeHolder,
                    converters.toMutableList().apply {
                        add(InstantWriteConverter())
                        add(InstantReadConverter())
                        addAll(R2dbcCustomConversions.STORE_CONVERTERS)
                    }
                ), mutableListOf<Any>()
            )
        }
    }
}
