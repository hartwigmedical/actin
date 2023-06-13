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
import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class ClinicalFeed {
    abstract fun patientEntries(): List<PatientEntry?>
    abstract fun digitalFileEntries(): List<DigitalFileEntry?>
    abstract fun questionnaireEntries(): List<QuestionnaireEntry?>
    abstract fun surgeryEntries(): List<SurgeryEntry?>
    abstract fun medicationEntries(): List<MedicationEntry?>
    abstract fun labEntries(): List<LabEntry?>
    abstract fun vitalFunctionEntries(): List<VitalFunctionEntry?>
    abstract fun intoleranceEntries(): List<IntoleranceEntry?>
    abstract fun bodyWeightEntries(): List<BodyWeightEntry?>
}