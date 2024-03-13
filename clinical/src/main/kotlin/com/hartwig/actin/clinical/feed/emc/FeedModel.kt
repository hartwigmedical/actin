package com.hartwig.actin.clinical.feed.emc

import com.hartwig.actin.clinical.feed.emc.bodyweight.BodyWeightEntry
import com.hartwig.actin.clinical.feed.emc.digitalfile.DigitalFileEntry
import com.hartwig.actin.clinical.feed.emc.intolerance.IntoleranceEntry
import com.hartwig.actin.clinical.feed.emc.lab.LabEntry
import com.hartwig.actin.clinical.feed.emc.medication.MedicationEntry
import com.hartwig.actin.clinical.feed.emc.patient.PatientEntry
import com.hartwig.actin.clinical.feed.emc.questionnaire.QuestionnaireEntry
import com.hartwig.actin.clinical.feed.emc.surgery.SurgeryEntry
import com.hartwig.actin.clinical.feed.emc.vitalfunction.VitalFunctionEntry
import java.io.IOException
import java.time.LocalDateTime

class FeedModel(private val feed: EmcClinicalFeed) {
    fun read(): List<FeedRecord> {
        return feed.patientEntries.map { patientEntry ->
            FeedRecord.fromFeed(feed, patientEntry.subject)
        }
    }

    class FeedRecord(
        private val patientEntry: PatientEntry,
        private val labEntries: List<LabEntry>,
        private val medicationEntries: List<MedicationEntry>,
        private val intoleranceEntries: List<IntoleranceEntry>,
        private val bodyWeightEntries: List<BodyWeightEntry>,
        private val questionnaireEntry: QuestionnaireEntry?,
        private val digitalFileEntries: List<DigitalFileEntry>,
        private val validationWarnings: Set<FeedValidationWarning>,
        private val uniqueSurgeryEntries: List<SurgeryEntry>,
        private val vitalFunctionEntries: List<VitalFunctionEntry>
    ) {
        fun patientEntry(): PatientEntry {
            return patientEntry
        }

        fun labEntries(): List<LabEntry> {
            return labEntries
        }

        fun medicationEntries(): List<MedicationEntry> {
            return medicationEntries
        }

        fun intoleranceEntries(): List<IntoleranceEntry> {
            return intoleranceEntries
        }

        fun uniqueBodyWeightEntries(): List<BodyWeightEntry> {
            return bodyWeightEntries
        }

        fun latestQuestionnaireEntry(): QuestionnaireEntry? {
            return questionnaireEntry
        }

        fun toxicityEntries(): List<DigitalFileEntry> {
            return digitalFileEntries.filter(DigitalFileEntry::isToxicityEntry)
        }

        fun bloodTransfusionEntries(): List<DigitalFileEntry> {
            return digitalFileEntries.filter(DigitalFileEntry::isBloodTransfusionEntry)
        }

        fun uniqueSurgeryEntries(): List<SurgeryEntry> {
            return uniqueSurgeryEntries
        }

        fun validationWarnings(): Set<FeedValidationWarning> {
            return validationWarnings
        }

        fun vitalFunctionEntries(): List<VitalFunctionEntry> {
            return vitalFunctionEntries
        }

        companion object {
            fun fromFeed(feed: EmcClinicalFeed, patientId: String): FeedRecord {
                return FeedRecord(
                    entriesForSubject(feed.patientEntries, patientId).firstOrNull()
                        ?: throw IllegalStateException("Could not find patient for subject $patientId"),
                    entriesForSubject(feed.labEntries, patientId),
                    entriesForSubject(feed.medicationEntries, patientId),
                    entriesForSubject(feed.intoleranceEntries, patientId),
                    entriesForSubject(feed.bodyWeightEntries, patientId).distinct(),
                    entriesForSubject(feed.questionnaireEntries, patientId).maxByOrNull(QuestionnaireEntry::authored),
                    entriesForSubject(feed.digitalFileEntries, patientId),
                    feed.validationWarnings.filter { it.subject == patientId }.toSet(),
                    entriesForSubject(feed.surgeryEntries, patientId).distinctBy { Pair(it.periodStart, it.periodEnd) },
                    entriesForSubject(feed.vitalFunctionEntries, patientId).distinctBy {
                        VitalFunctionProperties(
                            it.effectiveDateTime,
                            it.quantityValue ?: Double.NaN,
                            it.quantityUnit,
                            it.componentCodeDisplay,
                            it.isValid()
                        )
                    }
                )
            }

            private fun <T : FeedEntry> entriesForSubject(allEntries: List<T>, subject: String): List<T> {
                return allEntries.filter { entry: T -> entry.subject == subject }
            }
        }
    }

    private data class VitalFunctionProperties(
        val effectiveDateTime: LocalDateTime,
        val quantityValue: Double,
        val quantityUnit: String,
        val componentCodeDisplay: String,
        val valid: Boolean
    )

    companion object {
        @Throws(IOException::class)
        fun fromFeedDirectory(clinicalFeedDirectory: String): FeedModel {
            return FeedModel(ClinicalFeedReader.read(clinicalFeedDirectory))
        }
    }
}