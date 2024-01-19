package com.hartwig.actin.clinical.feed

import com.hartwig.actin.clinical.feed.bodyweight.BodyWeightEntry
import com.hartwig.actin.clinical.feed.bodyweight.BodyWeightEntryValidator
import com.hartwig.actin.clinical.feed.digitalfile.DigitalFileEntry
import com.hartwig.actin.clinical.feed.intolerance.IntoleranceEntry
import com.hartwig.actin.clinical.feed.lab.LabEntry
import com.hartwig.actin.clinical.feed.medication.MedicationEntry
import com.hartwig.actin.clinical.feed.patient.PatientEntry
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireEntry
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireEntryFeedValidator
import com.hartwig.actin.clinical.feed.surgery.SurgeryEntry
import com.hartwig.actin.clinical.feed.surgery.SurgeryEntryFeedValidator
import com.hartwig.actin.clinical.feed.vitalfunction.VitalFunctionEntry
import com.hartwig.actin.clinical.feed.vitalfunction.VitalFunctionFeedValidator

object FeedFileReaderFactory {
    fun createPatientReader(): FeedFileReader<PatientEntry> {
        return FeedFileReader(PatientEntry::class.java) { ClinicalFeed(patientEntries = entries(it)) }
    }

    fun createQuestionnaireReader(): FeedFileReader<QuestionnaireEntry> {
        return FeedFileReader(
            QuestionnaireEntry::class.java,
            QuestionnaireEntryFeedValidator()
        ) { ClinicalFeed(questionnaireEntries = entries(it)) }
    }

    fun createDigitalFileReader(): FeedFileReader<DigitalFileEntry> {
        return FeedFileReader(DigitalFileEntry::class.java) { ClinicalFeed(digitalFileEntries = entries(it)) }
    }

    fun createSurgeryReader(): FeedFileReader<SurgeryEntry> {
        return FeedFileReader(SurgeryEntry::class.java, SurgeryEntryFeedValidator()) { ClinicalFeed(surgeryEntries = entries(it)) }
    }

    fun createMedicationReader(): FeedFileReader<MedicationEntry> {
        return FeedFileReader(MedicationEntry::class.java) { ClinicalFeed(medicationEntries = entries(it)) }
    }

    fun createLabReader(): FeedFileReader<LabEntry> {
        return FeedFileReader(LabEntry::class.java) { ClinicalFeed(labEntries = entries(it)) }
    }

    fun createVitalFunctionReader(): FeedFileReader<VitalFunctionEntry> {
        return FeedFileReader(
            VitalFunctionEntry::class.java,
            VitalFunctionFeedValidator()
        ) { ClinicalFeed(vitalFunctionEntries = entries(it)) }
    }

    fun createIntoleranceReader(): FeedFileReader<IntoleranceEntry> {
        return FeedFileReader(IntoleranceEntry::class.java) { ClinicalFeed(intoleranceEntries = entries(it)) }
    }

    fun createBodyWeightReader(): FeedFileReader<BodyWeightEntry> {
        return FeedFileReader(BodyWeightEntry::class.java, BodyWeightEntryValidator()) { ClinicalFeed(bodyWeightEntries = entries(it)) }
    }

    private fun <T : FeedEntry> entries(it: List<FeedResult<T>>) =
        it.map { p -> p.entry }
}