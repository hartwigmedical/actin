package com.hartwig.actin.trial.config

import com.hartwig.actin.treatment.serialization.TrialJson
import com.hartwig.actin.trial.CohortDefinitionValidationWarning
import com.hartwig.actin.trial.TrialDefinitionValidationWarning
import com.hartwig.actin.trial.InclusionCriteriaValidationWarning
import com.hartwig.actin.trial.InclusionReferenceValidationWarning
import com.hartwig.actin.trial.TrialDatabaseValidationWarning
import com.hartwig.actin.trial.interpretation.EligibilityFactory
import org.apache.logging.log4j.LogManager

class TrialConfigDatabaseValidator(private val eligibilityFactory: EligibilityFactory) {

    fun validate(database: TrialConfigDatabase): List<TrialDatabaseValidationWarning> {
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
        return validTrials + validCohorts + validInclusionCriteria + validInclusionCriteriaReferences
    }

    private fun validateInclusionCriteria(
        trialIds: Set<String>, cohortIdsPerTrial: Map<String, Set<String>>,
        inclusionCriteria: List<InclusionCriteriaConfig>
    ): List<InclusionCriteriaValidationWarning> {
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
                allTrials + trial,
                allCohorts + cohorts, allCriteria + criteria
            )
        }

        return allCriteriaWithNonExistentTrial.map { criterion ->
            InclusionCriteriaValidationWarning(
                config = criterion,
                message = "Inclusion criterion defined on non-existing trial"
            )
        } +
                allNonExistentCohorts.map { (criterion, cohortId) ->
                    InclusionCriteriaValidationWarning(
                        config = criterion,
                        message = "Inclusion criterion defined on non-existing cohort '$cohortId'"
                    )
                } +
                allInvalidInclusionCriteria.map { criterion ->
                    InclusionCriteriaValidationWarning(
                        config = criterion,
                        message = "Not a valid inclusion criterion for trial"
                    )
                }
    }

    companion object {
        private val LOGGER = LogManager.getLogger(TrialConfigDatabaseValidator::class.java)

        private fun extractTrialIds(configs: List<TrialDefinitionConfig>): Set<String> {
            return configs.map(TrialDefinitionConfig::trialId).toSet()
        }

        private fun validateTrials(trialDefinitions: List<TrialDefinitionConfig>): List<TrialDatabaseValidationWarning> {
            val duplicatedTrialIds = validateNoDuplicates(
                trialDefinitions,
                TrialDefinitionConfig::trialId
            )
                .map { TrialDefinitionValidationWarning(it.second, "Duplicated trial id of ${it.first}") }

            val duplicatedTrialFileIds =
                validateNoDuplicates(
                    trialDefinitions,
                ) { trialDef -> TrialJson.trialFileId(trialDef.trialId) }.map {
                    TrialDefinitionValidationWarning(
                        it.second,
                        "Duplicated trial file id of ${it.first}"
                    )
                }
            return duplicatedTrialIds +
                    duplicatedTrialFileIds
        }

        private fun validateCohorts(
            trialIds: Set<String>,
            cohortDefinitions: List<CohortDefinitionConfig>
        ): List<CohortDefinitionValidationWarning> {
            return cohortDefinitions.groupBy(CohortDefinitionConfig::trialId)
                .flatMap { validateNoDuplicates(it.value, CohortDefinitionConfig::cohortId) }
                .map {
                    CohortDefinitionValidationWarning(
                        it.second,
                        "cohort ID for trial '${it.first}"
                    )
                } + cohortDefinitions.filterNot { trialIds.contains(it.trialId) }.map {
                CohortDefinitionValidationWarning(it, "Cohort '${it.cohortId}' defined on non-existing trial: '${it.trialId}'")
            }
        }

        private fun <T> validateNoDuplicates(
            collection: Collection<T>,
            extractKey: (T) -> String,
        ): List<Pair<String, T>> {
            return collection.groupBy(extractKey)
                .filter { it.value.size > 1 }.entries.flatMap { it.value.map { config -> it.key to config } }
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
        ): List<TrialDatabaseValidationWarning> {
            val referenceConfigsWithNonExistentTrials = inclusionCriteriaReferenceConfigs.filterNot { trialIds.contains(it.trialId) }


            val referenceIdsAreUniqueByTrial = inclusionCriteriaReferenceConfigs.groupBy(InclusionCriteriaReferenceConfig::trialId)
                .flatMap { validateNoDuplicates(it.value, InclusionCriteriaReferenceConfig::referenceId) }
                .map { InclusionReferenceValidationWarning(it.second, "Reference ID for trial '${it.second}") }

            val criteriaPerTrial = buildMapPerTrial(inclusionCriteriaConfigs)
            val referenceIdsPerTrial = buildMapPerTrial(inclusionCriteriaReferenceConfigs)
                .mapValues { it.value.map(InclusionCriteriaReferenceConfig::referenceId).toSet() }

            val undefinedReferencesWithTrialId = trialIds.filter { it in referenceIdsPerTrial && it in criteriaPerTrial }
                .flatMap { trialId ->
                    criteriaPerTrial[trialId]!!.flatMap(InclusionCriteriaConfig::referenceIds)
                        .filterNot { referenceIdsPerTrial[trialId]!!.contains(it) }
                        .map { Triple(trialId, it, criteriaPerTrial[trialId]!!) }
                }
            undefinedReferencesWithTrialId.flatMap { (trialId, referenceId, configs) ->
                configs.map { InclusionCriteriaValidationWarning(it, "Undefined reference ID on trial '$trialId': '$referenceId'") }
            }

            return referenceConfigsWithNonExistentTrials.map {
                InclusionReferenceValidationWarning(it, "Reference '${it.referenceId}' defined on non-existing trial: '${it.trialId}'")
            } + referenceIdsAreUniqueByTrial
        }

        private fun <T : TrialConfig> buildMapPerTrial(configs: List<T>): Map<String, List<T>> {
            return configs.groupBy(TrialConfig::trialId)
        }
    }
}