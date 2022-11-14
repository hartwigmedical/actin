package com.hartwig.actin.report.interpretation;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.interpretation.AggregatedEvidence;

import org.jetbrains.annotations.NotNull;

public class EvidenceInterpreter {

    @NotNull
    private final Set<String> actinInclusionEvents;

    @NotNull
    public static EvidenceInterpreter fromEvaluatedTrials(@NotNull List<EvaluatedTrial> trials) {
        Set<String> actinInclusionEvents = Sets.newHashSet();
        for (EvaluatedTrial trial : trials) {
            actinInclusionEvents.addAll(trial.molecularEvents());
        }
        return new EvidenceInterpreter(actinInclusionEvents);
    }

    private EvidenceInterpreter(@NotNull final Set<String> actinInclusionEvents) {
        this.actinInclusionEvents = actinInclusionEvents;
    }

    @NotNull
    public Set<String> eventsWithApprovedEvidence(@NotNull AggregatedEvidence evidence) {
        return evidence.approvedTreatmentsPerEvent().keySet();
    }

    @NotNull
    public Set<String> additionalEventsWithExternalTrialEvidence(@NotNull AggregatedEvidence evidence) {
        Set<String> eventsToFilter = Sets.newHashSet();
        eventsToFilter.addAll(eventsWithApprovedEvidence(evidence));
        eventsToFilter.addAll(actinInclusionEvents);

        Set<String> events = evidence.externalEligibleTrialsPerEvent().keySet();
        return filter(events, eventsToFilter);
    }

    @NotNull
    public Set<String> additionalEventsWithOnLabelExperimentalEvidence(@NotNull AggregatedEvidence evidence) {
        Set<String> eventsToFilter = Sets.newHashSet();
        eventsToFilter.addAll(eventsWithApprovedEvidence(evidence));
        eventsToFilter.addAll(actinInclusionEvents);

        Set<String> events = evidence.onLabelExperimentalTreatmentsPerEvent().keySet();
        return filter(events, eventsToFilter);
    }

    @NotNull
    public Set<String> additionalEventsWithOffLabelExperimentalEvidence(@NotNull AggregatedEvidence evidence) {
        Set<String> eventsToFilter = Sets.newHashSet();
        eventsToFilter.addAll(eventsWithApprovedEvidence(evidence));
        eventsToFilter.addAll(actinInclusionEvents);
        eventsToFilter.addAll(additionalEventsWithOnLabelExperimentalEvidence(evidence));

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
