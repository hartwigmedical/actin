package com.hartwig.actin.treatment.trial;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.treatment.datamodel.Cohort;
import com.hartwig.actin.treatment.datamodel.CriterionReference;
import com.hartwig.actin.treatment.datamodel.Eligibility;
import com.hartwig.actin.treatment.datamodel.ImmutableCohort;
import com.hartwig.actin.treatment.datamodel.ImmutableCriterionReference;
import com.hartwig.actin.treatment.datamodel.ImmutableEligibility;
import com.hartwig.actin.treatment.datamodel.ImmutableTrial;
import com.hartwig.actin.treatment.datamodel.Trial;
import com.hartwig.actin.treatment.trial.config.CohortDefinitionConfig;
import com.hartwig.actin.treatment.trial.config.InclusionCriteriaConfig;
import com.hartwig.actin.treatment.trial.config.InclusionCriteriaReferenceConfig;
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

            List<InclusionCriteriaReferenceConfig> references = trialModel.referencesForTrial(trialId);
            trials.add(ImmutableTrial.builder()
                    .trialId(trialId)
                    .acronym(trialConfig.acronym())
                    .title(trialConfig.title())
                    .generalEligibility(toEligibility(trialModel.generalInclusionCriteriaForTrial(trialId), references))
                    .cohorts(cohortsForTrial(trialId, references))
                    .build());
        }

        return trials;
    }

    @NotNull
    private List<Cohort> cohortsForTrial(@NotNull String trialId, @NotNull List<InclusionCriteriaReferenceConfig> references) {
        List<Cohort> cohorts = Lists.newArrayList();

        for (CohortDefinitionConfig cohortConfig : trialModel.cohortsForTrial(trialId)) {
            String cohortId = cohortConfig.cohortId();
            cohorts.add(ImmutableCohort.builder()
                    .cohortId(cohortId)
                    .open(cohortConfig.open())
                    .description(cohortConfig.description())
                    .eligibility(toEligibility(trialModel.specificInclusionCriteriaForCohort(trialId, cohortId), references))
                    .build());
        }

        return cohorts;
    }

    @NotNull
    private List<Eligibility> toEligibility(@NotNull List<InclusionCriteriaConfig> criteria,
            @NotNull List<InclusionCriteriaReferenceConfig> references) {
        List<Eligibility> eligibility = Lists.newArrayList();

        for (InclusionCriteriaConfig criterion : criteria) {
            eligibility.add(ImmutableEligibility.builder()
                    .references(resolveReferences(references, criterion.criterionIds()))
                    .function(EligibilityFactory.generateEligibilityFunction(criterion.inclusionRule()))
                    .build());
        }

        return eligibility;
    }

    @NotNull
    private static Set<CriterionReference> resolveReferences(@NotNull List<InclusionCriteriaReferenceConfig> configs,
            @NotNull Set<String> criterionIds) {
        Set<CriterionReference> references = Sets.newHashSet();

        for (String criterionId : criterionIds) {
            InclusionCriteriaReferenceConfig config = findReferenceConfig(configs, criterionId);
            references.add(ImmutableCriterionReference.builder().id(config.criterionId()).text(config.criterionText()).build());
        }

        return references;
    }

    @NotNull
    private static InclusionCriteriaReferenceConfig findReferenceConfig(@NotNull List<InclusionCriteriaReferenceConfig> configs,
            @NotNull String criterionId) {
        for (InclusionCriteriaReferenceConfig config : configs) {
            if (config.criterionId().equals(criterionId)) {
                return config;
            }
        }

        // Should not happen in a valid trial config database.
        throw new IllegalStateException("No config found for criterion: " + criterionId);
    }
}
