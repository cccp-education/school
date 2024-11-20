package ai

//import org.springframework.ai.chat.ChatClient
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.web.bind.annotation.GetMapping
//import org.springframework.web.bind.annotation.RequestParam
//import org.springframework.web.bind.annotation.RestController
//
//@RestController
//class SimpleAiController @Autowired constructor(private val chatClient: ChatClient) {
//    @GetMapping("/ai/simple")
//    fun completion(@RequestParam(value = "message", defaultValue = "Tell me a joke") message: String?)
//            : Map<String, String> = java.util.Map.of("generation", chatClient.call(message))
//}

/*
Voici un exemple de code en Kotlin avec Spring Boot pour créer un contrôleur REST réactif de manière déclarative et fonctionnelle, en utilisant un builder pour sa déclaration :

```kotlin
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.support.beans
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

@SpringBootApplication
class ReactiveDemoApplication

fun main(args: Array<String>) {
    runApplication<ReactiveDemoApplication>(*args) {
        addInitializers(beans {
            bean {
                router {
                    "/api".nest {
                        GET("/hello") { _ ->
                            ServerResponse.ok()
                                .contentType(MediaType.TEXT_PLAIN)
                                .bodyValue("Hello, World!")
                        }
                    }
                }
            }
        })
    }
}
```

Dans cet exemple :

- Nous avons une classe principale `ReactiveDemoApplication` annotée avec `@SpringBootApplication`.
- Dans la fonction `main`, nous utilisons `runApplication` pour démarrer l'application Spring Boot.
- Nous ajoutons un bean de type `router`, qui définit les routes de notre application.
- À l'intérieur du `router`, nous déclarons notre contrôleur REST réactif de manière déclarative et fonctionnelle.
- Le contrôleur est défini avec un builder, où nous définissons une route `"/api/hello"` qui répond à une requête GET avec "Hello, World!" comme corps de la réponse.

Cet exemple utilise la programmation fonctionnelle de Spring WebFlux pour créer un contrôleur REST réactif sans utiliser d'annotations. Au lieu de cela, nous définissons les routes et les handlers de manière déclarative en utilisant les fonctionnalités fournies par Spring WebFlux.
 */
