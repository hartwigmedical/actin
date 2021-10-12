package com.hartwig.actin.clinical.feed;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.clinical.feed.patient.PatientEntry;

import org.jetbrains.annotations.NotNull;

final class ClinicalFeedValidation {

    private ClinicalFeedValidation() {
    }

    public static void validate(@NotNull ClinicalFeed feed) {
        enforceUniquePatients(feed.patientEntries());
    }

    private static void enforceUniquePatients(@NotNull List<PatientEntry> patients) {
        Set<String> subjects = Sets.newHashSet();
        for (PatientEntry patient : patients) {
            String subject = patient.subject();
            if (subjects.contains(subject)) {
                throw new IllegalStateException("Duplicate subject found in clinical feed patient entries: " + subject);
            }
            subjects.add(subject);
        }
    }
}
