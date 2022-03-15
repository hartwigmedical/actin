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

    private EvaluatedTrialExtractor() {
    }

    @NotNull
    public static List<EvaluatedTrial> extract(@NotNull TreatmentMatch treatmentMatch) {
        List<EvaluatedTrial> trials = Lists.newArrayList();

        for (TrialEligibility trialMatch : treatmentMatch.trialMatches()) {
            if (trialMatch.cohorts().isEmpty()) {
                int evaluationsToCheckCount;
                Set<String> evaluationsToCheckMessages = Sets.newHashSet();
                if (trialMatch.isPotentiallyEligible()) {
                    List<Evaluation> evaluations = extractWarnsAndUndetermined(trialMatch.evaluations());
                    evaluationsToCheckCount = evaluations.size();
                    for (Evaluation evaluation : evaluations) {
                        evaluationsToCheckMessages.addAll(evaluation.warnGeneralMessages());
                        evaluationsToCheckMessages.addAll(evaluation.undeterminedGeneralMessages());
                    }
                } else {
                    List<Evaluation> evaluations = extractFails(trialMatch.evaluations());
                    evaluationsToCheckCount = evaluations.size();
                    for (Evaluation evaluation : evaluations) {
                        evaluationsToCheckMessages.addAll(evaluation.failGeneralMessages());
                    }
                }
                trials.add(ImmutableEvaluatedTrial.builder()
                        .trialId(trialMatch.identification().trialId())
                        .acronym(trialMatch.identification().acronym())
                        .isPotentiallyEligible(trialMatch.isPotentiallyEligible())
                        .isOpen(true)
                        .evaluationsToCheckCount(evaluationsToCheckCount)
                        .evaluationsToCheckMessages(evaluationsToCheckMessages)
                        .build());
            } else {
                for (CohortEligibility cohortMatch : trialMatch.cohorts()) {
                    int evaluationsToCheckCount;
                    Set<String> evaluationsToCheckMessages = Sets.newHashSet();
                    if (cohortMatch.isPotentiallyEligible()) {
                        List<Evaluation> cohortEvaluations = extractWarnsAndUndetermined(cohortMatch.evaluations());
                        for (Evaluation evaluation : cohortEvaluations) {
                            evaluationsToCheckMessages.addAll(evaluation.warnGeneralMessages());
                            evaluationsToCheckMessages.addAll(evaluation.undeterminedGeneralMessages());
                        }

                        List<Evaluation> trialEvaluations = extractWarnsAndUndetermined(trialMatch.evaluations());
                        for (Evaluation evaluation : trialEvaluations) {
                            evaluationsToCheckMessages.addAll(evaluation.warnGeneralMessages());
                            evaluationsToCheckMessages.addAll(evaluation.undeterminedGeneralMessages());
                        }

                        evaluationsToCheckCount = cohortEvaluations.size() + trialEvaluations.size();
                    } else {
                        List<Evaluation> cohortEvaluations = extractFails(cohortMatch.evaluations());
                        for (Evaluation evaluation : cohortEvaluations) {
                            evaluationsToCheckMessages.addAll(evaluation.failGeneralMessages());
                        }

                        List<Evaluation> trialEvaluations = extractFails(trialMatch.evaluations());
                        for (Evaluation evaluation : trialEvaluations) {
                            evaluationsToCheckMessages.addAll(evaluation.failGeneralMessages());
                        }

                        evaluationsToCheckCount = cohortEvaluations.size() + trialEvaluations.size();
                    }

                    trials.add(ImmutableEvaluatedTrial.builder()
                            .trialId(trialMatch.identification().trialId())
                            .acronym(trialMatch.identification().acronym())
                            .cohort(cohortMatch.metadata().description())
                            .isPotentiallyEligible(cohortMatch.isPotentiallyEligible())
                            .isOpen(cohortMatch.metadata().open())
                            .evaluationsToCheckCount(evaluationsToCheckCount)
                            .evaluationsToCheckMessages(evaluationsToCheckMessages)
                            .build());
                }
            }
        }

        return trials;
    }

    @NotNull
    private static List<Evaluation> extractWarnsAndUndetermined(@NotNull Map<Eligibility, Evaluation> evaluations) {
        return extractEvaluations(evaluations, Sets.newHashSet(EvaluationResult.WARN, EvaluationResult.UNDETERMINED));
    }

    @NotNull
    private static List<Evaluation> extractFails(@NotNull Map<Eligibility, Evaluation> evaluations) {
        return extractEvaluations(evaluations, Sets.newHashSet(EvaluationResult.FAIL));
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
