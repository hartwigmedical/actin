package com.hartwig.actin.algo;

import java.util.List;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.calendar.ReferenceDateProvider;
import com.hartwig.actin.algo.datamodel.CohortMatch;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableCohortMatch;
import com.hartwig.actin.algo.datamodel.ImmutableTrialMatch;
import com.hartwig.actin.algo.datamodel.TrialMatch;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.EvaluationFunctionFactory;
import com.hartwig.actin.algo.sort.CohortMatchComparator;
import com.hartwig.actin.algo.sort.TrialMatchComparator;
import com.hartwig.actin.doid.DoidModel;
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
    public List<TrialMatch> determineEligibility(@NotNull PatientRecord patient, @NotNull List<Trial> trials) {
        List<TrialMatch> trialMatches = Lists.newArrayList();
        for (Trial trial : trials) {
            Map<Eligibility, Evaluation> trialEvaluations = evaluateEligibility(patient, trial.generalEligibility());

            boolean passesAllTrialEvaluations = isPotentiallyEligible(trialEvaluations.values());

            boolean hasEligibleCohort = false;
            List<CohortMatch> cohortMatches = Lists.newArrayList();
            for (Cohort cohort : trial.cohorts()) {
                if (cohort.metadata().evaluable()) {
                    Map<Eligibility, Evaluation> cohortEvaluations = evaluateEligibility(patient, cohort.eligibility());
                    boolean isPotentiallyEligible = isPotentiallyEligible(cohortEvaluations.values());

                    if (isPotentiallyEligible) {
                        hasEligibleCohort = true;
                    }

                    cohortMatches.add(ImmutableCohortMatch.builder()
                            .metadata(cohort.metadata())
                            .isPotentiallyEligible(isPotentiallyEligible && passesAllTrialEvaluations)
                            .evaluations(cohortEvaluations)
                            .build());
                }
            }

            cohortMatches.sort(new CohortMatchComparator());

            boolean isEligible = passesAllTrialEvaluations && (trial.cohorts().isEmpty() || hasEligibleCohort);
            trialMatches.add(ImmutableTrialMatch.builder()
                    .identification(trial.identification())
                    .isPotentiallyEligible(isEligible)
                    .evaluations(trialEvaluations)
                    .cohorts(cohortMatches)
                    .build());
        }

        trialMatches.sort(new TrialMatchComparator());

        return trialMatches;
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

    @VisibleForTesting
    static boolean isPotentiallyEligible(@NotNull Iterable<Evaluation> evaluations) {
        for (Evaluation evaluation : evaluations) {
            if (!evaluation.recoverable() && (evaluation.result() == EvaluationResult.FAIL
                    || evaluation.result() == EvaluationResult.NOT_IMPLEMENTED)) {
                return false;
            }
        }
        return true;
    }
}
