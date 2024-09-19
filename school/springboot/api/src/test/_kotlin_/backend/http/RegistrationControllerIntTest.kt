@file:Suppress(
    "NonAsciiCharacters",
    "SpellCheckingInspection",
    "HttpUrlsUsage"
)

package backend.http

import backend.config.Constants.AUTHORIZATION_HEADER
import backend.config.Constants.BEARER_START_WITH
import backend.config.Constants.DEFAULT_LANGUAGE
import backend.config.Constants.ROLE_USER
import backend.config.Constants.SYSTEM_USER
import backend.domain.AccountPassword
import backend.domain.DataTest.USER_LOGIN
import backend.domain.DataTest.defaultAccount
import backend.domain.DataTest.defaultUser
import backend.services.TokenProvider
import backend.tdd.integration.AbstractRestIntegrationTest
import backend.tdd.integration.TEST_USER_LOGIN
import backend.tdd.integration.WithUnauthenticatedMockUser
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.getBean
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.test.context.support.WithMockUser
import java.time.Instant.now
import kotlin.test.Test
import kotlin.test.Ignore
import kotlin.test.assertEquals

class RegistrationControllerIntTest : AbstractRestIntegrationTest() {
//TODO reprendre tous ces test avec inmemory
    @Test//suprimable
    fun `test post un account valide sur le end point register`()
            : Unit = runBlocking {
        client
            .post()
            .uri("/api/register")
            .bodyValue(defaultAccount)
            .exchange()
            .expectStatus()
            .isCreated
    }


    @Test
    @WithUnauthenticatedMockUser
    fun `test le end point api authenticate est accessible à un account non authentifié`()
            : Unit = runBlocking {
        client
            .get()
            .uri("/api/authenticate")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .isEmpty
    }

    @Test
    fun `test si un user existant peut demander son account`(): Unit = runBlocking {
        assertEquals(0, countUser())
        assertEquals(0, countUserAuthority())
        checkInitDatabaseWithDefaultUser()
        assertEquals(1, countUser())
        assertEquals(1, countUserAuthority())


        context.getBean<TokenProvider>().createToken(
            context.getBean<ReactiveAuthenticationManager>()
                .authenticate(
                    UsernamePasswordAuthenticationToken(
                        defaultUser.login,
                        defaultUser.password
                    )
                ).awaitSingle(),
            true
        ).run {
            client
                .get()
                .uri("/api/account")
                .headers {
                    it.set(
                        AUTHORIZATION_HEADER,
                        "$BEARER_START_WITH $this"
                    )
                }
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk
                .expectHeader()
                .contentType(APPLICATION_JSON_VALUE)
                .expectBody()
                .jsonPath("$.login")
                .isEqualTo(defaultUser.login!!)
                .jsonPath("$.firstName")
                .isEqualTo(defaultUser.firstName!!)
                .jsonPath("$.lastName")
                .isEqualTo(defaultUser.lastName!!)
                .jsonPath("$.email")
                .isEqualTo(defaultUser.email!!)
                .jsonPath("$.langKey")
                .isEqualTo(DEFAULT_LANGUAGE)
                .jsonPath("$.authorities")
                .isEqualTo(ROLE_USER)
        }
    }


    @Test
    @WithMockUser(username = TEST_USER_LOGIN)
    fun `test un account inconnu retourne une INTERNAL_SERVER_ERROR sur le end point api account`()
            : Unit = runBlocking {
        assertEquals(countUser(), 0)
        client
            .get()
            .uri("/api/account")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isEqualTo(INTERNAL_SERVER_ERROR)
    }


    @Test
    @Throws(Exception::class)
    fun `test register account avec login invalid`(): Unit = runBlocking {
        assertEquals(countUser(), 0)
        client
            .post()
            .uri("/api/register")
            .contentType(APPLICATION_JSON)
            .bodyValue(
                AccountPassword(
                    password = defaultAccount.password
                ).apply {
                    login = "funky-log(n"
                    firstName = USER_LOGIN
                    lastName = USER_LOGIN
                    email = defaultAccount.email
                    langKey = DEFAULT_LANGUAGE
                    createdBy = SYSTEM_USER
                    createdDate = now()
                    lastModifiedBy = SYSTEM_USER
                    lastModifiedDate = now()
                    imageUrl = "http://placehold.it/50x50"
                })
            .exchange()
            .expectStatus()
            .isBadRequest
        assertEquals(countUser(), 0)
    }

