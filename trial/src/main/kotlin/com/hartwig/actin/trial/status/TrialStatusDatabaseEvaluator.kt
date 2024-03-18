package com.hartwig.actin.trial.status

import org.apache.logging.log4j.LogManager

class TrialStatusDatabaseEvaluator(private val trialStatusDatabase: TrialStatusDatabase) {

    fun evaluateDatabaseConfiguration(): Pair<List<IgnoreValidationError>, List<TrialStatusUnmappedValidationError>> {
        val unusedStudyMETCsToIgnore = extractUnusedStudyMETCsToIgnore()

        val ignoreValidationErrors = if (unusedStudyMETCsToIgnore.isEmpty()) {
            LOGGER.info(" No unused study METCs to ignore found")
            emptyList()
        } else {
            unusedStudyMETCsToIgnore.map {
                IgnoreValidationError(
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
                TrialStatusUnmappedValidationError(
                    it,
                    "Cohort ID that is configured to be unmapped is not actually referenced in CTC database"
                )
            }
        }

        return ignoreValidationErrors to unmappedValidationErrors
    }

    internal fun extractUnusedStudyMETCsToIgnore(): List<String> {
        val trialStatusStudyMETCs = trialStatusDatabase.entries.map { it.studyMETC }.toSet()

        return trialStatusDatabase.studyMETCsToIgnore.filter { !trialStatusStudyMETCs.contains(it) }
    }

    internal fun extractUnusedUnmappedCohorts(): List<Int> {
        val trialStatusCohortIds = trialStatusDatabase.entries.mapNotNull { it.cohortId }.toSet()

        return trialStatusDatabase.unmappedCohortIds.filter { !trialStatusCohortIds.contains(it) }
    }

    companion object {
        private val LOGGER = LogManager.getLogger(TrialStatusDatabaseEvaluator::class.java)
    }
}