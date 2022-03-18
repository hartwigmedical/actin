package com.hartwig.actin.algo;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.calendar.ReferenceDateProvider;
import com.hartwig.actin.algo.datamodel.CohortMatch;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableCohortMatch;
import com.hartwig.actin.algo.datamodel.ImmutableTreatmentMatch;
import com.hartwig.actin.algo.datamodel.ImmutableTrialMatch;
import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.algo.datamodel.TrialMatch;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.EvaluationFunctionFactory;
import com.hartwig.actin.algo.sort.CohortMatchComparator;
import com.hartwig.actin.algo.sort.TrialMatchComparator;
import com.hartwig.actin.treatment.datamodel.Cohort;
import com.hartwig.actin.treatment.datamodel.Eligibility;
import com.hartwig.actin.treatment.datamodel.Trial;
import com.hartwig.actin.treatment.sort.EligibilityComparator;

import org.jetbrains.annotations.NotNull;

public class TrialMatcher {

    @NotNull
    private final EvaluationFunctionFactory evaluationFunctionFactory;

    @NotNull
    public static TrialMatcher create(@NotNull DoidModel doidModel, @NotNull ReferenceDateProvider referenceDateProvider) {
        return new TrialMatcher(EvaluationFunctionFactory.create(doidModel, referenceDateProvider));
    }

    @VisibleForTesting
    TrialMatcher(@NotNull final EvaluationFunctionFactory evaluationFunctionFactory) {
        this.evaluationFunctionFactory = evaluationFunctionFactory;
    }

    @NotNull
    public TreatmentMatch determineEligibility(@NotNull PatientRecord patient, @NotNull List<Trial> trials) {
        List<TrialMatch> trialMatches = Lists.newArrayList();
        for (Trial trial : trials) {
            Map<Eligibility, Evaluation> trialEvaluations = evaluateEligibility(patient, trial.generalEligibility());

            List<CohortMatch> cohortMatching = Lists.newArrayList();
            boolean passesAllTrialEvaluations = isEligible(trialEvaluations);
            boolean hasEligibleCohort = false;
            for (Cohort cohort : trial.cohorts()) {
                Map<Eligibility, Evaluation> cohortEvaluations = evaluateEligibility(patient, cohort.eligibility());
                boolean isPotentiallyEligible = isEligible(cohortEvaluations) && !cohort.metadata().blacklist();

                if (isPotentiallyEligible) {
                    hasEligibleCohort = true;
                }

                cohortMatching.add(ImmutableCohortMatch.builder()
                        .metadata(cohort.metadata())
                        .isPotentiallyEligible(isPotentiallyEligible && passesAllTrialEvaluations)
                        .evaluations(cohortEvaluations)
                        .build());
            }

            cohortMatching.sort(new CohortMatchComparator());

            boolean isEligible = passesAllTrialEvaluations && (trial.cohorts().isEmpty() || hasEligibleCohort);
            trialMatches.add(ImmutableTrialMatch.builder()
                    .identification(trial.identification())
                    .isPotentiallyEligible(isEligible)
                    .evaluations(trialEvaluations)
                    .cohorts(cohortMatching)
                    .build());
        }

        trialMatches.sort(new TrialMatchComparator());

        return ImmutableTreatmentMatch.builder().sampleId(patient.sampleId()).trialMatches(trialMatches).build();
    }

    @NotNull
    private Map<Eligibility, Evaluation> evaluateEligibility(@NotNull PatientRecord patient, @NotNull List<Eligibility> eligibility) {
        Map<Eligibility, Evaluation> evaluations = Maps.newTreeMap(new EligibilityComparator());
        for (Eligibility entry : eligibility) {
            EvaluationFunction evaluator = evaluationFunctionFactory.create(entry.function());
            evaluations.put(entry, evaluator.evaluate(patient));
        }
        return evaluations;
    }

    private static boolean isEligible(@NotNull Map<Eligibility, Evaluation> evaluations) {
        Set<EvaluationResult> results = Sets.newHashSet();
        for (Evaluation evaluation : evaluations.values()) {
            results.add(evaluation.result());
        }

        return !results.contains(EvaluationResult.FAIL) && !results.contains(EvaluationResult.NOT_IMPLEMENTED);
    }
}
