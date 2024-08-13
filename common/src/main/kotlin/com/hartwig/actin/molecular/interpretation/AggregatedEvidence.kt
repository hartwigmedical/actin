package com.hartwig.actin.molecular.interpretation

import com.hartwig.actin.molecular.datamodel.evidence.ActinEvidenceCategory
import com.hartwig.actin.molecular.datamodel.evidence.ActionableTreatment
import com.hartwig.actin.molecular.datamodel.evidence.ExternalTrial

data class AggregatedEvidence(
    val externalEligibleTrialsPerEvent: Map<String, Set<ExternalTrial>> = emptyMap(),
    val actionableTreatments: Map<String, Set<ActionableTreatment>> = emptyMap(),
) {
    fun approvedTreatmentsPerEvent() = filter(ActinEvidenceCategory.APPROVED)
    fun onLabelTreatmentsPerEvent() = filter(ActinEvidenceCategory.ON_LABEL)
    fun offLabelTreatmentsPerEvent() = filter(ActinEvidenceCategory.OFF_LABEL)
    fun onLabelExperimentalTreatmentPerEvent() = filter(ActinEvidenceCategory.ON_LABEL_EXPERIMENTAL)
    fun offLabelExperimentalTreatmentsPerEvent() = filter(ActinEvidenceCategory.OFF_LABEL_EXPERIMENTAL)
    fun preClinicalPerEvent() = filter(ActinEvidenceCategory.PRE_CLINICAL)
    fun knownResistantPerEvent() = filter(ActinEvidenceCategory.KNOWN_RESISTANT)
    fun suspectResistantPerEvent() = filter(ActinEvidenceCategory.SUSPECT_RESISTANT)

    private fun filter(category: ActinEvidenceCategory) =
        actionableTreatments.mapValues { it.value.filter { it.category == category } }
}
