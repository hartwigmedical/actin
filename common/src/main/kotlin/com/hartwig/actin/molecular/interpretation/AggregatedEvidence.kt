package com.hartwig.actin.molecular.interpretation

import com.hartwig.actin.datamodel.molecular.evidence.ExternalTrial
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidence
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidenceCategories.approved
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidenceCategories.experimental
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidenceCategories.knownResistant
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidenceCategories.preclinical
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidenceCategories.suspectResistant

data class AggregatedEvidence(
    val externalEligibleTrialsPerEvent: Map<String, Set<ExternalTrial>> = emptyMap(),
    val treatmentEvidence: Map<String, Set<TreatmentEvidence>> = emptyMap(),
) {
    fun approvedTreatmentsPerEvent() = filterMap { approved(it.value) }
    fun onLabelExperimentalTreatmentPerEvent() = filterMap { experimental(it.value, true) }
    fun offLabelExperimentalTreatmentsPerEvent() = filterMap { experimental(it.value, false) }
    fun preClinicalTreatmentsPerEvent() = filterMap { preclinical(it.value, true) }
    fun knownResistantTreatmentsPerEvent() = filterMap { knownResistant(it.value, true) }
    fun suspectResistantTreatmentsPerEvent() = filterMap { suspectResistant(it.value, true) }

    private fun filterMap(mappingFunction: (Map.Entry<String, Set<TreatmentEvidence>>) -> List<TreatmentEvidence>) =
        treatmentEvidence.mapValues(mappingFunction).filterValues { it.isNotEmpty() }
}
