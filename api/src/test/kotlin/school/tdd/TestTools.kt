@file:Suppress("unused")

package school.tdd


//import school.accounts.models.Account
//import school.accounts.models.AccountCredentials
//import school.accounts.models.AccountUtils.generateActivationKey
//import school.users.entities.AccountAuthorityEntity
//import school.users.entities.AccountEntity
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Description
import org.hamcrest.TypeSafeDiagnosingMatcher
import org.springframework.boot.runApplication
import org.springframework.boot.web.reactive.context.StandardReactiveWebEnvironment
import org.springframework.context.ConfigurableApplicationContext
import school.Application
import school.base.utils.*
import school.base.utils.Constants.TEST
import school.base.utils.Constants.VIRGULE
import workspace.Log.i
import java.io.IOException
import java.lang.Byte.parseByte
import java.time.ZonedDateTime
import java.time.ZonedDateTime.parse
import java.time.format.DateTimeParseException
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance


object TestTools {

//@Suppress("MemberVisibilityCanBePrivate")
//object DataTests {
//    val adminAccount by lazy { accountCredentialsFactory(ADMIN) }
//    val defaultAccount by lazy { accountCredentialsFactory(USER) }
//    val accounts = setOf(adminAccount, defaultAccount)
//    const val DEFAULT_ACCOUNT_JSON = """{
//    "login": "$USER",
//    "firstName": "$USER",
//    "lastName": "$USER",
//    "email": "$USER@$DOMAIN_DEV_URL",
//    "password": "$USER",
//    "imageUrl": "http://placehold.it/50x50"
//}"""
//}


//fun accountCredentialsFactory(login: String): AccountCredentials =
//    AccountCredentials(
//        password = login,
//        login = login,
//        firstName = login,
//        lastName = login,
//        email = "$login@$DOMAIN_DEV_URL",
//        imageUrl = "http://placehold.it/50x50",
//    )


