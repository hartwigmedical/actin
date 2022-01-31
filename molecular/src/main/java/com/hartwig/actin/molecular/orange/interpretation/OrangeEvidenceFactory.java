package com.hartwig.actin.molecular.orange.interpretation;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.ImmutableMolecularEvidence;
import com.hartwig.actin.molecular.datamodel.MolecularEvidence;
import com.hartwig.actin.molecular.orange.datamodel.EvidenceLevel;
import com.hartwig.actin.molecular.orange.datamodel.TreatmentEvidence;
import com.hartwig.actin.molecular.orange.util.GenomicEventFormatter;

import org.jetbrains.annotations.NotNull;

final class OrangeEvidenceFactory {

    static final String ACTIN_SOURCE = "ACTIN";
    static final String ICLUSION_SOURCE = "ICLUSION";
    static final String CKB_SOURCE = "CKB";

    static final Set<String> NON_APPLICABLE_GENES = Sets.newHashSet();
    static final Set<String> NON_APPLICABLE_EVENTS = Sets.newHashSet();

    static {
        NON_APPLICABLE_GENES.add("CDKN2A");

        NON_APPLICABLE_EVENTS.add("VEGFA amp");
    }

    private OrangeEvidenceFactory() {
    }

    @NotNull
    public static List<MolecularEvidence> createActinTreatmentEvidence(@NotNull List<TreatmentEvidence> evidences) {
        List<MolecularEvidence> actinTreatmentEvidences = Lists.newArrayList();

        for (TreatmentEvidence evidence : reportedForSource(evidences, ACTIN_SOURCE)) {
            actinTreatmentEvidences.add(ImmutableMolecularEvidence.builder()
                    .event(toEvent(evidence))
                    .treatment(evidence.treatment())
                    .build());
        }
        return actinTreatmentEvidences;
    }

    @NotNull
    public static List<MolecularEvidence> createGeneralTrialEvidence(@NotNull List<TreatmentEvidence> evidences) {
        List<MolecularEvidence> generalTrialEvidence = Lists.newArrayList();

        for (TreatmentEvidence evidence : reportedForSource(evidences, ICLUSION_SOURCE)) {
            if (isPotentiallyApplicable(evidence)) {
                generalTrialEvidence.add(ImmutableMolecularEvidence.builder()
                        .event(toEvent(evidence))
                        .treatment(evidence.treatment())
                        .build());
            }
        }
        return generalTrialEvidence;
    }

    @NotNull
    public static List<MolecularEvidence> createGeneralResponsiveEvidence(@NotNull List<TreatmentEvidence> evidences) {
        List<MolecularEvidence> generalResponsiveEvidence = Lists.newArrayList();
        for (TreatmentEvidence evidence : reportedForSource(evidences, CKB_SOURCE)) {
            boolean isPotentiallyApplicable = isPotentiallyApplicable(evidence);
            boolean isResponsiveEvidence = evidence.direction().isResponsive();

            if (isPotentiallyApplicable && isResponsiveEvidence) {
                generalResponsiveEvidence.add(ImmutableMolecularEvidence.builder()
                        .event(toEvent(evidence))
                        .treatment(evidence.treatment())
                        .build());
            }
        }
        return generalResponsiveEvidence;
    }

    @NotNull
    public static List<MolecularEvidence> createGeneralResistanceEvidence(@NotNull List<TreatmentEvidence> evidences) {
        List<MolecularEvidence> generalResistanceEvidence = Lists.newArrayList();

        List<TreatmentEvidence> reportedCkbEvidences = reportedForSource(evidences, CKB_SOURCE);
        for (TreatmentEvidence evidence : reportedCkbEvidences) {
            boolean isPotentiallyApplicable = isPotentiallyApplicable(evidence);
            boolean isResistanceEvidence = evidence.direction().isResistant();
            boolean hasOnLabelResponsiveEvidenceOfSameLevelOrHigher =
                    hasOnLabelResponsiveEvidenceWithMaxLevel(reportedCkbEvidences, evidence.treatment(), evidence.level());

            if (isPotentiallyApplicable && isResistanceEvidence && hasOnLabelResponsiveEvidenceOfSameLevelOrHigher) {
                generalResistanceEvidence.add(ImmutableMolecularEvidence.builder()
                        .event(toEvent(evidence))
                        .treatment(evidence.treatment())
                        .build());
            }
        }

        return generalResistanceEvidence;
    }

    @NotNull
    private static List<TreatmentEvidence> reportedForSource(@NotNull List<TreatmentEvidence> evidences, @NotNull String source) {
        List<TreatmentEvidence> filtered = Lists.newArrayList();
        for (TreatmentEvidence evidence : evidences) {
            if (evidence.reported() && evidence.sources().contains(source)) {
                filtered.add(evidence);
            }
        }
        return filtered;
    }

    private static boolean hasOnLabelResponsiveEvidenceWithMaxLevel(@NotNull List<TreatmentEvidence> evidences, @NotNull String treatment,
            @NotNull EvidenceLevel maxLevel) {
        for (TreatmentEvidence evidence : evidences) {
            if (evidence.direction().isResponsive() && evidence.treatment().equals(treatment) && evidence.onLabel()
                    && maxLevel.isBetterOrEqual(evidence.level())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isPotentiallyApplicable(@NotNull TreatmentEvidence evidence) {
        if ((evidence.level() == EvidenceLevel.C || evidence.level() == EvidenceLevel.D || (evidence.level() == EvidenceLevel.B
                && evidence.direction().isPredicted())) && !evidence.onLabel()) {
            return false;
        }

        String gene = evidence.gene();
        for (String nonApplicableGene : NON_APPLICABLE_GENES) {
            if (gene != null && gene.equals(nonApplicableGene)) {
                return false;
            }
        }

        String event = toEvent(evidence);
        for (String nonApplicableEvent : NON_APPLICABLE_EVENTS) {
            if (event.equals(nonApplicableEvent)) {
                return false;
            }
        }

        return true;
    }

    @NotNull
    private static String toEvent(@NotNull TreatmentEvidence evidence) {
        String gene = evidence.gene();
        String event = GenomicEventFormatter.format(evidence.event());
        return gene != null ? gene + " " + event : event;
    }
}
