package com.hartwig.actin.treatment.trial;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.hartwig.actin.treatment.serialization.TrialJson;
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
        boolean validTrials = validateTrials(database.trialDefinitionConfigs());

        boolean validCohorts = validateCohorts(trialIds, database.cohortDefinitionConfigs());
        Multimap<String, String> cohortIdsPerTrial = extractCohortIdsPerTrial(trialIds, database.cohortDefinitionConfigs());

        boolean validInclusionCriteria = validateInclusionCriteria(trialIds, cohortIdsPerTrial, database.inclusionCriteriaConfigs());

        boolean validInclusionCriteriaReferences = validateInclusionCriteriaReferences(trialIds,
                database.inclusionCriteriaConfigs(),
                database.inclusionCriteriaReferenceConfigs());

        return validTrials && validCohorts && validInclusionCriteria && validInclusionCriteriaReferences;
    }

    @NotNull
    private static Set<String> extractTrialIds(@NotNull List<TrialDefinitionConfig> configs) {
        Set<String> trialIds = Sets.newHashSet();
        for (TrialDefinitionConfig config : configs) {
            trialIds.add(config.trialId());
        }
        return trialIds;
    }

    private static boolean validateTrials(@NotNull List<TrialDefinitionConfig> trialDefinitions) {
        boolean valid = true;
        Set<String> trialIds = Sets.newHashSet();
        Set<String> trialFileIds = Sets.newHashSet();
        for (TrialDefinitionConfig trialDefinition : trialDefinitions) {
            String trialId = trialDefinition.trialId();
            if (trialIds.contains(trialId)) {
                LOGGER.warn("Duplicate trial ID found: '{}'", trialId);
                valid = false;
            }
            trialIds.add(trialId);

            String trialFileId = TrialJson.trialFileId(trialDefinition.trialId());
            if (trialFileIds.contains(trialFileId)) {
                LOGGER.warn("Duplicate trial file ID found: '{}'", trialFileId);
                valid = false;
            }
            trialFileIds.add(trialFileId);
        }
        return valid;
    }

    private static boolean validateCohorts(@NotNull Set<String> trialIds, @NotNull List<CohortDefinitionConfig> cohortDefinitions) {
        boolean valid = true;
        Map<String, Set<String>> cohortIdsPerTrial = Maps.newHashMap();
        for (CohortDefinitionConfig cohortDefinition : cohortDefinitions) {
            String trialId = cohortDefinition.trialId();
            String cohortId = cohortDefinition.cohortId();
            if (!trialIds.contains(trialId)) {
                LOGGER.warn("Cohort '{}' defined on non-existing trial: '{}'", cohortId, trialId);
                valid = false;
            }
            Set<String> cohortIdsForTrial = cohortIdsPerTrial.get(cohortDefinition.trialId());
            if (cohortIdsForTrial == null) {
                cohortIdsForTrial = Sets.newHashSet();
            } else if (cohortIdsForTrial.contains(cohortDefinition.cohortId())) {
                LOGGER.warn("Duplicate cohort ID found for trial '{}': '{}'", trialId, cohortId);
                valid = false;
            }

            cohortIdsForTrial.add(cohortId);
            cohortIdsPerTrial.put(trialId, cohortIdsForTrial);
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
                cohortIds.add(cohortDefinition.cohortId());
            }
        }
        return cohortIds;
    }

    private static boolean validateInclusionCriteria(@NotNull Set<String> trialIds, @NotNull Multimap<String, String> cohortIdsPerTrial,
            @NotNull List<InclusionCriteriaConfig> inclusionCriteria) {
        boolean valid = true;
        for (InclusionCriteriaConfig criterion : inclusionCriteria) {
            if (!trialIds.contains(criterion.trialId())) {
                LOGGER.warn("Inclusion criterion '{}' defined on non-existing trial: '{}'", criterion.inclusionRule(), criterion.trialId());
                valid = false;
            } else {
                Collection<String> cohortIdsForTrial = cohortIdsPerTrial.get(criterion.trialId());
                for (String cohortId : criterion.appliesToCohorts()) {
                    if (!cohortIdsForTrial.contains(cohortId)) {
                        LOGGER.warn("Inclusion criterion '{}' defined on non-existing cohort: '{}'", criterion.inclusionRule(), cohortId);
                        valid = false;
                    }
                }
            }

            if (!EligibilityFactory.isValidInclusionCriterion(criterion.inclusionRule())) {
                LOGGER.warn("Not a valid inclusion criterion for trial '{}': '{}'", criterion.trialId(), criterion.inclusionRule());
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
                LOGGER.warn("Reference '{}' defined on non-existing trial: '{}'", referenceConfig.referenceId(), referenceConfig.trialId());
                valid = false;
            }
        }

        Map<String, Set<String>> referenceIdsPerTrial = Maps.newHashMap();
        for (InclusionCriteriaReferenceConfig config : inclusionCriteriaReferenceConfigs) {
            String trialId = config.trialId();
            String referenceId = config.referenceId();

            Set<String> referenceIdsForTrial = referenceIdsPerTrial.get(trialId);
            if (referenceIdsForTrial == null) {
                referenceIdsForTrial = Sets.newHashSet();
            } else if (referenceIdsForTrial.contains(referenceId)) {
                LOGGER.warn("Duplicate reference ID found for trial '{}': '{}'", trialId, referenceId);
                valid = false;
            }
            referenceIdsForTrial.add(referenceId);
            referenceIdsPerTrial.put(trialId, referenceIdsForTrial);
        }

        Multimap<String, InclusionCriteriaReferenceConfig> referencesPerTrial = buildMapPerTrial(inclusionCriteriaReferenceConfigs);
        Multimap<String, InclusionCriteriaConfig> criteriaPerTrial = buildMapPerTrial(inclusionCriteriaConfigs);
        for (String trialId : trialIds) {
            Collection<InclusionCriteriaReferenceConfig> references = referencesPerTrial.get(trialId);
            Collection<InclusionCriteriaConfig> criteria = criteriaPerTrial.get(trialId);

            if (references != null && criteria != null) {
                for (InclusionCriteriaConfig criterion : criteria) {
                    if (!allReferencesExist(references, criterion.referenceIds())) {
                        LOGGER.warn("Not all references are defined on trial '{}': '{}'", trialId, criterion.referenceIds());
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
            @NotNull Set<String> referenceIdsThatHaveToExist) {
        Set<String> referenceIdTruthSet = Sets.newHashSet();
        for (InclusionCriteriaReferenceConfig reference : references) {
            referenceIdTruthSet.add(reference.referenceId());
        }

        for (String referenceId : referenceIdsThatHaveToExist) {
            if (!referenceIdTruthSet.contains(referenceId)) {
                return false;
            }
        }
        return true;
    }
}
