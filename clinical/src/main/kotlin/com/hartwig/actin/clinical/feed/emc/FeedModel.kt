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
        val patientEntries = feed.patientEntries.groupBy(FeedEntry::subject)
        val labEntries = feed.labEntries.groupBy(FeedEntry::subject)
        val medicationEntries = feed.medicationEntries.groupBy(FeedEntry::subject)
        val intoleranceEntries = feed.intoleranceEntries.groupBy(FeedEntry::subject)
        val bodyWeightEntries = feed.bodyWeightEntries.groupBy(FeedEntry::subject)
        val questionnaireEntries = feed.questionnaireEntries.groupBy(FeedEntry::subject)
        val digitalFileEntries = feed.digitalFileEntries.groupBy(FeedEntry::subject)
        val validationWarnings = feed.validationWarnings.groupBy(FeedValidationWarning::subject)
        val surgeryEntries = feed.surgeryEntries.groupBy(FeedEntry::subject)
        val vitalFunctionEntries = feed.vitalFunctionEntries.groupBy(FeedEntry::subject)
        return feed.patientEntries.map { patientEntry ->
            val patientId = patientEntry.subject
            FeedRecord(
                patientId,
                patientEntries[patientId].orEmpty(),
                labEntries[patientId].orEmpty(),
                medicationEntries[patientId].orEmpty(),
                intoleranceEntries[patientId].orEmpty(),
                bodyWeightEntries[patientId].orEmpty(),
                questionnaireEntries[patientId].orEmpty(),
                digitalFileEntries[patientId].orEmpty(),
                validationWarnings[patientId].orEmpty(),
                surgeryEntries[patientId].orEmpty(),
                vitalFunctionEntries[patientId].orEmpty()
            )
        }
    }

    class FeedRecord(
        patientId: String,
        patientEntries: List<PatientEntry>,
        internal val labEntries: List<LabEntry>,
        internal val medicationEntries: List<MedicationEntry>,
        internal val intoleranceEntries: List<IntoleranceEntry>,
        allBodyWeightEntries: List<BodyWeightEntry>,
        questionnaireEntries: List<QuestionnaireEntry>,
        digitalFileEntries: List<DigitalFileEntry>,
        allValidationWarnings: List<FeedValidationWarning>,
        allSurgeryEntries: List<SurgeryEntry>,
        allVitalFunctionEntries: List<VitalFunctionEntry>
    ) {
        val patientEntry: PatientEntry =
            patientEntries.firstOrNull() ?: throw IllegalStateException("Could not find patient for subject $patientId")
        val uniqueBodyWeightEntries = allBodyWeightEntries.distinct()
        val latestQuestionnaireEntry = questionnaireEntries.maxByOrNull(QuestionnaireEntry::authored)
        val toxicityEntries = digitalFileEntries.filter(DigitalFileEntry::isToxicityEntry)
        val bloodTransfusionEntries = digitalFileEntries.filter(DigitalFileEntry::isBloodTransfusionEntry)
        val validationWarnings = allValidationWarnings.filter { it.subject == patientId }.toSet()
        val uniqueSurgeryEntries = allSurgeryEntries.distinctBy { Pair(it.periodStart, it.periodEnd) }
        val uniqueVitalFunctionEntries = allVitalFunctionEntries.distinctBy {
            VitalFunctionProperties(
                it.effectiveDateTime,
                it.quantityValue ?: Double.NaN,
                it.quantityUnit,
                it.componentCodeDisplay,
                it.isValid()
            )
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