package com.hartwig.actin.clinical.feed.emc

import com.hartwig.actin.clinical.feed.emc.digitalfile.DigitalFileEntry
import java.io.IOException
import java.time.LocalDateTime

class FeedModel(private val feed: EmcClinicalFeed) {
    fun toNewWay(): Map<String, FeedRecord> {
        val map: HashMap<String, FeedRecord> = HashMap()
        feed.patientEntries.forEach { patientEntry ->
            val patientId = patientEntry.subject
            map[patientId] = FeedRecord(
                patientEntry,
                entriesForSubject(feed.labEntries, patientId),
                entriesForSubject(feed.medicationEntries, patientId),
                entriesForSubject(feed.intoleranceEntries, patientId),
                entriesForSubject(feed.bodyWeightEntries, patientId).distinct(),
                entriesForSubject(feed.questionnaireEntries, patientId),
                entriesForSubject(feed.digitalFileEntries, patientId).filter(DigitalFileEntry::isToxicityEntry),
                entriesForSubject(feed.digitalFileEntries, patientId).filter(DigitalFileEntry::isBloodTransfusionEntry),
                feed.validationWarnings.filter { it.subject == patientId }.toSet(),
                entriesForSubject(feed.surgeryEntries, patientId).distinctBy { Pair(it.periodStart, it.periodEnd) },
                entriesForSubject(feed.vitalFunctionEntries, patientId).distinctBy {
                    VitalFunctionProperties(
                        it.effectiveDateTime,
                        it.quantityValue ?: Double.NaN,
                        it.quantityUnit,
                        it.componentCodeDisplay,
                        it.isValid()
                    )
                }
            )
        }
        return map.toMutableMap()
    }

    private data class VitalFunctionProperties(
        val effectiveDateTime: LocalDateTime,
        val quantityValue: Double,
        val quantityUnit: String,
        val componentCodeDisplay: String,
        val valid: Boolean
    )

    companion object {
        @Throws(IOException::class)
        fun fromFeedDirectory(clinicalFeedDirectory: String): FeedModel {
            return FeedModel(ClinicalFeedReader.read(clinicalFeedDirectory))
        }

        private fun <T : FeedEntry> entriesForSubject(allEntries: List<T>, subject: String): List<T> {
            return allEntries.filter { entry: T -> entry.subject == subject }
        }
    }
}