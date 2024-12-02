package com.hartwig.actin.trial.config

import com.hartwig.actin.datamodel.trial.EligibilityRule
import com.hartwig.actin.datamodel.trial.TrialPhase
import com.hartwig.actin.trial.interpretation.EligibilityFactory
import com.hartwig.actin.trial.serialization.TrialJson

class TrialConfigDatabaseValidator(private val eligibilityFactory: EligibilityFactory) {

    fun validate(database: TrialConfigDatabase): TrialConfigDatabaseValidation {
        val trialIds = extractTrialIds(database.trialDefinitionConfigs)
        return TrialConfigDatabaseValidation(
            trialDefinitionValidationErrors = validateTrials(database.trialDefinitionConfigs),
            cohortDefinitionValidationErrors = validateCohorts(trialIds, database.cohortDefinitionConfigs),
            inclusionCriteriaValidationErrors = validateInclusionCriteria(
                trialIds,
                extractCohortWithEvaluablePerTrial(trialIds, database.cohortDefinitionConfigs),
                database.inclusionCriteriaConfigs,
                database.inclusionCriteriaReferenceConfigs
            ),
            inclusionCriteriaReferenceValidationErrors = validateInclusionCriteriaReferences(
                trialIds, database.inclusionCriteriaReferenceConfigs
            ),
            unusedRulesToKeepValidationErrors = validateRulesToKeep(database.unusedRulesToKeep)
        )
    }

    private fun anyCohortEvaluable(cohortIds: Set<String>, allCohorts: Map<String, Boolean>): Boolean = when {
        cohortIds.isEmpty() -> allCohorts.values.any { it }
        else -> cohortIds.any { cohortId ->
            allCohorts[cohortId] ?: false
        }
    }

    private fun validateInclusionCriteria(
        trialIds: Set<String>,
        cohortsPerTrial: Map<String, Map<String, Boolean>>,
        inclusionCriteria: List<InclusionCriteriaConfig>,
        inclusionCriteriaReferenceConfigs: List<InclusionCriteriaReferenceConfig>
    ): Set<InclusionCriteriaValidationError> {
        if (inclusionCriteria.isEmpty()) {
            return emptySet()
        }
        val (allCriteriaWithNonExistentTrial, allNonExistentCohorts, allInvalidInclusionCriteria) = inclusionCriteria.map { criterion ->
            val trialExists = trialIds.contains(criterion.trialId)
            val criterionWithNonExistentTrial = if (!trialExists) setOf(criterion) else emptySet()

            val nonExistentCohorts: Set<Pair<InclusionCriteriaConfig, String>> = if (!trialExists) emptySet() else {
                (criterion.appliesToCohorts - cohortsPerTrial[criterion.trialId]!!.keys).map { Pair(criterion, it) }
                    .toSet()
            }

            val validateCriteria = criterionWithNonExistentTrial.isNotEmpty()
                    || (nonExistentCohorts.size == criterion.appliesToCohorts.size && criterion.appliesToCohorts.isNotEmpty())
                    || anyCohortEvaluable(criterion.appliesToCohorts, cohortsPerTrial[criterion.trialId]!!)

            val invalidInclusionCriteria =
                criterion.takeIf { validateCriteria && !eligibilityFactory.isValidInclusionCriterion(it.inclusionRule) }?.let { setOf(it) }
                    ?: emptySet()

            Triple(criterionWithNonExistentTrial, nonExistentCohorts, invalidInclusionCriteria)
        }.reduce { (allTrials, allCohorts, allCriteria), (trial, cohorts, criteria) ->
            Triple(
                allTrials + trial, allCohorts + cohorts, allCriteria + criteria
            )
        }

        val criteriaPerTrial = buildMapPerTrial(inclusionCriteria)
        val referenceIdsPerTrial = buildMapPerTrial(inclusionCriteriaReferenceConfigs).mapValues {
            it.value.map(InclusionCriteriaReferenceConfig::referenceId).toSet()
        }

        val allCriteriaWithNonExistentTrialErrors = allCriteriaWithNonExistentTrial.map { criterion ->
            InclusionCriteriaValidationError(
                config = criterion, message = "Inclusion criterion defined on non-existing trial"
            )
        }
        val allNonExistentCohortsErrors = allNonExistentCohorts.map { (criterion, cohortId) ->
            InclusionCriteriaValidationError(
                config = criterion, message = "Inclusion criterion defined on non-existing cohort '$cohortId'"
            )
        }
        val allInvalidInclusionCriteriaErrors = allInvalidInclusionCriteria.map { criterion ->
            InclusionCriteriaValidationError(
                config = criterion, message = "Not a valid inclusion criterion for trial"
            )
        }
        val trialsWithUndefinedReferenceIdErrors =
            trialIds.filter { it in referenceIdsPerTrial && it in criteriaPerTrial }.flatMap { trialId ->
                criteriaPerTrial[trialId]!!.flatMap(InclusionCriteriaConfig::referenceIds)
                    .filterNot { referenceIdsPerTrial[trialId]!!.contains(it) }.map { Triple(trialId, it, criteriaPerTrial[trialId]!!) }
            }.flatMap { (trialId, referenceId, configs) ->
                configs.map { InclusionCriteriaValidationError(it, "Undefined reference ID on trial '$trialId': '$referenceId'") }
            }
        return (allCriteriaWithNonExistentTrialErrors +
                allNonExistentCohortsErrors +
                allInvalidInclusionCriteriaErrors +
                trialsWithUndefinedReferenceIdErrors
                ).toSet()
    }


