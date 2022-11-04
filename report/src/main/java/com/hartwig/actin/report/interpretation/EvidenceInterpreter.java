package com.hartwig.actin.report.interpretation;

import java.util.Set;

import com.google.common.collect.Sets;

import org.jetbrains.annotations.NotNull;

public final class EvidenceInterpreter {

    private EvidenceInterpreter() {
    }

    @NotNull
    public static Set<String> eventsWithApprovedEvidence(@NotNull AggregatedEvidence evidence) {
        return evidence.approvedTreatmentsPerEvent().keySet();
    }

    @NotNull
    public static Set<String> additionalEventsWithExternalTrialEvidence(@NotNull AggregatedEvidence evidence) {
        Set<String> eventsToFilter = Sets.newHashSet();
        eventsToFilter.addAll(eventsWithApprovedEvidence(evidence));
        // TODO Add ACTIN events for filtering

        Set<String> events = evidence.externalEligibleTrialsPerEvent().keySet();
        return filter(events, eventsToFilter);
    }

    @NotNull
    public static Set<String> additionalEventsWithOnLabelExperimentalEvidence(@NotNull AggregatedEvidence evidence) {
        Set<String> eventsToFilter = Sets.newHashSet();
        eventsToFilter.addAll(eventsWithApprovedEvidence(evidence));
        // TODO Add ACTIN events for filtering

        Set<String> events = evidence.onLabelExperimentalTreatmentsPerEvent().keySet();
        return filter(events, eventsToFilter);
    }

    @NotNull
    public static Set<String> additionalEventsWithOffLabelExperimentalEvidence(@NotNull AggregatedEvidence evidence) {
        Set<String> eventsToFilter = Sets.newHashSet();
        eventsToFilter.addAll(eventsWithApprovedEvidence(evidence));
        eventsToFilter.addAll(additionalEventsWithOnLabelExperimentalEvidence(evidence));
        // TODO Add ACTIN events for filtering

        Set<String> events = evidence.offLabelExperimentalTreatmentsPerEvent().keySet();
        return filter(events, eventsToFilter);
    }

    @NotNull
    private static Set<String> filter(@NotNull Set<String> events, @NotNull Set<String> eventsToFilter) {
        Set<String> filtered = Sets.newHashSet();
        for (String event : events) {
            if (!eventsToFilter.contains(event)) {
                filtered.add(event);
            }
        }
        return filtered;
    }
}
