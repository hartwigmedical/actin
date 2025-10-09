package com.hartwig.actin.molecular.interpretation

import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.molecular.util.MolecularCharacteristicEvents
import com.hartwig.actin.util.MapFunctions
import org.apache.logging.log4j.LogManager

object AggregatedEvidenceFactory {

    private val LOGGER = LogManager.getLogger(AggregatedEvidenceFactory::class.java)

    fun create(molecular: MolecularTest): AggregatedEvidence {
        return if (!molecular.hasSufficientQuality) {
            AggregatedEvidence()
        } else mergeAggregatedEvidenceList(
            aggregateCharacteristicsEvidence(molecular.characteristics) + aggregateDriverEvidence(molecular.drivers)
        )
    }

    private fun aggregateCharacteristicsEvidence(characteristics: MolecularCharacteristics): List<AggregatedEvidence> {
        return listOfNotNull(

            createAggregatedEvidence(
                MolecularCharacteristicEvents.MICROSATELLITE_UNSTABLE,
                characteristics.microsatelliteStability?.evidence

                // I feel like this bit above we don't need? if I comment out and reduce the assert size to 4 (vs 5) it runs
                // but that could just because the test needs to change
                // In general I think the test needs to change to actually test everything below ???
            ),

            when (characteristics.microsatelliteStability?.isUnstable) {
                false -> createAggregatedEvidence(
                    MolecularCharacteristicEvents.MICROSATELLITE_STABLE,
                    characteristics.microsatelliteStability?.evidence
                )

                true -> createAggregatedEvidence(
                    MolecularCharacteristicEvents.MICROSATELLITE_UNSTABLE,
                    characteristics.microsatelliteStability?.evidence
                )

                else -> null
            },

            when (characteristics.homologousRecombination?.isDeficient) {
                false -> createAggregatedEvidence(
                    MolecularCharacteristicEvents.HOMOLOGOUS_RECOMBINATION_PROFICIENT,
                    characteristics.homologousRecombination?.evidence
                )

                true -> createAggregatedEvidence(
                    MolecularCharacteristicEvents.HOMOLOGOUS_RECOMBINATION_DEFICIENT,
                    characteristics.homologousRecombination?.evidence
                )

                else -> null
            },

            // Currently does not account for "ALMOST_SUFFICIENT_TUMOR_MUTATIONAL_BURDEN = "Almost sufficient TMB"
            // It will after draft PR completed
            when (characteristics.tumorMutationalBurden?.isHigh) {
                false -> createAggregatedEvidence(
                    MolecularCharacteristicEvents.LOW_TUMOR_MUTATIONAL_BURDEN,
                    characteristics.tumorMutationalBurden?.evidence
                )

                true -> createAggregatedEvidence(
                    MolecularCharacteristicEvents.HIGH_TUMOR_MUTATIONAL_BURDEN,
                    characteristics.tumorMutationalBurden?.evidence
                )

                else -> null
            },

            //Currently does not account for (1) ADEQUATE_TUMOR_MUTATIONAL_LOAD = "TML High" nor
            // (2) ALMOST_SUFFICIENT_TUMOR_MUTATIONAL_LOAD = "Almost sufficient TML"
            // It will after draft PR completed
            when (characteristics.tumorMutationalLoad?.isHigh) {
                false -> createAggregatedEvidence(
                    MolecularCharacteristicEvents.LOW_TUMOR_MUTATIONAL_LOAD,
                    characteristics.tumorMutationalLoad?.evidence
                )

                true -> createAggregatedEvidence(
                    MolecularCharacteristicEvents.HIGH_TUMOR_MUTATIONAL_LOAD,
                    characteristics.tumorMutationalLoad?.evidence
                )

                else -> null
            },
            /*

            aggregatedEvidenceForCharacteristic(
            MolecularCharacteristicEvents.MICROSATELLITE_UNSTABLE,
            characteristics.microsatelliteStability?.isUnstable,
            characteristics.microsatelliteStability?.evidence
            ),

            aggregatedEvidenceForCharacteristic(
            MolecularCharacteristicEvents.HOMOLOGOUS_RECOMBINATION_DEFICIENT,
            characteristics.homologousRecombination?.isDeficient,
            characteristics.homologousRecombination?.evidence
            ),

            aggregatedEvidenceForCharacteristic(
            MolecularCharacteristicEvents.HIGH_TUMOR_MUTATIONAL_BURDEN,
            characteristics.tumorMutationalBurden?.isHigh,
            characteristics.tumorMutationalBurden?.evidence
            ),

            aggregatedEvidenceForCharacteristic(
            MolecularCharacteristicEvents.HIGH_TUMOR_MUTATIONAL_LOAD,
            characteristics.tumorMutationalLoad?.isHigh,
            characteristics.tumorMutationalLoad?.evidence
            )

            */
        )
    }

    // Should we remove this now Khalid? and if so why?
    private fun aggregatedEvidenceForCharacteristic(
        characteristic: String,
        hasCharacteristic: Boolean?,
        evidence: ClinicalEvidence?
    ): AggregatedEvidence? {
        if (hasCharacteristic == true) {
            return createAggregatedEvidence(characteristic, evidence)
        } else if (hasEvidence(evidence)) {
            LOGGER.warn("There is evidence for $characteristic without presence of signature")
        }
        return null
    }

    private fun hasEvidence(evidence: ClinicalEvidence?): Boolean {
        return if (evidence == null) false else {
            listOf(
                evidence.treatmentEvidence,
                evidence.eligibleTrials
            ).any(Set<Any>::isNotEmpty)
        }
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
