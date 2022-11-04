package com.hartwig.actin.report.interpretation;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.characteristics.MolecularCharacteristics;
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class EvidenceInterpreter {

    private EvidenceInterpreter() {
    }

    @NotNull
    public static Set<String> eventsWithApprovedEvidence(@NotNull MolecularRecord molecular) {
        Set<String> eventsWithApprovedEvidence = Sets.newHashSet();

        eventsWithApprovedEvidence.addAll(characteristicsWithApprovedEvidence(molecular.characteristics()));
        eventsWithApprovedEvidence.addAll(driversWithApprovedEvidence(molecular.drivers()));

        return eventsWithApprovedEvidence;
    }

    @NotNull
    private static Set<String> characteristicsWithApprovedEvidence(@NotNull MolecularCharacteristics characteristics) {
        Set<String> events = Sets.newHashSet();
        if (hasApprovedEvidence(characteristics.microsatelliteEvidence())
                && hasCharacteristic(characteristics.isMicrosatelliteUnstable())) {
            events.add(MolecularEventFactory.MICROSATELLITE_UNSTABLE);
        }

        if (hasApprovedEvidence(characteristics.homologousRepairDeficiencyEvidence())
                && hasCharacteristic(characteristics.isHomologousRepairDeficient())) {
            events.add(MolecularEventFactory.HOMOLOGOUS_REPAIR_DEFICIENT);
        }

        if (hasApprovedEvidence(characteristics.tumorMutationalBurdenEvidence())
                && hasCharacteristic(characteristics.hasHighTumorMutationalBurden())) {
            events.add(MolecularEventFactory.HIGH_TUMOR_MUTATIONAL_BURDEN);
        }

        if (hasApprovedEvidence(characteristics.tumorMutationalLoadEvidence())
                && hasCharacteristic(characteristics.hasHighTumorMutationalLoad())) {
            events.add(MolecularEventFactory.HIGH_TUMOR_MUTATIONAL_LOAD);
        }

        return events;
    }

    @NotNull
    private static Set<String> driversWithApprovedEvidence(@NotNull MolecularDrivers drivers) {
        Set<String> events = Sets.newHashSet();
        for (Variant variant : drivers.variants()) {
            if (hasApprovedEvidence(variant.evidence())) {
                events.add(MolecularEventFactory.variantEvent(variant));
            }
        }
        return events;
    }

    private static boolean hasApprovedEvidence(@Nullable ActionableEvidence evidence) {
        return evidence != null && !evidence.approvedTreatments().isEmpty();
    }

    private static boolean hasCharacteristic(@Nullable Boolean characteristic) {
        return characteristic != null && characteristic;
    }
}
