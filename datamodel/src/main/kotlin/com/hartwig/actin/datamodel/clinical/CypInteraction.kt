package com.hartwig.actin.datamodel.clinical

data class CypInteraction(
    val type: Type,
    val strength: Strength,
    val cyp: String,
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
        MODERATE_SENSITIVE
    }
}
