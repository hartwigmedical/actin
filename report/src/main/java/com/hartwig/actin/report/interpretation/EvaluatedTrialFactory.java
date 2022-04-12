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
import com.hartwig.actin.molecular.datamodel.evidence.EvidenceEntry;
import com.hartwig.actin.treatment.datamodel.Eligibility;

import org.jetbrains.annotations.NotNull;

public final class EvaluatedTrialFactory {

    private EvaluatedTrialFactory() {
    }

    @NotNull
    public static List<EvaluatedTrial> create(@NotNull TreatmentMatch treatmentMatch, @NotNull Set<EvidenceEntry> actinEvidence) {
        List<EvaluatedTrial> trials = Lists.newArrayList();

        for (TrialMatch trialMatch : treatmentMatch.trialMatches()) {
            Set<String> trialWarnings = extractWarnings(trialMatch.evaluations());
            Set<String> trialFails = extractFails(trialMatch.evaluations());

            ImmutableEvaluatedTrial.Builder builder = ImmutableEvaluatedTrial.builder()
                    .trialId(trialMatch.identification().trialId())
                    .acronym(trialMatch.identification().acronym())
                    .hasMolecularEvidence(hasEvidenceForTreatment(actinEvidence, trialMatch.identification().acronym()));

            for (CohortMatch cohortMatch : trialMatch.cohorts()) {
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

    private static boolean hasEvidenceForTreatment(@NotNull Iterable<EvidenceEntry> evidences, @NotNull String treatmentToFind) {
        for (EvidenceEntry evidence : evidences) {
            if (evidence.treatment().equals(treatmentToFind)) {
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
