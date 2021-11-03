package com.hartwig.actin.treatment.trial;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.hartwig.actin.treatment.trial.config.CohortDefinitionConfig;
import com.hartwig.actin.treatment.trial.config.InclusionCriteriaConfig;
import com.hartwig.actin.treatment.trial.config.InclusionCriteriaReferenceConfig;
import com.hartwig.actin.treatment.trial.config.TrialConfig;
import com.hartwig.actin.treatment.trial.config.TrialDefinitionConfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class TrialConfigDatabaseValidator {

    private static final Logger LOGGER = LogManager.getLogger(TrialConfigDatabaseValidator.class);

    private TrialConfigDatabaseValidator() {
    }

    public static boolean isValid(@NotNull TrialConfigDatabase database) {
        Set<String> trialIds = extractTrialIds(database.trialDefinitionConfigs());

        boolean validCohorts = validateCohorts(trialIds, database.cohortDefinitionConfigs());

        Multimap<String, String> cohortIdsPerTrial = extractCohortIdsPerTrial(trialIds, database.cohortDefinitionConfigs());

        boolean validInclusionCriteria = validateInclusionCriteria(cohortIdsPerTrial, database.inclusionCriteriaConfigs());

        boolean validInclusionCriteriaReferences = validateInclusionCriteriaReferences(trialIds,
                database.inclusionCriteriaConfigs(),
                database.inclusionCriteriaReferenceConfigs());

        return validCohorts && validInclusionCriteria && validInclusionCriteriaReferences;
    }

    @NotNull
    private static Set<String> extractTrialIds(@NotNull List<TrialDefinitionConfig> configs) {
        Set<String> trialIds = Sets.newHashSet();
        for (TrialDefinitionConfig config : configs) {
            if (trialIds.contains(config.trialId())) {
                LOGGER.warn("Duplicate trial ID found: {}", config.trialId());
            }
            trialIds.add(config.trialId());
        }
        return trialIds;
    }

    private static boolean validateCohorts(@NotNull Set<String> trialIds, @NotNull List<CohortDefinitionConfig> cohortDefinitions) {
        boolean valid = true;
        for (CohortDefinitionConfig cohortDefinition : cohortDefinitions) {
            if (!trialIds.contains(cohortDefinition.trialId())) {
                LOGGER.warn("Cohort '{}' defined on non-existing trial: {}", cohortDefinition.cohortId(), cohortDefinition.trialId());
                valid = false;
            }
        }
        return valid;
    }

    @NotNull
    private static Multimap<String, String> extractCohortIdsPerTrial(@NotNull Set<String> trialIds,
            @NotNull List<CohortDefinitionConfig> cohortDefinitions) {
        Multimap<String, String> cohortsPerTrial = ArrayListMultimap.create();
        for (String trialId : trialIds) {
            cohortsPerTrial.putAll(trialId, cohortsForTrial(cohortDefinitions, trialId));
        }
        return cohortsPerTrial;
    }

    @NotNull
    private static Iterable<String> cohortsForTrial(@NotNull List<CohortDefinitionConfig> cohortDefinitions, @NotNull String trialId) {
        Set<String> cohortIds = Sets.newHashSet();
        for (CohortDefinitionConfig cohortDefinition : cohortDefinitions) {
            if (cohortDefinition.trialId().equals(trialId)) {
                if (cohortIds.contains(cohortDefinition.cohortId())) {
                    LOGGER.warn("Duplicate cohort ID found for trial {}: {}", trialId, cohortDefinition.cohortId());
                }
                cohortIds.add(cohortDefinition.cohortId());
            }
        }
        return cohortIds;
    }

    private static boolean validateInclusionCriteria(@NotNull Multimap<String, String> cohortIdsPerTrial,
            @NotNull List<InclusionCriteriaConfig> inclusionCriteria) {
        boolean valid = true;
        Set<String> trialIds = cohortIdsPerTrial.keySet();
        for (InclusionCriteriaConfig criterion : inclusionCriteria) {
            if (!trialIds.contains(criterion.trialId())) {
                LOGGER.warn("Inclusion criterion '{}' defined on non-existing trial: {}", criterion.inclusionRule(), criterion.trialId());
                valid = false;
            } else {
                Collection<String> cohortIdsForTrial = cohortIdsPerTrial.get(criterion.trialId());
                for (String cohortId : criterion.appliesToCohorts()) {
                    if (!cohortIdsForTrial.contains(cohortId)) {
                        LOGGER.warn("Inclusion criterion '{}' defined on non-existing cohort: {}", criterion.inclusionRule(), cohortId);
                        valid = false;
                    }
                }
            }

            if (!EligibilityFactory.isValidInclusionCriterion(criterion.inclusionRule())) {
                LOGGER.warn("Not a valid inclusion criterion for trial '{}': {}", criterion.trialId(), criterion.inclusionRule());
                valid = false;
            }

            if (criterion.criterionIds().isEmpty()) {
                LOGGER.warn("No criterion IDs defined for criterion {} on trial '{}", criterion.inclusionRule(), criterion.trialId());
                valid = false;
            }
        }
        return valid;
    }

    private static boolean validateInclusionCriteriaReferences(@NotNull Set<String> trialIds,
            @NotNull List<InclusionCriteriaConfig> inclusionCriteriaConfigs,
            @NotNull List<InclusionCriteriaReferenceConfig> inclusionCriteriaReferenceConfigs) {
        boolean valid = true;
        for (InclusionCriteriaReferenceConfig referenceConfig : inclusionCriteriaReferenceConfigs) {
            if (!trialIds.contains(referenceConfig.trialId())) {
                LOGGER.warn("Reference '{}' defined on non-existing trial: {}", referenceConfig.criterionId(), referenceConfig.trialId());
                valid = false;
            }
        }

        Multimap<String, InclusionCriteriaReferenceConfig> referencesPerTrial = buildMapPerTrial(inclusionCriteriaReferenceConfigs);
        Multimap<String, InclusionCriteriaConfig> criteriaPerTrial = buildMapPerTrial(inclusionCriteriaConfigs);
        for (String trialId : trialIds) {
            Collection<InclusionCriteriaReferenceConfig> references = referencesPerTrial.get(trialId);
            Collection<InclusionCriteriaConfig> criteria = criteriaPerTrial.get(trialId);

            if (references != null && criteria != null) {
                for (InclusionCriteriaConfig criterion : criteria) {
                    if (!allReferencesExist(references, criterion.criterionIds())) {
                        LOGGER.warn("Not all references are defined on trial '{}': {}", trialId, criterion.criterionIds());
                        valid = false;
                    }
                }
            }
        }

        return valid;
    }

    @NotNull
    private static <T extends TrialConfig> Multimap<String, T> buildMapPerTrial(@NotNull List<T> configs) {
        Multimap<String, T> map = ArrayListMultimap.create();
        for (T config : configs) {
            map.put(config.trialId(), config);
        }
        return map;
    }

    private static boolean allReferencesExist(@NotNull Collection<InclusionCriteriaReferenceConfig> references,
            @NotNull Set<String> criterionIds) {
        Set<String> referenceIds = Sets.newHashSet();
        for (InclusionCriteriaReferenceConfig reference : references) {
            referenceIds.add(reference.criterionId());
        }

        for (String criterionId : criterionIds) {
            if (!referenceIds.contains(criterionId)) {
                return false;
            }
        }
        return true;
    }
}
