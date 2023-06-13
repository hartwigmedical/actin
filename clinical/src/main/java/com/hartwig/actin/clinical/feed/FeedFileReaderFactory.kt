package com.hartwig.actin.clinical.feed

import com.hartwig.actin.clinical.feed.bodyweight.BodyWeightEntry
import com.hartwig.actin.clinical.feed.bodyweight.BodyWeightEntryCreator
import com.hartwig.actin.clinical.feed.digitalfile.DigitalFileEntry
import com.hartwig.actin.clinical.feed.digitalfile.DigitalFileEntryCreator
import com.hartwig.actin.clinical.feed.intolerance.IntoleranceEntry
import com.hartwig.actin.clinical.feed.intolerance.IntoleranceEntryCreator
import com.hartwig.actin.clinical.feed.lab.LabEntry
import com.hartwig.actin.clinical.feed.lab.LabEntryCreator
import com.hartwig.actin.clinical.feed.medication.MedicationEntry
import com.hartwig.actin.clinical.feed.medication.MedicationEntryCreator
import com.hartwig.actin.clinical.feed.patient.PatientEntry
import com.hartwig.actin.clinical.feed.patient.PatientEntryCreator
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireEntry
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireEntryCreator
import com.hartwig.actin.clinical.feed.surgery.SurgeryEntry
import com.hartwig.actin.clinical.feed.surgery.SurgeryEntryCreator
import com.hartwig.actin.clinical.feed.vitalfunction.VitalFunctionEntry
import com.hartwig.actin.clinical.feed.vitalfunction.VitalFunctionEntryCreator

object FeedFileReaderFactory {
    fun createPatientReader(): FeedFileReader<PatientEntry> {
        return FeedFileReader.Companion.create<PatientEntry>(PatientEntryCreator())
    }

    fun createQuestionnaireReader(): FeedFileReader<QuestionnaireEntry> {
        return FeedFileReader(QuestionnaireEntryCreator(), true)
    }

    fun createDigitalFileReader(): FeedFileReader<DigitalFileEntry> {
        return FeedFileReader.Companion.create<DigitalFileEntry>(DigitalFileEntryCreator())
    }

    fun createSurgeryReader(): FeedFileReader<SurgeryEntry> {
        return FeedFileReader.Companion.create<SurgeryEntry>(SurgeryEntryCreator())
    }

    fun createMedicationReader(): FeedFileReader<MedicationEntry> {
        return FeedFileReader.Companion.create<MedicationEntry>(MedicationEntryCreator())
    }

    fun createLabReader(): FeedFileReader<LabEntry> {
        return FeedFileReader.Companion.create<LabEntry>(LabEntryCreator())
    }

    fun createVitalFunctionReader(): FeedFileReader<VitalFunctionEntry> {
        return FeedFileReader.Companion.create<VitalFunctionEntry>(VitalFunctionEntryCreator())
    }

    fun createIntoleranceReader(): FeedFileReader<IntoleranceEntry> {
        return FeedFileReader.Companion.create<IntoleranceEntry>(IntoleranceEntryCreator())
    }

    fun createBodyWeightReader(): FeedFileReader<BodyWeightEntry> {
        return FeedFileReader.Companion.create<BodyWeightEntry>(BodyWeightEntryCreator())
    }
}