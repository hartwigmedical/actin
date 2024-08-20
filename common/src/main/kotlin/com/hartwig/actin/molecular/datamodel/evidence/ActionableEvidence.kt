package com.hartwig.actin.molecular.datamodel.evidence

import com.hartwig.serve.datamodel.EvidenceLevel

enum class ActinEvidenceCategory {
    APPROVED, ON_LABEL, OFF_LABEL, OFF_LABEL_EXPERIMENTAL, PRE_CLINICAL, KNOWN_RESISTANT, SUSPECT_RESISTANT, ON_LABEL_EXPERIMENTAL
}

enum class EvidenceTier {
    I, II, III, IV
}

data class ActionableTreatment(
    val name: String,
    val evidenceLevel: EvidenceLevel,
    val category: ActinEvidenceCategory
)

data class ActionableEvidence(
    val externalEligibleTrials: Set<ExternalTrial> = emptySet(),
    val actionableTreatments: Set<ActionableTreatment> = emptySet(),
) {

    fun approvedTreatments() = filter(ActinEvidenceCategory.APPROVED)
    fun onLabelTreatments() = filter(ActinEvidenceCategory.ON_LABEL)
    fun offLabelTreatments() = filter(ActinEvidenceCategory.OFF_LABEL)
    fun onLabelExperimentalTreatments() = filter(ActinEvidenceCategory.ON_LABEL_EXPERIMENTAL)
    fun offLabelExperimentalTreatments() = filter(ActinEvidenceCategory.OFF_LABEL_EXPERIMENTAL)
    fun preClinicalTreatments() = filter(ActinEvidenceCategory.PRE_CLINICAL)
    fun knownResistantTreatments() = filter(ActinEvidenceCategory.KNOWN_RESISTANT)
    fun suspectResistantTreatments() = filter(ActinEvidenceCategory.SUSPECT_RESISTANT)

    private fun filter(category: ActinEvidenceCategory) =
        actionableTreatments.filter { it.category == category }.map { it.name }.toSet()

    fun evidenceTier(): EvidenceTier {
        return when {
            actionableTreatments.any { it.evidenceLevel in setOf(EvidenceLevel.A, EvidenceLevel.B) } -> EvidenceTier.I
            actionableTreatments.any {
                it.evidenceLevel !in setOf(
                    EvidenceLevel.A,
                    EvidenceLevel.B
                )
            } && actionableTreatments.any { it.evidenceLevel !in setOf(EvidenceLevel.C, EvidenceLevel.D) } -> EvidenceTier.II

            else -> EvidenceTier.III
        }
    }

    operator fun plus(other: ActionableEvidence): ActionableEvidence {
        return ActionableEvidence(
            externalEligibleTrials + other.externalEligibleTrials,
            actionableTreatments + other.actionableTreatments
        )
    }
}
