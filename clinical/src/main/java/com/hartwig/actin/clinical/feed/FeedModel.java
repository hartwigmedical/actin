package com.hartwig.actin.clinical.feed;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.hartwig.actin.clinical.feed.bodyweight.BodyWeightEntry;
import com.hartwig.actin.clinical.feed.digitalfile.DigitalFileEntry;
import com.hartwig.actin.clinical.feed.intolerance.IntoleranceEntry;
import com.hartwig.actin.clinical.feed.lab.LabEntry;
import com.hartwig.actin.clinical.feed.medication.MedicationEntry;
import com.hartwig.actin.clinical.feed.patient.PatientEntry;
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireEntry;
import com.hartwig.actin.clinical.feed.surgery.SurgeryEntry;
import com.hartwig.actin.clinical.feed.vitalfunction.VitalFunctionEntry;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FeedModel {

    @NotNull
    private final ClinicalFeed feed;

    @NotNull
    public static FeedModel fromFeedDirectory(@NotNull String clinicalFeedDirectory) throws IOException {
        return new FeedModel(ClinicalFeedReader.read(clinicalFeedDirectory));
    }

    @VisibleForTesting
    FeedModel(@NotNull final ClinicalFeed feed) {
        this.feed = feed;
    }

    @NotNull
    public Set<String> subjects() {
        Set<String> subjects = Sets.newTreeSet(Ordering.natural());
        for (PatientEntry entry : feed.patientEntries()) {
            subjects.add(entry.subject());
        }
        return subjects;
    }

    @NotNull
    public PatientEntry patientEntry(@NotNull String subject) {
        return entriesForSubject(feed.patientEntries(), subject).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not find patient for subject " + subject));
    }

    @NotNull
    public List<DigitalFileEntry> bloodTransfusionEntries(@NotNull String subject) {
        return entriesForSubject(feed.digitalFileEntries(), subject).stream()
                .filter(DigitalFileEntry::isBloodTransfusionEntry)
                .collect(Collectors.toList());
    }

    @NotNull
    public List<DigitalFileEntry> toxicityEntries(@NotNull String subject) {
        return entriesForSubject(feed.digitalFileEntries(), subject).stream()
                .filter(DigitalFileEntry::isToxicityEntry)
                .collect(Collectors.toList());
    }

    @Nullable
    public QuestionnaireEntry latestQuestionnaireEntry(@NotNull String subject) {
        return entriesForSubject(feed.questionnaireEntries(), subject).stream()
                .max(Comparator.comparing(QuestionnaireEntry::authored))
                .orElse(null);
    }

    @NotNull
    public List<SurgeryEntry> uniqueSurgeryEntries(@NotNull String subject) {
        return new ArrayList<>(entriesForSubject(feed.surgeryEntries(), subject).stream()
                .collect(Collectors.toMap(surgery -> List.of(surgery.periodStart(), surgery.periodEnd()),
                        surgery -> surgery,
                        (surgery1, surgery2) -> surgery1))
                .values());
    }

    @NotNull
    public List<MedicationEntry> medicationEntries(@NotNull String subject) {
        return entriesForSubject(feed.medicationEntries(), subject);
    }

    @NotNull
    public List<LabEntry> labEntries(@NotNull String subject) {
        return entriesForSubject(feed.labEntries(), subject);
    }

    @NotNull
    public List<VitalFunctionEntry> vitalFunctionEntries(@NotNull String subject) {
        return entriesForSubject(feed.vitalFunctionEntries(), subject);
    }

    @NotNull
    public List<IntoleranceEntry> intoleranceEntries(@NotNull String subject) {
        return entriesForSubject(feed.intoleranceEntries(), subject);
    }

    @NotNull
    public List<BodyWeightEntry> uniqueBodyWeightEntries(@NotNull String subject) {
        return entriesForSubject(feed.bodyWeightEntries(), subject).stream().distinct().collect(Collectors.toList());
    }

    @NotNull
    private static <T extends FeedEntry> List<T> entriesForSubject(@NotNull List<T> allEntries, @NotNull String subject) {
        return allEntries.stream().filter(entry -> entry.subject().equals(subject)).collect(Collectors.toList());
    }
}
