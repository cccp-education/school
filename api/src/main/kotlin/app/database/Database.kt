package app.database

import app.utils.Properties
import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.core.io.FileSystemResource
import org.springframework.data.convert.CustomConversions.StoreConversions
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions.STORE_CONVERTERS
import org.springframework.data.r2dbc.dialect.DialectResolver.getDialect
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.TransactionalOperator.create
import users.UserDao.Relations.CREATE_TABLES
import workspace.Log.i
import java.io.File.createTempFile
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalDateTime.ofInstant
import java.time.ZoneOffset.UTC

@Configuration
@EnableTransactionManagement
@EnableR2dbcRepositories("app", "ai", "users")
class Database(private val properties: Properties) {

    //TODO: https://reflectoring.io/spring-bean-lifecycle/
    fun createSystemUser(): Unit = i("Creating system user")

    @Bean
    fun connectionFactoryInitializer(
        @Qualifier("connectionFactory")
        connectionFactory: ConnectionFactory
    ): ConnectionFactoryInitializer =
        ConnectionFactoryInitializer().apply {
            setConnectionFactory(connectionFactory)
            setDatabasePopulator(
                ResourceDatabasePopulator(
                    createTempFile("prefix", "suffix")
                        .apply { CREATE_TABLES.run(::writeText) }
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
    ): TransactionalOperator = create(reactiveTransactionManager)

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
        connectionFactory
            .run(::getDialect)
            .run {
                return@r2dbcCustomConversions R2dbcCustomConversions(
                    StoreConversions.of(
                        simpleTypeHolder,
                        converters.toMutableList().apply {
                            add(InstantWriteConverter())
                            add(InstantReadConverter())
                            addAll(STORE_CONVERTERS)
                        }
                    ), mutableListOf<Any>()
                )
            }
    }
}