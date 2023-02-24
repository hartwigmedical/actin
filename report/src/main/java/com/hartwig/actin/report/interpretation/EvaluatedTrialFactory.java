package com.hartwig.actin.report.interpretation;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.treatment.datamodel.Eligibility;
import org.jetbrains.annotations.NotNull;

public final class EvaluatedTrialFactory {

    private EvaluatedTrialFactory() {
    }

    @NotNull
    public static List<EvaluatedTrial> create(@NotNull TreatmentMatch treatmentMatch) {
        return treatmentMatch.trialMatches().stream().flatMap(trialMatch -> {
            Set<String> trialWarnings = extractWarnings(trialMatch.evaluations());
            Set<String> trialFails = extractFails(trialMatch.evaluations());
            Set<String> trialInclusionEvents = extractInclusionEvents(trialMatch.evaluations());

            String trialAcronym = trialMatch.identification().acronym();
            ImmutableEvaluatedTrial.Builder builder =
                    ImmutableEvaluatedTrial.builder().trialId(trialMatch.identification().trialId()).acronym(trialAcronym);

            boolean trialIsOpen = trialMatch.identification().open();
            // Handle case of trial without cohorts.
            if (trialMatch.cohorts().isEmpty()) {
                return Stream.of(builder.cohort(null)
                        .molecularEvents(trialInclusionEvents)
                        .isPotentiallyEligible(trialMatch.isPotentiallyEligible())
                        .isOpen(trialIsOpen)
                        .hasSlotsAvailable(trialIsOpen)
                        .warnings(trialWarnings)
                        .fails(trialFails)
                        .build());
            } else {
                return trialMatch.cohorts().stream().map(cohortMatch ->
                    builder.cohort(cohortMatch.metadata().description())
                            .molecularEvents(Sets.union(trialInclusionEvents, extractInclusionEvents(cohortMatch.evaluations())))
                            .isPotentiallyEligible(cohortMatch.isPotentiallyEligible())
                            .isOpen(trialIsOpen && cohortMatch.metadata().open() && !cohortMatch.metadata().blacklist())
                            .hasSlotsAvailable(cohortMatch.metadata().slotsAvailable())
                            .warnings(Sets.union(extractWarnings(cohortMatch.evaluations()), trialWarnings))
                            .fails(Sets.union(extractFails(cohortMatch.evaluations()), trialFails))
                            .build()
                );
            }
        })
                .sorted(new EvaluatedTrialComparator())
                .collect(Collectors.toList());
    }

    @NotNull
    private static Set<String> extractInclusionEvents(@NotNull Map<Eligibility, Evaluation> evaluationMap) {
        Set<String> inclusionEvents = Sets.newTreeSet(Ordering.natural());

        for (Evaluation evaluation : evaluationMap.values()) {
            inclusionEvents.addAll(evaluation.inclusionMolecularEvents());
        }

        return inclusionEvents;
    }

    @NotNull
    private static Set<String> extractWarnings(@NotNull Map<Eligibility, Evaluation> evaluationMap) {
        Set<String> messages = Sets.newTreeSet(Ordering.natural());
        for (Evaluation evaluation : evaluationMap.values()) {
            boolean isRecoverableFail = evaluation.result() == EvaluationResult.FAIL && evaluation.recoverable();
            boolean isWarn = evaluation.result() == EvaluationResult.WARN;
            boolean isUnrecoverableUndetermined = evaluation.result() == EvaluationResult.UNDETERMINED && !evaluation.recoverable();

            if (isRecoverableFail || isWarn || isUnrecoverableUndetermined) {
                messages.addAll(evaluation.failGeneralMessages());
                messages.addAll(evaluation.warnGeneralMessages());
                messages.addAll(evaluation.undeterminedGeneralMessages());
            }
        }
        return messages;
    }

    @NotNull
    private static Set<String> extractFails(@NotNull Map<Eligibility, Evaluation> evaluations) {
        Set<String> messages = Sets.newTreeSet(Ordering.natural());
        for (Evaluation evaluation : evaluations.values()) {
            if (evaluation.result() == EvaluationResult.FAIL && !evaluation.recoverable()) {
                messages.addAll(evaluation.failGeneralMessages());
            }
        }
        return messages;
    }
}
