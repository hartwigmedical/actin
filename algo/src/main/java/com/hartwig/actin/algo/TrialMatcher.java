package com.hartwig.actin.algo;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.CohortEligibility;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.ImmutableCohortEligibility;
import com.hartwig.actin.algo.datamodel.ImmutableTreatmentMatch;
import com.hartwig.actin.algo.datamodel.ImmutableTrialEligibility;
import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.algo.datamodel.TrialEligibility;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.EvaluationFunctionFactory;
import com.hartwig.actin.treatment.datamodel.Cohort;
import com.hartwig.actin.treatment.datamodel.Eligibility;
import com.hartwig.actin.treatment.datamodel.Trial;

import org.jetbrains.annotations.NotNull;

public final class TrialMatcher {

    private TrialMatcher() {
    }

    @NotNull
    public static TreatmentMatch determineEligibility(@NotNull PatientRecord patient, @NotNull List<Trial> trials) {
        List<TrialEligibility> trialMatches = Lists.newArrayList();
        for (Trial trial : trials) {
            List<CohortEligibility> cohortMatching = Lists.newArrayList();
            for (Cohort cohort : trial.cohorts()) {
                Map<Eligibility, Evaluation> evaluations = evaluateEligibility(patient, cohort.eligibility());
                cohortMatching.add(ImmutableCohortEligibility.builder()
                        .cohortId(cohort.cohortId())
                        .overallEvaluation(determineOverallEvaluation(evaluations))
                        .evaluations(evaluations)
                        .build());
            }

            Map<Eligibility, Evaluation> evaluations = evaluateEligibility(patient, trial.generalEligibility());
            trialMatches.add(ImmutableTrialEligibility.builder()
                    .trialId(trial.trialId())
                    .overallEvaluation(determineOverallEvaluation(evaluations))
                    .evaluations(evaluations)
                    .cohorts(cohortMatching)
                    .build());
        }

        return ImmutableTreatmentMatch.builder().sampleId(patient.sampleId()).trialMatches(trialMatches).build();
    }

    @NotNull
    private static Map<Eligibility, Evaluation> evaluateEligibility(@NotNull PatientRecord patient,
            @NotNull List<Eligibility> eligibility) {
        Map<Eligibility, Evaluation> evaluations = Maps.newHashMap();
        for (Eligibility entry : eligibility) {
            EvaluationFunction evaluator = EvaluationFunctionFactory.create(entry.function());
            evaluations.put(entry, evaluator.evaluate(patient));
        }
        return evaluations;
    }

    @NotNull
    private static Evaluation determineOverallEvaluation(@NotNull Map<Eligibility, Evaluation> evaluations) {
        Set<Evaluation> unique = Sets.newHashSet(evaluations.values());

        if (unique.contains(Evaluation.FAIL)) {
            return Evaluation.FAIL;
        } else if (unique.contains(Evaluation.UNDETERMINED) || unique.contains(Evaluation.NOT_IMPLEMENTED)) {
            return Evaluation.UNDETERMINED;
        } else if (unique.contains(Evaluation.PASS_BUT_WARN)) {
            return Evaluation.PASS_BUT_WARN;
        } else {
            return Evaluation.PASS;
        }
    }
}
