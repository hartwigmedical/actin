package com.hartwig.actin.clinical.feed.emc

class FeedModel(private val feed: EmcClinicalFeed) {
    fun read(): List<FeedRecord> {
        val patientEntries = feed.patientEntries.groupBy(FeedEntry::subject)
        val labEntries = feed.labEntries.groupBy(FeedEntry::subject)
        val medicationEntries = feed.medicationEntries.groupBy(FeedEntry::subject)
        val intoleranceEntries = feed.intoleranceEntries.groupBy(FeedEntry::subject)
        val bodyWeightEntries = feed.bodyWeightEntries.groupBy(FeedEntry::subject)
        val questionnaireEntries = feed.questionnaireEntries.groupBy(FeedEntry::subject)
        val digitalFileEntries = feed.digitalFileEntries.groupBy(FeedEntry::subject)
        val validationWarnings = feed.validationWarnings.groupBy(FeedValidationWarning::subject)
        val surgeryEntries = feed.surgeryEntries.groupBy(FeedEntry::subject)
        val vitalFunctionEntries = feed.vitalFunctionEntries.groupBy(FeedEntry::subject)
        return feed.patientEntries.map { patientEntry ->
            val patientId = patientEntry.subject
            FeedRecord.create(
                patientId,
                patientEntries[patientId].orEmpty(),
                labEntries[patientId].orEmpty(),
                medicationEntries[patientId].orEmpty(),
                intoleranceEntries[patientId].orEmpty(),
                bodyWeightEntries[patientId].orEmpty(),
                questionnaireEntries[patientId].orEmpty(),
                digitalFileEntries[patientId].orEmpty(),
                validationWarnings[patientId].orEmpty(),
                surgeryEntries[patientId].orEmpty(),
                vitalFunctionEntries[patientId].orEmpty()
            )
        }
    }


    companion object {
        fun fromFeedDirectory(clinicalFeedDirectory: String): FeedModel {
            return FeedModel(ClinicalFeedReader.read(clinicalFeedDirectory))
        }
    }
}