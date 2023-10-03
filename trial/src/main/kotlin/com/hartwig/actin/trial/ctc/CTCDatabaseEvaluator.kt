package com.hartwig.actin.trial.ctc

import com.hartwig.actin.trial.ctc.config.CTCDatabase
import org.apache.logging.log4j.LogManager

class CTCDatabaseEvaluator constructor(private val ctcDatabase: CTCDatabase) {

    fun evaluateDatabaseConfiguration() {
        val unusedStudyMETCsToIgnore = extractUnusedStudyMETCsToIgnore()

        if (unusedStudyMETCsToIgnore.isEmpty()) {
            LOGGER.info(" No unused study METCs to ignore found")
        } else {
            for (unusedStudyMETCToIgnore in unusedStudyMETCsToIgnore) {
                LOGGER.warn(
                    " Study that is configured to be ignored is not actually referenced in CTC database: '{}'",
                    unusedStudyMETCToIgnore
                )
            }
        }

        val unusedUnmappedCohortIds = extractUnusedUnmappedCohorts()

        if (unusedUnmappedCohortIds.isEmpty()) {
            LOGGER.info(" No unused unmapped cohort IDs found")
        } else {
            for (unusedUnmappedCohortId in unusedUnmappedCohortIds) {
                LOGGER.warn(
                    " Cohort ID that is configured to be unmapped is not actually referenced in CTC database: '{}'",
                    unusedUnmappedCohortId
                )
            }
        }
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
        private val LOGGER = LogManager.getLogger(CTCModel::class.java)
    }
}