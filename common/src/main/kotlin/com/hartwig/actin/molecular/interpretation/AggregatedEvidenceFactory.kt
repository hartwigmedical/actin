package com.hartwig.actin.molecular.interpretation

import com.hartwig.actin.molecular.datamodel.Drivers
import com.hartwig.actin.molecular.datamodel.MolecularCharacteristics
import com.hartwig.actin.molecular.datamodel.MolecularTest
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.util.MolecularCharacteristicEvents
import org.apache.logging.log4j.LogManager

object AggregatedEvidenceFactory {

    private val LOGGER = LogManager.getLogger(AggregatedEvidenceFactory::class.java)

    fun create(molecular: MolecularTest): AggregatedEvidence {
        return mergeAggregatedEvidenceList(
            aggregateCharacteristicsEvidence(molecular.characteristics) + aggregateDriverEvidence(molecular.drivers)
        )
    }

    private fun aggregateCharacteristicsEvidence(characteristics: MolecularCharacteristics): List<AggregatedEvidence> {
        return listOfNotNull(
            aggregatedEvidenceForCharacteristic(
                characteristics.isMicrosatelliteUnstable,
                MolecularCharacteristicEvents.MICROSATELLITE_UNSTABLE,
                characteristics.microsatelliteEvidence,
                "microsatellite stability"
            ),
            aggregatedEvidenceForCharacteristic(
                characteristics.isHomologousRepairDeficient,
                MolecularCharacteristicEvents.HOMOLOGOUS_REPAIR_DEFICIENT,
                characteristics.homologousRepairEvidence,
                "homologous repair deficiency"
            ),
            aggregatedEvidenceForCharacteristic(
                characteristics.hasHighTumorMutationalBurden,
                MolecularCharacteristicEvents.HIGH_TUMOR_MUTATIONAL_BURDEN,
                characteristics.tumorMutationalBurdenEvidence,
                "high tumor mutational burden"
            ),
            aggregatedEvidenceForCharacteristic(
                characteristics.hasHighTumorMutationalLoad,
                MolecularCharacteristicEvents.HIGH_TUMOR_MUTATIONAL_LOAD,
                characteristics.tumorMutationalLoadEvidence,
                "high tumor mutational load"
            )
        )
    }

    private fun aggregatedEvidenceForCharacteristic(
        characteristic: Boolean?, event: String, evidence: ActionableEvidence?, characteristicName: String
    ): AggregatedEvidence? {
        if (characteristic == true) {
            return createAggregatedEvidence(event, evidence)
        } else if (hasEvidence(evidence)) {
            LOGGER.warn("There is evidence for $characteristicName without presence of signature")
        }
        return null
    }

    private fun hasEvidence(evidence: ActionableEvidence?): Boolean {
        return if (evidence == null) false else {
            listOf(
                evidence.externalEligibleTrials,
                evidence.actionableTreatments
            ).any(Set<Any>::isNotEmpty)
        }
    }

    private fun aggregateDriverEvidence(drivers: Drivers): List<AggregatedEvidence> {
        return listOf(
            drivers.variants, drivers.copyNumbers, drivers.homozygousDisruptions, drivers.disruptions, drivers.fusions, drivers.viruses
        ).flatMap { driverSet -> driverSet.map { createAggregatedEvidence(it.event, it.evidence) } }
    }

    private fun createAggregatedEvidence(event: String, evidence: ActionableEvidence?): AggregatedEvidence {
        return if (evidence == null) {
            AggregatedEvidence()
        } else {
            AggregatedEvidence(
                externalEligibleTrialsPerEvent = evidenceMap(event, evidence.externalEligibleTrials),
                actionableTreatments = evidenceMap(event, evidence.actionableTreatments),
            )
        }
    }

    private fun mergeAggregatedEvidenceList(aggregatedEvidenceList: List<AggregatedEvidence>): AggregatedEvidence {
        return AggregatedEvidence(
            externalEligibleTrialsPerEvent =
            mergeMapsOfSets(aggregatedEvidenceList.map(AggregatedEvidence::externalEligibleTrialsPerEvent)),
            actionableTreatments =
            mergeMapsOfSets(aggregatedEvidenceList.map(AggregatedEvidence::actionableTreatments)),
        )
    }

    fun <T> mergeMapsOfSets(mapsOfSets: List<Map<String, Set<T>>>): Map<String, Set<T>> {
        return mapsOfSets
            .flatMap { it.entries }
            .groupBy({ it.key }, { it.value })
            .mapValues { it.value.flatten().toSet() }
    }

    private fun <T> evidenceMap(event: String, evidenceSubSet: Set<T>): Map<String, Set<T>> {
        return if (evidenceSubSet.isEmpty()) emptyMap() else mapOf(event to evidenceSubSet)
    }
}
