package com.hartwig.actin.datamodel.clinical

data class DrugInteraction(
    val type: Type,
    val strength: Strength,
    val name: String,
) {

    enum class Type {
        INDUCER,
        INHIBITOR,
        SUBSTRATE
    }

    enum class Strength {
        STRONG,
        MODERATE,
        WEAK,
        SENSITIVE,
        MODERATE_SENSITIVE,
        UNKNOWN
    }
}
