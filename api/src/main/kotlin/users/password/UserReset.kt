package users.password

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.*

@JvmRecord
data class UserReset(
    val id: UUID,
    val userId: UUID,
    @JsonIgnore
    @field:Size(max = 20)
    val resetKey: String,
    val resetDate: Instant,
)