    private fun extractTrialIds(configs: List<TrialDefinitionConfig>): Set<String> {
        return configs.map(TrialDefinitionConfig::trialId).toSet()
    }

    private fun validateTrials(trialDefinitions: List<TrialDefinitionConfig>): Set<TrialDefinitionValidationError> {
        val duplicatedTrialIds = duplicatedConfigsByKey(trialDefinitions, TrialDefinitionConfig::trialId)
            .map {
                TrialDefinitionValidationError(it.second, "Duplicated trial id of ${it.first}")
            }

        val duplicatedTrialFileIds = duplicatedConfigsByKey(trialDefinitions) { trialDef -> TrialJson.trialFileId(trialDef.trialId) }.map {
            TrialDefinitionValidationError(
                it.second, "Duplicated trial file id of ${it.first}"
            )
        }

        val invalidPhases = trialDefinitions.filter { it.phase != null && TrialPhase.fromString(it.phase) == null }
            .map { TrialDefinitionValidationError(it, "Invalid phase: '${it.phase}'") }

        val invalidLocations = trialDefinitions.filter { !TrialConfigDatabaseUtil.validateTrialLocation(it.location) }
            .map { TrialDefinitionValidationError(it, "Invalid Location: '${it.location}'") }

        return (duplicatedTrialIds + duplicatedTrialFileIds + invalidPhases + invalidLocations).toSet()
    }

    private fun validateCohorts(
        trialIds: Set<String>, cohortDefinitions: List<CohortDefinitionConfig>
    ): Set<CohortDefinitionValidationError> {
        val duplicatedCohortErrors = cohortDefinitions.groupBy(CohortDefinitionConfig::trialId)
            .flatMap { duplicatedConfigsByKey(it.value, CohortDefinitionConfig::cohortId) }
            .map { CohortDefinitionValidationError(it.second, "Cohort '${it.second.cohortId}' is duplicated.") }

        val nonExistingTrialErrors = cohortDefinitions.filterNot { trialIds.contains(it.trialId) }
            .map { CohortDefinitionValidationError(it, "Cohort '${it.cohortId}' defined on non-existing trial: '${it.trialId}'") }

        return (duplicatedCohortErrors + nonExistingTrialErrors).toSet()
    }

    private fun <T> duplicatedConfigsByKey(allConfigs: Collection<T>, extractKey: (T) -> String): List<Pair<String, T>> {
        return allConfigs.groupBy(extractKey)
            .filter { it.value.size > 1 }
            .entries.flatMap { (key, configs) -> configs.map { Pair(key, it) } }
    }

    private fun extractCohortWithEvaluablePerTrial(
        trialIds: Set<String>, cohortDefinitions: List<CohortDefinitionConfig>
    ): Map<String, Map<String, Boolean>> {
        val cohortIdsByTrial = cohortDefinitions
            .filter { it.trialId in trialIds }
            .groupBy(CohortDefinitionConfig::trialId) { it.cohortId to it.evaluable }
            .mapValues { it.value.toMap() }
        return trialIds.associateWith { emptyMap<String, Boolean>() } + cohortIdsByTrial
    }

    private fun validateInclusionCriteriaReferences(
        trialIds: Set<String>, inclusionCriteriaReferenceConfigs: List<InclusionCriteriaReferenceConfig>
    ): Set<InclusionCriteriaReferenceValidationError> {
        val referenceConfigsWithNonExistentTrials = inclusionCriteriaReferenceConfigs.filterNot { trialIds.contains(it.trialId) }

        val duplicatedReferenceIdByTrialErrors = inclusionCriteriaReferenceConfigs.groupBy(InclusionCriteriaReferenceConfig::trialId)
            .flatMap { duplicatedConfigsByKey(it.value, InclusionCriteriaReferenceConfig::referenceId) }
            .map { InclusionCriteriaReferenceValidationError(it.second, "Reference ID for trial '${it.second}") }

        val nonExistingTrialErrors = referenceConfigsWithNonExistentTrials.map {
            InclusionCriteriaReferenceValidationError(it, "Reference '${it.referenceId}' defined on non-existing trial: '${it.trialId}'")
        }
        return (duplicatedReferenceIdByTrialErrors + nonExistingTrialErrors).toSet()
    }

    private fun <T : TrialConfig> buildMapPerTrial(configs: List<T>): Map<String, List<T>> {
        return configs.groupBy(TrialConfig::trialId)
    }

    private fun validateRulesToKeep(ruleNames: List<String>): Set<UnusedRulesToKeepValidationError> {
        return ruleNames.mapNotNull { rule ->
            val trimmed = rule.trim()
            try {
                EligibilityRule.valueOf(trimmed)
                null
            } catch (exc: IllegalArgumentException) {
                UnusedRulesToKeepValidationError(trimmed)
            }
        }.toSet()
    }
}