    fun launcher(
        vararg profiles: String, userAuths: Set<Pair<String, String>> = emptySet()
    ): ConfigurableApplicationContext = runApplication<Application> {
        /**
         * before launching: configuration
         */
        /**
         * before launching: configuration
         */
        setEnvironment(StandardReactiveWebEnvironment().apply {
            setDefaultProfiles(TEST)
            addActiveProfile(TEST)
            profiles.toSet().map(::addActiveProfile)
        })
    }.apply {
        /**
         * after launching: verification & post construct
         */
        (when {
            environment.defaultProfiles.isNotEmpty() -> environment.defaultProfiles.reduce { acc, s -> "$acc, $s" }

            else -> ""
        }).let { "defaultProfiles : $it" }.let(::i)

        (when {
            environment.activeProfiles.isNotEmpty() -> environment.activeProfiles.reduce { acc, s -> "$acc, $s" }

            else -> ""
        }).let { "activeProfiles : $it" }.let(::i)

        //TODO: ajouter des users avec leurs roles
    }


//TODO: refaire en sql
//    suspend fun deleteAllUsers() {
//        findAllUsers().map {
//            it.unlockUser()
//        }.run {
//            deleteAllUserAuthorities()
//            deleteAllUsersWithoutUserAuthorites()
//        }
//    }

//    suspend fun deleteUserByIdWithAuthorities(id: UUID) =
//        deleteAllUserAuthorityByUserId(id).also {
//            r2dbcEntityTemplate.delete(AccountEntity::class.java)
//                .matching(Query.query(Criteria.where("id").`is`(id)))
//                .allAndAwait()
//        }
//
//    suspend fun deleteUserByLoginWithAuthorities(login: String) =
//        deleteAllUserAuthorityByUserLogin(login).also {
//            r2dbcEntityTemplate.delete(AccountEntity::class.java)
//                .matching(Query.query(Criteria.where("login").`is`(login)))
//                .allAndAwait()
//        }
//
//    suspend fun logCountUser() = i("countUser: ${countUser()}")
//
//    suspend fun logCountUserAuthority() = log
//        .info("countUserAuthority: ${countUserAuthority()}")


/////////////////////////////////////////////////////////
//    suspend fun saveUser(u: AccountEntity): AccountEntity? = r2dbcEntityTemplate
//        .insert(u)
//        .awaitFirstOrNull()
//
//    suspend fun saveUserWithAutorities(user: AccountEntity): AccountEntity? = r2dbcEntityTemplate
//        .insert(user)
//        .awaitSingle().apply {
//            authorities?.forEach {
//                if (id != null)
//                    r2dbcEntityTemplate
//                        .insert(
//                            AccountAuthority(
//                                userId = id!!,
//                                role = it.role
//                            )
//                        )
//                        .awaitSingle()
//            }
//        }
//
//    suspend fun findAllAuthorites(): Flow<AccountAuthority> = r2dbcEntityTemplate
//        .select(AccountAuthority::class.java)
//        .flow<AccountAuthority>()
//
//    suspend fun findOneUserByEmail(email: String): AccountAuthority? = r2dbcEntityTemplate
//        .select(AccountAuthority::class.java)
//        .matching(Query.query(Criteria.where("email").`is`(email)))
//        .awaitOneOrNull()
//
//    suspend fun findOneUserByLogin(login: String): AccountAuthority? = r2dbcEntityTemplate
//        .select(AccountAuthority::class.java)
//        .matching(Query.query(Criteria.where("login").`is`(login)))
//        .awaitOneOrNull()
//
//    suspend fun findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(
//        dateTime: LocalDateTime
//    ): Flow<AccountAuthority> = r2dbcEntityTemplate
//        .select(AccountAuthority::class.java)
//        .matching(
//            Query.query(
//                Criteria.where("activated")
//                    .`is`(false)
//                    .and("activation_key")
//                    .isNotNull
//                    .and("created_date").lessThan(dateTime)
//            )
//        ).flow()
//
//
//    suspend fun checkInitDatabaseWithDefaultUser(): AccountEntity =
//        saveUserWithAutorities(
//            defaultAccountEntity
//                .copy()
//                .apply {
//                    unlockUser()
//                    activated = true
//                }.run {
//                    deleteUserByLoginWithAuthorities(login!!)
//                    return@run this
//                }
//        )?.apply {
//            assertNotNull(id)
//            assertTrue(activated)
//            assertEquals(defaultAccount.email, email)
//            assertEquals(defaultAccount.login, login)
//        }!!
//
//    suspend fun logUsers() {
//        findAllUsers().apply {
//            if (count() == 1 || count() == 0)
//                i(single()::toString)
//            else map { i(it::toString) }
//        }
//    }

//}

    fun ByteArray.requestToString(): String = map {
        it.toInt().toChar().toString()
    }.reduce { acc: String, s: String -> acc + s }

    //TODO : change that ugly json formating
    fun ByteArray.logBody(): ByteArray = apply {
        if (isNotEmpty()) map { it.toInt().toChar().toString() }.reduce { request, s ->
                request + buildString {
                    append(s)
                    if (s == VIRGULE && request.last().isDigit()) append("\n\t")
                }
            }.replace("{\"", "\n{\n\t\"").replace("\"}", "\"\n}").replace("\",\"", "\",\n\t\"")
            .run { i("\nbody:$this") }
    }

