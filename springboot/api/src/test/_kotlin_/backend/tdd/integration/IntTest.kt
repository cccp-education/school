package backend.tdd.integration

//import org.junit.jupiter.api.AfterAll
//import org.junit.jupiter.api.BeforeAll
//import kotlin.test.Test
//import org.springframework.context.ConfigurableApplicationContext
//import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
//import org.springframework.test.http.reactive.server.WebTestClient
//import org.springframework.test.http.reactive.server.expectBody
//import org.springframework.test.http.reactive.server.expectBodyList

class IntTest {

//    private val client = WebTestClient.bindToServer().baseUrl("http://localhost:8181").build()
//
//    private lateinit var context: ConfigurableApplicationContext
//
//    @BeforeAll
//    fun beforeAll() {
//        context = webApp.run(profiles = "test")
//    }
//
//
//    @Test
//    fun `Check Properties`() {
//        assertTrue(
//            "anno-r2dbc-jwt@localhost" ==
//                    context.environment.getProperty("anno.mail.from")
//        )
//        assertTrue(
//            "http://127.0.0.1:8080" ==
//                    context.environment.getProperty("anno.mail.base-url")
//        )
//    }
//
//    @Test
//    fun `Request base endpoint`() {
//        client.get().uri("/").exchange()
//            .expectStatus().isUnauthorized
////			.expectStatus().is2xxSuccessful
////			.expectHeader().contentType("text/plain;charset=UTF-8")
//    }
//
//    @Test
//    fun `Request HTTP API endpoint for listing all users`() {
//        client.get().uri("/api/user").exchange()
//            .expectStatus().is2xxSuccessful
//            .expectHeader().contentType(APPLICATION_JSON_VALUE)
//            .expectBodyList<DemoUser>()
//            .hasSize(3)
//    }
//
//    @Test
//    fun `Request HTTP API endpoint for getting one specified user`() {
//        client.get().uri("/api/user/bclozel").exchange()
//            .expectStatus().is2xxSuccessful
//            .expectHeader().contentType(APPLICATION_JSON_VALUE)
//            .expectBody<DemoUser>()
//            .isEqualTo(DemoUser("bclozel", "Brian", "Clozel"))
//    }
//
//    @Test
//    fun `Request conf endpoint`() {
//        client.get().uri("/api/conf").exchange()
//            .expectStatus().isUnauthorized
////			.expectStatus().is2xxSuccessful
////			.expectHeader().contentType("text/plain;charset=UTF-8")
//    }
//
//    @AfterAll
//    fun afterAll() {
//        context.close()
//    }
}