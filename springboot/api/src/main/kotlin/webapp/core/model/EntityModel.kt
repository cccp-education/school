package webapp.core.model

// Abstract entity model with Generic ID, which can be of any type
abstract class EntityModel<T>(
    open val id: T? = null
) {
    object Members {
        const val ID_MEMBER = "id"

        // Generic extension function that allows the ID to be applied to any EntityModel type
        inline fun <reified T : EntityModel<ID>, ID> T.withId(id: ID): T {
            // Use reflection to create a copy with the passed ID
            return this::class.constructors.first { it.parameters.any { param -> param.name == ID_MEMBER } }
                .call(id, *this::class.constructors.first().parameters.drop(1).map { param ->
                    this::class.members.first { member -> member.name == param.name }.call(this)
                }.toTypedArray())
        }
    }
}