    fun ByteArray.logBodyRaw(): ByteArray = apply {
        if (isNotEmpty()) map {
            it.toInt().toChar().toString()
        }.reduce { request, s -> request + s }.run { i(this) }
    }

//fun createDataAccounts(accounts: Set<AccountCredentials>, dao: R2dbcEntityTemplate) {
//    assertEquals(0, countAccount(dao))
//    assertEquals(0, countAccountAuthority(dao))
//    accounts.map { acc ->
//        AccountEntity(acc.copy(
//            activationKey = generateActivationKey,
//            langKey = DEFAULT_LANGUAGE,
//            createdBy = SYSTEM_USER,
//            createdDate = now(),
//            lastModifiedBy = SYSTEM_USER,
//            lastModifiedDate = now(),
//            authorities = mutableSetOf(ROLE_USER).apply {
//                if (acc.login == ADMIN) add(ROLE_ADMIN)
//            }
//        )).run {
//            dao.insert(this).block()!!.id!!.let { uuid ->
//                authorities!!.map { authority ->
//                    dao.insert(
//                        AccountAuthorityEntity(
//                            userId = uuid,
//                            role = authority.role
//                        )
//                    ).block()
//                }
//            }
//        }
//    }
//    assertEquals(accounts.size, countAccount(dao))
//    assertTrue(accounts.size <= countAccountAuthority(dao))
//}


//fun createActivatedDataAccounts(accounts: Set<AccountCredentials>, dao: R2dbcEntityTemplate) {
//    assertEquals(0, countAccount(dao))
//    assertEquals(0, countAccountAuthority(dao))
//    accounts.map { acc ->
//        AccountEntity(
//            acc.copy(
//                activated = true,
//                langKey = DEFAULT_LANGUAGE,
//                createdBy = SYSTEM_USER,
//                createdDate = now(),
//                lastModifiedBy = SYSTEM_USER,
//                lastModifiedDate = now(),
//                authorities = mutableSetOf(ROLE_USER).apply {
//                    if (acc.login == ADMIN) add(ROLE_ADMIN)
//                }.toSet()
//            )
//        ).run {
//            dao.insert(this).block()!!.id!!.let { uuid ->
//                authorities!!.map { authority ->
//                    dao.insert(
//                        AccountAuthorityEntity(
//                            userId = uuid,
//                            role = authority.role
//                        )
//                    ).block()
//                }
//            }
//        }
//    }
//    assertEquals(accounts.size, countAccount(dao))
//    assertTrue(accounts.size <= countAccountAuthority(dao))
//}

//fun deleteAllAccounts(dao: R2dbcEntityTemplate) {
//    deleteAllAccountAuthority(dao)
//    deleteAccounts(dao)
//    assertEquals(0, countAccount(dao))
//    assertEquals(0, countAccountAuthority(dao))
//}

//fun deleteAccounts(repository: R2dbcEntityTemplate) {
//    repository.delete(AccountEntity::class.java).all().block()
//}

//fun deleteAllAccountAuthority(dao: R2dbcEntityTemplate) {
//    dao.delete(AccountAuthorityEntity::class.java).all().block()
//}

////TODO: revoir les updates avec id!=null
//fun saveAccount(model: AccountCredentials, dao: R2dbcEntityTemplate): Account? =
//    when {
//        model.id != null -> dao.update(
//            AccountEntity(model).copy(
//                version = dao.selectOne(
//                    query(
//                        where("login")
//                            .`is`(model.login!!)
//                            .ignoreCase(true)
//                    ),
//                    AccountEntity::class.java
//                ).block()!!.version
//            )
//        ).block()?.toModel
//
//        else -> dao.insert(AccountEntity(model)).block()?.toModel
//    }

//fun saveAccountAuthority(
//    id: UUID,
//    role: String,
//    dao: R2dbcEntityTemplate
//): AccountAuthorityEntity? =
//    dao.insert(AccountAuthorityEntity(userId = id, role = role)).block()


//fun countAccount(dao: R2dbcEntityTemplate): Int =
//    dao.select(AccountEntity::class.java).count().block()?.toInt()!!


//fun countAccountAuthority(dao: R2dbcEntityTemplate): Int =
//    dao.select(AccountAuthorityEntity::class.java).count().block()?.toInt()!!


//fun findOneByLogin(login: String, dao: R2dbcEntityTemplate): AccountCredentials? =
//    dao.select<AccountEntity>()
//        .matching(query(where("login").`is`(login).ignoreCase(true)))
//        .one().block()?.toCredentialsModel

//fun findOneByEmail(email: String, dao: R2dbcEntityTemplate): AccountCredentials? = dao
//    .select<AccountEntity>()
//    .matching(query(where("email").`is`(email).ignoreCase(true)))
//    .one().block()?.toCredentialsModel

//fun findAllAccountAuthority(dao: R2dbcEntityTemplate): Set<AccountAuthorityEntity> =
//    dao.select(AccountAuthorityEntity::class.java).all().toIterable().toHashSet()

