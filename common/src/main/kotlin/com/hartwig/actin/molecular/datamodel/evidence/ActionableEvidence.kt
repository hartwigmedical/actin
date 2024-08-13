package com.hartwig.actin.molecular.datamodel.evidence

import com.hartwig.serve.datamodel.EvidenceLevel

enum class EvidenceTier {
    I, II, III, IV
}

enum class ActinEvidenceCategory {
    APPROVED, ON_LABEL, OFF_LABEL, OFF_LABEL_EXPERIMENTAL, PRE_CLINICAL, KNOWN_RESISTANT, SUSPECT_RESISTANT, ON_LABEL_EXPERIMENTAL
}

data class ActionableTreatment(
    val name: String,
    val evidenceLevel: EvidenceLevel,
    val evidenceTier: EvidenceTier,
    val category: ActinEvidenceCategory
)

data class ActionableEvidence(
    val externalEligibleTrials: Set<ExternalTrial> = emptySet(),
    val actionableTreatments: Set<ActionableTreatment> = emptySet()
) {

    fun approvedTreatments() = filter(ActinEvidenceCategory.APPROVED)
    fun onLabelTreatments() = filter(ActinEvidenceCategory.ON_LABEL)
    fun offLabelTreatments() = filter(ActinEvidenceCategory.OFF_LABEL)
    fun onLabelExperimentalTreatments() = filter(ActinEvidenceCategory.ON_LABEL_EXPERIMENTAL)
    fun offLabelExperimentalTreatments() = filter(ActinEvidenceCategory.OFF_LABEL_EXPERIMENTAL)
    fun preClinicalTreatments() = filter(ActinEvidenceCategory.PRE_CLINICAL)
    fun knownResistant() = filter(ActinEvidenceCategory.KNOWN_RESISTANT)
    fun suspectResistant() = filter(ActinEvidenceCategory.SUSPECT_RESISTANT)

    private fun filter(category: ActinEvidenceCategory) =
        actionableTreatments.filter { it.category == category }.map { it.name }.toSet()


    operator fun plus(other: ActionableEvidence): ActionableEvidence {
        return ActionableEvidence(
            externalEligibleTrials + other.externalEligibleTrials,
            actionableTreatments + other.actionableTreatments
        )
    }
}
