package com.hartwig.actin.treatment.trial;

import java.io.IOException;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.hartwig.actin.treatment.trial.config.CohortDefinitionConfig;
import com.hartwig.actin.treatment.trial.config.InclusionCriteriaConfig;
import com.hartwig.actin.treatment.trial.config.TrialDefinitionConfig;

import org.jetbrains.annotations.NotNull;

public class TrialConfigModel {

    @NotNull
    private final TrialConfigDatabase database;

    @NotNull
    public static TrialConfigModel fromTrialConfigDirectory(@NotNull String trialConfigDirectory) throws IOException {
        TrialConfigDatabase database = TrialConfigDatabaseReader.read(trialConfigDirectory);
        if (!TrialConfigDatabaseValidator.isValid(database)) {
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
    public List<String> generalInclusionCriteriaForTrial(@NotNull String trialId) {
        List<String> configs = Lists.newArrayList();
        for (InclusionCriteriaConfig config : database.inclusionCriteriaConfigs()) {
            if (config.trialId().equals(trialId) && config.appliesToCohorts().isEmpty()) {
                configs.add(config.inclusionCriterion());
            }
        }
        return configs;
    }

    @NotNull
    public List<String> specificInclusionCriteriaForCohort(@NotNull String trialId, @NotNull String cohortId) {
        List<String> configs = Lists.newArrayList();
        for (InclusionCriteriaConfig config : database.inclusionCriteriaConfigs()) {
            if (config.trialId().equals(trialId) && config.appliesToCohorts().contains(cohortId)) {
                configs.add(config.inclusionCriterion());
            }
        }
        return configs;
    }
}
