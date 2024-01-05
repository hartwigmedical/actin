package com.hartwig.actin.clinical.datamodel

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
