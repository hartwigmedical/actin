package com.hartwig.actin.trial.ctc

import com.hartwig.actin.treatment.datamodel.CohortMetadata
import com.hartwig.actin.treatment.datamodel.ImmutableCohortMetadata
import com.hartwig.actin.trial.config.CohortDefinitionConfig
import com.hartwig.actin.trial.config.TrialDefinitionConfig
import com.hartwig.actin.trial.ctc.config.CTCDatabase
import com.hartwig.actin.trial.ctc.config.CTCDatabaseEntry
import org.apache.logging.log4j.LogManager

class CTCModel constructor(private val ctcDatabase: CTCDatabase) {

    fun isTrialOpen(trialConfig: TrialDefinitionConfig): Boolean? {
        if (!trialConfig.trialId.startsWith(CTC_TRIAL_PREFIX)) {
            LOGGER.debug(
                " Skipping study status retrieval for {} ({}) since study is not deemed a CTC trial",
                trialConfig.trialId,
                trialConfig.acronym
            )

            return null
        }

        val openInCTC: Boolean? = TrialStatusInterpreter.isOpen(ctcDatabase.entries, trialConfig.trialId)
        if (openInCTC != null) {
            if (trialConfig.open != null) {
                LOGGER.warn(
                    " Trial has a manually configured open status while status could be derived from CTC for {} ({})."
                            + " Taking CTC study status where open = {}", trialConfig.trialId, trialConfig.acronym, openInCTC
                )
            }
            return openInCTC
        }

        LOGGER.warn(
            " No study status found in CTC for trial '{} ({})'.",
            trialConfig.trialId,
            trialConfig.acronym
        )
        return null
    }

    fun resolveCohortMetadata(cohortConfig: CohortDefinitionConfig): CohortMetadata {
        val interpretedCohortStatus = CohortStatusInterpreter.interpret(ctcDatabase.entries, cohortConfig) ?: fromCohortConfig(cohortConfig)
        return ImmutableCohortMetadata.builder()
            .cohortId(cohortConfig.cohortId)
            .evaluable(cohortConfig.evaluable)
            .open(interpretedCohortStatus.open)
            .slotsAvailable(interpretedCohortStatus.slotsAvailable)
            .blacklist(cohortConfig.blacklist)
            .description(cohortConfig.description)
            .build()
    }

    fun checkModelForNewTrials(trialConfigs: List<TrialDefinitionConfig>) {
        val newTrialsInCTC = extractNewCTCStudyMETCs(trialConfigs)

        if (newTrialsInCTC.isEmpty()) {
            LOGGER.info(" No new studies found in CTC database that are not explicitly ignored.")
        } else {
            for (newTrialInCTC in newTrialsInCTC) {
                LOGGER.warn(" New trial detected in CTC that is not configured to be ignored: '{}'", newTrialInCTC)
            }
        }
    }

    internal fun extractNewCTCStudyMETCs(trialConfigs: List<TrialDefinitionConfig>): Set<String> {
        val configuredTrialIds = trialConfigs.map { it.trialId }

        return ctcDatabase.entries.filter { !ctcDatabase.studyMETCsToIgnore.contains(it.studyMETC) }
            .filter { !configuredTrialIds.contains(constructTrialId(it)) }
            .map { it.studyMETC }
            .toSet()
    }

    fun checkModelForNewCohorts(cohortConfigs: List<CohortDefinitionConfig>) {
        val newCohortEntriesInCTC = extractNewCTCCohorts(cohortConfigs)

        if (newCohortEntriesInCTC.isEmpty()) {
            LOGGER.info(" No new cohorts found in CTC database that are not explicitly unmapped.")
        } else {
            for (newCohortInCTC in newCohortEntriesInCTC) {
                LOGGER.warn(
                    " New cohort detected in CTC that is not configured as unmapped: '{}: {} (ID={})'", newCohortInCTC.studyMETC,
                    newCohortInCTC.cohortName, newCohortInCTC.cohortId
                )
            }
        }
    }

    internal fun extractNewCTCCohorts(cohortConfigs: List<CohortDefinitionConfig>): Set<CTCDatabaseEntry> {
        val configuredTrialIds = cohortConfigs.map { it.trialId }.toSet()

        val configuredCohortIds =
            cohortConfigs.flatMap(CohortDefinitionConfig::ctcCohortIds).mapNotNull(String::toIntOrNull)

        val childrenPerParent =
            ctcDatabase.entries.filter { it.cohortParentId != null }
                .groupBy({ it.cohortParentId }, { it.cohortId })

        return ctcDatabase.entries.asSequence().filter { configuredTrialIds.contains(constructTrialId(it)) }
            .filter { !ctcDatabase.studyMETCsToIgnore.contains(it.studyMETC) }
            .filter { it.cohortId != null }
            .filter { !ctcDatabase.unmappedCohortIds.contains(it.cohortId) }
            .filter { !configuredCohortIds.contains(it.cohortId) }
            .filter { childrenPerParent[it.cohortId] == null || !childrenPerParent[it.cohortId]!!.containsAll(configuredCohortIds) }
            .toSet()
    }

    companion object {
        private val LOGGER = LogManager.getLogger(CTCModel::class.java)
        const val CTC_TRIAL_PREFIX = "MEC"

        fun constructTrialId(entry: CTCDatabaseEntry): String {
            return CTC_TRIAL_PREFIX + " " + entry.studyMETC
        }

        private fun fromCohortConfig(cohortConfig: CohortDefinitionConfig): InterpretedCohortStatus {
            return if (cohortConfig.open == null || cohortConfig.slotsAvailable == null) {
                LOGGER.warn(
                    " Missing open and/or slots available data for cohort '{}' of trial '{}}'. "
                            + "Assuming cohort is closed with no slots available", cohortConfig.cohortId, cohortConfig.trialId
                )
                InterpretedCohortStatus(open = false, slotsAvailable = false)
            } else {
                InterpretedCohortStatus(open = cohortConfig.open, slotsAvailable = cohortConfig.slotsAvailable)
            }
        }
    }
}