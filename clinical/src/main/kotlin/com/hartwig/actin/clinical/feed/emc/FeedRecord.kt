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

class FeedRecord(
    private val patientEntry: PatientEntry,
    private val labEntries: List<LabEntry>,
    private val medicationEntries: List<MedicationEntry>,
    private val intoleranceEntries: List<IntoleranceEntry>,
    private val bodyWeightEntries: List<BodyWeightEntry>,
    private val questionnaireEntries: List<QuestionnaireEntry>,
    private val filter: List<DigitalFileEntry>,
    private val filter1: List<DigitalFileEntry>,
    private val validationWarnings: Set<FeedValidationWarning>,
    private val surgeryEntries: List<SurgeryEntry>,
    private val distinctBy1: List<VitalFunctionEntry>
) {
    // done
    fun patientEntry(): PatientEntry {
        return patientEntry
    }

    // done
    fun labEntries(): List<LabEntry> {
        return labEntries
    }

    // done
    fun medicationEntries(): List<MedicationEntry> {
        return medicationEntries
    }

    // done
    fun intoleranceEntries(): List<IntoleranceEntry> {
        return intoleranceEntries
    }

    // done
    fun uniqueBodyWeightEntries(): List<BodyWeightEntry> {
        return bodyWeightEntries
    }

    // done
    fun latestQuestionnaireEntry(): QuestionnaireEntry? {
        return questionnaireEntries.maxByOrNull(QuestionnaireEntry::authored)
    }

    // done
    fun toxicityEntries(): List<DigitalFileEntry> {
        return filter
        //return filter.filter(DigitalFileEntry::isToxicityEntry)
    }

    // done
    fun bloodTransfusionEntries(): List<DigitalFileEntry> {
//        return FeedModel.entriesForSubject(feed.digitalFileEntries, subject)
//            .filter(DigitalFileEntry::isBloodTransfusionEntry)
        return filter1
    }

    // done
    fun uniqueSurgeryEntries(): List<SurgeryEntry> {
        return surgeryEntries
    }

    // done
    fun validationWarnings(): Set<FeedValidationWarning> {
        return validationWarnings
    }

    // done
    fun vitalFunctionEntries(): List<VitalFunctionEntry> {
        return distinctBy1
    }
}
