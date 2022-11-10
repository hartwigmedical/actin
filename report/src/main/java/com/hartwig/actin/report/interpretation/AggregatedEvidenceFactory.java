package com.hartwig.actin.report.interpretation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.characteristics.MolecularCharacteristics;
import com.hartwig.actin.molecular.datamodel.driver.Amplification;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.Fusion;
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.Loss;
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.datamodel.driver.Virus;
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence;
import com.hartwig.actin.molecular.util.MolecularEventFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AggregatedEvidenceFactory {

    private static final Logger LOGGER = LogManager.getLogger(AggregatedEvidenceFactory.class);

    private AggregatedEvidenceFactory() {
    }

    @NotNull
    public static AggregatedEvidence create(@NotNull MolecularRecord molecular) {
        List<AggregatedEvidence> evidences = Lists.newArrayList();

        evidences.addAll(aggregateCharacteristicsEvidence(molecular.characteristics()));
        evidences.addAll(aggregateDriverEvidence(molecular.drivers()));

        ImmutableAggregatedEvidence.Builder aggregateBuilder = ImmutableAggregatedEvidence.builder();
        for (AggregatedEvidence evidence : evidences) {
            aggregateBuilder.putAllApprovedTreatmentsPerEvent(evidence.approvedTreatmentsPerEvent());
            aggregateBuilder.putAllExternalEligibleTrialsPerEvent(evidence.externalEligibleTrialsPerEvent());
            aggregateBuilder.putAllOnLabelExperimentalTreatmentsPerEvent(evidence.onLabelExperimentalTreatmentsPerEvent());
            aggregateBuilder.putAllOffLabelExperimentalTreatmentsPerEvent(evidence.offLabelExperimentalTreatmentsPerEvent());
            aggregateBuilder.putAllPreClinicalTreatmentsPerEvent(evidence.preClinicalTreatmentsPerEvent());
            aggregateBuilder.knownResistantTreatmentsPerEvent(evidence.knownResistantTreatmentsPerEvent());
            aggregateBuilder.suspectResistanceTreatmentsPerEvent(evidence.suspectResistanceTreatmentsPerEvent());
        }
        return aggregateBuilder.build();
    }

    @NotNull
    private static List<AggregatedEvidence> aggregateCharacteristicsEvidence(@NotNull MolecularCharacteristics characteristics) {
        List<AggregatedEvidence> evidences = Lists.newArrayList();

        if (hasCharacteristic(characteristics.isMicrosatelliteUnstable())) {
            evidences.add(createAggregateEvidence(MolecularEventFactory.MICROSATELLITE_UNSTABLE, characteristics.microsatelliteEvidence()));
        } else if (hasEvidence(characteristics.microsatelliteEvidence())) {
            LOGGER.warn("There is evidence for microsatellite stability without presence of signature");
        }

        if (hasCharacteristic(characteristics.isHomologousRepairDeficient())) {
            evidences.add(createAggregateEvidence(MolecularEventFactory.HOMOLOGOUS_REPAIR_DEFICIENT,
                    characteristics.homologousRepairDeficiencyEvidence()));
        } else if (hasEvidence(characteristics.homologousRepairDeficiencyEvidence())) {
            LOGGER.warn("There is evidence for homologous repair deficiency without presence of signature");
        }

        if (hasCharacteristic(characteristics.hasHighTumorMutationalBurden())) {
            evidences.add(createAggregateEvidence(MolecularEventFactory.HIGH_TUMOR_MUTATIONAL_BURDEN,
                    characteristics.tumorMutationalBurdenEvidence()));
        } else if (hasEvidence(characteristics.tumorMutationalBurdenEvidence())) {
            LOGGER.warn("There is evidence for high tumor mutational burden without presence of signature");
        }

        if (hasCharacteristic(characteristics.hasHighTumorMutationalLoad())) {
            evidences.add(createAggregateEvidence(MolecularEventFactory.HIGH_TUMOR_MUTATIONAL_LOAD,
                    characteristics.tumorMutationalLoadEvidence()));
        } else if (hasEvidence(characteristics.tumorMutationalLoadEvidence())) {
            LOGGER.warn("There is evidence for high tumor mutational load without presence of signature");
        }

        return evidences;
    }

    private static boolean hasCharacteristic(@Nullable Boolean characteristic) {
        return characteristic != null && characteristic;
    }

    @NotNull
    private static List<AggregatedEvidence> aggregateDriverEvidence(@NotNull MolecularDrivers drivers) {
        List<AggregatedEvidence> evidences = Lists.newArrayList();
        for (Variant variant : drivers.variants()) {
            evidences.add(createAggregateEvidence(MolecularEventFactory.event(variant), variant.evidence()));
        }

        for (Amplification amplification : drivers.amplifications()) {
            evidences.add(createAggregateEvidence(MolecularEventFactory.event(amplification), amplification.evidence()));
        }

        for (Loss loss : drivers.losses()) {
            evidences.add(createAggregateEvidence(MolecularEventFactory.event(loss), loss.evidence()));
        }

        for (HomozygousDisruption homozygousDisruption : drivers.homozygousDisruptions()) {
            evidences.add(createAggregateEvidence(MolecularEventFactory.event(homozygousDisruption), homozygousDisruption.evidence()));
        }

        for (Disruption disruption : drivers.disruptions()) {
            evidences.add(createAggregateEvidence(MolecularEventFactory.event(disruption), disruption.evidence()));
        }

        for (Fusion fusion : drivers.fusions()) {
            evidences.add(createAggregateEvidence(MolecularEventFactory.event(fusion), fusion.evidence()));
        }

        for (Virus virus : drivers.viruses()) {
            evidences.add(createAggregateEvidence(MolecularEventFactory.event(virus), virus.evidence()));
        }

        return evidences;
    }

    private static boolean hasEvidence(@Nullable ActionableEvidence evidence) {
        if (evidence == null) {
            return false;
        }

        return !evidence.approvedTreatments().isEmpty() || !evidence.externalEligibleTrials().isEmpty()
                || !evidence.onLabelExperimentalTreatments().isEmpty() || !evidence.offLabelExperimentalTreatments().isEmpty()
                || !evidence.preClinicalTreatments().isEmpty() || !evidence.knownResistantTreatments().isEmpty()
                || !evidence.suspectResistantTreatments().isEmpty();
    }

    @NotNull
    private static AggregatedEvidence createAggregateEvidence(@NotNull String event, @Nullable ActionableEvidence evidence) {
        ImmutableAggregatedEvidence.Builder builder = ImmutableAggregatedEvidence.builder();
        if (evidence == null) {
            return builder.build();
        }

        if (!evidence.approvedTreatments().isEmpty()) {
            builder.putAllApprovedTreatmentsPerEvent(event, evidence.approvedTreatments());
        }

        if (!evidence.externalEligibleTrials().isEmpty()) {
            builder.putAllExternalEligibleTrialsPerEvent(event, evidence.externalEligibleTrials());
        }

        if (!evidence.onLabelExperimentalTreatments().isEmpty()) {
            builder.putAllOnLabelExperimentalTreatmentsPerEvent(event, evidence.onLabelExperimentalTreatments());
        }

        if (!evidence.offLabelExperimentalTreatments().isEmpty()) {
            builder.putAllOffLabelExperimentalTreatmentsPerEvent(event, evidence.offLabelExperimentalTreatments());
        }

        if (!evidence.preClinicalTreatments().isEmpty()) {
            builder.putAllPreClinicalTreatmentsPerEvent(event, evidence.preClinicalTreatments());
        }

        if (!evidence.knownResistantTreatments().isEmpty()) {
            builder.putAllKnownResistantTreatmentsPerEvent(event, evidence.knownResistantTreatments());
        }

        if (!evidence.suspectResistantTreatments().isEmpty()) {
            builder.putAllSuspectResistanceTreatmentsPerEvent(event, evidence.suspectResistantTreatments());
        }

        return builder.build();
    }
}
