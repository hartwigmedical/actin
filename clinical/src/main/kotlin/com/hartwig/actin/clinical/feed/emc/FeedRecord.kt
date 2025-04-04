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
import com.hartwig.actin.datamodel.clinical.ingestion.FeedValidationWarning
import java.time.LocalDateTime

data class FeedRecord(
    val patientEntry: PatientEntry,
    val labEntries: List<LabEntry>,
    val medicationEntries: List<MedicationEntry>,
    val intoleranceEntries: List<IntoleranceEntry>,
    val uniqueBodyWeightEntries: List<BodyWeightEntry>,
    val questionnaireEntries: List<QuestionnaireEntry>,
    val toxicityEntries: List<DigitalFileEntry>,
    val bloodTransfusionEntries: List<DigitalFileEntry>,
    val validationWarnings: Set<FeedValidationWarning>,
    val uniqueSurgeryEntries: List<SurgeryEntry>,
    val uniqueVitalFunctionEntries: List<VitalFunctionEntry>
) {

    companion object {
        fun create(
            patientId: String,
            patientEntries: List<PatientEntry>,
            labEntries: List<LabEntry>,
            medicationEntries: List<MedicationEntry>,
            intoleranceEntries: List<IntoleranceEntry>,
            allBodyWeightEntries: List<BodyWeightEntry>,
            questionnaireEntries: List<QuestionnaireEntry>,
            digitalFileEntries: List<DigitalFileEntry>,
            allValidationWarnings: List<FeedValidationWarning>,
            allSurgeryEntries: List<SurgeryEntry>,
            allVitalFunctionEntries: List<VitalFunctionEntry>
        ): FeedRecord {
            val patientEntry: PatientEntry =
                patientEntries.firstOrNull() ?: throw IllegalStateException("Could not find patient for subject $patientId")
            return FeedRecord(patientEntry,
                labEntries,
                medicationEntries,
                intoleranceEntries,
                allBodyWeightEntries.distinct(),
                questionnaireEntries,
                digitalFileEntries.filter(DigitalFileEntry::isToxicityEntry),
                digitalFileEntries.filter(DigitalFileEntry::isBloodTransfusionEntry),
                allValidationWarnings.filter { it.subject == patientId }.toSet(),
                allSurgeryEntries.distinctBy { Pair(it.periodStart, it.periodEnd) },
                allVitalFunctionEntries.distinctBy {
                    VitalFunctionProperties(
                        it.effectiveDateTime,
                        it.quantityValue ?: Double.NaN,
                        it.quantityUnit,
                        it.componentCodeDisplay,
                        it.isValid()
                    )
                })
        }
    }

    private data class VitalFunctionProperties(
        val effectiveDateTime: LocalDateTime,
        val quantityValue: Double,
        val quantityUnit: String,
        val componentCodeDisplay: String,
        val valid: Boolean
    )
}