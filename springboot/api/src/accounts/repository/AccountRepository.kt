package webapp.accounts.repository

import webapp.accounts.models.Account
import webapp.accounts.models.AccountCredentials
import webapp.users.entities.AccountRecord

interface AccountRepository {
    suspend fun save(model: AccountCredentials): Account?
    suspend fun findOne(emailOrLogin: String): AccountCredentials?
    suspend fun findOneWithAuthorities(emailOrLogin: String): AccountCredentials?
    suspend fun findOneByActivationKey(key: String): AccountCredentials?
    suspend fun findOneByResetKey(key: String): AccountRecord<*>?//<AuthorityRecord>?
    suspend fun findActivationKeyByLogin(login: String): String?
    suspend fun delete(account: Account)
    suspend fun signup(accountCredentials: AccountCredentials)
}

/*
import community.accounts.password.PasswordReset
import community.accounts.signup.Signup
import java.time.Instant

interface AccountRepository {
    fun resetPassword(account: Account): Pair<Account, String>
    fun changePassword(newPassword: Pair<Account, String>)
    fun completePassword(accountExtra: Pair<Account, AccountExtra>)
    suspend fun save(account: Account): Pair<Account, AccountExtra>?
    suspend fun findOne(emailOrLogin: String): Pair<Account, AccountExtra>?
    suspend fun findOneWithAuthorities(emailOrLogin: String): Pair<Account, AccountExtra>?
    suspend fun findOneByActivationKey(key: String): Account?
    suspend fun findOneByResetKey(key: String): Pair<Account, PasswordReset>?
    suspend fun findActivationKeyByLogin(login: String): String?
    suspend fun delete(account: Account)
    suspend fun save(signup: Signup): Signup?
    suspend fun generateResetKey(email: String): Pair<String, Instant>?
}
 */