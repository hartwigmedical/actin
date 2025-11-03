package com.hartwig.actin.molecular.util

import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.evidence.Actionable
import com.hartwig.actin.datamodel.molecular.evidence.ExternalTrial
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidence
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidenceCategories
import com.hartwig.actin.molecular.interpretation.ActionableAndEvidenceFactory

data class AggregatedEvidence(
    val treatmentEvidencePerEvent: Map<String, Set<TreatmentEvidence>> = emptyMap(),
    val eligibleTrialsPerEvent: Map<String, Set<ExternalTrial>> = emptyMap(),
) {
    fun approvedTreatmentsPerEvent() = filterMap { TreatmentEvidenceCategories.approved(it.value) }
    fun onLabelExperimentalTreatmentPerEvent() = filterMap { TreatmentEvidenceCategories.experimental(it.value, true) }
    fun offLabelExperimentalTreatmentsPerEvent() = filterMap { TreatmentEvidenceCategories.experimental(it.value, false) }
    fun preClinicalTreatmentsPerEvent() = filterMap { TreatmentEvidenceCategories.preclinical(it.value, true) }
    fun knownResistantTreatmentsPerEvent() = filterMap { TreatmentEvidenceCategories.knownResistant(it.value, true) }
    fun suspectResistantTreatmentsPerEvent() = filterMap { TreatmentEvidenceCategories.suspectResistant(it.value, true) }

    private fun filterMap(mappingFunction: (Map.Entry<String, Set<TreatmentEvidence>>) -> List<TreatmentEvidence>) =
        treatmentEvidencePerEvent.mapValues(mappingFunction).filterValues { it.isNotEmpty() }

    companion object {
        fun create(molecular: MolecularTest): AggregatedEvidence {
            return AggregatedEvidence(
                mapByEvent(ActionableAndEvidenceFactory.createTreatmentEvidences(molecular)), mapByEvent(
                    ActionableAndEvidenceFactory.createTrialEvidences(
                        molecular
                    )
                )
            )
        }

        private fun <E> mapByEvent(actionableAndEvidences: List<Pair<Actionable, Set<E>>>): Map<String, Set<E>> {
            return actionableAndEvidences.groupBy({ it.first.eventName()!! }, { it.second })
                .mapValues { (_, values) -> values.flatten().toSet() }
        }
    }
}