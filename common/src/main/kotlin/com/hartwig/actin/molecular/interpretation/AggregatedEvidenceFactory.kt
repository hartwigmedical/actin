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
            aggregateCharacteristicsEvidence(molecular.characteristics) + aggregateDriverEvidence(molecular.drivers) + aggregateDriverEvidenceVariants(
                molecular.drivers
            )
        )
    }

    private fun aggregateCharacteristicsEvidence(characteristics: MolecularCharacteristics): List<AggregatedEvidence> {
        return listOfNotNull(
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
        )
    }

    private fun aggregatedEvidenceForCharacteristic(
        characteristic: String,
        hasCharacteristic: Boolean?,
        evidence: ClinicalEvidence?
    ): AggregatedEvidence? {
        if (hasCharacteristic == true) {
            return createAggregatedEvidence(characteristic, null, null, evidence)
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
            drivers.copyNumbers, drivers.homozygousDisruptions, drivers.disruptions, drivers.fusions, drivers.viruses
        ).flatMap { driverSet -> driverSet.map { createAggregatedEvidence(it.event, null, null, it.evidence) } }
    }

    private fun aggregateDriverEvidenceVariants(drivers: Drivers): List<AggregatedEvidence> {
        return listOf(
            drivers.variants
        ).flatMap { driverSet ->
            driverSet.map {
                createAggregatedEvidence(
                    it.event,
                    it.canonicalImpact.affectedCodon,
                    it.canonicalImpact.affectedExon,
                    it.evidence
                )
            }
        }
    }


    private fun createAggregatedEvidence(
        event: String,
        codon: Int?,
        exons: Int?,
        evidence: ClinicalEvidence?
    ): AggregatedEvidence {
        return if (evidence == null) {
            AggregatedEvidence()
        } else {

            val aggregatedEvidenceKey = AggregatedEvidenceKey(
                codon = codon,
                exon = exons,
                event = event,
            )

            AggregatedEvidence(
                treatmentEvidencePerEvent = mapByEvent(aggregatedEvidenceKey, evidence.treatmentEvidence),
                eligibleTrialsPerEvent = mapByEvent(aggregatedEvidenceKey, evidence.eligibleTrials),
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

    private fun <T> mapByEvent(key: AggregatedEvidenceKey, subset: Set<T>): Map<AggregatedEvidenceKey, Set<T>> {
        return if (subset.isEmpty()) emptyMap() else mapOf(key to subset)
    }
}
