package com.hartwig.actin.trial.status

import com.hartwig.actin.trial.config.CohortDefinitionConfig
import com.hartwig.actin.trial.config.CohortDefinitionValidationError
import com.hartwig.actin.trial.config.TrialDefinitionConfig
import com.hartwig.actin.trial.config.TrialDefinitionValidationError
import com.hartwig.actin.trial.datamodel.CohortMetadata
import com.hartwig.actin.trial.interpretation.ConfigInterpreter

class TrialStatusConfigInterpreter(
    private val trialStatusDatabase: TrialStatusDatabase,
    private val trialPrefix: String? = null,
    private val ignoreNewTrials: Boolean
) :
    ConfigInterpreter {

    private val trialDefinitionValidationErrors = mutableListOf<TrialDefinitionValidationError>()
    private val trialStatusDatabaseValidationErrors = mutableListOf<TrialStatusDatabaseValidationError>()
    private val trialStatusConfigValidationErrors = mutableListOf<TrialStatusDatabaseConfigValidationError>()
    private val cohortDefinitionValidationErrors = mutableListOf<CohortDefinitionValidationError>()

    override fun validation(): TrialStatusDatabaseValidation {
        return TrialStatusDatabaseValidation(
            trialDefinitionValidationErrors,
            trialStatusDatabaseValidationErrors
        )
    }

    override fun isTrialOpen(trialConfig: TrialDefinitionConfig): Boolean? {
        val (openInTrialStatusDatabase, interpreterValidationErrors) = TrialStatusInterpreter.isOpen(
            trialStatusDatabase.entries,
            trialConfig,
            this::constructTrialId
        )
        trialDefinitionValidationErrors.addAll(interpreterValidationErrors)

        if (trialStatusDatabase.studiesNotInTrialStatusDatabase.contains(trialConfig.trialId) && openInTrialStatusDatabase != null) {
            trialStatusConfigValidationErrors.add(
                TrialStatusDatabaseConfigValidationError(
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
                trialDefinitionValidationErrors.add(
                    TrialDefinitionValidationError(
                        trialConfig,
                        "Trial has a manually configured open status while status could be derived from trial status database"
                    )
                )
            }
            return openInTrialStatusDatabase
        }

        trialDefinitionValidationErrors.add(
            TrialDefinitionValidationError(
                trialConfig,
                "No study status found in trial status database overview, using manually configured status for study status"
            )
        )
        return null
    }

    override fun resolveCohortMetadata(cohortConfig: CohortDefinitionConfig): CohortMetadata {
        val (maybeInterpretedCohortStatus, cohortDefinitionValidationErrors, trialStatusDatabaseValidationErrors) = CohortStatusInterpreter.interpret(
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
            blacklist = cohortConfig.blacklist,
            description = cohortConfig.description
        )
    }

    override fun checkModelForNewTrials(trialConfigs: List<TrialDefinitionConfig>) {
        val newTrialsInTrialStatusDatabase = extractNewTrialStatusDatabaseStudies(trialConfigs)

        if (newTrialsInTrialStatusDatabase.isEmpty()) {
            LOGGER.info(" No new studies found in trial status database that are not explicitly ignored.")
        } else {
            if (!ignoreNewTrials) {
                trialStatusDatabaseValidationErrors.addAll(newTrialsInTrialStatusDatabase.map {
                    TrialStatusDatabaseValidationError(
                        it,
                        " New trial detected in trial status database that is not configured to be ignored"
                    )
                })
            }
        }
    }

    override fun checkModelForUnusedStudiesNotInTrialStatusDatabase(trialConfigs: List<TrialDefinitionConfig>) {
        val unusedMecStudiesNotInTrialStatusDatabase = extractUnusedStudiesNotInTrialStatusDatabase(trialConfigs)

        if (unusedMecStudiesNotInTrialStatusDatabase.isNotEmpty()) {
            unusedMecStudiesNotInTrialStatusDatabase.map {
                trialStatusConfigValidationErrors.add(
                    TrialStatusDatabaseConfigValidationError(
                        unusedMecStudiesNotInTrialStatusDatabase.joinToString { ", " },
                        "Trial ID that is configured to be ignored is not actually present in trial database"
                    )
                )
            }
        }
    }

    internal fun extractUnusedStudiesNotInTrialStatusDatabase(trialConfigs: List<TrialDefinitionConfig>): List<String> {
        val trialConfigIds = trialConfigs.map { it.trialId }.toSet()
        return trialStatusDatabase.studiesNotInTrialStatusDatabase.filter { !trialConfigIds.contains(it) }
    }

    internal fun extractNewTrialStatusDatabaseStudies(trialConfigs: List<TrialDefinitionConfig>): Set<TrialStatusEntry> {
        val configuredTrialIds = trialConfigs.map { it.trialId }

        return trialStatusDatabase.entries.filter { !trialStatusDatabase.studyMETCsToIgnore.contains(it.metcStudyID) }
            .filter { !configuredTrialIds.contains(constructTrialId(it)) }
            .toSet()
    }

    override fun checkModelForNewCohorts(cohortConfigs: List<CohortDefinitionConfig>) {
        val newCohortEntriesInTrialStatusDatabase = extractNewTrialStatusDatabaseCohorts(cohortConfigs)

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

    internal fun extractNewTrialStatusDatabaseCohorts(cohortConfigs: List<CohortDefinitionConfig>): Set<TrialStatusEntry> {
        val configuredTrialIds = cohortConfigs.map { it.trialId }.toSet()

        val configuredCohortIds =
            cohortConfigs.flatMap(CohortDefinitionConfig::externalCohortIds).mapNotNull(String::toIntOrNull)

        val childrenPerParent =
            trialStatusDatabase.entries.filter { it.cohortParentId != null }
                .groupBy({ it.cohortParentId }, { it.cohortId })

        return trialStatusDatabase.entries.asSequence()
            .filter { configuredTrialIds.contains(constructTrialId(it)) }
            .filter { !trialStatusDatabase.studyMETCsToIgnore.contains(it.metcStudyID) }
            .filter { it.cohortId != null }
            .filter { !trialStatusDatabase.unmappedCohortIds.contains(it.cohortId) }
            .filter { !configuredCohortIds.contains(it.cohortId) }
            .filter { childrenPerParent[it.cohortId] == null || !childrenPerParent[it.cohortId]!!.containsAll(configuredCohortIds) }
            .toSet()
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

    private fun constructTrialId(entry: TrialStatusEntry): String {
        return trialPrefix?.let { "$it ${entry.metcStudyID}" } ?: entry.metcStudyID
    }

    companion object {
        private val LOGGER = org.apache.logging.log4j.LogManager.getLogger(TrialStatusInterpreter::class.java)
    }
}