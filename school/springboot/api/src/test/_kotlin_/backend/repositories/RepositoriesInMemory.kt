@file:Suppress("unused", "FunctionName", "RedundantUnitReturnType")

package backend


import backend.Constants.ROLE_USER
import org.springframework.stereotype.Repository
import java.util.*
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.awaitOneOrNull
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query

interface AuthorityRepository {
    suspend fun findOne(role: String): String?
}

interface AccountRepository {
    suspend fun findOneByLogin(login: String): Account?

    suspend fun findOneByEmail(email: String): Account?

    suspend fun save(model: AccountCredentials): Account?

    suspend fun delete(account: Account)

    suspend fun findActivationKeyByLogin(login: String): String?

    suspend fun count(): Long
    suspend fun suppress(account: Account)
    suspend fun signup(model: AccountCredentials)
    suspend fun findOneActivationKey(key: String): AccountCredentials?
}

interface AccountAuthorityRepository {
    suspend fun save(id: UUID, authority: String): Unit

    suspend fun delete(id: UUID, authority: String): Unit

    suspend fun count(): Long

    suspend fun deleteAll(): Unit

    suspend fun deleteAllByAccountId(id: UUID): Unit
}


@Repository
class AuthorityRepositoryInMemory : AuthorityRepository {
    companion object {
        private val authorities by lazy {
            mutableSetOf(
                "ADMIN",
                "USER",
                "ANONYMOUS"
            ).map { AuthorityEntity(it) }.toSet()
        }
    }

    override suspend fun findOne(role: String): String? =
        authorities.find { it.role == role }?.role
}

