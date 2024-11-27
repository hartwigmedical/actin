package com.hartwig.actin.trial.status

import com.hartwig.actin.datamodel.trial.CohortMetadata
import com.hartwig.actin.trial.config.CohortDefinitionConfig
import com.hartwig.actin.trial.config.CohortDefinitionValidationError
import com.hartwig.actin.trial.config.TrialConfigDatabaseValidation
import com.hartwig.actin.trial.config.TrialDefinitionConfig
import com.hartwig.actin.trial.config.TrialDefinitionValidationError

class TrialStatusConfigInterpreter(
    private val trialStatusDatabase: TrialStatusDatabase,
    private val trialPrefix: String? = null,
    private val ignoreNewTrials: Boolean = false
) {

    private val trialStatusDatabaseExtractor = TrialStatusDatabaseExtractor(trialStatusDatabase, trialPrefix)
    private val trialDefinitionValidationErrors = mutableListOf<TrialDefinitionValidationError>()
    private val cohortDefinitionValidationErrors = mutableListOf<CohortDefinitionValidationError>()
    private val trialStatusConfigValidationErrors = mutableListOf<TrialStatusConfigValidationError>()
    private val trialStatusDatabaseValidationErrors = mutableListOf<TrialStatusDatabaseValidationError>()

    fun validation(): TrialStatusDatabaseValidation {
        return TrialStatusDatabaseValidation(
            trialStatusConfigValidationErrors,
            trialStatusDatabaseValidationErrors
        )
    }

    fun appendTrialConfigValidation(trialConfigDatabaseValidation: TrialConfigDatabaseValidation): TrialConfigDatabaseValidation {
        return TrialConfigDatabaseValidation(
            trialDefinitionValidationErrors = trialConfigDatabaseValidation.trialDefinitionValidationErrors + trialDefinitionValidationErrors,
            cohortDefinitionValidationErrors = trialConfigDatabaseValidation.cohortDefinitionValidationErrors + cohortDefinitionValidationErrors,
            inclusionCriteriaValidationErrors = trialConfigDatabaseValidation.inclusionCriteriaValidationErrors,
            inclusionCriteriaReferenceValidationErrors = trialConfigDatabaseValidation.inclusionCriteriaReferenceValidationErrors,
            unusedRulesToKeepValidationErrors = trialConfigDatabaseValidation.unusedRulesToKeepValidationErrors
        )
    }

    fun isTrialOpen(trialConfig: TrialDefinitionConfig): Boolean? {
        val (openInTrialStatusDatabase, interpreterValidationErrors) = TrialStatusInterpreter.isOpen(
            trialStatusDatabase.entries,
            trialConfig,
            trialStatusDatabaseExtractor::constructTrialId
        )
        trialDefinitionValidationErrors.addAll(interpreterValidationErrors)

        if (trialStatusDatabase.studiesNotInTrialStatusDatabase.contains(trialConfig.trialId) && openInTrialStatusDatabase != null) {
            trialStatusConfigValidationErrors.add(
                TrialStatusConfigValidationError(
                    trialConfig.trialId,
                    "Trial is configured as not in trial status database while status could be derived from trial status database"
                )
            )
        }

        if (trialStatusDatabase.studiesNotInTrialStatusDatabase.contains(trialConfig.trialId)) {
            LOGGER.debug(
                " Skipping study status retrieval for {} ({}) since study is not deemed a trial status database trial",
                trialConfig.trialId,
                trialConfig.acronym
            )

            return null
        }

        if (openInTrialStatusDatabase != null) {
            if (trialConfig.open != null) {
                trialStatusConfigValidationErrors.add(
                    TrialStatusConfigValidationError(
                        trialConfig.trialId,
                        "Trial has a manually configured status while status could be derived from trial status database (" + if (openInTrialStatusDatabase) "Open)" else "Closed)"
                    )
                )
            }
            return openInTrialStatusDatabase
        }

        trialStatusConfigValidationErrors.add(
            TrialStatusConfigValidationError(
                trialConfig.trialId,
                "No study status found in trial status database overview, using manually configured status for study status"
            )
        )
        return null
    }

    fun resolveCohortMetadata(cohortConfig: CohortDefinitionConfig): CohortMetadata {
        val (maybeInterpretedCohortStatus, cohortDefinitionValidationErrors, trialStatusDatabaseValidationErrors) =
            CohortStatusInterpreter.interpret(
                trialStatusDatabase.entries,
                cohortConfig
            )
        this.cohortDefinitionValidationErrors.addAll(cohortDefinitionValidationErrors)
        this.trialStatusDatabaseValidationErrors.addAll(trialStatusDatabaseValidationErrors)
        val interpretedCohortStatus = maybeInterpretedCohortStatus ?: fromCohortConfig(cohortConfig)
        return CohortMetadata(
            cohortId = cohortConfig.cohortId,
            evaluable = cohortConfig.evaluable,
            open = interpretedCohortStatus.open,
            slotsAvailable = interpretedCohortStatus.slotsAvailable,
            ignore = cohortConfig.ignore,
            description = cohortConfig.description
        )
    }

    fun checkModelForNewTrials(trialConfigs: List<TrialDefinitionConfig>) {
        val newTrialsInTrialStatusDatabase = trialStatusDatabaseExtractor.extractNewTrialStatusDatabaseStudies(trialConfigs)

        if (newTrialsInTrialStatusDatabase.isEmpty()) {
            LOGGER.info(" No new studies found in trial status database that are not explicitly ignored.")
        } else {
            if (!ignoreNewTrials) {
                trialStatusDatabaseValidationErrors.addAll(newTrialsInTrialStatusDatabase.distinctBy { it.metcStudyID }.map {
                    TrialStatusDatabaseValidationError(
                        it,
                        " New trial detected in trial status database that is not configured to be ignored"
                    )
                })
            }
        }
    }

    fun checkModelForUnusedStudiesNotInTrialStatusDatabase(trialConfigs: List<TrialDefinitionConfig>) {
        val unusedMecStudiesNotInTrialStatusDatabase =
            trialStatusDatabaseExtractor.extractUnusedStudiesNotInTrialStatusDatabase(trialConfigs)

        if (unusedMecStudiesNotInTrialStatusDatabase.isNotEmpty()) {
            unusedMecStudiesNotInTrialStatusDatabase.map {
                trialStatusConfigValidationErrors.add(
                    TrialStatusConfigValidationError(
                        it,
                        "Trial ID that is configured to be ignored is not actually present in trial database"
                    )
                )
            }
        }
    }

    fun checkModelForNewCohorts(cohortConfigs: List<CohortDefinitionConfig>) {
        val newCohortEntriesInTrialStatusDatabase = trialStatusDatabaseExtractor.extractNewTrialStatusDatabaseCohorts(cohortConfigs)

        if (newCohortEntriesInTrialStatusDatabase.isEmpty()) {
            LOGGER.info(" No new cohorts found in trial status database that are not explicitly unmapped.")
        } else {
            trialStatusDatabaseValidationErrors.addAll(newCohortEntriesInTrialStatusDatabase.map {
                TrialStatusDatabaseValidationError(
                    it, "New cohort detected in trial status database that is not configured as unmapped"
                )
            })
        }
    }

    fun checkModelForUnusedStudyMETCsToIgnore() {
        val unusedStudyMETCsToIgnore = trialStatusDatabaseExtractor.extractUnusedStudyMETCsToIgnore()

        if (unusedStudyMETCsToIgnore.isEmpty()) {
            LOGGER.info(" No unused study METCs to ignore found")
        } else {
            unusedStudyMETCsToIgnore.map {
                trialStatusConfigValidationErrors.add(
                    TrialStatusConfigValidationError(
                        it,
                        "Study that is configured to be ignored is not actually referenced in trial status database"
                    )
                )
            }
        }
    }

    fun checkModelForUnusedUnmappedCohortIds() {

        val unusedUnmappedCohortIds = trialStatusDatabaseExtractor.extractUnusedUnmappedCohorts()

        if (unusedUnmappedCohortIds.isEmpty()) {
            LOGGER.info(" No unused unmapped cohort IDs found")
        } else {
            unusedUnmappedCohortIds.map {
                trialStatusConfigValidationErrors.add(
                    TrialStatusConfigValidationError(
                        it,
                        "Cohort ID that is configured to be unmapped is not actually referenced in trial status database"
                    )
                )
            }
        }
    }

    private fun fromCohortConfig(cohortConfig: CohortDefinitionConfig): InterpretedCohortStatus {
        return if (cohortConfig.open == null || cohortConfig.slotsAvailable == null) {
            cohortDefinitionValidationErrors.add(
                CohortDefinitionValidationError(
                    cohortConfig,
                    "Missing open and/or slots available data for cohort"
                )
            )
            InterpretedCohortStatus(open = false, slotsAvailable = false)
        } else {
            InterpretedCohortStatus(open = cohortConfig.open, slotsAvailable = cohortConfig.slotsAvailable)
        }
    }

    companion object {
        private val LOGGER = org.apache.logging.log4j.LogManager.getLogger(TrialStatusInterpreter::class.java)
    }
}