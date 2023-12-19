package com.hartwig.actin.molecular.interpretation

import com.google.common.collect.Lists
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.characteristics.MolecularCharacteristics
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.util.MolecularCharacteristicEvents
import org.apache.logging.log4j.LogManager

object AggregatedEvidenceFactory {
    private val LOGGER = LogManager.getLogger(AggregatedEvidenceFactory::class.java)

    @JvmStatic
    fun create(molecular: MolecularRecord): AggregatedEvidence {
        val evidences: MutableList<AggregatedEvidence> = Lists.newArrayList()
        evidences.addAll(aggregateCharacteristicsEvidence(molecular.characteristics()))
        evidences.addAll(aggregateDriverEvidence(molecular.drivers()))
        val aggregateBuilder: ImmutableAggregatedEvidence.Builder = ImmutableAggregatedEvidence.builder()
        for (evidence in evidences) {
            aggregateBuilder.putAllApprovedTreatmentsPerEvent(evidence.approvedTreatmentsPerEvent())
            aggregateBuilder.putAllExternalEligibleTrialsPerEvent(evidence.externalEligibleTrialsPerEvent())
            aggregateBuilder.putAllOnLabelExperimentalTreatmentsPerEvent(evidence.onLabelExperimentalTreatmentsPerEvent())
            aggregateBuilder.putAllOffLabelExperimentalTreatmentsPerEvent(evidence.offLabelExperimentalTreatmentsPerEvent())
            aggregateBuilder.putAllPreClinicalTreatmentsPerEvent(evidence.preClinicalTreatmentsPerEvent())
            aggregateBuilder.putAllKnownResistantTreatmentsPerEvent(evidence.knownResistantTreatmentsPerEvent())
            aggregateBuilder.putAllSuspectResistanceTreatmentsPerEvent(evidence.suspectResistanceTreatmentsPerEvent())
        }
        return aggregateBuilder.build()
    }

    private fun aggregateCharacteristicsEvidence(characteristics: MolecularCharacteristics): List<AggregatedEvidence> {
        val evidences: MutableList<AggregatedEvidence> = Lists.newArrayList()
        if (hasCharacteristic(characteristics.isMicrosatelliteUnstable())) {
            evidences.add(
                createAggregateEvidence(
                    MolecularCharacteristicEvents.MICROSATELLITE_UNSTABLE,
                    characteristics.microsatelliteEvidence()
                )
            )
        } else if (hasEvidence(characteristics.microsatelliteEvidence())) {
            LOGGER.warn("There is evidence for microsatellite stability without presence of signature")
        }
        if (hasCharacteristic(characteristics.isHomologousRepairDeficient)) {
            evidences.add(
                createAggregateEvidence(
                    MolecularCharacteristicEvents.HOMOLOGOUS_REPAIR_DEFICIENT,
                    characteristics.homologousRepairEvidence()
                )
            )
        } else if (hasEvidence(characteristics.homologousRepairEvidence())) {
            LOGGER.warn("There is evidence for homologous repair deficiency without presence of signature")
        }
        if (hasCharacteristic(characteristics.hasHighTumorMutationalBurden())) {
            evidences.add(
                createAggregateEvidence(
                    MolecularCharacteristicEvents.HIGH_TUMOR_MUTATIONAL_BURDEN,
                    characteristics.tumorMutationalBurdenEvidence()
                )
            )
        } else if (hasEvidence(characteristics.tumorMutationalBurdenEvidence())) {
            LOGGER.warn("There is evidence for high tumor mutational burden without presence of signature")
        }
        if (hasCharacteristic(characteristics.hasHighTumorMutationalLoad())) {
            evidences.add(
                createAggregateEvidence(
                    MolecularCharacteristicEvents.HIGH_TUMOR_MUTATIONAL_LOAD,
                    characteristics.tumorMutationalLoadEvidence()
                )
            )
        } else if (hasEvidence(characteristics.tumorMutationalLoadEvidence())) {
            LOGGER.warn("There is evidence for high tumor mutational load without presence of signature")
        }
        return evidences
    }

    private fun hasCharacteristic(characteristic: Boolean?): Boolean {
        return characteristic != null && characteristic
    }

    private fun aggregateDriverEvidence(drivers: MolecularDrivers): List<AggregatedEvidence> {
        val evidences: MutableList<AggregatedEvidence> = Lists.newArrayList()
        for (variant in drivers.variants()) {
            evidences.add(createAggregateEvidence(variant!!.event(), variant.evidence()))
        }
        for (copyNumber in drivers.copyNumbers()) {
            evidences.add(createAggregateEvidence(copyNumber!!.event(), copyNumber.evidence()))
        }
        for (homozygousDisruption in drivers.homozygousDisruptions()) {
            evidences.add(createAggregateEvidence(homozygousDisruption!!.event(), homozygousDisruption.evidence()))
        }
        for (disruption in drivers.disruptions()) {
            evidences.add(createAggregateEvidence(disruption!!.event(), disruption.evidence()))
        }
        for (fusion in drivers.fusions()) {
            evidences.add(createAggregateEvidence(fusion!!.event(), fusion.evidence()))
        }
        for (virus in drivers.viruses()) {
            evidences.add(createAggregateEvidence(virus!!.event(), virus.evidence()))
        }
        return evidences
    }

    private fun hasEvidence(evidence: ActionableEvidence?): Boolean {
        return if (evidence == null) {
            false
        } else (!evidence.approvedTreatments().isEmpty() || !evidence.externalEligibleTrials().isEmpty()
                || !evidence.onLabelExperimentalTreatments().isEmpty() || !evidence.offLabelExperimentalTreatments().isEmpty()
                || !evidence.preClinicalTreatments().isEmpty() || !evidence.knownResistantTreatments().isEmpty()
                || !evidence.suspectResistantTreatments().isEmpty())
    }

    private fun createAggregateEvidence(event: String, evidence: ActionableEvidence?): AggregatedEvidence {
        val builder: ImmutableAggregatedEvidence.Builder = ImmutableAggregatedEvidence.builder()
        if (evidence == null) {
            return builder.build()
        }
        if (!evidence.approvedTreatments().isEmpty()) {
            builder.putAllApprovedTreatmentsPerEvent(event, evidence.approvedTreatments())
        }
        if (!evidence.externalEligibleTrials().isEmpty()) {
            builder.putAllExternalEligibleTrialsPerEvent(event, evidence.externalEligibleTrials())
        }
        if (!evidence.onLabelExperimentalTreatments().isEmpty()) {
            builder.putAllOnLabelExperimentalTreatmentsPerEvent(event, evidence.onLabelExperimentalTreatments())
        }
        if (!evidence.offLabelExperimentalTreatments().isEmpty()) {
            builder.putAllOffLabelExperimentalTreatmentsPerEvent(event, evidence.offLabelExperimentalTreatments())
        }
        if (!evidence.preClinicalTreatments().isEmpty()) {
            builder.putAllPreClinicalTreatmentsPerEvent(event, evidence.preClinicalTreatments())
        }
        if (!evidence.knownResistantTreatments().isEmpty()) {
            builder.putAllKnownResistantTreatmentsPerEvent(event, evidence.knownResistantTreatments())
        }
        if (!evidence.suspectResistantTreatments().isEmpty()) {
            builder.putAllSuspectResistanceTreatmentsPerEvent(event, evidence.suspectResistantTreatments())
        }
        return builder.build()
    }
}
