package com.hartwig.actin.trial.status

import com.hartwig.actin.trial.config.CohortDefinitionConfig
import com.hartwig.actin.trial.config.CohortDefinitionValidationError
import com.hartwig.actin.trial.config.TrialDefinitionConfig
import com.hartwig.actin.trial.config.TrialDefinitionValidationError
import com.hartwig.actin.trial.datamodel.CohortMetadata
import com.hartwig.actin.trial.interpretation.ConfigInterpreter
import org.apache.logging.log4j.LogManager

class TrialStatusConfigInterpreter(private val trialStatusDatabase: TrialStatusDatabase) : ConfigInterpreter {

    private val trialDefinitionValidationErrors = mutableListOf<TrialDefinitionValidationError>()
    private val trialStatusDatabaseValidationErrors = mutableListOf<TrialStatusDatabaseValidationError>()
    private val cohortDefinitionValidationErrors = mutableListOf<CohortDefinitionValidationError>()

    override fun validation(): TrialStatusDatabaseValidation {
        return TrialStatusDatabaseValidation(
            trialDefinitionValidationErrors,
            trialStatusDatabaseValidationErrors
        )
    }

    override fun isTrialOpen(trialConfig: TrialDefinitionConfig): Boolean? {
        if (!trialConfig.trialId.startsWith(MEC_TRIAL_PREFIX)) {
            LOGGER.debug(
                " Skipping study status retrieval for {} ({}) since study is not managed by the trial status database",
                trialConfig.trialId,
                trialConfig.acronym
            )

            return null
        }

        val (openInTrialStatusDatabase, interpreterValidationErrors) = TrialStatusInterpreter.isOpen(trialStatusDatabase.entries, trialConfig)
        trialDefinitionValidationErrors.addAll(interpreterValidationErrors)
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
                "No study status found in trial status overview, using manually configured status for study status"
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
            trialStatusDatabaseValidationErrors.addAll(newTrialsInTrialStatusDatabase.map {
                TrialStatusDatabaseValidationError(it, " New trial detected in trial status database that is not configured to be ignored")
            })
        }
    }

    internal fun extractNewTrialStatusDatabaseStudies(trialConfigs: List<TrialDefinitionConfig>): Set<TrialStatusEntry> {
        val configuredTrialIds = trialConfigs.map { it.trialId }

        return trialStatusDatabase.entries.filter { !trialStatusDatabase.studyMETCsToIgnore.contains(it.studyMETC) }
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
                    it, "New cohort detected in trial status dataabase that is not configured as unmapped"
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
            .filter { !trialStatusDatabase.studyMETCsToIgnore.contains(it.studyMETC) }
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

    companion object {
        private val LOGGER = LogManager.getLogger(TrialStatusConfigInterpreter::class.java)
        const val MEC_TRIAL_PREFIX = "MEC"

        fun constructTrialId(entry: TrialStatusEntry): String {
            return MEC_TRIAL_PREFIX + " " + entry.studyMETC
        }
    }
}