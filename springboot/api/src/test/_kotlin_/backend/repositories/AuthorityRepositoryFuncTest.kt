package backend.repositories

import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import backend.repositories.entities.Authority
import backend.tdd.functional.AbstractBaseFunctionalTest
import kotlin.test.Test
import kotlin.test.assertEquals
import javax.inject.Inject
class AuthorityRepositoryFuncTest : AbstractBaseFunctionalTest() {

    @Inject
    private lateinit var authorityRepository: AuthorityRepository

    @Test
    fun `test count authority`(): Unit = runBlocking {
        assertEquals(countAuthority(), authorityRepository.count())
    }

    @Test
    fun `test save ROLE_TEST authority`(): Unit = runBlocking {
        "ROLE_TEST".apply roleTest@{
            countAuthority().apply countAuthorityBeforeSave@{
                assertEquals(defaultRoles.size.toLong(), this@countAuthorityBeforeSave)
                authorityRepository.save(Authority(role = this@roleTest))
                assertEquals(this@countAuthorityBeforeSave + 1, countAuthority())
                deleteAuthorityByRole(this@roleTest)
            }
        }
    }
}