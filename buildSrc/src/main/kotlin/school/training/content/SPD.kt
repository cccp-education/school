package school.training.content

import com.fasterxml.jackson.annotation.JsonRootName

/** SPD: Scénario Pédagogique Détaillé */
@JvmRecord
@JsonRootName(value = "SPD")
data class SPD(
//    val titre: String,
//    val objectif: String,
    val table:Pair<SequenceTable,TimingTable>
) {
    class SequenceTable {

    }

    class TimingTable {

    }
}