package com.hartwig.actin.molecular.interpretation

import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.evidence.Actionable
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.datamodel.molecular.evidence.ExternalTrial
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidence

object AggregatedEvidenceFactory {

    fun create(molecular: MolecularTest): AggregatedEvidence {
        return if (!molecular.hasSufficientQuality) {
            AggregatedEvidence()
        } else {
            AggregatedEvidence(
                createTreatmentEvidences(molecular),
                createTrialEvidences(molecular)
            )
        }
    }

    fun createTreatmentEvidences(molecular: MolecularTest): Map<String, Set<TreatmentEvidence>> {
        return mapByEvent(actionableList(molecular)) { evidence -> evidence.treatmentEvidence }
    }

    fun createTrialEvidences(molecular: MolecularTest): Map<String, Set<ExternalTrial>> {
        return mapByEvent(actionableList(molecular)) { evidence -> evidence.eligibleTrials }
    }

    private fun actionableList(molecular: MolecularTest): List<Actionable> {
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

    private fun <E> mapByEvent(actionableList: List<Actionable>, valueFunction: (ClinicalEvidence) -> Set<E>): Map<String, Set<E>> {
        return actionableList.filter { it.eventName() != null }.groupBy({ it.eventName()!! }, { valueFunction(it.evidence) })
            .mapValues { (_, values) -> values.flatten().toSet() }
            .filterValues { it.isNotEmpty() }
    }
}