@Repository
class AccountRepositoryInMemory(
    private val accountAuthorityRepository: AccountAuthorityRepository,
    private val authorityRepository: AuthorityRepository
) : AccountRepository {

    companion object {
        private val accounts by lazy { mutableSetOf<AccountRecord<AuthorityRecord>>() }
    }

    override suspend fun findOneByLogin(login: String) =
        accounts.find { login.equals(it.login, ignoreCase = true) }?.toModel()

    override suspend fun findOneByEmail(email: String) =
        accounts.find { email.equals(it.email, ignoreCase = true) }?.toModel()


    override suspend fun save(model: AccountCredentials): Account? =
        create(model).run {
            when {
                this != null -> return@run this
                else -> update(model)
            }
        }

    private fun create(model: AccountCredentials) =
        if (`mail & login do not exist`(model))
            model.copy(id = UUID.randomUUID()).apply {
                @Suppress("UNCHECKED_CAST")
                accounts += AccountEntity(this) as AccountRecord<AuthorityRecord>
            }.toAccount() else null

    private fun `mail & login do not exist`(model: AccountCredentials) =
        accounts.none {
            it.login.equals(model.login, ignoreCase = true)
                    && it.email.equals(model.email, ignoreCase = true)
        }


    private fun `mail exists and login exists`(model: AccountCredentials) =
        accounts.any {
            model.email.equals(it.email, ignoreCase = true)
                    && model.login.equals(it.login, ignoreCase = true)
        }


    private fun `mail exists and login does not`(model: AccountCredentials) =
        accounts.any {
            model.email.equals(it.email, ignoreCase = true)
                    && !model.login.equals(it.login, ignoreCase = true)
        }


    private fun `mail does not exist and login exists`(model: AccountCredentials) =
        accounts.any {
            !model.email.equals(it.email, ignoreCase = true)
                    && model.login.equals(it.login, ignoreCase = true)
        }

    private fun update(
        model: AccountCredentials,
    ): Account? = when {
        `mail exists and login does not`(model) -> changeLogin(model)?.run { patch(this) }
        `mail does not exist and login exists`(model) -> changeEmail(model)?.run { patch(this) }
        `mail exists and login exists`(model) -> patch(model)
        else -> null
    }

    private fun changeLogin(
        model: AccountCredentials,
    ): AccountCredentials? =
        try {
            @Suppress("CAST_NEVER_SUCCEEDS")
            (accounts.first { model.email.equals(it.email, ignoreCase = true) } as AccountCredentials).run {
                val retrieved: AccountCredentials = copy(login = model.login)
                accounts.remove(this as AccountRecord<AuthorityRecord>?)
                (retrieved as AccountRecord<AuthorityRecord>?)?.run { accounts.add(this) }
                model
            }
        } catch (_: NoSuchElementException) {
            null
        }


    private fun changeEmail(
        model: AccountCredentials,
    ): AccountCredentials? = try {
        @Suppress("CAST_NEVER_SUCCEEDS")
        (accounts.first { model.login.equals(it.login, ignoreCase = true) } as AccountCredentials).run {
            val retrieved: AccountCredentials = copy(email = model.email)
            accounts.remove(this as AccountRecord<AuthorityRecord>?)
            (retrieved as AccountRecord<AuthorityRecord>?)?.run { accounts.add(this) }
            model
        }
    } catch (_: NoSuchElementException) {
        null
    }

    private fun patch(
        model: AccountCredentials?,
    ): Account? =
        model.run {
            val retrieved = accounts.find { this?.email?.equals(it.email, ignoreCase = true)!! }
            accounts.remove(accounts.find { this?.email?.equals(it.email, ignoreCase = true)!! })
            ((retrieved?.toCredentialsModel())?.copy(
                password = `if password is null or empty then no change`(model, retrieved.toCredentialsModel()),
                activationKey = `switch activationKey case then patch`(model, retrieved.toCredentialsModel()),
                authorities = `if authorities are null or empty then no change`(model, retrieved.toCredentialsModel())
            ).apply {
                @Suppress("UNCHECKED_CAST")
                accounts.add(this?.let { AccountEntity(it) } as AccountRecord<AuthorityRecord>)
            }?.toAccount())
        }


    private fun `if password is null or empty then no change`(
        model: AccountCredentials?,
        retrieved: AccountCredentials
    ): String = when {
        model == null -> retrieved.password!!
        model.password == null -> retrieved.password!!
        model.password.isNotEmpty() -> model.password
        else -> retrieved.password!!
    }

    @Suppress("FunctionName")
    private fun `switch activationKey case then patch`(
        model: AccountCredentials?,
        retrieved: AccountCredentials
    ): String? = when {
        model == null -> null
        model.activationKey == null -> null
        !retrieved.activated
                && retrieved.activationKey.isNullOrBlank()
                && model.activationKey.isNotEmpty() -> model.activationKey

        !retrieved.activated
                && !retrieved.activationKey.isNullOrBlank()
                && model.activationKey.isNotEmpty() -> retrieved.activationKey

        else -> null
    }

    private fun `if authorities are null or empty then no change`(
        model: AccountCredentials?,
        retrieved: AccountCredentials
    ): Set<String> {
        if (model != null) {
            if (model.authorities != null) {
                if (model.authorities.isNotEmpty() && model.authorities.contains(ROLE_USER))
                    return model.authorities
                if (retrieved.authorities != null)
                    if (retrieved.authorities.isNotEmpty()
                        && retrieved.authorities.contains(ROLE_USER)
                        && !model.authorities.contains(ROLE_USER)
                    ) return retrieved.authorities
            } else
                if (!retrieved.authorities.isNullOrEmpty()
                    && retrieved.authorities.contains(ROLE_USER)
                ) return retrieved.authorities
        }
        return emptySet()
    }


    override suspend fun delete(account: Account) {
        accounts.apply { if (isNotEmpty()) remove(find { it.id == account.id }) }
    }

    override suspend fun findActivationKeyByLogin(login: String): String? =
        accounts.find {
            it.login.equals(login, ignoreCase = true)
        }?.activationKey

    override suspend fun count(): Long = accounts.size.toLong()
    override suspend fun suppress(account: Account) {
        accountAuthorityRepository.deleteAllByAccountId(account.id!!)
        delete(account)
    }

    override suspend fun signup(model: AccountCredentials) {
        accountAuthorityRepository.save(save(model)?.id!!, ROLE_USER)
    }

    override suspend fun findOneActivationKey(key: String): AccountCredentials? {
        TODO("Not yet implemented")
    }
}


@Repository
class AccountAuthorityRepositoryInMemory : AccountAuthorityRepository {
    companion object {
        private val accountAuthorities by lazy { mutableSetOf<AccountAuthority>() }
    }

    override suspend fun count(): Long = accountAuthorities.size.toLong()

    override suspend fun save(id: UUID, authority: String) {
        accountAuthorities.add(AccountAuthority(userId = id, role = authority))
    }


    override suspend fun delete(id: UUID, authority: String): Unit =
        accountAuthorities.run {
            filter { it.userId == id && it.role == authority }
                .map { remove(it) }
        }


    override suspend fun deleteAll() = accountAuthorities.clear()


    override suspend fun deleteAllByAccountId(id: UUID): Unit =
        accountAuthorities.run {
            filter { it.userId == id }
                .map { remove(it) }
        }

}
