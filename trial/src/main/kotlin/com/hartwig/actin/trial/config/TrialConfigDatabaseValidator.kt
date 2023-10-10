package com.hartwig.actin.trial.config

import com.hartwig.actin.treatment.serialization.TrialJson
import com.hartwig.actin.trial.interpretation.EligibilityFactory
import org.apache.logging.log4j.LogManager

class TrialConfigDatabaseValidator(private val eligibilityFactory: EligibilityFactory) {

    fun isValid(database: TrialConfigDatabase): Boolean {
        val trialIds = extractTrialIds(database.trialDefinitionConfigs)
        val validTrials = validateTrials(database.trialDefinitionConfigs)
        val validCohorts = validateCohorts(trialIds, database.cohortDefinitionConfigs)
        val cohortIdsPerTrial = extractCohortIdsPerTrial(trialIds, database.cohortDefinitionConfigs)
        val validInclusionCriteria = validateInclusionCriteria(trialIds, cohortIdsPerTrial, database.inclusionCriteriaConfigs)
        val validInclusionCriteriaReferences = validateInclusionCriteriaReferences(
            trialIds,
            database.inclusionCriteriaConfigs,
            database.inclusionCriteriaReferenceConfigs
        )
        return validTrials && validCohorts && validInclusionCriteria && validInclusionCriteriaReferences
    }

    private fun validateInclusionCriteria(
        trialIds: Set<String>, cohortIdsPerTrial: Map<String, Set<String>>,
        inclusionCriteria: List<InclusionCriteriaConfig>
    ): Boolean {
        if (inclusionCriteria.isEmpty()) {
            return true
        }
        val (allCriteriaWithNonExistentTrial, allNonExistentCohorts, allInvalidInclusionCriteria) = inclusionCriteria.map { criterion ->
            val trialExists = trialIds.contains(criterion.trialId)
            val criterionWithNonExistentTrial = if (!trialExists) setOf(criterion) else emptySet()

            val nonExistentCohorts: Set<Pair<InclusionCriteriaConfig, String>> = if (!trialExists) emptySet() else {
                (criterion.appliesToCohorts - cohortIdsPerTrial[criterion.trialId]!!).map { Pair(criterion, it) }.toSet()
            }
            val invalidInclusionCriteria = if (eligibilityFactory.isValidInclusionCriterion(criterion.inclusionRule)) emptySet() else {
                setOf(criterion)
            }
            Triple(criterionWithNonExistentTrial, nonExistentCohorts, invalidInclusionCriteria)
        }.reduce { (allTrials, allCohorts, allCriteria), (trial, cohorts, criteria) ->
            Triple(
                allTrials + trial,
                allCohorts + cohorts, allCriteria + criteria
            )
        }

        allCriteriaWithNonExistentTrial.forEach { criterion ->
            LOGGER.warn("Inclusion criterion '{}' defined on non-existing trial: '{}'", criterion.inclusionRule, criterion.trialId)
        }
        allNonExistentCohorts.forEach { (criterion, cohortId) ->
            LOGGER.warn("Inclusion criterion '{}' defined on non-existing cohort: '{}'", criterion.inclusionRule, cohortId)
        }
        allInvalidInclusionCriteria.forEach { criterion ->
            LOGGER.warn("Not a valid inclusion criterion for trial '{}': '{}'", criterion.trialId, criterion.inclusionRule)
        }
        return allCriteriaWithNonExistentTrial.isEmpty() && allNonExistentCohorts.isEmpty() && allInvalidInclusionCriteria.isEmpty()
    }

