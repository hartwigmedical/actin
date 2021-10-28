package com.hartwig.actin.treatment.trial;

import java.io.IOException;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.hartwig.actin.treatment.datamodel.Cohort;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.ImmutableCohort;
import com.hartwig.actin.treatment.datamodel.ImmutableTrial;
import com.hartwig.actin.treatment.datamodel.Trial;
import com.hartwig.actin.treatment.trial.config.CohortDefinitionConfig;
import com.hartwig.actin.treatment.trial.config.TrialDefinitionConfig;

import org.jetbrains.annotations.NotNull;

public class TrialFactory {

    @NotNull
    private final TrialConfigModel trialModel;

    @NotNull
    public static List<Trial> fromTrialConfigDirectory(@NotNull String trialConfigDirectory) throws IOException {
        return new TrialFactory(TrialConfigModel.fromTrialConfigDirectory(trialConfigDirectory)).create();
    }

    @VisibleForTesting
    TrialFactory(@NotNull final TrialConfigModel trialModel) {
        this.trialModel = trialModel;
    }

    @NotNull
    public List<Trial> create() {
        List<Trial> trials = Lists.newArrayList();

        for (TrialDefinitionConfig trialConfig : trialModel.trials()) {
            String trialId = trialConfig.trialId();

            trials.add(ImmutableTrial.builder()
                    .trialId(trialId)
                    .acronym(trialConfig.acronym())
                    .title(trialConfig.title())
                    .generalEligibilityFunctions(toEligibilityFunctions(trialModel.generalInclusionCriteriaForTrial(trialId)))
                    .cohorts(cohortsForTrial(trialId))
                    .build());
        }

        return trials;
    }

    @NotNull
    private List<Cohort> cohortsForTrial(@NotNull String trialId) {
        List<Cohort> cohorts = Lists.newArrayList();

        for (CohortDefinitionConfig cohortConfig : trialModel.cohortsForTrial(trialId)) {
            String cohortId = cohortConfig.cohortId();
            cohorts.add(ImmutableCohort.builder()
                    .cohortId(cohortId)
                    .open(cohortConfig.open())
                    .description(cohortConfig.description())
                    .eligibilityFunctions(toEligibilityFunctions(trialModel.specificInclusionCriteriaForCohort(trialId, cohortId)))
                    .build());
        }

        return cohorts;
    }

    @NotNull
    private static List<EligibilityFunction> toEligibilityFunctions(@NotNull List<String> inclusionCriteria) {
        List<EligibilityFunction> eligibilityFunctions = Lists.newArrayList();
        for (String inclusionCriterion : inclusionCriteria) {
            eligibilityFunctions.add(EligibilityFactory.generateEligibilityFunction(inclusionCriterion));
        }
        return eligibilityFunctions;
    }
}
