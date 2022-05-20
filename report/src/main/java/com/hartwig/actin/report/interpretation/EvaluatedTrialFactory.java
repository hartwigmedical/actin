package com.hartwig.actin.report.interpretation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.algo.datamodel.CohortMatch;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.algo.datamodel.TrialMatch;
import com.hartwig.actin.molecular.datamodel.evidence.ActinTrialEvidence;
import com.hartwig.actin.treatment.datamodel.Eligibility;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class EvaluatedTrialFactory {

    private EvaluatedTrialFactory() {
    }

    @NotNull
    public static List<EvaluatedTrial> create(@NotNull TreatmentMatch treatmentMatch, @NotNull Set<ActinTrialEvidence> actinEvidence) {
        List<EvaluatedTrial> trials = Lists.newArrayList();

        for (TrialMatch trialMatch : treatmentMatch.trialMatches()) {
            Set<String> trialWarnings = extractWarnings(trialMatch.evaluations());
            Set<String> trialFails = extractFails(trialMatch.evaluations());

            ImmutableEvaluatedTrial.Builder builder = ImmutableEvaluatedTrial.builder()
                    .trialId(trialMatch.identification().trialId())
                    .acronym(trialMatch.identification().acronym());

            String trialAcronym = trialMatch.identification().acronym();
            boolean trialIsOpen = trialMatch.identification().open();
            for (CohortMatch cohortMatch : trialMatch.cohorts()) {
                Set<String> cohortWarnings = extractWarnings(cohortMatch.evaluations());
                Set<String> cohortFails = extractFails(cohortMatch.evaluations());

                trials.add(builder.hasMolecularEvidence(hasEvidenceForTrial(actinEvidence, trialAcronym, cohortMatch.metadata().cohortId()))
                        .cohort(cohortMatch.metadata().description())
                        .isPotentiallyEligible(cohortMatch.isPotentiallyEligible())
                        .isOpen(trialIsOpen && cohortMatch.metadata().open() && !cohortMatch.metadata().blacklist())
                        .hasSlotsAvailable(cohortMatch.metadata().slotsAvailable())
                        .warnings(Sets.union(cohortWarnings, trialWarnings))
                        .fails(Sets.union(cohortFails, trialFails))
                        .build());
            }

            // Handle case of trial without cohorts.
            if (trialMatch.cohorts().isEmpty()) {
                trials.add(builder.hasMolecularEvidence(hasEvidenceForTrial(actinEvidence, trialAcronym, null))
                        .cohort(null)
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

    private static boolean hasEvidenceForTrial(@NotNull Iterable<ActinTrialEvidence> evidences, @NotNull String trialAcronymToFind,
            @Nullable String cohortIdToFind) {
        for (ActinTrialEvidence evidence : evidences) {
            boolean isCohortMatch;
            if (evidence.cohortId() == null) {
                isCohortMatch = cohortIdToFind == null;
            } else {
                isCohortMatch = evidence.cohortId().equals(cohortIdToFind);
            }

            if (evidence.trialAcronym().equals(trialAcronymToFind) && isCohortMatch) {
                return true;
            }
        }

        return false;
    }

    @NotNull
    private static Set<String> extractWarnings(@NotNull Map<Eligibility, Evaluation> evaluationMap) {
        Set<String> messages = Sets.newHashSet();
        for (Evaluation evaluation : evaluationMap.values()) {
            if (evaluation.result() == EvaluationResult.WARN) {
                messages.addAll(evaluation.warnGeneralMessages());
            } else if (evaluation.result() == EvaluationResult.UNDETERMINED) {
                messages.addAll(evaluation.undeterminedGeneralMessages());
            } else if (evaluation.result() == EvaluationResult.FAIL && evaluation.recoverable()) {
                messages.addAll(evaluation.failGeneralMessages());
            }
        }
        return messages;
    }

    @NotNull
    private static Set<String> extractFails(@NotNull Map<Eligibility, Evaluation> evaluations) {
        Set<String> messages = Sets.newHashSet();
        for (Evaluation evaluation : evaluations.values()) {
            if (evaluation.result() == EvaluationResult.FAIL && !evaluation.recoverable()) {
                messages.addAll(evaluation.failGeneralMessages());
            }
        }
        return messages;
    }
}
