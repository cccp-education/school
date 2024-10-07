//@file:Suppress("NonAsciiCharacters")
package school.users
//package school.repository
//
//import kotlinx.coroutines.runBlocking
//import org.junit.jupiter.api.AfterAll
//import org.junit.jupiter.api.AfterEach
//import org.junit.jupiter.api.BeforeAll
//import org.springframework.beans.factory.getBean
//import org.springframework.context.ConfigurableApplicationContext
//import org.springframework.data.r2dbc.base.R2dbcEntityTemplate
//import school.*
//import school.DataTests.accounts
//import school.DataTests.defaultAccount
//import school.accounts.models.AccountUtils.generateActivationKey
//import school.accounts.models.toAccount
//import school.accounts.repository.AccountRepository
//import school.accounts.repository.AccountRepositoryR2dbc
//import school.base.property.DEFAULT_LANGUAGE
//import school.base.property.ROLE_USER
//import school.base.property.SYSTEM_USER
//import java.time.Instant
//import kotlin.test.Test
//import kotlin.test.assertEquals
//import kotlin.test.assertNotNull
//
//
//internal class AccountRepositoryR2dbcTest {
//    private lateinit var context: ConfigurableApplicationContext
//
//    private val dao: R2dbcEntityTemplate by lazy { context.getBean() }
//    private val accountRepository: AccountRepository by lazy { context.getBean<AccountRepositoryR2dbc>() }
//
//    //    @BeforeAll
////    fun `lance le server en profile test`() = runApplication<Application> {
////        testLoader(this)
////    }.run { context = this }
//    @BeforeAll
//    fun `lance le server en profile test`() {
//        context = launcher()
//    }
//
//    @AfterAll
//    fun `arrête le serveur`() = context.close()
//
//
//    @AfterEach
//    fun tearDown() = deleteAllAccounts(dao)
//
//
//    @Test
//    fun test_save() = runBlocking {
//            val countBefore = countAccount(dao)
//            assertEquals(0, countBefore)
//            accountRepository.save(defaultAccount)
//            assertEquals(countBefore + 1, countAccount(dao))
//    }
//
//    @Test
//    fun test_delete() = runBlocking {
//        assertEquals(0, countAccount(dao))
//        createDataAccounts(accounts, dao)
//        assertEquals(accounts.size, countAccount(dao))
//        assertEquals(accounts.size + 1, countAccountAuthority(dao))
//        accountRepository.delete(defaultAccount.toAccount())
//        assertEquals(accounts.size - 1, countAccount(dao))
//        assertEquals(accounts.size, countAccountAuthority(dao))
//    }
//
//    @Test
//    fun test_findOne_with_Email() = runBlocking {
//        assertEquals(0, countAccount(dao))
//        createDataAccounts(accounts, dao)
//        assertEquals(accounts.size, countAccount(dao))
//        assertEquals(
//            defaultAccount.login,
//            accountRepository.findOne(defaultAccount.email!!)!!.login
//        )
//    }
//
//    @Test
//    fun test_findOne_with_Login() = runBlocking {
//        assertEquals(0, countAccount(dao))
//        createDataAccounts(accounts, dao)
//        assertEquals(accounts.size, countAccount(dao))
//        assertEquals(
//            defaultAccount.email,
//            accountRepository.findOne(defaultAccount.login!!)!!.email
//        )
//    }
//
//    @Test
//    fun test_signup() {
//        assertEquals(0, countAccount(dao))
//        assertEquals(0, countAccountAuthority(dao))
//        runBlocking {
//            accountRepository.signup(
//                defaultAccount.copy(
//                    activationKey = generateActivationKey,
//                    langKey = DEFAULT_LANGUAGE,
//                    createdBy = SYSTEM_USER,
//                    createdDate = Instant.now(),
//                    lastModifiedBy = SYSTEM_USER,
//                    lastModifiedDate = Instant.now(),
//                    authorities = mutableSetOf(ROLE_USER)
//                )
//            )
//        }
//        assertEquals(1, countAccount(dao))
//        assertEquals(1, countAccountAuthority(dao))
//    }
//
//    @Test
//    fun test_findActivationKeyByLogin() {
//        assertEquals(0, countAccount(dao))
//        createDataAccounts(accounts, dao)
//        assertEquals(accounts.size, countAccount(dao))
//        assertEquals(accounts.size + 1, countAccountAuthority(dao))
//        runBlocking {
//            assertEquals(
//                findOneByEmail(defaultAccount.email!!, dao)!!.activationKey,
//                accountRepository.findActivationKeyByLogin(defaultAccount.login!!)
//            )
//        }
//    }
//
//    @Test
//    fun test_findOneByActivationKey() {
//        assertEquals(0, countAccount(dao))
//        createDataAccounts(accounts, dao)
//        assertEquals(accounts.size, countAccount(dao))
//        assertEquals(accounts.size + 1, countAccountAuthority(dao))
//        findOneByLogin(defaultAccount.login!!, dao).run findOneByLogin@{
//            assertNotNull(this@findOneByLogin)
//            assertNotNull(this@findOneByLogin.activationKey)
//            runBlocking {
//                accountRepository.findOneByActivationKey(this@findOneByLogin.activationKey!!)
//                    .run findOneByActivationKey@{
//                        assertNotNull(this@findOneByActivationKey)
//                        assertNotNull(this@findOneByActivationKey.id)
//                        assertEquals(
//                            this@findOneByLogin.id,
//                            this@findOneByActivationKey.id
//                        )
//                    }
//            }
//        }
//    }
//}