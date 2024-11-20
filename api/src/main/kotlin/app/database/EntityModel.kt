package app.database

// Abstract entity model with Generic ID, which can be of any type
abstract class EntityModel<T>(
    open val id: T? = null
) {
    object Members {
        const val ID_MEMBER = "id"

        // Generic extension function that allows the ID to be applied to any EntityModel type
        inline fun <reified T : EntityModel<ID>, ID> T.withId(id: ID): T =
            // Use reflection to create a copy with the passed ID
            this::class.constructors.first {
                it.parameters.any { param -> param.name == ID_MEMBER }
            }.call(id, *this::class
                .constructors
                .first()
                .parameters
                .drop(1).map { param ->
                    this::class
                        .members
                        .first { member -> member.name == param.name }
                        .call(this)
                }.toTypedArray()
            )
    }

    companion object {
        const val MODEL_FIELD_OBJECTNAME = "objectName"
        const val MODEL_FIELD_FIELD = "field"
        const val MODEL_FIELD_MESSAGE = "message"
    }
}

