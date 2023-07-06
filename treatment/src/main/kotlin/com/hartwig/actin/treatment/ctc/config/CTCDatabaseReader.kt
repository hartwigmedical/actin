package com.hartwig.actin.treatment.ctc.config

import com.hartwig.actin.treatment.ctc.CTCDatabaseEvaluator
import com.hartwig.actin.util.Paths
import org.apache.logging.log4j.LogManager

object CTCDatabaseReader {

    private val LOGGER = LogManager.getLogger(CTCDatabaseReader::class.java)

    private const val CTC_DATABASE_TSV = "ctc_database.tsv"
    private const val IGNORE_STUDIES_TSV = "ignore_studies.tsv"
    private const val UNMAPPED_COHORTS_TSV = "unmapped_cohorts.tsv"

    fun read(ctcConfigDirectory: String): CTCDatabase {
        LOGGER.info("Reading CTC config from {}", ctcConfigDirectory)
        val basePath = Paths.forceTrailingFileSeparator(ctcConfigDirectory)
        val ctcDatabase = CTCDatabase(
            entries = readCTCDatabaseEntries(basePath + CTC_DATABASE_TSV),
            studyMETCsToIgnore = readIgnoreStudies(basePath + IGNORE_STUDIES_TSV),
            unmappedCohortIds = readUnmappedCohorts(basePath + UNMAPPED_COHORTS_TSV)
        )

        LOGGER.info("Evaluating usage of CTC database configuration")
        val ctcDatabaseEvaluator = CTCDatabaseEvaluator(ctcDatabase)
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
}