package com.hartwig.actin.trial.status

import com.hartwig.actin.trial.config.CohortDefinitionConfig
import com.hartwig.actin.trial.config.TrialDefinitionConfig

class TrialStatusDatabaseExtractor(
    private val trialStatusDatabase: TrialStatusDatabase,
    private val trialPrefix: String? = null
) {
    fun extractUnusedStudyMETCsToIgnore(): List<String> {
        val trialStatusStudyMETCs = trialStatusDatabase.entries.map { it.nctId }.toSet()
        return trialStatusDatabase.studyMETCsToIgnore.filter { !trialStatusStudyMETCs.contains(it) }
    }

    fun extractUnusedUnmappedCohorts(): List<String> {
        val trialStatusCohortIds = trialStatusDatabase.entries.map { it.cohortId }.toSet()
        return trialStatusDatabase.unmappedCohortIds.filter { !trialStatusCohortIds.contains(it) }
    }

    fun extractUnusedStudiesNotInTrialStatusDatabase(trialConfigs: List<TrialDefinitionConfig>): List<String> {
        val trialConfigIds = trialConfigs.map { it.trialId }.toSet()
        return trialStatusDatabase.studiesNotInTrialStatusDatabase.filter { !trialConfigIds.contains(it) }
    }

    fun extractNewTrialStatusDatabaseStudies(trialConfigs: List<TrialDefinitionConfig>): Set<CohortStatusEntry> {
        val configuredTrialIds = trialConfigs.map { it.trialId }

        return trialStatusDatabase.entries.filter { !trialStatusDatabase.studyMETCsToIgnore.contains(it.nctId) }
            .filter { !configuredTrialIds.contains(constructTrialId(it)) }
            .toSet()
    }

    fun extractNewTrialStatusDatabaseCohorts(cohortConfigs: List<CohortDefinitionConfig>): Set<CohortStatusEntry> {
        val configuredTrialIds = cohortConfigs.map { it.trialId }.toSet()
        val configuredCohortIds = cohortConfigs.flatMap(CohortDefinitionConfig::externalCohortIds)

        val childrenPerParent =
            trialStatusDatabase.entries.filter { it.cohortParentId != null }
                .groupBy({ it.cohortParentId }, { it.cohortId })

        return trialStatusDatabase.entries.asSequence()
            .filter { configuredTrialIds.contains(constructTrialId(it)) }
            .filter { !trialStatusDatabase.studyMETCsToIgnore.contains(it.nctId) }
            .filter { !trialStatusDatabase.unmappedCohortIds.contains(it.cohortId) }
            .filter { !configuredCohortIds.contains(it.cohortId) }
            .filter { childrenPerParent[it.cohortId] == null || !childrenPerParent[it.cohortId]!!.containsAll(configuredCohortIds) }
            .toSet()
    }


    fun constructTrialId(entry: CohortStatusEntry): String {
        return trialPrefix?.let { "$it ${entry.nctId}" } ?: entry.nctId
    }
}