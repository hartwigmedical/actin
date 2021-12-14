package com.hartwig.actin.molecular.interpretation;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.EvidenceLevel;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.TreatmentEvidence;
import com.hartwig.actin.molecular.util.GenomicEventFormatter;

import org.jetbrains.annotations.NotNull;

public final class MolecularInterpreter {

    static final String ACTIN_SOURCE = "ACTIN";
    static final String ICLUSION_SOURCE = "ICLUSION";
    static final String CKB_SOURCE = "CKB";

    private static final Set<String> NON_APPLICABLE_START_KEYWORDS = Sets.newHashSet();
    private static final Set<String> NON_APPLICABLE_EVENTS = Sets.newHashSet();

    static {
        NON_APPLICABLE_START_KEYWORDS.add("CDKN2A");

        NON_APPLICABLE_EVENTS.add("VEGFA full gain");
        NON_APPLICABLE_EVENTS.add("VEGFA partial gain");
    }

    private MolecularInterpreter() {
    }

    @NotNull
    public static MolecularInterpretation interpret(@NotNull MolecularRecord record) {
        List<TreatmentEvidence> evidencesForTrials = filterEvidences(record, ACTIN_SOURCE);
        List<TreatmentEvidence> iclusionEvidences = filterEvidences(record, ICLUSION_SOURCE);
        List<TreatmentEvidence> ckbEvidences = filterEvidences(record, CKB_SOURCE);

        return ImmutableMolecularInterpretation.builder()
                .eventsWithTrialEligibility(applicableResponsiveEvents(evidencesForTrials))
                .iclusionApplicableEvents(applicableResponsiveEvents(iclusionEvidences))
                .ckbApplicableResponsiveEvents(applicableResponsiveEvents(ckbEvidences))
                .ckbApplicableResistanceEvents(applicableResistanceEvents(ckbEvidences))
                .build();
    }

    @NotNull
    private static List<TreatmentEvidence> filterEvidences(@NotNull MolecularRecord record, @NotNull String source) {
        List<TreatmentEvidence> filtered = Lists.newArrayList();
        for (TreatmentEvidence evidence : record.evidences()) {
            if (evidence.sources().contains(source)) {
                filtered.add(evidence);
            }
        }
        return filtered;
    }

    @NotNull
    private static Set<String> applicableResponsiveEvents(@NotNull List<TreatmentEvidence> evidences) {
        Set<String> events = Sets.newTreeSet();
        for (TreatmentEvidence evidence : evidences) {
            boolean isPotentiallyApplicable = isPotentiallyApplicable(evidence);
            boolean isResponsiveEvidence = evidence.direction().isResponsive();

            if (isPotentiallyApplicable && isResponsiveEvidence) {
                events.add(GenomicEventFormatter.format(evidence.genomicEvent()));
            }
        }
        return events;
    }

    @NotNull
    private static Set<String> applicableResistanceEvents(@NotNull List<TreatmentEvidence> evidences) {
        Multimap<String, String> treatmentsPerEvent = ArrayListMultimap.create();
        for (TreatmentEvidence evidence : evidences) {
            boolean isPotentiallyApplicable = isPotentiallyApplicable(evidence);
            boolean isResistanceEvidence = evidence.direction().isResistant();
            boolean hasOnLabelResponsiveEvidenceOfSameLevelOrHigher =
                    hasOnLabelResponsiveEvidenceWithMinLevel(evidences, evidence.treatment(), evidence.level());

            if (isPotentiallyApplicable && isResistanceEvidence && hasOnLabelResponsiveEvidenceOfSameLevelOrHigher) {
                treatmentsPerEvent.put(GenomicEventFormatter.format(evidence.genomicEvent()), evidence.treatment());
            }
        }

        Set<String> events = Sets.newHashSet();
        for (Map.Entry<String, Collection<String>> entry : treatmentsPerEvent.asMap().entrySet()) {
            StringJoiner joiner = new StringJoiner(", ");
            for (String treatment : entry.getValue()) {
                joiner.add(treatment);
            }
            events.add(entry.getKey() + " (" + joiner + ")");
        }
        return events;
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
        if ((evidence.level() == EvidenceLevel.C || evidence.level() == EvidenceLevel.D) && !evidence.onLabel()) {
            return false;
        }

        for (String nonApplicableStartKeyword : NON_APPLICABLE_START_KEYWORDS) {
            if (evidence.genomicEvent().startsWith(nonApplicableStartKeyword)) {
                return false;
            }
        }

        for (String nonApplicableEvent : NON_APPLICABLE_EVENTS) {
            if (evidence.genomicEvent().equals(nonApplicableEvent)) {
                return false;
            }
        }

        return true;
    }
}
