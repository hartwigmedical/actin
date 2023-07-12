package com.hartwig.actin.clinical.feed

import com.hartwig.actin.clinical.AtcModel
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
        return FeedFileReader.create(PatientEntryCreator())
    }

    fun createQuestionnaireReader(): FeedFileReader<QuestionnaireEntry> {
        return FeedFileReader(QuestionnaireEntryCreator(), true)
    }

    fun createDigitalFileReader(): FeedFileReader<DigitalFileEntry> {
        return FeedFileReader.create(DigitalFileEntryCreator())
    }

    fun createSurgeryReader(): FeedFileReader<SurgeryEntry> {
        return FeedFileReader.create(SurgeryEntryCreator())
    }

    fun createMedicationReader(atcModel: AtcModel): FeedFileReader<MedicationEntry> {
        return FeedFileReader.create(MedicationEntryCreator(atcModel))
    }

    fun createLabReader(): FeedFileReader<LabEntry> {
        return FeedFileReader.create(LabEntryCreator())
    }

    fun createVitalFunctionReader(): FeedFileReader<VitalFunctionEntry> {
        return FeedFileReader.create(VitalFunctionEntryCreator())
    }

    fun createIntoleranceReader(): FeedFileReader<IntoleranceEntry> {
        return FeedFileReader.create(IntoleranceEntryCreator())
    }

    fun createBodyWeightReader(): FeedFileReader<BodyWeightEntry> {
        return FeedFileReader.create(BodyWeightEntryCreator())
    }
}