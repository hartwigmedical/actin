package com.hartwig.actin.molecular.interpretation

import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.characteristics.MolecularCharacteristics
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.util.MolecularCharacteristicEvents
import org.apache.logging.log4j.LogManager

object AggregatedEvidenceFactory {

    private val LOGGER = LogManager.getLogger(AggregatedEvidenceFactory::class.java)

    fun create(molecular: MolecularRecord): AggregatedEvidence {
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
                evidence.approvedTreatments,
                evidence.externalEligibleTrials,
                evidence.onLabelExperimentalTreatments,
                evidence.offLabelExperimentalTreatments,
                evidence.preClinicalTreatments,
                evidence.knownResistantTreatments,
                evidence.suspectResistantTreatments
            ).any(Set<String>::isNotEmpty)
        }
    }

    private fun aggregateDriverEvidence(drivers: MolecularDrivers): List<AggregatedEvidence> {
        return listOf(
            drivers.variants, drivers.copyNumbers, drivers.homozygousDisruptions, drivers.disruptions, drivers.fusions, drivers.viruses
        ).flatMap { driverSet -> driverSet.map { createAggregatedEvidence(it.event, it.evidence) } }
    }

    private fun createAggregatedEvidence(event: String, evidence: ActionableEvidence?): AggregatedEvidence {
        return if (evidence == null) {
            AggregatedEvidence()
        } else {
            AggregatedEvidence(
                approvedTreatmentsPerEvent = evidenceMap(event, evidence.approvedTreatments),
                externalEligibleTrialsPerEvent = evidenceMap(event, evidence.externalEligibleTrials),
                onLabelExperimentalTreatmentsPerEvent = evidenceMap(event, evidence.onLabelExperimentalTreatments),
                offLabelExperimentalTreatmentsPerEvent = evidenceMap(event, evidence.offLabelExperimentalTreatments),
                preClinicalTreatmentsPerEvent = evidenceMap(event, evidence.preClinicalTreatments),
                knownResistantTreatmentsPerEvent = evidenceMap(event, evidence.knownResistantTreatments),
                suspectResistantTreatmentsPerEvent = evidenceMap(event, evidence.suspectResistantTreatments)
            )
        }
    }

    private fun mergeAggregatedEvidenceList(aggregatedEvidenceList: List<AggregatedEvidence>): AggregatedEvidence {
        return AggregatedEvidence(
            approvedTreatmentsPerEvent = mergeMapsOfLists(aggregatedEvidenceList.map(AggregatedEvidence::approvedTreatmentsPerEvent)),
            externalEligibleTrialsPerEvent =
            mergeMapsOfLists(aggregatedEvidenceList.map(AggregatedEvidence::externalEligibleTrialsPerEvent)),
            onLabelExperimentalTreatmentsPerEvent =
            mergeMapsOfLists(aggregatedEvidenceList.map(AggregatedEvidence::onLabelExperimentalTreatmentsPerEvent)),
            offLabelExperimentalTreatmentsPerEvent =
            mergeMapsOfLists(aggregatedEvidenceList.map(AggregatedEvidence::offLabelExperimentalTreatmentsPerEvent)),
            preClinicalTreatmentsPerEvent =
            mergeMapsOfLists(aggregatedEvidenceList.map(AggregatedEvidence::preClinicalTreatmentsPerEvent)),
            knownResistantTreatmentsPerEvent =
            mergeMapsOfLists(aggregatedEvidenceList.map(AggregatedEvidence::knownResistantTreatmentsPerEvent)),
            suspectResistantTreatmentsPerEvent =
            mergeMapsOfLists(aggregatedEvidenceList.map(AggregatedEvidence::suspectResistantTreatmentsPerEvent)),
        )
    }

    private fun mergeMapsOfLists(mapsOfSets: List<Map<String, List<String>>>): Map<String, List<String>> {
        return mapsOfSets
            .flatMap { it.entries }
            .groupBy({ it.key }, { it.value })
            .mapValues { it.value.flatten() }
    }

    private fun evidenceMap(event: String, evidenceSubSet: Set<String>): Map<String, List<String>> {
        return if (evidenceSubSet.isEmpty()) emptyMap() else mapOf(event to evidenceSubSet.toList())
    }
}
