package com.hartwig.actin.clinical.feed

import com.google.common.annotations.VisibleForTesting
import com.google.common.collect.Ordering
import com.google.common.collect.Sets
import com.hartwig.actin.clinical.feed.bodyweight.BodyWeightEntry
import com.hartwig.actin.clinical.feed.digitalfile.DigitalFileEntry
import com.hartwig.actin.clinical.feed.intolerance.IntoleranceEntry
import com.hartwig.actin.clinical.feed.lab.LabEntry
import com.hartwig.actin.clinical.feed.medication.MedicationEntry
import com.hartwig.actin.clinical.feed.patient.PatientEntry
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireEntry
import com.hartwig.actin.clinical.feed.surgery.SurgeryEntry
import com.hartwig.actin.clinical.feed.vitalfunction.VitalFunctionEntry
import java.io.IOException
import java.util.function.BinaryOperator
import java.util.function.Function
import java.util.stream.Collectors

class FeedModel @VisibleForTesting internal constructor(private val feed: ClinicalFeed) {
    fun subjects(): Set<String> {
        val subjects: MutableSet<String> = Sets.newTreeSet(Ordering.natural())
        for (entry in feed.patientEntries()) {
            subjects.add(entry!!.subject())
        }
        return subjects
    }

    fun patientEntry(subject: String): PatientEntry {
        return entriesForSubject(feed.patientEntries(), subject).stream()
            .findFirst()
            .orElseThrow { IllegalStateException("Could not find patient for subject $subject") }
    }

    fun bloodTransfusionEntries(subject: String): List<DigitalFileEntry?> {
        return entriesForSubject(feed.digitalFileEntries(), subject).stream()
            .filter { obj: DigitalFileEntry? -> obj!!.isBloodTransfusionEntry }
            .collect(Collectors.toList())
    }

    fun toxicityEntries(subject: String): List<DigitalFileEntry?> {
        return entriesForSubject(feed.digitalFileEntries(), subject).stream()
            .filter { obj: DigitalFileEntry? -> obj!!.isToxicityEntry }
            .collect(Collectors.toList())
    }

    fun latestQuestionnaireEntry(subject: String): QuestionnaireEntry? {
        return entriesForSubject(feed.questionnaireEntries(), subject).stream()
            .max(Comparator.comparing { obj: QuestionnaireEntry? -> obj!!.authored() })
            .orElse(null)
    }

    fun uniqueSurgeryEntries(subject: String): List<SurgeryEntry?> {
        return ArrayList(
            entriesForSubject(feed.surgeryEntries(), subject).stream()
                .collect(
                    Collectors.toMap(
                        Function { surgery: SurgeryEntry? ->
                            java.util.List.of(
                                surgery!!.periodStart(), surgery.periodEnd()
                            )
                        },
                        Function { surgery: SurgeryEntry? -> surgery },
                        BinaryOperator { surgery1: SurgeryEntry?, surgery2: SurgeryEntry? -> surgery1 })
                )
                .values
        )
    }

    fun medicationEntries(subject: String): List<MedicationEntry?> {
        return entriesForSubject(feed.medicationEntries(), subject)
    }

    fun labEntries(subject: String): List<LabEntry?> {
        return entriesForSubject(feed.labEntries(), subject)
    }

    fun vitalFunctionEntries(subject: String): List<VitalFunctionEntry?> {
        return entriesForSubject(feed.vitalFunctionEntries(), subject)
    }

    fun intoleranceEntries(subject: String): List<IntoleranceEntry?> {
        return entriesForSubject(feed.intoleranceEntries(), subject)
    }

    fun uniqueBodyWeightEntries(subject: String): List<BodyWeightEntry?> {
        return entriesForSubject(feed.bodyWeightEntries(), subject).stream().distinct().collect(Collectors.toList())
    }

    companion object {
        @JvmStatic
        @Throws(IOException::class)
        fun fromFeedDirectory(clinicalFeedDirectory: String): FeedModel {
            return FeedModel(ClinicalFeedReader.read(clinicalFeedDirectory))
        }

        private fun <T : FeedEntry?> entriesForSubject(allEntries: List<T>, subject: String): List<T> {
            return allEntries.stream().filter { entry: T -> entry!!.subject() == subject }.collect(Collectors.toList())
        }
    }
}