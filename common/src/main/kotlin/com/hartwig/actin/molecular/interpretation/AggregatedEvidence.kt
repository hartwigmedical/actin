package com.hartwig.actin.molecular.interpretation

import com.hartwig.actin.molecular.datamodel.evidence.ActinEvidenceCategory
import com.hartwig.actin.molecular.datamodel.evidence.ActionableTreatment
import com.hartwig.actin.molecular.datamodel.evidence.ExternalTrial

data class AggregatedEvidence(
    val externalEligibleTrialsPerEvent: Map<String, Set<ExternalTrial>> = emptyMap(),
    val actionableTreatments: Map<String, Set<ActionableTreatment>> = emptyMap(),
) {
    fun approvedTreatmentsPerEvent() = filter(ActinEvidenceCategory.APPROVED)
    fun onLabelExperimentalTreatmentPerEvent() = filter(ActinEvidenceCategory.ON_LABEL_EXPERIMENTAL)
    fun offLabelExperimentalTreatmentsPerEvent() = filter(ActinEvidenceCategory.OFF_LABEL_EXPERIMENTAL)
    fun preClinicalTreatmentsPerEvent() = filter(ActinEvidenceCategory.PRE_CLINICAL)
    fun knownResistantTreatmentsPerEvent() = filter(ActinEvidenceCategory.KNOWN_RESISTANT)
    fun suspectResistantTreatmentsPerEvent() = filter(ActinEvidenceCategory.SUSPECT_RESISTANT)

    private fun filter(category: ActinEvidenceCategory) =
        actionableTreatments.mapValues { it.value.filter { c -> c.category == category } }
            .filterValues { it.isNotEmpty() }
}
