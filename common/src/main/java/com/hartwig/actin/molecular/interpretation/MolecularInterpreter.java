package com.hartwig.actin.molecular.interpretation;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.EvidenceLevel;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.MolecularTreatmentEvidence;
import com.hartwig.actin.molecular.util.GenomicEventFormatter;

import org.jetbrains.annotations.NotNull;

public final class MolecularInterpreter {

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
        return ImmutableMolecularInterpretation.builder()
                .applicableResponsiveEvents(applicableResponsiveEvents(record))
                .applicableResistanceEvents(applicableResistanceEvents(record))
                .build();
    }

    @NotNull
    private static Set<String> applicableResponsiveEvents(@NotNull MolecularRecord record) {
        Set<String> events = Sets.newTreeSet();
        for (MolecularTreatmentEvidence evidence : record.evidences()) {
            boolean isPotentiallyApplicable = isPotentiallyApplicable(evidence);
            boolean isResponsiveEvidence = evidence.direction().isResponsive();

            if (isPotentiallyApplicable && isResponsiveEvidence) {
                events.add(GenomicEventFormatter.format(evidence.genomicEvent()));
            }
        }
        return events;
    }

    @NotNull
    private static Set<String> applicableResistanceEvents(@NotNull MolecularRecord record) {
        Multimap<String, String> treatmentsPerEvent = ArrayListMultimap.create();
        for (MolecularTreatmentEvidence evidence : record.evidences()) {
            boolean isPotentiallyApplicable = isPotentiallyApplicable(evidence);
            boolean isResistanceEvidence = evidence.direction().isResistant();
            boolean hasOnLabelResponsiveEvidenceOfSameLevelOrHigher =
                    hasOnLabelResponsiveEvidenceWithMinLevel(record.evidences(), evidence.treatment(), evidence.level());

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

    private static boolean hasOnLabelResponsiveEvidenceWithMinLevel(@NotNull List<MolecularTreatmentEvidence> evidences,
            @NotNull String treatment, @NotNull EvidenceLevel minLevel) {
        for (MolecularTreatmentEvidence evidence : evidences) {
            if (evidence.direction().isResponsive() && evidence.treatment().equals(treatment) && evidence.onLabel()
                    && minLevel.isBetterOrEqual(evidence.level())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isPotentiallyApplicable(@NotNull MolecularTreatmentEvidence evidence) {
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
