package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.algo.CohortMatch
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.algo.TreatmentMatch
import com.hartwig.actin.datamodel.algo.TrialMatch
import com.hartwig.actin.datamodel.trial.CohortMetadata
import com.hartwig.actin.datamodel.trial.Eligibility

object CohortFactory {
    fun create(treatmentMatch: TreatmentMatch, filterOnSOCExhaustionAndTumorType: Boolean): List<Cohort> {
        return filteredMatches(
            treatmentMatch.trialMatches, filterOnSOCExhaustionAndTumorType, TrialMatch::evaluations
        ).flatMap { trialMatch: TrialMatch ->
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
                    Cohort(
                        trialId = trialId,
                        acronym = acronym,
                        cohort = null,
                        molecularEvents = trialInclusionEvents,
                        isPotentiallyEligible = trialMatch.isPotentiallyEligible,
                        isOpen = trialIsOpen,
                        hasSlotsAvailable = trialIsOpen,
                        warnings = trialWarnings,
                        fails = trialFails,
                        phase = phase,
                        ignore = false
                    )
                )
            } else {
                filteredMatches(
                    trialMatch.cohorts, filterOnSOCExhaustionAndTumorType, CohortMatch::evaluations
                ).map { cohortMatch: CohortMatch ->
                    Cohort(
                        trialId = trialId,
                        acronym = acronym,
                        cohort = cohortMatch.metadata.description,
                        molecularEvents = trialInclusionEvents.union(extractInclusionEvents(cohortMatch.evaluations)),
                        isPotentiallyEligible = cohortMatch.isPotentiallyEligible,
                        isOpen = trialIsOpen && cohortMatch.metadata.open,
                        hasSlotsAvailable = cohortMatch.metadata.slotsAvailable,
                        warnings = trialWarnings.union(extractWarnings(cohortMatch.evaluations)),
                        fails = trialFails.union(extractFails(cohortMatch.evaluations)),
                        phase = phase,
                        ignore = cohortMatch.metadata.ignore
                    )
                }
            }
        }.sortedWith(CohortComparator())
    }

    fun createNonEvaluableCohorts(
        treatmentMatch: TreatmentMatch,
        filterOnSOCExhaustionAndTumorType: Boolean
    ): List<Cohort> {
        return filteredMatches(
            treatmentMatch.trialMatches, filterOnSOCExhaustionAndTumorType, TrialMatch::evaluations
        ).flatMap { trialMatch: TrialMatch ->
            val identification = trialMatch.identification
            // Handle case of trial without cohorts.
            if (trialMatch.nonEvaluableCohorts.isEmpty()) {
                emptyList()
            } else {
                trialMatch.nonEvaluableCohorts.map { cohortMetadata: CohortMetadata ->
                    Cohort(
                        trialId = identification.trialId,
                        acronym = identification.acronym,
                        cohort = cohortMetadata.description,
                        molecularEvents = emptySet(),
                        isPotentiallyEligible = false,
                        isOpen = identification.open && cohortMetadata.open,
                        hasSlotsAvailable = cohortMetadata.slotsAvailable,
                        warnings = emptySet(),
                        fails = emptySet(),
                        phase = null,
                        ignore = cohortMetadata.ignore
                    )
                }
            }
        }.sortedWith(CohortComparator())
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

    private fun <T> filteredMatches(
        matches: List<T>, filterOnSOCExhaustionAndTumorType: Boolean, evaluations: (T) -> Map<Eligibility, Evaluation>
    ) = if (!filterOnSOCExhaustionAndTumorType) matches else {
        matches.filter {
            val trialWarningsAndFails = extractWarnings(evaluations(it)) + extractFails(evaluations(it))
            !trialWarningsAndFails.any { trialWarningOrFail -> trialWarningOrFail.contains("Patient has not exhausted SOC") } && "Tumor type" !in trialWarningsAndFails
        }
    }

    private fun extractFails(evaluations: Map<Eligibility, Evaluation>): Set<String> {
        return evaluations.values.filter { it.result == EvaluationResult.FAIL && !it.recoverable }
            .flatMap(Evaluation::failGeneralMessages)
            .toSet()
    }
}