package com.hartwig.actin.trial.status

import com.hartwig.actin.trial.FileUtil
import com.hartwig.actin.util.ResourceFile

object CohortStatusFile {

    fun read(tsv: String): List<CohortStatusEntry> {
        return FileUtil.createObjectsFromTsv(tsv, CohortStatusFile::create)
    }

    private fun create(fields: Map<String, Int>, parts: List<String>): CohortStatusEntry {
        return CohortStatusEntry(
            nctId = parts[fields["nctId"]!!],
            trialStatus = TrialStatus.valueOf(parts[fields["trialStatus"]!!].uppercase()),
            cohortId = parts[fields["cohortId"]!!],
            cohortParentId = ResourceFile.optionalString(parts[fields["cohortParentId"]!!]),
            cohortStatus = TrialStatus.valueOf(parts[fields["cohortStatus"]!!].uppercase()),
            cohortSlotsNumberAvailable = ResourceFile.optionalInteger(parts[fields["cohortSlotsNumberAvailable"]!!]),
            cohortSlotsDateUpdate = ResourceFile.optionalString(parts[fields["cohortSlotsDateUpdate"]!!])
        )
    }
}