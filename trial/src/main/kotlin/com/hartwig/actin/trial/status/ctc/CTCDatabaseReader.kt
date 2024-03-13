package com.hartwig.actin.trial.status.ctc

import com.hartwig.actin.trial.status.IgnoreStudiesFile
import com.hartwig.actin.trial.status.TrialStatusDatabase
import com.hartwig.actin.trial.status.TrialStatusDatabaseEvaluator
import com.hartwig.actin.trial.status.TrialStatusEntry
import com.hartwig.actin.trial.status.UnmappedCohortFile
import com.hartwig.actin.util.Paths
import org.apache.logging.log4j.LogManager

object CTCDatabaseReader {

    private val LOGGER = LogManager.getLogger(CTCDatabaseReader::class.java)

    private const val CTC_DATABASE_TSV = "ctc_database.tsv"
    private const val IGNORE_STUDIES_TSV = "ignore_studies.tsv"
    private const val UNMAPPED_COHORTS_TSV = "unmapped_cohorts.tsv"

    fun read(ctcConfigDirectory: String): TrialStatusDatabase {
        LOGGER.info("Reading CTC config from {}", ctcConfigDirectory)
        val basePath = Paths.forceTrailingFileSeparator(ctcConfigDirectory)
        val trialStatusDatabase = TrialStatusDatabase(
            entries = readCTCDatabaseEntries(basePath + CTC_DATABASE_TSV),
            studyMETCsToIgnore = readIgnoreStudies(basePath + IGNORE_STUDIES_TSV),
            unmappedCohortIds = readUnmappedCohorts(basePath + UNMAPPED_COHORTS_TSV)
        )

        LOGGER.info("Evaluating usage of CTC database configuration")
        val ctcDatabaseEvaluator = TrialStatusDatabaseEvaluator(trialStatusDatabase)
        ctcDatabaseEvaluator.evaluateDatabaseConfiguration()

        return trialStatusDatabase
    }

    private fun readCTCDatabaseEntries(tsv: String): List<TrialStatusEntry> {
        val entries = CTCDatabaseEntryFile.read(tsv)
        LOGGER.info(" Read {} CTC database entries from {}", entries.size, tsv)
        return entries
    }

    private fun readIgnoreStudies(tsv: String): Set<String> {
        val ignoreStudies = IgnoreStudiesFile.read(tsv)
        LOGGER.info(" Read {} study METCs to ignore from {}", ignoreStudies.size, tsv)
        return ignoreStudies
    }

    private fun readUnmappedCohorts(tsv: String): Set<Int> {
        val unmappedCohorts = UnmappedCohortFile.read(tsv)
        LOGGER.info(" Read {} unmapped cohorts from {}", unmappedCohorts.size, tsv)
        return unmappedCohorts
    }
}