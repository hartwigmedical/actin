package com.hartwig.actin.trial.status

import com.hartwig.actin.util.Paths
import org.apache.logging.log4j.LogManager

private const val IGNORE_STUDIES_TSV = "ignore_studies.tsv"
private const val UNMAPPED_COHORTS_TSV = "unmapped_cohorts.tsv"
private const val STUDIES_NOT_IN_TRIAL_STATUS_DATABASE_TSV = "studies_not_in_trial_status_database.tsv"

class TrialStatusDatabaseReader(private val trialStatusEntryReader: TrialStatusEntryReader) {

    fun read(configDirectory: String): TrialStatusDatabase {
        LOGGER.info("Reading trial status config from {}", configDirectory)
        val basePath = Paths.forceTrailingFileSeparator(configDirectory)
        val entries = trialStatusEntryReader.read(configDirectory)
        val trialStatusDatabase = TrialStatusDatabase(
            entries = entries,
            studyMETCsToIgnore = readIgnoreStudies(basePath + IGNORE_STUDIES_TSV),
            unmappedCohortIds = readUnmappedCohorts(basePath + UNMAPPED_COHORTS_TSV),
            studiesNotInTrialStatusDatabase = readStudiesNotInTrialStatusDatabaseStudies(basePath + STUDIES_NOT_IN_TRIAL_STATUS_DATABASE_TSV)
        )
        return trialStatusDatabase
    }

    private fun readIgnoreStudies(tsv: String): Set<String> {
        val ignoreStudies = IgnoreStudiesFile.read(tsv)
        LOGGER.info(" Read {} study METC ids to ignore from {}", ignoreStudies.size, tsv)
        return ignoreStudies
    }

    private fun readUnmappedCohorts(tsv: String): Set<String> {
        val unmappedCohorts = UnmappedCohortFile.read(tsv)
        LOGGER.info(" Read {} unmapped cohorts from {}", unmappedCohorts.size, tsv)
        return unmappedCohorts
    }

    private fun readStudiesNotInTrialStatusDatabaseStudies(tsv: String): Set<String> {
        val notInTrialStatusDatabaseStudies = StudiesNotInTrialStatusDatabase.read(tsv)
        LOGGER.info(" Read {} studies not in trial status database from {}", notInTrialStatusDatabaseStudies.size, tsv)
        return notInTrialStatusDatabaseStudies
    }

    companion object {
        private val LOGGER = LogManager.getLogger(TrialStatusDatabaseReader::class.java)
    }
}