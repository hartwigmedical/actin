package com.hartwig.actin.trial.ctc

import com.hartwig.actin.trial.CTCIgnoreValidationError
import com.hartwig.actin.trial.CTCUnmappedValidationError
import com.hartwig.actin.trial.ctc.config.CTCDatabase
import org.apache.logging.log4j.LogManager

class CTCDatabaseEvaluator(private val ctcDatabase: CTCDatabase) {

    fun evaluateDatabaseConfiguration(): Pair<List<CTCIgnoreValidationError>, List<CTCUnmappedValidationError>> {
        val unusedStudyMETCsToIgnore = extractUnusedStudyMETCsToIgnore()

        val ignoreValidationErrors = if (unusedStudyMETCsToIgnore.isEmpty()) {
            LOGGER.info(" No unused study METCs to ignore found")
            emptyList()
        } else {
            unusedStudyMETCsToIgnore.map {
                CTCIgnoreValidationError(
                    it,
                    "Study that is configured to be ignored is not actually referenced in CTC database"
                )
            }
        }

        val unusedUnmappedCohortIds = extractUnusedUnmappedCohorts()

        val unmappedValidationErrors = if (unusedUnmappedCohortIds.isEmpty()) {
            LOGGER.info(" No unused unmapped cohort IDs found")
            emptyList()
        } else {
            unusedUnmappedCohortIds.map {
                CTCUnmappedValidationError(
                    it,
                    "Cohort ID that is configured to be unmapped is not actually referenced in CTC database"
                )
            }
        }

        return ignoreValidationErrors to unmappedValidationErrors
    }

    internal fun extractUnusedStudyMETCsToIgnore(): List<String> {
        val ctcStudyMETCs = ctcDatabase.entries.map { it.studyMETC }.toSet()

        return ctcDatabase.studyMETCsToIgnore.filter { !ctcStudyMETCs.contains(it) }
    }

    internal fun extractUnusedUnmappedCohorts(): List<Int> {
        val ctcCohortIds = ctcDatabase.entries.mapNotNull { it.cohortId }.toSet()

        return ctcDatabase.unmappedCohortIds.filter { !ctcCohortIds.contains(it) }
    }

    companion object {
        private val LOGGER = LogManager.getLogger(CTCDatabaseEvaluator::class.java)
    }
}