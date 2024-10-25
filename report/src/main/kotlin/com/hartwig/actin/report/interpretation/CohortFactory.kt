package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.algo.CohortMatch
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.algo.TreatmentMatch
import com.hartwig.actin.datamodel.algo.TrialMatch
import com.hartwig.actin.datamodel.trial.CohortMetadata
import com.hartwig.actin.datamodel.trial.Eligibility

object CohortFactory {
    fun createEvaluableCohorts(treatmentMatch: TreatmentMatch, filterOnSOCExhaustionAndTumorType: Boolean): List<Cohort> {
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
            val missingGenesForTrial = trialMatch.evaluations.values.any { it.isMissingGenesForSufficientEvaluation }
            // Handle case of trial without cohorts.
            if (trialMatch.cohorts.isEmpty()) {
                listOf(
                    Cohort(
                        trialId = trialId,
                        acronym = acronym,
                        name = null,
                        molecularEvents = trialInclusionEvents,
                        isPotentiallyEligible = trialMatch.isPotentiallyEligible,
                        isMissingGenesForSufficientEvaluation = missingGenesForTrial,
                        isOpen = trialIsOpen,
                        hasSlotsAvailable = trialIsOpen,
                        warnings = trialWarnings,
                        fails = trialFails,
                        phase = phase,
                    )
                )
            } else {
                filteredMatches(
                    trialMatch.cohorts, filterOnSOCExhaustionAndTumorType, CohortMatch::evaluations
                ).map { cohortMatch: CohortMatch ->
                    Cohort(
                        trialId = trialId,
                        acronym = acronym,
                        name = cohortMatch.metadata.description,
                        molecularEvents = trialInclusionEvents.union(extractInclusionEvents(cohortMatch.evaluations)),
                        isPotentiallyEligible = cohortMatch.isPotentiallyEligible,
                        isMissingGenesForSufficientEvaluation = missingGenesForTrial ||
                                cohortMatch.evaluations.values.any { it.isMissingGenesForSufficientEvaluation },
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

    fun createNonEvaluableCohorts(treatmentMatch: TreatmentMatch): List<Cohort> {
        return treatmentMatch.trialMatches.flatMap { trialMatch: TrialMatch ->
            val identification = trialMatch.identification
            if (trialMatch.nonEvaluableCohorts.isEmpty()) {
                emptyList()
            } else {
                trialMatch.nonEvaluableCohorts.map { cohortMetadata: CohortMetadata ->
                    Cohort(
                        trialId = identification.trialId,
                        acronym = identification.acronym,
                        name = cohortMetadata.description,
                        isOpen = identification.open && cohortMetadata.open,
                        hasSlotsAvailable = cohortMetadata.slotsAvailable,
                        ignore = cohortMetadata.ignore,
                        phase = identification.phase
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