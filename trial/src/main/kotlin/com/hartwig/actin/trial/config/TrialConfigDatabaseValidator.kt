package com.hartwig.actin.trial.config

import com.hartwig.actin.treatment.serialization.TrialJson
import com.hartwig.actin.trial.CohortDefinitionValidationError
import com.hartwig.actin.trial.InclusionCriteriaValidationError
import com.hartwig.actin.trial.InclusionReferenceValidationError
import com.hartwig.actin.trial.TrialDatabaseValidation
import com.hartwig.actin.trial.TrialDefinitionValidationError
import com.hartwig.actin.trial.interpretation.EligibilityFactory

class TrialConfigDatabaseValidator(private val eligibilityFactory: EligibilityFactory) {

    fun validate(database: TrialConfigDatabase): TrialDatabaseValidation {
        val trialIds = extractTrialIds(database.trialDefinitionConfigs)
        return TrialDatabaseValidation(
            inclusionCriteriaValidationErrors = validateInclusionCriteria(
                trialIds,
                extractCohortIdsPerTrial(trialIds, database.cohortDefinitionConfigs),
                database.inclusionCriteriaConfigs,
                database.inclusionCriteriaReferenceConfigs
            ).toSet(),
            inclusionReferenceValidationErrors = validateInclusionCriteriaReferences(
                trialIds, database.inclusionCriteriaReferenceConfigs
            ).toSet(),
            cohortDefinitionValidationErrors = validateCohorts(trialIds, database.cohortDefinitionConfigs).toSet(),
            trialDefinitionValidationErrors = validateTrials(database.trialDefinitionConfigs).toSet()
        )
    }

    private fun validateInclusionCriteria(
        trialIds: Set<String>,
        cohortIdsPerTrial: Map<String, Set<String>>,
        inclusionCriteria: List<InclusionCriteriaConfig>,
        inclusionCriteriaReferenceConfigs: List<InclusionCriteriaReferenceConfig>
    ): List<InclusionCriteriaValidationError> {
        if (inclusionCriteria.isEmpty()) {
            return emptyList()
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
                allTrials + trial, allCohorts + cohorts, allCriteria + criteria
            )
        }

        val criteriaPerTrial = buildMapPerTrial(inclusionCriteria)
        val referenceIdsPerTrial = buildMapPerTrial(inclusionCriteriaReferenceConfigs).mapValues {
            it.value.map(InclusionCriteriaReferenceConfig::referenceId).toSet()
        }

        return allCriteriaWithNonExistentTrial.map { criterion ->
            InclusionCriteriaValidationError(
                config = criterion, message = "Inclusion criterion defined on non-existing trial"
            )
        } + allNonExistentCohorts.map { (criterion, cohortId) ->
            InclusionCriteriaValidationError(
                config = criterion, message = "Inclusion criterion defined on non-existing cohort '$cohortId'"
            )
        } + allInvalidInclusionCriteria.map { criterion ->
            InclusionCriteriaValidationError(
                config = criterion, message = "Not a valid inclusion criterion for trial"
            )
        } + trialIds.filter { it in referenceIdsPerTrial && it in criteriaPerTrial }.flatMap { trialId ->
            criteriaPerTrial[trialId]!!.flatMap(InclusionCriteriaConfig::referenceIds)
                .filterNot { referenceIdsPerTrial[trialId]!!.contains(it) }.map { Triple(trialId, it, criteriaPerTrial[trialId]!!) }
        }.flatMap { (trialId, referenceId, configs) ->
            configs.map { InclusionCriteriaValidationError(it, "Undefined reference ID on trial '$trialId': '$referenceId'") }
        }
    }


    private fun extractTrialIds(configs: List<TrialDefinitionConfig>): Set<String> {
        return configs.map(TrialDefinitionConfig::trialId).toSet()
    }

    private fun validateTrials(trialDefinitions: List<TrialDefinitionConfig>): List<TrialDefinitionValidationError> {
        val duplicatedTrialIds = validateNoDuplicates(
            trialDefinitions, TrialDefinitionConfig::trialId
        ).map { TrialDefinitionValidationError(it.second, "Duplicated trial id of ${it.first}") }

        val duplicatedTrialFileIds = validateNoDuplicates(
            trialDefinitions,
        ) { trialDef -> TrialJson.trialFileId(trialDef.trialId) }.map {
            TrialDefinitionValidationError(
                it.second, "Duplicated trial file id of ${it.first}"
            )
        }
        return duplicatedTrialIds + duplicatedTrialFileIds
    }

    private fun validateCohorts(
        trialIds: Set<String>, cohortDefinitions: List<CohortDefinitionConfig>
    ): List<CohortDefinitionValidationError> {
        return cohortDefinitions.groupBy(CohortDefinitionConfig::trialId)
            .flatMap { validateNoDuplicates(it.value, CohortDefinitionConfig::cohortId) }.map {
                CohortDefinitionValidationError(
                    it.second, "Cohort '${it.second.cohortId}' is duplicated."
                )
            } + cohortDefinitions.filterNot { trialIds.contains(it.trialId) }.map {
            CohortDefinitionValidationError(it, "Cohort '${it.cohortId}' defined on non-existing trial: '${it.trialId}'")
        }
    }

    private fun <T> validateNoDuplicates(
        collection: Collection<T>,
        extractKey: (T) -> String,
    ): List<Pair<String, T>> {
        return collection.groupBy(extractKey).filter { it.value.size > 1 }.entries.flatMap { it.value.map { config -> it.key to config } }
    }

    private fun extractCohortIdsPerTrial(
        trialIds: Set<String>, cohortDefinitions: List<CohortDefinitionConfig>
    ): Map<String, Set<String>> {
        val cohortIdsByTrial =
            cohortDefinitions.filter { it.trialId in trialIds }.groupBy(CohortDefinitionConfig::trialId, CohortDefinitionConfig::cohortId)
                .mapValues { it.value.toSet() }
        return trialIds.associateWith { emptySet<String>() } + cohortIdsByTrial
    }

    private fun validateInclusionCriteriaReferences(
        trialIds: Set<String>, inclusionCriteriaReferenceConfigs: List<InclusionCriteriaReferenceConfig>
    ): List<InclusionReferenceValidationError> {
        val referenceConfigsWithNonExistentTrials = inclusionCriteriaReferenceConfigs.filterNot { trialIds.contains(it.trialId) }


        val referenceIdsAreUniqueByTrial = inclusionCriteriaReferenceConfigs.groupBy(InclusionCriteriaReferenceConfig::trialId)
            .flatMap { validateNoDuplicates(it.value, InclusionCriteriaReferenceConfig::referenceId) }
            .map { InclusionReferenceValidationError(it.second, "Reference ID for trial '${it.second}") }



        return referenceConfigsWithNonExistentTrials.map {
            InclusionReferenceValidationError(it, "Reference '${it.referenceId}' defined on non-existing trial: '${it.trialId}'")
        } + referenceIdsAreUniqueByTrial
    }

    private fun <T : TrialConfig> buildMapPerTrial(configs: List<T>): Map<String, List<T>> {
        return configs.groupBy(TrialConfig::trialId)
    }
}