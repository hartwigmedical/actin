package com.hartwig.actin.clinical.datamodel

enum class CypInteractionType {
    INDUCER,
    INHIBITOR,
    SUBSTRATE
}

enum class CypInteractionStrength {
    STRONG,
    MODERATE,
    WEAK,
    SENSITIVE,
    MODERATE_SENSITIVE
}

data class CypInteraction(
    val type: CypInteractionType,
    val strength: CypInteractionStrength,
    val cyp: String,
)
