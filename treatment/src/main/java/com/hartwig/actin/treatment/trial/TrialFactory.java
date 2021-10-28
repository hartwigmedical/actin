package com.hartwig.actin.treatment.trial;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.treatment.datamodel.Cohort;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.datamodel.ImmutableCohort;
import com.hartwig.actin.treatment.datamodel.ImmutableEligibilityFunction;
import com.hartwig.actin.treatment.datamodel.ImmutableTrial;
import com.hartwig.actin.treatment.datamodel.Trial;
import com.hartwig.actin.treatment.trial.config.CohortDefinitionConfig;
import com.hartwig.actin.treatment.trial.config.InclusionCriteriaConfig;
import com.hartwig.actin.treatment.trial.config.TrialDefinitionConfig;

import org.jetbrains.annotations.NotNull;

public class TrialFactory {

    private static final Set<EligibilityRule> VALID_COMPOSITE_RULES =
            Sets.newHashSet(EligibilityRule.AND, EligibilityRule.OR, EligibilityRule.NOT, EligibilityRule.WARN_IF);

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
    private static List<EligibilityFunction> toEligibilityFunctions(@NotNull List<InclusionCriteriaConfig> inclusionConfigs) {
        List<EligibilityFunction> eligibilityFunctions = Lists.newArrayList();
        for (InclusionCriteriaConfig inclusionConfig : inclusionConfigs) {
            eligibilityFunctions.add(generateEligibilityFunction(inclusionConfig.inclusionCriterion()));
        }
        return eligibilityFunctions;
    }

    @NotNull
    @VisibleForTesting
    static EligibilityFunction generateEligibilityFunction(@NotNull String criterion) {
        EligibilityRule rule;
        List<Object> parameters = Lists.newArrayList();
        if (criterion.contains("(")) {
            rule = extractCompositeRule(criterion.trim());
            for (String compositeInput : extractCompositeInputs(criterion)) {
                parameters.add(generateEligibilityFunction(compositeInput));
            }
        } else if (criterion.contains("[")) {
            rule = extractParameterizedRule(criterion);
            parameters.addAll(extractParameterizedInputs(criterion));
        } else {
            rule = EligibilityRule.valueOf(criterion.trim());
        }

        return ImmutableEligibilityFunction.builder().rule(rule).parameters(parameters).build();
    }

    @NotNull
    private static EligibilityRule extractCompositeRule(@NotNull String criterion) {
        EligibilityRule rule = EligibilityRule.valueOf(criterion.substring(0, criterion.indexOf("(")));
        if (!VALID_COMPOSITE_RULES.contains(rule)) {
            throw new IllegalStateException("Not a valid composite rule: " + rule);
        }
        return rule;
    }

    @NotNull
    private static EligibilityRule extractParameterizedRule(@NotNull String criterion) {
        return EligibilityRule.valueOf(criterion.substring(0, criterion.indexOf("[")).trim());
    }

    @NotNull
    private static List<String> extractCompositeInputs(@NotNull String criterion) {
        if (!criterion.contains("(") || !criterion.contains(")")) {
            throw new IllegalStateException("Not a valid criterion: " + criterion);
        }

        String params = criterion.substring(criterion.indexOf("(") + 1, criterion.lastIndexOf(")"));

        int relevantCommaPosition = -1;

        int parenthesisCount = 0;
        int commaCount = 0;
        for (int i = 0; i < params.length(); i++) {
            if (params.charAt(i) == '(') {
                parenthesisCount++;
            } else if (params.charAt(i) == ',') {
                commaCount++;
                if (commaCount > parenthesisCount && relevantCommaPosition < 0) {
                    relevantCommaPosition = i;
                }
            }
        }

        List<String> result = Lists.newArrayList();
        if (relevantCommaPosition > 0) {
            result.add(params.substring(0, relevantCommaPosition).trim());
            result.add(params.substring(relevantCommaPosition + 1).trim());
        } else {
            result.add(params.trim());
        }

        return result;
    }

    @NotNull
    private static List<String> extractParameterizedInputs(@NotNull String criterion) {
        if (!criterion.contains("[") || !criterion.contains("]")) {
            throw new IllegalStateException("Not a valid parameterized criterion: " + criterion);
        }

        List<String> params = Lists.newArrayList();

        params.add(criterion.substring(criterion.indexOf("[") + 1, criterion.indexOf("]")));

        return params;
    }
}
