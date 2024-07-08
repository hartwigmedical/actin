package com.hartwig.actin.report.interpretation

import com.hartwig.actin.algo.datamodel.CohortMatch
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.datamodel.TreatmentMatch
import com.hartwig.actin.algo.datamodel.TrialMatch
import com.hartwig.actin.trial.datamodel.Eligibility

object EvaluatedCohortFactory {
    fun create(treatmentMatch: TreatmentMatch, filterSOCExhaustionAndTumorType: Boolean): List<EvaluatedCohort> {
        return treatmentMatch.trialMatches.filter { trialMatch: TrialMatch ->
            val trialWarningsAndFails = extractWarnings(trialMatch.evaluations) + extractFails(trialMatch.evaluations)
            if (filterSOCExhaustionAndTumorType) {!trialWarningsAndFails.any{it.contains("Patient has not exhausted SOC")} && "Tumor type" !in trialWarningsAndFails}
            else true
        }.flatMap { trialMatch: TrialMatch ->
            val trialWarnings = extractWarnings(trialMatch.evaluations)
            val trialFails = extractFails(trialMatch.evaluations)
            val trialInclusionEvents = extractInclusionEvents(trialMatch.evaluations)
            val identification = trialMatch.identification
            val trialId = identification.trialId
            val acronym = identification.acronym
            val trialIsOpen = identification.open
            val phase = identification.phase
            // Handle case of trial without cohorts.
            if (trialMatch.cohorts.isEmpty()) {
                listOf(
                    EvaluatedCohort(
                        trialId = trialId,
                        acronym = acronym,
                        cohort = null,
                        molecularEvents = trialInclusionEvents,
                        isPotentiallyEligible = trialMatch.isPotentiallyEligible,
                        isOpen = trialIsOpen,
                        hasSlotsAvailable = trialIsOpen,
                        warnings = trialWarnings,
                        fails = trialFails,
                        phase = phase
                    )
                )
            } else {
                trialMatch.cohorts
                    .map { cohortMatch: CohortMatch ->
                        EvaluatedCohort(
                            trialId = trialId,
                            acronym = acronym,
                            cohort = cohortMatch.metadata.description,
                            molecularEvents = trialInclusionEvents.union(extractInclusionEvents(cohortMatch.evaluations)),
                            isPotentiallyEligible = cohortMatch.isPotentiallyEligible,
                            isOpen = trialIsOpen && cohortMatch.metadata.open && !cohortMatch.metadata.blacklist,
                            hasSlotsAvailable = cohortMatch.metadata.slotsAvailable,
                            warnings = trialWarnings.union(extractWarnings(cohortMatch.evaluations)),
                            fails = trialFails.union(extractFails(cohortMatch.evaluations)),
                            phase = phase
                        )
                    }
            }
        }.sortedWith(EvaluatedCohortComparator())
    }

    private fun extractInclusionEvents(evaluationMap: Map<Eligibility, Evaluation>): Set<String> {
        return evaluationMap.values.flatMap(Evaluation::inclusionMolecularEvents).toSet()
    }

    private fun extractWarnings(evaluationMap: Map<Eligibility, Evaluation>): Set<String> {
        return evaluationMap.values.flatMap { evaluation ->
            when {
                evaluation.result == EvaluationResult.FAIL && evaluation.recoverable ->
                    evaluation.failGeneralMessages

                evaluation.result == EvaluationResult.WARN ->
                    evaluation.warnGeneralMessages

                evaluation.result == EvaluationResult.UNDETERMINED && !evaluation.recoverable ->
                    evaluation.undeterminedGeneralMessages

                else -> emptySet()
            }
        }.toSet()
    }

    private fun extractFails(evaluations: Map<Eligibility, Evaluation>): Set<String> {
        return evaluations.values.filter { it.result == EvaluationResult.FAIL && !it.recoverable }
            .flatMap(Evaluation::failGeneralMessages)
            .toSet()
    }
}