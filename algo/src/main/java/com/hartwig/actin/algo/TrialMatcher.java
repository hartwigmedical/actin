package com.hartwig.actin.algo;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.CohortEligibility;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableCohortEligibility;
import com.hartwig.actin.algo.datamodel.ImmutableTreatmentMatch;
import com.hartwig.actin.algo.datamodel.ImmutableTrialEligibility;
import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.algo.datamodel.TrialEligibility;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.EvaluationFunctionFactory;
import com.hartwig.actin.algo.sort.CohortEligibilityComparator;
import com.hartwig.actin.algo.sort.TrialEligibilityComparator;
import com.hartwig.actin.treatment.datamodel.Cohort;
import com.hartwig.actin.treatment.datamodel.Eligibility;
import com.hartwig.actin.treatment.datamodel.Trial;
import com.hartwig.actin.treatment.sort.EligibilityComparator;

import org.jetbrains.annotations.NotNull;

public class TrialMatcher {

    @NotNull
    private final EvaluationFunctionFactory evaluationFunctionFactory;

    @NotNull
    public static TrialMatcher withDoidModel(@NotNull DoidModel doidModel) {
        return new TrialMatcher(EvaluationFunctionFactory.withDoidModel(doidModel));
    }

    @VisibleForTesting
    TrialMatcher(@NotNull final EvaluationFunctionFactory evaluationFunctionFactory) {
        this.evaluationFunctionFactory = evaluationFunctionFactory;
    }

    @NotNull
    public TreatmentMatch determineEligibility(@NotNull PatientRecord patient, @NotNull List<Trial> trials) {
        List<TrialEligibility> trialMatches = Lists.newArrayList();
        for (Trial trial : trials) {
            Map<Eligibility, Evaluation> trialEvaluations = evaluateEligibility(patient, trial.generalEligibility());

            List<CohortEligibility> cohortMatching = Lists.newArrayList();
            for (Cohort cohort : trial.cohorts()) {
                Map<Eligibility, Evaluation> cohortEvaluations = evaluateEligibility(patient, cohort.eligibility());
                cohortMatching.add(ImmutableCohortEligibility.builder()
                        .metadata(cohort.metadata())
                        .overallEvaluation(determineOverallEvaluation(cohortEvaluations))
                        .evaluations(cohortEvaluations)
                        .build());
            }

            cohortMatching.sort(new CohortEligibilityComparator());

            trialMatches.add(ImmutableTrialEligibility.builder()
                    .identification(trial.identification())
                    .overallEvaluation(determineOverallEvaluation(trialEvaluations))
                    .evaluations(trialEvaluations)
                    .cohorts(cohortMatching)
                    .build());
        }

        trialMatches.sort(new TrialEligibilityComparator());

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

    @NotNull
    private static EvaluationResult determineOverallEvaluation(@NotNull Map<Eligibility, Evaluation> evaluations) {
        Set<EvaluationResult> results = Sets.newHashSet();
        for (Evaluation evaluation : evaluations.values()) {
            results.add(evaluation.result());
        }

        if (results.contains(EvaluationResult.FAIL)) {
            return EvaluationResult.FAIL;
        } else if (results.contains(EvaluationResult.NOT_IMPLEMENTED)) {
            return EvaluationResult.UNDETERMINED;
        } else if (results.contains(EvaluationResult.UNDETERMINED) || results.contains(EvaluationResult.WARN)) {
            return EvaluationResult.WARN;
        } else {
            return EvaluationResult.PASS;
        }
    }
}
