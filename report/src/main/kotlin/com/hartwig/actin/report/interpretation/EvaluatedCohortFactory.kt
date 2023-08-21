package com.hartwig.actin.report.interpretation

import com.google.common.collect.Ordering
import com.google.common.collect.Sets
import com.hartwig.actin.algo.datamodel.CohortMatch
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.datamodel.TreatmentMatch
import com.hartwig.actin.algo.datamodel.TrialMatch
import com.hartwig.actin.treatment.datamodel.Eligibility
import java.util.stream.Collectors
import java.util.stream.Stream

object EvaluatedCohortFactory {
    fun create(treatmentMatch: TreatmentMatch): List<EvaluatedCohort> {
        return treatmentMatch.trialMatches().stream().flatMap { trialMatch: TrialMatch ->
            val trialWarnings = extractWarnings(trialMatch.evaluations())
            val trialFails = extractFails(trialMatch.evaluations())
            val trialInclusionEvents = extractInclusionEvents(trialMatch.evaluations())
            val builder = ImmutableEvaluatedCohort.builder()
                .trialId(trialMatch.identification().trialId())
                .acronym(trialMatch.identification().acronym())
            val trialIsOpen = trialMatch.identification().open()
            // Handle case of trial without cohorts.
            if (trialMatch.cohorts().isEmpty()) {
                return@flatMap Stream.of(
                    builder.cohort(null)
                        .molecularEvents(trialInclusionEvents)
                        .isPotentiallyEligible(trialMatch.isPotentiallyEligible)
                        .isOpen(trialIsOpen)
                        .hasSlotsAvailable(trialIsOpen)
                        .warnings(trialWarnings)
                        .fails(trialFails)
                        .build()
                )
            } else {
                return@flatMap trialMatch.cohorts()
                    .stream()
                    .map { cohortMatch: CohortMatch ->
                        builder.cohort(cohortMatch.metadata().description())
                            .molecularEvents(Sets.union(trialInclusionEvents, extractInclusionEvents(cohortMatch.evaluations())))
                            .isPotentiallyEligible(cohortMatch.isPotentiallyEligible)
                            .isOpen(trialIsOpen && cohortMatch.metadata().open() && !cohortMatch.metadata().blacklist())
                            .hasSlotsAvailable(cohortMatch.metadata().slotsAvailable())
                            .warnings(Sets.union(trialWarnings, extractWarnings(cohortMatch.evaluations())))
                            .fails(Sets.union(trialFails, extractFails(cohortMatch.evaluations())))
                            .build()
                    }
            }
        }.sorted(EvaluatedCohortComparator()).collect(Collectors.toList())
    }

    private fun extractInclusionEvents(evaluationMap: Map<Eligibility, Evaluation>): Set<String> {
        val inclusionEvents: MutableSet<String> = Sets.newTreeSet(Ordering.natural())
        for (evaluation in evaluationMap.values) {
            inclusionEvents.addAll(evaluation.inclusionMolecularEvents())
        }
        return inclusionEvents
    }

    private fun extractWarnings(evaluationMap: Map<Eligibility, Evaluation>): Set<String> {
        val messages: MutableSet<String> = Sets.newTreeSet(Ordering.natural())
        for (evaluation in evaluationMap.values) {
            val isRecoverableFail = evaluation.result() == EvaluationResult.FAIL && evaluation.recoverable()
            val isWarn = evaluation.result() == EvaluationResult.WARN
            val isUnrecoverableUndetermined = evaluation.result() == EvaluationResult.UNDETERMINED && !evaluation.recoverable()
            if (isRecoverableFail || isWarn || isUnrecoverableUndetermined) {
                messages.addAll(evaluation.failGeneralMessages())
                messages.addAll(evaluation.warnGeneralMessages())
                messages.addAll(evaluation.undeterminedGeneralMessages())
            }
        }
        return messages
    }

    private fun extractFails(evaluations: Map<Eligibility, Evaluation>): Set<String> {
        val messages: MutableSet<String> = Sets.newTreeSet(Ordering.natural())
        for (evaluation in evaluations.values) {
            if (evaluation.result() == EvaluationResult.FAIL && !evaluation.recoverable()) {
                messages.addAll(evaluation.failGeneralMessages())
            }
        }
        return messages
    }
}