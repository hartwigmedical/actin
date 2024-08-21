package com.hartwig.actin.molecular.datamodel.evidence

import com.hartwig.serve.datamodel.EvidenceLevel

enum class ActinEvidenceCategory {
    APPROVED, OFF_LABEL_EXPERIMENTAL, PRE_CLINICAL, KNOWN_RESISTANT, SUSPECT_RESISTANT, ON_LABEL_EXPERIMENTAL
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
    fun onLabelExperimentalTreatments() =
        filter(ActinEvidenceCategory.ON_LABEL_EXPERIMENTAL).filter { it !in approvedTreatments() }.toSet()

    fun offLabelExperimentalTreatments() =
        filter(ActinEvidenceCategory.OFF_LABEL_EXPERIMENTAL).filter { it !in approvedTreatments() && it !in onLabelExperimentalTreatments() }
            .toSet()

    fun preClinicalTreatments() =
        filter(ActinEvidenceCategory.PRE_CLINICAL).filter { it !in approvedTreatments() && it !in onLabelExperimentalTreatments() && it !in offLabelExperimentalTreatments() }
            .toSet()

    fun knownResistantTreatments() = filter(ActinEvidenceCategory.KNOWN_RESISTANT).filter {
        treatmentsRelevantForResistance(it)
    }.toSet()

    fun suspectResistantTreatments() = filter(ActinEvidenceCategory.SUSPECT_RESISTANT).filter { treatmentsRelevantForResistance(it) }
        .filter { it !in knownResistantTreatments() }.toSet()

    private fun treatmentsRelevantForResistance(it: String) =
        it in approvedTreatments() || it in onLabelExperimentalTreatments() || it in offLabelExperimentalTreatments()

    private fun filter(category: ActinEvidenceCategory) =
        actionableTreatments.filter { it.category == category }.map { it.name }.toSet()

    operator fun plus(other: ActionableEvidence): ActionableEvidence {
        return ActionableEvidence(
            externalEligibleTrials + other.externalEligibleTrials,
            actionableTreatments + other.actionableTreatments
        )
    }
}