    @Test
    @Throws(Exception::class)
    fun `test register account avec un email invalid`(): Unit = runBlocking {
        assertEquals(countUser(), 0)
        client
            .post()
            .uri("/api/register")
            .contentType(APPLICATION_JSON)
            .bodyValue(
                AccountPassword(
                    password = defaultAccount.password
                ).apply {
                    login = defaultAccount.login
                    firstName = USER_LOGIN
                    lastName = USER_LOGIN
                    email = "invalid"
                    langKey = DEFAULT_LANGUAGE
                    createdBy = SYSTEM_USER
                    createdDate = now()
                    lastModifiedBy = SYSTEM_USER
                    lastModifiedDate = now()
                    imageUrl = "http://placehold.it/50x50"
                })
            .exchange()
            .expectStatus()
            .isBadRequest
        assertEquals(expected = countUser(), actual = 0)
    }

    @Test
    @Throws(Exception::class)
    fun `test register account avec un password invalid`(): Unit = runBlocking {
        assertEquals(expected = countUser(), actual = 0)
        client.post()
            .uri("/api/register")
            .contentType(APPLICATION_JSON)
            .bodyValue(
                AccountPassword(
                    password = "123"
                ).apply {
                    login = defaultAccount.login
                    firstName = USER_LOGIN
                    lastName = USER_LOGIN
                    email = defaultAccount.email
                    langKey = DEFAULT_LANGUAGE
                    createdBy = SYSTEM_USER
                    createdDate = now()
                    lastModifiedBy = SYSTEM_USER
                    lastModifiedDate = now()
                    imageUrl = "http://placehold.it/50x50"
                }
            )
            .exchange()
            .expectStatus()
            .isBadRequest
        assertEquals(countUser(), 0)
    }

    @Test
    @Throws(Exception::class)
    fun `test register account avec un password null`(): Unit = runBlocking {
        assertEquals(countUser(), 0)
        client
            .post()
            .uri("/api/register")
            .contentType(APPLICATION_JSON)
            .bodyValue(
                AccountPassword(
                    password = null
                ).apply {
                    login = defaultAccount.login
                    firstName = USER_LOGIN
                    lastName = USER_LOGIN
                    email = defaultAccount.email
                    langKey = DEFAULT_LANGUAGE
                    createdBy = SYSTEM_USER
                    createdDate = now()
                    lastModifiedBy = SYSTEM_USER
                    lastModifiedDate = now()
                    imageUrl = "http://placehold.it/50x50"
                })
            .exchange()
            .expectStatus()
            .isBadRequest
        assertEquals(countUser(), 0)
    }

    @Test
    @Throws(Exception::class)
    fun `test register account avec un email existant activé`(): Unit = runBlocking {
        assertEquals(0, countUser())
        assertEquals(0, countUserAuthority())
        checkInitDatabaseWithDefaultUser()
        assertEquals(1, countUser())
        assertEquals(1, countUserAuthority())

        client
            .post()
            .uri("/api/register")
            .contentType(APPLICATION_JSON)
            .bodyValue(
                AccountPassword(
                    password = defaultAccount.password
                ).apply {
                    login = TEST_USER_LOGIN
                    firstName = defaultAccount.firstName
                    lastName = defaultAccount.lastName
                    email = defaultAccount.email
                    langKey = defaultAccount.langKey
                    createdBy = defaultAccount.createdBy
                    now().apply {
                        lastModifiedDate = this
                        lastModifiedDate = this
                    }
                    lastModifiedBy = defaultAccount.lastModifiedBy
                    imageUrl = "http://placehold.it/50x50"
                })
            .exchange()
            .expectStatus()
            .is4xxClientError

        assertEquals(1, countUser())
    }


