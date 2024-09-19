package com.hartwig.actin.trial.status

import com.hartwig.actin.datamodel.trial.CohortMetadata
import com.hartwig.actin.trial.config.CohortDefinitionConfig
import com.hartwig.actin.trial.config.CohortDefinitionValidationError
import com.hartwig.actin.trial.config.TrialConfigDatabaseValidation
import com.hartwig.actin.trial.config.TrialDefinitionConfig
import com.hartwig.actin.trial.config.TrialDefinitionValidationError
import com.hartwig.actin.trial.interpretation.ConfigInterpreter

class TrialStatusConfigInterpreter(
    private val trialStatusDatabase: TrialStatusDatabase,
    private val trialPrefix: String? = null,
    private val ignoreNewTrials: Boolean = false
) :
    ConfigInterpreter {

    private val trialDefinitionValidationErrors = mutableListOf<TrialDefinitionValidationError>()
    private val cohortDefinitionValidationErrors = mutableListOf<CohortDefinitionValidationError>()
    private val trialStatusConfigValidationErrors = mutableListOf<TrialStatusConfigValidationError>()
    private val trialStatusDatabaseValidationErrors = mutableListOf<TrialStatusDatabaseValidationError>()

    override fun validation(): TrialStatusDatabaseValidation {
        return TrialStatusDatabaseValidation(
            trialStatusConfigValidationErrors,
            trialStatusDatabaseValidationErrors
        )
    }

    override fun appendTrialConfigValidation(trialConfigDatabaseValidation: TrialConfigDatabaseValidation): TrialConfigDatabaseValidation {
        return TrialConfigDatabaseValidation(
            trialDefinitionValidationErrors = trialConfigDatabaseValidation.trialDefinitionValidationErrors + trialDefinitionValidationErrors,
            cohortDefinitionValidationErrors = trialConfigDatabaseValidation.cohortDefinitionValidationErrors + cohortDefinitionValidationErrors,
            inclusionCriteriaValidationErrors = trialConfigDatabaseValidation.inclusionCriteriaValidationErrors,
            inclusionCriteriaReferenceValidationErrors = trialConfigDatabaseValidation.inclusionCriteriaReferenceValidationErrors,
            unusedRuleToKeepValidationErrors = trialConfigDatabaseValidation.unusedRuleToKeepValidationErrors
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
                    TrialStatusConfigValidationError(
                        it,
                        "Trial ID that is configured to be ignored is not actually present in trial database"
                    )
                )
            }
        }
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

    override fun checkModelForUnusedStudyMETCsToIgnore() {
        val unusedStudyMETCsToIgnore = extractUnusedStudyMETCsToIgnore()

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

    override fun checkModelForUnusedUnmappedCohortIds() {

        val unusedUnmappedCohortIds = extractUnusedUnmappedCohorts()

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

    internal fun extractUnusedStudyMETCsToIgnore(): List<String> {
        val trialStatusStudyMETCs = trialStatusDatabase.entries.map { it.metcStudyID }.toSet()
        return trialStatusDatabase.studyMETCsToIgnore.filter { !trialStatusStudyMETCs.contains(it) }
    }

    internal fun extractUnusedUnmappedCohorts(): List<String> {
        val trialStatusCohortIds = trialStatusDatabase.entries.mapNotNull { it.cohortId }.toSet()
        return trialStatusDatabase.unmappedCohortIds.filter { !trialStatusCohortIds.contains(it) }
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

    internal fun extractNewTrialStatusDatabaseCohorts(cohortConfigs: List<CohortDefinitionConfig>): Set<TrialStatusEntry> {
        val configuredTrialIds = cohortConfigs.map { it.trialId }.toSet()
        val configuredCohortIds = cohortConfigs.flatMap(CohortDefinitionConfig::externalCohortIds)

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