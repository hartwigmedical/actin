package com.hartwig.actin.treatment.ctc

import com.google.common.annotations.VisibleForTesting
import com.hartwig.actin.treatment.ctc.config.CTCDatabase
import com.hartwig.actin.treatment.ctc.config.CTCDatabaseEntry
import com.hartwig.actin.treatment.ctc.config.CTCDatabaseReader
import com.hartwig.actin.treatment.datamodel.CohortMetadata
import com.hartwig.actin.treatment.datamodel.ImmutableCohortMetadata
import com.hartwig.actin.treatment.trial.config.CohortDefinitionConfig
import com.hartwig.actin.treatment.trial.config.TrialDefinitionConfig
import org.apache.logging.log4j.LogManager
import java.io.IOException
import kotlin.streams.toList

class CTCModel @VisibleForTesting internal constructor(private val ctcDatabase: CTCDatabase) {

    fun checkDatabaseForNewTrials(trialConfigs: List<TrialDefinitionConfig>) {
        val configuredTrialIds = trialConfigs.map { it.trialId }

        val newTrialsInCTC =
            ctcDatabase.entries.filter { !ctcDatabase.studyMETCsToIgnore.contains(it.studyMETC) }
                .filter { configuredTrialIds.contains(extractTrialId(it)) }
                .distinct()

        if (newTrialsInCTC.isEmpty()) {
            LOGGER.info(" No new studies found in CTC database that are not explicitly ignored.")
        } else {
            for (newTrialInCTC in newTrialsInCTC) {
                LOGGER.warn(" New trial detected in CTC that is not configured to be ignored: '{}'", newTrialInCTC)
            }
        }

        val ctcStudyMETCs = ctcDatabase.entries.stream().map { it.studyMETC }.distinct().toList()
        val unusedStudyMETCsToIgnore = ctcDatabase.studyMETCsToIgnore.stream().filter { ctcStudyMETCs.contains(it) }.distinct().toList()

        for (unusedStudyMETCToIgnore in unusedStudyMETCsToIgnore) {
            LOGGER.warn(" Study that is configured to be ignored is not actually referenced in CTC database: '{}'", unusedStudyMETCToIgnore)
        }
    }

    fun isTrialOpen(trialConfig: TrialDefinitionConfig): Boolean {
        if (!trialConfig.trialId.startsWith(CTC_TRIAL_PREFIX)) {
            LOGGER.debug(
                " Skipping study status retrieval for {} ({}) since study is not deemed a CTC trial",
                trialConfig.trialId,
                trialConfig.acronym
            )
            return trialConfig.open
        }

        val openInCTC = TrialStatusInterpreter.isOpen(ctcDatabase.entries, trialConfig)
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
            " No study status found in CTC for trial {} ({}). Reverting to internal trial config",
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
                    " Missing open and/or slots available data for cohort '{}' of trial '{}'. "
                            + "Assuming cohort is closed with no slots available", cohortConfig.cohortId, cohortConfig.trialId
                )
                InterpretedCohortStatus(open = false, slotsAvailable = false)
            } else {
                InterpretedCohortStatus(open = cohortConfig.open, slotsAvailable = cohortConfig.slotsAvailable)
            }
        }
    }
}