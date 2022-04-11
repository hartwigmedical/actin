package com.hartwig.actin.report.interpretation;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.EvidenceAnalysis;
import com.hartwig.actin.molecular.datamodel.EvidenceEntry;

import org.jetbrains.annotations.NotNull;

public final class EvidenceInterpreter {

    private EvidenceInterpreter() {
    }

    @NotNull
    public static Set<String> eventsWithApprovedEvidence(@NotNull EvidenceAnalysis evidence) {
        return events(evidence.approvedResponsiveEvidence());
    }

    @NotNull
    public static Set<String> eventsWithActinEvidence(@NotNull EvidenceAnalysis evidence) {
        return events(evidence.actinTrials());
    }

    @NotNull
    public static Set<EvidenceEntry> additionalEvidenceForExternalTrials(@NotNull EvidenceAnalysis evidence) {
        Set<String> eventsToFilter = Sets.newHashSet();
        eventsToFilter.addAll(eventsWithApprovedEvidence(evidence));
        eventsToFilter.addAll(eventsWithActinEvidence(evidence));

        return filter(evidence.externalTrials(), eventsToFilter);
    }

    @NotNull
    public static Set<String> additionalEventsWithExternalTrialEvidence(@NotNull EvidenceAnalysis evidence) {
        return events(additionalEvidenceForExternalTrials(evidence));
    }

    @NotNull
    public static Set<String> additionalEventsWithExperimentalEvidence(@NotNull EvidenceAnalysis evidence) {
        Set<String> eventsToFilter = Sets.newHashSet();
        eventsToFilter.addAll(eventsWithApprovedEvidence(evidence));
        eventsToFilter.addAll(eventsWithActinEvidence(evidence));

        return events(filter(evidence.experimentalResponsiveEvidence(), eventsToFilter));
    }

    @NotNull
    public static Set<String> additionalEventsWithOtherEvidence(@NotNull EvidenceAnalysis evidence) {
        Set<String> eventsToFilter = Sets.newHashSet();
        eventsToFilter.addAll(eventsWithApprovedEvidence(evidence));
        eventsToFilter.addAll(eventsWithActinEvidence(evidence));
        eventsToFilter.addAll(additionalEventsWithExperimentalEvidence(evidence));

        return events(filter(evidence.otherResponsiveEvidence(), eventsToFilter));
    }

    @NotNull
    private static Set<String> events(@NotNull Iterable<EvidenceEntry> evidences) {
        Set<String> events = Sets.newTreeSet();
        for (EvidenceEntry evidence : evidences) {
            events.add(evidence.event());
        }
        return events;
    }

    @NotNull
    private static Set<EvidenceEntry> filter(@NotNull Set<EvidenceEntry> evidences, @NotNull Set<String> eventsToFilter) {
        Set<EvidenceEntry> filtered = Sets.newHashSet();
        for (EvidenceEntry evidence : evidences) {
            if (!eventsToFilter.contains(evidence.event())) {
                filtered.add(evidence);
            }
        }
        return filtered;
    }
}
