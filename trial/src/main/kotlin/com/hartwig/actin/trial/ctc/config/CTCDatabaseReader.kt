package com.hartwig.actin.trial.ctc.config

import com.hartwig.actin.trial.config.TrialConfigDatabaseReader
import com.hartwig.actin.trial.ctc.CTCDatabaseEvaluator
import com.hartwig.actin.util.Paths
import org.apache.logging.log4j.LogManager

object CTCDatabaseReader {

    private val LOGGER = LogManager.getLogger(CTCDatabaseReader::class.java)

    private const val CTC_DATABASE_TSV = "ctc_database.tsv"
    private const val IGNORE_STUDIES_TSV = "ignore_studies.tsv"
    private const val UNMAPPED_COHORTS_TSV = "unmapped_cohorts.tsv"
    private const val MEC_NOT_IN_CTC_TSV = "mec_studies_not_in_ctc.tsv"

    fun read(ctcConfigDirectory: String, ctcTrialConfigDirectory: String): CTCDatabase {
        LOGGER.info("Reading CTC config from {}", ctcConfigDirectory)
        val basePath = Paths.forceTrailingFileSeparator(ctcConfigDirectory)
        val ctcDatabase = CTCDatabase(
            entries = readCTCDatabaseEntries(basePath + CTC_DATABASE_TSV),
            studyMETCsToIgnore = readIgnoreStudies(basePath + IGNORE_STUDIES_TSV),
            unmappedCohortIds = readUnmappedCohorts(basePath + UNMAPPED_COHORTS_TSV),
            mecStudiesNotInCTC = readMECNotInCTCStudies(basePath + MEC_NOT_IN_CTC_TSV)
        )

        LOGGER.info("Evaluating usage of CTC database configuration")
        val ctcDatabaseEvaluator = CTCDatabaseEvaluator(ctcDatabase, TrialConfigDatabaseReader.read(ctcTrialConfigDirectory))
        ctcDatabaseEvaluator.evaluateDatabaseConfiguration()

        return ctcDatabase
    }

    private fun readCTCDatabaseEntries(tsv: String): List<CTCDatabaseEntry> {
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

    private fun readMECNotInCTCStudies(tsv: String): Set<String> {
        val notInCTCStudies = MECNotInCTCFile.read(tsv)
        LOGGER.info(" Read {} MEC studies without CTC status from {}", notInCTCStudies.size, tsv)
        return notInCTCStudies
    }
}