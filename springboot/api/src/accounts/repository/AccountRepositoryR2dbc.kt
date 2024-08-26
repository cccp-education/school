package webapp.accounts.repository

import jakarta.validation.Validator
import kotlinx.coroutines.reactive.collect
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.dao.DataAccessException
import org.springframework.data.r2dbc.core.*
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query.query
import org.springframework.stereotype.Repository
import webapp.accounts.models.Account
import webapp.accounts.models.AccountCredentials
import webapp.core.property.*
import webapp.users.entities.AccountAuthorityEntity
import webapp.users.entities.AccountEntity

@Repository
class AccountRepositoryR2dbc(
    private val dao: R2dbcEntityTemplate,
    private val validator: Validator,
) : AccountRepository {
    override suspend fun save(model: AccountCredentials) =
        try {
            if (model.id != null) dao.update(
                AccountEntity(model).copy(
                    version = dao.selectOne(
                        query(
                            where(LOGIN_FIELD).`is`(model.login!!).ignoreCase(true)
                                .or(where(EMAIL_FIELD).`is`(model.email!!).ignoreCase(true))
                        ), AccountEntity::class.java
                    ).awaitSingle()!!.version
                )
            ).awaitSingle()?.toModel
            else dao.insert(AccountEntity(model)).awaitSingle()?.toModel
        } catch (_: DataAccessException) {
            null
        } catch (_: NoSuchElementException) {
            null
        }

    override suspend fun findOne(emailOrLogin: String) = dao
        .select<AccountEntity>()
        .matching(
            query(
                where(
                    if (validator.validateProperty(
                            AccountCredentials(email = emailOrLogin),
                            EMAIL_FIELD
                        ).isEmpty()
                    ) EMAIL_FIELD else LOGIN_FIELD
                ).`is`(emailOrLogin).ignoreCase(true)
            )
        ).awaitOneOrNull()
        ?.toCredentialsModel

    private suspend fun withAuthorities(account: AccountCredentials?) = when {
        account == null -> null
        account.id == null -> null
        else -> account.copy(authorities = mutableSetOf<String>().apply {
            dao.select<AccountAuthorityEntity>()
                .matching(query(where(ACCOUNT_AUTH_USER_ID_FIELD).`is`(account.id)))
                .all()
                .collect { add(it.role) }
        })
    }

    override suspend fun findOneWithAuthorities(emailOrLogin: String) = withAuthorities(findOne(emailOrLogin))


    override suspend fun findActivationKeyByLogin(login: String) =
        dao.select<AccountEntity>()
            .matching(query(where(LOGIN_FIELD).`is`(login).ignoreCase(true)))
            .awaitOneOrNull()
            ?.activationKey

    override suspend fun signup(accountCredentials: AccountCredentials) = dao
        .insert(AccountEntity(accountCredentials))
        .awaitSingleOrNull()
        ?.id
        .run {
            if (this != null) dao.insert(
                AccountAuthorityEntity(
                    userId = this,
                    role = ROLE_USER
                )
            ).awaitSingleOrNull()
        }

    override suspend fun findOneByActivationKey(key: String) = dao
        .selectOne(
            query(where(ACTIVATION_KEY_FIELD).`is`(key)),
            AccountEntity::class.java
        ).awaitSingleOrNull()
        ?.toCredentialsModel

    override suspend fun findOneByResetKey(key: String) = dao
        .selectOne(
            query(where(RESET_KEY_FIELD).`is`(key)),
            AccountEntity::class.java
        ).awaitSingleOrNull() //as  AccountRecord<*>?

    override suspend fun delete(account: Account) {
        when {
            account.login != null || account.email != null && account.id == null -> (
                    when {
                        account.login != null -> findOne(account.login)
                        account.email != null -> findOne(account.email)
                        else -> null
                    }).run {
                if (this != null) dao
                    .delete<AccountAuthorityEntity>()
                    .matching(query(where(ACCOUNT_AUTH_USER_ID_FIELD).`is`(id!!)))
                    .allAndAwait()
                    .also { dao.delete(AccountEntity(this)).awaitSingle() }
            }
        }
    }
}

