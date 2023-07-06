package com.hartwig.actin.treatment.ctc

import com.hartwig.actin.treatment.ctc.config.CTCDatabase
import com.hartwig.actin.treatment.ctc.config.CTCDatabaseEntry
import com.hartwig.actin.treatment.ctc.config.CTCDatabaseReader
import com.hartwig.actin.treatment.datamodel.CohortMetadata
import com.hartwig.actin.treatment.datamodel.ImmutableCohortMetadata
import com.hartwig.actin.treatment.trial.config.CohortDefinitionConfig
import com.hartwig.actin.treatment.trial.config.TrialDefinitionConfig
import org.apache.logging.log4j.LogManager
import java.io.IOException

class CTCModel internal constructor(private val ctcDatabase: CTCDatabase) {

    fun isTrialOpen(trialConfig: TrialDefinitionConfig): Boolean {
        if (!trialConfig.trialId.startsWith(CTC_TRIAL_PREFIX)) {
            LOGGER.debug(
                " Skipping study status retrieval for {} ({}) since study is not deemed a CTC trial",
                trialConfig.trialId,
                trialConfig.acronym
            )
            return trialConfig.open
        }

        val openInCTC: Boolean? = TrialStatusInterpreter.isOpen(ctcDatabase.entries, trialConfig)
        if (openInCTC != null) {
            if (openInCTC != trialConfig.open) {
                LOGGER.warn(
                    " CTC and internal trial config are inconsistent in terms of study status for {} ({})."
                            + " Taking CTC study status where open = {}", trialConfig.trialId, trialConfig.acronym, openInCTC
                )
            }
            return openInCTC
        }

        LOGGER.warn(
            " No study status found in CTC for trial '{} ({}'). Reverting to internal trial config",
            trialConfig.trialId,
            trialConfig.acronym
        )
        return trialConfig.open
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
            .filter { !configuredTrialIds.contains(extractTrialId(it)) }
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

        val configuredCohortIds: List<Int> =
            cohortConfigs.filter { isExclusivelyNumeric(it.ctcCohortIds) }.map { it -> it.ctcCohortIds.map { it.toInt() } }.flatten()

        val childrenPerParent =
            ctcDatabase.entries.filter { it.cohortParentId != null }
                .groupBy({ it.cohortParentId }, { it.cohortId })

        return ctcDatabase.entries.asSequence().filter { configuredTrialIds.contains(extractTrialId(it)) }
            .filter { !ctcDatabase.studyMETCsToIgnore.contains(it.studyMETC) }
            .filter { it.cohortId != null }
            .filter { !ctcDatabase.unmappedCohortIds.contains(it.cohortId) }
            .filter { !configuredCohortIds.contains(it.cohortId) }
            .filter { childrenPerParent[it.cohortId] == null || !childrenPerParent[it.cohortId]!!.containsAll(configuredCohortIds) }
            .toSet()
    }

    private fun isExclusivelyNumeric(ctcCohortIds: Set<String>): Boolean {
        return ctcCohortIds.isNotEmpty() && ctcCohortIds.all { it.toIntOrNull() != null }
    }

    fun evaluateModelConfiguration() {
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
        val ctcStudyMETCs = ctcDatabase.entries.map { it.studyMETC }.distinct()

        return ctcDatabase.studyMETCsToIgnore.filter { !ctcStudyMETCs.contains(it) }
    }

    fun extractUnusedUnmappedCohorts(): List<Int> {
        val ctcCohortIds = ctcDatabase.entries.mapNotNull { it.cohortId }.distinct()

        return ctcDatabase.unmappedCohortIds.filter { !ctcCohortIds.contains(it) }
    }

    companion object {
        private val LOGGER = LogManager.getLogger(CTCModel::class.java)
        const val CTC_TRIAL_PREFIX = "MEC"

        @Throws(IOException::class)
        fun createFromCTCConfigDirectory(ctcConfigDirectory: String): CTCModel {
            return CTCModel(CTCDatabaseReader.read(ctcConfigDirectory))
        }

        fun extractTrialId(entry: CTCDatabaseEntry): String {
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