package com.hartwig.actin.trial.status.ctc

import com.hartwig.actin.trial.FileUtil
import com.hartwig.actin.trial.status.TrialStatusEntry
import com.hartwig.actin.trial.status.TrialStatusEntryReader
import com.hartwig.actin.util.ResourceFile
import org.apache.logging.log4j.LogManager

private const val CTC_DATABASE_TSV = "ctc_database.tsv"

class CTCTrialStatusEntryReader : TrialStatusEntryReader {

    override fun read(inputPath: String): List<TrialStatusEntry> {
        val entries = FileUtil.createObjectsFromTsv("$inputPath/$CTC_DATABASE_TSV", ::create)
        LOGGER.info(" Read {} CTC database entries from {}", entries.size, inputPath)
        return entries
    }

    private fun create(fields: Map<String, Int>, parts: List<String>): TrialStatusEntry {
        return TrialStatusEntry(
            metcStudyID = parts[fields["StudyMETC"]!!],
            studyStatus = CTCStatusResolver.resolve(parts[fields["StudyStatus"]!!]),
            cohortId = ResourceFile.optionalString(parts[fields["CohortId"]!!]),
            cohortParentId = ResourceFile.optionalString(parts[fields["CohortParentId"]!!]),
            cohortStatus = ResourceFile.optionalString(parts[fields["CohortStatus"]!!])?.let { CTCStatusResolver.resolve(it) },
            cohortSlotsNumberAvailable = ResourceFile.optionalInteger(parts[fields["CohortSlotsNumberAvailable"]!!]),
            cohortSlotsDateUpdate = ResourceFile.optionalString(parts[fields["CohortSlotsDateUpdate"]!!])
        )
    }

    companion object {
        private val LOGGER = LogManager.getLogger(CTCTrialStatusEntryReader::class.java)
    }
}