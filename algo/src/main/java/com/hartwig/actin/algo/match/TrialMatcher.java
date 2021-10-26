package com.hartwig.actin.algo.match;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.CohortEligibility;
import com.hartwig.actin.algo.datamodel.EligibilityEvaluation;
import com.hartwig.actin.algo.datamodel.ImmutableCohortEligibility;
import com.hartwig.actin.algo.datamodel.ImmutableSampleTreatmentMatch;
import com.hartwig.actin.algo.datamodel.ImmutableTrialEligibility;
import com.hartwig.actin.algo.datamodel.SampleTreatmentMatch;
import com.hartwig.actin.algo.datamodel.TrialEligibility;
import com.hartwig.actin.algo.eligibility.EvaluatorFunction;
import com.hartwig.actin.algo.eligibility.EvaluatorFunctionFactory;
import com.hartwig.actin.treatment.datamodel.Cohort;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.Trial;

import org.jetbrains.annotations.NotNull;

public final class TrialMatcher {

    private TrialMatcher() {
    }

    @NotNull
    public static SampleTreatmentMatch determineEligibility(@NotNull PatientRecord patient, @NotNull List<Trial> trials) {
        List<TrialEligibility> trialMatches = Lists.newArrayList();
        for (Trial trial : trials) {
            List<CohortEligibility> cohortMatching = Lists.newArrayList();
            for (Cohort cohort : trial.cohorts()) {
                cohortMatching.add(ImmutableCohortEligibility.builder()
                        .cohortId(cohort.cohortId())
                        .evaluations(evaluateEligibility(patient, cohort.eligibilityFunctions()))
                        .build());
            }

            trialMatches.add(ImmutableTrialEligibility.builder()
                    .trialId(trial.trialId())
                    .evaluations(evaluateEligibility(patient, trial.generalEligibilityFunctions()))
                    .cohorts(cohortMatching)
                    .build());
        }

        return ImmutableSampleTreatmentMatch.builder().sampleId(patient.sampleId()).trialMatches(trialMatches).build();
    }

    @NotNull
    private static Map<EligibilityFunction, EligibilityEvaluation> evaluateEligibility(@NotNull PatientRecord patient,
            @NotNull List<EligibilityFunction> eligibilityFunctions) {
        Map<EligibilityFunction, EligibilityEvaluation> eligibility = Maps.newHashMap();
        for (EligibilityFunction function : eligibilityFunctions) {
            EvaluatorFunction evaluator = EvaluatorFunctionFactory.create(function);
            eligibility.put(function, evaluator.evaluate(patient));
        }
        return eligibility;
    }
}
