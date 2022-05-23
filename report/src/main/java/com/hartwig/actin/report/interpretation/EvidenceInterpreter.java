package com.hartwig.actin.report.interpretation;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.evidence.ActinTrialEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.EvidenceEntry;
import com.hartwig.actin.molecular.datamodel.evidence.MolecularEvidence;

import org.jetbrains.annotations.NotNull;

public final class EvidenceInterpreter {

    private EvidenceInterpreter() {
    }

    @NotNull
    public static Set<String> eventsWithApprovedEvidence(@NotNull MolecularEvidence evidence) {
        return events(evidence.approvedEvidence());
    }

    @NotNull
    public static Set<String> eventsWithInclusiveActinEvidence(@NotNull MolecularEvidence evidence) {
        Set<ActinTrialEvidence> inclusive = Sets.newHashSet();
        for (ActinTrialEvidence actinTrial : evidence.actinTrials()) {
            if (actinTrial.isInclusionCriterion()) {
                inclusive.add(actinTrial);
            }
        }
        return events(inclusive);
    }

    @NotNull
    public static Set<String> additionalEventsWithExternalTrialEvidence(@NotNull MolecularEvidence evidence) {
        Set<String> eventsToFilter = Sets.newHashSet();
        eventsToFilter.addAll(eventsWithApprovedEvidence(evidence));
        eventsToFilter.addAll(eventsWithInclusiveActinEvidence(evidence));

        return events(filter(evidence.externalTrials(), eventsToFilter));
    }

    @NotNull
    public static Set<String> additionalEventsWithOnLabelExperimentalEvidence(@NotNull MolecularEvidence evidence) {
        Set<String> eventsToFilter = Sets.newHashSet();
        eventsToFilter.addAll(eventsWithApprovedEvidence(evidence));
        eventsToFilter.addAll(eventsWithInclusiveActinEvidence(evidence));

        return events(filter(evidence.onLabelExperimentalEvidence(), eventsToFilter));
    }

    @NotNull
    public static Set<String> additionalEventsWithOffLabelExperimentalEvidence(@NotNull MolecularEvidence evidence) {
        Set<String> eventsToFilter = Sets.newHashSet();
        eventsToFilter.addAll(eventsWithApprovedEvidence(evidence));
        eventsToFilter.addAll(eventsWithInclusiveActinEvidence(evidence));
        eventsToFilter.addAll(additionalEventsWithOnLabelExperimentalEvidence(evidence));

        return events(filter(evidence.offLabelExperimentalEvidence(), eventsToFilter));
    }

    @NotNull
    private static Set<String> events(@NotNull Iterable<? extends EvidenceEntry> evidences) {
        Set<String> events = Sets.newTreeSet();
        for (EvidenceEntry evidence : evidences) {
            events.add(evidence.event());
        }
        return events;
    }

    @NotNull
    private static <X extends EvidenceEntry> Set<X> filter(@NotNull Set<X> evidences, @NotNull Set<String> eventsToFilter) {
        Set<X> filtered = Sets.newHashSet();
        for (X evidence : evidences) {
            if (!eventsToFilter.contains(evidence.event())) {
                filtered.add(evidence);
            }
        }
        return filtered;
    }
}