    companion object {
        private val LOGGER = LogManager.getLogger(TrialConfigDatabaseValidator::class.java)

        private fun extractTrialIds(configs: List<TrialDefinitionConfig>): Set<String> {
            return configs.map(TrialDefinitionConfig::trialId).toSet()
        }

        private fun validateTrials(trialDefinitions: List<TrialDefinitionConfig>): Boolean {
            return validateNoDuplicates(trialDefinitions, TrialDefinitionConfig::trialId, "trial ID") &&
                    validateNoDuplicates(trialDefinitions, { trialDef -> TrialJson.trialFileId(trialDef.trialId) }, "trial file ID")
        }

        private fun validateCohorts(trialIds: Set<String>, cohortDefinitions: List<CohortDefinitionConfig>): Boolean {
            val cohortsWithNonExistentTrial = cohortDefinitions.filterNot { trialIds.contains(it.trialId) }
            cohortsWithNonExistentTrial.forEach {
                LOGGER.warn("Cohort '${it.cohortId}' defined on non-existing trial: '${it.trialId}'")
            }

            val cohortIdsAreUniqueByTrial = cohortDefinitions.groupBy(CohortDefinitionConfig::trialId)
                .all { validateNoDuplicates(it.value, CohortDefinitionConfig::cohortId, "cohort ID for trial '${it.key}") }

            return cohortsWithNonExistentTrial.isEmpty() && cohortIdsAreUniqueByTrial
        }

        private fun <T> validateNoDuplicates(collection: Collection<T>, extractKey: (T) -> String, keyName: String): Boolean {
            val duplicateKeys = collection.groupBy(extractKey).filter { it.value.size > 1 }.keys
            duplicateKeys.forEach { LOGGER.warn("Duplicate $keyName found: '$it'") }
            return duplicateKeys.isEmpty()
        }

        private fun extractCohortIdsPerTrial(
            trialIds: Set<String>,
            cohortDefinitions: List<CohortDefinitionConfig>
        ): Map<String, Set<String>> {
            val cohortIdsByTrial = cohortDefinitions.filter { it.trialId in trialIds }
                .groupBy(CohortDefinitionConfig::trialId, CohortDefinitionConfig::cohortId)
                .mapValues { it.value.toSet() }
            return trialIds.associateWith { emptySet<String>() } + cohortIdsByTrial
        }

        private fun validateInclusionCriteriaReferences(
            trialIds: Set<String>,
            inclusionCriteriaConfigs: List<InclusionCriteriaConfig>,
            inclusionCriteriaReferenceConfigs: List<InclusionCriteriaReferenceConfig>
        ): Boolean {
            val referenceConfigsWithNonExistentTrials = inclusionCriteriaReferenceConfigs.filterNot { trialIds.contains(it.trialId) }
            referenceConfigsWithNonExistentTrials.forEach {
                LOGGER.warn("Reference '${it.referenceId}' defined on non-existing trial: '${it.trialId}'")
            }

            val referenceIdsAreUniqueByTrial = inclusionCriteriaReferenceConfigs.groupBy(InclusionCriteriaReferenceConfig::trialId)
                .all { validateNoDuplicates(it.value, InclusionCriteriaReferenceConfig::referenceId, "reference ID for trial '${it.key}") }

            val criteriaPerTrial = buildMapPerTrial(inclusionCriteriaConfigs)
            val referenceIdsPerTrial = buildMapPerTrial(inclusionCriteriaReferenceConfigs)
                .mapValues { it.value.map(InclusionCriteriaReferenceConfig::referenceId).toSet() }

            val undefinedReferencesWithTrialId = trialIds.filter { it in referenceIdsPerTrial && it in criteriaPerTrial }
                .flatMap { trialId ->
                    criteriaPerTrial[trialId]!!.flatMap(InclusionCriteriaConfig::referenceIds)
                        .filterNot { referenceIdsPerTrial[trialId]!!.contains(it) }
                        .map { Pair(trialId, it) }
                }
            undefinedReferencesWithTrialId.forEach { (trialId, referenceId) ->
                LOGGER.warn("Undefined reference ID on trial '{}': '{}'", trialId, referenceId)
            }

            return referenceConfigsWithNonExistentTrials.isEmpty() && referenceIdsAreUniqueByTrial && undefinedReferencesWithTrialId.isEmpty()
        }

        private fun <T : TrialConfig> buildMapPerTrial(configs: List<T>): Map<String, List<T>> {
            return configs.groupBy(TrialConfig::trialId)
        }
    }
}