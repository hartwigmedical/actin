package com.hartwig.actin.clinical.feed

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
import java.time.LocalDateTime

class FeedModel(private val feed: ClinicalFeed) {
    fun subjects(): Set<String> {
        return feed.patientEntries.map { it.subject }.toSortedSet()
    }

    fun patientEntry(subject: String): PatientEntry {
        return entriesForSubject(feed.patientEntries, subject).firstOrNull()
            ?: throw IllegalStateException("Could not find patient for subject $subject")
    }

    fun bloodTransfusionEntries(subject: String): List<DigitalFileEntry> {
        return entriesForSubject(feed.digitalFileEntries, subject)
            .filter { obj: DigitalFileEntry -> obj.isBloodTransfusionEntry }
    }

    fun toxicityEntries(subject: String): List<DigitalFileEntry> {
        return entriesForSubject(feed.digitalFileEntries, subject)
            .filter { obj: DigitalFileEntry -> obj.isToxicityEntry }
    }

    fun latestQuestionnaireEntry(subject: String): QuestionnaireEntry? {
        return entriesForSubject(feed.questionnaireEntries, subject).maxByOrNull { obj: QuestionnaireEntry -> obj.authored }
    }

    fun uniqueSurgeryEntries(subject: String): List<SurgeryEntry> {
        return entriesForSubject(feed.surgeryEntries, subject).distinctBy { Pair(it.periodStart, it.periodEnd) }
    }

    fun medicationEntries(subject: String): List<MedicationEntry> {
        return entriesForSubject(feed.medicationEntries, subject)
    }

    fun labEntries(subject: String): List<LabEntry> {
        return entriesForSubject(feed.labEntries, subject)
    }

    fun vitalFunctionEntries(subject: String): List<VitalFunctionEntry> {
        return entriesForSubject(
            feed.vitalFunctionEntries,
            subject
        ).distinctBy {
            VitalFunctionProperties(
                it.effectiveDateTime,
                it.quantityValue,
                it.quantityUnit,
                it.componentCodeDisplay,
                it.valid
            )
        }
    }

    data class VitalFunctionProperties(
        val effectiveDateTime: LocalDateTime,
        val quantityValue: Double,
        val quantityUnit: String,
        val componentCodeDisplay: String,
        val valid: Boolean
    )

    fun intoleranceEntries(subject: String): List<IntoleranceEntry> {
        return entriesForSubject(feed.intoleranceEntries, subject)
    }

    fun uniqueBodyWeightEntries(subject: String): List<BodyWeightEntry> {
        return entriesForSubject(feed.bodyWeightEntries, subject).distinct()
    }

    companion object {
        @Throws(IOException::class)
        fun fromFeedDirectory(clinicalFeedDirectory: String): FeedModel {
            return FeedModel(ClinicalFeedReader.read(clinicalFeedDirectory))
        }

        private fun <T : FeedEntry> entriesForSubject(allEntries: List<T>, subject: String): List<T> {
            return allEntries.filter { entry: T -> entry.subject == subject }
        }
    }
}