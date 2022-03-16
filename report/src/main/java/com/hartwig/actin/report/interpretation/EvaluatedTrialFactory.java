package com.hartwig.actin.report.interpretation;

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
import com.hartwig.actin.molecular.datamodel.MolecularEvidence;
import com.hartwig.actin.treatment.datamodel.Eligibility;

import org.jetbrains.annotations.NotNull;

public final class EvaluatedTrialFactory {

    private static final Set<EvaluationResult> WARN_RESULTS = Sets.newHashSet(EvaluationResult.WARN, EvaluationResult.UNDETERMINED);
    private static final Set<EvaluationResult> FAIL_RESULTS = Sets.newHashSet(EvaluationResult.FAIL);

    private EvaluatedTrialFactory() {
    }

    @NotNull
    public static List<EvaluatedTrial> create(@NotNull TreatmentMatch treatmentMatch, @NotNull Set<MolecularEvidence> actinEvidence) {
        List<EvaluatedTrial> trials = Lists.newArrayList();

        for (TrialEligibility trialMatch : treatmentMatch.trialMatches()) {
            Set<String> trialWarnings = extractWarnings(trialMatch.evaluations());
            Set<String> trialFails = extractFails(trialMatch.evaluations());

            ImmutableEvaluatedTrial.Builder builder = ImmutableEvaluatedTrial.builder()
                    .trialId(trialMatch.identification().trialId())
                    .acronym(trialMatch.identification().acronym())
                    .hasMolecularEvidence(hasEvidenceForTreatment(actinEvidence, trialMatch.identification().acronym()));

            for (CohortEligibility cohortMatch : trialMatch.cohorts()) {
                Set<String> cohortWarnings = extractWarnings(cohortMatch.evaluations());
                Set<String> cohortFails = extractFails(cohortMatch.evaluations());

                if (cohortMatch.metadata().blacklist()) {
                    cohortFails.add("Cohort blacklisted");
                }

                trials.add(builder.cohort(cohortMatch.metadata().description())
                        .isPotentiallyEligible(cohortMatch.isPotentiallyEligible())
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

        trials.sort(new EvaluatedTrialComparator());

        return trials;
    }

    private static boolean hasEvidenceForTreatment(@NotNull Iterable<MolecularEvidence> evidences, @NotNull String treatmentToFind) {
        for (MolecularEvidence evidence : evidences) {
            if (evidence.treatment().equals(treatmentToFind)) {
                return true;
            }
        }

        return false;
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
