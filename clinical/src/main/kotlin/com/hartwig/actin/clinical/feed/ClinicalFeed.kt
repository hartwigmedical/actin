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

data class ClinicalFeed(
    val validationWarnings: List<FeedValidationWarning> = emptyList(),
    val patientEntries: List<PatientEntry> = emptyList(),
    val digitalFileEntries: List<DigitalFileEntry> = emptyList(),
    val questionnaireEntries: List<QuestionnaireEntry> = emptyList(),
    val surgeryEntries: List<SurgeryEntry> = emptyList(),
    val medicationEntries: List<MedicationEntry> = emptyList(),
    val labEntries: List<LabEntry> = emptyList(),
    val vitalFunctionEntries: List<VitalFunctionEntry> = emptyList(),
    val intoleranceEntries: List<IntoleranceEntry> = emptyList(),
    val bodyWeightEntries: List<BodyWeightEntry> = emptyList(),
) {
    operator fun plus(other: ClinicalFeed?): ClinicalFeed {
        return if (other == null) this else ClinicalFeed(
            validationWarnings = validationWarnings + other.validationWarnings,
            patientEntries = patientEntries + other.patientEntries,
            digitalFileEntries = digitalFileEntries + other.digitalFileEntries,
            questionnaireEntries = questionnaireEntries + other.questionnaireEntries,
            surgeryEntries = surgeryEntries + other.surgeryEntries,
            medicationEntries = medicationEntries + other.medicationEntries,
            labEntries = labEntries + other.labEntries,
            vitalFunctionEntries = vitalFunctionEntries + other.vitalFunctionEntries,
            intoleranceEntries = intoleranceEntries + other.intoleranceEntries,
            bodyWeightEntries = bodyWeightEntries + other.bodyWeightEntries
        )
    }
}