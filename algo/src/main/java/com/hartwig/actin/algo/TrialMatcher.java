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
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.datamodel.ImmutableTreatmentMatch;
import com.hartwig.actin.algo.datamodel.ImmutableTrialEligibility;
import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.algo.datamodel.TrialEligibility;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.EvaluationFunctionFactory;
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
            List<CohortEligibility> cohortMatching = Lists.newArrayList();
            for (Cohort cohort : trial.cohorts()) {
                Map<Eligibility, EvaluationResult> evaluations = evaluateEligibility(patient, cohort.eligibility());
                cohortMatching.add(ImmutableCohortEligibility.builder()
                        .metadata(cohort.metadata())
                        .overallEvaluation(determineOverallEvaluation(evaluations))
                        .evaluations(convert(evaluations))
                        .build());
            }

            Map<Eligibility, EvaluationResult> evaluations = evaluateEligibility(patient, trial.generalEligibility());
            trialMatches.add(ImmutableTrialEligibility.builder()
                    .identification(trial.identification())
                    .overallEvaluation(determineOverallEvaluation(evaluations))
                    .evaluations(convert(evaluations))
                    .cohorts(cohortMatching)
                    .build());
        }

        trialMatches.sort(new TrialEligibilityComparator());

        return ImmutableTreatmentMatch.builder().sampleId(patient.sampleId()).trialMatches(trialMatches).build();
    }

    // TODO Remove / temp code.
    @NotNull
    private static Map<Eligibility, Evaluation> convert(@NotNull Map<Eligibility, EvaluationResult> evaluations) {
        Map<Eligibility, Evaluation> converted = Maps.newTreeMap(new EligibilityComparator());
        for (Map.Entry<Eligibility, EvaluationResult> entry : evaluations.entrySet()) {
            converted.put(entry.getKey(), ImmutableEvaluation.builder().result(entry.getValue()).build());
        }
        return converted;
    }

    @NotNull
    private Map<Eligibility, EvaluationResult> evaluateEligibility(@NotNull PatientRecord patient, @NotNull List<Eligibility> eligibility) {
        Map<Eligibility, EvaluationResult> evaluations = Maps.newTreeMap(new EligibilityComparator());
        for (Eligibility entry : eligibility) {
            EvaluationFunction evaluator = evaluationFunctionFactory.create(entry.function());
            evaluations.put(entry, evaluator.evaluate(patient));
        }
        return evaluations;
    }

    @NotNull
    private static EvaluationResult determineOverallEvaluation(@NotNull Map<Eligibility, EvaluationResult> evaluations) {
        Set<EvaluationResult> unique = Sets.newHashSet(evaluations.values());

        if (unique.contains(EvaluationResult.FAIL)) {
            return EvaluationResult.FAIL;
        } else if (unique.contains(EvaluationResult.NOT_IMPLEMENTED)) {
            return EvaluationResult.UNDETERMINED;
        } else if (unique.contains(EvaluationResult.UNDETERMINED) || unique.contains(EvaluationResult.PASS_BUT_WARN)) {
            return EvaluationResult.PASS_BUT_WARN;
        } else {
            return EvaluationResult.PASS;
        }
    }
}
