package com.hartwig.actin.trial.ctc

import com.hartwig.actin.trial.CTCDatabaseValidationError
import com.hartwig.actin.trial.CohortDefinitionValidationError
import com.hartwig.actin.trial.CtcDatabaseValidation
import com.hartwig.actin.trial.TrialDefinitionValidationError
import com.hartwig.actin.trial.config.CohortDefinitionConfig
import com.hartwig.actin.trial.config.TrialDefinitionConfig
import com.hartwig.actin.trial.ctc.config.CTCDatabase
import com.hartwig.actin.trial.ctc.config.CTCDatabaseEntry
import com.hartwig.actin.trial.datamodel.CohortMetadata
import com.hartwig.actin.trial.interpretation.ConfigInterpreter
import org.apache.logging.log4j.LogManager

class CTCConfigInterpreter(private val ctcDatabase: CTCDatabase) : ConfigInterpreter {

    private val trialDefinitionValidationErrors = mutableListOf<TrialDefinitionValidationError>()
    private val ctcDatabaseValidationErrors = mutableListOf<CTCDatabaseValidationError>()
    private val cohortDefinitionValidationErrors = mutableListOf<CohortDefinitionValidationError>()

    override fun validation(): CtcDatabaseValidation {
        return CtcDatabaseValidation(
            trialDefinitionValidationErrors,
            ctcDatabaseValidationErrors
        )
    }

    override fun isTrialOpen(trialConfig: TrialDefinitionConfig): Boolean? {
        if (!trialConfig.trialId.startsWith(CTC_TRIAL_PREFIX) || ctcDatabase.mecStudyNotInCTC.contains(trialConfig.trialId)) {
            LOGGER.debug(
                " Skipping study status retrieval for {} ({}) since study is not deemed a CTC trial",
                trialConfig.trialId,
                trialConfig.acronym
            )

            return null
        }

        val (openInCTC, interpreterValidationErrors) = TrialStatusInterpreter.isOpen(ctcDatabase.entries, trialConfig)
        trialDefinitionValidationErrors.addAll(interpreterValidationErrors)
        if (openInCTC != null) {
            if (trialConfig.open != null) {
                trialDefinitionValidationErrors.add(
                    TrialDefinitionValidationError(
                        trialConfig,
                        "Trial has a manually configured open status while status could be derived from CTC"
                    )
                )
            }
            return openInCTC
        }

        trialDefinitionValidationErrors.add(
            TrialDefinitionValidationError(
                trialConfig,
                "No study status found in CTC overview, using manually configured status for study status"
            )
        )
        return null
    }

    override fun resolveCohortMetadata(cohortConfig: CohortDefinitionConfig): CohortMetadata {
        val (maybeInterpretedCohortStatus, cohortDefinitionValidationErrors, ctcDatabaseValidationErrors) = CohortStatusInterpreter.interpret(
            ctcDatabase.entries,
            cohortConfig
        )
        this.cohortDefinitionValidationErrors.addAll(cohortDefinitionValidationErrors)
        this.ctcDatabaseValidationErrors.addAll(ctcDatabaseValidationErrors)
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
        val newTrialsInCTC = extractNewCTCStudies(trialConfigs)

        if (newTrialsInCTC.isEmpty()) {
            LOGGER.info(" No new studies found in CTC database that are not explicitly ignored.")
        } else {
            ctcDatabaseValidationErrors.addAll(newTrialsInCTC.map {
                CTCDatabaseValidationError(it, " New trial detected in CTC that is not configured to be ignored")
            })
        }
    }

    internal fun extractNewCTCStudies(trialConfigs: List<TrialDefinitionConfig>): Set<CTCDatabaseEntry> {
        val configuredTrialIds = trialConfigs.map { it.trialId }

        return ctcDatabase.entries.filter { !ctcDatabase.studyMETCsToIgnore.contains(it.studyMETC) }
            .filter { !configuredTrialIds.contains(constructTrialId(it)) }
            .toSet()
    }

    override fun checkModelForNewCohorts(cohortConfigs: List<CohortDefinitionConfig>) {
        val newCohortEntriesInCTC = extractNewCTCCohorts(cohortConfigs)

        if (newCohortEntriesInCTC.isEmpty()) {
            LOGGER.info(" No new cohorts found in CTC database that are not explicitly unmapped.")
        } else {
            ctcDatabaseValidationErrors.addAll(newCohortEntriesInCTC.map {
                CTCDatabaseValidationError(
                    it, "New cohort detected in CTC that is not configured as unmapped"
                )
            })
        }
    }

    internal fun extractNewCTCCohorts(cohortConfigs: List<CohortDefinitionConfig>): Set<CTCDatabaseEntry> {
        val configuredTrialIds = cohortConfigs.map { it.trialId }.toSet()

        val configuredCohortIds =
            cohortConfigs.flatMap(CohortDefinitionConfig::externalCohortIds).mapNotNull(String::toIntOrNull)

        val childrenPerParent =
            ctcDatabase.entries.filter { it.cohortParentId != null }
                .groupBy({ it.cohortParentId }, { it.cohortId })

        return ctcDatabase.entries.asSequence()
            .filter { configuredTrialIds.contains(constructTrialId(it)) }
            .filter { !ctcDatabase.studyMETCsToIgnore.contains(it.studyMETC) }
            .filter { it.cohortId != null }
            .filter { !ctcDatabase.unmappedCohortIds.contains(it.cohortId) }
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
        private val LOGGER = LogManager.getLogger(CTCConfigInterpreter::class.java)
        const val CTC_TRIAL_PREFIX = "MEC"

        fun constructTrialId(entry: CTCDatabaseEntry): String {
            return CTC_TRIAL_PREFIX + " " + entry.studyMETC
        }
    }
}