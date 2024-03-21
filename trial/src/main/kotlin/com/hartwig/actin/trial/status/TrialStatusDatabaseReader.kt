package com.hartwig.actin.trial.status

import com.hartwig.actin.util.Paths
import org.apache.logging.log4j.LogManager

private const val IGNORE_STUDIES_TSV = "ignore_studies.tsv"
private const val UNMAPPED_COHORTS_TSV = "unmapped_cohorts.tsv"
private const val MEC_NOT_IN_CTC_TSV = "studies_not_in_ctc.tsv"

class TrialStatusDatabaseReader(private val trialStatusEntryReader: TrialStatusEntryReader) {

    fun read(configDirectory: String): TrialStatusDatabase {
        LOGGER.info("Reading trial status config from {}", configDirectory)
        val basePath = Paths.forceTrailingFileSeparator(configDirectory)
        val entries = trialStatusEntryReader.read(configDirectory)
        val trialStatusDatabase = TrialStatusDatabase(
            entries = entries,
            studyMETCsToIgnore = readIgnoreStudies(basePath + IGNORE_STUDIES_TSV),
            unmappedCohortIds = readUnmappedCohorts(basePath + UNMAPPED_COHORTS_TSV),
            studiesNotInTrialStatusDatabase = readStudiesNotInCTCStudies(basePath + MEC_NOT_IN_CTC_TSV)
        )

        LOGGER.info("Evaluating usage of trial status database configuration")
        val trialStatusDatabaseEvaluator = TrialStatusDatabaseEvaluator(trialStatusDatabase)
        trialStatusDatabaseEvaluator.evaluateDatabaseConfiguration()

        return trialStatusDatabase
    }

    private fun readIgnoreStudies(tsv: String): Set<String> {
        val ignoreStudies = IgnoreStudiesFile.read(tsv)
        LOGGER.info(" Read {} study METC ids to ignore from {}", ignoreStudies.size, tsv)
        return ignoreStudies
    }

    private fun readUnmappedCohorts(tsv: String): Set<Int> {
        val unmappedCohorts = UnmappedCohortFile.read(tsv)
        LOGGER.info(" Read {} unmapped cohorts from {}", unmappedCohorts.size, tsv)
        return unmappedCohorts
    }

    private fun readStudiesNotInCTCStudies(tsv: String): Set<String> {
        val notInCTCStudies = StudiesNotInTrialStatusDatabase.read(tsv)
        LOGGER.info(" Read {} studies without CTC status from {}", notInCTCStudies.size, tsv)
        return notInCTCStudies
    }

    companion object {
        private val LOGGER = LogManager.getLogger(TrialStatusDatabaseReader::class.java)
    }
}