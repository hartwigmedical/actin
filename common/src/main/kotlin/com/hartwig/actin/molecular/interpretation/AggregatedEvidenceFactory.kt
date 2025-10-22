package com.hartwig.actin.molecular.interpretation

import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristicEvents
import com.hartwig.actin.util.MapFunctions

object AggregatedEvidenceFactory {

    fun create(molecular: MolecularTest): AggregatedEvidence {
        return if (!molecular.hasSufficientQuality) {
            AggregatedEvidence()
        } else mergeAggregatedEvidenceList(
            aggregateCharacteristicsEvidence(molecular.characteristics) + aggregateDriverEvidence(molecular.drivers)
        )
    }

    private fun aggregateCharacteristicsEvidence(characteristics: MolecularCharacteristics): List<AggregatedEvidence> {
        return listOfNotNull(

            when (characteristics.microsatelliteStability?.isUnstable) {
                true -> createAggregatedEvidence(
                    MolecularCharacteristicEvents.MICROSATELLITE_UNSTABLE,
                    characteristics.microsatelliteStability?.evidence
                )

                false -> createAggregatedEvidence(
                    MolecularCharacteristicEvents.MICROSATELLITE_STABLE,
                    characteristics.microsatelliteStability?.evidence
                )

                else -> null
            },

            when (characteristics.homologousRecombination?.isDeficient) {
                true -> createAggregatedEvidence(
                    MolecularCharacteristicEvents.HOMOLOGOUS_RECOMBINATION_DEFICIENT,
                    characteristics.homologousRecombination?.evidence
                )

                else -> null
            },

            when (characteristics.tumorMutationalBurden?.isHigh) {
                true -> createAggregatedEvidence(
                    MolecularCharacteristicEvents.HIGH_TUMOR_MUTATIONAL_BURDEN,
                    characteristics.tumorMutationalBurden?.evidence
                )

                false -> createAggregatedEvidence(
                    MolecularCharacteristicEvents.LOW_TUMOR_MUTATIONAL_BURDEN,
                    characteristics.tumorMutationalBurden?.evidence
                )

                else -> null
            },

            when (characteristics.tumorMutationalLoad?.isHigh) {
                true -> createAggregatedEvidence(
                    MolecularCharacteristicEvents.HIGH_TUMOR_MUTATIONAL_LOAD,
                    characteristics.tumorMutationalLoad?.evidence
                )

                false -> createAggregatedEvidence(
                    MolecularCharacteristicEvents.LOW_TUMOR_MUTATIONAL_LOAD,
                    characteristics.tumorMutationalLoad?.evidence
                )

                else -> null
            },
        )
    }

    private fun aggregateDriverEvidence(drivers: Drivers): List<AggregatedEvidence> {
        return listOf(
            drivers.variants, drivers.copyNumbers, drivers.homozygousDisruptions, drivers.disruptions, drivers.fusions, drivers.viruses
        ).flatMap { driverSet -> driverSet.map { createAggregatedEvidence(it.event, it.evidence) } }
    }

    private fun createAggregatedEvidence(event: String, evidence: ClinicalEvidence?): AggregatedEvidence {
        return if (evidence == null) {
            AggregatedEvidence()
        } else {
            AggregatedEvidence(
                treatmentEvidencePerEvent = mapByEvent(event, evidence.treatmentEvidence),
                eligibleTrialsPerEvent = mapByEvent(event, evidence.eligibleTrials),
            )
        }
    }

    private fun mergeAggregatedEvidenceList(aggregatedEvidenceList: List<AggregatedEvidence>): AggregatedEvidence {
        return AggregatedEvidence(
            treatmentEvidencePerEvent = MapFunctions.mergeMapsOfSets(
                mapsOfSets = aggregatedEvidenceList.map(AggregatedEvidence::treatmentEvidencePerEvent)
            ),
            eligibleTrialsPerEvent = MapFunctions.mergeMapsOfSets(
                mapsOfSets = aggregatedEvidenceList.map(AggregatedEvidence::eligibleTrialsPerEvent)
            ),
        )
    }

    private fun <T> mapByEvent(event: String, subset: Set<T>): Map<String, Set<T>> {
        return if (subset.isEmpty()) emptyMap() else mapOf(event to subset)
    }
}
