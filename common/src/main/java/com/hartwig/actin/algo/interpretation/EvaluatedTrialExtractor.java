package com.hartwig.actin.algo.interpretation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.algo.datamodel.CohortEligibility;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.algo.datamodel.TrialEligibility;
import com.hartwig.actin.treatment.datamodel.Eligibility;

import org.jetbrains.annotations.NotNull;

public final class EvaluatedTrialExtractor {

    private static final Set<EvaluationResult> WARN_RESULTS = Sets.newHashSet(EvaluationResult.WARN, EvaluationResult.UNDETERMINED);
    private static final Set<EvaluationResult> FAIL_RESULTS = Sets.newHashSet(EvaluationResult.FAIL);

    private EvaluatedTrialExtractor() {
    }

    @NotNull
    public static List<EvaluatedTrial> extract(@NotNull TreatmentMatch treatmentMatch) {
        List<EvaluatedTrial> trials = Lists.newArrayList();

        for (TrialEligibility trialMatch : treatmentMatch.trialMatches()) {
            Set<String> trialWarnings = extractWarnings(trialMatch.evaluations());
            Set<String> trialFails = extractFails(trialMatch.evaluations());

            ImmutableEvaluatedTrial.Builder builder = ImmutableEvaluatedTrial.builder()
                    .trialId(trialMatch.identification().trialId())
                    .acronym(trialMatch.identification().acronym());

            for (CohortEligibility cohortMatch : trialMatch.cohorts()) {
                Set<String> cohortWarnings = extractWarnings(cohortMatch.evaluations());
                Set<String> cohortFails = extractFails(cohortMatch.evaluations());

                trials.add(builder.cohort(cohortMatch.metadata().description())
                        .isPotentiallyEligible(cohortMatch.isPotentiallyEligible() && trialMatch.isPotentiallyEligible())
                        .isOpen(cohortMatch.metadata().open())
                        .warnings(Sets.union(cohortWarnings, trialWarnings))
                        .fails(Sets.union(cohortFails, trialFails))
                        .build());
            }

            // Handle case of trial without cohorts.
            if (trialMatch.cohorts().isEmpty()) {
                trials.add(builder.cohort(null)
                        .isPotentiallyEligible(trialMatch.isPotentiallyEligible())
                        .isOpen(true)
                        .warnings(trialWarnings)
                        .fails(trialFails)
                        .build());
            }
        }

        return trials;
    }

    @NotNull
    private static Set<String> extractWarnings(@NotNull Map<Eligibility, Evaluation> evaluationMap) {
        Set<String> messages = Sets.newHashSet();
        for (Evaluation evaluation : extractEvaluations(evaluationMap, WARN_RESULTS)) {
            messages.addAll(evaluation.warnGeneralMessages());
            messages.addAll(evaluation.undeterminedGeneralMessages());
        }
        return messages;
    }

    @NotNull
    private static Set<String> extractFails(@NotNull Map<Eligibility, Evaluation> evaluations) {
        Set<String> messages = Sets.newHashSet();
        for (Evaluation evaluation : extractEvaluations(evaluations, FAIL_RESULTS)) {
            messages.addAll(evaluation.failGeneralMessages());
        }
        return messages;
    }

    @NotNull
    private static List<Evaluation> extractEvaluations(@NotNull Map<Eligibility, Evaluation> evaluations,
            @NotNull Set<EvaluationResult> includeFilter) {
        List<Evaluation> filtered = Lists.newArrayList();
        for (Evaluation evaluation : evaluations.values()) {
            if (includeFilter.contains(evaluation.result())) {
                filtered.add(evaluation);
            }
        }
        return filtered;
    }
}
