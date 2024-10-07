//@file:Suppress("LeakingThis", "unused")
//
//package features
//
//import backend.Account
//import backend.AccountCredentials
//import backend.Log.log
//import backend.RandomUtils
//import backend.BackendApplication
//import com.fasterxml.jackson.databind.ObjectMapper
//import io.cucumber.datatable.DataTable
//import io.cucumber.java8.Fr
//import kotlinx.coroutines.reactor.mono
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT
//import org.springframework.http.HttpHeaders.CONTENT_TYPE
//import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
//import org.springframework.test.context.ActiveProfiles
//import org.springframework.test.web.reactive.server.FluxExchangeResult
//import org.springframework.test.web.reactive.server.WebTestClient
//import org.springframework.test.web.reactive.server.returnResult
//import reactor.kotlin.core.publisher.toMono
//import webapp.Application
//import kotlin.test.assertEquals
//
//@SpringBootTest(
//    classes = [Application::class],
//    webEnvironment = DEFINED_PORT
//)
//@ActiveProfiles("test")
//class SignUpStepDefinition : Fr {
//    @Autowired
//    private lateinit var objectMapper: ObjectMapper
//
//    //    private lateinit var response: org.springframework.web.reactive.function.client.ClientResponse
//    private var response: FluxExchangeResult<Account>? = null
//    private val client = WebTestClient.bindToServer()
//        .baseUrl("http://localhost:8080")
//        .defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
//        .build()
//    private lateinit var currentAccount: AccountCredentials
//    private var accounts: List<AccountCredentials> = mutableListOf()
//
//
//    init {
////initialiser un coroutine context reactor
//
//        Etantdonné("une liste de login, email, password, firstName, lastName") { dataTable: DataTable ->
//            if (accounts.isNotEmpty()) (accounts as MutableList).clear()
//            dataTable.asMaps().map {
//                (accounts as MutableList).add(
//                    AccountCredentials(
//                        activationKey = RandomUtils.generateActivationKey,
//                        login = it["login"],
//                        email = it["email"],
//                        password = it["password"],
//                        firstName = it["firstName"],
//                        lastName = it["lastName"],
//                        activated = false,
//                        authorities = mutableSetOf()
//                    )
//                )
//            }
//        }
//        Etantdonné("l'utilisateur de la liste qui a pour login {string}") { login: String ->
//            currentAccount = accounts.first { it.login.equals(login, ignoreCase = true) }
//        }
//        Quand("on envoie la requête d'inscription de {string}") { login: String ->
//            assertEquals(expected = currentAccount.login, actual = login)
//            mono {
//                client.post().uri("/api/signup")
//                    .bodyValue(currentAccount)
//                    .toMono()
//                    .block()
//                    ?.exchange().apply { }
//                    ?.returnResult<Account>().apply {
//                        log.info(this!!.status)
//                        response = this
//                    }
//            }
//        }
//
//        Alors("le résultat est la création d'un nouveau compte non activé") {
//            assert(response != null)
//            response?.status?.is2xxSuccessful
//            log.info(response?.status)
//            //TODO: ne pas oublier de nettoyer la base
//        }
//    }
//
//}
///*
//package backend.features
////https://github.com/walter-the-coder/spring-boot-cucumber-wiremock-simulator
////import backend.tdd.calculator.add
////import backend.tdd.calculator.subtract
////import io.cucumber.java.Before
//////import io.cucumber.java8.En
////import io.cucumber.java.en.And
////import io.cucumber.java.en.Given
////import io.cucumber.java.en.Then
////import io.cucumber.java.en.When
////import io.cucumber.java8.En
////
////
////
////class Calculator8StepDefinition:En {
////    init{
////     var firstNumber: Int = 0
////     var secondNumber: Int = 0
////     var result: Int = 0
////    }
////
////    @Before
////    fun setUp() {
////        firstNumber = 0
////        secondNumber = 0
////        result = 0
////    }
////
////    @Given("a integer {int}")
////    fun `a integer`(number: Int) {
////        firstNumber = number
////    }
////
////    @And("a second integer {int}")
////    fun `a second integer`(number: Int) {
////        secondNumber = number
////    }
////
////    @When("the numbers are added")
////    fun `the numbers are added`() {
////        result = add(firstNumber, secondNumber)
////    }
////
////    @When("the numbers are subtracted")
////    fun `the numbers are subtracted`() {
////        result = subtract(firstNumber, secondNumber)
////    }
////
////    @Then("the result is {int}")
////    fun `the result is`(result: Int) {
////        assert(result == this.result)
////    }
////
////}
//
// */
//
