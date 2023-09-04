package com.hartwig.actin.treatment.ctc.config

import com.hartwig.actin.treatment.FileUtil
import com.hartwig.actin.util.ResourceFile

object CTCDatabaseEntryFile {

    fun read(tsv: String): List<CTCDatabaseEntry> {
        return FileUtil.createObjectsFromTsv(tsv, ::create)
    }

    private fun create(fields: Map<String, Int>, parts: List<String>): CTCDatabaseEntry {
        return CTCDatabaseEntry(
            studyId = ResourceFile.integer(parts[fields["StudyID"]!!]),
            studyMETC = parts[fields["StudyMETC"]!!],
            studyAcronym = parts[fields["StudyAcroniem"]!!],
            studyTitle = parts[fields["StudyTitle"]!!],
            studyStatus = parts[fields["StudyStatus"]!!],
            cohortId = ResourceFile.optionalInteger(parts[fields["CohortId"]!!]),
            cohortParentId = ResourceFile.optionalInteger(parts[fields["CohortParentId"]!!]),
            cohortName = ResourceFile.optionalString(parts[fields["CohortName"]!!]),
            cohortStatus = ResourceFile.optionalString(parts[fields["CohortStatus"]!!]),
            cohortSlotsNumberAvailable = ResourceFile.optionalInteger(parts[fields["CohortSlotsNumberAvailable"]!!]),
            cohortSlotsDateUpdate = ResourceFile.optionalString(parts[fields["CohortSlotsDateUpdate"]!!])
        )
    }
}