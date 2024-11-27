package com.hartwig.actin.trial.status

import com.hartwig.actin.trial.config.CohortDefinitionConfig
import com.hartwig.actin.trial.config.TrialDefinitionConfig

class TrialStatusDatabaseExtractor(
    private val trialStatusDatabase: TrialStatusDatabase
) {

    fun extractNewTrialStatusDatabaseStudies(trialConfigs: List<TrialDefinitionConfig>): Set<CohortStatusEntry> {
        val configuredTrialIds = trialConfigs.map { it.nctId }

        return trialStatusDatabase.entries.filter { !configuredTrialIds.contains(it.nctId) }.toSet()
    }

    fun extractNewTrialStatusDatabaseCohorts(cohortConfigs: List<CohortDefinitionConfig>): Set<CohortStatusEntry> {
        val configuredTrialIds = cohortConfigs.map { it.nctId }.toSet()
        val configuredCohortIds = cohortConfigs.flatMap(CohortDefinitionConfig::externalCohortIds)

        val childrenPerParent =
            trialStatusDatabase.entries.filter { it.cohortParentId != null }
                .groupBy({ it.cohortParentId }, { it.cohortId })

        return trialStatusDatabase.entries.asSequence()
            .filter { configuredTrialIds.contains(it.nctId) }
            .filter { !configuredCohortIds.contains(it.cohortId) }
            .filter { childrenPerParent[it.cohortId] == null || !childrenPerParent[it.cohortId]!!.containsAll(configuredCohortIds) }
            .toSet()
    }
}