    private fun createObjectMapper() = ObjectMapper().apply {
        configure(WRITE_DURATIONS_AS_TIMESTAMPS, false)
        setSerializationInclusion(NON_EMPTY)
        registerModule(JavaTimeModule())
    }

    /**
     * Convert an object to JSON byte array.
     *
     * @param object the object to convert.
     * @return the JSON byte array.
     * @throws IOException
     */
    @Throws(IOException::class)
    fun convertObjectToJsonBytes(`object`: Any): ByteArray = createObjectMapper().writeValueAsBytes(`object`)

    /**
     * Create a byte array with a specific size filled with specified data.
     *
     * @param size the size of the byte array.
     * @param data the data to put in the byte array.
     * @return the JSON byte array.
     */
    fun createByteArray(size: Int, data: String) = ByteArray(size) { parseByte(data, 2) }

    /**
     * A matcher that tests that the examined string represents the same instant as the reference datetime.
     */
    class ZonedDateTimeMatcher(private val date: ZonedDateTime) : TypeSafeDiagnosingMatcher<String>() {

        override fun matchesSafely(item: String, mismatchDescription: Description): Boolean {
            try {
                if (!date.isEqual(parse(item))) {
                    mismatchDescription.appendText("was ").appendValue(item)
                    return false
                }
                return true
            } catch (e: DateTimeParseException) {
                mismatchDescription.appendText("was ").appendValue(item)
                    .appendText(", which could not be parsed as a ZonedDateTime")
                return false
            }
        }

        override fun describeTo(description: Description) {
            description.appendText("a String representing the same Instant as ").appendValue(date)
        }
    }

    /**
     * Creates a matcher that matches when the examined string represents the same instant as the reference datetime.
     * @param date the reference datetime against which the examined string is checked.
     */
    fun sameInstant(date: ZonedDateTime) = ZonedDateTimeMatcher(date)

    /**
     * Verifies the equals/hashcode contract on the domain object.
     */
    fun <T : Any> equalsVerifier(clazz: KClass<T>) {
        clazz.createInstance().apply i@{
            assertThat(toString()).isNotNull
            assertThat(this).isEqualTo(this)
            assertThat(hashCode()).isEqualTo(hashCode())
            // Test with an instance of another class
            assertThat(this).isNotEqualTo(Any())
            assertThat(this).isNotEqualTo(null)
            // Test with an instance of the same class
            clazz.createInstance().apply j@{
                assertThat(this@i).isNotEqualTo(this@j)
                // HashCodes are equals because the objects are not persisted yet
                assertThat(this@i.hashCode()).isEqualTo(this@j.hashCode())
            }
        }
    }

    val token64Zero
        get() = mutableListOf<String>().apply {
            repeat(64) { add(0.toString()) }
        }.reduce { acc, i -> "$acc$i" }


    val writers = listOf(
        "Karl Marx",
        "Jean-Jacques Rousseau",
        "Victor Hugo",
        "Platon",
        "René Descartes",
        "Socrate",
        "Homère",
        "Paul Verlaine",
        "Claude Roy",
        "Bernard Friot",
        "François Bégaudeau",
        "Frederic Lordon",
        "Antonio Gramsci",
        "Georg Lukacs",
        "Franz Kafka",
        "Arthur Rimbaud",
        "Gérard de Nerval",
        "Paul Verlaine",
        "Rocé",
        "Chrétien de Troyes",
        "François Rabelais",
        "Montesquieu",
        "Georg Hegel",
        "Friedrich Engels",
        "Voltaire",
    )

}