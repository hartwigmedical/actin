package com.hartwig.actin.molecular.orange.interpretation;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.ImmutableMolecularEvidence;
import com.hartwig.actin.molecular.datamodel.MolecularEvidence;
import com.hartwig.actin.molecular.orange.datamodel.EvidenceLevel;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;
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
    public static MolecularEvidence create(@NotNull OrangeRecord record) {
        return ImmutableMolecularEvidence.builder()
                .actinTrialEvidence(createActinTrialEligibility(record.evidences()))
                .generalTrialSource("iClusion")
                .generalTrialEvidence(createGeneralTrialEligibility(record.evidences()))
                .generalEvidenceSource("CKB")
                .generalResponsiveEvidence(createGeneralResponsiveEvidence(record.evidences()))
                .generalResistanceEvidence(createGeneralResistanceEvidence(record.evidences()))
                .build();
    }

    @NotNull
    private static Multimap<String, String> createActinTrialEligibility(@NotNull List<TreatmentEvidence> evidences) {
        Multimap<String, String> actinTrialEligibility = ArrayListMultimap.create();

        for (TreatmentEvidence evidence : filter(evidences, ACTIN_SOURCE)) {
            if (evidence.reported()) {
                actinTrialEligibility.put(toEvent(evidence), evidence.treatment());
            }
        }
        return actinTrialEligibility;
    }

    @NotNull
    private static Multimap<String, String> createGeneralTrialEligibility(@NotNull List<TreatmentEvidence> evidences) {
        Multimap<String, String> generalTrialEligibility = ArrayListMultimap.create();

        for (TreatmentEvidence evidence : filter(evidences, ICLUSION_SOURCE)) {
            if (evidence.reported() && isPotentiallyApplicable(evidence)) {
                generalTrialEligibility.put(toEvent(evidence), evidence.treatment());
            }
        }
        return generalTrialEligibility;
    }

    @NotNull
    private static Multimap<String, String> createGeneralResponsiveEvidence(@NotNull List<TreatmentEvidence> evidences) {
        Multimap<String, String> generalResponsiveEvidence = ArrayListMultimap.create();
        for (TreatmentEvidence evidence : filter(evidences, CKB_SOURCE)) {
            boolean isReported = evidence.reported();
            boolean isPotentiallyApplicable = isPotentiallyApplicable(evidence);
            boolean isResponsiveEvidence = evidence.direction().isResponsive();

            if (isReported && isPotentiallyApplicable && isResponsiveEvidence) {
                generalResponsiveEvidence.put(toEvent(evidence), evidence.treatment());
            }
        }
        return generalResponsiveEvidence;
    }

    @NotNull
    private static Multimap<String, String> createGeneralResistanceEvidence(@NotNull List<TreatmentEvidence> evidences) {
        Multimap<String, String> generalResistanceEvidence = ArrayListMultimap.create();

        for (TreatmentEvidence evidence : filter(evidences, CKB_SOURCE)) {
            boolean isReported = evidence.reported();
            boolean isPotentiallyApplicable = isPotentiallyApplicable(evidence);
            boolean isResistanceEvidence = evidence.direction().isResistant();
            boolean hasOnLabelResponsiveEvidenceOfSameLevelOrHigher =
                    hasOnLabelResponsiveEvidenceWithMinLevel(evidences, evidence.treatment(), evidence.level());

            if (isReported && isPotentiallyApplicable && isResistanceEvidence && hasOnLabelResponsiveEvidenceOfSameLevelOrHigher) {
                generalResistanceEvidence.put(toEvent(evidence), evidence.treatment());
            }
        }

        return generalResistanceEvidence;
    }

    @NotNull
    private static List<TreatmentEvidence> filter(@NotNull List<TreatmentEvidence> evidences, @NotNull String source) {
        List<TreatmentEvidence> filtered = Lists.newArrayList();
        for (TreatmentEvidence evidence : evidences) {
            if (evidence.sources().contains(source)) {
                filtered.add(evidence);
            }
        }
        return filtered;
    }

    private static boolean hasOnLabelResponsiveEvidenceWithMinLevel(@NotNull List<TreatmentEvidence> evidences, @NotNull String treatment,
            @NotNull EvidenceLevel minLevel) {
        for (TreatmentEvidence evidence : evidences) {
            if (evidence.direction().isResponsive() && evidence.treatment().equals(treatment) && evidence.onLabel()
                    && minLevel.isBetterOrEqual(evidence.level())) {
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
