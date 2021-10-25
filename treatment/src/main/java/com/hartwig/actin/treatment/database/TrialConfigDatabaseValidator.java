package com.hartwig.actin.treatment.database;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.hartwig.actin.treatment.database.config.CohortDefinitionConfig;
import com.hartwig.actin.treatment.database.config.InclusionCriteriaConfig;
import com.hartwig.actin.treatment.database.config.TrialDefinitionConfig;

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

        return validCohorts && validInclusionCriteria;
    }

    private static boolean validateInclusionCriteria(@NotNull Multimap<String, String> cohortIdsPerTrial,
            @NotNull List<InclusionCriteriaConfig> inclusionCriteria) {
        boolean valid = true;
        Set<String> trialIds = cohortIdsPerTrial.keySet();
        for (InclusionCriteriaConfig criterion : inclusionCriteria) {
            if (!trialIds.contains(criterion.trialId())) {
                LOGGER.warn("Inclusion criterion '{}' defined on non-existing trial: {}", criterion.eligibilityRule(), criterion.trialId());
                valid = false;
            } else {
                Collection<String> cohortIdsForTrial = cohortIdsPerTrial.get(criterion.trialId());
                for (String cohortId : criterion.appliesToCohorts()) {
                    if (!cohortIdsForTrial.contains(cohortId)) {
                        LOGGER.warn("Inclusion criterion '{}' defined on non-existing cohort: {}", criterion.eligibilityRule(), cohortId);
                        valid = false;
                    }
                }
            }
        }
        return valid;
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
}
