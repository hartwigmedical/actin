package com.hartwig.actin.molecular.interpretation

import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.evidence.Actionable
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.datamodel.molecular.evidence.ExternalTrial
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidence

object AggregatedEvidenceFactory {

    fun create(molecular: MolecularTest): AggregatedEvidence {
        return AggregatedEvidence(
            mapByEvent(createTreatmentEvidences(molecular)), mapByEvent(createTrialEvidences(molecular))
        )
    }

    private fun <E> mapByEvent(actionableAndEvidences: List<Pair<Actionable, Set<E>>>): Map<String, Set<E>> {
        return actionableAndEvidences.groupBy({ it.first.eventName()!! }, { it.second })
            .mapValues { (_, values) -> values.flatten().toSet() }
    }

    fun createTreatmentEvidences(molecular: MolecularTest): List<Pair<Actionable, Set<TreatmentEvidence>>> {
        return actionableAndEvidences(actionableList(molecular)) { evidence -> evidence.treatmentEvidence }
    }

    fun createTrialEvidences(molecular: MolecularTest): List<Pair<Actionable, Set<ExternalTrial>>> {
        return actionableAndEvidences(actionableList(molecular)) { evidence -> evidence.eligibleTrials }
    }

    private fun actionableList(molecular: MolecularTest): List<Actionable> {
        return if (!molecular.hasSufficientQuality) {
            emptyList()
        } else {
            val drivers = molecular.drivers
            val characteristics = molecular.characteristics
            return drivers.variants + drivers.copyNumbers + drivers.homozygousDisruptions + drivers.disruptions + drivers.fusions + drivers.viruses +
                    listOfNotNull(
                        characteristics.microsatelliteStability,
                        characteristics.homologousRecombination,
                        characteristics.tumorMutationalBurden,
                        characteristics.tumorMutationalLoad
                    )
        }
    }

    private fun <E> actionableAndEvidences(
        actionableList: List<Actionable>, valueFunction: (ClinicalEvidence) -> Set<E>
    ): List<Pair<Actionable, Set<E>>> {
        return actionableList.filter { it.eventName() != null }.map { it to valueFunction(it.evidence) }.filter { it.second.isNotEmpty() }
    }
}
