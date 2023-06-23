package com.hartwig.actin.treatment.trial;

import java.io.IOException;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.hartwig.actin.treatment.trial.config.CohortDefinitionConfig;
import com.hartwig.actin.treatment.trial.config.InclusionCriteriaConfig;
import com.hartwig.actin.treatment.trial.config.InclusionCriteriaReferenceConfig;
import com.hartwig.actin.treatment.trial.config.TrialDefinitionConfig;

import org.jetbrains.annotations.NotNull;

public class TrialConfigModel {

    @NotNull
    private final TrialConfigDatabase database;

    @NotNull
    public static TrialConfigModel create(@NotNull String trialConfigDirectory, @NotNull EligibilityFactory eligibilityFactory)
            throws IOException {
        TrialConfigDatabase database = TrialConfigDatabaseReader.read(trialConfigDirectory);
        if (!new TrialConfigDatabaseValidator(eligibilityFactory).isValid(database)) {
            throw new IllegalStateException("Trial config database is not considered valid. Cannot create config model.");
        }
        return new TrialConfigModel(database);
    }

    @VisibleForTesting
    TrialConfigModel(@NotNull final TrialConfigDatabase database) {
        this.database = database;
    }

    @NotNull
    public List<TrialDefinitionConfig> trials() {
        return database.trialDefinitionConfigs();
    }

    @NotNull
    public List<CohortDefinitionConfig> cohortsForTrial(@NotNull String trialId) {
        List<CohortDefinitionConfig> cohorts = Lists.newArrayList();
        for (CohortDefinitionConfig cohort : database.cohortDefinitionConfigs()) {
            if (cohort.trialId().equals(trialId)) {
                cohorts.add(cohort);
            }
        }
        return cohorts;
    }

    @NotNull
    public List<InclusionCriteriaConfig> generalInclusionCriteriaForTrial(@NotNull String trialId) {
        List<InclusionCriteriaConfig> configs = Lists.newArrayList();
        for (InclusionCriteriaConfig config : database.inclusionCriteriaConfigs()) {
            if (config.trialId().equals(trialId) && config.appliesToCohorts().isEmpty()) {
                configs.add(config);
            }
        }
        return configs;
    }

    @NotNull
    public List<InclusionCriteriaConfig> specificInclusionCriteriaForCohort(@NotNull String trialId, @NotNull String cohortId) {
        List<InclusionCriteriaConfig> configs = Lists.newArrayList();
        for (InclusionCriteriaConfig config : database.inclusionCriteriaConfigs()) {
            if (config.trialId().equals(trialId) && config.appliesToCohorts().contains(cohortId)) {
                configs.add(config);
            }
        }
        return configs;
    }

    @NotNull
    public List<InclusionCriteriaReferenceConfig> referencesForTrial(@NotNull String trialId) {
        List<InclusionCriteriaReferenceConfig> configs = Lists.newArrayList();
        for (InclusionCriteriaReferenceConfig config : database.inclusionCriteriaReferenceConfigs()) {
            if (config.trialId().equals(trialId)) {
                configs.add(config);
            }
        }
        return configs;
    }
}
