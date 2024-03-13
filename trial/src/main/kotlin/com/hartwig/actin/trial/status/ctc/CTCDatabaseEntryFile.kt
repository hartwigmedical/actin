package com.hartwig.actin.trial.status.ctc

import com.hartwig.actin.trial.FileUtil
import com.hartwig.actin.trial.status.TrialStatus
import com.hartwig.actin.trial.status.TrialStatusEntry
import com.hartwig.actin.util.ResourceFile

object CTCDatabaseEntryFile {

    fun read(tsv: String): List<TrialStatusEntry> {
        return FileUtil.createObjectsFromTsv(tsv, CTCDatabaseEntryFile::create)
    }

    private fun create(fields: Map<String, Int>, parts: List<String>): TrialStatusEntry {
        return TrialStatusEntry(
            studyId = ResourceFile.integer(parts[fields["StudyID"]!!]),
            studyMETC = parts[fields["StudyMETC"]!!],
            studyAcronym = parts[fields["StudyAcroniem"]!!],
            studyTitle = parts[fields["StudyTitle"]!!],
            studyStatus = fromStatusString(parts[fields["StudyStatus"]!!]),
            cohortId = ResourceFile.optionalInteger(parts[fields["CohortId"]!!]),
            cohortParentId = ResourceFile.optionalInteger(parts[fields["CohortParentId"]!!]),
            cohortName = ResourceFile.optionalString(parts[fields["CohortName"]!!]),
            cohortStatus = ResourceFile.optionalString(parts[fields["CohortStatus"]!!])?.let { fromStatusString(it) },
            cohortSlotsNumberAvailable = ResourceFile.optionalInteger(parts[fields["CohortSlotsNumberAvailable"]!!]),
            cohortSlotsDateUpdate = ResourceFile.optionalString(parts[fields["CohortSlotsDateUpdate"]!!])
        )
    }

    private val OPEN_STATES = setOf("Open")
    private val CLOSED_STATES = setOf("Gesloten", "Nog niet geopend", "Gesloten voor inclusie", "Onbekend", "Tijdelijk gesloten")

    private fun fromStatusString(string: String): TrialStatus {
        return when {
            OPEN_STATES.any { it.equals(string, ignoreCase = true) } -> TrialStatus.OPEN

            CLOSED_STATES.any { it.equals(string, ignoreCase = true) } -> TrialStatus.CLOSED

            else -> {
                TrialStatus.UNINTERPRETABLE
            }
        }

    }
}