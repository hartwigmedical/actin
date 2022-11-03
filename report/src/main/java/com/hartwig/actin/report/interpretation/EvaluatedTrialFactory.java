package com.hartwig.actin.report.interpretation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.hartwig.actin.algo.datamodel.CohortMatch;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.algo.datamodel.TrialMatch;
import com.hartwig.actin.treatment.datamodel.Eligibility;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class EvaluatedTrialFactory {

    private EvaluatedTrialFactory() {
    }

    @NotNull
    public static List<EvaluatedTrial> create(@NotNull TreatmentMatch treatmentMatch) {
        List<EvaluatedTrial> trials = Lists.newArrayList();

        for (TrialMatch trialMatch : treatmentMatch.trialMatches()) {
            Set<String> trialWarnings = extractWarnings(trialMatch.evaluations());
            Set<String> trialFails = extractFails(trialMatch.evaluations());

            String trialAcronym = trialMatch.identification().acronym();
            ImmutableEvaluatedTrial.Builder builder =
                    ImmutableEvaluatedTrial.builder().trialId(trialMatch.identification().trialId()).acronym(trialAcronym);

            boolean trialIsOpen = trialMatch.identification().open();
            for (CohortMatch cohortMatch : trialMatch.cohorts()) {
                Set<String> cohortWarnings = extractWarnings(cohortMatch.evaluations());
                Set<String> cohortFails = extractFails(cohortMatch.evaluations());

                trials.add(builder.cohort(cohortMatch.metadata().description())
                        .molecularEvents(eventsForTrial(trialAcronym, cohortMatch.metadata().cohortId()))
                        .isPotentiallyEligible(cohortMatch.isPotentiallyEligible())
                        .isOpen(trialIsOpen && cohortMatch.metadata().open() && !cohortMatch.metadata().blacklist())
                        .hasSlotsAvailable(cohortMatch.metadata().slotsAvailable())
                        .warnings(Sets.union(cohortWarnings, trialWarnings))
                        .fails(Sets.union(cohortFails, trialFails))
                        .build());
            }

            // Handle case of trial without cohorts.
            if (trialMatch.cohorts().isEmpty()) {
                trials.add(builder.cohort(null)
                        .molecularEvents(eventsForTrial(trialAcronym, null))
                        .isPotentiallyEligible(trialMatch.isPotentiallyEligible())
                        .isOpen(trialIsOpen)
                        .hasSlotsAvailable(trialIsOpen)
                        .warnings(trialWarnings)
                        .fails(trialFails)
                        .build());
            }
        }

        trials.sort(new EvaluatedTrialComparator());

        return trials;
    }

    @NotNull
    private static Set<String> eventsForTrial(@NotNull String trialAcronymToFind, @Nullable String cohortIdToFind) {
        Set<String> events = Sets.newTreeSet(Ordering.natural());
//        for (ActinTrialEvidence evidence : evidences) {
//            boolean isCohortMatch;
//            if (evidence.cohortId() == null) {
//                isCohortMatch = cohortIdToFind == null;
//            } else {
//                isCohortMatch = evidence.cohortId().equals(cohortIdToFind);
//            }
//
//            if (evidence.isInclusionCriterion() && evidence.trialAcronym().equals(trialAcronymToFind) && isCohortMatch) {
//                events.add(evidence.event());
//            }
//        }

        return events;
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
