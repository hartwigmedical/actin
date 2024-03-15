package com.hartwig.actin.trial.status.ctc

import com.hartwig.actin.trial.status.TrialStatusEntry
import com.hartwig.actin.trial.status.TrialStatusEntryReader
import org.apache.logging.log4j.LogManager

private const val CTC_DATABASE_TSV = "ctc_database.tsv"

class CTCTrialStatusEntryReader : TrialStatusEntryReader {

    override fun read(inputPath: String): List<TrialStatusEntry> {
        val entries = CTCDatabaseEntryFile.read("$inputPath/$CTC_DATABASE_TSV")
        LOGGER.info(" Read {} CTC database entries from {}", entries.size, inputPath)
        return entries
    }

    companion object {
        private val LOGGER = LogManager.getLogger(CTCTrialStatusEntryReader::class.java)
    }
}