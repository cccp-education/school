package users.security

import app.database.EntityModel
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class Role(
    @field:NotNull
    @field:Size(max = 50)
    override val id: String
) : EntityModel<String>() {
}