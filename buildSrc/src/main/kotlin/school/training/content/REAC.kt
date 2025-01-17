package school.training.content

import com.fasterxml.jackson.annotation.JsonRootName

@JvmRecord
@JsonRootName(value = "REAC")
data class REAC(
    val name: String,
    val position: String,
    val activities: List<TypicalActivity>
){
    data class TypicalActivity(val name: String, val skills: List<ProfessionalSkill>)
    data class ProfessionalSkill(val description: String)
}