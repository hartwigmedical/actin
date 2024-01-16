package com.hartwig.actin.clinical.feed

import com.hartwig.actin.clinical.feed.bodyweight.BodyWeightEntry
import com.hartwig.actin.clinical.feed.bodyweight.BodyWeightEntryValidator
import com.hartwig.actin.clinical.feed.digitalfile.DigitalFileEntry
import com.hartwig.actin.clinical.feed.intolerance.IntoleranceEntry
import com.hartwig.actin.clinical.feed.lab.LabEntry
import com.hartwig.actin.clinical.feed.medication.MedicationEntry
import com.hartwig.actin.clinical.feed.patient.PatientEntry
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireEntry
import com.hartwig.actin.clinical.feed.surgery.SurgeryEntry
import com.hartwig.actin.clinical.feed.surgery.SurgeryEntryFeedValidator
import com.hartwig.actin.clinical.feed.vitalfunction.VitalFunctionEntry
import com.hartwig.actin.clinical.feed.vitalfunction.VitalFunctionFeedValidator

object FeedFileReaderFactory {
    fun createPatientReader(): FeedFileReader<PatientEntry> {
        return FeedFileReader(PatientEntry::class.java)
    }

    fun createQuestionnaireReader(): FeedFileReader<QuestionnaireEntry> {
        return FeedFileReader(QuestionnaireEntry::class.java)
    }

    fun createDigitalFileReader(): FeedFileReader<DigitalFileEntry> {
        return FeedFileReader(DigitalFileEntry::class.java)
    }

    fun createSurgeryReader(): FeedFileReader<SurgeryEntry> {
        return FeedFileReader(SurgeryEntry::class.java, SurgeryEntryFeedValidator())
    }

    fun createMedicationReader(): FeedFileReader<MedicationEntry> {
        return FeedFileReader(MedicationEntry::class.java)
    }

    fun createLabReader(): FeedFileReader<LabEntry> {
        return FeedFileReader(LabEntry::class.java)
    }

    fun createVitalFunctionReader(): FeedFileReader<VitalFunctionEntry> {
        return FeedFileReader(VitalFunctionEntry::class.java, VitalFunctionFeedValidator())
    }

    fun createIntoleranceReader(): FeedFileReader<IntoleranceEntry> {
        return FeedFileReader(IntoleranceEntry::class.java)
    }

    fun createBodyWeightReader(): FeedFileReader<BodyWeightEntry> {
        return FeedFileReader(BodyWeightEntry::class.java, BodyWeightEntryValidator())
    }
}