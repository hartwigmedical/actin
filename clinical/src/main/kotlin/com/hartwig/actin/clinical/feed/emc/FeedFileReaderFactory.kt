package com.hartwig.actin.clinical.feed.emc

import com.hartwig.actin.clinical.feed.emc.bodyweight.BodyWeightEntry
import com.hartwig.actin.clinical.feed.emc.bodyweight.BodyWeightEntryValidator
import com.hartwig.actin.clinical.feed.emc.digitalfile.DigitalFileEntry
import com.hartwig.actin.clinical.feed.emc.intolerance.IntoleranceEntry
import com.hartwig.actin.clinical.feed.emc.lab.LabEntry
import com.hartwig.actin.clinical.feed.emc.medication.MedicationEntry
import com.hartwig.actin.clinical.feed.emc.patient.PatientEntry
import com.hartwig.actin.clinical.feed.emc.questionnaire.QuestionnaireEntry
import com.hartwig.actin.clinical.feed.emc.questionnaire.QuestionnaireEntryFeedValidator
import com.hartwig.actin.clinical.feed.emc.surgery.SurgeryEntry
import com.hartwig.actin.clinical.feed.emc.surgery.SurgeryEntryFeedValidator
import com.hartwig.actin.clinical.feed.emc.vitalfunction.VitalFunctionEntry
import com.hartwig.actin.clinical.feed.emc.vitalfunction.VitalFunctionFeedValidator

object FeedFileReaderFactory {
    fun createPatientReader(): FeedFileReader<PatientEntry> {
        return FeedFileReader(PatientEntry::class.java) { EmcClinicalFeed(patientEntries = entries(it)) }
    }

    fun createQuestionnaireReader(): FeedFileReader<QuestionnaireEntry> {
        return FeedFileReader(
            QuestionnaireEntry::class.java,
            QuestionnaireEntryFeedValidator()
        ) { EmcClinicalFeed(questionnaireEntries = entries(it)) }
    }

    fun createDigitalFileReader(): FeedFileReader<DigitalFileEntry> {
        return FeedFileReader(DigitalFileEntry::class.java) { EmcClinicalFeed(digitalFileEntries = entries(it)) }
    }

    fun createSurgeryReader(): FeedFileReader<SurgeryEntry> {
        return FeedFileReader(SurgeryEntry::class.java, SurgeryEntryFeedValidator()) { EmcClinicalFeed(surgeryEntries = entries(it)) }
    }

    fun createMedicationReader(): FeedFileReader<MedicationEntry> {
        return FeedFileReader(MedicationEntry::class.java) { EmcClinicalFeed(medicationEntries = entries(it)) }
    }

    fun createLabReader(): FeedFileReader<LabEntry> {
        return FeedFileReader(LabEntry::class.java) { EmcClinicalFeed(labEntries = entries(it)) }
    }

    fun createVitalFunctionReader(): FeedFileReader<VitalFunctionEntry> {
        return FeedFileReader(
            VitalFunctionEntry::class.java,
            VitalFunctionFeedValidator()
        ) { EmcClinicalFeed(vitalFunctionEntries = entries(it)) }
    }

    fun createIntoleranceReader(): FeedFileReader<IntoleranceEntry> {
        return FeedFileReader(IntoleranceEntry::class.java) { EmcClinicalFeed(intoleranceEntries = entries(it)) }
    }

    fun createBodyWeightReader(): FeedFileReader<BodyWeightEntry> {
        return FeedFileReader(BodyWeightEntry::class.java, BodyWeightEntryValidator()) { EmcClinicalFeed(bodyWeightEntries = entries(it)) }
    }

    private fun <T : FeedEntry> entries(it: List<FeedResult<T>>) =
        it.map { p -> p.entry }
}