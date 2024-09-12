package com.hartwig.actin.trial.config

import com.hartwig.actin.datamodel.trial.EligibilityRule
import com.hartwig.actin.trial.interpretation.EligibilityFactory

class TrialConfigModel(
    private val trialDefinitionConfigs: List<TrialDefinitionConfig>,
    private val cohortsByTrialId: Map<String, List<CohortDefinitionConfig>>,
    private val generalInclusionCriteriaByTrial: Map<String, List<InclusionCriteriaConfig>>,
    private val specificInclusionCriteriaByTrialAndCohort: Map<String, Map<String, List<InclusionCriteriaConfig>>>,
    private val referencesByTrialAndId: Map<String, Map<String, InclusionCriteriaReferenceConfig>>,
    val unusedRulesToKeep: Set<EligibilityRule>,
    private val trialDatabaseValidation: TrialConfigDatabaseValidation
) {

    fun validation(): TrialConfigDatabaseValidation {
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
            database: TrialConfigDatabase, trialConfigDatabaseValidator: TrialConfigDatabaseValidator
        ): TrialConfigModel {
            val specificInclusionCriteriaByTrialAndCohort =
                configsByTrial(database.inclusionCriteriaConfigs.filter { it.appliesToCohorts.isNotEmpty() })
                    .mapValues { inclusionCriteriaConfigsByCohortId(it.value) }

            val referencesByTrialAndId = configsByTrial(database.inclusionCriteriaReferenceConfigs)
                .mapValues { it.value.associateBy(InclusionCriteriaReferenceConfig::referenceId) }

            val validRules = EligibilityRule.values().map(EligibilityRule::toString).toSet()

            return TrialConfigModel(
                database.trialDefinitionConfigs,
                configsByTrial(database.cohortDefinitionConfigs),
                configsByTrial(database.inclusionCriteriaConfigs.filter { it.appliesToCohorts.isEmpty() }),
                specificInclusionCriteriaByTrialAndCohort,
                referencesByTrialAndId,
                database.unusedRulesToKeep.filter(validRules::contains).map(EligibilityRule::valueOf).toSet(),
                trialConfigDatabaseValidator.validate(database)
            )
        }

        private fun inclusionCriteriaConfigsByCohortId(
            inclusionCriteriaConfigs: List<InclusionCriteriaConfig>
        ): Map<String, List<InclusionCriteriaConfig>> {
            return inclusionCriteriaConfigs.flatMap { config -> config.appliesToCohorts.map { Pair(it, config) } }
                .groupBy({ it.first }, { it.second })
        }

        private fun <T : TrialConfig> configsByTrial(configs: List<T>): Map<String, List<T>> {
            return configs.groupBy(TrialConfig::trialId)
        }
    }
}