/*
import community.ROLE_USER
import community.SYSTEM_USER
import community.accounts.Account.Companion.ACCOUNT_AUTH_USER_ID_FIELD
import community.accounts.Account.Companion.ACTIVATION_KEY_FIELD
import community.accounts.Account.Companion.EMAIL_FIELD
import community.accounts.Account.Companion.LOGIN_FIELD
import community.accounts.Account.Companion.RESET_KEY_FIELD
import community.accounts.password.PasswordReset
import community.accounts.signup.Signup
import community.security.generateResetKey
import jakarta.validation.Validator
import kotlinx.coroutines.reactive.collect
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.dao.DataAccessException
import org.springframework.data.r2dbc.core.*
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query.query
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.Instant.now

@Suppress("KotlinConstantConditions", "unused")
@Repository
class AccountRepositoryR2dbc(
    private val dao: R2dbcEntityTemplate,
    private val validator: Validator,
) : AccountRepository {

    override suspend fun delete(account: Account) {
        @Suppress("KotlinConstantConditions", "unused")
        when {
            account.login != null || account.email != null && account.id == null -> when {
                account.login != null -> findOne(account.login)
                account.email != null -> findOne(account.email)
                else -> null
            }.run {
                if (this != null) dao
                    .delete<UserRole>()
                    .matching(query(where(ACCOUNT_AUTH_USER_ID_FIELD).`is`(first.id!!)))
                    .allAndAwait()
                    .also {
                        dao.delete(User(first)).awaitSingle()
                    }
            }
        }
    }

    override fun resetPassword(account: Account): Pair<Account, String> {
        TODO("Not yet implemented")
    }

    override fun changePassword(newPassword: Pair<Account, String>) {
        TODO("Not yet implemented")
    }

    override fun completePassword(accountExtra: Pair<Account, AccountExtra>) {
        TODO("Not yet implemented")
    }

    override suspend fun save(account: Account): Pair<Account, AccountExtra>? =
        try {
            if (account.id != null) dao.update(
                User(account).copy(
                    version = dao.selectOne(
                        query(
                            where(LOGIN_FIELD).`is`(account.login!!).ignoreCase(true)
                                .or(where(EMAIL_FIELD).`is`(account.email!!).ignoreCase(true))
                        ), User::class.java
                    ).awaitSingle()!!.version
                )
            ).awaitSingle().extraPair
            else dao.insert(User(account)).awaitSingle().extraPair
        } catch (_: DataAccessException) {
            null
        } catch (_: NoSuchElementException) {
            null
        }




    override suspend fun findOne(emailOrLogin: String)
            : Pair<Account, AccountExtra>? = dao
        .select<User>()
        .matching(
            query(
                where(
                    if (validator.validateProperty(
                            Account(email = emailOrLogin),
                            EMAIL_FIELD
                        ).isEmpty()
                    ) EMAIL_FIELD else LOGIN_FIELD
                ).`is`(emailOrLogin).ignoreCase(true)
            )
        ).awaitOneOrNull()?.extraPair

    private suspend fun withAuthorities(account: Account?) = when {
        account == null -> null
        account.id == null -> null
        else -> account.copy(authorities = mutableSetOf<String>().apply {
            dao.select<UserRole>()
                .matching(query(where(ACCOUNT_AUTH_USER_ID_FIELD).`is`(account.id)))
                .all()
                .collect { add(it.role) }
        })
    }

    override suspend fun findOneWithAuthorities(emailOrLogin: String)
            : Pair<Account, AccountExtra>? = findOne(emailOrLogin)?.let {
        it.copy(first = it.first.copy(authorities = withAuthorities(it.first)?.authorities))
    }


    override suspend fun findActivationKeyByLogin(login: String) =
        dao.select<User>()
            .matching(query(where(LOGIN_FIELD).`is`(login).ignoreCase(true)))
            .awaitOneOrNull()
            ?.activationKey

    override suspend fun save(signup: Signup): Signup? = dao.insert(
        User(signup.account)
            .copy(
                createdBy = dao.selectOne(
                    query(where(LOGIN_FIELD).`is`(SYSTEM_USER)),
                    User::class.java
                ).awaitSingle().id
            )
    ).awaitSingleOrNull().run {
        return when {
            this != null && id != null -> toAccount.apply {
                dao.insert(UserRole(userId = id!!, role = ROLE_USER))
                    .awaitSingleOrNull()
            }

            else -> null
        }?.run { signup.copy(this) }
    }


    override suspend fun findOneByActivationKey(key: String) =
        dao.selectOne(
            query(where(ACTIVATION_KEY_FIELD).`is`(key)),
            User::class.java
        ).awaitSingleOrNull()?.toAccount


    override suspend fun findOneByResetKey(key: String) =
        dao.selectOne(
            query(where(RESET_KEY_FIELD).`is`(key)),
            User::class.java
        ).awaitSingleOrNull()
            .run { if (this != null) Pair(toAccount, PasswordReset(key = key)) else null }


    override suspend fun generateResetKey(email: String): Pair<String, Instant>? {
//        copy(//TODO: repo.generateResetKey(account)
//            resetKey = community.accounts.security.generateResetKey,
//            resetDate = Instant.now()
//        )
//        TODO("Not yet implemented")
//        findOne(email)?.run {
//            User(this.first).copy(
//                resetKey = community.accounts.security.generateResetKey,
//                resetDate = now()
//            )
//        }
        //////////////
//        dao
        return generateResetKey to now()
    }
}
 */