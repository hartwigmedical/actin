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

data class EmcClinicalFeed(
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
    operator fun plus(other: EmcClinicalFeed?): EmcClinicalFeed {
        return if (other == null) this else EmcClinicalFeed(
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