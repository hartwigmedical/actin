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
    val patientEntries: List<PatientEntry>,
    val digitalFileEntries: List<DigitalFileEntry>,
    val questionnaireEntries: List<QuestionnaireEntry>,
    val surgeryEntries: List<SurgeryEntry>,
    val medicationEntries: List<MedicationEntry>,
    val labEntries: List<LabEntry>,
    val vitalFunctionEntries: List<VitalFunctionEntry>,
    val intoleranceEntries: List<IntoleranceEntry>,
    val bodyWeightEntries: List<BodyWeightEntry>,
)