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
    public static List<MolecularEvidence> createActinTrials(@NotNull List<TreatmentEvidence> evidences) {
        List<MolecularEvidence> result = Lists.newArrayList();
        for (TreatmentEvidence evidence : reportedApplicableForSource(evidences, ACTIN_SOURCE)) {
            result.add(toMolecularEvidence(evidence));
        }
        return result;
    }

    @NotNull
    public static List<MolecularEvidence> createExternalTrials(@NotNull List<TreatmentEvidence> evidences) {
        List<MolecularEvidence> result = Lists.newArrayList();
        for (TreatmentEvidence evidence : reportedApplicableForSource(evidences, ICLUSION_SOURCE)) {
            result.add(toMolecularEvidence(evidence));
        }
        return result;
    }

    @NotNull
    public static List<MolecularEvidence> createApprovedResponsiveEvidence(@NotNull List<TreatmentEvidence> evidences) {
        List<MolecularEvidence> result = Lists.newArrayList();
        for (TreatmentEvidence evidence : reportedApplicableForSource(evidences, CKB_SOURCE)) {
            if (evidence.direction().isResponsive() && isApproved(evidence)) {
                result.add(toMolecularEvidence(evidence));
            }
        }
        return result;
    }

    @NotNull
    public static List<MolecularEvidence> createExperimentalResponsiveEvidence(@NotNull List<TreatmentEvidence> evidences) {
        List<MolecularEvidence> result = Lists.newArrayList();

        List<TreatmentEvidence> ckbEvidences = reportedApplicableForSource(evidences, CKB_SOURCE);
        for (TreatmentEvidence evidence : ckbEvidences) {
            if (evidence.direction().isResponsive() && isExperimental(evidence)) {
                result.add(toMolecularEvidence(evidence));
            }
        }
        return result;
    }

    @NotNull
    public static List<MolecularEvidence> createOtherResponsiveEvidence(@NotNull List<TreatmentEvidence> evidences) {
        List<MolecularEvidence> result = Lists.newArrayList();

        List<TreatmentEvidence> ckbEvidences = reportedApplicableForSource(evidences, CKB_SOURCE);
        for (TreatmentEvidence evidence : ckbEvidences) {
            if (evidence.direction().isResponsive() && isOther(evidence)) {
                result.add(toMolecularEvidence(evidence));
            }
        }
        return result;
    }

    @NotNull
    public static List<MolecularEvidence> createResistanceEvidence(@NotNull List<TreatmentEvidence> evidences) {
        List<MolecularEvidence> result = Lists.newArrayList();

        List<TreatmentEvidence> reportedCkbEvidences = reportedApplicableForSource(evidences, CKB_SOURCE);
        for (TreatmentEvidence evidence : reportedCkbEvidences) {
            boolean hasEqualOrWorseResponsive = hasEqualOrWorseResponsive(reportedCkbEvidences, evidence.treatment(), evidence.level());

            if (evidence.direction().isResistant() && hasEqualOrWorseResponsive) {
                result.add(toMolecularEvidence(evidence));
            }
        }

        return result;
    }

    private static boolean hasEqualOrWorseResponsive(@NotNull List<TreatmentEvidence> evidences, @NotNull String treatment,
            @NotNull EvidenceLevel resistanceLevel) {
        for (TreatmentEvidence evidence : evidences) {
            boolean isRelevant = isApproved(evidence) || isExperimental(evidence) || isOther(evidence);

            if (evidence.direction().isResponsive() && evidence.treatment().equals(treatment) && isRelevant
                    && resistanceLevel.isBetterOrEqual(evidence.level())) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    private static List<TreatmentEvidence> reportedApplicableForSource(@NotNull List<TreatmentEvidence> evidences, @NotNull String source) {
        List<TreatmentEvidence> filtered = Lists.newArrayList();
        for (TreatmentEvidence evidence : evidences) {
            if (evidence.reported() && evidence.sources().contains(source) && isPotentiallyApplicable(evidence)) {
                filtered.add(evidence);
            }
        }
        return filtered;
    }

    private static boolean isPotentiallyApplicable(@NotNull TreatmentEvidence evidence) {
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

    private static boolean isApproved(@NotNull TreatmentEvidence evidence) {
        return evidence.onLabel() && evidence.level() == EvidenceLevel.A;
    }

    private static boolean isExperimental(@NotNull TreatmentEvidence evidence) {
        return (evidence.onLabel() && evidence.level() == EvidenceLevel.B) || (!evidence.onLabel() && evidence.level() == EvidenceLevel.A);
    }

    private static boolean isOther(@NotNull TreatmentEvidence evidence) {
        if (isApproved(evidence) || isExperimental(evidence)) {
            return false;
        }

        return evidence.level() != EvidenceLevel.D && evidence.level() != EvidenceLevel.C;
    }

    @NotNull
    private static MolecularEvidence toMolecularEvidence(@NotNull TreatmentEvidence evidence) {
        return ImmutableMolecularEvidence.builder().event(toEvent(evidence)).treatment(evidence.treatment()).build();
    }

    @NotNull
    private static String toEvent(@NotNull TreatmentEvidence evidence) {
        String gene = evidence.gene();
        String event = GenomicEventFormatter.format(evidence.event());
        return gene != null ? gene + " " + event : event;
    }
}
