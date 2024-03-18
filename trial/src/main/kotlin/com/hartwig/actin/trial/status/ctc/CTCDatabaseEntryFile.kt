package com.hartwig.actin.trial.status.ctc

import com.hartwig.actin.trial.FileUtil
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
            studyStatus = CTCStatusResolver.resolve(parts[fields["StudyStatus"]!!]),
            cohortId = ResourceFile.optionalInteger(parts[fields["CohortId"]!!]),
            cohortParentId = ResourceFile.optionalInteger(parts[fields["CohortParentId"]!!]),
            cohortName = ResourceFile.optionalString(parts[fields["CohortName"]!!]),
            cohortStatus = ResourceFile.optionalString(parts[fields["CohortStatus"]!!])?.let { CTCStatusResolver.resolve(it) },
            cohortSlotsNumberAvailable = ResourceFile.optionalInteger(parts[fields["CohortSlotsNumberAvailable"]!!]),
            cohortSlotsDateUpdate = ResourceFile.optionalString(parts[fields["CohortSlotsDateUpdate"]!!])
        )
    }
}