package com.hartwig.actin.report.interpretation;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.MolecularEvidence;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;

import org.jetbrains.annotations.NotNull;

public final class EvidenceInterpreter {

    private EvidenceInterpreter() {
    }

    @NotNull
    public static Set<String> eventsWithApprovedEvidence(@NotNull MolecularRecord molecular) {
        return events(molecular.approvedResponsiveEvidence());
    }

    @NotNull
    public static Set<String> eventsWithActinEvidence(@NotNull MolecularRecord molecular) {
        return events(molecular.actinTrials());
    }

    @NotNull
    public static Set<MolecularEvidence> additionalEvidenceForExternalTrials(@NotNull MolecularRecord molecular) {
        Set<String> eventsToFilter = Sets.newHashSet();
        eventsToFilter.addAll(eventsWithApprovedEvidence(molecular));
        eventsToFilter.addAll(eventsWithActinEvidence(molecular));

        return filter(molecular.externalTrials(), eventsToFilter);
    }

    @NotNull
    public static Set<String> additionalEventsWithExternalTrialEvidence(@NotNull MolecularRecord molecular) {
        return events(additionalEvidenceForExternalTrials(molecular));
    }

    @NotNull
    public static Set<String> additionalEventsWithExperimentalEvidence(@NotNull MolecularRecord molecular) {
        Set<String> eventsToFilter = Sets.newHashSet();
        eventsToFilter.addAll(eventsWithApprovedEvidence(molecular));
        eventsToFilter.addAll(eventsWithActinEvidence(molecular));

        return events(filter(molecular.experimentalResponsiveEvidence(), eventsToFilter));
    }

    @NotNull
    public static Set<String> additionalEventsWithOtherEvidence(@NotNull MolecularRecord molecular) {
        Set<String> eventsToFilter = Sets.newHashSet();
        eventsToFilter.addAll(eventsWithApprovedEvidence(molecular));
        eventsToFilter.addAll(eventsWithActinEvidence(molecular));
        eventsToFilter.addAll(additionalEventsWithExperimentalEvidence(molecular));

        return events(filter(molecular.otherResponsiveEvidence(), eventsToFilter));
    }

    @NotNull
    private static Set<String> events(@NotNull Iterable<MolecularEvidence> evidences) {
        Set<String> events = Sets.newTreeSet();
        for (MolecularEvidence evidence : evidences) {
            events.add(evidence.event());
        }
        return events;
    }

    @NotNull
    private static Set<MolecularEvidence> filter(@NotNull Set<MolecularEvidence> evidences, @NotNull Set<String> eventsToFilter) {
        Set<MolecularEvidence> filtered = Sets.newHashSet();
        for (MolecularEvidence evidence : evidences) {
            if (!eventsToFilter.contains(evidence.event())) {
                filtered.add(evidence);
            }
        }
        return filtered;
    }
}