    @Test
    @Throws(Exception::class)
    fun `test register account avec un login existant`(): Unit = runBlocking {
        assertEquals(0, countUser())
        assertEquals(0, countUserAuthority())
        checkInitDatabaseWithDefaultUser()
        assertEquals(1, countUser())
        assertEquals(1, countUserAuthority())

        client
            .post()
            .uri("/api/register")
            .contentType(APPLICATION_JSON)
            .bodyValue(
                AccountPassword(
                    password = defaultAccount.password
                ).apply {
                    login = defaultAccount.login
                    firstName = defaultAccount.firstName
                    lastName = defaultAccount.lastName
                    email = "j.doe@acme.com"
                    langKey = defaultAccount.langKey
                    createdBy = defaultAccount.createdBy
                    createdDate = now()
                    lastModifiedBy = defaultAccount.lastModifiedBy
                    lastModifiedDate = now()
                })
            .exchange()
            .expectStatus()
            .isBadRequest

        assertEquals(1, countUser())
    }

    @Ignore
    @Test
    @Throws(Exception::class)
    fun `test register account avec un email dupliqué`(): Unit = runBlocking {
/*
        // First user
        ManagedUserVM firstUser = new ManagedUserVM();
        firstUser.setLogin("test-register-duplicate-email");
        firstUser.setPassword("password");
        firstUser.setFirstName("Alice");
        firstUser.setLastName("Test");
        firstUser.setEmail("test-register-duplicate-email@example.com");
        firstUser.setImageUrl("http://placehold.it/50x50");
        firstUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        firstUser.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        // Register first user
        accountWebTestClient
            .post()
            .uri("/api/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(firstUser))
            .exchange()
            .expectStatus()
            .isCreated();

        Optional<User> testUser1 = userRepository.findOneByLogin("test-register-duplicate-email").blockOptional();
        assertThat(testUser1).isPresent();

        // Duplicate email, different login
        ManagedUserVM secondUser = new ManagedUserVM();
        secondUser.setLogin("test-register-duplicate-email-2");
        secondUser.setPassword(firstUser.getPassword());
        secondUser.setFirstName(firstUser.getFirstName());
        secondUser.setLastName(firstUser.getLastName());
        secondUser.setEmail(firstUser.getEmail());
        secondUser.setImageUrl(firstUser.getImageUrl());
        secondUser.setLangKey(firstUser.getLangKey());
        secondUser.setAuthorities(new HashSet<>(firstUser.getAuthorities()));

        // Register second (non activated) user
        accountWebTestClient
            .post()
            .uri("/api/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(secondUser))
            .exchange()
            .expectStatus()
            .isCreated();

        Optional<User> testUser2 = userRepository.findOneByLogin("test-register-duplicate-email").blockOptional();
        assertThat(testUser2).isEmpty();

        Optional<User> testUser3 = userRepository.findOneByLogin("test-register-duplicate-email-2").blockOptional();
        assertThat(testUser3).isPresent();

        // Duplicate email - with uppercase email address
        ManagedUserVM userWithUpperCaseEmail = new ManagedUserVM();
        userWithUpperCaseEmail.setId(firstUser.getId());
        userWithUpperCaseEmail.setLogin("test-register-duplicate-email-3");
        userWithUpperCaseEmail.setPassword(firstUser.getPassword());
        userWithUpperCaseEmail.setFirstName(firstUser.getFirstName());
        userWithUpperCaseEmail.setLastName(firstUser.getLastName());
        userWithUpperCaseEmail.setEmail("TEST-register-duplicate-email@example.com");
        userWithUpperCaseEmail.setImageUrl(firstUser.getImageUrl());
        userWithUpperCaseEmail.setLangKey(firstUser.getLangKey());
        userWithUpperCaseEmail.setAuthorities(new HashSet<>(firstUser.getAuthorities()));

        // Register third (not activated) user
        accountWebTestClient
            .post()
            .uri("/api/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(userWithUpperCaseEmail))
            .exchange()
            .expectStatus()
            .isCreated();

        Optional<User> testUser4 = userRepository.findOneByLogin("test-register-duplicate-email-3").blockOptional();
        assertThat(testUser4).isPresent();
        assertThat(testUser4.get().getEmail()).isEqualTo("test-register-duplicate-email@example.com");

        testUser4.get().setActivated(true);
        userService.updateUser((new AdminUserDTO(testUser4.get()))).block();

        // Register 4th (already activated) user
        accountWebTestClient
            .post()
            .uri("/api/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(secondUser))
            .exchange()
            .expectStatus()
            .is4xxClientError();
        
 */
    }

/*
    @Test
    void testRegisterAdminIsIgnored() throws Exception {
        ManagedUserVM validUser = new ManagedUserVM();
        validUser.setLogin("badguy");
        validUser.setPassword("password");
        validUser.setFirstName("Bad");
        validUser.setLastName("Guy");
        validUser.setEmail("badguy@example.com");
        validUser.setActivated(true);
        validUser.setImageUrl("http://placehold.it/50x50");
        validUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        validUser.setAuthorities(Collections.singleton(AuthoritiesConstants.ADMIN));

        accountWebTestClient
            .post()
            .uri("/api/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(validUser))
            .exchange()
            .expectStatus()
            .isCreated();

        Optional<User> userDup = userRepository.findOneWithAuthoritiesByLogin("badguy").blockOptional();
        assertThat(userDup).isPresent();
        assertThat(userDup.get().getAuthorities())
            .hasSize(1)
            .containsExactly(authorityRepository.findById(AuthoritiesConstants.USER).block());
    }
*/
    
/*
    @Test
    void testActivateAccount() {
        final String activationKey = "some activation key";
        User user = new User();
        user.setLogin("activate-account");
        user.setEmail("activate-account@example.com");
        user.setPassword(RandomStringUtils.random(60));
        user.setActivated(false);
        user.setActivationKey(activationKey);
        user.setCreatedBy(Constants.SYSTEM);

        userRepository.save(user).block();

        accountWebTestClient.get().uri("/api/activate?key={activationKey}", activationKey).exchange().expectStatus().isOk();

        user = userRepository.findOneByLogin(user.getLogin()).block();
        assertThat(user.isActivated()).isTrue();
    }
*/
    
/*
    @Test
    void testActivateAccountWithWrongKey() {
        accountWebTestClient
            .get()
            .uri("/api/activate?key=wrongActivationKey")
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
*/
    
/*
    @Test
    @WithMockUser("save-account")
    void testSaveAccount() throws Exception {
        User user = new User();
        user.setLogin("save-account");
        user.setEmail("save-account@example.com");
        user.setPassword(RandomStringUtils.random(60));
        user.setActivated(true);
        user.setCreatedBy(Constants.SYSTEM);
        userRepository.save(user).block();

        AdminUserDTO userDTO = new AdminUserDTO();
        userDTO.setLogin("not-used");
        userDTO.setFirstName("firstname");
        userDTO.setLastName("lastname");
        userDTO.setEmail("save-account@example.com");
        userDTO.setActivated(false);
        userDTO.setImageUrl("http://placehold.it/50x50");
        userDTO.setLangKey(Constants.DEFAULT_LANGUAGE);
        userDTO.setAuthorities(Collections.singleton(AuthoritiesConstants.ADMIN));

        accountWebTestClient
            .post()
            .uri("/api/account")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(userDTO))
            .exchange()
            .expectStatus()
            .isOk();

        User updatedUser = userRepository.findOneWithAuthoritiesByLogin(user.getLogin()).block();
        assertThat(updatedUser.getFirstName()).isEqualTo(userDTO.getFirstName());
        assertThat(updatedUser.getLastName()).isEqualTo(userDTO.getLastName());
        assertThat(updatedUser.getEmail()).isEqualTo(userDTO.getEmail());
        assertThat(updatedUser.getLangKey()).isEqualTo(userDTO.getLangKey());
        assertThat(updatedUser.getPassword()).isEqualTo(user.getPassword());
        assertThat(updatedUser.getImageUrl()).isEqualTo(userDTO.getImageUrl());
        assertThat(updatedUser.isActivated()).isTrue();
        assertThat(updatedUser.getAuthorities()).isEmpty();
    }
*/
    
/*
    @Test
    @WithMockUser("save-invalid-email")
    void testSaveInvalidEmail() throws Exception {
        User user = new User();
        user.setLogin("save-invalid-email");
        user.setEmail("save-invalid-email@example.com");
        user.setPassword(RandomStringUtils.random(60));
        user.setActivated(true);
        user.setCreatedBy(Constants.SYSTEM);

        userRepository.save(user).block();

        AdminUserDTO userDTO = new AdminUserDTO();
        userDTO.setLogin("not-used");
        userDTO.setFirstName("firstname");
        userDTO.setLastName("lastname");
        userDTO.setEmail("invalid email");
        userDTO.setActivated(false);
        userDTO.setImageUrl("http://placehold.it/50x50");
        userDTO.setLangKey(Constants.DEFAULT_LANGUAGE);
        userDTO.setAuthorities(Collections.singleton(AuthoritiesConstants.ADMIN));

        accountWebTestClient
            .post()
            .uri("/api/account")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(userDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertThat(userRepository.findOneByEmailIgnoreCase("invalid email").blockOptional()).isNotPresent();
    }
*/
    
/*
    @Test
    @WithMockUser("save-existing-email")
    void testSaveExistingEmail() throws Exception {
        User user = new User();
        user.setLogin("save-existing-email");
        user.setEmail("save-existing-email@example.com");
        user.setPassword(RandomStringUtils.random(60));
        user.setActivated(true);
        user.setCreatedBy(Constants.SYSTEM);
        userRepository.save(user).block();

        User anotherUser = new User();
        anotherUser.setLogin("save-existing-email2");
        anotherUser.setEmail("save-existing-email2@example.com");
        anotherUser.setPassword(RandomStringUtils.random(60));
        anotherUser.setActivated(true);
        anotherUser.setCreatedBy(Constants.SYSTEM);

        userRepository.save(anotherUser).block();

        AdminUserDTO userDTO = new AdminUserDTO();
        userDTO.setLogin("not-used");
        userDTO.setFirstName("firstname");
        userDTO.setLastName("lastname");
        userDTO.setEmail("save-existing-email2@example.com");
        userDTO.setActivated(false);
        userDTO.setImageUrl("http://placehold.it/50x50");
        userDTO.setLangKey(Constants.DEFAULT_LANGUAGE);
        userDTO.setAuthorities(Collections.singleton(AuthoritiesConstants.ADMIN));

        accountWebTestClient
            .post()
            .uri("/api/account")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(userDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        User updatedUser = userRepository.findOneByLogin("save-existing-email").block();
        assertThat(updatedUser.getEmail()).isEqualTo("save-existing-email@example.com");
    }
*/
    
/*
    @Test
    @WithMockUser("save-existing-email-and-login")
    void testSaveExistingEmailAndLogin() throws Exception {
        User user = new User();
        user.setLogin("save-existing-email-and-login");
        user.setEmail("save-existing-email-and-login@example.com");
        user.setPassword(RandomStringUtils.random(60));
        user.setActivated(true);
        user.setCreatedBy(Constants.SYSTEM);
        userRepository.save(user).block();

        AdminUserDTO userDTO = new AdminUserDTO();
        userDTO.setLogin("not-used");
        userDTO.setFirstName("firstname");
        userDTO.setLastName("lastname");
        userDTO.setEmail("save-existing-email-and-login@example.com");
        userDTO.setActivated(false);
        userDTO.setImageUrl("http://placehold.it/50x50");
        userDTO.setLangKey(Constants.DEFAULT_LANGUAGE);
        userDTO.setAuthorities(Collections.singleton(AuthoritiesConstants.ADMIN));

        accountWebTestClient
            .post()
            .uri("/api/account")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(userDTO))
            .exchange()
            .expectStatus()
            .isOk();

        User updatedUser = userRepository.findOneByLogin("save-existing-email-and-login").block();
        assertThat(updatedUser.getEmail()).isEqualTo("save-existing-email-and-login@example.com");
    }
*/
    
/*
    @Test
    @WithMockUser("change-password-wrong-existing-password")
    void testChangePasswordWrongExistingPassword() throws Exception {
        User user = new User();
        String currentPassword = RandomStringUtils.random(60);
        user.setPassword(passwordEncoder.encode(currentPassword));
        user.setLogin("change-password-wrong-existing-password");
        user.setEmail("change-password-wrong-existing-password@example.com");
        user.setCreatedBy(Constants.SYSTEM);
        userRepository.save(user).block();

        accountWebTestClient
            .post()
            .uri("/api/account/change-password")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(new PasswordChangeDTO("1" + currentPassword, "new password")))
            .exchange()
            .expectStatus()
            .isBadRequest();

        User updatedUser = userRepository.findOneByLogin("change-password-wrong-existing-password").block();
        assertThat(passwordEncoder.matches("new password", updatedUser.getPassword())).isFalse();
        assertThat(passwordEncoder.matches(currentPassword, updatedUser.getPassword())).isTrue();
    }
*/
    
/*
    @Test
    @WithMockUser("change-password")
    void testChangePassword() throws Exception {
        User user = new User();
        String currentPassword = RandomStringUtils.random(60);
        user.setPassword(passwordEncoder.encode(currentPassword));
        user.setLogin("change-password");
        user.setEmail("change-password@example.com");
        user.setCreatedBy(Constants.SYSTEM);
        userRepository.save(user).block();

        accountWebTestClient
            .post()
            .uri("/api/account/change-password")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(new PasswordChangeDTO(currentPassword, "new password")))
            .exchange()
            .expectStatus()
            .isOk();

        User updatedUser = userRepository.findOneByLogin("change-password").block();
        assertThat(passwordEncoder.matches("new password", updatedUser.getPassword())).isTrue();
    }
*/
    
/*
    @Test
    @WithMockUser("change-password-too-small")
    void testChangePasswordTooSmall() throws Exception {
        User user = new User();
        String currentPassword = RandomStringUtils.random(60);
        user.setPassword(passwordEncoder.encode(currentPassword));
        user.setLogin("change-password-too-small");
        user.setEmail("change-password-too-small@example.com");
        user.setCreatedBy(Constants.SYSTEM);
        userRepository.save(user).block();

        String newPassword = RandomStringUtils.random(ManagedUserVM.PASSWORD_MIN_LENGTH - 1);

        accountWebTestClient
            .post()
            .uri("/api/account/change-password")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(new PasswordChangeDTO(currentPassword, newPassword)))
            .exchange()
            .expectStatus()
            .isBadRequest();

        User updatedUser = userRepository.findOneByLogin("change-password-too-small").block();
        assertThat(updatedUser.getPassword()).isEqualTo(user.getPassword());
    }
*/
    
/*
    @Test
    @WithMockUser("change-password-too-long")
    void testChangePasswordTooLong() throws Exception {
        User user = new User();
        String currentPassword = RandomStringUtils.random(60);
        user.setPassword(passwordEncoder.encode(currentPassword));
        user.setLogin("change-password-too-long");
        user.setEmail("change-password-too-long@example.com");
        user.setCreatedBy(Constants.SYSTEM);
        userRepository.save(user).block();

        String newPassword = RandomStringUtils.random(ManagedUserVM.PASSWORD_MAX_LENGTH + 1);

        accountWebTestClient
            .post()
            .uri("/api/account/change-password")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(new PasswordChangeDTO(currentPassword, newPassword)))
            .exchange()
            .expectStatus()
            .isBadRequest();

        User updatedUser = userRepository.findOneByLogin("change-password-too-long").block();
        assertThat(updatedUser.getPassword()).isEqualTo(user.getPassword());
    }
*/
    
/*
    @Test
    @WithMockUser("change-password-empty")
    void testChangePasswordEmpty() throws Exception {
        User user = new User();
        String currentPassword = RandomStringUtils.random(60);
        user.setPassword(passwordEncoder.encode(currentPassword));
        user.setLogin("change-password-empty");
        user.setEmail("change-password-empty@example.com");
        user.setCreatedBy(Constants.SYSTEM);
        userRepository.save(user).block();

        accountWebTestClient
            .post()
            .uri("/api/account/change-password")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(new PasswordChangeDTO(currentPassword, "")))
            .exchange()
            .expectStatus()
            .isBadRequest();

        User updatedUser = userRepository.findOneByLogin("change-password-empty").block();
        assertThat(updatedUser.getPassword()).isEqualTo(user.getPassword());
    }
*/
    
/*
    @Test
    void testRequestPasswordReset() {
        User user = new User();
        user.setPassword(RandomStringUtils.random(60));
        user.setActivated(true);
        user.setLogin("password-reset");
        user.setEmail("password-reset@example.com");
        user.setCreatedBy(Constants.SYSTEM);
        userRepository.save(user).block();

        accountWebTestClient
            .post()
            .uri("/api/account/reset-password/init")
            .bodyValue("password-reset@example.com")
            .exchange()
            .expectStatus()
            .isOk();
    }
*/
    
/*
    @Test
    void testRequestPasswordResetUpperCaseEmail() {
        User user = new User();
        user.setPassword(RandomStringUtils.random(60));
        user.setActivated(true);
        user.setLogin("password-reset-upper-case");
        user.setEmail("password-reset-upper-case@example.com");
        user.setCreatedBy(Constants.SYSTEM);
        userRepository.save(user).block();

        accountWebTestClient
            .post()
            .uri("/api/account/reset-password/init")
            .bodyValue("password-reset-upper-case@EXAMPLE.COM")
            .exchange()
            .expectStatus()
            .isOk();
    }
*/
    
/*
    @Test
    void testRequestPasswordResetWrongEmail() {
        accountWebTestClient
            .post()
            .uri("/api/account/reset-password/init")
            .bodyValue("password-reset-wrong-email@example.com")
            .exchange()
            .expectStatus()
            .isOk();
    }
*/
    
/*
    @Test
    void testFinishPasswordReset() throws Exception {
        User user = new User();
        user.setPassword(RandomStringUtils.random(60));
        user.setLogin("finish-password-reset");
        user.setEmail("finish-password-reset@example.com");
        user.setResetDate(Instant.now().plusSeconds(60));
        user.setResetKey("reset key");
        user.setCreatedBy(Constants.SYSTEM);
        userRepository.save(user).block();

        KeyAndPasswordVM keyAndPassword = new KeyAndPasswordVM();
        keyAndPassword.setKey(user.getResetKey());
        keyAndPassword.setNewPassword("new password");

        accountWebTestClient
            .post()
            .uri("/api/account/reset-password/finish")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(keyAndPassword))
            .exchange()
            .expectStatus()
            .isOk();

        User updatedUser = userRepository.findOneByLogin(user.getLogin()).block();
        assertThat(passwordEncoder.matches(keyAndPassword.getNewPassword(), updatedUser.getPassword())).isTrue();
    }
*/
    
/*
    @Test
    void testFinishPasswordResetTooSmall() throws Exception {
        User user = new User();
        user.setPassword(RandomStringUtils.random(60));
        user.setLogin("finish-password-reset-too-small");
        user.setEmail("finish-password-reset-too-small@example.com");
        user.setResetDate(Instant.now().plusSeconds(60));
        user.setResetKey("reset key too small");
        user.setCreatedBy(Constants.SYSTEM);
        userRepository.save(user).block();

        KeyAndPasswordVM keyAndPassword = new KeyAndPasswordVM();
        keyAndPassword.setKey(user.getResetKey());
        keyAndPassword.setNewPassword("foo");

        accountWebTestClient
            .post()
            .uri("/api/account/reset-password/finish")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(keyAndPassword))
            .exchange()
            .expectStatus()
            .isBadRequest();

        User updatedUser = userRepository.findOneByLogin(user.getLogin()).block();
        assertThat(passwordEncoder.matches(keyAndPassword.getNewPassword(), updatedUser.getPassword())).isFalse();
    }
*/
    
/*
    @Test
    void testFinishPasswordResetWrongKey() throws Exception {
        KeyAndPasswordVM keyAndPassword = new KeyAndPasswordVM();
        keyAndPassword.setKey("wrong reset key");
        keyAndPassword.setNewPassword("new password");

        accountWebTestClient
            .post()
            .uri("/api/account/reset-password/finish")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(keyAndPassword))
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
 */
}