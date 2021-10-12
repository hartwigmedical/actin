package com.hartwig.actin.clinical.feed;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.clinical.feed.bloodpressure.BloodPressureEntry;
import com.hartwig.actin.clinical.feed.encounter.EncounterEntry;
import com.hartwig.actin.clinical.feed.intolerance.IntoleranceEntry;
import com.hartwig.actin.clinical.feed.lab.LabEntry;
import com.hartwig.actin.clinical.feed.medication.MedicationEntry;
import com.hartwig.actin.clinical.feed.patient.PatientEntry;
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireEntry;
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireExtraction;

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
        Set<String> subjects = Sets.newTreeSet(Comparator.naturalOrder());
        for (PatientEntry entry : feed.patientEntries()) {
            subjects.add(entry.subject());
        }
        return subjects;
    }

    @NotNull
    public PatientEntry patientEntry(@NotNull String subject) {
        for (PatientEntry entry : feed.patientEntries()) {
            if (entry.subject().equals(subject)) {
                return entry;
            }
        }

        throw new IllegalStateException("Could not find patient for subject " + subject);
    }

    @NotNull
    public List<QuestionnaireEntry> toxicityQuestionnaireEntries(@NotNull String subject) {
        List<QuestionnaireEntry> toxicities = Lists.newArrayList();
        for (QuestionnaireEntry entry : entriesForSubject(feed.questionnaireEntries(), subject)) {
            if (QuestionnaireExtraction.isToxicityEntry(entry)) {
                toxicities.add(entry);
            }
        }
        return toxicities;
    }

    @Nullable
    public QuestionnaireEntry latestQuestionnaireEntry(@NotNull String subject) {
        List<QuestionnaireEntry> questionnaires = entriesForSubject(feed.questionnaireEntries(), subject);
        QuestionnaireEntry latest = null;
        for (QuestionnaireEntry questionnaire : questionnaires) {
            if (QuestionnaireExtraction.isActualQuestionnaire(questionnaire) && (latest == null || questionnaire.authoredDateTime()
                    .isAfter(latest.authoredDateTime()))) {
                latest = questionnaire;
            }
        }
        return latest;
    }

    @NotNull
    public List<EncounterEntry> uniqueEncounterEntries(@NotNull String subject) {
        List<EncounterEntry> entries = Lists.newArrayList();
        for (EncounterEntry entry : entriesForSubject(feed.encounterEntries(), subject)) {
            if (isNewEncounter(entries, entry)) {
                entries.add(entry);
            }
        }
        return entries;
    }

    private static boolean isNewEncounter(@NotNull List<EncounterEntry> entries, @NotNull EncounterEntry entryToEvaluate) {
        for (EncounterEntry entry : entries) {
            if (entry.periodStart().equals(entryToEvaluate.periodStart()) && entry.periodEnd().equals(entry.periodEnd())) {
                return false;
            }
        }
        return true;
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
    public List<BloodPressureEntry> bloodPressureEntries(@NotNull String subject) {
        return entriesForSubject(feed.bloodPressureEntries(), subject);
    }

    @NotNull
    public List<IntoleranceEntry> intoleranceEntries(@NotNull String subject) {
        return entriesForSubject(feed.intoleranceEntries(), subject);
    }

    @NotNull
    private static <T extends FeedEntry> List<T> entriesForSubject(@NotNull List<T> allEntries, @NotNull String subject) {
        List<T> entries = Lists.newArrayList();
        for (T entry : allEntries) {
            if (entry.subject().equals(subject)) {
                entries.add(entry);
            }
        }
        return entries;
    }
}
