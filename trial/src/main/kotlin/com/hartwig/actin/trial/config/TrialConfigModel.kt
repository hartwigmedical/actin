package com.hartwig.actin.trial.config

import com.hartwig.actin.trial.TrialDatabaseValidation
import com.hartwig.actin.trial.interpretation.EligibilityFactory

class TrialConfigModel(
    private val trialDefinitionConfigs: List<TrialDefinitionConfig>,
    private val cohortsByTrialId: Map<String, List<CohortDefinitionConfig>>,
    private val generalInclusionCriteriaByTrial: Map<String, List<InclusionCriteriaConfig>>,
    private val specificInclusionCriteriaByTrialAndCohort: Map<String, Map<String, List<InclusionCriteriaConfig>>>,
    private val referencesByTrialAndId: Map<String, Map<String, InclusionCriteriaReferenceConfig>>,
    private val trialDatabaseValidation: TrialDatabaseValidation
) {

    fun validation(): TrialDatabaseValidation {
        return trialDatabaseValidation
    }

    fun trials(): List<TrialDefinitionConfig> {
        return trialDefinitionConfigs
    }

    fun cohorts(): List<CohortDefinitionConfig> {
        return cohortsByTrialId.values.flatten()
    }

    fun cohortsForTrial(trialId: String): List<CohortDefinitionConfig> {
        return cohortsByTrialId[trialId] ?: emptyList()
    }

    fun generalInclusionCriteriaForTrial(trialId: String): List<InclusionCriteriaConfig> {
        return generalInclusionCriteriaByTrial[trialId] ?: emptyList()
    }

    fun specificInclusionCriteriaForCohort(trialId: String, cohortId: String): List<InclusionCriteriaConfig> {
        return specificInclusionCriteriaByTrialAndCohort[trialId]?.get(cohortId) ?: emptyList()
    }

    fun referencesForTrial(trialId: String): Map<String, InclusionCriteriaReferenceConfig> {
        return referencesByTrialAndId[trialId] ?: emptyMap()
    }

    companion object {

        fun create(trialConfigDirectory: String, eligibilityFactory: EligibilityFactory): TrialConfigModel {
            val database = TrialConfigDatabaseReader.read(trialConfigDirectory)
            return createFromDatabase(database, TrialConfigDatabaseValidator(eligibilityFactory))
        }

        fun createFromDatabase(
            database: TrialConfigDatabase,
            trialConfigDatabaseValidator: TrialConfigDatabaseValidator
        ): TrialConfigModel {
            val specificInclusionCriteriaByTrialAndCohort =
                configsByTrial(database.inclusionCriteriaConfigs.filter { it.appliesToCohorts.isNotEmpty() }).mapValues { configsByTrial ->
                    configsByTrial.value.flatMap { config: InclusionCriteriaConfig ->
                        config.appliesToCohorts.map { cohortId -> Pair(cohortId, config) }
                    }
                        .groupBy({ it.first }, { it.second })
                }

            val referencesByTrialAndId = configsByTrial(database.inclusionCriteriaReferenceConfigs)
                .mapValues { it.value.associateBy(InclusionCriteriaReferenceConfig::referenceId) }

            return TrialConfigModel(
                database.trialDefinitionConfigs,
                configsByTrial(database.cohortDefinitionConfigs),
                configsByTrial(database.inclusionCriteriaConfigs.filter { it.appliesToCohorts.isEmpty() }),
                specificInclusionCriteriaByTrialAndCohort,
                referencesByTrialAndId,
                trialConfigDatabaseValidator.validate(database)
            )
        }

        private fun <T : TrialConfig> configsByTrial(configs: List<T>): Map<String, List<T>> {
            return configs.groupBy(TrialConfig::trialId)
        }
    }
}