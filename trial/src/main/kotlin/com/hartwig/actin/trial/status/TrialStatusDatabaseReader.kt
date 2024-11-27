package com.hartwig.actin.trial.status

import com.hartwig.actin.util.Paths
import org.apache.logging.log4j.LogManager

private const val COHORT_STATUS_TSV = "cohort_status.tsv"

class TrialStatusDatabaseReader {

    fun read(configDirectory: String): TrialStatusDatabase {
        LOGGER.info("Reading trial and cohort status config from {}", configDirectory)
        val basePath = Paths.forceTrailingFileSeparator(configDirectory)
        val entries = CohortStatusFile.read(basePath + COHORT_STATUS_TSV)
        val trialStatusDatabase = TrialStatusDatabase(
            entries = entries,
        )
        return trialStatusDatabase
    }

    companion object {
        private val LOGGER = LogManager.getLogger(TrialStatusDatabaseReader::class.java)
    }
}